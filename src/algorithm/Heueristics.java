package algorithm;

public class Heueristics
{
	/**
	 * Returns the highest number of columns possible, when a square of 2
	 * 
	 * @param numberOfImages
	 * @return The highest number of columns possible, when a square of 2
	 */
	public static int getColumns(int numberOfImages)
	{
		int i = 1;

		for (; i < Integer.MAX_VALUE; i++)
		{
			if (numberOfImages / Math.pow(i, 2) < 1)
			{
				return i;
			}
		}

		return i;
	}
}
