package compiler.static_instructs.subs;

import compiler.static_instructs.Ld;
import gbc_framework.utils.ByteUtils;

public class LdAMemAddr extends Ld
{
	public static final int SIZE = 3;
	short addr;
	boolean loadToA;
	
	public LdAMemAddr(boolean loadToA, short addr)
	{
		super(SIZE); // size
		this.addr = addr;
		this.loadToA = loadToA;
	}
	
	@Override
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt) 
	{
		// A, val
		if (loadToA)
		{
			bytes[indexToWriteAt++] = (byte) 0xFA;
		}
		// val, A
		else
		{
			bytes[indexToWriteAt++] = (byte) 0xEA;
		}
		ByteUtils.writeAsShort(addr, bytes, indexToWriteAt);
	}
}
