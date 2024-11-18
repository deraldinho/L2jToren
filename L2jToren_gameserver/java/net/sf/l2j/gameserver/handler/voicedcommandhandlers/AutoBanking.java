package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.taskmanager.AutoGoldBar;

public class AutoBanking implements IVoicedCommandHandler
{
    private static final String[] _voicedCommands =
            {
                    "gbstart",
                    "gbstop",
            };

    @Override
    public boolean useVoicedCommand(final String command, final Player player, final String target)
    {
        if (command.equalsIgnoreCase("gbstart"))
        {
            if (player.isAutoGb())
            {
                player.setAutoGb(false);
                AutoGoldBar.getInstance().remove(player);
            }
            else
            {
                player.setAutoGb(true);
                AutoGoldBar.getInstance().add(player);
            }
            Menu.showHtml(player);
        }
        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}