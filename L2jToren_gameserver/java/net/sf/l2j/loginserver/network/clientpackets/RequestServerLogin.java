package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.model.Account;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;
import net.sf.l2j.loginserver.network.serverpackets.PlayFail;
import net.sf.l2j.loginserver.network.serverpackets.PlayOk;

/**
 * Este pacote é enviado pelo cliente quando o usuário seleciona um servidor de jogo para entrar.
 */
public class RequestServerLogin extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;
	
	@Override
	public boolean readImpl()
	{
		// Lê a chave de sessão e o ID do servidor.
		if (super._buf.remaining() >= 9)
		{
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		final SessionKey sk = getClient().getSessionKey();
		
		// Se a tela de licença for exibida, a chave de sessão é verificada novamente.
		if (Config.SHOW_LICENCE && !sk.checkLoginPair(_skey1, _skey2))
		{
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		// Verifica a integridade da conta.
		final Account account = getClient().getAccount();
		if (account == null)
		{
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		// Verifica se é possível logar no servidor de jogo selecionado (ex: não está cheio, está online, etc.).
		if (!account.isLoginPossible(_serverId))
		{
			getClient().close(PlayFail.REASON_TOO_MANY_PLAYERS);
			return;
		}
		
		// Marca que o cliente entrou em um Game Server e envia a resposta final "PlayOk".
		// O cliente usará a chave de sessão para se autenticar no Game Server.
		getClient().setJoinedGS(true);
		getClient().sendPacket(new PlayOk(sk));
	}
}