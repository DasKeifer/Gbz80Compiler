package compiler.static_instructs.subs;

import compiler.CompilerConstants.Register;
import compiler.static_instructs.Sub;

public class SubReg extends Sub
{
	public static final int SIZE = 1;
	Register reg;
	
	public SubReg(Register reg)
	{
		super(SIZE);
		this.reg = reg;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt] = (byte) (0x90 | reg.getValue());
	}
}
