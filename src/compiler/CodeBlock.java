package compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compiler.reference_instructs.PlaceholderInstruction;

import java.util.Set;

import gbc_framework.ByteBlock;
import gbc_framework.RomConstants;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.RomUtils;

public class CodeBlock implements ByteBlock
{	
	public static final String END_OF_DATA_BLOCK_SUBSEG_LABEL = "__end_of_data_block__";
	LinkedHashMap<String, Segment> segments; // linked to keep order
	private String id;

	private String rootSegmentName;
	private Segment currSegment;
	private String endSegmentName; // Root name + "." + END_OF_DATA_BLOCK_SUBSEG_LABEL
	private Segment endSegment; // and empty segment so we can refer to with "." + END_OF_BLOCK_LABEL for any datablock
	
	private static InstructionParser instructParserSingleton = new InstructionParser();
	
	public static void setInstructionParserSingleton(InstructionParser instructParserToUseForAllDataBlocks)
	{
		instructParserSingleton = instructParserToUseForAllDataBlocks;
	}
	
	// Constructor to keep instruction/line less constructors from being ambiguous
	public CodeBlock(String startingSegmentName)
	{
		setDataBlockCommonData(startingSegmentName.trim());
	}
	
	public CodeBlock(List<String> sourceLines)
	{
		List<String> sourceLinesTrimmed = new ArrayList<>(sourceLines);
		String segName = CompilerUtils.tryParseSegmentName(sourceLinesTrimmed.remove(0));
		if (segName == null)
		{
			throw new IllegalArgumentException("The first line must be a Segment label (i.e. the segment name followed by a ':'");
		}
		setDataBlockCommonData(segName);

		for (String line : sourceLinesTrimmed)
		{
			parseLine(line);
		}
	}
	
	private void setDataBlockCommonData(String id)
	{
		segments = new LinkedHashMap<>();
		this.id = id;
		endSegmentName = CompilerUtils.formSubsegmentName(END_OF_DATA_BLOCK_SUBSEG_LABEL, id);
		endSegment = new Segment();
		newSegment(id);
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
			segName = CompilerUtils.tryParseFullSubsegmentName(line, rootSegmentName);
			if (segName != null)
			{
				newSubSegment(segName);
			}
		}

		// If its not a segment, then its a line that will turn into bytes
		if (segName == null)
		{
			// If its a placeholder, we defer filling it out
			if (CompilerUtils.containsPlaceholder(line) || CompilerUtils.containsImplicitPlaceholder(line, rootSegmentName))
			{
				appendPlaceholderInstruction(PlaceholderInstruction.create(line, rootSegmentName));
			}
			else
			{
				appendInstruction(instructParserSingleton.parseInstruction(line, rootSegmentName));
			}
		}
	}
	
	public void newSubSegment(String fullSubSegName)
	{
		currSegment = new Segment();
		
		// Ensure there was no conflict within the block
		if (segments.put(fullSubSegName, currSegment) != null)
		{
			throw new IllegalArgumentException("Duplicate segment label was found: " + fullSubSegName);
		}
	}
	
	public void newSegment(String name)
	{
		rootSegmentName = name;
		currSegment = new Segment();
		
		// Ensure there was no conflict within the block
		if (segments.put(name, currSegment) != null)
		{
			throw new IllegalArgumentException("Duplicate segment label was found: " + rootSegmentName);
		}
	}
	
	public void parseAppendInstruction(String instruct)
	{
		parseLine(instruct);
	}
	
	public void appendPlaceholderInstruction(PlaceholderInstruction instruct)
	{
		currSegment.appendPlaceholderInstruction(instruct);
	}
	
	public void appendInstruction(Instruction instruct)
	{
		currSegment.appendInstruction(instruct);
	}
	
	public String getEndSegmentId()
	{
		return endSegmentName;
	}

	public Set<String> getSegmentIds() 
	{
		return getSegmentIds(true);
	}
	
	public Set<String> getSegmentIds(boolean includeEndOfSegRef) 
	{
		LinkedHashSet<String> segIds = new LinkedHashSet<>(segments.keySet());
		if (includeEndOfSegRef)
		{
			segIds.add(CompilerUtils.formSubsegmentName(END_OF_DATA_BLOCK_SUBSEG_LABEL, id));
		}
		return segIds;
	}
	
	public Map<String, Segment> getSegmentsById()
	{
		return getSegmentsById(true);
	}
	
	public Map<String, Segment> getSegmentsById(boolean includeEndOfSegRef)
	{
		Map<String, Segment> segRefsById = new LinkedHashMap<>(segments);
		if (includeEndOfSegRef)
		{
			segRefsById.put(endSegmentName, endSegment);
		}
		return segRefsById;
	}

	public void replacePlaceholderIds(Map<String, String> placeholderToArgsForIds)
	{
		// Replace placeholders in Id
		id = CompilerUtils.replacePlaceholders(id, placeholderToArgsForIds);
		
		LinkedHashMap<String, Segment> refreshedSegments = new LinkedHashMap<>();
		// Use segments because we know we don't need to replace anything in the
		// end segment placeholder
		for (Entry<String, Segment> seg : segments.entrySet())
		{
			String segId = CompilerUtils.replacePlaceholders(seg.getKey(), placeholderToArgsForIds);
			seg.getValue().fillPlaceholders(placeholderToArgsForIds, instructParserSingleton);
			
			if (refreshedSegments.put(segId, seg.getValue()) != null)
			{
				throw new IllegalArgumentException("Duplicate segment label was found while replacing placeholders: " + segId);
			}
		}
		segments = refreshedSegments;
	}

	public void writeBytes(byte[] bytes, AssignedAddresses assignedAddresses)
	{
		// Don't need to write anything for the end of the segment
		for (Entry<String, Segment> segEntry : segments.entrySet())
		{
			BankAddress segAddress = assignedAddresses.getThrow(segEntry.getKey());
			segEntry.getValue().writeBytes(bytes, RomUtils.convertToGlobalAddress(segAddress), assignedAddresses);
		}
		
		// End reference has no code so no writing needs to be done
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public int getWorstCaseSize(AssignedAddresses assignedAddresses)
	{
		BankAddress blockAddress = assignedAddresses.getTry(getId());
		return getSizeAndSegmentsRelativeAddresses(blockAddress, assignedAddresses, null, false); // null = don't care about the relative address of segments, false = do not include end segment
	}

	@Override
	public void addAllIds(Set<String> usedIds)
	{
		// See if it already had an entry that is not this instance of the block
		if (!usedIds.add(getId()))
		{
			throw new IllegalArgumentException("Duplicate block ID detected! There must be only " +
					"one allocation block per data block: " + getId());
		}
	
		// Add the references for its segments
		for (String segmentId : getSegmentsById().keySet())
		{
			if (!segmentId.equals(getId()) && !usedIds.add(segmentId))
			{
				throw new IllegalArgumentException("Duplicate segment ID detected: " + segmentId);
			}
		}
	}
		
	@Override
	public void assignBank(byte bank, AssignedAddresses assignedAddresses)
	{
		for (String segId : getSegmentsById().keySet())
		{
			assignedAddresses.putBankOnly(segId, bank);
		}
	}

	@Override
	public void removeAddresses(AssignedAddresses assignedAddresses)
	{
		for (String segId : getSegmentsById().keySet())
		{
			assignedAddresses.remove(segId);
		}
	}

	@Override
	public void assignAddresses(BankAddress blockAddress, AssignedAddresses assignedAddresses) 
	{
		// Will throw if the addresses are invalid - means we can make some assumptions here
		AssignedAddresses relAddresses = getSegmentsRelativeAddresses(blockAddress, assignedAddresses);
		
		// For each segment relative address, offset it to the block address and add it to the
		// allocated indexes		
		String segmentId;
		Iterator<String> segItr = getSegmentIds().iterator();
		while (segItr.hasNext())
		{
			segmentId = segItr.next();
			BankAddress relAddress = relAddresses.getThrow(segmentId);
			int addressInBank = relAddress.getAddressInBank() + blockAddress.getAddressInBank();
			// Make sure we didn't pass the end of the bank
			if (addressInBank > RomConstants.BANK_SIZE)
			{
				throw new RuntimeException("assignBlockAndSegmentBankAddresses Passed the end of a bank "
						+ "while assigning addresses for segment (" + segmentId + "). Each data block "
						+ "should fit assuming the blocks were successfully packed in earlier stages");
			}
			// If its the end of the bank, its ok if its the last segment or the next segment
			// is the end of block placeholder segment
			else if (addressInBank == RomConstants.BANK_SIZE)
			{
				if (!segItr.hasNext() || segItr.next().equals(getEndSegmentId()))
				{
					assignedAddresses.put(segmentId, (byte) (relAddress.getBank() + 1), (short) 0);
				}
				else
				{
					throw new RuntimeException("assignBlockAndSegmentBankAddresses Reached the end of a bank while "
							+ "assigning addresses but there is still a segment (" + segmentId + ") to assign. Each "
							+ "data block should fit assuming the blocks were successfully packed in earlier stages");
				}
			}
			else
			{
				assignedAddresses.put(segmentId, relAddress.getBank(), (short) addressInBank);
			}
		}
	}
	
	private AssignedAddresses getSegmentsRelativeAddresses(BankAddress blockAddress, AssignedAddresses assignedAddresses)
	{
		AssignedAddresses relAddresses = new AssignedAddresses();
		getSizeAndSegmentsRelativeAddresses(blockAddress, assignedAddresses, relAddresses, true); // true = include end segment
		return relAddresses;
	}
	
	private int getSizeAndSegmentsRelativeAddresses(
			BankAddress blockAddress, 
			AssignedAddresses assignedAddresses, 
			AssignedAddresses relAddresses,
			boolean includeEndSeg
	)
	{
		if (relAddresses == null)
		{
			relAddresses = new AssignedAddresses();
		}
		else
		{
			relAddresses.clear();
		}
		
		// If we have none or one segment, its super easy...
		// Note we shouldn't ever have none but it doesn't hurt to catch this case in the chance
		// that this changes later
		// Empty because segments doesn't include the end segment
		if (segments.isEmpty())
		{
			return 0;
		}
		
		// Otherwise we need more complex logic
		// Get the starting point of the addresses
		Map<String, Segment> segmentsToUse = getSegmentsById(includeEndSeg);
		getSegRelAddressesStartingPoint(segmentsToUse, blockAddress, assignedAddresses, relAddresses);

		// Now do more passes until we have a stable length for all the segments
		boolean stable = false;
		int size = 0;
		while (!stable)
		{
			// Assume we are good until proven otherwise and reset the size
			stable = true;
			size = 0;
			
			BankAddress relAddress = blockAddress.newAtStartOfBank();
			Iterator<Entry<String, Segment>> segIter = segmentsToUse.entrySet().iterator();
			while (segIter.hasNext())
			{
				// Get the next entry and its address
				Entry<String, Segment> segEntry = segIter.next();
				BankAddress foundAddress = relAddresses.getThrow(segEntry.getKey());

				// Now get where we found it to be
				checkAndSetRelativeAddressToSize(segEntry.getKey(), size, relAddress);
				
				// The greater than case is handled by worst case size below - if it would be over length,
				// it is caught there on the previous iteration
				
				// If it expected address doesn't equal the assigned one, 
				// we aren't stable yet and we need to update the stored address
				if (!foundAddress.equals(relAddress))
				{
					stable = false;
					foundAddress.setToCopyOf(relAddress);
				}
						
				// Now determine the overall block size so far/where the next segments rel address would be
				int segSize = segEntry.getValue().getWorstCaseSize(relAddress, assignedAddresses, relAddresses);
				// If its less than 0, it didn't fit at the address meaning this block is too big
				if (segSize < 0)
				{
					throw new RuntimeException("Failed to assign relative address in the bank for the datablock \"" +
							getId() + "\"- Adding a segment (" + segEntry.getKey() + "( makes the datablock's worst " +
							"case size greater than the bank size (" + RomConstants.BANK_SIZE + ")");
				}
				size += segSize;
			}
		}
		
		return size;
	}
	
	private void checkAndSetRelativeAddressToSize(String segmentBeingChecked, int size, BankAddress relAddressToSet)
	{
		if (size < RomConstants.BANK_SIZE)
		{
			relAddressToSet.setAddressInBank((short) size);
		}
		else if (size == RomConstants.BANK_SIZE)
		{
			// If we are at the limit at this is the end segment, we are fine
			if (segmentBeingChecked.equals(endSegmentName))
			{
				relAddressToSet.setBank((byte) (relAddressToSet.getBank() + 1));
				relAddressToSet.setAddressInBank((short) 0);
			}
			// Otherwise we have an issue
			else
			{
				throw new RuntimeException("Failed to assign relative address in the bank for the datablock \"" + 
						getId() + "\" - The datablock's worst case size equals the bank size (" + 
						RomConstants.BANK_SIZE + ") but there are still more segments (" + segmentBeingChecked + 
						") to add");
			}
		}
	}
	
	private void getSegRelAddressesStartingPoint(
			Map<String, Segment> segmentsToUse, 
			BankAddress blockAddress, 
			AssignedAddresses assignedAddresses, 
			AssignedAddresses startingPoint
	)
	{
		BankAddress allocAddress = assignedAddresses.getTry(segmentsToUse.entrySet().iterator().next().getKey());
		if (allocAddress.isFullAddress())
		{
			// This means some allocation has already been done. Leverage that for a starting
			// point
			for (Entry<String, Segment> segEntry : segmentsToUse.entrySet())
			{
				allocAddress = assignedAddresses.getTry(segEntry.getKey());
				if (allocAddress == null)
				{
					throw new RuntimeException("Assigned addresses for data block segment fragmentation detected for block \"" + 
							getId() + "\" when getting relative segment starting points - Not all segments (" + 
							segEntry.getKey() + ") are assigned addresses!");
				}
				else if (!allocAddress.isFullAddress())
				{
					throw new RuntimeException("Assigned addresses for data block segment fragmentation detected for block \"" + 
							getId() + "\"when getting relative segment starting points - Not all are full addresses (" + 
							segEntry.getKey() + ")!");
				}
				startingPoint.put(segEntry.getKey(), blockAddress.newRelativeToAddressInBank(allocAddress.getAddressInBank()));
			}
		}
		// Otherwise we need to generate it from scratch
		else
		{			
			generateSegRelAddressesStartingPoint(segmentsToUse, blockAddress, assignedAddresses, startingPoint);
		}
	}
	
	private void generateSegRelAddressesStartingPoint(
			Map<String, Segment> segmentsToUse, 
			BankAddress blockAddress, 
			AssignedAddresses assignedAddresses, 
			AssignedAddresses startingPoint
	)
	{
		BankAddress nextSegRelAddress = blockAddress.newAtStartOfBank();
		for (Entry<String, Segment> segEntry : segmentsToUse.entrySet())
		{
			// If the address is null (i.e. we reached the end of the block)
			// and we are not on the end segment, we ran out of room. 
			if (nextSegRelAddress == null)
			{
				// If it is the end segment, then we are good - set the address to the start
				// of the next bank and we are done
				if (segEntry.getKey().equals(endSegmentName))
				{
					startingPoint.put(segEntry.getKey(), new BankAddress((byte) (blockAddress.getBank() + 1), (short) 0));
					break;
				}
				else
				{
					throw new RuntimeException("Failed to assign relative address in the bank for the datablock \"" + 
							getId() + "\" starting point - The datablock's worst case size equals the bank size (" + 
							RomConstants.BANK_SIZE + ") but there are still more segments (" + segEntry.getKey() + 
							") to add");
				}
			}
			else 
			{
				startingPoint.put(segEntry.getKey(), nextSegRelAddress);
				int segSize = segEntry.getValue().getWorstCaseSize(nextSegRelAddress, assignedAddresses, startingPoint);
				// If its less than 0, it didn't fit at the address meaning this block is too big
				if (segSize < 0)
				{
					throw new RuntimeException("Failed to assign relative address in the bank for the datablock \"" +
							getId() + "\" starting point - Adding a segment (" + segEntry.getKey() +
							") makes the datablock's worst case size greater than the bank size (" + 
							RomConstants.BANK_SIZE + ")");
				}
				nextSegRelAddress = nextSegRelAddress.newOffsetted(segSize);
			}
		}
	}
}
