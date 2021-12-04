package compiler.static_instructs;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gbc_framework.SegmentedWriter;
import compiler.StaticInstruction;

public class RawBytes extends StaticInstruction
{
	List<byte[]> allBytes;
	
	public RawBytes(byte... bytes) 
	{
		super(bytes.length);
		
		allBytes = new LinkedList<>();
		allBytes.add(bytes);
	}
	
	public RawBytes(byte[]... bytes) 
	{
		super(determineSize(bytes));
		allBytes = new LinkedList<>();
		Collections.addAll(allBytes, bytes);
	}
	
	public RawBytes(List<byte[]> bytes) 
	{
		super(bytes.size());
		allBytes = new LinkedList<>(bytes);
	}
	
	private static int determineSize(byte[]... bytes)
	{
		int size = 0;
		for (byte[] set : bytes)
		{
			size += set.length;
		}
		return size;
	}

	@Override
	public void writeStaticBytes(SegmentedWriter writer) throws IOException
	{
		for (byte[] set : allBytes)
		{
			writer.append(set);
		}
	}
}
