package compiler;


import gbc_framework.rom_addressing.AssignedAddresses;

public abstract class StaticInstruction extends FixedLengthInstruct
{
	protected StaticInstruction(int size) 
	{
		super(size);
	}
	
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt, AssignedAddresses assignedAddresses)
	{
		writeStaticBytes(bytes, indexToAddAt);
	}

	public abstract void writeStaticBytes(byte[] bytes, int indexToAddAt);
}
