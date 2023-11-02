package compiler.reference_instructs;


import java.io.IOException;

import gbc_framework.QueuedWriter;
import compiler.LabelReferenceInstruction;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;


public class BlockBankLoadedAddress extends LabelReferenceInstruction
{
	boolean includeBank;
	public static final int SIZE = 2;
	
	public BlockBankLoadedAddress(String addressLabel, boolean includeBank)
	{
		super(addressLabel);
		this.includeBank = includeBank;
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
		BankAddress address = assignedAddresses.getThrow(getLabel());
		if (!address.isFullAddress())
		{
			throw new IllegalAccessError("BlockBankLoaded Address tried to write address for " + getLabel() + " but it is not fully assigned: " + address.toString());
		}
		write(writer, address);
		return SIZE;
	}

	public static void write(QueuedWriter writer, BankAddress toWrite) throws IOException
	{
		writer.append(ByteUtils.shortToLittleEndianBytes(RomUtils.convertFromBankOffsetToLoadedOffset(toWrite)));
	}
}
