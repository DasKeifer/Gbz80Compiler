package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.QueuedWriter;
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
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		// A, val
		if (loadToA)
		{
			writer.append((byte) 0xFA);
		}
		// val, A
		else
		{
			writer.append((byte) 0xEA);
		}
		writer.append(ByteUtils.shortToLittleEndianBytes(addr));
	}
}
