package compiler;


import java.io.IOException;

import gbc_framework.QueuedWriter;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

public abstract class StaticInstruction extends FixedLengthInstruct
{
	protected StaticInstruction(int size) 
	{
		super(size);
	}
	
	@Override
	public void writeFixedSizeBytes(QueuedWriter writer, BankAddress unused1, AssignedAddresses unused2) throws IOException
	{
		writeStaticBytes(writer);
	}

	public abstract void writeStaticBytes(QueuedWriter writer) throws IOException;
}
