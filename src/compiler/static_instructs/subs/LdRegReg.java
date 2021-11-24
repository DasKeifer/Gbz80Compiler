package compiler.static_instructs.subs;

import compiler.CompilerConstants.Register;
import compiler.static_instructs.Ld;

public class LdRegReg extends Ld
{
	Register to;
	Register from;
	
	public LdRegReg(Register loadTo, Register loadFrom)
	{
		super(1); // size
		to = loadTo;
		from = loadFrom;
	}

	@Override
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt) 
	{
		bytes[indexToWriteAt] = (byte) (0x40 | (to.getValue() << 3) | (from.getValue()));
	}
}
