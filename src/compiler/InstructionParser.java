package compiler;

import compiler.reference_instructs.Jump;
import compiler.reference_instructs.JumpCallCommon;
import compiler.static_instructs.BankCall1;
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
import compiler.static_instructs.Ret;
import compiler.static_instructs.Sub;

public class InstructionParser 
{	
	public Instruction parseInstruction(String line, String rootSegment)
	{		
		// Split the keyword off and then the args apart
		String[] keyArgs = CompilerUtils.splitInstruction(line);
		String[] args = CompilerUtils.splitArgs(keyArgs);
		
		return parseInstructionImpl(keyArgs, args, rootSegment);
	}

	// Override by derived class to parse custom instructs and then call this version
	protected Instruction parseInstructionImpl(String[] keyArgs, String[] args, String rootSegment)
	{
		switch (keyArgs[0])
		{
			// Loading
			case "lb":
				return Lb.create(args);
			case "ld":
				return Ld.create(args);
			case "ldh":
				return Ldh.create(args);
		
			// Logic
			case "cp":
				return Cp.create(args);
			case "or":
				return Or.create(args);				
				
			// Flow control
			case "jr":
				// JR is a bit special because we only allow it inside a block and we only
				// allow referencing labels
				return Jump.createJr(args, rootSegment);
			case "jp":
			case "farjp":
				return JumpCallCommon.create(args, rootSegment, true); // true == jump
			case "call":
			case "farcall":
				return JumpCallCommon.create(args, rootSegment, false); // false == call
			case "ret":
				return Ret.create(args);
			case "bank1call":
				return BankCall1.create(args);
				
			// Arithmetic
			case "dec":
				return Dec.create(args);
			case "inc":
				return Inc.create(args);
			case "sub":
				return Sub.create(args);
				
			// Misc
			case "pop":
				return Pop.create(args);
			case "push":
				return Push.create(args);
			case "nop":
				return Nop.create(args);
				
			// Writing raw data
				
			default:
				throw new UnsupportedOperationException("Unrecognized instruction key: " + keyArgs[0]);
		}
	}
}
