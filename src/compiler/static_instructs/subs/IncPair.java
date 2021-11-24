package compiler.static_instructs.subs;

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
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		bytes[indexToWriteAt] = (byte) (0x03 | pair.getValue() << 4);
	}
}
