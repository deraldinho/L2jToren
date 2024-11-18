package net.sf.l2j.gameserver.autofarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmType;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmArea;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.extractable.ExtractableProductItem;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillCreateItem;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public class AutoFarmRoutine
{
	private final AutoFarmProfile _fp;
	private final Map<String, Integer> _cachedMessages = new HashMap<>();
	private boolean _noAttackSkillItems;
	private int _skillAttackFailCount;
	
	public AutoFarmRoutine(AutoFarmProfile fp)
	{
		_fp = fp;
	}
	
	public synchronized void start()
	{
		if (!_fp.isEnabled())
			return;
		
		if (!Config.AUTOFARM_ENABLED)
		{
			stop("Sistema não pode ser utilizado agora");
			return;
		}
		
		final Player player = _fp.getPlayer();
		if (player.isDead())
		{
			stop("Seu character morreu");
			return;
		}
		
		if (_skillAttackFailCount >= 20)
		{
			stop("Seu character não consegue atacar mais");
			return;
		}
		
		if (_fp.getEndTime() != 0 && _fp.getFinalEndTime() < System.currentTimeMillis())
		{
			// Se ainda estiver atacando, será encerrado assim que o monstro for morto
			if (!isPlayerAttacking(player))
				stop("Duração programada foi concluída");
			
			return;
		}
		
		run(player);
	}
	
	public synchronized void stop(String msg)
	{
		_skillAttackFailCount = 0;
		_cachedMessages.clear();
		AutoFarmManager.getInstance().stopPlayer(_fp, msg);
		onEnd();
	}
	
	private synchronized void run(Player player)
	{
		final AutoFarmArea area = _fp.getSelectedArea();
		
		// Remover targets inválidos
		if (!(player.getTarget() instanceof Monster monster) || monster.isDead() || (area.getType() == AutoFarmType.ZONA && !monster.isInsideZone(ZoneId.AUTO_FARM)))
			player.setTarget(null);
		
		if (Config.AUTOFARM_SHOW_ROUTE_RANGE && area.getType() != AutoFarmType.ZONA)
			showRangePreview(player);
		
		// Não há o que fazer
		if (player.isSleeping() || player.isStunned() || player.isImmobilized())
		{
			sendAdminMessage(player, "Immobilized");
			return;
		}
		
		// Alta prioridade porque muitas ações dependem deles
		if (_fp.useAutoPotion())
		{
			testHpPotions(player);
			testMpPotions(player);
		}
		
		// Antes de começar a atacar, nós nos preparamos
		if (testHealSkill(player) || testBuffSkill(player))
		{
			sendAdminMessage(player, "Healing");
			return;
		}
		
		if (player.getAI().getCurrentIntention().getType() == IntentionType.PICK_UP && player.getMove().getTask() != null)
		{
			sendAdminMessage(player, "Picking Herbs");
			return;
		}
		
		// Não está dentro da zona. Possiveis cenários:
		// Character pode estar no caminho para a área
		// Está atacando um monstro pelo lado de fora da área
		// O player forçou o movimento do character
		if (area.getType() == AutoFarmType.ZONA && !isPlayerAttacking(player))
		{
			// Se saiu para atacar nós permitimos, pode ter feito por ser o caminho mais curto
			// Mas se saiu e não tem a intenção de atacar, então deve voltar
			// Não é interessante sempre forçar a volta porque o novo destino pode ser algum não interessante, portanto dependeria de mais análises
			
			if (!player.isInsideZone(ZoneId.AUTO_FARM))
			{
				if (area.getFarmZone().tryGoBackInside())
					sendMessage(player, "retornando para o interior da zona");
				else
				{
					// Não foi possí­vel fazer nada
					stop("Character fora da zona");
				}
				
				return;
			}

			if (area.isOwnerNearEdge(50) && area.getFarmZone().tryGoBackInside())
				sendMessage(player, "voltando para o interior da zona");
		}
		else if (area.getType() == AutoFarmType.ROTA)
		{
			// Primeiro devemos chegar no caminho da rota
			if (!area.getRouteZone().reachedFirstNode())
			{
				area.getRouteZone().moveToNextPoint();
				return;
			}
			
			// Agora podemos voltar
			if (!area.isOwnerNearEdge(50))
			{
				area.getRouteZone().moveToNextPoint();
				return;
			}
		}
		
		// Se não pegarmos a erva antes, ela vai desaparecer
		if (_fp.pickHerbs() && tryPickUpHerbs(player))
			return;
		
		if (!isPlayerAttacking(player) && (player.getAI().getCurrentIntention().getType() != IntentionType.ATTACK || !GeoEngine.getInstance().canMoveAround(player.getX(), player.getY(), player.getZ())))
		{
			for (Monster monster : getTarget(true))
			{
				// Target está em uma distāncia compatível, é visível e o player consegue movimentar-se na sua direção
				if (monster.getAI().getAggroList().getHate(player) > 0 || (player.distance3D(monster) < _fp.getAttackRange() && GeoEngine.getInstance().canSeeTarget(monster, player) && GeoEngine.getInstance().canMoveToTarget(monster, player)))
				{
					sendAdminMessage(player, "New target: " + monster.getName());
					player.setTarget(monster);
					break;
				}
				
				if (!area.isMovementAllowed())
					continue;

				// Não achamos um novo target e já estamos em rota
				if (player.getMove().getTask() != null)
					continue;
				
				// Se não estamos andando em direção a algum monstro, testamos essa posibilidade
				if (player.getMove().maybeMoveToLocation(monster.getPosition().clone(), 0, true, false))
				{
					sendAdminMessage(player, "Needs movement");
					break;
				}
			}
		}
		
		// Temos que verificar se o target está visível novamente porque ele pode se mover
		if (player.getTarget() != null && player.distance3D(player.getTarget().getPosition()) <= _fp.getAttackRange() && GeoEngine.getInstance().canSeeTarget(player, player.getTarget()))
		{
			final Monster _target = player.getTarget().getMonster();
			final L2Skill skill = getCastSkill(player);
			
			if (skill != null) // && !iscasting?
			{
				player.getAI().tryToCast(_target, skill);
				sendAdminMessage(player, "Casting");
			}
			else if (!player.isMageClass() || (player.isMageClass() && _fp.getAttackSkills().isEmpty()))
			{
				player.getAI().tryToAttack(_target);
				sendAdminMessage(player, "Attacking");
			}
			else if (_noAttackSkillItems)
			{
				_skillAttackFailCount++;
				sendAdminMessage(player, "Skill cast fail " + _skillAttackFailCount);
			}
				
			_cachedMessages.clear();
		}
		else if (player.getMove().getTask() == null && !player.isRooted() && !isPlayerAttacking(player))
		{
			if (area.getType() == AutoFarmType.ROTA)
			{
				area.getRouteZone().moveToNextPoint();
				return;
			}
			
			if (player.getTarget() == null && player.getAI().getCurrentIntention().getType() == IntentionType.IDLE && player.getAI().getNextIntention().getType() == IntentionType.IDLE)
				trySendMessage(player, "aguardando o spawn de novos monstros.");
		}
	}
	
	private synchronized void onEnd()
	{
		if (_fp.getMacro() == null)
			return;
		
		final Player player = _fp.getPlayer();
		if (player.isInCombat())
		{
			AttackStanceTaskManager.getInstance().remove(player);
			player.broadcastPacket(new AutoAttackStop(player.getObjectId()));
		}
		
		switch (_fp.getMacro())
		{
			case ESCAPE:
				player.getAI().tryToCast(player, 2099, 1);
				break;
				
			case LOGOUT:
				player.logout(true);
				break;
				
			case ITEM:
				final ItemInstance item = player.getInventory().getItemByItemId(_fp.getMacroAdditionalId());
				if (item == null)
				{
					player.sendMessage("Não foi possí­vel executar o macro. O item não está disponí­vel.");
					break;
				}
				
				final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
				handler.useItem(player, item, false);
				break;
				
			case SKILL:
				final L2Skill skill = player.getSkill(_fp.getMacroAdditionalId());
				if (skill == null || player.isSkillDisabled(skill))
				{
					player.sendMessage("Não foi possí­vel executar o macro. A skill não está disponí­vel.");
					break;
				}
				
				player.getAI().tryToCast(player, skill);
		}
	}

	/*
	 * Usamos canSeeTarget ao invés de canMoveToTarget porque para atacar o pathfind não é utilizado
	 */
	private List<Monster> getTarget(boolean checkGeoEngine)
	{
		final Player player = _fp.getPlayer();
		final List<Monster> targets = new ArrayList<>();
		
		for (Monster monster : _fp.getSelectedArea().getMonsters())
		{
			if (monster.isDead())
				continue;
			
			if (monster.isRaidRelated() && !_fp.attackRaid())
				continue;
			
			if (!_fp.getTargets().isEmpty() && !_fp.getTargets().contains(monster.getName()))
				continue;
			
			targets.add(monster);
		}
		
		targets.sort(Comparator.comparingDouble(t -> player.distance3D(t)));
		return targets;
	}
	
	private L2Skill getCastSkill(Player player)
	{
		// Chance de debuff
		if (Rnd.get(100) <= Config.AUTOFARM_DEBUFF_CHANCE || !player.getTarget().getMonster().getAI().getAggroList().contains(player))
		{
			final L2Skill debuff = getAttackSkill(player, true);
			if (debuff != null)
				return debuff;
		}
		
		// Fighter tem o ataque fí­sico
		if (!player.isMageClass() && Rnd.nextBoolean())
			return null;
		
		return getAttackSkill(player, false);
	}
	
	private L2Skill getAttackSkill(Player player, boolean debuff)
	{
		final List<L2Skill> skills = _fp.getAttackSkills(debuff);
		Collections.shuffle(skills);
		
		for (int i = 0; i < skills.size(); i++)
		{
			final L2Skill skill = Rnd.get(skills);
			if (player.isSkillDisabled(skill))
				continue;
			
			if (player.distance3D(player.getTarget()) > (skill.getCastRange() > 0 ? skill.getCastRange() : skill.getSkillRadius()))
				continue;
			
			// Essa skill Critical Blow da classe Adventurer é tipo um buff
			if (skill.getId() == 409 && player.getFirstEffect(skill) != null)
				continue;
			
			if (skill.getItemConsumeId() != 0)
			{
				// Salvamos o estado para verificar se o player pode continuar farmando
				final ItemInstance consumable = player.getInventory().getItemByItemId(skill.getItemConsumeId());
				_noAttackSkillItems = consumable == null || consumable.getCount() < skill.getItemConsume();
				
				if (_noAttackSkillItems)
					continue;
			}
			
			if (!debuff)
				return skill;
			
			if (player.getTarget().getMonster().getFirstEffect(skill) == null)
				return skill;
		}
		
		return null;
	}
	
	/*
	 * Caso necessário, retorna uma skill para curar o HP ou MP
	 */
	private boolean testHealSkill(Player player)
	{
		// O life tem prioridade
		if (player.getStatus().getHpRatio() < Config.AUTOFARM_HP_HEAL_RATE)
		{
			for (L2Skill skill : _fp.getHpHealSkills())
			{
				if (!player.isSkillDisabled(skill))
				{
					player.getAI().tryToCast(player, skill);
					return true;
				}
			}
		}
		
		if (player.getStatus().getMpRatio() < Config.AUTOFARM_MP_HEAL_RATE)
		{
			for (L2Skill skill : _fp.getMpHealSkills())
			{
				if (!player.isSkillDisabled(skill))
				{
					player.getAI().tryToCast(player, skill);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean testBuffSkill(Player player)
	{
		if (player.getCast().isCastingNow())
			return false;
		
		// A ordem das skills do player pode criar prioridade nesse caso
		for (L2Skill skill : _fp.getBuffSkills())
		{
			if (skill.hasEffects() && player.getFirstEffect(skill) != null || player.isSkillDisabled(skill))
				continue;

			if (skill.getItemConsumeId() != 0)
			{
				final ItemInstance consumable = player.getInventory().getItemByItemId(skill.getItemConsumeId());
				if (consumable == null || consumable.getCount() < skill.getItemConsume())
					continue;
			}
			
			if (skill.getSkillType() == SkillType.CREATE_ITEM || skill.getSkillType() == SkillType.EXTRACTABLE)
			{
				if (testCreateItemSkill(player, skill))
					return true;
				
				continue;
			}
			else if (skill.getSkillType() == SkillType.NEGATE)
			{
				if (testNegateSkill(player, skill))
					return true;
				
				continue;
			}
			
			player.getAI().tryToCast(player, skill);
			return true;
		}
		
		return false;
	}
	
	private static boolean testNegateSkill(Player player, L2Skill skill)
	{
		boolean doCast = false;
		for (AbstractEffect effect : player.getAllEffects())
		{
			if (ArraysUtil.contains(skill.getNegateId(), effect.getSkill().getId()))
				doCast = true;
			else
			{
				// Disablers.java
				for (SkillType skillType : skill.getNegateStats())
				{
					final L2Skill effectSkill = effect.getSkill();
					if (effectSkill.getEffectType() != null && effectSkill.getEffectAbnormalLvl() >= 0)
					{
						if (effectSkill.getEffectType() == skillType && effectSkill.getEffectAbnormalLvl() <= skill.getNegateLvl())
							doCast = true;
					}
					else if (effectSkill.getSkillType() == skillType && effectSkill.getAbnormalLvl() <= skill.getNegateLvl())
						doCast = true;
				}
			}
		}
		
		if (doCast)
			player.getAI().tryToCast(player, skill);
		
		return doCast;
	}
	
	private static boolean testCreateItemSkill(Player player, L2Skill skill)
	{
		boolean doCast = false;
		if (skill.getSkillType() == SkillType.CREATE_ITEM)
		{
			// Summon CP Potion
			if (skill.getId() == 1324)
				doCast = true;
			else
			{
				final L2SkillCreateItem createSkill = (L2SkillCreateItem) skill;
				final int createItemCount = createSkill._createItemCount;
				
				for (int createItemId : createSkill._createItemId)
				{
					if (player.getInventory().getItemCount(createItemId) < createItemCount)
						doCast = true;
				}
			}
		}
		else if (skill.getSkillType() == SkillType.EXTRACTABLE) // apenas a Quiver of Arrow
		{
			final ExtractableProductItem extractable = skill.getExtractableSkill().productItems().iterator().next();
			if (player.getInventory().getItemCount(extractable.items().iterator().next().getId()) < 200)
				doCast = true;
		}
		
		if (doCast)
			player.getAI().tryToCast(player, skill);
		
		return doCast;
	}
	
	private static void testHpPotions(Player player)
	{
		if (player.getStatus().getHpRatio() > Config.AUTOFARM_HP_HEAL_RATE)
			return;
		
		testAutoPotions(player, Config.AUTOFARM_HP_POTIONS);
	}
	
	private static void testMpPotions(Player player)
	{
		if (player.getStatus().getMpRatio() > Config.AUTOFARM_MP_HEAL_RATE)
			return;
		
		testAutoPotions(player, Config.AUTOFARM_MP_POTIONS);
	}
	
	private static void testAutoPotions(Player player, int[] ids)
	{
		for (int i : ids)
		{
			final ItemInstance potion = player.getInventory().getItemByItemId(i);
			if (potion == null)
				continue;
			
			if (player.isItemDisabled(potion))
				continue;
			
			boolean useItem = true;
			for (IntIntHolder holder : potion.getEtcItem().getSkills())
			{
				// Essa verificação irá ser feita no handler, mas antecipamos para evitar envio de mensagens de falha
				if (player.isSkillDisabled(holder.getSkill()))
				{
					useItem = false;
					break;
				}
				
				// Efeitos com a mesma stack
				if (holder.getSkill().hasEffects())
				{
					for (AbstractEffect ae : player.getAllEffects())
					{
						for (EffectTemplate effect : holder.getSkill().getEffectTemplates())
						{
							if (ae.getTemplate().getStackType().equals(effect.getStackType()))
							{
								useItem = false;
								break;
							}
						}
					}
				}
			}
			
			if (!useItem)
				continue;
			
			final IItemHandler handler = ItemHandler.getInstance().getHandler(potion.getEtcItem());
			handler.useItem(player, potion, false);
			break; // somente um por vez
		}
	}
	
	private boolean tryPickUpHerbs(Player player)
	{
		if (player.getStatus().getHpRatio() > Config.AUTOFARM_HP_HEAL_RATE && player.getStatus().getMpRatio() > Config.AUTOFARM_MP_HEAL_RATE)
			return false;
		
		for (ItemInstance herb : player.getKnownTypeInRadius(ItemInstance.class, _fp.getFinalRadius() + 100, i -> i.getOwnerId() == player.getObjectId() && i.getName().contains("Herb of Mana") || i.getName().contains("Herb of Life")))
		{
			if (herb.getName().contains("Herb of Life") && player.getStatus().getHpRatio() > Config.AUTOFARM_HP_HEAL_RATE)
				continue;
			
			if (herb.getName().contains("Herb of Mana") && player.getStatus().getMpRatio() > Config.AUTOFARM_MP_HEAL_RATE)
				continue;
			
			player.getAI().tryToPickUp(herb.getObjectId(), false);
			return true;
		}
		
		return false;
	}
	
	private void showRangePreview(Player player)
	{
		if (_fp.getLastPlayerLocation() == null)
			_fp.updatePlayerLocation();
		
		final int currentRadius = _fp.getFinalRadius();
		
		if (currentRadius <= 100)
		{
			ZoneBuilder.getInstance().clearCylinderPreview(player);
		}
		else if (_fp.getFinalRadius() > 100 && (_fp.isRadiusChanged() || player.distance2D(_fp.getLastPlayerLocation()) > 30))
		{
			ZoneBuilder.getInstance().previewCylinder(player, _fp.getFinalRadius());
			_fp.updatePlayerLocation();
		}
	}
	
	private static boolean isPlayerAttacking(Player player)
	{
		return player.getAttack().isAttackingNow() || player.getCast().isCastingNow();
	}
	
	private static void sendAdminMessage(Player player, String msg)
	{
		if (!player.isGM())
			return;
		
		player.sendMessage("AutoFarmLog: " + msg);
	}
	
	private static void sendMessage(Player player, String msg)
	{
		if (!Config.AUTOFARM_SEND_LOG_MESSAGES)
			return;
		
		player.sendMessage(String.format("AutoFarm (%s): %s", new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()), msg));
	}
	
	private void trySendMessage(Player player, String msg)
	{
		if (!Config.AUTOFARM_SEND_LOG_MESSAGES)
			return;
		
		final int count = _cachedMessages.merge(msg, 1, Integer::sum);
		if (count >= 30)
		{
			_cachedMessages.remove(msg);
			return;
		}
		
		if (count == 1 || count >= 30)
			player.sendMessage(String.format("AutoFarm (%s): %s", new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()), msg));
	}
}
