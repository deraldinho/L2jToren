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

    private static final String INSERT_VIP = "INSERT INTO vip_system (char_id, vip_level, vip_exp, vip_time) VALUES (?,?,?,?)";
    private static final String UPDATE_VIP = "UPDATE vip_system SET vip_level =?, vip_exp =?, vip_time =? WHERE char_id =?";
    private static final String SELECT_VIP = "SELECT * FROM vip_system WHERE char_id =?";

    private int charId;
    private int vipLevel;
    private long vipExp;
    private long vipTime;

    // Construtor para inicializar o VIP com base em um objeto Player.
    public Vip(Player player) {
        this.charId = player.getObjectId();
        this.vipLevel = Config.DEFAULT_VIP_LEVEL;
        this.vipExp = Config.DEFAULT_VIP_EXP;
        this.vipTime = 0;
        saveOrUpdate();
    }

    // Construtor para inicializar o VIP com base em StatSet.
    public Vip(StatSet set) {
        this.charId = set.getInteger("char_id", 0); // Valor padrão 0
        this.vipLevel = set.getInteger("vip_level", Config.DEFAULT_VIP_LEVEL);
        this.vipExp = set.getLong("vip_exp", 0L);
        this.vipTime = set.getLong("vip_time", 0L);
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

    // Salva ou atualiza os dados VIP no banco de dados.
    private void saveOrUpdate() {
        if (exists(charId)) {
            update();
        } else {
            create();
        }
    }

    // Cria uma nova entrada VIP no banco de dados.
    private void create() {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_VIP)) {
            ps.setInt(1, charId);
            ps.setInt(2, vipLevel);
            ps.setLong(3, vipExp);
            ps.setLong(4, vipTime);
            ps.executeUpdate();
            LOGGER.info("VIP inserido com sucesso para charId: {}", charId);
        } catch (SQLException e) {
            LOGGER.error("Erro ao inserir VIP para charId: " + charId, e);
        }
    }

    // Atualiza uma entrada VIP existente no banco de dados.
    private void update() {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_VIP)) {
            ps.setInt(1, vipLevel);
            ps.setLong(2, vipExp);
            ps.setLong(3, vipTime);
            ps.setInt(4, charId);
            ps.executeUpdate();
            LOGGER.info("VIP atualizado com sucesso para charId: {}", charId);
        } catch (SQLException e) {
            LOGGER.error("Erro ao atualizar VIP para charId: " + charId, e);
        }
    }

    // Verifica se uma entrada VIP já existe no banco de dados.
    private boolean exists(int charId) {
        return getVip(charId) != null;
    }

    // Recupera dados VIP de um personagem específico do banco de dados.
    public static Vip getVip(int charId) {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_VIP)) {
            ps.setInt(1, charId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LOGGER.info("VIP carregado com sucesso para charId: {}", charId);
                    StatSet set = new StatSet();
                    set.set("char_id", charId);
                    set.set("vip_level", rs.getInt("vip_level"));
                    set.set("vip_exp", rs.getLong("vip_exp"));
                    set.set("vip_time", rs.getLong("vip_time"));
                    return new Vip(set);
                } else {
                    LOGGER.warn("Nenhum dado VIP encontrado para charId: {}", charId);
                }
            } catch (SQLException e) {
                LOGGER.error("Erro ao processar ResultSet para charId: {}", charId, e);
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao estabelecer conexão para obter VIP para charId: {}", charId, e);
        }
        return null;
    }

    // Restaura dados VIP de um personagem específico do banco de dados.
    public static Vip restoreVip(int charId) {
        Vip vip = getVip(charId);
        if (vip == null) {
            StatSet set = new StatSet();
            set.set("char_id", charId);
            set.set("vip_level", Config.DEFAULT_VIP_LEVEL);
            set.set("vip_exp", Config.DEFAULT_VIP_EXP);
            set.set("vip_time", 0L);
            vip = new Vip(set);
        }
        return vip;
    }

    // Salva os dados VIP no banco de dados.
    public void storeVipData() {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_VIP)) {
            ps.setInt(1, getVipLevel());
            ps.setLong(2, getVipExp());
            ps.setLong(3, getVipTime());
            ps.setInt(4, getCharId());
            ps.execute();
            LOGGER.info("Dados VIP do jogador com charId {} salvos com sucesso.", charId);
        } catch (SQLException e) {
            LOGGER.error("Erro ao salvar dados VIP para charId: " + charId, e);
        }
    }

    // Getters e setters com validação.
    public int getCharId() {
        return charId;
    }

    public void setCharId(int charId) {
        this.charId = charId;
        update();
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        if (vipLevel < 0 || vipLevel > 5) {
            throw new IllegalArgumentException("Nível VIP deve estar entre 0 e 5.");
        }
        this.vipLevel = vipLevel;
        update();
    }

    public long getVipExp() {
        return vipExp;
    }

    public void setVipExp(long vipExp) {
        if (vipExp < 0) {
            throw new IllegalArgumentException("Experiência VIP não pode ser negativa.");
        }
        this.vipExp = vipExp;
        update();
    }

    public long getVipTime() {
        return vipTime;
    }

    public void setVipTime(long vipTime) {
        if (vipTime < 0) {
            throw new IllegalArgumentException("Tempo VIP não pode ser negativo.");
        }
        this.vipTime = vipTime;
        update();
    }

    public void addVipExp(long vipExp) {
        this.vipExp += vipExp;
        update();
        if (this.vipExp >= VipData.getInstance().getVip(vipLevel).requiredExpToVipLevelUp()) {
            vipLevelUp();
        }
    }

    private void vipLevelUp() {
        vipLevel++;
        update();
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
