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
import gbc_framework.rom_addressing.BankAddress.BankAddressLimitType;

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
		
		BankAddress instructAddr = new BankAddress(segmentAddress);
		for (Instruction item : data)
		{
			// If it doesn't fit within the bank or at the start of the next, we have an issue
			if (!instructAddr.offset(item.getWorstCaseSize(instructAddr, assignedAddresses, tempIndexes), BankAddressLimitType.WITHIN_BANK_OR_START_OF_NEXT))
			{
				return -1;
			}
		}
		
		// If its a new bank and its not just at the start, then we have an issue as well
		if (instructAddr.isSameBank(segmentAddress))
		{
			return instructAddr.getAddressInBank() - segmentAddress.getAddressInBank();
		}
		// If its just in the next bank, its fine as its non-inclusive
		else if (instructAddr.getAddressInBank() == 0)
		{
			return RomConstants.BANK_SIZE - instructAddr.getAddressInBank();
		}
		// Otherwise we got lucky and got past the above check while offsetting from instructions
		else
		{
			return -1;
		}
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
			instructAddress.offset(
					item.writeBytes(writer, instructAddress, assignedAddresses), BankAddressLimitType.WITHIN_BANK);
		}
		
		return segmentStartAddress.getDifference(instructAddress);
	}
}
