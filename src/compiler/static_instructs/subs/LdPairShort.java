package compiler.static_instructs.subs;

import java.io.IOException;

import gbc_framework.SegmentedWriter;
import compiler.CompilerConstants.RegisterPair;
import compiler.static_instructs.Ld;
import gbc_framework.utils.ByteUtils;

public class LdPairShort extends Ld
{
	RegisterPair pair;
	short value;
	
	public LdPairShort(RegisterPair pair, short value)
	{
		super(3); // size
		this.pair = pair;
		this.value = value;
	}

	@Override
	public void writeStaticBytes(SegmentedWriter writer) throws IOException
	{
		writer.append((byte) (0x01 | (pair.getValue() << 4)));
		writer.append(ByteUtils.shortToLittleEndianBytes(value));
	}
}
