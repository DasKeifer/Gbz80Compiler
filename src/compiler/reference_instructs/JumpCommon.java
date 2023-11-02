package compiler.reference_instructs;


import java.io.IOException;

import compiler.Instruction;
import compiler.LabelReferenceInstruction;
import compiler.CompilerConstants.InstructionConditions;
import gbc_framework.QueuedWriter;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.ByteUtils;
import gbc_framework.utils.RomUtils;

public class JumpCommon extends LabelReferenceInstruction
{
	protected InstructionConditions conditions;
	protected short addressToGoTo;
	// TODO: Have optimization settings to prevent JR & JP conversion?
	protected boolean isJr;
	
	protected JumpCommon(String labelToGoTo, InstructionConditions conditions, boolean isJr) 
	{
		super(labelToGoTo);
		this.conditions = conditions;
		addressToGoTo = -1;
		this.isJr = isJr;
	}
	
	// Only allowed for JP
	protected JumpCommon(short bankLoadedAddressToGoTo, InstructionConditions conditions, boolean isJr) 
	{
		this.conditions = conditions;
		this.addressToGoTo = bankLoadedAddressToGoTo;
		this.isJr = isJr;
	}
	
	protected static boolean canJr(BankAddress instAddress, BankAddress addressToGoTo)
	{
		// If we don't have full addresses we can't tell if we can jump or not
		if (!instAddress.isFullAddress() || !addressToGoTo.isFullAddress())
		{
			return false;
		}
		
		return isValidJrDistance(getJrValue(instAddress, addressToGoTo));
	}
	
	protected static boolean canJr(int instAddress, int addressToGoTo)
	{
		return isValidJrDistance(getJrValue(instAddress, addressToGoTo));
	}
	
	protected static int getJrValue(BankAddress instAddress, BankAddress addressToGoTo)
	{
		if (!instAddress.isSameBank(addressToGoTo))
		{
			return Integer.MAX_VALUE;
		}
		return getJrValue(addressToGoTo.getAddressInBank(), instAddress.getAddressInBank());
	}
	
	protected static int getJrValue(int instBankLoadedAddress, int bankLoadedAddressToGoTo)
	{
		// Minus 2 because its relative to the end of the jump
		// instruction (i.e. we jump less far) and we assume JR for this
		return instBankLoadedAddress - bankLoadedAddressToGoTo - 2;
	}
	
	protected static boolean isValidJump(BankAddress instructAddr, BankAddress labelAddr)
	{
		// It must be either in the same bank or in the always loaded bank
		return labelAddr.getBank() == instructAddr.getBank() || labelAddr.getBank() == 0 || instructAddr.getBank() == 0;
	}
	
	protected static boolean isValidJrDistance(int distance)
	{
		return distance >= -128 && distance <= 127; 
	}
	
	@Override
	public int getWorstCaseSize(BankAddress instructAddress, AssignedAddresses assignedAddresses, AssignedAddresses tempAssigns)
	{
		BankAddress toGoTo = Instruction.tryGetAddress(getLabel(), assignedAddresses, tempAssigns);
		if (canJr(instructAddress, toGoTo))
		{
			return 2;
		}
		return 3;
	}
	
	@Override
	public int writeBytes(QueuedWriter writer, BankAddress instructionAddress, AssignedAddresses assignedAddresses) throws IOException 
	{
		short localAddr = addressToGoTo;
		if (addressToGoTo < 0)
		{
			BankAddress labelAddr = Instruction.tryGetAddress(getLabel(), assignedAddresses, null);
			if (!isValidJump(instructionAddress, labelAddr))
			{
				throw new IllegalArgumentException("Assigned address for jump at " + instructionAddress.toString() +
						" is not in range of assigned label address at " + labelAddr.toString());
			}
			localAddr = RomUtils.convertFromBankOffsetToLoadedOffset(labelAddr);
		}
		short instLocalAddr = RomUtils.convertFromBankOffsetToLoadedOffset(instructionAddress);
		
		// JR
		if (canJr(instLocalAddr, localAddr))
		{
			return Jr.write(writer, conditions, (byte) getJrValue(instLocalAddr, localAddr));
		}

		// JP
		// Unconditional jump
		if (InstructionConditions.NONE == conditions)
		{
			writer.append((byte) 0xC3);
		}
		else // conditional jump
		{
			writer.append((byte) ((byte) 0xC2 | ((conditions.getValue() << 3) & 0xff)));
		}
		
		// Now write the local address
		writer.append(ByteUtils.shortToLittleEndianBytes(localAddr));
		return 3;
	}
}
