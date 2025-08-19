package net.sf.l2j.loginserver;

import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.SQLException; // Added this line
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;

import net.sf.l2j.commons.crypt.BCrypt;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.crypt.ScrambledKeyPair;
import net.sf.l2j.loginserver.data.manager.GameServerManager;
import net.sf.l2j.loginserver.data.manager.IpBanManager;
import net.sf.l2j.loginserver.data.sql.AccountTable;
import net.sf.l2j.loginserver.enums.AccountKickedReason;
import net.sf.l2j.loginserver.enums.LoginClientState;
import net.sf.l2j.loginserver.model.Account;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.serverpackets.AccountKicked;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;
import net.sf.l2j.loginserver.network.serverpackets.LoginOk;
import net.sf.l2j.loginserver.network.serverpackets.ServerList;

/**
 * Gerencia o processo de login, incluindo autenticação e gerenciamento de sessão.
 */
public class LoginController
{
	private static final CLogger LOGGER = new CLogger(LoginController.class.getName());
	
	// Tempo limite para o login em milissegundos.
	public static final int LOGIN_TIMEOUT = 60 * 1000;
	
	// Mapa de clientes que estão atualmente autenticados no login server.
	private final Map<String, LoginClient> _clients = new ConcurrentHashMap<>();
	// Mapa de tentativas de login falhas por endereço IP.
	private final Map<InetAddress, Integer> _failedAttempts = new ConcurrentHashMap<>();
	
	// Pares de chaves RSA para comunicação segura.
	protected ScrambledKeyPair[] _keyPairs;
	
	// Chaves Blowfish para criptografia de pacotes.
	protected byte[][] _blowfishKeys;
	private static final int BLOWFISH_KEYS = 20;
	
	protected LoginController()
	{
		_keyPairs = new ScrambledKeyPair[10];
		
		// Gera as chaves criptográficas.
		try
		{
			final KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
			final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
			
			// Inicializa o gerador de pares de chaves.
			keygen.initialize(spec);
			
			// Gera o conjunto inicial de pares de chaves.
			for (int i = 0; i < 10; i++)
				_keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
			
			LOGGER.info("Cache de 10 pares de chaves RSA (2048 bits) para comunicação com o cliente gerado.");
			
			// Testa o cifrador.
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, _keyPairs[0].getKeyPair().getPrivate());
			
			// Armazena as chaves para a comunicação Blowfish.
			_blowfishKeys = new byte[BLOWFISH_KEYS][16];
			
			for (int i = 0; i < BLOWFISH_KEYS; i++)
			{
				for (int j = 0; j < _blowfishKeys[i].length; j++)
					_blowfishKeys[i][j] = (byte) (Rnd.get(255) + 1);
			}
			LOGGER.info("Armazenadas {} chaves para comunicação Blowfish.", _blowfishKeys.length);
		}
		catch (GeneralSecurityException gse)
		{
			LOGGER.error("Falha ao gerar as chaves.", gse);
		}
		
		// Thread para "limpar" conexões inativas.
		final Thread purge = new PurgeThread();
		purge.setDaemon(true);
		purge.start();
	}
	
	/**
	 * @return Uma chave Blowfish aleatória do cache.
	 */
	public byte[] getRandomBlowfishKey()
	{
		return Rnd.get(_blowfishKeys);
	}
	
	/**
	 * Remove um cliente autenticado do mapa de clientes.
	 * @param account O nome da conta a ser removida.
	 */
	public void removeAuthedLoginClient(String account)
	{
		if (account == null)
			return;
		
		_clients.remove(account);
	}
	
	/**
	 * @param account O nome da conta.
	 * @return O LoginClient autenticado para a conta especificada, ou null se não houver.
	 */
	public LoginClient getAuthedClient(String account)
	{
		return _clients.get(account);
	}
	
	/**
	 * Atualiza o contador de tentativas. Se o número máximo for atingido, o cliente será banido.
	 * @param address O {@link InetAddress} a ser testado.
	 */
	private void recordFailedAttempt(InetAddress address)
	{
		final int attempts = _failedAttempts.merge(address, 1, (k, v) -> k + v);
		if (attempts >= Config.LOGIN_TRY_BEFORE_BAN)
		{
			// Adiciona um ban para o endereço IP fornecido.
			IpBanManager.getInstance().addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000L);
			
			// Limpa todas as tentativas de login falhas para este IP.
			_failedAttempts.remove(address);
			
			LOGGER.info("O endereço de IP: {} foi banido devido a muitas tentativas de login.", address.getHostAddress());
		}
	}
	
	/**
	 * Se as senhas não corresponderem, registra a tentativa falha e, eventualmente, bane o {@link InetAddress} se AUTO_CREATE_ACCOUNTS estiver desativado.
	 * @param client O {@link LoginClient} a ser eventualmente banido após várias tentativas falhas.
	 * @param login O login {@link String} a ser testado.
	 * @param password A senha {@link String} a ser testada.
	 */
	public void retrieveAccountInfo(LoginClient client, String login, String password)
	{
		final InetAddress addr = client.getConnection().getInetAddress();
		final long currentTime = System.currentTimeMillis();
		
		// Recupera ou cria (se a criação automática estiver ativada) uma conta com base no login e senha fornecidos.
		Account account = null;
		try
		{
			account = AccountTable.getInstance().getAccount(login);
		}
		catch (SQLException e)
		{
			LOGGER.error("Erro de SQL ao recuperar informações da conta para {}.", e, login);
			client.close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		if (account == null)
		{
			// A criação automática está desativada, adiciona uma tentativa falha.
			if (!Config.AUTO_CREATE_ACCOUNTS)
			{
				recordFailedAttempt(addr);
				client.close(LoginFail.REASON_USER_OR_PASS_WRONG);
				return;
			}
			
			// Gera uma conta e alimenta a variável.
			try
			{
				account = AccountTable.getInstance().createAccount(login, BCrypt.hashPw(password), currentTime);
			}
			catch (SQLException e)
			{
				LOGGER.error("Erro de SQL ao criar a conta para {}.", e, login);
				client.close(LoginFail.REASON_ACCESS_FAILED);
				return;
			}
			if (account == null)
			{
				client.close(LoginFail.REASON_ACCESS_FAILED);
				return;
			}
			
			// Limpa todas as tentativas de login falhas.
			_failedAttempts.remove(addr);
			
			LOGGER.info("Conta '{}' criada automaticamente.", login);
		}
		else
		{
			// Verifica se a senha não criptografada corresponde a uma que foi previamente hasheada.
			if (!BCrypt.checkPw(password, account.getPassword()))
			{
				recordFailedAttempt(addr);
				client.close(LoginFail.REASON_PASS_WRONG);
				return;
			}
			
			// Limpa todas as tentativas de login falhas.
			_failedAttempts.remove(addr);
			
			// Atualiza a hora do último acesso da conta.
			try
			{
				if (!AccountTable.getInstance().setAccountLastTime(login, currentTime))
				{
					client.close(LoginFail.REASON_ACCESS_FAILED);
					return;
				}
			}
			catch (SQLException e)
			{
				LOGGER.error("Erro de SQL ao atualizar o último acesso da conta para {}.", e, login);
				client.close(LoginFail.REASON_ACCESS_FAILED);
				return;
			}
		}
		
		// A conta está banida, retorna.
		if (account.getAccessLevel() < 0)
		{
			client.close(new AccountKicked(AccountKickedReason.PERMANENTLY_BANNED));
			return;
		}
		
		// A conta já está no login server, retorna.
		final GameServerInfo gsi = getAccountOnGameServer(login);
		if (gsi != null)
		{
			client.close(LoginFail.REASON_ACCOUNT_IN_USE);
			
			if (gsi.isAuthed())
				gsi.getGameServerThread().kickPlayer(login);
			
			return;
		}
		
		// A conta já está no game server, fecha o cliente anterior.
		final LoginClient oldClient = _clients.putIfAbsent(login, client);
		if (oldClient != null)
		{
			oldClient.close(LoginFail.REASON_ACCOUNT_IN_USE);
			removeAuthedLoginClient(login);
			client.close(LoginFail.REASON_ACCOUNT_IN_USE);
			return;
		}
		
		account.setClientIp(addr);
		
		client.setAccount(account);
		client.setState(LoginClientState.AUTHED_LOGIN);
		client.setSessionKey(new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt()));
		client.sendPacket((Config.SHOW_LICENCE) ? new LoginOk(client.getSessionKey()) : new ServerList(account));
	}
	
	/**
	 * @param account O nome da conta.
	 * @return A SessionKey para a conta especificada, ou null se não houver.
	 */
	public SessionKey getKeyForAccount(String account)
	{
		final LoginClient client = _clients.get(account);
		return (client == null) ? null : client.getSessionKey();
	}
	
	/**
	 * @param account O nome da conta.
	 * @return As informações do GameServer onde a conta está, ou null se não estiver em nenhum.
	 */
	public GameServerInfo getAccountOnGameServer(String account)
	{
		for (GameServerInfo gsi : GameServerManager.getInstance().getRegisteredGameServers().values())
		{
			final GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return gsi;
		}
		return null;
	}
	
	/**
	 * @return Um dos pares de chaves {@link ScrambledKeyPair} em cache para se comunicar com os Login Clients.
	 */
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return Rnd.get(_keyPairs);
	}
	
	/**
	 * Thread para limpar conexões de clientes que estão inativos por muito tempo.
	 */
	private class PurgeThread extends Thread
	{
		public PurgeThread()
		{
			setName("PurgeThread");
		}
		
		@Override
		public void run()
		{
			while (!isInterrupted())
			{
				for (LoginClient client : _clients.values())
				{
					if ((client.getConnectionStartTime() + LOGIN_TIMEOUT) < System.currentTimeMillis())
						client.close(LoginFail.REASON_ACCESS_FAILED);
				}
				
				try
				{
					Thread.sleep(LOGIN_TIMEOUT / 2);
				}
				catch (InterruptedException e)
				{
					LOGGER.info("A PurgeThread foi interrompida.", e);
					return;
				}
			}
		}
	}
	
	public static LoginController getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LoginController INSTANCE = new LoginController();
	}
}
