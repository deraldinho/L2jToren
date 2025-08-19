package net.sf.l2j.loginserver;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;

/**
 * Esta classe ouve e aceita conexões de entrada de Servidores de Jogo (Game Servers).
 */
public class GameServerListener extends FloodProtectedListener
{
	private static final CLogger LOGGER = new CLogger(GameServerListener.class.getName());
	
	// Lista de threads de Game Servers conectados.
	private static List<GameServerThread> _gameServers = new CopyOnWriteArrayList<>();
	
	public GameServerListener() throws IOException
	{
		super(Config.GAMESERVER_LOGIN_HOSTNAME, Config.GAMESERVER_LOGIN_PORT);
	}
	
	/**
	 * Chamado quando um novo cliente (Game Server) se conecta.
	 * @param s O Socket da conexão do Game Server.
	 */
	@Override
	public void addClient(Socket s)
	{
		try
		{
			// Cria uma nova thread para lidar com a comunicação com o Game Server e a adiciona à lista.
			_gameServers.add(new GameServerThread(s));
		}
		catch (IOException e)
		{
			LOGGER.error("Falha ao inicializar a GameServerThread, fechando conexão.", e);
			try
			{
				s.close();
			}
			catch (IOException e2)
			{
				// Sem necessidade de logar, apenas ignorar.
			}
		}
	}
	
	/**
	 * Remove um GameServerThread da lista de servidores de jogo ativos.
	 * @param gst A thread do Game Server a ser removida.
	 */
	public void removeGameServer(GameServerThread gst)
	{
		_gameServers.remove(gst);
	}
}
