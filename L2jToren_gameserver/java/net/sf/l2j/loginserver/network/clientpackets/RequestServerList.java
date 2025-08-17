package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.loginserver.model.Account;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;
import net.sf.l2j.loginserver.network.serverpackets.ServerList;

/**
 * Este pacote é enviado pelo cliente para solicitar a lista de servidores de jogo.
 */
public class RequestServerList extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	
	@Override
	public boolean readImpl()
	{
		// Lê os 8 bytes da chave de sessão.
		if (super._buf.remaining() >= 8)
		{
			_skey1 = readD(); // Parte 1 da chave de sessão (enviada em LoginOk)
			_skey2 = readD(); // Parte 2 da chave de sessão (enviada em LoginOk)
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		final Account account = getClient().getAccount();
		if (account == null)
		{
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		// Valida a chave de sessão para garantir que o pedido é legítimo.
		if (!getClient().getSessionKey().checkLoginPair(_skey1, _skey2))
		{
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		// Se a chave de sessão for válida, envia a lista de servidores para o cliente.
		getClient().sendPacket(new ServerList(account));
	}
}
