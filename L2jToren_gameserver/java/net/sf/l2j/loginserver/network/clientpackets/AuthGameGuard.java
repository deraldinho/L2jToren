package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.loginserver.enums.LoginClientState;
import net.sf.l2j.loginserver.network.serverpackets.GGAuth;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;

/**
 * Este pacote é enviado pelo cliente para autenticar com o GameGuard.
 */
public class AuthGameGuard extends L2LoginClientPacket
{
	private int _sessionId;
	private int _data1;
	private int _data2;
	private int _data3;
	private int _data4;
	
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public int getData1()
	{
		return _data1;
	}
	
	public int getData2()
	{
		return _data2;
	}
	
	public int getData3()
	{
		return _data3;
	}
	
	public int getData4()
	{
		return _data4;
	}
	
	@Override
	protected boolean readImpl()
	{
		// Lê os 20 bytes de dados do GameGuard.
		if (super._buf.remaining() >= 20)
		{
			_sessionId = readD();
			_data1 = readD();
			_data2 = readD();
			_data3 = readD();
			_data4 = readD();
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		// Compara o ID da sessão recebido com o ID da sessão do cliente no servidor.
		if (_sessionId == getClient().getSessionId())
		{
			// Se corresponderem, atualiza o estado do cliente para AUTHED_GG (autenticado no GameGuard).
			getClient().setState(LoginClientState.AUTHED_GG);
			// Envia uma resposta de sucesso na autenticação do GameGuard.
			getClient().sendPacket(new GGAuth(getClient().getSessionId()));
		}
		else
		{
			// Se não corresponderem, fecha a conexão.
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
		}
	}
}
