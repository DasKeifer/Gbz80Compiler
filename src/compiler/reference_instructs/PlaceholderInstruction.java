package compiler.reference_instructs;


import java.util.Map;

import compiler.CompilerUtils;
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
		if (CompilerUtils.containsPlaceholder(line) || CompilerUtils.containsPlaceholder(rootBlockName))
		{
			return new PlaceholderInstruction(line, rootBlockName);
		}
		throw new IllegalArgumentException("Line does not explicitly or implicitly contain placeholder text!");
	}

	public void fillPlaceholdersAndCreateInstruction(Map<String, String> placeholderToArgs, InstructionParser instructParser)
	{
		String lineReplaced = CompilerUtils.replacePlaceholders(line, placeholderToArgs);
		String rootBlockNameReplaced = CompilerUtils.replacePlaceholders(rootBlockName, placeholderToArgs);
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
	public int writeBytes(byte[] bytes, int addressToWriteAt, AssignedAddresses assignedAddresses) 
	{
		if (inst == null)
		{
			throw new IllegalArgumentException("Cannot write placeholder instructions! Must replace all values in it");
		}
		return inst.writeBytes(bytes, addressToWriteAt, assignedAddresses);
	}
}
