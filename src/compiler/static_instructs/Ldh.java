package compiler.static_instructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;

import java.util.Arrays;

import compiler.CompilerConstants.Register;

public class Ldh extends StaticInstruction
{
	public static final int SIZE = 2;
	byte value;
	boolean isAFirst;
	
	// values for constants defined in hram.asm if I want to add them as an enum
	
	public Ldh(byte ffAddressSecondByte, boolean isACommaVal) 
	{
		super(SIZE);
		value = ffAddressSecondByte;
		isAFirst = isACommaVal;
	}
	
	public static Ldh create(String[] args)
	{		
		final String SUPPORT_STRING = "Ldh only supports (A, [$FF(00 - FF)]) or ([$FF(00 - FF)], A): Given ";
		if (args.length != 2)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}

		// A, val
		short memAddress;
		try
		{
			memAddress = CompilerUtils.parseMemoryAddressArg(args[1]);
			if (CompilerUtils.parseRegisterArg(args[0]) == Register.A &&
					// This will check only the first byte. Might be tempting to do a greater than 
					// compare but shouldn't because Java only used signed values so the compare would
					// need to be different than expected
					(byte) (memAddress >> 8) == (byte)0xFF) 
			{
				// Pass only second byte
				return new Ldh((byte) (memAddress & 0xFF), true); // true = A, val
			}
		}
		catch (IllegalArgumentException iae) 
		{
			// The instruct doesn't fit - try the next one
		}

		// val, A
		try
		{
			memAddress = CompilerUtils.parseMemoryAddressArg(args[0]);
			if (// This will check only the first byte. Might be tempting to do a greater than 
					// compare but shouldn't because Java only used signed values so the compare would
					// need to be different than expected
					(byte) (memAddress >> 8) == (byte)0xFF &&
					CompilerUtils.parseRegisterArg(args[1]) == Register.A)
			{
				// Pass only second byte
				return new Ldh((byte) (memAddress & 0xFF), false); // false = val, A
			}
		}
		catch (IllegalArgumentException iae) 
		{
			// The instruct doesn't fit
			// Could throw here but kept to preserve the pattern being used for
			// the instructs to support more easily adding future ones without
			// forgetting to add the throw at the end
		}
		
		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt)
	{
		// A, val
		if (isAFirst)
		{
			bytes[indexToWriteAt++] = (byte) 0xF0;
		}
		// val, A
		else
		{
			bytes[indexToWriteAt++] = (byte) 0xE0;
		}
		bytes[indexToWriteAt] = value;
	}
}
