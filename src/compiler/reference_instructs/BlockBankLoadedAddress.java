package compiler.reference_instructs;


import java.io.IOException;

import gbc_framework.QueuedWriter;
import compiler.FixedLengthInstruct;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;


public class BlockBankLoadedAddress extends FixedLengthInstruct
{
	String addressLabel;
	boolean includeBank;
	
	public BlockBankLoadedAddress(String addressLabel, boolean includeBank)
	{
		// Size depends on if the bank is included or not
		super(includeBank ? 3 : 2);
		this.addressLabel = addressLabel;
		this.includeBank = includeBank;
	}
	
	@Override
	public void writeFixedSizeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException 
	{
		BankAddress address = assignedAddresses.getThrow(addressLabel);
		if (!address.isFullAddress())
		{
			throw new IllegalAccessError("BlockBankLoaded Address tried to write address for " + addressLabel + " but it is not fully assigned: " + address.toString());
		}
		
		if (includeBank)
		{
			writer.append(address.getBank());
		}
		writer.append(ByteUtils.shortToLittleEndianBytes(RomUtils.convertFromBankOffsetToLoadedOffset(address)));
	}
}
