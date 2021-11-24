package compiler.static_instructs.subs;

import compiler.CompilerConstants.Register;
import compiler.static_instructs.Inc;

public class IncReg extends Inc
{
	Register reg;

	public IncReg(Register reg)
	{
		super();
		this.reg = reg;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt] = (byte) (0x04 | reg.getValue() << 3);
	}
}
