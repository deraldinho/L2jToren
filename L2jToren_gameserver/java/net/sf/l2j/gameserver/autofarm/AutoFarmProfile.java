package net.sf.l2j.gameserver.autofarm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmMacro;
import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmType;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmArea;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmOpen;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.skills.L2Skill;

public class AutoFarmProfile
{
	private String _playerTitle;
	private int _currentSelectAreaId;
	private int _currentBuldingAreaId;
	private int _currentSkillSlot;
	private int _radius;
	private int _lastAttackRange;
	private int _macroAdditionalId;
	private int _lastClassId;
	private long _startTime;
	private long _endTime;
	private long _lastActiveTime;
	private boolean _autoPotion;
	private boolean _attackRaid;
	private boolean _isEnabled;
	private boolean _isRunning;
	private boolean _isAddingLocation;
	private boolean _isAddingLocationLocked;
	private boolean _pickHerbs;
	private Set<String> _targets;
	private Map<Integer, Integer> _skills;
	private Map<Integer, AutoFarmArea> _areas;
	private ReentrantLock _lock;
	private Location _lastLocation;
	private Player _player;
	private AutoFarmMacro _macro;
	private AutoFarmRoutine _routine;
	
	public AutoFarmProfile(Player player)
	{
		_player = player;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public void updatePlayer(Player player)
	{
		_player = player;
	}
	
	public boolean isEnabled()
	{
		return _isEnabled;
	}
	
	public void setEnabled(boolean status)
	{
		_isEnabled = status;
		
		if (!_isEnabled)
		{
			if (getSelectedArea() != null && getSelectedArea().getType() == AutoFarmType.ROTA)
				getSelectedArea().getRouteZone().reset();
		}
		else
			_lastActiveTime = System.currentTimeMillis();
		
		_startTime = status ? System.currentTimeMillis() : 0;
	}
	
	public boolean isRunning()
	{
		return _isRunning;
	}
	
	public void setRunning(boolean status)
	{
		_isRunning = status;
	}
	
	public int getBuildingAreaId()
	{
		return _currentBuldingAreaId;
	}
	
	public void setBuildingAreaId(int value)
	{
		_currentBuldingAreaId = value;
	}
	
	public int getSelectedAreaId()
	{
		return _currentSelectAreaId;
	}
	
	public void setSelectedAreaId(int id)
	{
		if (_currentSelectAreaId == id)
			return;
		
		if (_currentSelectAreaId != 0)
			getAreaById(_currentSelectAreaId).getMonsterHistory().clear();
		
		_radius = 0;
		_currentSelectAreaId = id;
		_targets.clear();
		_lastActiveTime = System.currentTimeMillis();
	}

	public AutoFarmArea getAreaById(int id)
	{
		if (id == 1 && !getAreas().containsKey(1))
		{
			final AutoFarmArea area = new AutoFarmOpen(_player.getObjectId());
			getAreas().put(area.getId(), area);
		}
		
		return getAreas().getOrDefault(id, null);
	}
	
	public AutoFarmArea getSelectedArea()
	{
		return getAreas().getOrDefault(_currentSelectAreaId, null);
	}
	
	public AutoFarmArea getBuildingArea()
	{
		return getAreas().getOrDefault(_currentBuldingAreaId, null);
	}
	
	public Map<Integer, AutoFarmArea> getAreas()
	{
		if (_areas == null)
			_areas = new HashMap<>();
		
		return _areas; 
	}
	
	public void startRoutine()
	{
		if (_routine == null)
			_routine = new AutoFarmRoutine(this);
		
		if (_lock == null)
			_lock = new ReentrantLock();
		
		if (_lock.tryLock())
		{
			try
			{
				_routine.start();
			}
			catch (Exception e)
			{
				System.out.println(e);
			}
			finally 
			{
				_lock.unlock();
			}
		}
	}
	
	public boolean isAddingLocation()
	{
		return _isAddingLocation;
	}
	
	public void setAddingLocation(boolean status)
	{
		if (!status)
			_isAddingLocationLocked = false;
		
		_isAddingLocation = status;
	}
	
	public boolean isAddingLocationLocked()
	{
		return _isAddingLocationLocked;
	}
	
	public void toggleAddingLocationLock()
	{
		_isAddingLocation = !_isAddingLocationLocked;
		_isAddingLocationLocked = !_isAddingLocationLocked;
	}
	
	public int getCurrentSkillSlot()
	{
		return _currentSkillSlot;
	}
	
	public void setCurrentSkillSlot(int value)
	{
		_currentSkillSlot = value;
	}
	
	/*
	 * Usamos o nome do Npc ao invés do seu ID porque é comum que muitos tenham o mesmo nome, mas não o mesmo ID
	 */
	public Set<String> getTargets()
	{
		if (_targets == null)
			_targets = new HashSet<>();
		
		return _targets;
	}
	
	public Map<Integer, Integer> getSkills()
	{
		if (_skills == null)
			_skills = new HashMap<>(6);
		
		return _skills;
	}
	
	public long getStartTime()
	{
		return _startTime;
	}
	
	public long getEndTime()
	{
		return _endTime;
	}
	
	public void setEndTime(long value)
	{
		if (value == 0)
		{
			_macro = null;
			_macroAdditionalId = 0;
		}
		
		_endTime = value;
	}
	
	public long getFinalEndTime()
	{
		return _endTime + _startTime;
	}
	
	public long getLastActiveTime()
	{
		return _lastActiveTime;
	}
	
	public boolean useAutoPotion()
	{
		return _autoPotion;
	}
	
	public void toggleAutoPotion()
	{
		_autoPotion = !_autoPotion;
	}
	
	public boolean attackRaid()
	{
		return _attackRaid;
	}
	
	public void toggleAttackRaid()
	{
		_attackRaid = !_attackRaid;
	}
	
	public boolean pickHerbs()
	{
		return _pickHerbs;
	}
	
	public void togglePickHerbs()
	{
		_pickHerbs = !_pickHerbs;
	}
	
	public void setMacro(AutoFarmMacro m, int additionalId)
	{
		_macro = m;
		_macroAdditionalId = additionalId;
	}
	
	public AutoFarmMacro getMacro()
	{
		return _macro;
	}
	
	/**
	 * @return ID da skill ou item associado ao macro
	 */
	public int getMacroAdditionalId()
	{
		return _macroAdditionalId;
	}

	/**
	 * @return Título utilizado pelo player antes de ativar o auto farm
	 */
	public String getPlayerTitle()
	{
		return _playerTitle;
	}
	
	public void setPlayerTitle(String value)
	{
		_playerTitle = value;
	}
	
	public void updatePlayerLocation()
	{
		_lastLocation = _player.getPosition().clone();
	}
	
	public Location getLastPlayerLocation()
	{
		return _lastLocation;
	}
	
	public void checkLastClassId()
	{
		if (_lastClassId == _player.getActiveClass())
			return;
		
		if (_lastClassId != 0)
			_skills.clear();
		
		_lastClassId = _player.getActiveClass();
	}
	
	public int getAreaMaxRadius()
	{
		if (_currentSelectAreaId == 0)
			return 0;
		else if (getSelectedArea().getType() == AutoFarmType.OPEN && Config.AUTOFARM_MAX_OPEN_RADIUS > 0)
			return Config.AUTOFARM_MAX_OPEN_RADIUS;
		else
			return getAttackRange();
	}
	
	/*
	 * Ei! Você aí, que tal reescrever isso?
	 */
	public int getFinalRadius()
	{
		if (_radius == 0 && _currentSelectAreaId != 0 && getSelectedArea().getType() == AutoFarmType.OPEN)
		{
			if (Config.AUTOFARM_MAX_OPEN_RADIUS != 0)
				return Config.AUTOFARM_MAX_OPEN_RADIUS > 1000 ? Config.AUTOFARM_MAX_OPEN_RADIUS / 2 : Config.AUTOFARM_MAX_OPEN_RADIUS;

			if (getAttackRange() < 100)
				return 900;

			return getAttackRange();
		}
		else if (_radius == 0 || (_radius > getAttackRange() && _currentSelectAreaId != 0 && getSelectedArea().getType() != AutoFarmType.OPEN)) // arma mudou
			return getAreaMaxRadius();
		else
			return _radius;
	}
	
	public void setRadius(int value)
	{
		_lastAttackRange = _radius;
		_radius = value;
		
		if (pickHerbs() && !getSelectedArea().isMovementAllowed())
			togglePickHerbs();
	}
	
	/**
	 * @return Verifica se o range foi alterado pelo PLAYER
	 */
	public boolean isRadiusChanged()
	{
		if (_lastAttackRange != 0 && _lastAttackRange != _radius)
		{
			_lastAttackRange = _radius;
			return true;
		}

		return false;
	}
	
	public int getAttackRange()
	{
		if (getAttackSkills().isEmpty())
			return _player.getStatus().getPhysicalAttackRange();
		
		return getAttackSkills().stream().mapToInt(s -> s.getCastRange() > 0 ? s.getCastRange() : s.getSkillRadius()).max().orElse(0);
	}

	/**
	 * @return Todas as skills de ataque (incluindo debuffs)
	 */
	public List<L2Skill> getAttackSkills()
	{
		return getSkills().values().stream().map(s -> _player.getSkill(s)).filter(s -> s.isSkillTypeOffensive()).collect(Collectors.toList());
	}
	
	public List<L2Skill> getAttackSkills(boolean debuff)
	{
		return getSkills().values().stream().map(s -> _player.getSkill(s)).filter(s -> s.isDebuff() == debuff && s.isSkillTypeOffensive()).collect(Collectors.toList());
	}
	
	public List<L2Skill> getBuffSkills()
	{
		return getSkills().values().stream().map(s -> _player.getSkill(s)).filter(s -> !s.isDebuff() && !s.isSkillTypeOffensive() && !s.getSkillType().name().contains("HEAL")).toList();
	}
	
	public List<L2Skill> getDebuffSkills()
	{
		return getSkills().values().stream().map(s -> _player.getSkill(s)).filter(s -> s.isDebuff()).toList();
	}
	
	public List<L2Skill> getHpHealSkills()
	{
		return getSkills().values().stream().map(s -> _player.getSkill(s)).filter(s -> s.getSkillType() == SkillType.HEAL || s.getSkillType() == SkillType.HEAL_PERCENT || s.getSkillType() == SkillType.HEAL_STATIC).toList();
	}
	
	public List<L2Skill> getMpHealSkills()
	{
		return getSkills().values().stream().map(s -> _player.getSkill(s)).filter(s -> s.getSkillType() == SkillType.MANAHEAL || s.getSkillType() == SkillType.MANAHEAL_PERCENT).toList();
	}
}
