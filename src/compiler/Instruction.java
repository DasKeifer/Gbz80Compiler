package compiler;


import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;

public interface Instruction
{			
	public abstract int getWorstCaseSize(BankAddress instructionAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns);

	// Return size written or something else?
	public abstract int writeBytes(byte[] bytes, int addressToWriteAt, AssignedAddresses assignedAddresses);
}
