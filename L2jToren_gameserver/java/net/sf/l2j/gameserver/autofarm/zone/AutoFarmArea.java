package net.sf.l2j.gameserver.autofarm.zone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmType;
import net.sf.l2j.gameserver.autofarm.AutoFarmManager;
import net.sf.l2j.gameserver.autofarm.AutoFarmProfile;
import net.sf.l2j.gameserver.autofarm.zone.form.ZoneNPolyZ;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;

public class AutoFarmArea extends ZoneType
{
	protected final Set<String> _monsterHistory = new HashSet<>();
	private final String _name;
	private final int _ownerId;
	private final AutoFarmType _type;
	private final List<Location> _nodes = new ArrayList<>();
	private boolean _isFromDb;
	private boolean _isChanged;
	
	public AutoFarmArea(String name, int ownerId, AutoFarmType type)
	{
		super(IdFactory.getInstance().getNextId());
		
		_name = name;
		_ownerId = ownerId;
		_type = type;
	}
	
	public AutoFarmArea(int id, String name, int ownerId, AutoFarmType type)
	{
		super(id);
		
		_name = name;
		_ownerId = ownerId;
		_type = type;
		_isFromDb = true;
	}
	
	@Override
	public void onEnter(Creature character)
	{
	}

	@Override
	public void onExit(Creature character)
	{
	}
	
	public ZoneNPolyZ getZoneZ()
	{
		return (ZoneNPolyZ) getZone();
	}
	
	public List<Monster> getMonsters()
	{
		return null;
	}
	
	public Set<String> getMonsterHistory()
	{
		return _monsterHistory;
	}
	
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	public AutoFarmType getType()
	{
		return _type;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public List<Location> getNodes()
	{
		return _nodes;
	}
	
	public AutoFarmZone getFarmZone()
	{
		return null;
	}
	
	public AutoFarmRoute getRouteZone()
	{
		return null;
	}
	
	public boolean isFromDb()
	{
		return _isFromDb;
	}
	
	public void setIsFromDb()
	{
		_isFromDb = true;
	}
	
	/*
	 * Verifica se algum node foi adicionado ou removido a essa área
	 */
	public boolean isChanged()
	{
		return _isChanged;
	}
	
	public void setIsChanged(boolean status)
	{
		_isChanged = status;
	}
	
	public boolean isMovementAllowed()
	{
		switch (_type)
		{
			case OPEN:
				return getProfile().getFinalRadius() > getProfile().getAttackRange();
				
			case ROTA:
				return false;
				
			default:
				return true;
		}
	}
	
	public AutoFarmProfile getProfile()
	{
		return AutoFarmManager.getInstance().getPlayer(_ownerId);
	}
	
	public Player getOwner()
	{
		return getProfile().getPlayer();
	}
	
	public boolean isOwnerNearOrInside(int proximityRadius)
	{
		final Player player = getOwner();
		final int ax1 = player.getX() - proximityRadius;
		final int ax2 = player.getX() + proximityRadius;
		final int ay1 = player.getY() - proximityRadius;
		final int ay2 = player.getY() + proximityRadius;
		final int az1 = player.getZ() - proximityRadius;
		final int az2 = player.getZ() + proximityRadius;
	    return getZoneZ().intersectsRectangle(ax1, ax2, ay1, ay2, az1, az2);
	}
	
	public boolean isOwnerNearEdge(int proximityRadius)
	{
		final Player player = getOwner();
		final int ax1 = player.getX() - proximityRadius;
		final int ax2 = player.getX() + proximityRadius;
		final int ay1 = player.getY() - proximityRadius;
		final int ay2 = player.getY() + proximityRadius;
	    return getZoneZ().intersectsRectangleOnEdge(ax1, ax2, ay1, ay2);
	}
}
