package compiler;


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
	public int writeBytes(byte[] bytes, int indexToAddAt, AssignedAddresses assignedAddresses)
	{
		writeFixedSizeBytes(bytes, indexToAddAt, assignedAddresses);
		return size;
	}

	public abstract void writeFixedSizeBytes(byte[] bytes, int indexToAddAt, AssignedAddresses assignedAddresses);
}
