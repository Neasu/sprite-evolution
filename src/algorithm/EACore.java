package algorithm;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import com.sun.xml.internal.ws.assembler.jaxws.MustUnderstandTubeFactory;

public class EACore
{
	// Variables

	// Config
	private int		generationSize		= 0;
	private double	selectionPercentage	= 0.0;
	private long	generationCount		= 0;
	private long	maxGenerationCount	= 0;
	private double	mutationRate		= 0.0;

	private ArrayList<EAIndividual>	generation		= null;
	private EAIndividual			individualZ		= null;
	private EAIndividual			bestIndividual	= null;

	// Constructors
	/**
	 * 
	 * @param generationSize
	 * @param maxGenerationCount
	 * @param mutationRate
	 * @param selectionPercentage
	 * @param individualZ
	 */
	public EACore(int generationSize, long maxGenerationCount, double mutationRate, double selectionPercentage, EAIndividual individualZ)
	{
		initialize(generationSize, selectionPercentage, maxGenerationCount, mutationRate, individualZ);
	}

	// Methods
	private void initialize(int generationSize, double selectionPercentage, long maxGenerationCount, double mutationRate, EAIndividual individualZ)
	{
		this.generationSize = generationSize;
		this.selectionPercentage = selectionPercentage;
		this.individualZ = individualZ;
		this.maxGenerationCount = maxGenerationCount;
		this.mutationRate = mutationRate;

		// Initialize and evaluate
		this.generation = new ArrayList<EAIndividual>(Arrays.asList(EACore.createGeneration(generationSize, 1.0, individualZ)));
		this.generation.sort(new EAIndividualComperator());
		this.bestIndividual = this.generation.get(0);
		
		evaluate();
	}

	public void evaluate()
	{
		for (EAIndividual i : generation)
		{
			try
			{
				BufferedImage img = i.getData().getFullImage();
				int filesize = io.ImgLoader.getImageFileSize(img);
				i.setFileSize(filesize);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		generation.sort(new EAIndividualComperator());

		if (bestIndividual.getFileSize() > generation.get(0).getFileSize())
		{
			bestIndividual = generation.get(0);
		}
	}

	public void select()
	{
		// Calculate the part of the generation that dies using the selection rate
		int numberOfDeaths = (int) (generation.size() * selectionPercentage + 0.5);

		int size = generation.size();

		for (int i = 0; i < numberOfDeaths; i++)
		{
			generation.remove(size - i - 1);
		}
	}

	public void nextGeneration()
	{
		EAIndividual[] nextGen = createGeneration(generationSize, mutationRate, generation);
		generation = new ArrayList<EAIndividual>(Arrays.asList(nextGen));
		generationCount++;
	}

	// Statics
	public static EAIndividual mutate(EAIndividual individual, double mutationRate)
	{
		EAIndividual result = individual.getCopy();
		
		int width = result.getData().getWidth();
		int height = result.getData().getHeight();

		int possibleOperations = (int) (Math.pow(width * height, 2) * 0.5 + 0.5); // The possible number of operations to execute
		int operationsToExecute = (int) (possibleOperations * mutationRate + 0.5); // Number of changing operations to execute based on the possible number of operations to execute and the mutation rate
		
		for(int i = 0; i < operationsToExecute; i++)
		{
			int x1 = 0 + (int)(Math.random() * (width - 0  + 0));
			int y1 = 0 + (int)(Math.random() * (height - 0 + 0));
			
			int x2 = 0 + (int)(Math.random() * (width - 0 + 0));
			int y2 = 0 + (int)(Math.random() * (height - 0 + 0));
			
			result.getData().swapImage(x1, y1, x2, y2);
		}

		return result;
	}

	public static EAIndividual[] createGeneration(int generationSize, double mutationRate, EAIndividual individualZ)
	{
		if (generationSize < 0 || individualZ == null)
		{
			return null;
		}

		EAIndividual[] newIndividuals = new EAIndividual[generationSize];

		for (int i = 0; i < generationSize; i++)
		{
			newIndividuals[i] = EACore.mutate(individualZ, mutationRate); // Mutate the starting individual with 100%
		}

		return newIndividuals;
	}

	/**
	 * Creates a new generation from an existing one. If the size of the given generation is smaller than the wished generation
	 * size, every a number of children is calculated for each parent, based on its quality in relation to the overall quality
	 * of this generation.
	 * 
	 * @param generationSize
	 *            The size of the new generation
	 * @param mutationRate
	 *            The rate of mutation that will be used to generate the children
	 * @param generation
	 *            The parent generation
	 * @return A new generation with the given size, generated from the parent generation
	 */
	public static EAIndividual[] createGeneration(int generationSize, double mutationRate, ArrayList<EAIndividual> generation)
	{
		EAIndividual[] result = new EAIndividual[generationSize];

		if (generation.size() < generationSize)
		{
			int overallQuality = 0; // The quality of the whole generation (all qualities added up)

			for (EAIndividual i : generation)
			{
				overallQuality += i.getFileSize();
			}

			int resultCursor = 0;
			for (int i = 0; i < generation.size(); i++)
			{
				double qualityRelation = (1.0 / overallQuality) * generation.get(i).getFileSize(); // The relation of the quality
																									// of this individual to the
																									// sum of qualities
				double slotRelation = 1 + 1 - (1.0 / generationSize) * generation.size();
				int slots = (int) ((generationSize * qualityRelation) * slotRelation + 0.5); // The number of children this individual in going
																				// to have, based on its relative quality (at
																				// least one)

				if (slots == 0)
				{
					slots = 1;
				}

				int j = 0;
				for (; j < slots; j++)
				{
					if (!(j + resultCursor < generationSize)) // Could get out of bounds otherwise
					{
						break;
					}

					result[j + resultCursor] = EACore.mutate(generation.get(i), mutationRate);
				}

				resultCursor += j; // Increase the position by 1 so the last children of this parent will not be overwritten by
									// the first children of the next parent
			}
		}
		else
		{
			for(int i = 0; i < generationSize; i++)
			{
				result[i] = EACore.mutate(generation.get(i), mutationRate);
			}
		}

		return result;
	}

	// Getters & Setters
	public EAIndividual getIndividualZ()
	{
		return individualZ;
	}

	public double getSelectionPercentage()
	{
		return selectionPercentage;
	}

	public long getGenerationSize()
	{
		return generationSize;
	}

	public long getMaxGenerationCount()
	{
		return maxGenerationCount;
	}

	public long getGenerationCount()
	{
		return generationCount;
	}
	
	public EAIndividual[] getGeneration()
	{
		EAIndividual[] result = new EAIndividual[generation.size()];
		
		generation.toArray(result);
		
		return result;
	}

	public EAIndividual getBestIndividual()
	{
		return bestIndividual;
	}
}
