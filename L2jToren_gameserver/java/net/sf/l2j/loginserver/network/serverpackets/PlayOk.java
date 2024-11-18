package net.sf.l2j.loginserver.network.serverpackets;

import net.sf.l2j.loginserver.network.SessionKey;

public final class PlayOk extends L2LoginServerPacket
{
	private final int _playOk1;
	private final int _playOk2;
	
	public PlayOk(SessionKey sessionKey)
	{
		_playOk1 = sessionKey.playOkId1();
		_playOk2 = sessionKey.playOkId2();
	}
	
	@Override
	protected void write()
	{
		writeC(0x07);
		writeD(_playOk1);
		writeD(_playOk2);
	}
}