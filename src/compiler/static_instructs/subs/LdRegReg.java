package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.SegmentedWriter;
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
	public void writeStaticBytes(SegmentedWriter writer) throws IOException
	{
		writer.append((byte) (0x40 | (to.getValue() << 3) | (from.getValue())));
	}
}
