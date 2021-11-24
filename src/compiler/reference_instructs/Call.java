package compiler.reference_instructs;


import compiler.CompilerConstants.InstructionConditions;

public class Call extends JumpCallCommon
{
	private static final byte CONDITIONLESS_INSTRUCT = (byte) 0xCD;
	private static final byte CONDITIONED_INSTRUCT = (byte) 0xC4;
	private static final byte FAR_INSTRUCT_RST_VAL = 0x28;
	
	public Call(String labelToGoTo, InstructionConditions conditions) 
	{
		super(labelToGoTo, conditions, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
	}
	
	public Call(int addressToGoTo, InstructionConditions conditions) 
	{
		super(addressToGoTo, conditions, CONDITIONLESS_INSTRUCT, CONDITIONED_INSTRUCT, FAR_INSTRUCT_RST_VAL);
	}
}
