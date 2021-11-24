package compiler;

import gbc_framework.utils.ByteUtils;

public class CompilerConstants 
{
	public enum Register
	{
		B	(0x0), 
		C	(0x1), 
		D	(0x2), 
		E	(0x3), 
		H	(0x4), 
		L	(0x5), 
		BRACKET_HL_BRACKET	(0x6), 
		A	(0x7), 
		NONE(0xFF);
		
		private byte value;
		
		private Register(int inValue)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException(
						"Invalid constant input for Register enum " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
	}
	
	public enum RegisterPair
	{
		BC	(0x0), 
		DE	(0x1), 
		HL	(0x2),
		SP	(0x3),
		NONE(0xFF);
		
		private byte value;
		
		private RegisterPair(int inValue)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException(
						"Invalid constant input for RegisterPair enum " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
	}
	
	public enum PushPopRegisterPair
	{
		BC	(0x0), 
		DE	(0x1), 
		HL	(0x2),
		AF	(0x3),
		NONE(0xFF);
		
		private byte value;
		
		private PushPopRegisterPair(int inValue)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException(
						"Invalid constant input for PushPopRegisterPair enum " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
	}
	
	public enum InstructionConditions
	{
		NZ	(0x0), 
		Z	(0x1), 
		NC	(0x2), 
		C	(0x3),
		NONE(0xFF);
		
		private byte value;
		
		private InstructionConditions(int inValue)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException(
						"Invalid constant input for InstructionConditions enum " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
	}
}
