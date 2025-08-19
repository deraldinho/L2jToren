package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.loginserver.network.clientpackets.IncomingPacketFromGameServer;

public class ChangeAccessLevel extends IncomingPacketFromGameServer
{
	private final int _level;
	private final String _account;
	
	public ChangeAccessLevel(byte[] decrypt)
	{
		super(decrypt);
		_level = readD();
		_account = readS();
	}
	
	public String getAccount()
	{
		return _account;
	}
	
	public int getLevel()
	{
		return _level;
	}
}