package net.sf.l2j.loginserver.network.clientpackets;

import java.nio.charset.StandardCharsets;

public abstract class IncomingPacketFromGameServer
{
	private final byte[] _decrypt;
	private int _off;
	
	protected IncomingPacketFromGameServer(byte[] decrypt)
	{
		if (decrypt == null || decrypt.length == 0)
			throw new IllegalArgumentException("Packet payload cannot be null or empty.");
		
		_decrypt = decrypt;
		_off = 1; // skip packet type id
	}
	
	public int readD()
	{
		ensureRemaining(4, "D (int)");
		return (_decrypt[_off++] & 0xff) | ((_decrypt[_off++] & 0xff) << 8) | ((_decrypt[_off++] & 0xff) << 16) | ((_decrypt[_off++] & 0xff) << 24);
	}
	
	public int readC()
	{
		ensureRemaining(1, "C (byte)");
		return _decrypt[_off++] & 0xff;
	}
	
	public int readH()
	{
		ensureRemaining(2, "H (short)");
		return (_decrypt[_off++] & 0xff) | ((_decrypt[_off++] & 0xff) << 8);
	}
	
	public double readF()
	{
		ensureRemaining(8, "F (double)");
		final long result = (_decrypt[_off++] & 0xffL) | ((_decrypt[_off++] & 0xffL) << 8) | ((_decrypt[_off++] & 0xffL) << 16) | ((_decrypt[_off++] & 0xffL) << 24) | ((_decrypt[_off++] & 0xffL) << 32) | ((_decrypt[_off++] & 0xffL) << 40) | ((_decrypt[_off++] & 0xffL) << 48) | ((_decrypt[_off++] & 0xffL) << 56);
		return Double.longBitsToDouble(result);
	}
	
	public String readS()
	{
		final int start = _off;
		
		// Find the null terminator in UTF-16LE encoding.
		int end = start;
		while (end < _decrypt.length - 1 && (_decrypt[end] != 0 || _decrypt[end + 1] != 0))
		{
			end += 2;
			if (end >= _decrypt.length)
				throw new IllegalArgumentException("String not null-terminated or buffer ended prematurely.");
		}
		
		if (end + 2 > _decrypt.length)
			throw new IllegalArgumentException("String not properly null-terminated (missing null bytes).");
		
		_off = end + 2;
		return new String(_decrypt, start, end - start, StandardCharsets.UTF_16LE);
	}
	
	public final byte[] readB(int length)
	{
		ensureRemaining(length, "B (byte array)");
		
		final byte[] result = new byte[length];
		System.arraycopy(_decrypt, _off, result, 0, length);
		_off += length;
		return result;
	}
	
	private void ensureRemaining(int length, String type)
	{
		// Subtraction-based validation avoids integer overflow from expressions such as _off + length.
		if (length < 0 || _off < 0 || _off > _decrypt.length || length > _decrypt.length - _off)
			throw new IllegalArgumentException("Not enough data to read " + type + " of length " + length + ".");
	}
}