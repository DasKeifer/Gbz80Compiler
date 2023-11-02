package compiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructionParser 
{
	protected List<InstructionSetParser> parsers;
	protected Map<String, InstructionSetParser> instructToParser;
	
	public InstructionParser(List<InstructionSetParser> parsers)
	{
		this.parsers = parsers;
		instructToParser = new HashMap<>();
	}
	
	public void initInstructMap(List<InstructionSetParser> parsers)
	{
		for (InstructionSetParser parser : parsers)
		{
			for (String supportedInstruct : parser.getSupportedInstructions())
			{
				instructToParser.putIfAbsent(supportedInstruct, parser);
			}
		}
	}
	
	public Instruction parseInstruction(String line, String rootSegment)
	{		
		// Split the keyword off and then the args apart
		String[] keyArgs = CompilerUtils.splitInstruction(line);
		String args = "";
		if (keyArgs.length > 1)
		{
			args = keyArgs[1];
		}
		
		InstructionSetParser parser = instructToParser.get(keyArgs[0]);
		return parser.parseInstruction(keyArgs[0], args, rootSegment);
	}
}
