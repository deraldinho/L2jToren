package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.autofarm.AutoFarmManager;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AutoFarmHandler implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		201
	};

	@Override
	public void useUserCommand(int id, Player player)
	{
		AutoFarmManager.getInstance().showIndexWindow(player);
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}