package compiler;

import java.util.Iterator;
import java.util.List;

public class ParserCodeBlock extends CodeBlock
{	
	private InstructionParser instructParser;
	
	// Constructor to keep instruction/line less constructors from being ambiguous
	public ParserCodeBlock(String startingSegmentName, InstructionParser parser)
	{
		super(startingSegmentName);
		instructParser = parser;
	}
	
	public ParserCodeBlock(List<String> sourceLines, InstructionParser parser)
	{
		this(CompilerUtils.tryParseSegmentName(sourceLines.get(0)), parser);

		Iterator<String> linesItr = sourceLines.iterator();
		linesItr.next(); // skip the starting segment name
		while (linesItr.hasNext())
		{
			parseLine(linesItr.next());
		}
	}
	
	private void parseLine(String line)
	{
		// split of the instruction (if there is one)
		line = line.trim();
		
		String segName = CompilerUtils.tryParseSegmentName(line);
		// If its not null, its a new segment
		if (segName != null)
		{
			newSegment(segName);
		}
		else // Otherwise see if its a subsegment
		{
			segName = CompilerUtils.tryParseFullSubsegmentName(line, getRootSegmentName());
			if (segName != null)
			{
				newSubSegment(segName);
			}
		}

		// If its not a segment, then its a line that will turn into bytes
		if (segName == null)
		{
			appendInstruction(instructParser.parseInstruction(line, getRootSegmentName()));
		}
	}
	
	public void parseAndAppendInstruction(String instruct)
	{
		parseLine(instruct);
	}
}
