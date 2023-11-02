package compiler.reference_instructs;


import java.io.IOException;

import gbc_framework.QueuedWriter;
import compiler.LabelReferenceInstruction;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;

public class BlockGlobalAddress extends LabelReferenceInstruction
{
	public static final int SIZE = 3;
	int offset;
	
	public BlockGlobalAddress(String addressLabel, int offset)
	{
		// TODO: Check if only subsegment portion of address
		super(addressLabel);
		this.offset = offset;
	}
	
	public int getSize()
	{
		return SIZE;
	}
	
	@Override
	public int getWorstCaseSize(BankAddress unused1, AssignedAddresses unused2, AssignedAddresses unused3)
	{
		return SIZE;
	}
	
	@Override
	public int writeBytes(QueuedWriter writer, BankAddress unused, AssignedAddresses assignedAddresses) throws IOException
	{
		BankAddress address = assignedAddresses.getTry(getLabel());
		if (!address.isFullAddress())
		{
			throw new IllegalAccessError("BlockGlobalAddress tried to write address for " + getLabel() + " but it is not fully assigned: " + address.toString());
		}
		
		write(writer, address, offset);
		return SIZE;
	}

	public static void write(QueuedWriter writer, BankAddress toWrite) throws IOException
	{
		write(writer, toWrite, 0);
	}
	
	public static void write(QueuedWriter writer, BankAddress toWrite, int offset) throws IOException
	{
		writer.append(ByteUtils.toLittleEndianBytes(RomUtils.convertToGlobalAddress(toWrite) - offset, SIZE));
	}
}
