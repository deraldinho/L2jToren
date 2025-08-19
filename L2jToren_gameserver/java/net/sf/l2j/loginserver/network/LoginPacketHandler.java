package net.sf.l2j.loginserver.network;

import java.nio.ByteBuffer;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.IPacketHandler;
import net.sf.l2j.commons.mmocore.ReceivablePacket;

import net.sf.l2j.loginserver.enums.LoginClientState;
import net.sf.l2j.loginserver.network.clientpackets.AuthGameGuard;
import net.sf.l2j.loginserver.network.clientpackets.RequestAuthLogin;
import net.sf.l2j.loginserver.network.clientpackets.RequestServerList;
import net.sf.l2j.loginserver.network.clientpackets.RequestServerLogin;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail;

/**
 * O manipulador de pacotes para o Login Server. É responsável por determinar qual pacote
 * deve ser executado com base no opcode e no estado do cliente.
 */
public final class LoginPacketHandler implements IPacketHandler<LoginClient>
{
	private static final CLogger LOGGER = new CLogger(LoginPacketHandler.class.getName());
	
	// Opcodes dos pacotes do cliente
	private static final int C_REQUEST_AUTH_LOGIN = 0x00;
	private static final int C_REQUEST_SERVER_LOGIN = 0x02;
	private static final int C_REQUEST_SERVER_LIST = 0x05;
	private static final int C_AUTH_GAME_GUARD = 0x07;
	
	@Override
	public ReceivablePacket<LoginClient> handlePacket(ByteBuffer buf, LoginClient client)
	{
		// Lê o primeiro byte para obter o opcode do pacote.
		int opcode = buf.get() & 0xFF;
		
		ReceivablePacket<LoginClient> packet = null;
		LoginClientState state = client.getState();
		
		// O processamento do pacote depende do estado atual do cliente (máquina de estados).
		switch (state)
		{
			case CONNECTED:
				// No estado inicial, apenas a autenticação do GameGuard é esperada.
				if (opcode == C_AUTH_GAME_GUARD)
					packet = new AuthGameGuard();
				else
				{
					debugOpcode(opcode, state);
					client.close(LoginFail.REASON_ACCESS_FAILED);
				}
				break;
			
			case AUTHED_GG:
				// Após a autenticação do GameGuard, o pedido de login é esperado.
				if (opcode == C_REQUEST_AUTH_LOGIN)
					packet = new RequestAuthLogin();
				else
				{
					debugOpcode(opcode, state);
					client.close(LoginFail.REASON_ACCESS_FAILED);
				}
				break;
			
			case AUTHED_LOGIN:
				// Após o login bem-sucedido, o cliente pode solicitar a lista de servidores ou o login em um servidor.
				if (opcode == C_REQUEST_SERVER_LIST)
					packet = new RequestServerList();
				else if (opcode == C_REQUEST_SERVER_LOGIN)
					packet = new RequestServerLogin();
				else
				{
					debugOpcode(opcode, state);
					client.close(LoginFail.REASON_ACCESS_FAILED);
				}
				break;
		}
		return packet;
	}
	
	/**
	 * Registra um aviso se um opcode desconhecido for recebido para um determinado estado.
	 * @param opcode O opcode desconhecido.
	 * @param state O estado atual do cliente.
	 */
	private static void debugOpcode(int opcode, LoginClientState state)
	{
		LOGGER.warn("Opcode desconhecido: {} para o estado: {}.", opcode, state.name());
	}
}
