package compiler;


import java.io.IOException;
import java.util.Map;

import gbc_framework.QueuedWriter;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

public interface Instruction
{			
	public abstract boolean containsPlaceholder();
	
	public abstract void replacePlaceholderIfPresent(Map<String, String> placeholderToArgs);
	
	public abstract int getWorstCaseSize(BankAddress instructionAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns);

	// Return size written or something else?
	public abstract int writeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException;
	
	public static BankAddress tryGetAddress(String label, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		BankAddress address = BankAddress.UNASSIGNED;
		// Try and get it from the temp indexes first
		if (tempAssigns != null)
		{
			address = tempAssigns.getTry(label);
		}
		
		// If it wasn't in the temp or there wasn't a temp, then try the assigned addresses
		if (address == BankAddress.UNASSIGNED && assignedAddresses != null)
		{
			address = assignedAddresses.getTry(label);
		}
		return address;
	}
}
