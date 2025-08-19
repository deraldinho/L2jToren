package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.loginserver.network.clientpackets.IncomingPacketFromGameServer;

public class PlayerLogout extends IncomingPacketFromGameServer
{
	private final String _account;
	
	public PlayerLogout(byte[] decrypt)
	{
		super(decrypt);
		_account = readS();
	}
	
	public String getAccount()
	{
		return _account;
	}
}