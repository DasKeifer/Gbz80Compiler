package compiler.static_instructs;

import java.io.IOException;
import java.util.Arrays;

import gbc_framework.QueuedWriter;
import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.Register;

public class Dec extends StaticInstruction
{
	public static final int SIZE = 1;
	Register reg;

	public Dec(Register reg)
	{
		super(SIZE); // Size
		this.reg = reg;
	}

	public static Dec create(String[] args)
	{		
		final String SUPPORT_STRING = "Dec only supports (Register): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new Dec(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
				// The instruct doesn't fit
				// Could throw here but kept to preserve the pattern being used for
				// the instructs to support more easily adding future ones without
				// forgetting to add the throw at the end
		}

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
	
	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		writer.append((byte) (0x05 | reg.getValue() << 3));
	}
}
