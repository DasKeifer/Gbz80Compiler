package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.QueuedWriter;
import compiler.CompilerConstants.RegisterPair;
import compiler.static_instructs.Inc;

public class IncPair extends Inc
{
	RegisterPair pair;

	public IncPair(RegisterPair pair)
	{
		super();
		this.pair = pair;
	}

	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		writer.append((byte) (0x03 | pair.getValue() << 4));
	}
}
