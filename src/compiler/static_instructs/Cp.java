package compiler.static_instructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.static_instructs.subs.CpByte;
import compiler.static_instructs.subs.CpReg;

public abstract class Cp extends StaticInstruction
{
	protected Cp(int size)
	{
		super(size);
	}

	public static Cp create(String[] args)
	{
		final String SUPPORT_STRING = "Cp only supports (Byte) or (Register): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new CpByte(CompilerUtils.parseByteArg(args[0]));
		}
		catch (IllegalArgumentException iae) 
		{
			// The instruct doesn't fit - try the next one
		} 

		try
		{
			return new CpReg(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch(IllegalArgumentException iae) 
		{
			// The instruct doesn't fit
			// Could throw here but kept to preserve the pattern being used for
			// the instructs to support more easily adding future ones without
			// forgetting to add the throw at the end
		}

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
}
