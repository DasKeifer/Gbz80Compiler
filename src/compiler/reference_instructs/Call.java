package compiler.reference_instructs;


import java.io.IOException;
import java.util.Arrays;

import compiler.CompilerConstants.InstructionConditions;
import compiler.CompilerUtils;
import compiler.Instruction;
import compiler.LabelReferenceInstruction;
import gbc_framework.QueuedWriter;
import gbc_framework.SegmentNamingUtils;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;

public class Call extends LabelReferenceInstruction
{
	private InstructionConditions conditions;
	private short addressToGoTo;
	
	public Call(String labelToGoTo) 
	{
		super(labelToGoTo);
		this.conditions = InstructionConditions.NONE;
		addressToGoTo = -1;
	}
	
	public Call(String labelToGoTo, InstructionConditions conditions) 
	{
		super(labelToGoTo);
		this.conditions = conditions;
		addressToGoTo = -1;
	}
	
	public Call(short bankLoadedAddressToGoTo) 
	{
		this.conditions = InstructionConditions.NONE;
		this.addressToGoTo = bankLoadedAddressToGoTo;
	}
	
	public Call(short bankLoadedAddressToGoTo, InstructionConditions conditions) 
	{
		this.conditions = conditions;
		this.addressToGoTo = bankLoadedAddressToGoTo;
	}
	
	public static Call create(String[] args, String rootSegment)
	{	
		final String supportedArgs = "call only supports (int bankLoadedAddressToGoTo), (String labelToGoTo), (InstructionCondition, int bankLoadedAddressToGoTo) and (InstructionCondition, String labelToGoTo): ";	
		
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
			return new Call(CompilerUtils.parseBankLoadedAddrArg(labelOrAddrToGoTo), conditions);
		}
		// Otherwise it should be a label
		catch (IllegalArgumentException iae)
		{
			if (SegmentNamingUtils.isOnlySubsegmentPartOfLabel(labelOrAddrToGoTo))
			{
				labelOrAddrToGoTo = CompilerUtils.formSegmentLabelArg(labelOrAddrToGoTo, rootSegment);
			}
			return new Call(labelOrAddrToGoTo, conditions);
		}
	}
	
	protected static boolean isValidCall(BankAddress instructAddr, BankAddress labelAddr)
	{
		// It must be either in the same bank or in the always loaded bank
		return labelAddr.getBank() == instructAddr.getBank() || labelAddr.getBank() == 0 || instructAddr.getBank() == 0;
	}
	
	@Override
	public int getWorstCaseSize(BankAddress instructAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		// Always 3 - no call relative like there is for jump
		return 3;
	}
	
	@Override
	public int writeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException 
	{
		short localAddr = addressToGoTo;
		if (addressToGoTo < 0)
		{
			BankAddress labelAddr = Instruction.tryGetAddress(getLabel(), assignedAddresses, null);
			if (!isValidCall(instructionAddress, labelAddr))
			{
				throw new IllegalArgumentException("Assigned address for call at " + instructionAddress.toString() +
						" is not in range of assigned label address at " + labelAddr.toString());
			}
			localAddr = RomUtils.convertFromBankOffsetToLoadedOffset(labelAddr);
		}
		
		// always call
		if (InstructionConditions.NONE == conditions)
		{
			writer.append((byte) 0xCD);
		}
		// Conditional call
		else
		{
			writer.append((byte) ((byte) 0xC4 | ((conditions.getValue() << 3) & 0xff)));
		}
		
		// Now write the local address
		writer.append(ByteUtils.shortToLittleEndianBytes(localAddr));
		return 3;
	}
	
	protected InstructionConditions getConditions() {
		return conditions;
	}

	protected void setConditions(InstructionConditions conditions) {
		this.conditions = conditions;
	}

	protected short getAddressToGoTo() {
		return addressToGoTo;
	}

	protected void setAddressToGoTo(short addressToGoTo) {
		this.addressToGoTo = addressToGoTo;
	}
}
