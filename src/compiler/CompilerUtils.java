package compiler;


import java.util.Arrays;

import compiler.CompilerConstants.*;
import gbc_framework.SegmentNamingUtils;
import gbc_framework.utils.ByteUtils;

public final class CompilerUtils 
{
	private CompilerUtils() {}
	
	static final String SEGMENT_ENDLINE = ":";
	static final String STRING_QUOTE = "\n";
	static final String LINE_BREAK = "\n";
	static final String HEX_VAL_MARKER = "$";
	
	public static String tryParseSegmentName(String line)
	{
		if (line.endsWith(CompilerUtils.SEGMENT_ENDLINE))
		{
			return getSegmentName(line);
		}
		return null;
	}

	private static String getSegmentName(String line)
	{
		return line.substring(0, line.indexOf(CompilerUtils.SEGMENT_ENDLINE)).trim();
	}
	
	public static String tryParseFullSubsegmentName(String line, String rootSegmentName)
	{
		if (SegmentNamingUtils.isOnlySubsegmentPartOfLabel(line))
		{
			return getSubsegmentName(line, rootSegmentName);
		}
		return null;
	}
	
	private static String getSubsegmentName(String line, String rootSegmentName)
	{
		return rootSegmentName + line.trim();
	}
	
	private static String tryParseBracketedArg(String arg)
	{
		arg = arg.trim();
		if (arg.startsWith("[") && arg.endsWith("]"))
		{
			// only -1 because the end is exclusive
			return arg.substring(1, arg.length() - 1);
		}
		
		return null;
	}
	
	public static boolean parseBoolArg(String string) 
	{
		return Boolean.parseBoolean(string);
	}
	
	public static byte parseByteArg(String arg)
	{
		return ByteUtils.parseByte(extractHexValString(arg, 2));
	}

	public static short parseShortArg(String arg)
	{
		return (short) ByteUtils.parseBytes(extractHexValString(arg, 4), 2);
	}

	public static byte parseSecondByteOfShort(String arg)
	{
		return ByteUtils.parseByte(extractHexValString(arg, 2, 2));
	}
	
	public static short parseBankLoadedAddrArg(String arg)
	{
		short addr = (short) ByteUtils.parseBytes(extractHexValString(arg, 6), 3);
		if (addr >= CompilerConstants.MAX_LOADED_SIZE || addr < 0)
		{
			throw new IllegalArgumentException("Bad bank loaded address found: " + addr);
		}
		return addr;
	}

	public static int parseGlobalAddrArg(String arg)
	{
		int addr = (int) ByteUtils.parseBytes(extractHexValString(arg, 6), 3);
		if (addr >= CompilerConstants.MAX_SIZE || addr < 0)
		{
			throw new IllegalArgumentException("Bad bank loaded address found: " + addr);
		}
		return addr;
	}

	private static String extractHexValString(String arg, int numChars)
	{
		return extractHexValString(arg, numChars, 0);
	}

	public static boolean isHexArg(String arg)
	{
		return arg.contains(HEX_VAL_MARKER);
	}
	
	private static String extractHexValString(String arg, int maxNumChars, int offsetChars)
	{
		int valIdx = arg.indexOf(HEX_VAL_MARKER) + 1;
		if (valIdx <= 0)
		{
			throw new IllegalArgumentException("Failed to find the " + HEX_VAL_MARKER + 
					" hex value marker: " + arg);
		}
		
		// Handle shorter strings
		int endIdx = valIdx + maxNumChars + offsetChars;
		if (endIdx > arg.length())
		{
			endIdx = arg.length();
		}
		
		// Get the base string, split on space and return the first in case we overflowed into another arg
		return arg.substring(valIdx + offsetChars, endIdx).split(" ", 2)[0];
	}
	
	public static Register parseRegisterArg(String arg)
	{
		if (arg.trim().equalsIgnoreCase("[hl]"))
		{
			return Register.BRACKET_HL_BRACKET;
		}
		return Register.valueOf(arg.trim().toUpperCase());
	}
	
	public static boolean parseHLIncDecArg(String arg)
	{
		if (arg.trim().equalsIgnoreCase("[hli]"))
		{
			return true;
		}
		else if (arg.trim().equalsIgnoreCase("[hld]"))
		{
			return false;
		}
		
		throw new IllegalArgumentException("Passed arg is not [hli] or [hld]: " + arg);
	}

	public static RegisterPair parseRegisterPairArg(String arg)
	{
		return RegisterPair.valueOf(arg.trim().toUpperCase());
	}

	public static PushPopRegisterPair parsePushPopRegisterPairArg(String arg)
	{
		return PushPopRegisterPair.valueOf(arg.trim().toUpperCase());
	}
	
	public static InstructionConditions parseInstructionConditionsArg(String arg)
	{
		return InstructionConditions.valueOf(arg.trim().toUpperCase());
	}
	
	public static short parseMemoryAddressArg(String arg)
	{
		arg = tryParseBracketedArg(arg);
		if (arg != null)
		{
			return parseShortArg(arg);
		}
		throw new IllegalArgumentException("Passed arg is not surrounded in brackets so it is not a valid memory address: " + arg);
	}

	public static String formSegmentLabelArg(String arg, String rootSegment)
	{
		String trimmed = arg.trim();
		if (SegmentNamingUtils.isOnlySubsegmentPartOfLabel(trimmed))
		{
			return rootSegment + trimmed;
		}
		// Otherwise we assume its the full name
		return trimmed;
	}
	
	public static String[] splitInstruction(String line)
	{
		// Split and trim the array
		return Arrays.stream(line.split(" ", 2)).map(String::trim).toArray(String[]::new);
	}

	public static String[] splitArgs(String args)
	{
		// Split and trim the array
		return Arrays.stream(args.split(",")).map(String::trim).toArray(String[]::new);
	}
}
