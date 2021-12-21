package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.QueuedWriter;
import compiler.static_instructs.Sub;

public class SubByte extends Sub
{
	public static final int SIZE = 2;
	byte val;
	
	public SubByte(byte val)
	{
		super(SIZE);
		this.val = val;
	}

	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		writer.append(
				(byte) 0xD6, 
				val
		);
	}
}
