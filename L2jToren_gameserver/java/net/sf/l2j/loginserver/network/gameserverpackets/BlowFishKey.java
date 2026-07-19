package net.sf.l2j.loginserver.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.Cipher;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.loginserver.network.clientpackets.IncomingPacketFromGameServer;

public class BlowFishKey extends IncomingPacketFromGameServer
{
	private static final CLogger LOGGER = new CLogger(BlowFishKey.class.getName());
	
	byte[] _key;
	
	public BlowFishKey(byte[] decrypt, RSAPrivateKey privateKey)
	{
		super(decrypt);
		
		final int size = readD();
		final int expectedRsaBlockSize = (privateKey.getModulus().bitLength() + 7) / 8;
		if (size != expectedRsaBlockSize)
			throw new IllegalArgumentException("Invalid encrypted Blowfish key size: " + size + ", expected: " + expectedRsaBlockSize + ".");
		
		final byte[] tempKey = readB(size);
		
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
			
			final byte[] tempDecryptKey = rsaCipher.doFinal(tempKey);
			
			// There are nulls before the key; remove only the leading padding bytes.
			int i = 0;
			final int len = tempDecryptKey.length;
			for (; i < len; i++)
			{
				if (tempDecryptKey[i] != 0)
					break;
			}
			
			if (i == len)
				throw new IllegalArgumentException("Decrypted Blowfish key is empty.");
			
			_key = new byte[len - i];
			System.arraycopy(tempDecryptKey, i, _key, 0, len - i);
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.error("Couldn't decrypt blowfish key (RSA)", e);
			throw new IllegalArgumentException("Couldn't decrypt Blowfish key.", e);
		}
	}
	
	public byte[] getKey()
	{
		return _key;
	}
}