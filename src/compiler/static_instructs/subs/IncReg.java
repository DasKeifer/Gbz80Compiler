package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.QueuedWriter;
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

	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		writer.append((byte) (0x04 | reg.getValue() << 3));
	}
}
