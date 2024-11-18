package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class AutoGoldBar implements Runnable
{
    @Override
    public final void run()
    {
        if (_players.isEmpty())
            return;

        for (Map.Entry<Player, Long> entry : _players.entrySet())
        {
            final Player player = entry.getKey();
            for (IntIntHolder gold : Config.BANKING_SYSTEM_ITEM)
            {
                int adenaRequired = Config.BANKING_SYSTEM_ADENA;
                String goldName = ItemData.getInstance().getTemplate(gold.getId()).getName();
                if(player.getInventory().getItemCount(57) > adenaRequired)
                {
                    if (player.reduceAdena(adenaRequired, true))
                    {
                        player.addItem(gold.getId(), gold.getValue(), true);
                        player.sendMessage("Converted " + adenaRequired + " Adena into " + gold.getValue() + " " + goldName + ".");
                        player.sendPacket(new PlaySound("ItemSound3.ItemSound3.sys_exchange_success"));
                    }
                }
            }
        }
    }

    private final Map<Player, Long> _players = new ConcurrentHashMap<>();

    protected AutoGoldBar()
    {
        // Run task each 10 second.
        ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
    }

    public final void add(Player player)
    {
        _players.put(player, System.currentTimeMillis());
    }

    public final void remove(Creature player)
    {
        _players.remove(player);
    }

    public static final AutoGoldBar getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final AutoGoldBar _instance = new AutoGoldBar();
    }
}