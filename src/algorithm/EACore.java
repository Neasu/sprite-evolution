package algorithm;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import com.sun.xml.internal.ws.assembler.jaxws.MustUnderstandTubeFactory;

import io.ImgLoader;

public class EACore
{
	// Variables

	// Config
	private int						generationSize		= 0;
	private double					selectionPercentage	= 0.0;
	private int						generationCount		= 0;
	private int						maxGenerationCount	= 0;
	private double					mutationRate		= 0.0;

	private ArrayList<EAIndividual>	generation			= null;
	private ArrayList<EAIndividual>	knownIndividuals	= null;

	private EAIndividual			individualZ			= null;
	private EAIndividual			bestIndividual		= null;

	private static Random			rand				= null;

	// Constructors
	/**
	 * 
	 * @param generationSize
	 * @param maxGenerationCount
	 * @param mutationRate
	 * @param selectionPercentage
	 * @param individualZ
	 */
	public EACore(int generationSize, int maxGenerationCount, double mutationRate, double selectionPercentage, EAIndividual individualZ)
	{
		initialize(generationSize, selectionPercentage, maxGenerationCount, mutationRate, individualZ);
	}

	// Methods
	private void initialize(int generationSize, double selectionPercentage, int maxGenerationCount, double mutationRate, EAIndividual individualZ)
	{
		this.generationSize = generationSize;
		this.selectionPercentage = selectionPercentage;
		this.individualZ = individualZ;
		this.maxGenerationCount = maxGenerationCount;
		this.mutationRate = mutationRate;

		rand = new Random(Calendar.getInstance().getTimeInMillis());

		this.knownIndividuals = new ArrayList<EAIndividual>();

		// Initialize and evaluate

		// this.generation = new ArrayList<EAIndividual>();
		// this.generation.add(getIndividualZ());
		// getIndividualZ().setFileSize(ImgLoader.getImageFileSize(getIndividualZ().getData().getFullImage()));
		// this.bestIndividual = getIndividualZ();
		//
		this.bestIndividual = getIndividualZ();
		this.generation = new ArrayList<EAIndividual>(Arrays.asList(createGeneration(generationSize, mutationRate, individualZ))); // Generate
																																	// first
																																	// generation
																																	// from
																																	// individualZ
	}

	public boolean evaluate()
	{
		for (EAIndividual i : generation)
		{
			try
			{
				BufferedImage img = i.getData().getFullImage();
				int filesize = io.ImgLoader.getImageFileSize(img);
				i.setFileSize(filesize);

				if (bestIndividual != null && bestIndividual.getFileSize() < i.getFileSize())
				{
					knownIndividuals.add(i);
				}
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		generation.sort(new EAIndividualComperator());

		if (bestIndividual == null || bestIndividual.getFileSize() == 0 || bestIndividual.getFileSize() > generation.get(0).getFileSize())
		{
			bestIndividual = generation.get(0);
			return true;
		}
		else
		{
			return false;
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

	public void nextGenerationFromLast()
	{
		EAIndividual[] nextGen = createGeneration(generationSize, mutationRate, generation);
		generation = new ArrayList<EAIndividual>(Arrays.asList(nextGen));
		generationCount++;
	}

	public void nextGenerationFromBest()
	{
		generation = new ArrayList<EAIndividual>(Arrays.asList(createGeneration(generationSize, mutationRate, getBestIndividual())));
		generationCount++;
	}

	// Statics
	public EAIndividual mutate(EAIndividual individual, double mutationRate)
	{
		EAIndividual result = individual.getCopy();

		int width = result.getData().getWidth();
		int height = result.getData().getHeight();

		int operationsToExecute = (int) ((((width * height) * 0.5) * mutationRate));

		if (operationsToExecute < 1)
		{
			operationsToExecute = 1;
		}

		for (int i = 0; i < operationsToExecute; i++)
		{
			int x1 = 0;
			int y1 = 0;

			int x2 = 0;
			int y2 = 0;

			while (x1 == x2 && y1 == y2)
			{
				x1 = EACore.rand.nextInt(width);
				y1 = EACore.rand.nextInt(height);
				x2 = EACore.rand.nextInt(width);
				y2 = EACore.rand.nextInt(height);
			}

			result.getData().swapImage(x1, y1, x2, y2);
		}

		if (isindividualKnown(result))
		{
			return mutate(individual, mutationRate);
		}

		return result;
	}

	public EAIndividual[] createGeneration(int generationSize, double mutationRate, EAIndividual individualZ)
	{
		if (generationSize < 0 || individualZ == null)
		{
			return null;
		}

		EAIndividual[] newIndividuals = new EAIndividual[generationSize];

		for (int i = 0; i < generationSize; i++)
		{
			newIndividuals[i] = mutate(individualZ, mutationRate); // Mutate the starting individual
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
	public EAIndividual[] createGeneration(int generationSize, double mutationRate, ArrayList<EAIndividual> generation)
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
				int slots = (int) (generationSize / generation.size() + 0.5);

				if (overallQuality > 0)
				{
					double qualityRelation = 1.0;
					qualityRelation = (1.0 / overallQuality) * generation.get(i).getFileSize();

					double slotRelation = 1 + 1 - (1.0 / generationSize) * generation.size();
					slots = (int) ((generationSize * qualityRelation) * slotRelation + 0.5);
				}

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

					result[j + resultCursor] = mutate(generation.get(i), mutationRate);
				}

				resultCursor += j; // Increase the position by 1 so the last children of this parent will not be overwritten by
									// the first children of the next parent
			}
		}
		else
		{
			for (int i = 0; i < generationSize; i++)
			{
				result[i] = mutate(generation.get(i), mutationRate);
			}
		}

		return result;
	}

	private void wipeGeneration()
	{
		for (int i = 0; i < generation.size(); i++)
		{
			generation.remove(i);
		}
	}

	private boolean isindividualKnown(EAIndividual o)
	{
		if (o == null || generation == null || generation.get(0) == null)
		{
			return false;
		}

		for (EAIndividual eaIndividual : knownIndividuals)
		{
			if (eaIndividual.getData().isEqual(o.getData()))
			{
				return true;
			}
		}

		return false;
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

	public int getMaxGenerationCount()
	{
		return maxGenerationCount;
	}

	public int getGenerationCount()
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
