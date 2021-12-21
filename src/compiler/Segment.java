package compiler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gbc_framework.QueuedWriter;
import compiler.reference_instructs.PlaceholderInstruction;
import gbc_framework.RomConstants;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

public class Segment
{
	List<Instruction> data;
	List<PlaceholderInstruction> placeholderInstructs;
	
	public Segment()
	{
		data = new LinkedList<>();
		placeholderInstructs = new LinkedList<>();
	}
	
	public void appendInstruction(Instruction instruct)
	{
		data.add(instruct);
	}
	
	public void appendPlaceholderInstruction(PlaceholderInstruction instruct)
	{
		appendInstruction(instruct);
		placeholderInstructs.add(instruct);
	}
	
	public int getWorstCaseSize()
	{
		return getWorstCaseSize(null, null, null);
	}
	
	public int getWorstCaseSize(BankAddress segmentAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempIndexes)
	{
		// If its null, assume its unassigned
		if (segmentAddress == null)
		{
			segmentAddress = BankAddress.UNASSIGNED.newAtStartOfBank();
		}
		// If its address is not assigned, we need to set it to the start of the bank
		// or else it will cause oddities
		else if (segmentAddress.isAddressInBankUnassigned())
		{
			segmentAddress = segmentAddress.newAtStartOfBank();
		}
		
		BankAddress instructAddr = segmentAddress;
		for (Instruction item : data)
		{
			int instructSize = item.getWorstCaseSize(instructAddr, assignedAddresses, tempIndexes);
			// If it doesn't fit, we have an issue
			if (!instructAddr.fitsInBankAddressWithOffset(instructSize))
			{
				return -1;
			}
			instructAddr = instructAddr.newOffsettedWithinBank(instructSize);
		}
		
		// The instruction can be null if the last instruction perfectly aligned with the end
		// of the bank
		if (instructAddr == null)
		{
			return RomConstants.BANK_SIZE - segmentAddress.getAddressInBank();
		}
		return instructAddr.getAddressInBank() - segmentAddress.getAddressInBank();
	}
	
	public void fillPlaceholders(Map<String, String> placeholderToArgs, InstructionParser instructParser)
	{
		for (PlaceholderInstruction instruct : placeholderInstructs)
		{
			instruct.fillPlaceholdersAndCreateInstruction(placeholderToArgs, instructParser);
		}
	}
	
	public int writeBytes(QueuedWriter writer, BankAddress segmentStartAddress, AssignedAddresses assignedAddresses) throws IOException
	{
		BankAddress instructAddress = new BankAddress(segmentStartAddress);
		for (Instruction item : data)
		{
			instructAddress.offsetWithinBank(item.writeBytes(writer, instructAddress, assignedAddresses));
		}
		
		return segmentStartAddress.getDifference(instructAddress);
	}
}
