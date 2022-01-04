package compiler.reference_instructs;


import java.io.IOException;
import java.util.Map;

import gbc_framework.QueuedWriter;
import gbc_framework.SegmentNamingUtils;
import compiler.Instruction;
import compiler.InstructionParser;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

public class PlaceholderInstruction implements Instruction
{
	String line;
	Instruction inst;
	String rootBlockName;
	
	private PlaceholderInstruction(String line, String rootBlockName)
	{
		this.line = line;
		this.rootBlockName = rootBlockName;
	}
	
	public static PlaceholderInstruction create(String line, String rootBlockName)
	{
		if (SegmentNamingUtils.containsPlaceholder(line) || SegmentNamingUtils.containsPlaceholder(rootBlockName))
		{
			return new PlaceholderInstruction(line, rootBlockName);
		}
		throw new IllegalArgumentException("Line does not explicitly or implicitly contain placeholder text!");
	}

	public void fillPlaceholdersAndCreateInstruction(Map<String, String> placeholderToArgs, InstructionParser instructParser)
	{
		String lineReplaced = SegmentNamingUtils.replacePlaceholders(line, placeholderToArgs);
		String rootBlockNameReplaced = SegmentNamingUtils.replacePlaceholders(rootBlockName, placeholderToArgs);
		inst = instructParser.parseInstruction(lineReplaced, rootBlockNameReplaced);
	}

	@Override
	public int getWorstCaseSize(BankAddress instructionAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns) 
	{
		if (inst == null)
		{
			return 3; // Just a typical instruction size. Shouldn't be used really
		}
		return inst.getWorstCaseSize(instructionAddress, assignedAddresses, tempAssigns);
	}

	@Override
	public int writeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException 
	{
		if (inst == null)
		{
			throw new IllegalArgumentException("Cannot write placeholder instructions! Must replace all values in it");
		}
		return inst.writeBytes(writer, instructionAddress, assignedAddresses);
	}
}
