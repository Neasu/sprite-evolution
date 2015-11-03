package algorithm;

import java.util.Comparator;

public class EAIndividualComparator implements Comparator<EAIndividual>
{
	@Override
	public int compare(EAIndividual o1, EAIndividual o2)
	{
		return new Integer(o1.getFileSize()).compareTo(new Integer(o2.getFileSize()));		// sorts ascending
	}
}
