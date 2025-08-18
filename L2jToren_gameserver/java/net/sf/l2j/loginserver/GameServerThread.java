package net.sf.l2j.loginserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.SQLException; // Added this line
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.crypt.NewCrypt;
import net.sf.l2j.loginserver.data.manager.GameServerManager;
import net.sf.l2j.loginserver.data.manager.IpBanManager;
import net.sf.l2j.loginserver.data.sql.AccountTable;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.gameserverpackets.BlowFishKey;
import net.sf.l2j.loginserver.network.gameserverpackets.ChangeAccessLevel;
import net.sf.l2j.loginserver.network.gameserverpackets.GameServerAuth;
import net.sf.l2j.loginserver.network.gameserverpackets.PlayerAuthRequest;
import net.sf.l2j.loginserver.network.gameserverpackets.PlayerInGame;
import net.sf.l2j.loginserver.network.gameserverpackets.PlayerLogout;
import net.sf.l2j.loginserver.network.gameserverpackets.ServerStatus;
import net.sf.l2j.loginserver.network.loginserverpackets.AuthResponse;
import net.sf.l2j.loginserver.network.loginserverpackets.InitLS;
import net.sf.l2j.loginserver.network.loginserverpackets.KickPlayer;
import net.sf.l2j.loginserver.network.loginserverpackets.LoginServerFail;
import net.sf.l2j.loginserver.network.loginserverpackets.PlayerAuthResponse;
import net.sf.l2j.loginserver.network.serverpackets.ServerBasePacket;

/**
 * Representa a thread de comunicação entre o Login Server e um único Game Server.
 */
public class GameServerThread extends Thread
{
	private static final CLogger LOGGER = new CLogger(GameServerThread.class.getName());
	
	// Conjunto de contas atualmente online neste Game Server.
	private final Set<String> _accountsOnGameServer = new HashSet<>();
	
	private final Socket _connection;
	private final String _connectionIp;
	
	// Chaves RSA para a troca inicial de chaves de criptografia.
	private final RSAPublicKey _publicKey;
	private final RSAPrivateKey _privateKey;
	
	private InputStream _in;
	private OutputStream _out;
	
	// Criptografia Blowfish para a comunicação de pacotes.
	private NewCrypt _blowfish;
	
	// Informações sobre o Game Server conectado.
	private GameServerInfo _gsi;
	
	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
		
try
		{
			_in = _connection.getInputStream();
			_out = new BufferedOutputStream(_connection.getOutputStream());
		}
		catch (IOException e)
		{
			LOGGER.error("Não foi possível obter os streams de entrada/saída do gameserver.", e);
		}
		
		// Obtém um par de chaves do gerenciador para a comunicação RSA.
		final KeyPair pair = GameServerManager.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		
		// Inicializa a criptografia com uma chave estática temporária.
		_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
		
		start();
	}
	
	@Override
	public void run()
	{
		// Garante que não haverá mais processamento para esta conexão se o servidor for considerado banido.
		if (IpBanManager.getInstance().isBannedAddress(_connection.getInetAddress()))
		{
			LOGGER.info("O gameserver banido com o IP {} tentou se registrar.", _connection.getInetAddress().getHostAddress());
			forceClose(LoginServerFail.REASON_IP_BANNED);
			return;
		}
		
try
		{
			// Envia o pacote de inicialização com a chave pública RSA.
			sendPacket(new InitLS(_publicKey.getModulus().toByteArray()));
			
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			for (;;)
			{
				// Lê o tamanho do pacote.
				lengthLo = _in.read();
				lengthHi = _in.read();
				length = lengthHi * 256 + lengthLo;
				
				if (lengthHi < 0 || length < 2 || _connection.isClosed())
					break;
				
				byte[] data = new byte[length - 2];
				
				int receivedBytes = 0;
				int newBytes = 0;
				// Lê os dados do pacote.
				while (newBytes != -1 && receivedBytes < length - 2)
				{
					newBytes = _in.read(data, 0, length - 2);
					receivedBytes = receivedBytes + newBytes;
				}
				
				if (receivedBytes != length - 2)
					break;
				
				// Descriptografa se já tivermos uma chave Blowfish.
				_blowfish.decrypt(data, 0, data.length);
				
				// Verifica o checksum para garantir a integridade dos dados.
				checksumOk = NewCrypt.verifyChecksum(data);
				if (!checksumOk)
				{
					LOGGER.warn("Checksum falhou para o gameserver {}. Fechando conexão.", _connectionIp);
					forceClose(LoginServerFail.REASON_BAD_CHECKSUM);
					return;
				}
				
				// Processa o pacote com base no seu opcode.
				int packetType = data[0] & 0xff;
				switch (packetType)
				{
					case 0x00: // BlowFishKey
						onReceiveBlowfishKey(data);
						break;
					
					case 0x01: // GameServerAuth
						onGameServerAuth(data);
						break;
					
					case 0x02: // PlayerInGame
						onReceivePlayerInGame(data);
						break;
					
					case 0x03: // PlayerLogout
						onReceivePlayerLogOut(data);
						break;
					
					case 0x04: // ChangeAccessLevel
						onReceiveChangeAccessLevel(data);
						break;
					
					case 0x05: // PlayerAuthRequest
						onReceivePlayerAuthRequest(data);
						break;
					
					case 0x06: // ServerStatus
						onReceiveServerStatus(data);
						break;
					
default:
						LOGGER.warn("Opcode desconhecido ({}) do gameserver, fechando conexão.", Integer.toHexString(packetType).toUpperCase());
						forceClose(LoginServerFail.NOT_AUTHED);
				}
			}
		}
		catch (IOException e)
		{
			LOGGER.debug("Não foi possível processar o pacote.", e);
		}
		finally
		{
			// Se o gameserver estava autenticado, marca-o como desconectado.
			if (isAuthed())
			{
				_gsi.setDown();
				LOGGER.info("GameServer [{}] {} foi definido como desconectado.", getServerId(), GameServerManager.getInstance().getServerNames().get(getServerId()));
			}
			// Remove o gameserver da lista de listeners.
			LoginServer.getInstance().getGameServerListener().removeGameServer(this);
			LoginServer.getInstance().getGameServerListener().removeFloodProtection(_connectionIp);
		}
	}
	
	/**
	 * Chamado ao receber a chave Blowfish do Game Server.
	 * @param data Os dados do pacote.
	 */
	private void onReceiveBlowfishKey(byte[] data)
	{
		final BlowFishKey bfk = new BlowFishKey(data, _privateKey);
		_blowfish = new NewCrypt(bfk.getKey());
	}
	
	/**
	 * Chamado ao receber o pacote de autenticação do Game Server.
	 * @param data Os dados do pacote.
	 */
	private void onGameServerAuth(byte[] data)
	{
		handleRegProcess(new GameServerAuth(data));
		
		if (isAuthed())
			sendPacket(new AuthResponse(_gsi.getId()));
	}
	
	/**
	 * Chamado ao receber a lista de jogadores no jogo.
	 * @param data Os dados do pacote.
	 */
	private void onReceivePlayerInGame(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerInGame pig = new PlayerInGame(data);
			for (String account : pig.getAccounts())
				_accountsOnGameServer.add(account);
		}
		else
			forceClose(LoginServerFail.NOT_AUTHED);
	}
	
	/**
	 * Chamado quando um jogador desloga do Game Server.
	 * @param data Os dados do pacote.
	 */
	private void onReceivePlayerLogOut(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerLogout plo = new PlayerLogout(data);
			_accountsOnGameServer.remove(plo.getAccount());
		}
		else
			forceClose(LoginServerFail.NOT_AUTHED);
	}
	
	/**
	 * Chamado quando o Game Server solicita uma mudança de nível de acesso para uma conta.
	 * @param data Os dados do pacote.
	 */
	private void onReceiveChangeAccessLevel(byte[] data)
	{
		if (isAuthed())
		{
			final ChangeAccessLevel cal = new ChangeAccessLevel(data);
			try
			{
				AccountTable.getInstance().setAccountAccessLevel(cal.getAccount(), cal.getLevel());
				LOGGER.info("Nível de acesso da conta {} alterado para {}.", cal.getAccount(), cal.getLevel());
			}
			catch (SQLException e)
			{
				LOGGER.error("Erro de SQL ao alterar o nível de acesso da conta {} para {}.", e, cal.getAccount(), cal.getLevel());
			}
		}
		else
			forceClose(LoginServerFail.NOT_AUTHED);
	}
	
	/**
	 * Chamado quando o Game Server solicita a autenticação de um jogador.
	 * @param data Os dados do pacote.
	 */
	private void onReceivePlayerAuthRequest(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerAuthRequest par = new PlayerAuthRequest(data);
			final SessionKey key = LoginController.getInstance().getKeyForAccount(par.getAccount());
			
			if (key != null && key.equals(par.getKey()))
			{
				LoginController.getInstance().removeAuthedLoginClient(par.getAccount());
				sendPacket(new PlayerAuthResponse(par.getAccount(), true));
			}
			else
				sendPacket(new PlayerAuthResponse(par.getAccount(), false));
		}
		else
			forceClose(LoginServerFail.NOT_AUTHED);
	}
	
	/**
	 * Chamado ao receber uma atualização de status do Game Server.
	 * @param data Os dados do pacote.
	 */
	private void onReceiveServerStatus(byte[] data)
	{
		if (isAuthed())
			new ServerStatus(data, getServerId()); // A própria classe fará as ações.
		else
			forceClose(LoginServerFail.NOT_AUTHED);
	}
	
	/**
	 * Lida com o processo de registro do Game Server.
	 * @param gameServerAuth O pacote de autenticação do Game Server.
	 */
	private void handleRegProcess(GameServerAuth gameServerAuth)
	{
		final int id = gameServerAuth.getDesiredID();
		final byte[] hexId = gameServerAuth.getHexID();
		
		GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServers().get(id);
		// Existe um gameserver registrado com este id?
		if (gsi != null)
		{
			// O hex id corresponde?
			if (Arrays.equals(gsi.getHexId(), hexId))
			{
				// Verifica se este GS já está conectado.
				synchronized (gsi)
				{
					if (gsi.isAuthed())
						forceClose(LoginServerFail.REASON_ALREADY_LOGGED_IN);
					else
						attachGameServerInfo(gsi, gameServerAuth);
				}
			}
			else
			{
				// Já existe um servidor registrado com o id desejado e hex id diferente.
				// Tenta registrar este com um id alternativo.
				if (Config.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID())
				{
					gsi = new GameServerInfo(id, hexId, this);
					if (GameServerManager.getInstance().registerWithFirstAvailableId(gsi))
					{
						attachGameServerInfo(gsi, gameServerAuth);
						GameServerManager.getInstance().registerServerOnDB(gsi);
					}
					else
						forceClose(LoginServerFail.REASON_NO_FREE_ID);
				}
				// O id do servidor já está em uso e não podemos obter um novo para você.
				else
					forceClose(LoginServerFail.REASON_WRONG_HEXID);
			}
		}
		else
		{
			// Podemos registrar neste id?
			if (Config.ACCEPT_NEW_GAMESERVER)
			{
				gsi = new GameServerInfo(id, hexId, this);
				if (GameServerManager.getInstance().register(id, gsi))
				{
					attachGameServerInfo(gsi, gameServerAuth);
					GameServerManager.getInstance().registerServerOnDB(gsi);
				}
				// Alguém pegou este ID enquanto isso.
				else
					forceClose(LoginServerFail.REASON_ID_RESERVED);
			}
			else
				forceClose(LoginServerFail.REASON_WRONG_HEXID);
		}
	}
	
	/**
	 * Anexa um GameServerInfo a esta Thread.
	 * <li>Atualiza os valores do GameServerInfo com base no pacote GameServerAuth.</li>
	 * <li><b>Define o GameServerInfo como Autenticado.</b></li>
	 * @param gsi O GameServerInfo a ser anexado.
	 * @param gameServerAuth As informações do servidor.
	 */
	private void attachGameServerInfo(GameServerInfo gsi, GameServerAuth gameServerAuth)
	{
		setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(gameServerAuth.getPort());
		
		if (!gameServerAuth.getHostName().equals("*"))
		{
			try
			{
				_gsi.setHostName(InetAddress.getByName(gameServerAuth.getHostName()).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				LOGGER.error("Não foi possível resolver o hostname '{}'.", e, gameServerAuth.getHostName());
				_gsi.setHostName(_connectionIp);
			}
		}
		else
			_gsi.setHostName(_connectionIp);
		
		gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
		gsi.setAuthed(true);
		
		LOGGER.info("Gameserver [{}] {} conectado em: {}.", getServerId(), GameServerManager.getInstance().getServerNames().get(getServerId()), _gsi.getHostName());
	}
	
	/**
	 * Força o fechamento da conexão com o Game Server.
	 * @param reason O motivo do fechamento.
	 */
	private void forceClose(int reason)
	{
		sendPacket(new LoginServerFail(reason));
		
try
		{
			_connection.close();
		}
		catch (IOException e)
		{
			LOGGER.debug("Falha ao desconectar o servidor banido, o servidor já está desconectado.", e);
		}
	}
	
	/**
	 * Envia um pacote para o Game Server.
	 * @param sl O pacote a ser enviado.
	 */
	private void sendPacket(ServerBasePacket sl)
	{
		try
		{
			byte[] data = sl.getContent();
			NewCrypt.appendChecksum(data);
			data = _blowfish.crypt(data);
			
			int len = data.length + 2;
			synchronized (_out)
			{
				_out.write(len & 0xff);
				_out.write(len >> 8 & 0xff);
				_out.write(data);
				_out.flush();
			}
		}
		catch (IOException e)
		{
			LOGGER.error("Exceção ao enviar o pacote {}.", e, sl.getClass().getSimpleName());
		}
	}
	
	/**
	 * @param account O nome da conta.
	 * @return True se a conta estiver neste Game Server, false caso contrário.
	 */
	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}
	
	/**
	 * @return A contagem de jogadores neste Game Server.
	 */
	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}
	
	/**
	 * Expulsa um jogador do Game Server.
	 * @param account O nome da conta a ser expulsa.
	 */
	public void kickPlayer(String account)
	{
		sendPacket(new KickPlayer(account));
	}
	
	/**
	 * @return True se o Game Server estiver autenticado.
	 */
	public boolean isAuthed()
	{
		return _gsi != null && _gsi.isAuthed();
	}
	
	public GameServerInfo getGameServerInfo()
	{
		return _gsi;
	}
	
	public void setGameServerInfo(GameServerInfo gsi)
	{
		_gsi = gsi;
	}
	
	private int getServerId()
	{
		return (_gsi == null) ? -1 : _gsi.getId();
	}
	
	public String getConnectionIp()
	{
		return _connectionIp;
	}
}
