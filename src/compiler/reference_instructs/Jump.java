package compiler.reference_instructs;


import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.CompilerConstants.InstructionConditions;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

public class Jump extends JumpCallCommon
{
	private static final byte CONDITIONLESS_INSTRUCT = (byte) 0xC3;
	private static final byte CONDITIONED_INSTRUCT = (byte) 0xC2;
	private static final byte FAR_INSTRUCT_RST_VAL = 0x30;
	
	public Jump(String labelToGoTo) 
	{
		super(labelToGoTo, InstructionConditions.NONE, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
	}
	
	public Jump(String labelToGoTo, InstructionConditions conditions) 
	{
		super(labelToGoTo, conditions, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
	}
	
	public Jump(int addressToGoTo) 
	{
		super(addressToGoTo, InstructionConditions.NONE, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
	}
	
	public Jump(int addressToGoTo, InstructionConditions conditions) 
	{
		super(addressToGoTo, conditions, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
	}
	
	public static Jump createJr(String[] args, String rootSegment)
	{		
		final String supportedArgs = "jr only supports (String labelToJumpTo) and (InstructionCondition, String labelToJumpTo): ";	
		final String offsetError = "jr cannot accept a offset for internal calls, use a reference and for writing over data see nop - " + supportedArgs;	
		
		String labelToJumpTo = args[0];
		InstructionConditions conditions = InstructionConditions.NONE;
		if (args.length == 2)
		{
			if (CompilerUtils.isHexArg(args[1]))
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
		else if (CompilerUtils.isHexArg(args[0]))
		{
			throw new IllegalArgumentException(offsetError + Arrays.toString(args));
		}

		if (CompilerUtils.isOnlySubsegmentPartOfLabel(labelToJumpTo))
		{
			return new Jump(CompilerUtils.formSegmentLabelArg(labelToJumpTo, rootSegment), conditions);
		}
		
		return new Jump(labelToJumpTo, conditions);
	}

	@Override
	public int getWorstCaseSize(BankAddress instructAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		// If its a JR return that size. Otherwise use the default sizing for local, non JR or
		// a far jump
		BankAddress addressToGoTo = getAddressToGoTo(assignedAddresses, tempAssigns);
		if (canJr(instructAddress, addressToGoTo))
		{
			return 2;
		}
		
		return super.getWorstCaseSize(instructAddress, assignedAddresses, tempAssigns);
	}
	
	private static boolean canJr(BankAddress instAddress, BankAddress addressToGoTo)
	{
		// If we don't have full addresses we can't tell if we can jump or not
		if (!instAddress.isFullAddress() || !addressToGoTo.isFullAddress())
		{
			return false;
		}
		
		// Otherwise its local and linked so we know we will use either
		// JR or JP
		
		// See how far we need to jump. 
		int diff = getJrValue(instAddress, addressToGoTo);
		
		// Return based on if it fits in a byte (i.e. we can use JR)
		return diff >= -128 && diff <= 127;
	}
	
	private static int getJrValue(BankAddress instAddress, BankAddress addressToGoTo)
	{
		if (!instAddress.isSameBank(addressToGoTo))
		{
			return Integer.MAX_VALUE;
		}
		
		// Minus 2 because its relative to the end of the jump
		// instruction (i.e. we jump less far) and we assume JR for this
		return addressToGoTo.getAddressInBank() - instAddress.getAddressInBank() - 2;
	}

	@Override
	public int writeBytes(byte[] bytes, int addressToWriteAt, AssignedAddresses assignedAddresses) 
	{		
		BankAddress bankAddressToWriteAt = new BankAddress(addressToWriteAt);
		BankAddress addressToGoTo = getAddressToGoTo(assignedAddresses, null);
		if (!addressToGoTo.isFullAddress())
		{
			if (labelToGoTo != null)
			{
				throw new IllegalAccessError("Jump tried to write address for " + labelToGoTo + " but it is not fully assigned: " + addressToGoTo.toString());
			}
			throw new IllegalAccessError("Jump tried to write specific address but it is not fully assigned: " + addressToGoTo.toString());
		}
		
		// See if we want to JR
		if (canJr(bankAddressToWriteAt, addressToGoTo))
		{
			return writeJr(bytes, addressToWriteAt, (byte) getJrValue(bankAddressToWriteAt, addressToGoTo));
		}
		// Otherwise its a normal jump or farjump
		else
		{
			return super.writeBytes(bytes, addressToWriteAt, assignedAddresses);
		}
	}
}
