package compiler.reference_instructs;

import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.CompilerConstants.InstructionConditions;
import gbc_framework.SegmentNamingUtils;

public class Jp extends JumpCommon
{
	public Jp(String labelToGoTo) 
	{
		super(labelToGoTo, InstructionConditions.NONE, false); // isJr = false
	}
	
	public Jp(String labelToGoTo, InstructionConditions conditions) 
	{
		super(labelToGoTo, conditions, false); // isJr = false
	}
	
	public Jp(short bankLoadedAddressToGoTo) 
	{
		super(bankLoadedAddressToGoTo, InstructionConditions.NONE, false); // isJr = false
	}
	
	public Jp(short bankLoadedAddressToGoTo, InstructionConditions conditions) 
	{
		super(bankLoadedAddressToGoTo, conditions, false); // isJr = false
	}
	
	public static Jp create(String[] args, String rootSegment)
	{	
		final String supportedArgs = "jp only supports (int bankLoadedAddressToGoTo), (String labelToGoTo), (InstructionCondition, int bankLoadedAddressToGoTo) and (InstructionCondition, String labelToGoTo): ";	
		
		String labelOrAddrToGoTo = args[0];
		InstructionConditions conditions = InstructionConditions.NONE;
		if (args.length == 2)
		{
			labelOrAddrToGoTo = args[1];
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
			throw new IllegalArgumentException(supportedArgs + "given " + Arrays.toString(args));
		}
		
		// See if its a hex address
		try 
		{
			return new Jp(CompilerUtils.parseBankLoadedAddrArg(labelOrAddrToGoTo), conditions);
		}
		// Otherwise it should be a label
		catch (IllegalArgumentException iae)
		{
			if (SegmentNamingUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo))
			{
				labelOrAddrToGoTo = CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment);
			}
			return new Jp(labelOrAddrToGoTo, conditions);
		}
	}
}
