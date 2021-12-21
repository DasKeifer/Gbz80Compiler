package compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gbc_framework.QueuedWriter;
import compiler.reference_instructs.PlaceholderInstruction;
import compiler.static_instructs.Nop;

import java.util.Set;

import gbc_framework.SegmentedByteBlock;
import gbc_framework.RomConstants;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.RomUtils;

public class CodeBlock implements SegmentedByteBlock
{	
	LinkedHashMap<String, Segment> segments; // linked to keep order
	private String id;

	private String rootSegmentName;
	private Segment currSegment;
	
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
	
	@Override
	public Set<String> getSegmentIds() 
	{
		return getOrderedSegmentIds();
	}

	public Set<String> getOrderedSegmentIds() 
	{
		return new LinkedHashSet<>(segments.keySet());
	}
	
	public Map<String, Segment> getOrderedSegmentsById()
	{
		return new LinkedHashMap<>(segments);
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
	
	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public int getWorstCaseSize(AssignedAddresses assignedAddresses)
	{        
		BankAddress blockAddress = BankAddress.UNASSIGNED;
		if (assignedAddresses != null)
		{
			blockAddress = assignedAddresses.getTry(getId());
		}
		return blockAddress.newAtStartOfBank().getDifference(
				getSegmentsRelativeAddresses(blockAddress, assignedAddresses, null)); // null = don't care about the relative address of segments
	}
		
	@Override
	public void assignBank(byte bank, AssignedAddresses assignedAddresses)
	{
		for (String segId : getOrderedSegmentsById().keySet())
		{
			assignedAddresses.putBankOnly(segId, bank);
		}
	}

	@Override
	public void removeAddresses(AssignedAddresses assignedAddresses)
	{
		for (String segId : getOrderedSegmentsById().keySet())
		{
			assignedAddresses.remove(segId);
		}
	}

	@Override
	public BankAddress getSegmentsRelativeAddresses(
			BankAddress blockAddress,
			AssignedAddresses assignedAddresses, 
			AssignedAddresses relAddresses
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
			return BankAddress.ZERO;
		}
		
		// Otherwise we need more complex logic
		// Get the starting point of the addresses
		Map<String, Segment> orderedSegmentsToUse = getOrderedSegmentsById();
		getSegRelAddressesStartingPoint(blockAddress, orderedSegmentsToUse, assignedAddresses, relAddresses);

		// Now do more passes until we have a stable length for all the segments
		boolean stable = false;
		BankAddress nextExpectedAddress = blockAddress.newAtStartOfBank();
		while (!stable)
		{
			// Assume we are good until proven otherwise and reset the size
			stable = true;			
			nextExpectedAddress = blockAddress.newAtStartOfBank();
			Iterator<Entry<String, Segment>> segItr = orderedSegmentsToUse.entrySet().iterator();
			while (segItr.hasNext())
			{
				Entry<String, Segment> segEntry = segItr.next();
				
				// Get the next entry and its address
				BankAddress foundAddress = relAddresses.getThrow(segEntry.getKey());
				
				// If it expected address doesn't equal the assigned one, 
				// we aren't stable yet and we need to update the stored address
				if (!foundAddress.equals(nextExpectedAddress))
				{
					stable = false;
					foundAddress.setToCopyOf(nextExpectedAddress);
				}
				
				// See if we can set the next address to the offset
				checkAndOffsetExpectedAddressBySize(segEntry.getValue().getWorstCaseSize(blockAddress, assignedAddresses, relAddresses),
						!segItr.hasNext(), segEntry.getKey(), nextExpectedAddress);
			}
		}
		
		// Return the total size of the block
		return nextExpectedAddress;
	}
	
	private void checkAndOffsetExpectedAddressBySize(int segSize, boolean isLast, String segmentBeingChecked, BankAddress nextExpectedAddressToSet)
	{
		if (!nextExpectedAddressToSet.offsetWithinBank(segSize))
		{
			// If not, if it has another segment, we are in trouble regardless
			if (!isLast)
			{
				throw new RuntimeException("Failed to assign relative address in the bank for the datablock \"" + 
						getId() + "\" - The datablock's worst case size is greater than or equal to the bank size (" + 
						RomConstants.BANK_SIZE + ") and there are still more segments (" + segmentBeingChecked + 
						") to add");
			}
			// But if this was the last segment, see if we just fit in the
			// bank in which case we can set the address to the next bank and
			// be ok
			else if (!nextExpectedAddressToSet.fitsInBankAddressWithOffset(segSize))
			{
				throw new RuntimeException("Failed to assign relative address in the bank for the datablock \"" + 
						getId() + "\" - The datablock's worst case size is greater than or equal to the bank size (" + 
						RomConstants.BANK_SIZE + ") when adding the last segment (" + segmentBeingChecked + ")");
			}
			nextExpectedAddressToSet.setToCopyOf(nextExpectedAddressToSet.newAtStartOfNextBank());
		}
	}
	
	private void getSegRelAddressesStartingPoint(
			BankAddress blockBank,
			Map<String, Segment> orderedSegmentsToUse,
			AssignedAddresses assignedAddresses, 
			AssignedAddresses startingPoint
	)
	{		
		BankAddress baseAllocAddr = BankAddress.UNASSIGNED;
		if (assignedAddresses != null)
		{
			baseAllocAddr = assignedAddresses.getTry(orderedSegmentsToUse.entrySet().iterator().next().getKey());
		}
		
		if (assignedAddresses != null && baseAllocAddr.isFullAddress())
		{
			// This means some allocation has already been done. Leverage that for a starting
			// point
			for (Entry<String, Segment> segEntry : orderedSegmentsToUse.entrySet())
			{
				BankAddress allocAddress = assignedAddresses.getTry(segEntry.getKey());
				if (!allocAddress.isFullAddress())
				{
					// TODO: call other version below
					throw new RuntimeException("Assigned addresses for data block segment fragmentation detected for block \"" + 
							getId() + "\"when getting relative segment starting points - Not all are full addresses (" + 
							segEntry.getKey() + ")!");
				}
				int diff = baseAllocAddr.getDifference(allocAddress);
				if (diff < 0 || diff > RomConstants.BANK_SIZE) // segBefore block or block larger than bank
				{
					throw new RuntimeException("TODO: use below");
				}
				startingPoint.put(segEntry.getKey(), new BankAddress(blockBank.getBank(), (short) diff));
			}
		}
		// Otherwise we need to generate it from scratch
		else
		{			
			generateSegRelAddressesStartingPoint(blockBank, orderedSegmentsToUse, startingPoint);
		}
	}
	
	private void generateSegRelAddressesStartingPoint(
			BankAddress blockBank,
			Map<String, Segment> orderedSegmentsToUse, 
			AssignedAddresses startingPoint
	)
	{
		BankAddress nextExpectedAddress = blockBank.newAtStartOfBank();			
		Iterator<Entry<String, Segment>> segItr = orderedSegmentsToUse.entrySet().iterator();
		while (segItr.hasNext())
		{
			Entry<String, Segment> segEntry = segItr.next();
			startingPoint.put(segEntry.getKey(), new BankAddress(nextExpectedAddress));
			
			// See if we can set the next address to the offset
			checkAndOffsetExpectedAddressBySize(segEntry.getValue().getWorstCaseSize(),
					!segItr.hasNext(), segEntry.getKey(), nextExpectedAddress);
		}
	}

	@Override
	public BankAddress write(QueuedWriter writer, AssignedAddresses assignedAddresses) throws IOException
	{
		// Set the expected address to the address of this block
		BankAddress endOfLastSegment = new BankAddress(assignedAddresses.getThrow(id));
		
		// Trigger a new write segment in the writer. All block segments will be written in 
		// the same write segment
		writer.startNewBlock(RomUtils.convertToGlobalAddress(endOfLastSegment));
		
		// Now write each segment keeping track of its end address to check for gaps
		// and to return the overall write size		
		Iterator<Entry<String, Segment>> segItr = getOrderedSegmentsById().entrySet().iterator();
		while (segItr.hasNext())
		{
			if (RomUtils.convertToGlobalAddress(endOfLastSegment) > 124000 && RomUtils.convertToGlobalAddress(endOfLastSegment) < 124400)
			{
			int i = 0;
			}
			Entry<String, Segment> segEntry = segItr.next();
			BankAddress nextSegAddress = assignedAddresses.getThrow(segEntry.getKey());
			
			// Ensure there are no gaps
			checkAndFillSegmentGaps(endOfLastSegment, nextSegAddress, writer, segEntry.getKey());
			
			// See if we can set the next address to the offset - we should be able to
			// or else the write would fail
			checkAndOffsetExpectedAddressBySize(segEntry.getValue().writeBytes(writer, nextSegAddress, assignedAddresses),
					!segItr.hasNext(), segEntry.getKey(), endOfLastSegment);
		}
		return endOfLastSegment;
	}
	
	@Override
	public void checkAndFillSegmentGaps(
			BankAddress endOfPreviousSegment,
			BankAddress foundNextAddress, 
			QueuedWriter writer,
			String nextSegName
	) throws IOException
	{
		int diff = endOfPreviousSegment.getDifference(foundNextAddress);
		
		// If we endOfPreviousSegment (previous + size) is before the next segment,
		// then fill with nops
		if (diff > 0)
		{
			new Nop(diff).writeBytes(writer, endOfPreviousSegment, null);
		}
		// If its higher than the next address, then we would overwrite the data so throw
		else if (diff < 0)
		{
			throw new IllegalArgumentException("Encountered error while writing - the end of the previous segment (" + 
					endOfPreviousSegment.toString() + ") is greater than the next address (" + foundNextAddress.toString() + 
					") meaning some bytes would be overwritten when writing segment ID " + nextSegName + " of block " + id);
		}
		// else do nothing - we are good
	}
}
