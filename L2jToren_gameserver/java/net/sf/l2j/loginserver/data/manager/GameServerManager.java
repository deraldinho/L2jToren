package net.sf.l2j.loginserver.data.manager;

import java.math.BigInteger;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.data.StatSet;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.loginserver.model.GameServerInfo;

import org.w3c.dom.Document;

/**
 * Gerencia todos os Game Servers registrados, tanto do banco de dados quanto os que estão atualmente online.
 */
public class GameServerManager implements IXmlReader
{
	private static final CLogger LOGGER = new CLogger(GameServerManager.class.getName());
	
	private static final int KEYS_SIZE = 10;
	
	private static final String LOAD_SERVERS = "SELECT * FROM gameservers";
	private static final String ADD_SERVER = "INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)";
	
	// Mapa de nomes de servidores (ID -> Nome) carregados de serverNames.xml.
	private final Map<Integer, String> _serverNames = new HashMap<>();
	// Mapa de todos os Game Servers registrados (online e offline).
	private final Map<Integer, GameServerInfo> _registeredServers = new ConcurrentHashMap<>();
	
	// Cache de pares de chaves RSA para a comunicação inicial com os Game Servers.
	private KeyPair[] _keyPairs;
	
	protected GameServerManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		// Carrega os nomes dos servidores do XML.
		parseFile("serverNames.xml");
		LOGGER.info("Carregados {} nomes de servidores.", _serverNames.size());
		
		// Carrega os servidores registrados do banco de dados.
		loadRegisteredGameServers();
		LOGGER.info("Carregados {} gameserver(s) registrados.", _registeredServers.size());
		
		// Inicializa os pares de chaves RSA.
		initRSAKeys();
		LOGGER.info("Cache de {} chaves RSA (2048 bits) para comunicação com gameservers gerado.", _keyPairs.length);
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "server", serverNode ->
		{
			final StatSet set = parseAttributes(serverNode);
			_serverNames.put(set.getInteger("id"), set.getString("name"));
		}));
	}
	
	/**
	 * Inicializa o cache de pares de chaves RSA.
	 */
	private void initRSAKeys()
	{
		try
		{
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4));
			
			_keyPairs = new KeyPair[KEYS_SIZE];
			for (int i = 0; i < KEYS_SIZE; i++)
				_keyPairs[i] = keyGen.genKeyPair();
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.error("Erro ao carregar as chaves RSA para a comunicação com o Game Server.", e);
		}
	}
	
	/**
	 * Carrega os servidores de jogo registrados a partir do banco de dados.
	 */
	private void loadRegisteredGameServers()
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SERVERS);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final int id = rs.getInt("server_id");
				_registeredServers.put(id, new GameServerInfo(id, stringToHex(rs.getString("hexid"))));
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Erro ao carregar os gameservers registrados.", e);
		}
	}
	
	/**
	 * @return O mapa de todos os Game Servers registrados.
	 */
	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _registeredServers;
	}
	
	/**
	 * Tenta registrar um Game Server com o primeiro ID disponível.
	 * @param gsi As informações do Game Server a ser registrado.
	 * @return true se o registro for bem-sucedido, false caso contrário.
	 */
	public boolean registerWithFirstAvailableId(GameServerInfo gsi)
	{
		for (int id : _serverNames.keySet())
		{
			if (_registeredServers.putIfAbsent(id, gsi) == null)
			{
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tenta registrar um Game Server com um ID específico.
	 * @param id O ID solicitado.
	 * @param gsi As informações do Game Server.
	 * @return true se o registro for bem-sucedido, false caso contrário.
	 */
	public boolean register(int id, GameServerInfo gsi)
	{
		if (!_registeredServers.containsKey(id))
		{
			_registeredServers.put(id, gsi);
			gsi.setId(id);
			return true;
		}
		return false;
	}
	
	/**
	 * Registra um Game Server no banco de dados.
	 * @param gsi As informações do Game Server a serem salvas.
	 */
	public void registerServerOnDB(GameServerInfo gsi)
	{
		registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getHostName());
	}
	
	/**
	 * Registra um Game Server no banco de dados.
	 * @param hexId O HexId do servidor.
	 * @param id O ID do servidor.
	 * @param hostName O Hostname do servidor.
	 */
	public void registerServerOnDB(byte[] hexId, int id, String hostName)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(ADD_SERVER))
		{
			ps.setString(1, hexToString(hexId));
			ps.setInt(2, id);
			ps.setString(3, hostName);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.error("Erro ao salvar os dados do gameserver.", e);
		}
	}
	
	/**
	 * @return O mapa de nomes de servidores.
	 */
	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}
	
	/**
	 * @return Um par de chaves RSA aleatório do cache.
	 */
	public KeyPair getKeyPair()
	{
		return Rnd.get(_keyPairs);
	}
	
	private static byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	private static String hexToString(byte[] hex)
	{
		return (hex == null) ? "null" : new BigInteger(hex).toString(16);
	}
	
	public static GameServerManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GameServerManager INSTANCE = new GameServerManager();
	}
}
