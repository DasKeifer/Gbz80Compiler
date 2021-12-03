package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.SegmentedWriter;
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
	public void writeStaticBytes(SegmentedWriter writer) throws IOException
	{
		writer.append(
				(byte) 0xD6, 
				val
		);
	}
}
