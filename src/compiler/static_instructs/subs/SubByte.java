package compiler.static_instructs.subs;

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
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt++] = (byte) 0xD6;
		bytes[indexToWriteAt] = val;
	}
}
