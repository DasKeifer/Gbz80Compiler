package compiler.static_instructs.subs;

import compiler.CompilerConstants.Register;
import compiler.static_instructs.Cp;

public class CpReg extends Cp
{
	public static final int SIZE = 1;
	Register reg;

	public CpReg(Register reg)
	{
		super(SIZE);
		this.reg = reg;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt] = (byte) (0xB8 | reg.getValue());
	}
}
