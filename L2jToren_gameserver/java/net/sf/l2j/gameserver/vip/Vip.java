package net.sf.l2j.gameserver.vip;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.StatSet;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.xml.VipData;
import net.sf.l2j.gameserver.model.actor.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Vip {

    private static final CLogger LOGGER = new CLogger(Vip.class.getName());

    private int charId;
    private int vipLevel;
    private long vipExp;
    private long vipTime;


    // Construtor para inicializar o VIP com base em StatSet.
    public Vip(StatSet set) {
        this.charId = set.getInteger("char_id", charId); // Valor padrão 0
        this.vipLevel = set.getInteger("vip_level", Config.DEFAULT_VIP_LEVEL);
        this.vipExp = set.getLong("vip_exp", Config.DEFAULT_VIP_EXP);
        this.vipTime = set.getLong("vip_time", Config.DEFAULT_VIP_TIME);

    }

    // Cria um StatSet com os dados necessários do VIP.
    public StatSet toStatSet() {
        StatSet set = new StatSet();
        set.set("char_id", charId);
        set.set("vip_level", vipLevel);
        set.set("vip_exp", vipExp);
        set.set("vip_time", vipTime);
        return set;
    }


    // Getters e setters com validação.
    public int getCharId() {
        return charId;
    }

    public void setCharId(int charId) {
        this.charId = charId;

    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        if (vipLevel < 0 || vipLevel > 5) {
            throw new IllegalArgumentException("Nível VIP deve estar entre 0 e 5.");
        }
        this.vipLevel = vipLevel;
    }

    public long getVipExp() {
        return vipExp;
    }

    public void setVipExp(long vipExp) {
        if (vipExp < 0) {
            throw new IllegalArgumentException("Experiência VIP não pode ser negativa.");
        }
        this.vipExp = vipExp;
    }

    public long getVipTime() {
        return vipTime;
    }

    public void setVipTime(long vipTime) {
        if (vipTime < 0) {
            throw new IllegalArgumentException("Tempo VIP não pode ser negativo.");
        }
        this.vipTime = vipTime;
    }

    public void addVipExp(long vipExp) {
        this.vipExp += vipExp;
        if (this.vipExp >= VipData.getInstance().getVip(vipLevel).requiredExpToVipLevelUp()) {
            vipLevelUp();
        }
    }

    private void vipLevelUp() {
        vipLevel++;
    }

    @Override
    public String toString() {
        return "Vip{" +
                "charId=" + charId +
                ", vipLevel=" + vipLevel +
                ", vipExp=" + vipExp +
                ", vipTime=" + vipTime +
                '}';
    }

    public long requiredExpToVipLevelUp() {
        return getVipExp();
    }
}
