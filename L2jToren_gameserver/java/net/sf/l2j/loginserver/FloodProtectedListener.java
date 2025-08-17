package net.sf.l2j.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.Config;

/**
 * Uma classe abstrata para um listener de socket com proteção contra flood.
 */
public abstract class FloodProtectedListener extends Thread
{
	private static final CLogger LOGGER = new CLogger(FloodProtectedListener.class.getName());
	
	// Mapa para rastrear conexões de entrada e detectar floods.
	private final Map<String, ForeignConnection> _flooders = new ConcurrentHashMap<>();
	
	private final ServerSocket _serverSocket;
	
	protected FloodProtectedListener(String listenIp, int port) throws IOException
	{
		if (listenIp.equals("*"))
			_serverSocket = new ServerSocket(port);
		else
			_serverSocket = new ServerSocket(port, 50, InetAddress.getByName(listenIp));
	}
	
	/**
	 * Método abstrato para adicionar um novo cliente. Deve ser implementado pela subclasse.
	 * @param s O Socket do cliente.
	 */
	public abstract void addClient(Socket s);
	
	@SuppressWarnings("resource")
	@Override
	public void run()
	{
		Socket connection = null;
		while (true)
		{
			try
			{
				connection = _serverSocket.accept();
				
				// Se a proteção contra flood estiver ativada, verifica a conexão.
				if (Config.FLOOD_PROTECTION)
				{
					final String address = connection.getInetAddress().getHostAddress();
					final long currentTime = System.currentTimeMillis();
					
					final ForeignConnection fc = _flooders.get(address);
					if (fc != null)
					{
						fc.attempts++;
						// Verifica se a conexão é muito rápida ou excede os limites.
						if ((fc.attempts > Config.FAST_CONNECTION_LIMIT && (currentTime - fc.lastConnection) < Config.NORMAL_CONNECTION_TIME) || (currentTime - fc.lastConnection) < Config.FAST_CONNECTION_TIME || fc.attempts > Config.MAX_CONNECTION_PER_IP)
						{
							fc.lastConnection = currentTime;
							fc.attempts -= 1;
							
							connection.close();
							
							if (!fc.isFlooding)
								LOGGER.info("Flood detectado de {}.", address);
							
							fc.isFlooding = true;
							continue;
						}
						
						// Se a conexão estava inundando o servidor, mas agora passou na verificação.
						if (fc.isFlooding)
						{
							fc.isFlooding = false;
							LOGGER.info("{} não é mais considerado como flooding.", address);
						}
						
						fc.lastConnection = currentTime;
					}
					else
						_flooders.put(address, new ForeignConnection(currentTime));
				}
				// Se a conexão for válida, adiciona o cliente.
				addClient(connection);
			}
			catch (IOException e)
			{
				try
				{
					if (connection != null)
						connection.close();
				}
				catch (Exception e2)
				{
					LOGGER.error("Erro ao fechar a conexão após exceção.", e2);
				}
				
				if (isInterrupted())
				{
					try
					{
						_serverSocket.close();
					}
					catch (IOException io)
					{
						LOGGER.error("Erro ao fechar o server socket.", io);
					}
					break;
				}
			}
		}
	}
	
	/**
	 * Remove um IP do rastreamento de flood, geralmente quando uma conexão é encerrada normalmente.
	 * @param ip O endereço IP a ser removido.
	 */
	public void removeFloodProtection(String ip)
	{
		if (!Config.FLOOD_PROTECTION)
			return;
		
		final ForeignConnection fc = _flooders.get(ip);
		if (fc != null)
		{
			fc.attempts -= 1;
			
			if (fc.attempts == 0)
				_flooders.remove(ip);
		}
	}
	
	/**
	 * Classe interna para armazenar informações sobre uma conexão externa.
	 */
	protected static class ForeignConnection
	{
		public int attempts;
		public long lastConnection;
		public boolean isFlooding = false;
		
		public ForeignConnection(long time)
		{
			lastConnection = time;
			attempts = 1;
		}
	}
}
