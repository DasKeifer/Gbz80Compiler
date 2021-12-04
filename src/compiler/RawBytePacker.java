package compiler;

import java.util.LinkedList;
import java.util.List;

import compiler.static_instructs.RawBytes;

public class RawBytePacker 
{
	List<byte[]> allBytes;
	
	public RawBytePacker()
	{
		allBytes = new LinkedList<>();
	}
	
	public void append(byte... bytes)
	{
		allBytes.add(bytes);
	}
	
	public void append(List<Byte> bytes)
	{
		byte[] asArray = new byte[bytes.size()];
		int index = 0;
		for (Byte val : bytes)
		{
			asArray[index++] = val;
		}
		allBytes.add(asArray);
	}
	
	public RawBytes createRawByteInsruct()
	{
		return new RawBytes(allBytes);
	}
}
