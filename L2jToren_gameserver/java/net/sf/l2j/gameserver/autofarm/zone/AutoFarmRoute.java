package net.sf.l2j.gameserver.autofarm.zone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.autofarm.AutoFarmManager;
import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;

public class AutoFarmRoute extends AutoFarmArea
{
	private int _index;
	private int _radius;
	private boolean _reversePath;
	private boolean _isOnARoute;
	private boolean _reachedFirstNode;
	
	public AutoFarmRoute(int id, String name, int ownerId)
	{
		super(id, name, ownerId, AutoFarmType.ROTA);
	}
	
	public AutoFarmRoute(String name, int ownerId)
	{
		super(name, ownerId, AutoFarmType.ROTA);
	}

	@Override
	public void visualizeZone(ExServerPrimitive debug)
	{
		getZone().visualizeZone("ROTA " + getName(), debug);
	}
	
	@Override
	public AutoFarmRoute getRouteZone()
	{
		return this;
	}
	
	@Override
	public List<Location> getNodes()
	{
		return super.getNodes();
	}
	
	@Override
	public List<Monster> getMonsters()
	{
		// Aumentamos o range para obter mais opções, mas serão retornados apenas os comatíveis
		if (getProfile().getFinalRadius() < 100)
		{
			final List<Monster> monsters = new ArrayList<>();
			for (Monster m :getOwner().getKnownTypeInRadius(Monster.class, getProfile().getFinalRadius() * 2))
			{
				if (MathUtil.checkIfInRange(getProfile().getFinalRadius(), m, getOwner(), true))
				{
					monsters.add(m);
					continue;
				}
				
				// Monstros que estão fora do range de ataque só são retornados caso ele esteja tentando atacar o player
				if (m.getAI().getAggroList().getHate(getOwner()) > 0)
				{
					monsters.add(m);
					continue;
				}
			}
			
			return monsters;
		}
		
		return getOwner().getKnownTypeInRadius(Monster.class, getProfile().getFinalRadius());
	}
	
	@Override
	public Set<String> getMonsterHistory()
	{
		_monsterHistory.addAll(getKnownTypeInRadius(Monster.class, AutoFarmManager.MAX_ROUTE_LINE_LENGTH).stream().map(Monster::getName).toList());
		return _monsterHistory;
	}
	
	public void reset()
	{
		_isOnARoute = false;
		_reachedFirstNode = false;
	}
	
	/**
	 * @return A rota ainda não foi iniciada
	 */
	public boolean isOwnerOnARoute()
	{
		return _isOnARoute;
	}
	
	/**
	 * @return O player está começando agora a rota (ainda não chegou no index mais próximo)
	 */
	public boolean reachedFirstNode()
	{
		return _reachedFirstNode;
	}
	
	public int getRadius()
	{
		return _radius;
	}
	
	public void setRadius(int value)
	{
		_radius = value;
	}
	
	/*
	 * from NpcAI
	 */
	public void moveToNextPoint()
	{
		final Player player = getOwner();
		
		// Choose the nearest Location if we weren't on a route.
		if (!_isOnARoute)
		{
			final Location nearestNode = getNodes().stream().min(Comparator.comparingDouble(wl -> player.distance3D(wl))).get();
			_index = getNodes().indexOf(nearestNode);
		}
		else if (player.isIn3DRadius(getNodes().get(_index), 50))
		{
			// Player está com problemas para se movimentar
			if (player.getMove().getGeoPathFailCount() >= 10)
			{
				reset();
				AutoFarmManager.getInstance().stopPlayer(player, "Character fora da rota");
				return;
			}
			
			// O player acabou de chegar no primeiro node, podemos liberar as outras ações da routine
			if (_isOnARoute && !_reachedFirstNode)
				_reachedFirstNode = true;
			
			// Actor is on reverse path. Decrease the index.
			if (_reversePath && _index > 0)
			{
				_index--;
				
				if (_index == 0)
					_reversePath = false;
			}
			// Set the next node value.
			else if (_index < getNodes().size() - 1)
				_index++;
			// Reset the index, and return the behavior to normal state.
			else
			{
				_index = getNodes().size() - 2;
				_reversePath = true;
			}
		}
		
		// Retrieve next node.
		Location node = getNodes().get(_index);
		
		// Test the path. If no path is found, we set the reverse path.
		if (!GeoEngine.getInstance().canMoveToTarget(player.getPosition(), node))
		{
			final List<Location> path = GeoEngine.getInstance().findPath(player.getX(), player.getY(), player.getZ(), node.getX(), node.getY(), node.getZ(), true, null);
			if (path.isEmpty())
			{
				player.getMove().addGeoPathFailCount();
				
				if (_index == 0)
				{
					_index = getNodes().size() - 2;
					_reversePath = true;
				}
				else
					_index--;
				
				node = getNodes().get(_index);
			}
		}
		
		player.getAI().tryToMoveTo(node, null);
		_isOnARoute = true;
	}
	
	/*
	 * Adaptado de WorldObject
	 */
	private final <A extends WorldObject> List<A> getKnownTypeInRadius(Class<A> type, int radius)
	{
		final List<A> result = new ArrayList<>();
		final int depth = (radius <= 2048) ? 1 : (int) ((radius / 2048) + 1);
		
		for (Location loc : getNodes())
		{
			final WorldRegion wr = World.getInstance().getRegion(loc);
			wr.forEachRegion(depth, r -> r.forEachType(type, o -> MathUtil.checkIfInRange(radius, o, loc, true), result::add));
			
		}
		return result;
	}
	
	// aCis < 409
//	@SuppressWarnings("unchecked")
//	private final <A> List<A> getKnownTypeInRadius(Class<A> type, int radius)
//	{
//		final List<A> result = new ArrayList<>();
//		final int depth = (radius <= 2048) ? 1 : (int) ((radius / 2048) + 1);
//		
//		for (Location loc : getNodes())
//		{
//			for (WorldRegion reg : World.getInstance().getRegion(loc).getSurroundingRegions(depth))
//			{
//				for (WorldObject obj : reg.getObjects())
//				{
//					if (!type.isAssignableFrom(obj.getClass()) || !MathUtil.checkIfInRange(radius, obj, loc, true)) 
//						continue;
//					
//					result.add((A) obj);
//				}
//			}
//		}
//		
//		return result;
//	}
}
