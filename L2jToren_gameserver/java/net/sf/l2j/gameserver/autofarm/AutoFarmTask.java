package net.sf.l2j.gameserver.autofarm;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.autofarm.AutoFarmManager.AutoFarmType;
import net.sf.l2j.gameserver.autofarm.zone.AutoFarmArea;

public class AutoFarmTask implements Runnable
{
	private int _runTick;

	public AutoFarmTask()
	{
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}

	@Override
	public void run()
	{
		_runTick++;

		// Routine
		AutoFarmManager.getInstance().getPlayers().parallelStream().filter(AutoFarmProfile::isEnabled).forEach(AutoFarmProfile::startRoutine);

		// Remover do world zonas n�o utilizadas
		if (_runTick >= 60)
		{
			for (AutoFarmProfile fp : AutoFarmManager.getInstance().getPlayers())
			{
				if (fp.isEnabled())
					continue;

				if (System.currentTimeMillis() > fp.getLastActiveTime() + TimeUnit.MINUTES.toMillis(10))
				{
					final List<AutoFarmArea> areas = fp.getAreas().values().stream().filter(a -> a.getId() != fp.getSelectedAreaId() && a.isFromDb() && a.getType() == AutoFarmType.ZONA && a.getFarmZone().isBuilt()).toList();
					areas.forEach(a -> a.getFarmZone().removeFromWorld());
				}
			}

			_runTick = 0;
		}
	}

	public static final AutoFarmTask getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final AutoFarmTask INSTANCE = new AutoFarmTask();
	}
}
