package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.QueuedWriter;
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

	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		writer.append((byte) (0x90 | reg.getValue()));
	}
}
