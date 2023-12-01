package compiler.reference_instructs;


import java.io.IOException;
import java.util.Arrays;

import gbc_framework.QueuedWriter;
import gbc_framework.SegmentNamingUtils;
import compiler.CompilerUtils;
import compiler.static_instructs.Nop;
import compiler.CompilerConstants.InstructionConditions;

public class Jr extends JumpCommon
{	
	protected static final int MAX_FORWARD_JUMP = 127;
	protected static final int MAX_BACKWARD_JUMP = -128;
	protected static final int INSTRUCT_SIZE = 2;
	
	public Jr(String labelToGoTo) 
	{
		super(labelToGoTo, InstructionConditions.NONE, true); // isJr = true
	}
	
	public Jr(String labelToGoTo, InstructionConditions conditions) 
	{
		super(labelToGoTo, conditions, true); // isJr = true
	}
	
	public static Jr create(String[] args, String rootSegment)
	{		
		final String supportedArgs = "jr only supports (String labelToJumpTo) and (InstructionCondition, String labelToJumpTo): ";	
		final String offsetError = "jr cannot accept a offset for internal calls, use a reference and for writing over data see nop - " + supportedArgs;	
		
		String labelToJumpTo = args[0];
		InstructionConditions conditions = InstructionConditions.NONE;
		if (args.length == 2)
		{
			if (CompilerUtils.isOnlyHex(args[1]))
			{
				throw new IllegalArgumentException(offsetError + Arrays.toString(args));
			}
			labelToJumpTo = args[1];
			
			try
			{
				conditions = CompilerUtils.parseInstructionConditionsArg(args[0]);
			}
			catch (IllegalArgumentException iae)
			{
				throw new IllegalArgumentException(supportedArgs + iae.getMessage());
			}
		}
		else if (args.length != 1)
		{
			throw new IllegalArgumentException(supportedArgs + Arrays.toString(args));
		}
		else if (CompilerUtils.isOnlyHex(args[0]))
		{
			throw new IllegalArgumentException(offsetError + Arrays.toString(args));
		}

		if (SegmentNamingUtils.isOnlySubsegmentPartOfLabel(labelToJumpTo))
		{
			labelToJumpTo = CompilerUtils.formSegmentLabelArg(labelToJumpTo, rootSegment);
		}
		return new Jr(labelToJumpTo, conditions);
	}
	
	public static void blankWithJrs(QueuedWriter writer, int bytesToSkip) throws IOException
	{
		int leftToSkip = bytesToSkip;
		while (leftToSkip > 0)
		{
			// If its 3 or less, its more efficient or just as efficient to do nops
			if (leftToSkip <= 3)
			{
				Nop.writeNops(writer, leftToSkip);
				break;
			}
			
			// -2 because its relative to end of jr instruction
			int jumpSizeMinus2 = leftToSkip - 2;

			// If its larger than our max jump (plus instruct size since its relative to end of the instruct)
			// we need to cut it off there for this iteration
			if (jumpSizeMinus2 > Jr.INSTRUCT_SIZE)
			{
				jumpSizeMinus2 = Jr.INSTRUCT_SIZE;
			}
			
			// write the jump
			Jr.write(writer, InstructionConditions.NONE, (byte)jumpSizeMinus2); 
			Nop.writeNops(writer, jumpSizeMinus2);
			leftToSkip -= (jumpSizeMinus2 + 2);
		}
	}
	
	public static int write(QueuedWriter writer, InstructionConditions conditions, byte relAddress) throws IOException 
	{
		if (InstructionConditions.NONE == conditions)
		{
			writer.append((byte) 0x18);
		}
		else
		{
			writer.append((byte) (0x20 | (conditions.getValue() << 3)));
		}
		writer.append(relAddress);
		return Jr.INSTRUCT_SIZE;
	}
}
