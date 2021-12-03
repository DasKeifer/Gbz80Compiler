package compiler.static_instructs;

import java.io.IOException;
import java.util.Arrays;

import gbc_framework.SegmentedWriter;
import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.Register;

public class Or extends StaticInstruction
{
	Register reg;
	
	public Or(Register reg)
	{
		super(1); // Size
		this.reg = reg;
	}

	public static Or create(String[] args)
	{		
		final String SUPPORT_STRING = "Or only supports (Register): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new Or(CompilerUtils.parseRegisterArg(args[0]));
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
	public void writeStaticBytes(SegmentedWriter writer) throws IOException
	{
		writer.append((byte) (0xB0 | reg.getValue()));
	}
}
