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

    private static final String SELECT_VIP_DATA = "SELECT vip_level, vip_exp, vip_time FROM vip_system WHERE char_id = ?";
    private static final String INSERT_VIP_DATA = "INSERT INTO vip_system (char_id, vip_level, vip_exp, vip_time) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_VIP_DATA = "UPDATE vip_system SET vip_level = ?, vip_exp = ?, vip_time = ? WHERE char_id = ?";
    private static final String DELETE_VIP_DATA = "DELETE FROM vip_system WHERE char_id = ?";

    private int charId;
    private int vipLevel;
    private long vipExp;
    private long vipTime;

    // Construtor para inicializar o VIP com base em StatSet.
    public Vip(StatSet set) {
        this.charId = set.getInteger("char_id", 0); // Valor padrão 0
        this.vipLevel = set.getInteger("vip_level", Config.DEFAULT_VIP_LEVEL);
        this.vipExp = set.getLong("vip_exp", Config.DEFAULT_VIP_EXP);
        this.vipTime = set.getLong("vip_time", Config.DEFAULT_VIP_TIME);
    }

    // Construtor para carregar VIP de um charId existente ou criar um novo com valores padrão.
    public Vip(int charId) {
        this.charId = charId;
        load();
    }

    // Construtor para carregar VIP de um Player existente ou criar um novo com valores padrão.
    public Vip(Player player) {
        this(player.getObjectId());
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
        if (vipLevel < 0 || vipLevel > 5) { // Assuming max VIP level is 5 based on validation
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
        // Assuming VipData.getInstance().getVip(vipLevel) returns an object that has requiredExpToVipLevelUp()
        if (this.vipLevel < 5 && this.vipExp >= VipData.getInstance().getVip(vipLevel).requiredExpToVipLevelUp()) {
            vipLevelUp();
        }
    }

    private void vipLevelUp() {
        if (vipLevel < 5) { // Prevent leveling up beyond max level
            vipLevel++;
            // Reset vipExp after level up if needed, or carry over excess exp
            // For now, let's assume excess exp carries over.
        }
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
        // Assuming VipData.getInstance().getVip(vipLevel) returns an object with requiredExpToVipLevelUp()
        // This method should return the experience required for the *current* vipLevel to level up.
        // If vipLevel is already max, it should return a very large number or handle it.
        if (vipLevel >= 5) { // Using 5 as max VIP level based on setVipLevel validation
            return Long.MAX_VALUE; // Or some other appropriate value
        }
        // Assuming VipData.getInstance().getVip(vipLevel) returns an object that has requiredExpToVipLevelUp()
        return VipData.getInstance().getVip(vipLevel).requiredExpToVipLevelUp();
    }

    /**
     * Loads VIP data for the current charId from the database.
     * If no data is found, initializes with default values.
     */
    private void load() {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_VIP_DATA)) {
            ps.setInt(1, charId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.vipLevel = rs.getInt("vip_level");
                    this.vipExp = rs.getLong("vip_exp");
                    this.vipTime = rs.getLong("vip_time");
                } else {
                    // If no data found, initialize with default values from Config
                    this.vipLevel = Config.DEFAULT_VIP_LEVEL;
                    this.vipExp = Config.DEFAULT_VIP_EXP;
                    this.vipTime = Config.DEFAULT_VIP_TIME;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load VIP data for charId " + charId + ": " + e.getMessage(), e);
            // Initialize with default values in case of error
            this.vipLevel = Config.DEFAULT_VIP_LEVEL;
            this.vipExp = Config.DEFAULT_VIP_EXP;
            this.vipTime = Config.DEFAULT_VIP_TIME;
        }
    }

    /**
     * Saves the current VIP data to the database.
     * Performs an UPDATE if the record exists, otherwise an INSERT.
     */
    public void save() {
        try (Connection con = ConnectionPool.getConnection()) {
            // Check if the record already exists
            boolean exists = false;
            try (PreparedStatement ps = con.prepareStatement(SELECT_VIP_DATA)) {
                ps.setInt(1, charId);
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                // Update existing record
                try (PreparedStatement ps = con.prepareStatement(UPDATE_VIP_DATA)) {
                    ps.setInt(1, vipLevel);
                    ps.setLong(2, vipExp);
                    ps.setLong(3, vipTime);
                    ps.setInt(4, charId);
                    ps.executeUpdate();
                }
            } else {
                // Insert new record
                try (PreparedStatement ps = con.prepareStatement(INSERT_VIP_DATA)) {
                    ps.setInt(1, charId);
                    ps.setInt(2, vipLevel);
                    ps.setLong(3, vipExp);
                    ps.setLong(4, vipTime);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to save VIP data for charId " + charId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the VIP data for the current charId from the database.
     */
    public void delete() {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_VIP_DATA)) {
            ps.setInt(1, charId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to delete VIP data for charId " + charId + ": " + e.getMessage(), e);
        }
    }
}
