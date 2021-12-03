package compiler.reference_instructs;


import java.io.IOException;

import gbc_framework.SegmentedWriter;
import compiler.FixedLengthInstruct;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;

public class BlockGlobalAddress extends FixedLengthInstruct
{
	static final int SIZE = 3;
	String addressLabel;
	int offset;
	
	public BlockGlobalAddress(String addressLabel, int offset)
	{
		super(SIZE);
		this.addressLabel = addressLabel;
		this.offset = offset;
	}

	@Override
	public int getWorstCaseSize(BankAddress unused1, AssignedAddresses unused2, AssignedAddresses unused3)
	{
		return SIZE;
	}

	@Override
	public void writeFixedSizeBytes(SegmentedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException 
	{
		BankAddress address = assignedAddresses.getTry(addressLabel);
		if (!address.isFullAddress())
		{
			throw new IllegalAccessError("BlockGlobalAddress tried to write address for " + addressLabel + " but it is not fully assigned: " + address.toString());
		}
		
		writer.append(ByteUtils.toLittleEndianBytes(RomUtils.convertToGlobalAddress(address) - (long) offset, SIZE));
	}
}
