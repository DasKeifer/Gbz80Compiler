package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.QueuedWriter;
import compiler.CompilerConstants.Register;
import compiler.static_instructs.Ld;

public class LdRegByte extends Ld 
{
	Register reg;
	byte value;
	
	public LdRegByte(Register reg, byte value)
	{
		super(2); // size
		this.reg = reg;
		this.value = value;
	}

	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		writer.append(
				(byte) (0x06 | (reg.getValue() << 3)),
				value
		);
	}
}
