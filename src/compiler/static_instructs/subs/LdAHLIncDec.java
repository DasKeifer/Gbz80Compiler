package compiler.static_instructs.subs;

import compiler.static_instructs.Ld;

public class LdAHLIncDec extends Ld
{
	boolean loadToA;
	boolean increment;
	
	public LdAHLIncDec(boolean loadToA, boolean increment)
	{
		super(1); // size
		this.loadToA = loadToA;
		this.increment = increment;
	}
	
	@Override
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt) 
	{
		byte val = 0x2;
		if (loadToA)
		{
			val = 0xA;
		}
		if (increment)
		{
			val |= 0x20; // 2 << 4
		}
		else
		{
			val |= 0x30; // 3 << 4
		}
		
		bytes[indexToWriteAt] = val;
	}
}
