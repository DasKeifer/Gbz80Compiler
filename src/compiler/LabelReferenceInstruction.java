package compiler;


import java.util.Map;
import gbc_framework.SegmentNamingUtils;

public abstract class LabelReferenceInstruction implements Instruction
{
	private String label;

	protected LabelReferenceInstruction() 
	{
		this.label = "";
	}
	
	protected LabelReferenceInstruction(String label) 
	{
		this.label = label;
	}

	@Override
	public boolean containsPlaceholder() 
	{
		return SegmentNamingUtils.isOnlySubsegmentPartOfLabel(label);
	}
	
	@Override
	public void replacePlaceholderIfPresent(Map<String, String> placeholderToArgs) 
	{
		label = SegmentNamingUtils.replacePlaceholders(label, placeholderToArgs);
	}
	
	protected String getLabel()
	{
		return label;
	}
}
