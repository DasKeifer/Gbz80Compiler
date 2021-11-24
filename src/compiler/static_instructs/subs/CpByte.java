package compiler.static_instructs.subs;

import compiler.static_instructs.Cp;

public class CpByte extends Cp
{
	public static final int SIZE = 2;
	byte value;

	public CpByte(byte value)
	{
		super(SIZE);
		this.value = value;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt++] = (byte) 0xFE;
		bytes[indexToWriteAt] = value;
	}
}
