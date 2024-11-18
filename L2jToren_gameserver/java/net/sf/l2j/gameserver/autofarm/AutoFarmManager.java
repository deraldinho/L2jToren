package net.sf.l2j.gameserver.autofarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.sf.l2j.commons.data.Pagination;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmArea;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmRoute;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmZone;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.IconTable;
import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.TownZone;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive.Point;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class AutoFarmManager
{
	public static final int MAX_ROUTE_LINE_LENGTH = 1500;
	
	private static final CLogger LOGGER = new CLogger(AutoFarmManager.class.getName());
	
	private static final String HTML_PATH = "data/html/mods/AutoFarm/";
	private static final String ZONE_NAME_REGEX = "^[A-Za-z0-9]{1,12}$";
	private static final String TARGET_NAME_REGEX = "^[A-Za-z0-9 ]{4,42}$";
	
	private final Map<Integer, AutoFarmProfile> _players = new ConcurrentHashMap<>();
	
	public enum AutoFarmType
	{
		ZONA,
		ROTA,
		OPEN;
	}
	
	public enum AutoFarmMacro
	{
		ESCAPE,
		LOGOUT,
		ITEM,
		SKILL;
	}
	
	public Collection<AutoFarmProfile> getPlayers()
	{
		return _players.values();
	}
	
	/*
	 * Nem sempre é necessário criar uma classe xD
	 */
	public AutoFarmProfile getPlayer(int id)
	{
		return _players.get(id);
	}
	
	public AutoFarmProfile getProfile(Player player)
	{
		return _players.computeIfAbsent(player.getObjectId(), k -> new AutoFarmProfile(player));
	}
	
	public boolean isPlayerAddingLoc(int playerId)
	{
		return _players.containsKey(playerId) && getPlayer(playerId).isAddingLocation();
	}
	
	public boolean isPlayerActive(int playerId)
	{
		return _players.containsKey(playerId) && getPlayer(playerId).isEnabled();
	}
	
	public boolean isRouteFarmActive(int playerId)
	{
		return _players.containsKey(playerId) && getPlayer(playerId).isEnabled() && getPlayer(playerId).getSelectedArea().getType() == AutoFarmType.ROTA;
	}
	
	public void onPlayerLogin(Player player)
	{
		if (getPlayer(player.getObjectId()) != null)
		{
			final AutoFarmProfile fp = getProfile(player);
			fp.updatePlayer(player);
			
			if (!fp.getAreas().isEmpty())
				return;
		}
		
		AutoFarmData.getInstance().restorePlayer(player);
	}
	
	public void handleBypass(Player player, String bypass)
	{
		try
		{
			if (!Config.AUTOFARM_ENABLED)
			{
				showIndexWindow(player, "Sistema desativado no momento");
				return;
			}
			
			if (player.isDead())
				return;

			final StringTokenizer st = new StringTokenizer(bypass, " ");
			if (!st.hasMoreTokens())
				return;
			
			final String command = st.nextToken();
			final AutoFarmProfile fp = getProfile(player);

			// Em todos os outros casos esse modo é desativado
			if (!bypass.startsWith("zb addnode") && !bypass.startsWith("zb lock_new_nodes"))
				fp.setAddingLocation(false);
			
			if (command.equals("index"))
			{
				fp.checkLastClassId();
				showIndexWindow(player, null);
			}
			else if (command.equals("list_areas"))
				handleListAreasBypass(player, fp, st);
			else if (command.equals("skills"))
				handleSkillsBypass(player, fp, st);
			else if (command.equals("targets"))
				handleTargetsBypass(player, fp, st);
			else if (command.equals("macro"))
				handleMacroBypass(player, fp, st);
			else if (command.equals("zb"))
				handleZoneBuilderBypass(player, fp, st);
			else if (command.equals("select_area")) 
				handleSelectAreaBypass(player, fp, Integer.valueOf(st.nextToken()));
			else if (command.equals("timer"))
				handleTimerBypass(player, fp, st);
			else if (command.equals("options"))
				handleAreaOptionsBypass(player, fp, st);
			else if (command.equals("toggle"))
				handleToggleBypass(player, fp, st.nextToken());
			else if (command.equals("clearpreview"))
			{
				if (!fp.isEnabled())
					ZoneBuilder.getInstance().clearAllPreview(player);
			}
			else if (command.equals("help"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile(HTML_PATH + "help/" + st.nextToken());
				player.sendPacket(html);
			}
			else if (command.equals("change_status"))
			{
				if (fp.isEnabled())
					stopPlayer(player, null);
				else
					startPlayer(player);
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Falha ao lidar com o bypass do AutoFarmManager. Player: {}, Bypass: {}", e, player.getName(), bypass);
		}
	}
	
	private void handleToggleBypass(Player player, AutoFarmProfile fp, String action)
	{
		String msg = null;
		
		if (action.equals("autopotion"))
			fp.toggleAutoPotion();
		else if (action.equals("attackraid"))
			fp.toggleAttackRaid();
		else if (action.equals("pickherbs"))
		{
			if (fp.getSelectedArea() != null && !fp.pickHerbs() && !fp.getSelectedArea().isMovementAllowed())
			{
				if (fp.getSelectedArea().getType() == AutoFarmType.ROTA)
					msg = "Opção indisponí­vel para rotas";
				else
					msg = "Incompatível com seu range e o raio da área";
			}
			
			if (msg == null)
				fp.togglePickHerbs();
		}
		
		showIndexWindow(player, msg);
	}
	
	private static void handleTimerBypass(Player player, AutoFarmProfile fp, StringTokenizer st)
	{
		if (st.hasMoreTokens())
		{
			final String action = st.nextToken();
			if (action.equals("set"))
			{
				final int value = st.hasMoreTokens() ? Integer.valueOf(st.nextToken()) : 0;
				if (value < 0)
					return;
				
				if (Config.AUTOFARM_MAX_TIMER != 0 && (value == 0 || value > Config.AUTOFARM_MAX_TIMER))
				{
					showTimerWindow(player, fp, "Escolha um valor entre 1 e " + Config.AUTOFARM_MAX_TIMER);
					return;
				}
				
				fp.setEndTime(value > 0 ? TimeUnit.MINUTES.toMillis(value) : value);
			}
		}
		
		showTimerWindow(player, fp, null);
	}
	
	private static void handleAreaOptionsBypass(Player player, AutoFarmProfile fp, StringTokenizer st)
	{
		String msg = null;
		if (st.hasMoreTokens())
		{
			if (fp.getSelectedArea() == null)
				msg = "Escolha sua área primeiro";
			else if (fp.getSelectedArea().getType() == AutoFarmType.ZONA)
				msg = "Sua área atual é incompatí­vel com essa opção";
			else
			{
				final String action = st.nextToken();
				if (action.equals("radius"))
				{
					final int range = Integer.valueOf(st.nextToken());
					if (range >= 100 && range <= fp.getAreaMaxRadius())
						fp.setRadius(range);
				}
				
				if (!fp.isEnabled())
					ZoneBuilder.getInstance().previewCylinder(player, fp.getFinalRadius());
			}
		}
		
		final Set<Integer> options = new LinkedHashSet<>();
		
		// Exibir a opção selecionada sempre no í­nicio
		options.add(fp.getFinalRadius());

		// Adiciona os múltiplos de 100
		final int maxRange = fp.getAreaMaxRadius();
		for (int i = 100; i <= (maxRange / 100) * 100; i += 100)
			options.add(i);

		// Se o valor não for divisí­vel por 100, adiciona o restante
		if (maxRange % 100 != 0)
			options.add(maxRange);

		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "arealist_options.htm"); 
		html.replace("%current_radius%", fp.getFinalRadius());
		html.replace("%attack_range%", fp.getAttackRange());
		html.replace("%radius%", options.stream().map(String::valueOf).collect(Collectors.joining(";")));
		html.replace("%msg%", msg != null ? msg + "<br>" : "");
		player.sendPacket(html);
	}
	
	private void handleSelectAreaBypass(Player player, AutoFarmProfile fp, int areaId)
	{
		if (fp.getAreaById(areaId) == null)
			return;
		
		if (fp.isEnabled())
		{
			showIndexWindow(player, "Ação indisponí­vel no momento");
			return;
		}
		
		final AutoFarmArea area = fp.getAreaById(areaId);
		
		if (fp.getSelectedAreaId() == areaId)
		{
			fp.setSelectedAreaId(0);
			ZoneBuilder.getInstance().clearAllPreview(player);
			showAreaListWindow(player, area.getType());
			return;
		}
		
		if (area.getType() != AutoFarmType.OPEN && !area.isFromDb())
		{
			showAreaListWindow(player, area.getType(), "Zona inválida");
			return;
		}
		
		if (!canSeeArea(player, area))
		{
			showAreaListWindow(player, area.getType(), "Área muito distante para ser utilizada");
			return;
		}
		
		// Atualizar variáveis antes
		fp.setSelectedAreaId(areaId);
		
		// Temos que fazer isso antes para que a lista de targets esteja pronta quando o player acessá-la
		if (area.getType() == AutoFarmType.ZONA && !area.getFarmZone().isBuilt())
			area.getFarmZone().addToWorld();
		
		if (area.getType() == AutoFarmType.OPEN)
			ZoneBuilder.getInstance().previewCylinder(player, fp.getFinalRadius());
		else
			ZoneBuilder.getInstance().previewFinalArea(fp, areaId);
		
		showAreaListWindow(player, area.getType());
	}
	
	private void handleListAreasBypass(Player player, AutoFarmProfile fp, StringTokenizer st) throws IllegalArgumentException
	{
		if (fp.getBuildingAreaId() != 0 && fp.getBuildingArea().isChanged())
		{
			showConfirmChangeWindow(player, fp.getBuildingArea());
			return;
		}
		
		AutoFarmType type = null;
		if (st.hasMoreTokens())
			type = AutoFarmType.valueOf(st.nextToken());
		else if (fp.getSelectedArea() != null)
			type = fp.getSelectedArea().getType();
		else
			type = AutoFarmType.ZONA;
		
		showAreaListWindow(player, type);
	}
	
	private static void handleMacroBypass(Player player, AutoFarmProfile fp, StringTokenizer st) throws NumberFormatException, IllegalArgumentException, NoSuchElementException
	{
		if (st.hasMoreTokens())
		{
			final String action = st.nextToken();
			if (fp.getEndTime() == 0)
			{
				showMacroWindow(player, fp, "<font color=FFA000>Macros podem ser utilizados quando uma duração é especí­ficada para o AutoFarm.</font>");
			}
			else if (action.equals("set"))
			{
				final AutoFarmMacro macro = AutoFarmMacro.valueOf(st.nextToken());
				if (fp.getMacro() == macro)
					fp.setMacro(null, 0);
				else
					fp.setMacro(macro, Integer.valueOf(st.nextToken()));
				
				showMacroWindow(player, fp, null);
			}
			else if (action.equals("list"))
			{
				final AutoFarmMacro type = AutoFarmMacro.valueOf(st.nextToken());
				if (type == AutoFarmMacro.ITEM)
					showMacroItemWindow(player, Integer.valueOf(st.nextToken()));
				else if (type == AutoFarmMacro.SKILL)
					showMacroSkillWindow(player, Integer.valueOf(st.nextToken()));
			}
		}
		else
			showMacroWindow(player, fp, null);
	}
	
	private void handleZoneBuilderBypass(Player player, AutoFarmProfile fp, StringTokenizer st) throws NumberFormatException
	{
		if (!st.hasMoreTokens())
			return;
		
		if (fp.isEnabled())
		{
			showIndexWindow(player, "Ação indisponí­vel no momento");
			return;
		}
		
		final String action = st.nextToken();
		if (action.equals("new"))
		{
			createArea(player, fp, st);
		}
		else if (action.equals("addnode"))
		{
			newNodeMode(player, fp, false);
		}
		else if (action.equals("lock_new_nodes"))
		{
			newNodeMode(player, fp, true);
		}
		else if (action.equals("removenode"))
		{
			removeAreaNode(player, fp, Integer.valueOf(st.nextToken()));
		}
		else if (action.equals("clearnodes"))
		{
			if (fp.getBuildingArea() == null || ZoneBuilder.getInstance().getPoints(player).isEmpty() && fp.getBuildingArea().getNodes().isEmpty())
				return;
			
			// Algo impede que o preview seja limpo nesse momento, provavelmente algum problema de concorríªncia
			// Essa alternativa foi a mais simples de corrigir
			ThreadPool.execute(() -> ZoneBuilder.getInstance().clearAllPreview(player));

			// O html pode ser exibido antes da conclusão da tarefa acima, então é importante que ele exiba a lista vazia
			ZoneBuilder.getInstance().getPoints(player).clear();
			
			fp.getBuildingArea().setIsChanged(true);
			showEditAreaWindow(player, fp.getBuildingAreaId(), null);
		}
		else if (action.equals("save"))
		{
			saveArea(player, fp);
		}
		else if (action.equals("discart"))
		{
			final AutoFarmArea area = fp.getBuildingArea();
			if (area == null || !area.isChanged())
				return;
			
			area.setIsChanged(false);
			ZoneBuilder.getInstance().clearAllPreview(player);
			showAreaListWindow(player, area.getType());
		}
		else
		{
			final int areaId = Integer.valueOf(st.nextToken());
			if (fp.getAreaById(areaId) == null)
				return;
			
			if (action.equals("delete"))
			{
				deleteArea(player, fp, areaId);
			}
			else if (action.equals("edit"))
			{
				if (fp.getBuildingAreaId() != areaId)
					ZoneBuilder.getInstance().clearAllPreview(player);
				
				fp.setBuildingAreaId(areaId);
				showEditAreaWindow(player, areaId, null);
			}
			else if (action.equals("preview"))
			{
				previewArea(player, fp, areaId, st.hasMoreTokens() ? st.nextToken().equals("FINAL") : false);
			}
		}
	}
	
	private void handleSkillsBypass(Player player, AutoFarmProfile fp, StringTokenizer st) throws NumberFormatException, NoSuchElementException
	{
		if (!st.hasMoreTokens())
			return;
		
		final String action = st.nextToken();
		if (action.equals("page"))
		{
			final int page = Integer.valueOf(st.nextToken());
			if (page == 1 && st.hasMoreTokens())
				fp.setCurrentSkillSlot(Integer.valueOf(st.nextToken()));
			
			showSkillsWindow(player, fp, page);
			return;
		}
		
		final Map<Integer, Integer> skills = fp.getSkills();
		if (action.equals("clear"))
			skills.clear();
		else
		{
			final int skillId = Integer.valueOf(st.nextToken());
			
			if (action.equals("add"))
			{
				if (player.getSkill(skillId) == null)
					return;
				
				if (skills.size() == 6)
				{
					showIndexWindow(player, "Número máximo de skills atingido");
					return;
				}
				
				// Procurar outro slot livre
				if (skills.get(fp.getCurrentSkillSlot()) != null)
				{
					final List<Integer> slots = new ArrayList<>();
					for (int i = 1; i <= 6; i++)
					{
			            if (!skills.containsKey(i))
			            	slots.add(i);
			        }
					
					fp.setCurrentSkillSlot(slots.get(0));
				}
				
				skills.put(fp.getCurrentSkillSlot(), skillId);
			}
			else if (action.equals("remove"))
			{
				final int slot = skills.entrySet().stream().filter(m -> m.getValue() == skillId).map(Entry::getKey).findFirst().orElse(-1);
				if (slot < 0)
					return;
						
				skills.remove(slot);
			}
		}
		
		showSkillsWindow(player, fp, st.hasMoreTokens() ? Integer.valueOf(st.nextToken()) : 1);
	}
	
	private void handleTargetsBypass(Player player, AutoFarmProfile fp, StringTokenizer st) throws NumberFormatException
	{
		if (!st.hasMoreTokens())
			return;
		
		if (fp.getSelectedAreaId() == 0)
		{
			showIndexWindow(player, "Escolha uma área primeiro");
			return;
		}
		
		final String action = st.nextToken();
		if (action.equals("page") && st.hasMoreTokens())
		{
			showTargetsWindow(player, fp, Integer.valueOf(st.nextToken()));
			return;
		}
		
		if (action.equals("clear"))
		{
			fp.getTargets().clear();
		}
		else
		{
			final String targetName = Util.getRemainingTokens(st);
			if (targetName.isEmpty())
				return;
			
			if (action.equals("add"))
			{
				if (!StringUtil.isValidString(targetName, TARGET_NAME_REGEX))
					return;
				
				fp.getTargets().add(targetName);
			}
			else if (action.equals("remove"))
			{
				fp.getTargets().remove(targetName);
			}
		}
		
		showTargetsWindow(player, fp, 1);
	}
	
	
	private static void showMacroItemWindow(Player player, int page)
	{
		final StringBuilder sb = new StringBuilder();
		final Pagination<ItemInstance> pagination = new Pagination<>(player.getInventory().getItems().stream(), page, 7, i -> i.isEtcItem() && i.getEtcItem().getHandlerName() != null);
		
		for (ItemInstance item : pagination)
		{
			String table = HtmCache.getInstance().getHtm(HTML_PATH + "parts/macro_table.htm");
			table = table.replace("%icon%", IconTable.getInstance().getIcon(item.getItemId()));
			table = table.replace("%id%", String.valueOf(item.getItemId()));
			table = table.replace("%name%", item.getName());
			sb.append(table);
		}
		
		if (sb.isEmpty())
			sb.append("<tr><td width=280 align=center><br>Nenhum item compatí­vel disponível</td></tr>");
		if (pagination.getTotalEntries() > 7)
		{
			pagination.generateSpace(36);
			pagination.generatePages("bypass autofarm macro list ITEM %page%");
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "macro_list.htm");
		html.replace("%list%", sb.toString());
		html.replace("%pagination%", pagination.getContent());
		html.replace("%type%", "ITEM");
		player.sendPacket(html);
	}
	
	private static void showMacroSkillWindow(Player player, int page)
	{
		final StringBuilder sb = new StringBuilder();
		final Pagination<L2Skill> pagination = new Pagination<>(player.getSkills().values().stream(), page, 7, s -> !s.isPassive() && !s.isSkillTypeOffensive());
		
		for (L2Skill skill : pagination)
		{
			final int skillId = skill.getId();
			final String icon = (skillId < 100) ? "icon.skill00" + skillId : (skillId < 1000) ? "icon.skill0" + skillId : "icon.skill" + skillId;
			String table = HtmCache.getInstance().getHtm(HTML_PATH + "parts/macro_table.htm");
			table = table.replace("%icon%", icon);
			table = table.replace("%id%", String.valueOf(skillId));
			table = table.replace("%name%", skill.getName());
			sb.append(table);
		}
		
		if (sb.isEmpty())
			sb.append("<tr><td width=280 align=center><br>Nenhuma skill compatí­vel disponível</td></tr>");
		if (pagination.getTotalEntries() > 7)
		{
			pagination.generateSpace(36);
			pagination.generatePages("bypass autofarm macro list SKILL %page%");
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "macro_list.htm");
		html.replace("%list%", sb.toString());
		html.replace("%pagination%", pagination.getContent());
		html.replace("%type%", "SKILL");
		player.sendPacket(html);
	}
	
	private static void showMacroWindow(Player player, AutoFarmProfile fp, String msg)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "macro.htm");
		
		for (AutoFarmMacro macro : AutoFarmMacro.values())
		{
			if (macro == fp.getMacro())
			{
				if (macro == AutoFarmMacro.ITEM)
				{
					html.replace("%item_bypass%", "set ITEM 0");
					html.replace("%item_name%", "(" + ItemData.getInstance().getTemplate(fp.getMacroAdditionalId()).getName() + ")");
				}
				else if (macro == AutoFarmMacro.SKILL)
				{
					html.replace("%skill_bypass%", "set SKILL 0");
					html.replace("%skill_name%", "(" + player.getSkill(fp.getMacroAdditionalId()).getName() + ")");
				}
			}
			html.replace("%" + macro.name() + "%", macro == fp.getMacro() ? "checked" : "unable");
		}
		
		html.replace("%msg%", msg != null ? msg + "<br>" : "");
		html.replace("%skill_bypass%", "list SKILL 1");
		html.replace("%item_bypass%", "list ITEM 1");
		html.replace("%item_name%", "");
		html.replace("%skill_name%", "");
		player.sendPacket(html);
	}
	
	private void showTargetsWindow(Player player, AutoFarmProfile fp, int page)
	{
		if (fp.getSelectedArea() == null)
		{
			showIndexWindow(player, "Escolha uma área primeiro");
			return;
		}
		
		// Por causa da forma como HTML está estruturado, não podemos usar o pagination para salvar a lista
		final StringBuilder sb = new StringBuilder();
		final Pagination<String> pagination = new Pagination<>(fp.getSelectedArea().getMonsterHistory().stream(), page, 6, Util.getMonsterComparator(fp.getTargets()));
		
		for (String monster : pagination)
		{
			String table = HtmCache.getInstance().getHtm(HTML_PATH + "parts/targets_table.htm");
			if (fp.getTargets().contains(monster))
			{
				table = table.replace("%action%", "remove");
				table = table.replace("%button_back%", "L2UI_CH3.mapbutton_zoomout2");
				table = table.replace("%button_fore%", "L2UI_CH3.mapbutton_zoomout1");
			}
			else
			{
				table = table.replace("%action%", "add");
				table = table.replace("%button_back%", "L2UI_CH3.mapbutton_zoomin2");
				table = table.replace("%button_fore%", "L2UI_CH3.mapbutton_zoomin1");
			}
			
			table = table.replace("%npc_name%", monster);
			sb.append(table);
		}
		
		if (sb.isEmpty())
			sb.append("<tr><td width=280 align=center>Vazio</td></tr>");
		else if (pagination.getTotalEntries() > 6)
		{
			pagination.generateSpace(36);
			pagination.generatePages("bypass autofarm targets page %page%");
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "targets.htm");
		html.replace("%list%", sb.toString());
		html.replace("%pagination%", pagination.getContent());
		html.replace("%target_count%", fp.getTargets().size());
		html.replace("%area%", fp.getSelectedArea().getType().name());
		html.replace("%area_name%", fp.getSelectedArea().getName());
		player.sendPacket(html);
	}
	
	private static void showSkillsWindow(Player player, AutoFarmProfile fp, int page)
	{
		final StringBuilder sb = new StringBuilder();
		final Pagination<L2Skill> pagination = new Pagination<>(player.getSkills().values().stream(), page, 6, s -> Util.isSkillCompatible(s), Util.getSkillComparator(fp.getSkills().values()));
		
		for (L2Skill skill : pagination)
		{
			final int skillId = skill.getId();
			final String icon = (skillId < 100) ? "icon.skill00" + skillId : (skillId < 1000) ? "icon.skill0" + skillId : "icon.skill" + skillId;

			String table = HtmCache.getInstance().getHtm(HTML_PATH + "parts/skills_table.htm");
			if (fp.getSkills().values().contains(skill.getId()))
			{
				table = table.replace("%action%", "remove");
				table = table.replace("%button_back%", "L2UI_CH3.mapbutton_zoomout2");
				table = table.replace("%button_fore%", "L2UI_CH3.mapbutton_zoomout1");
			}
			else
			{
				table = table.replace("%action%", "add");
				table = table.replace("%button_back%", "L2UI_CH3.mapbutton_zoomin2");
				table = table.replace("%button_fore%", "L2UI_CH3.mapbutton_zoomin1");
			}
			
			final int skillRange = skill.getCastRange() > 0 ? skill.getCastRange() : skill.getSkillRadius();
			table = table.replace("%skill_icon%", icon);
			table = table.replace("%skill_id%", String.valueOf(skillId));
			table = table.replace("%skill_name%", skill.getName());
			table = table.replace("%skill_type%", skill.isDebuff() ? "Debuff" : skill.isSkillTypeOffensive() ? "Ataque" : "Buff");
			table = table.replace("%skill_range%", skill.isOffensive() && skillRange > 0 ? " - " + skillRange : "");
			sb.append(table);
		}
		
		if (pagination.getTotalEntries() > 6)
		{
			pagination.generateSpace(36);
			pagination.generatePages("bypass autofarm skills page %page%");
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "skills.htm");
		html.replace("%list%", sb.toString());
		html.replace("%msg%", "Selecionadas: " + fp.getSkills().size());
		html.replace("%pagination%", pagination.getContent());
		html.replace("%page%", page);
		player.sendPacket(html);
	}
	
	/*
	 * Alguns checks são feitos durante o bypass, portanto passamos por lá antes
	 */
	public void showIndexWindow(Player player)
	{
		handleBypass(player, "index");
	}
	
	private void showIndexWindow(Player player, String msg)
	{
		final AutoFarmProfile fp = getProfile(player);
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "index.htm");
		
		boolean showingSkill = false;
		for (int slot = 0; slot < 6; slot++)
		{
			if (fp.getSkills().containsKey(slot))
			{
				showingSkill = true;
				final int skillId = fp.getSkills().get(slot);
				final String icon = (skillId < 100) ? "icon.skill00" + skillId : (skillId < 1000) ? "icon.skill0" + skillId : "icon.skill" + skillId;
				
				html.replace("%1_negative_height%" + slot, -2);
				html.replace("%skill_icon%" + slot, icon);
				html.replace("%skill_width%" + slot, 31);
				html.replace("%skill_height%" + slot, 32);
				html.replace("%2_negative_height%" + slot, -48);
				html.replace("%bg_icon%" + slot, "L2UI_CH3.refineslot2");
				html.replace("%bg_width%" + slot, 36);
				html.replace("%bg_height%" + slot, 48);
			}
			else
			{
				html.replace("%1_negative_height%" + slot, -12);
				html.replace("%skill_icon%" + slot, "L2UI_CH3.refineslot2");
				html.replace("%skill_width%" + slot, 36);
				html.replace("%skill_height%" + slot, 48);
				html.replace("%2_negative_height%" + slot, showingSkill ? -48 : -42);
				html.replace("%bg_icon%" + slot, "L2UI_CH3.multisell_plusicon");
				html.replace("%bg_width%" + slot, 31);
				html.replace("%bg_height%" + slot, 30);
			}
		}
		
		if (fp.isEnabled())
		{
			html.replace("%remaining_time%", fp.getEndTime() != 0 ? (TimeUnit.MILLISECONDS.toMinutes(fp.getFinalEndTime() - System.currentTimeMillis()) + " min") : "0 min");
			html.replace("%end_time%", fp.getEndTime() != 0 ? new SimpleDateFormat("HH:mm:ss").format(fp.getFinalEndTime()) : "00:00:00");
			html.replace("%start_time%", new SimpleDateFormat("HH:mm:ss").format(fp.getStartTime()));
			html.replace("%status_color%", "00FF00");
			html.replace("%status%", "ON");
		}
		else
		{
			html.replace("%remaining_time%", "0 min");
			html.replace("%end_time%", "00:00:00");
			html.replace("%start_time%", "00:00:00");
			html.replace("%status_color%", "FF0000");
			html.replace("%status%", "OFF");
		}
		
		html.replace("%area%",  fp.getSelectedAreaId() != 0 ? StringUtil.trimAndDress(fp.getSelectedArea().getName(), 10) : "Definir");
		html.replace("%targets%", !fp.getTargets().isEmpty() ? String.valueOf(fp.getTargets().size()) : "Definir");
		html.replace("%duration%", fp.getEndTime() != 0 ? TimeUnit.MILLISECONDS.toMinutes(fp.getEndTime()) + " m" : "Definir");
		html.replace("%macro%", fp.getMacro() != null ? fp.getMacro().toString().toLowerCase() : "Definir");
		html.replace("%autopotion%", fp.useAutoPotion() ? "checked" : "unable");
		html.replace("%attack_raid%", fp.attackRaid() ? "checked" : "unable");
		html.replace("%pickherbs%", fp.pickHerbs() ? "checked" : "unable");
		html.replace("%alert%", msg != null ? HtmCache.getInstance().getHtm(HTML_PATH + "parts/index_alert.htm").replace("%msg%", msg) : "");
		html.replace("%fix%", showingSkill ? "<img height=\"-6\">" : "");
		html.replace("%optional%", player.isMageClass() ? "" : "(opcional)");
		player.sendPacket(html);
	}
	
	public void showZoneWindow(Player player)
	{
		showEditAreaWindow(player, getProfile(player).getBuildingAreaId(), null);
	}
	
	private void showEditAreaWindow(Player player, int areaId, String msg)
	{
		final AutoFarmArea area = getProfile(player).getAreaById(areaId);
		if (area == null)
			return;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "areaedit.htm");
		
		if (ZoneBuilder.getInstance().getPoints(player).isEmpty() && (area.isChanged() || area.getNodes().isEmpty()))
		{
			String table = HtmCache.getInstance().getHtm(HTML_PATH + "parts/areaedit_table.htm");
			table = table.replaceAll("%node%", "0");
			table = table.replace("%x%", "0");
			table = table.replace("%y%", "0");
			table = table.replace("%z%", "0");
			html.replace("%list%", table);
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			if (ZoneBuilder.getInstance().getPoints(player).isEmpty())
			{
				int indice = 0;
				for (Location loc : area.getNodes())
				{
					indice++;
					String table = HtmCache.getInstance().getHtm(HTML_PATH + "parts/areaedit_table.htm");
					table = table.replaceAll("%node%", String.valueOf(indice));
					table = table.replace("%x%", String.valueOf(loc.getX()));
					table = table.replace("%y%", String.valueOf(loc.getY()));
					table = table.replace("%z%", String.valueOf(loc.getZ()));
					sb.append(table);
				}
			}
			else
			{
				for (Entry<Integer, Point> entry : ZoneBuilder.getInstance().getPoints(player).entrySet())
				{
					String table = HtmCache.getInstance().getHtm(HTML_PATH + "parts/areaedit_table.htm");
					table = table.replaceAll("%node%", String.valueOf(entry.getKey()));
					table = table.replace("%x%", String.valueOf(entry.getValue().getX()));
					table = table.replace("%y%", String.valueOf(entry.getValue().getY()));
					table = table.replace("%z%", String.valueOf(entry.getValue().getZ()));
					sb.append(table);
				}
			}
			html.replace("%list%", sb.toString());
		}
		
		final AutoFarmProfile profile = getProfile(player);
		html.replace("%checkbox%", profile.getSelectedAreaId() == areaId ? "checked" : "unable");
		html.replace("%name%", area.getName());
		html.replace("%id%", areaId);
		html.replace("%msg%", msg != null ? msg + "<br>" : "");
		html.replace("%type%", area.getType().name());
		html.replace("%lock%", profile.isAddingLocationLocked() ? "travado" : "travar");
		html.replace("%adding%", profile.isAddingLocation() ? "-" : "+");
		player.sendPacket(html);
	}
	
	private void showAreaListWindow(Player player, AutoFarmType type)
	{
		showAreaListWindow(player, type, null);
	}
	
	private void showAreaListWindow(Player player, AutoFarmType type, String msg)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		final AutoFarmProfile fp = getProfile(player);
		
		if (type == AutoFarmType.OPEN)
		{
			html.setFile(HTML_PATH + "arealist_open.htm");
			html.replace("%radius%", Math.max(0, fp.getFinalRadius()));
			html.replace("%checkbox%", fp.getSelectedAreaId() == 1 ? "checked" : "unable");
		}
		else
		{
			final List<AutoFarmArea> areas = getProfile(player).getAreas().values().stream().filter(a -> a.getType() == type).sorted(Comparator.comparing(AutoFarmArea::getName)).toList();
			html.setFile(HTML_PATH + (type == AutoFarmType.ZONA ? "arealist_zone.htm" : "arealist_route.htm"));
			
			if (areas.isEmpty())
			{
				html.replace("%list%", "vazio");
			}
			else
			{
				final StringBuilder sb = new StringBuilder();
				for (AutoFarmArea area : areas)
				{
					String table = HtmCache.getInstance().getHtm(HTML_PATH + "parts/arealist_table.htm");
					table = table.replaceAll("%id%", String.valueOf(area.getId()));
					table = table.replace("%name%", area.getName());
					table = table.replaceAll("%checkbox%", fp.getSelectedAreaId() == area.getId() ? "checked" : "unable");
					sb.append(table);
				}
				html.replace("%list%", sb.toString());
			}
		}
		
		html.replace("%msg%", msg != null ? "<font color=FFA000>" + msg + "</font><br>": "");
		player.sendPacket(html);
	}
	
	private static void showTimerWindow(Player player, AutoFarmProfile fp, String msg)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + "timer.htm");
		html.replace("%msg%", msg != null ? "<font color=FFA000>" + msg + "</font><br>": "");
		html.replace("%current%", fp.getEndTime() == 0 ? "" : TimeUnit.MILLISECONDS.toMinutes(fp.getEndTime()) + " minuto(s)<br>");
		player.sendPacket(html);
	}
	
	private static void showConfirmChangeWindow(Player player, AutoFarmArea area)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HTML_PATH + (area.isFromDb() ? "confirm_change.htm" : "confirm_save.htm")); 
		html.replace("%type%", area.getType().toString());
		html.replace("%name%", area.getName());
		html.replace("%id%", area.getId());
		player.sendPacket(html);
	}
	
	private static boolean canSeeArea(Player player, AutoFarmArea area)
	{
		if (area.getType() == AutoFarmType.OPEN)
			return true;
		
		if (area.getZone() == null && area.isFromDb())
			ZoneBuilder.getInstance().setAutoFarmAreaZone(player, area);
		
		// Pode ser outra diferente
//		if (player.isInsideZone(ZoneId.AUTO_FARM))
//			return true;
		
		if (area.getNodes().stream().anyMatch(n -> GeoEngine.getInstance().canMoveToTarget(player, n)))
			return true;
		
		return area.isOwnerNearOrInside(500);
	}
	
	private void startPlayer(Player player)
	{
		if (player.denyAiAction() || player.getCast().isCastingNow())
			return;
		
		if (Config.AUTOFARM_DISABLE_TOWN && player.isInsideZone(ZoneId.TOWN))
		{
			showIndexWindow(player, "Não é possí­vel iniciar na cidade");
			return;
		}
		
		final AutoFarmProfile fp = getProfile(player);
		if (fp.getSelectedAreaId() == 0)
		{
			showIndexWindow(player, "É necessário selecionar uma área");
			return;
		}
		
		if (Config.AUTOFARM_MAX_TIMER != 0 && fp.getEndTime() == 0)
		{
			showIndexWindow(player, "É necessário definir uma duração");
			return;
		}
		
		final String playerIp = player.getClient().getConnection().getInetAddress().getHostAddress();
		if (!Config.AUTOFARM_ALLOW_DUALBOX && _players.values().stream().filter(AutoFarmProfile::isEnabled).anyMatch(p -> p.getPlayer().getClient().getConnection().getInetAddress().getHostAddress().equals(playerIp)))
		{
			showIndexWindow(player, "Não é permitido dualbox");
			return;
		}
		
		if (!canSeeArea(player, fp.getSelectedArea()))
		{
			showIndexWindow(player, "Área muito distante para ser visualizada");
			return;
		}
		
		// A área está perto, já podemos exibir, mesmo que o player não seja movido automaticamente
		ZoneBuilder.getInstance().previewFinalArea(fp, fp.getSelectedAreaId());
		
		// Está fora da zona. Tentamos entrar ou não iniciamos
		if (fp.getSelectedArea().getType() == AutoFarmType.ZONA && !player.isInsideZone(ZoneId.AUTO_FARM) && !fp.getSelectedArea().getFarmZone().tryGoBackInside())
		{
			showIndexWindow(player, "Mova-se até o interior da zona");
			return;
		}
		
		if (player.getTarget() != null)
			player.setTarget(null);
		
		if (Config.AUTOFARM_CHANGE_PLAYER_TITLE)
		{
			fp.setPlayerTitle(player.getTitle());
			player.setTitle("Auto Farming");
			player.broadcastTitleInfo();
		}
		
		if (Config.AUTOFARM_CHANGE_PLAYER_NAME_COLOR)
		{
			final int color = Integer.decode("0x" + Config.AUTOFARM_NAME_COLOR);
			player.getAppearance().setNameColor(color);
			player.getAppearance().setTitleColor(color);
			player.broadcastUserInfo();
		}
		
		fp.setEnabled(true);
		fp.startRoutine();
		showIndexWindow(player, null);
	}
	
	public void stopPlayer(AutoFarmProfile fp, String msg)
	{
		stopPlayer(fp.getPlayer(), msg);
	}
	
	public void stopPlayer(Player player, String msg)
	{
		// Esse player não usou o sistema
		if (getPlayer(player.getObjectId()) == null)
			return;
		
		final AutoFarmProfile fp = getProfile(player);
		
		// Logout
		if (!player.isOnline())
			fp.updatePlayer(null);
		
		if (fp.isEnabled())
		{
			if (fp.getPlayerTitle() != null)
			{
				player.setTitle(fp.getPlayerTitle());
				player.broadcastTitleInfo();
			}
			
			if (Config.AUTOFARM_CHANGE_PLAYER_NAME_COLOR)
			{
				player.getAppearance().setNameColor(player.getAccessLevel().getNameColor());
				player.getAppearance().setTitleColor(player.getAccessLevel().getTitleColor());
				player.broadcastUserInfo();
			}
			
			ZoneBuilder.getInstance().clearAllPreview(player);
			fp.setEnabled(false);
			player.abortAll(false);
		}
		
		showIndexWindow(player, msg);
	}
	
	private void createArea(Player player, AutoFarmProfile fp, StringTokenizer st) throws IllegalArgumentException, NumberFormatException
	{
		if (!st.hasMoreTokens())
			return;
		
		final AutoFarmType type = AutoFarmType.valueOf(st.nextToken());
		final String name = st.hasMoreTokens() ? st.nextToken().toLowerCase() : "";
		
		if (name.isEmpty() || !StringUtil.isValidString(name, ZONE_NAME_REGEX))
		{
			showAreaListWindow(player, type, "Nome inválido");
			return;
		}
		
		if (fp.getAreas().values().stream().anyMatch(a -> a.getName().equals(name)))
		{
			showAreaListWindow(player, type, "Nome já utilizado");
			return;
		}
		
		if (type == AutoFarmType.ZONA)
		{
			if (!player.isGM() && fp.getAreas().values().stream().filter(a -> a.getType() == AutoFarmType.ZONA).count() >= Config.AUTOFARM_MAX_ZONES)
			{
				showAreaListWindow(player, type, "Limite de zonas atingido");
				return;
			}
			
			final AutoFarmZone zone = new AutoFarmZone(name, player.getObjectId());
			fp.getAreas().put(zone.getId(), zone);
			fp.setBuildingAreaId(zone.getId());
		}
		else if (type == AutoFarmType.ROTA)
		{
			if (!player.isGM() && fp.getAreas().values().stream().filter(a -> a.getType() == AutoFarmType.ROTA).count() >= Config.AUTOFARM_MAX_ROUTES)
			{
				showAreaListWindow(player, type, "Limite de rotas atingido");
				return;
			}
			
			final AutoFarmRoute route = new AutoFarmRoute(name, player.getObjectId());
			fp.getAreas().put(route.getId(), route);
			fp.setBuildingAreaId(route.getId());
		}
		
		showAreaListWindow(player, type);
		ZoneBuilder.getInstance().clearAllPreview(player);
	}
	
	/*
	 * Prepara a area para receber novos nodes
	 */
	private void newNodeMode(Player player, AutoFarmProfile fp, boolean lock)
	{
		if (fp.getBuildingArea() == null)
			return;
		
		if (fp.isAddingLocation())
		{
			if (lock)
				fp.toggleAddingLocationLock();
			else
				fp.setAddingLocation(false);
			
			showEditAreaWindow(player, fp.getBuildingAreaId(), fp.isAddingLocation() ? "Indique com o mouse o local" : null);
			return;
		}
		
		if (fp.getBuildingArea().getZone() != null && !canSeeArea(player, fp.getBuildingArea()))
		{
			showEditAreaWindow(player, fp.getBuildingAreaId(), "Área muito distante para ser visualizada");
			return;
		}
		
		// Se vamos editar a zona é necessário restaurá-la
		if (ZoneBuilder.getInstance().getPoints(player).isEmpty() && !fp.getBuildingArea().isChanged())
		{
			ZoneBuilder.getInstance().restoreDebugPoints(player, fp.getBuildingArea().getNodes());
			ZoneBuilder.getInstance().preview(player);
		}
		
		if (fp.getBuildingArea().getType() == AutoFarmType.ZONA && ZoneBuilder.getInstance().getPoints(player).size() >= Math.min(Config.AUTOFARM_MAX_ZONE_NODES, 20))
		{
			showEditAreaWindow(player, fp.getBuildingAreaId(), "Sua zona atingiu o número máximo de pontos.");
			return;
		}
		else if (fp.getBuildingArea().getType() == AutoFarmType.ROTA && ZoneBuilder.getInstance().getPoints(player).size() >= Math.min(Config.AUTOFARM_MAX_ROUTE_NODES, 20))
		{
			showEditAreaWindow(player, fp.getBuildingAreaId(), "Sua rota atingiu o número máximo de pontos.");
			return;
		}
		
		if (lock)
			fp.toggleAddingLocationLock();
		else
			fp.setAddingLocation(true);
		
		showEditAreaWindow(player, fp.getBuildingAreaId(), "Indique com o mouse o local");
	}
	
	public void addAreaNode(Player player, Location loc)
	{
		final AutoFarmProfile fp = getProfile(player);
		final AutoFarmArea area = fp.getBuildingArea();
		if (area == null)
			return;

		String msg = null;
		if (Config.AUTOFARM_DISABLE_TOWN && ZoneManager.getInstance().getZone(loc.getX(), loc.getY(), loc.getZ(), TownZone.class) != null)
		{
			msg = "Localização inválida";
		}
		else if (area.getType() == AutoFarmType.ZONA && Config.AUTOFARM_MAX_ZONE_AREA != 0 && ZoneBuilder.getInstance().calculateArea(player) > Config.AUTOFARM_MAX_ZONE_AREA)
		{
			msg = "Sua zona atingiu o tamanho máximo.";
		}
		else if (area.getType() == AutoFarmType.ROTA && Config.AUTOFARM_MAX_ROUTE_PERIMITER != 0 && ZoneBuilder.getInstance().calculatePerimeter(player) > Config.AUTOFARM_MAX_ROUTE_PERIMITER)
		{
			msg = "Sua rota atingiu o comprimento máximo.";
		}
		else
		{
			final Map<Integer, Point> points = ZoneBuilder.getInstance().getPoints(player);
			if (points.size() > 0)
			{
				final Point lastPoint = points.get(points.size());
				if (!GeoEngine.getInstance().canSee(lastPoint.getX(), lastPoint.getY(), lastPoint.getZ() + 20, 0, loc.getX(), loc.getY(), loc.getZ(), 0, null, null))
				{
					msg = "Ponto geograficamente indisponí­vel";
				}
				else if (lastPoint.distance3D(loc) > MAX_ROUTE_LINE_LENGTH)
				{
					msg = "Ponto muito distante";
				}
			}
		}
		
		if (msg == null)
		{
			area.setIsChanged(true);
			ZoneBuilder.getInstance().addPoint(player, loc);
		}
		
		if (!fp.isAddingLocationLocked())
			fp.setAddingLocation(false);
		
		showEditAreaWindow(player, area.getId(), msg);
	}
	
	private void removeAreaNode(Player player, AutoFarmProfile fp, int nodeId)
	{
		if (fp.getBuildingArea() == null || nodeId < 0)
			return;
		
		if (fp.getBuildingArea().getZone() != null && !canSeeArea(player, fp.getBuildingArea()))
		{
			showEditAreaWindow(player, fp.getBuildingAreaId(), "Área muito distante para ser visualizada");
			return;
		}
		
		final Map<Integer, Point> points = ZoneBuilder.getInstance().getPoints(player);
		
		// Se vamos editar a zona, é necessário restaurá-la
		if (points.isEmpty())
			ZoneBuilder.getInstance().restoreDebugPoints(player, fp.getBuildingArea().getNodes());
		
		// Não deve acontecer
		if (points.isEmpty() || points.size() < nodeId)
			return;

		// Ponto removido
		ZoneBuilder.getInstance().removePoint(player, nodeId);
		
		// Se após remover um node a área ficou exatamente como era antes, então não foi alterada de fato
		if (Util.isNodeListEquals(points.values(), fp.getBuildingArea().getNodes()))
			fp.getBuildingArea().setIsChanged(false);
		else
			fp.getBuildingArea().setIsChanged(true);
		
		showEditAreaWindow(player, fp.getBuildingAreaId(), null);
	}
	
	private void deleteArea(Player player, AutoFarmProfile fp, int areaId)
	{
		final AutoFarmArea area = fp.getAreaById(areaId);
		if (area.getType() == AutoFarmType.OPEN)
			return;
		
		if (area.isFromDb())
			AutoFarmData.getInstance().deleteArea(player.getObjectId(), areaId);
		
		if (area.getType() == AutoFarmType.ZONA)
			area.getFarmZone().removeFromWorld();
		
		boolean clearPreview = false;
		if (fp.getSelectedAreaId() == areaId)
		{
			clearPreview = true;
			fp.setSelectedAreaId(0);
		}
		
		if (fp.getBuildingAreaId() == areaId)
		{
			clearPreview = true;
			fp.setBuildingAreaId(0);
		}
		
		if (clearPreview)
			ZoneBuilder.getInstance().clearAllPreview(player);
		
		fp.getAreas().remove(areaId);
		fp.getTargets().clear();
		showAreaListWindow(player, area.getType());
	}
	
	private void saveArea(Player player, AutoFarmProfile fp)
	{
		final AutoFarmArea area = fp.getBuildingArea();
		if (area == null)
			return;
		
		if (!area.isChanged())
		{
			showEditAreaWindow(player, area.getId(), null);
			return;
		}
		
		if (ZoneBuilder.getInstance().getPoints(player).isEmpty())
		{
			showEditAreaWindow(player, area.getId(), "Construa sua zona antes de salvar");
			return;
		}
		
		if (area.getType() == AutoFarmType.ROTA)
		{
			
			if (ZoneBuilder.getInstance().getPoints(player).size() < 2)
			{
				showEditAreaWindow(player, area.getId(), "Uma rota precisa de pelo menos 2 pontos");
				return;
			}
		}
		else
		{
			if (ZoneBuilder.getInstance().getPoints(player).size() < 3)
			{
				showEditAreaWindow(player, area.getId(), "Uma zona precisa de pelo menos 3 pontos");
				return;
			}
		}
		
		// Ao limpar os pontos da área, o ZoneBuilder irá selecionar os que estão nele
		area.getNodes().clear();
		
		// Criar ZoneNPolyZ
		ZoneBuilder.getInstance().setAutoFarmAreaZone(player, area);
		
		// Exibir zona final
		ZoneBuilder.getInstance().previewFinalArea(fp, area.getId());

		// Atualizar variaveis
		fp.setSelectedAreaId(area.getId());
		
		if (area.getType() == AutoFarmType.ZONA)
		{
			area.getFarmZone().removeFromWorld();
			area.getFarmZone().addToWorld();
		}
		
		if (!area.isFromDb())
			AutoFarmData.getInstance().insertArea(player.getObjectId(), fp.getBuildingArea());
		
		area.setIsChanged(false);
		AutoFarmData.getInstance().insertNodes(fp.getBuildingArea());
		showEditAreaWindow(player, area.getId(), "Salvo!");
	}
	
	private void previewArea(Player player, AutoFarmProfile fp, int areaId, boolean finalView)
	{
		final AutoFarmArea area = fp.getAreaById(areaId);
		if (area.getType() != AutoFarmType.OPEN && area.isChanged() || !area.isFromDb())
			return;
		
		// Já está sendo visualizada
		if (!ZoneBuilder.getInstance().getPoints(player).isEmpty() && fp.getBuildingAreaId() == areaId)
		{
			ZoneBuilder.getInstance().clearAllPreview(player);
			return;
		}
		
		// Verificamos depois para que o preview possa ser sempre limpo
		if (!canSeeArea(player, area))
		{
			if (finalView)
				showAreaListWindow(player, area.getType(), "Área muito distante para ser visualizada");
			else
				showEditAreaWindow(player, areaId, "Área muito distante para ser visualizada");
			
			return;
		}
		
		if (finalView)
		{
			ZoneBuilder.getInstance().previewFinalArea(fp, areaId);
			showAreaListWindow(player, area.getType());
		}
		else
		{
			ZoneBuilder.getInstance().restoreDebugPoints(player, fp.getBuildingArea().getNodes());
			ZoneBuilder.getInstance().preview(player);
			showEditAreaWindow(player, areaId, null);
		}
			
		fp.setBuildingAreaId(areaId); // necessário para desativar o preview depois
	}
	
	public static final AutoFarmManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AutoFarmManager INSTANCE = new AutoFarmManager();
	}
}
