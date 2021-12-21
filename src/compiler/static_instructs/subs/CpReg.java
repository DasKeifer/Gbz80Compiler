package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.QueuedWriter;
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

	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		writer.append((byte) (0xB8 | reg.getValue()));
	}
}
