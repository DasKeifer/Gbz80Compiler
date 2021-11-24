package compiler.static_instructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.InstructionConditions;

public class Ret extends StaticInstruction
{
	public static final int SIZE = 1;
	InstructionConditions conditions;
	
	public Ret(InstructionConditions retConditions)
	{
		super(1); // Size
		conditions = retConditions;
	}
	
	public Ret()
	{
		this(InstructionConditions.NONE);
	}
	
	public static Ret create(String[] args)
	{		
		final String SUPPORT_STRING = "Ret only supports ([No Args]) or (InstructionCondition): Given ";
		if (args.length > 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		if (args.length == 0)
		{
			return new Ret();
		}
		else if (args.length == 1)
		{
			try
			{
				return new Ret(CompilerUtils.parseInstructionConditionsArg(args[0]));
			}
			catch (IllegalArgumentException iae)
			{
				// The instruct doesn't fit
				// Could throw here but kept to preserve the pattern being used for
				// the instructs to support more easily adding future ones without
				// forgetting to add the throw at the end
			}
		}

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		if (InstructionConditions.NONE == conditions)
		{
			bytes[indexToWriteAt] = (byte) 0xC9;
		}
		else
		{
			bytes[indexToWriteAt] = (byte) (0xC0 | (conditions.getValue() << 3));
		}
	}
}
