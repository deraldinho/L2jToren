package net.sf.l2j.gameserver.autofarm.zone;

import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmType;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;

public class AutoFarmOpen extends AutoFarmArea
{
	public AutoFarmOpen(int ownerId)
	{
		super(1, "Aberta", ownerId, AutoFarmType.OPEN);
	}
	
	@Override
	public List<Monster> getMonsters()
	{
		return getOwner().getKnownTypeInRadius(Monster.class, getProfile().getFinalRadius());
	}
	
	@Override
	public Set<String> getMonsterHistory()
	{
		_monsterHistory.addAll(getMonsters().stream().map(Monster::getName).toList());
		return _monsterHistory;
	}
	
	@Override
	public void visualizeZone(ExServerPrimitive debug)
	{
	}
}
