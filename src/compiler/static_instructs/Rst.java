package compiler.static_instructs;

import java.io.IOException;
import java.util.Arrays;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import gbc_framework.QueuedWriter;

public class Rst extends StaticInstruction
{
	protected byte rstVal;
	
	public Rst(byte rstVal)
	{
		super(1);
		this.rstVal = rstVal;
	}
	
	protected static byte convertToRstVal(byte rst)
	{
		byte val = rst;
		// If its an index instead of the actual value, convert it
		if (val < 8)
		{
			val *= 8;
		}
		else if (val % 8 != 0)
		{
			// TODO: Error!
			return 0;
		}
		return val;
	}

	public static Rst create(String[] args)
	{
		final String SUPPORT_STRING = "rst only supports (Byte): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new Rst(CompilerUtils.parseByteArg(args[0]));
		}
		catch (IllegalArgumentException iae) 
		{
			// The instruct doesn't fit - try the next one
		}

		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}	
	
	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		write(writer, rstVal);
	}
	
	public static void write(QueuedWriter writer, byte rstValue) throws IOException
	{
		byte rst = convertToRstVal(rstValue);
		writer.append((byte) (0xC7 | rst));
	}
}
