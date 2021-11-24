package compiler.static_instructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.static_instructs.subs.IncPair;
import compiler.static_instructs.subs.IncReg;

public abstract class Inc extends StaticInstruction
{
	public static final int SIZE = 1;
	protected Inc()
	{
		super(SIZE);
	}

	public static Inc create(String[] args)
	{
		final String SUPPORT_STRING = "Inc only supports (Register) or (RegisterPair): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new IncReg(CompilerUtils.parseRegisterArg(args[0]));
		}
		catch(IllegalArgumentException iae)
		{
			// The instruct doesn't fit - try the next one
		}

		try
		{
			return new IncPair(CompilerUtils.parseRegisterPairArg(args[0]));
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
