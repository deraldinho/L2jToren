package net.sf.l2j.gameserver.autofarm.zone;

import java.util.List;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;

public class AutoFarmZone extends AutoFarmArea
{
	private boolean _isBuilt;
	
	public AutoFarmZone(int zoneId, String name, int ownerId)
	{
		super(zoneId, name, ownerId, AutoFarmType.ZONA);
	}
	
	public AutoFarmZone(String name, int ownerId)
	{
		super(name, ownerId, AutoFarmType.ZONA);
	}

	@Override
	public void onEnter(Creature character)
	{
		if (character.getActingPlayer() != null)
		{
			// Somente monstros e o player que criou
			if (character.getObjectId() != getOwnerId())
				return;
			
			if (!getProfile().isEnabled())
				character.sendMessage("Você entrou em uma zona de AutoFarm.");
		}
		else
			getMonsterHistory().add(character.getName());
		
		character.setInsideZone(ZoneId.AUTO_FARM, true);
	}

	@Override
	public void onExit(Creature character)
	{
		if (character.getActingPlayer() != null && !getProfile().isEnabled())
			character.sendMessage("Você saiu da zona de AutoFarm.");
		
		character.setInsideZone(ZoneId.AUTO_FARM, false);
	}
	
	@Override
	public void visualizeZone(ExServerPrimitive debug)
	{
		getZone().visualizeZone("ZONA " + getName(), debug);
	}
	
	@Override
	public AutoFarmZone getFarmZone()
	{
		return this;
	}
	
	public boolean isBuilt()
	{
		return _isBuilt;
	}
	
	@Override
	public List<Monster> getMonsters()
	{
		return getKnownTypeInside(Monster.class);
	}
	
	public Location findValidLocation()
	{
		Location center = getZoneZ().findPointInCenter();
		int attempt = 0;
		int radius = 100; // distância inicial para afastar

		while (attempt < 10)
		{
			// Máximo de 10 tentativas
			if (getOwner().getMove().maybeMoveToLocation(center, 0, true, true))
			{
				return center; // Encontrou um local válido
			}

			// Aumenta a distância do centro e tenta novamente
			double angle = Math.random() * 2 * Math.PI; // Gera um ângulo aleatório
			int newX = (int) (center.getX() + radius * Math.cos(angle));
			int newY = (int) (center.getY() + radius * Math.sin(angle));

			// Garante que o novo ponto ainda esteja dentro dos limites Z
			int newZ = center.getZ();

			center = new Location(newX, newY, newZ);
			radius += 100; // Aumenta o raio para a próxima tentativa
			attempt++;
		}

		return null; // Se não encontrar um local válido após 10 tentativas
	}
	
	public boolean tryGoBackInside()
	{
		final Location way = getWayIn();
		if (way != null)
		{
			getOwner().getAI().tryToMoveTo(way, null);
			return true;
		}
		
		return false;
	}
	
	private Location getWayIn()
	{
		// Enquanto o player estiver no caminho até o centro ou o monstro, ele entrará na zona
		final Player owner = getOwner();
		final Location center = getZoneZ().findPointInCenter();
		
		if (GeoEngine.getInstance().canMoveToTarget(owner, center))
			return center;
		
		final List<Monster> monsters = getMonsters().stream().filter(m -> GeoEngine.getInstance().canMoveToTarget(owner, m)).toList();
		if (!monsters.isEmpty())
			return Rnd.get(monsters).getPosition().clone();
		
		// Localização aleatória dentro da zona
		for (int i = 0; i < 10; i++)
		{
			final Location loc = getZoneZ().getRandomPoint();
			if (loc != null && GeoEngine.getInstance().canMoveToTarget(owner, loc))
				return loc;
		}
		
		// O player terá que se locomover manualmente
		return null;
	}
	
	
	public void addToWorld()
	{
		if (_isBuilt)
			return;

		updateWorldRegions();
		_isBuilt = true;
	}
	
	public void removeFromWorld()
	{
		if (!_isBuilt)
			return;

		getCreatures().forEach(c -> removeCreature(c));
		updateWorldRegions();
		_isBuilt = false;
	}
	
	private void updateWorldRegions()
	{
		final WorldRegion[][] regions = World.getInstance().getWorldRegions();
		for (int x = 0; x < regions.length; x++)
		{
			final int xLoc = World.getRegionX(x);
			final int xLoc2 = World.getRegionX(x + 1);
			for (int y = 0; y < regions[x].length; y++)
			{
				if (getZone().intersectsRectangle(xLoc, xLoc2, World.getRegionY(y), World.getRegionY(y + 1)))
				{
					if (_isBuilt)
						regions[x][y].removeZone(this);
					else
					{
						// from ZoneManager
						// Revalidate creatures in zones.
						for (WorldObject object : regions[x][y].getObjects())
						{
							if (object instanceof Creature)
								revalidateInZone(((Creature) object));
						}
						
						regions[x][y].addZone(this);
					}
				}
			}
		}
	}
}
