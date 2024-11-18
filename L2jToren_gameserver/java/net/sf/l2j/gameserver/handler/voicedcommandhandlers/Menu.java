package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;



public class Menu implements IVoicedCommandHandler
{
    private static final String[] _voicedCommands =
            {
                    "menu",
                    "setPartyRefuse",
                    "setTradeRefuse",
                    "setbuffsRefuse",
                    "setMessageRefuse",
            };

    private static final String ACTIVED = "<font color=00FF00>ON</font>";
    private static final String DESATIVED = "<font color=FF0000>OFF</font>";

    @Override
    public boolean useVoicedCommand(String command, Player player, String target)
    {
        if (command.equals("menu"))
            showHtml(player);

        else if (command.equals("setPartyRefuse"))
        {
            if (player.isPartyInRefuse())
                player.setIsPartyInRefuse(false);
            else
                player.setIsPartyInRefuse(true);
            showHtml(player);
        }

        else if (command.equals("setTradeRefuse"))
        {
            if (player.getTradeRefusal())
                player.setTradeRefusal(false);
            else
                player.setTradeRefusal(true);
            showHtml(player);
        }

        else if (command.equals("setMessageRefuse"))
        {
            if (player.isInRefusalMode())
                player.setInRefusalMode(false);
            else
                player.setInRefusalMode(true);
            showHtml(player);
        }
        else if (command.equals("setbuffsRefuse"))
        {
            if (player.isBuffProtected())
                player.setIsBuffProtected(false);
            else
                player.setIsBuffProtected(true);
            showHtml(player);
        }
        else if (command.startsWith("autobank"))
        {
            if (player.isAutoGb())
            {
                player.setAutoGb(false);
                player.sendMessage("Auto Goldbar desativado");
            }
            else{
                player.setAutoGb(true);
                player.sendMessage("Auto Goldbar ativado");
            }
            showHtml(player);
        }
        return true;
    }

    protected static void showHtml(Player player)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/menu.htm");  //getAllPlayersCount()
        html.replace("%online%", World.getInstance().getPlayers().size());
        html.replace("%partyRefusal%", player.isPartyInRefuse() ? ACTIVED : DESATIVED);
        html.replace("%tradeRefusal%", player.getTradeRefusal() ? ACTIVED : DESATIVED);
        html.replace("%buffsRefusal%", player.isBuffProtected() ? ACTIVED : DESATIVED);
        html.replace("%messageRefusal%", player.isInRefusalMode() ? ACTIVED : DESATIVED);
        html.replace("%autobank%", player.isAutoGb() ? ACTIVED : DESATIVED);
        player.sendPacket(html);
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}