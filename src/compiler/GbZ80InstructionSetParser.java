package compiler;

import java.util.List;

import compiler.reference_instructs.Call;
import compiler.reference_instructs.Jp;
import compiler.reference_instructs.Jr;
import compiler.static_instructs.Cp;
import compiler.static_instructs.Dec;
import compiler.static_instructs.Inc;
import compiler.static_instructs.Lb;
import compiler.static_instructs.Ld;
import compiler.static_instructs.Ldh;
import compiler.static_instructs.Nop;
import compiler.static_instructs.Or;
import compiler.static_instructs.Pop;
import compiler.static_instructs.Push;
import compiler.static_instructs.RawBytes;
import compiler.static_instructs.Ret;
import compiler.static_instructs.Rst;
import compiler.static_instructs.Sub;

public class GbZ80InstructionSetParser implements InstructionSetParser
{		
	@Override
	public List<String> getSupportedInstructions() 
	{
		return List.of("lb", "ld", "ldh", "cp", "or", "jr", "jp", "call", "ret", "dec", "inc", "sub", "rst", "pop", "push", "nop", "bytes");
	}

	@Override
	public Instruction parseInstruction(String instruction, String args, String rootSegment)
	{
		String[] splitArgs = CompilerUtils.splitArgs(args);
		switch (instruction)
		{
			// Loading
			case "lb":
				return Lb.create(splitArgs);
			case "ld":
				return Ld.create(splitArgs);
			case "ldh":
				return Ldh.create(splitArgs);
		
			// Logic
			case "cp":
				return Cp.create(splitArgs);
			case "or":
				return Or.create(splitArgs);				
				
			// Flow control
			case "jr":
				// JR is a bit special because we only allow it inside a block and we only
				// allow referencing labels
				return Jr.create(splitArgs, rootSegment);
			case "jp":
				return Jp.create(splitArgs, rootSegment);
			case "call":
				return Call.create(splitArgs, rootSegment);
			case "ret":
				return Ret.create(splitArgs);
				
			// Arithmetic
			case "dec":
				return Dec.create(splitArgs);
			case "inc":
				return Inc.create(splitArgs);
			case "sub":
				return Sub.create(splitArgs);
				
			// Misc
			case "rst":
				return Rst.create(splitArgs);
			case "pop":
				return Pop.create(splitArgs);
			case "push":
				return Push.create(splitArgs);
			case "nop":
				return Nop.create(splitArgs);
			case "bytes":
				return RawBytes.create(splitArgs);
				
			default:
				throw new UnsupportedOperationException("Unrecognized instruction: " + instruction);
		}
	}
}
