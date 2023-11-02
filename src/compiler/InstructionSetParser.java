package compiler;

import java.util.List;

public interface InstructionSetParser 
{
	public Instruction parseInstruction(String instruction, String args, String rootSegment);
	public List<String> getSupportedInstructions();
}
