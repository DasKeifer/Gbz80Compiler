package compiler.static_instructs;

import java.io.IOException;
import java.util.Arrays;

import gbc_framework.QueuedWriter;
import compiler.CompilerUtils;
import compiler.StaticInstruction;
import compiler.CompilerConstants.InstructionConditions;
import compiler.reference_instructs.JumpCallCommon;

public class Nop extends StaticInstruction
{
	public static final byte NOP_VALUE = 0x00;
	
	private boolean allowJump;
	
	public Nop()
	{
		this(1);
		allowJump = false;
	}
	
	public Nop(int nopSize)
	{
		this(nopSize, false);
	}
	
	public Nop(int nopSize, boolean allowJump)
	{
		super(nopSize);
		this.allowJump = allowJump;
	}

	public static Nop create(String[] args)
	{		
		final String SUPPORT_STRING = "Nop only supports (), (byte effectiveNumNops), (byte effectiveNumNops, bool allowJrOptimization): Given ";
		if (args.length > 2)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		if (args.length == 0)
		{
			return new Nop();
		}
		else if (args.length == 1)
		{
			try
			{
				return new Nop(CompilerUtils.parseByteArg(args[0]));
			}
			catch (IllegalArgumentException iae)
			{
				// The instruct doesn't fit
				// Could throw here but kept to preserve the pattern being used for
				// the instructs to support more easily adding future ones without
				// forgetting to add the throw at the end
			}
		}
		else
		{
			try
			{
				return new Nop(CompilerUtils.parseByteArg(args[0]), CompilerUtils.parseBoolArg(args[1]));
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

	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		int size = getSize();
		int offset = 0;
		
		// takes 3 cycles to jump so 4 or greater its more efficient to jump
		if (allowJump && size > 3)
		{
			// -2 because its relative to the end of the JR command
			JumpCallCommon.writeJr(writer, InstructionConditions.NONE, (byte) (size - 2));
			offset = 2; // Start writing nops after the jump
		}
		
		// Write the Nops for the rest of the size for safety
		for (/*already set*/; offset < size; offset++)
		{
			writer.append(NOP_VALUE);
		}
	}
}
