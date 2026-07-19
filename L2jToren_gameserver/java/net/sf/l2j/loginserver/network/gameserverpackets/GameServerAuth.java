package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.loginserver.network.clientpackets.IncomingPacketFromGameServer;

public class GameServerAuth extends IncomingPacketFromGameServer
{
	private static final int HEX_ID_LENGTH = 16;
	
	private final byte[] _hexId;
	private final int _desiredId;
	private final boolean _hostReserved;
	private final boolean _acceptAlternativeId;
	private final int _maxPlayers;
	private final int _port;
	private final String _hostName;
	
	public GameServerAuth(byte[] decrypt)
	{
		super(decrypt);
		
		_desiredId = readC();
		_acceptAlternativeId = readC() != 0;
		_hostReserved = readC() != 0;
		_hostName = readS();
		_port = readH();
		_maxPlayers = readD();
		
		final int size = readD();
		if (size != HEX_ID_LENGTH)
			throw new IllegalArgumentException("Invalid GameServer hexId size: " + size + ", expected: " + HEX_ID_LENGTH + ".");
		
		_hexId = readB(size);
	}
	
	public byte[] getHexID()
	{
		return _hexId;
	}
	
	public boolean getHostReserved()
	{
		return _hostReserved;
	}
	
	public int getDesiredID()
	{
		return _desiredId;
	}
	
	public boolean acceptAlternateID()
	{
		return _acceptAlternativeId;
	}
	
	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	public String getHostName()
	{
		return _hostName;
	}
	
	public int getPort()
	{
		return _port;
	}
}