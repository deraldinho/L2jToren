package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.taskmanager.AutoGoldBar;

public class BankSystem implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"deposit",
		"withdraw",
	};

	@Override
	public boolean useVoicedCommand(String command, Player player, String target)
	{
		if (!Config.BANKING_SYSTEM)
		{
			return false;
		}

		if (command.equals("deposit"))
		{
			for (IntIntHolder gold : Config.BANKING_SYSTEM_ITEM)
			{
				int adenaRequired = Config.BANKING_SYSTEM_ADENA;
				String goldName = ItemData.getInstance().getTemplate(gold.getId()).getName();
				if (player.getInventory().getItemCount(57) < adenaRequired)
				{
					player.sendMessage("Not enough Adena to deposit into " + goldName + ".");
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					return false;
				}
				if (player.reduceAdena(adenaRequired, true))
				{
					player.addItem(gold.getId(), gold.getValue(), true);
					player.sendMessage("Converted " + adenaRequired + " Adena into " + gold.getValue() + " " + goldName + ".");
					player.sendPacket(new PlaySound("ItemSound3.ItemSound3.sys_exchange_success"));
				}
				else
				{
					player.sendMessage("Failed to reduce Adena. Try again.");
				}
			}
		}
		else if (command.equals("withdraw"))
		{
			long currentAdena = player.getInventory().getItemCount(57, 0);
			long maxAdena = 2147483647L;
			for (IntIntHolder gold : Config.BANKING_SYSTEM_ITEM)
			{
				int adenaRequired = Config.BANKING_SYSTEM_ADENA;
				String goldName = ItemData.getInstance().getTemplate(gold.getId()).getName();
				if (player.getInventory().getItemCount(gold.getId()) < gold.getValue())
				{
					player.sendMessage("Not enough " + goldName + "(s) to exchange for Adena.");
					return false;
				}
				if (maxAdena - currentAdena < adenaRequired)
				{
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					player.sendMessage("Not enough space in inventory for the requested amount of Adena.");
					return false;
				}
				if (player.destroyItemByItemId(gold.getId(), gold.getValue(), true))
				{
					player.addAdena(adenaRequired, true);
					player.sendMessage("Exchanged " + gold.getValue() + " " + goldName + "(s) for " + adenaRequired + " Adena.");
					player.sendPacket(new PlaySound("ItemSound3.ItemSound3.sys_exchange_success"));
				}
				else
				{
					player.sendMessage("Failed to remove Gold Bars. Try again.");
				}
			}
		}

		return true;
	}



	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}