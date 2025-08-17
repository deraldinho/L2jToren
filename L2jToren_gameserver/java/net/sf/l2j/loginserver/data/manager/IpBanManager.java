package net.sf.l2j.loginserver.data.manager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import net.sf.l2j.commons.logging.CLogger;

/**
 * Gerencia a lista de endereços IP banidos.
 */
public class IpBanManager
{
	private static final CLogger LOGGER = new CLogger(IpBanManager.class.getName());
	
	// Mapa de IPs banidos e o tempo de expiração do ban. 0 = permanente.
	private final Map<InetAddress, Long> _bannedIps = new ConcurrentHashMap<>();
	
	protected IpBanManager()
	{
		// Carrega o arquivo de IPs banidos.
		final Path file = Paths.get("config", "banned_ips.properties");
		if (file == null)
		{
			LOGGER.warn("banned_ips.properties não encontrado. O carregamento da lista de banidos foi ignorado.");
			return;
		}
		
		// Carrega cada linha, ignorando as que contêm #.
		try (Stream<String> stream = Files.lines(file))
		{
			stream.filter(l -> !l.contains("#")).forEach(l ->
			{
				try
				{
					_bannedIps.putIfAbsent(InetAddress.getByName(l), 0L);
				}
				catch (UnknownHostException e)
				{
					LOGGER.error("Endereço de banimento inválido ({}).", l, e);
				}
			});
		}
		catch (IOException e)
		{
			LOGGER.error("Erro ao ler o arquivo banned_ips.properties.", e);
		}
		LOGGER.info("Carregados {} IP(s) banidos.", _bannedIps.size());
	}
	
	public Map<InetAddress, Long> getBannedIps()
	{
		return _bannedIps;
	}
	
	/**
	 * Adiciona o {@link InetAddress} passado como parâmetro à lista de banidos, com a duração fornecida.
	 * @param address O {@link InetAddress} a ser banido.
	 * @param duration A duração em milissegundos. 0 significa que o banimento é permanente.
	 */
	public void addBanForAddress(InetAddress address, long duration)
	{
		// Adiciona o tempo atual, mas apenas se o parâmetro for > 0.
		if (duration > 0)
			duration += System.currentTimeMillis();
		
		_bannedIps.putIfAbsent(address, duration);
	}
	
	/**
	 * @param address O {@link InetAddress} a ser testado.
	 * @return True se o {@link InetAddress} estiver banido, caso contrário, false.<br>
	 *         <br>
	 *         Se o tempo de banimento existir, compara com o tempo atual. Se for antigo, remove o banimento. Timers com valor 0 nunca expiram.
	 */
	public boolean isBannedAddress(InetAddress address)
	{
		if (address == null)
			return true;
		
		final Long time = _bannedIps.get(address);
		if (time != null)
		{
			// Se o tempo for maior que 0 e menor que o tempo atual, o banimento expirou.
			if (time > 0 && time < System.currentTimeMillis())
			{
				// Remove o banimento da memória.
				_bannedIps.remove(address);
				
				LOGGER.info("Removido banimento de endereço de IP expirado {}.", address.getHostAddress());
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static IpBanManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IpBanManager INSTANCE = new IpBanManager();
	}
}
