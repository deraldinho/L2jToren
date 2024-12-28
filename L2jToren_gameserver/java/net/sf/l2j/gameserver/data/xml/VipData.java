package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.StatSet;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.vip.Vip;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class VipData implements IXmlReader {

    private static final CLogger LOGGER = new CLogger(VipData.class.getName());

    private final Map<Integer, Vip> _vips = new HashMap<>();
    private int _maxVipLevel;

    protected VipData() {
        load();
    }

    @Override
    public void load() {
        _vips.clear();
        parseFile("./data/xml/vipLevels.xml");
        LOGGER.info("Loaded " + _vips.size() + " VIP levels.");
    }

    @Override
    public void parseDocument(Document doc, Path path) {
        // Parsing do documento XML para carregar os níveis VIP
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeName().equalsIgnoreCase("list")) {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if (d.getNodeName().equalsIgnoreCase("viplevel")) {
                        final StatSet set = new StatSet();

                        final int level = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());
                        final long requiredExpToLevelUp = Long.parseLong(d.getAttributes().getNamedItem("requiredExpToLevelUp").getNodeValue());

                        set.set("level", level);
                        set.set("requiredExpToLevelUp", requiredExpToLevelUp);

                        _vips.put(level, new Vip(set));

                        // Log para verificar o carregamento dos dados
                        LOGGER.info("Carregado nível VIP: " + level + " com exp necessária: " + requiredExpToLevelUp);

                        // Atualiza o nível máximo de VIP
                        if (level > _maxVipLevel) {
                            _maxVipLevel = level;
                        }
                    }
                }
            }
        }
    }

    public Vip getVip(int level) {
        // Recupera o objeto Vip associado ao nível fornecido
        Vip vip = _vips.get(level);

        // Verifica se o objeto Vip foi encontrado
        if (vip == null) {
            // Caso não seja encontrado, registra um aviso no log
            LOGGER.warn("Nível VIP {} não encontrado. Retornando nível VIP padrão.", level);

            // Define o Vip padrão (nível 0)
            vip = _vips.get(0); // Assume que nível 0 é o nível VIP padrão
        }
        // Retorna o objeto Vip correspondente ou o padrão
        return vip;
    }


    public long getRequiredExpForHighestVipLevel() {
        return _vips.get(_maxVipLevel).requiredExpToVipLevelUp();
    }

    public int getMaxVipLevel() {
        return _maxVipLevel;
    }

    public int getRealMaxVipLevel() {
        return _maxVipLevel - 1;
    }

    public static VipData getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final VipData INSTANCE = new VipData();
    }
}
