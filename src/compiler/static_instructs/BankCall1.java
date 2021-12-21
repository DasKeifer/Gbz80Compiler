package compiler.static_instructs;

import java.io.IOException;
import java.util.Arrays;

import gbc_framework.QueuedWriter;
import compiler.CompilerUtils;
import compiler.StaticInstruction;
import gbc_framework.utils.ByteUtils;

public class BankCall1 extends StaticInstruction
{
	public static final int SIZE = 3;
	short value;

	public BankCall1(short bank1Address)
	{
		super(SIZE);
		this.value = bank1Address;
	}
	
	public static BankCall1 create(String[] args)
	{		
		final String SUPPORT_STRING = "BankCall1 only supports (short): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new BankCall1(CompilerUtils.parseShortArg(args[0]));
		}
		catch (IllegalArgumentException iae) 
		{
			// The instruct doesn't fit - try the next one (if there is one)
			// Could throw here but kept to preserve the pattern being used for
			// the instructs to support more easily adding future ones without
			// forgetting to add the throw at the end
		}
		
		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
	
	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		// bankcall1 is in RST 18
		writer.append((byte) (0xC7 | 0x18));
		writer.append(ByteUtils.shortToLittleEndianBytes(value));
	}
}
