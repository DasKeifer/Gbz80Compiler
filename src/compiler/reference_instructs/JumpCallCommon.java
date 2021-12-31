package compiler.reference_instructs;


import compiler.CompilerConstants.InstructionConditions;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

import java.io.IOException;
import java.util.Arrays;

import gbc_framework.QueuedWriter;
import compiler.CompilerUtils;
import compiler.Instruction;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;

public abstract class JumpCallCommon implements Instruction
{
	InstructionConditions conditions;
	String labelToGoTo;
	BankAddress addressToGoTo;
	
	private byte conditionlessInstruct;
	private byte conditionedInstruct;
	private byte farInstuctRstVal;
	
	
	protected JumpCallCommon(String labelToGoTo, InstructionConditions conditions, byte conditionlessInstruct, byte conditionedInstruct, byte farInstuctRstVal)
	{
		this.conditions = conditions;
		this.labelToGoTo = labelToGoTo;
		this.conditionlessInstruct = conditionlessInstruct;
		this.conditionedInstruct = conditionedInstruct;
		this.farInstuctRstVal = farInstuctRstVal;
		
		addressToGoTo = BankAddress.UNASSIGNED;
	}
	
	protected JumpCallCommon(int addressToGoTo, InstructionConditions conditions, byte conditionlessInstruct, byte conditionedInstruct, byte farInstuctRstVal)
	{
		this.conditions = conditions;
		labelToGoTo = "";
		this.conditionlessInstruct = conditionlessInstruct;
		this.conditionedInstruct = conditionedInstruct;
		this.farInstuctRstVal = farInstuctRstVal;
		
		this.addressToGoTo = new BankAddress(addressToGoTo);
	}
	
	public static boolean useRootSegment(String[] args, boolean isJp)
	{
		String callOrJpString = "call/farcall";
		if (isJp)
		{
			callOrJpString = "jr/jp/farjp";
		}
		
		String labelOrAddrToGoTo = args[0];
		if (args.length == 2)
		{
			labelOrAddrToGoTo = args[1];
		}
		else if (args.length != 1)
		{
			throw new IllegalArgumentException(callOrJpString + " only supports 1 or 2 args: given " + Arrays.toString(args));
		}
		
		// If its a subsegment it uses the root segment
		return CompilerUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo);
	}
	
	
	public static JumpCallCommon create(String[] args, String rootSegment, boolean isJp)
	{	
		final String supportedArgs = " only supports (int gloabalAddressToGoTo), (String labelToGoTo), (InstructionCondition, int gloabalAddressToGoTo) and (InstructionCondition, String labelToGoTo): ";	
		String callOrJpString = "call/farcall";
		if (isJp)
		{
			callOrJpString = "jp/farjp";
		}
		
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
				throw new IllegalArgumentException(callOrJpString + supportedArgs + iae.getMessage());	
			}
		}
		else if (args.length != 1)
		{
			throw new IllegalArgumentException(callOrJpString + supportedArgs + "given " + Arrays.toString(args));
		}

		return createHelper(labelOrAddrToGoTo, conditions, rootSegment, isJp);
	}
	
	private static JumpCallCommon createHelper(String labelOrAddrToGoTo, InstructionConditions conditions, String rootSegment, boolean isJp)
	{
		// See if its a hex address
		try 
		{
			if (isJp)
			{
				return new Jump(CompilerUtils.parseGlobalAddrArg(labelOrAddrToGoTo), conditions);
			}
			else
			{
				return new Call(CompilerUtils.parseGlobalAddrArg(labelOrAddrToGoTo), conditions);
			}
		}
		// Otherwise it should be a label
		catch (IllegalArgumentException iae)
		{
			if (isJp)
			{
				if (CompilerUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo))
				{	
					return new Jump(CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment), conditions);
				}
				return new Jump(labelOrAddrToGoTo, conditions);
			}
			else
			{
				if (CompilerUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo))
				{	
					return new Call(CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment), conditions);
				}
				return new Call(labelOrAddrToGoTo, conditions);
			}
		}
	}
	
	protected BankAddress getAddressToGoTo(AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		BankAddress address = addressToGoTo;
		if (address == BankAddress.UNASSIGNED)
		{
			address = Instruction.tryGetAddress(labelToGoTo, assignedAddresses, tempAssigns);
		}
		return address;
	}
	
	@Override
	public int getWorstCaseSize(BankAddress instructAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		if (isFarJpCall(instructAddress, getAddressToGoTo(assignedAddresses, tempAssigns)))
		{
			// If its not assigned, assume the worst
			return getFarJpCallSize();
		}
		
		// local jp/call
		return 3;
	}
	
	protected static boolean isFarJpCall(BankAddress instructAddress, BankAddress toGoTo)
	{
		// If either bank is unassigned, assume the worst
		if (instructAddress.getBank() == BankAddress.UNASSIGNED_BANK || toGoTo.getBank() == BankAddress.UNASSIGNED_BANK)
		{
			return true;
		}
		
		// If its assigned a specific address and its in the same bank or its in the home bank then we
		// don't need to use a farcall
		return !isInBankOrHomeBank(instructAddress.getBank(), toGoTo.getBank());
	}
	
	protected static boolean isInBankOrHomeBank(byte instructBank, byte toGoToBank)
	{
		return toGoToBank == 0 || toGoToBank == instructBank;
	}
	
	protected int getFarJpCallSize()
	{
		// To do a conditional far jp/call we need to do a JR before it
		if (conditions != InstructionConditions.NONE)
		{
			return 6;
		}
		return 4;
	}
	
	@Override
	public int writeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException 
	{	
		BankAddress toGoToAddress = getAddressToGoTo(assignedAddresses, null);
		if (!toGoToAddress.isFullAddress())
		{
			if (labelToGoTo != null)
			{
				throw new IllegalAccessError("JumpCallCommon tried to write address for " + labelToGoTo + " but it is not fully assigned: " + addressToGoTo.toString());
			}
			throw new IllegalAccessError("JumpCallCommon tried to write specific address but it is not fully assigned: " + addressToGoTo.toString());
		}
		
		if (isFarJpCall(instructionAddress, toGoToAddress))
		{			
			int writeSize = 0;
			// To do a conditional far jp/call we need to do a JR before it
			if (conditions != InstructionConditions.NONE)
			{
				// Write a local JR to skip the farcall/jp
				writeSize += writeJr(writer, (byte) 4);
			}
			
			writeSize += writeFarJpCall(writer, instructionAddress, toGoToAddress);
			return writeSize;
		}
		else
		{
			return writeJpCall(writer, instructionAddress, toGoToAddress);
		}
	}
	
	protected int writeJpCall(QueuedWriter writer, BankAddress instructionAddress, BankAddress addressToGoTo) throws IOException 
	{		
		// always call
		if (InstructionConditions.NONE == conditions)
		{
			writer.append(conditionlessInstruct);
		}
		// Conditional call
		else
		{
			writer.append((byte) (conditionedInstruct | ((conditions.getValue() << 3) & 0xff)));
		}
		
		// Now write the local address
		writer.append(ByteUtils.shortToLittleEndianBytes(RomUtils.convertFromBankOffsetToLoadedOffset(addressToGoTo)));
		return 3;
	}

	protected int writeFarJpCall(QueuedWriter writer, BankAddress instructionAddress, BankAddress addressToGoTo) throws IOException 
	{		
		// This is an "RST" call (id 0xC7). These are special calls loaded into the ROM at the beginning. For
		// this ROM, RST5 (id 0x28) jumps to the "FarCall" function in the home.asm which handles
		// doing the call to any location in the ROM
		writer.append((byte) (0xC7 | farInstuctRstVal)); 
		
		// Now write the rest of the address
		writer.append(addressToGoTo.getBank());
		writer.append(ByteUtils.shortToLittleEndianBytes(RomUtils.convertFromBankOffsetToLoadedOffset(addressToGoTo)));
		return 4;
	}
	
	protected int writeJr(QueuedWriter writer, byte relAddress) throws IOException 
	{
		writeJr(writer, conditions, relAddress);
		return 2;
	}	
	
	public static void writeJr(QueuedWriter writer, InstructionConditions conditions, byte relAddress) throws IOException 
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
	}
}
