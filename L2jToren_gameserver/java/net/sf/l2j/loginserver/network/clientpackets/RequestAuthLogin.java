package net.sf.l2j.loginserver.network.clientpackets;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;

/**
 * Este pacote é enviado pelo cliente para autenticar o login e a senha.
 */
public class RequestAuthLogin extends L2LoginClientPacket
{
	// O bloco de 128 bytes brutos e criptografados.
	private final byte[] _raw = new byte[128];
	
	@Override
	public boolean readImpl()
	{
		// Lê os 128 bytes do buffer se estiverem disponíveis.
		if (super._buf.remaining() >= 128)
		{
			readB(_raw);
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		final LoginClient client = getClient();
		
		byte[] decrypted = null;
		try
		{
			// Cria um cifrador RSA para descriptografar os dados.
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			// Inicializa o cifrador em modo de descriptografia com a chave privada RSA do cliente.
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.error("Falha ao gerar o cifrador.", e);
			client.close(LoginFail.REASON_ACCESS_FAILED);
			return;
		}
		
		try
		{
			// Extrai o nome de usuário e a senha do array de bytes descriptografado.
			// Os offsets (0x5E, 0x6C) são específicos do protocolo do Lineage 2.
			final String user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			final String password = new String(decrypted, 0x6C, 16).trim();
			
			// Envia as informações da conta para o LoginController para verificação.
			LoginController.getInstance().retrieveAccountInfo(client, user, password);
		}
		catch (Exception e)
		{
			LOGGER.error("Falha ao descriptografar usuário/senha.", e);
			client.close(LoginFail.REASON_ACCESS_FAILED);
		}
	}
}
