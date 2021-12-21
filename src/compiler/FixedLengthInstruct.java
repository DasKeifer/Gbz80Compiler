package compiler;


import java.io.IOException;

import gbc_framework.QueuedWriter;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

public abstract class FixedLengthInstruct implements Instruction
{
	private int size;
	
	protected FixedLengthInstruct(int size) 
	{
		this.size = size;
	}
	
	public int getSize()
	{
		return size;
	}

	@Override
	public int getWorstCaseSize(BankAddress unused1, AssignedAddresses unused2, AssignedAddresses unused3)
	{
		return getSize();
	}
	
	@Override
	public int writeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException
	{
		writeFixedSizeBytes(writer, instructionAddress, assignedAddresses);
		return size;
	}

	public abstract void writeFixedSizeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException;
}
