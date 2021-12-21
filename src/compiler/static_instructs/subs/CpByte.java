package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.QueuedWriter;
import compiler.static_instructs.Cp;

public class CpByte extends Cp
{
	public static final int SIZE = 2;
	byte value;

	public CpByte(byte value)
	{
		super(SIZE);
		this.value = value;
	}

	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		writer.append((byte) 0xFE);
		writer.append(value);
	}
}
