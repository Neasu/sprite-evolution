package algorithm;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import com.sun.xml.internal.ws.assembler.jaxws.MustUnderstandTubeFactory;

import core.Program;
import io.ImgLoader;

public class EACore
{
	// Variables

	// Config
	private int						generationSize		= 0;
	private double					selectionRate		= 0.0;
	private int						generationCount		= 0;
	private int						maxGenerationCount	= 0;
	private double					mutationRate		= 0.0;
	private double					mutationChance		= 0.0;
	private double					macroMutationChance	= 0.0;
	private double					macroMutationRate	= 0.0;

	private ArrayList<EAIndividual>	generation			= null;

	private EAIndividual			individualZ			= null;
	private EAIndividual			bestIndividual		= null;

	private static Random			rand				= null;

	// Constructors
/**
 * 
 * @param generationSize
 * @param maxGenerationCount
 * @param mutationRate
 * @param muatationChance
 * @param selectionRate
 * @param macroMutationChance
 * @param macroMutationRate
 * @param individualZ
 */
	public EACore(int generationSize, int maxGenerationCount, double mutationRate, double muatationChance, double selectionRate, double macroMutationChance, double macroMutationRate,
			EAIndividual individualZ)
	{
		initialize(generationSize, selectionRate, maxGenerationCount, mutationRate, muatationChance, macroMutationChance, macroMutationRate, individualZ);
	}

	// Methods
	private void initialize(int generationSize, double selectionRate, int maxGenerationCount, double mutationRate, double mutationChance, double macroMutationChance, double macroMutationRate,
			EAIndividual individualZ)
	{
		this.generationSize = generationSize;
		this.selectionRate = selectionRate;
		this.individualZ = individualZ;
		this.maxGenerationCount = maxGenerationCount;
		this.mutationRate = mutationRate;
		this.mutationChance = mutationChance;
		this.macroMutationChance = macroMutationChance;
		this.macroMutationRate = macroMutationRate;

		rand = new Random(Calendar.getInstance().getTimeInMillis());

		// Initialize and evaluate

		// this.generation = new ArrayList<EAIndividual>();
		// this.generation.add(getIndividualZ());
		// getIndividualZ().setFileSize(ImgLoader.getImageFileSize(getIndividualZ().getData().getFullImage()));
		// this.bestIndividual = getIndividualZ();
		//
		this.bestIndividual = getIndividualZ();
		this.generation = new ArrayList<EAIndividual>(Arrays.asList(createGeneration(generationSize, mutationRate, mutationChance, individualZ))); // Generate
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
			}
			catch (Exception e)
			{
				// TODO
				e.printStackTrace();
			}
		}

		generation.sort(new EAIndividualComparator());

		EAIndividual top = generation.get(0);

		if (bestIndividual == null || bestIndividual.getFileSize() == 0 || bestIndividual.getFileSize() > top.getFileSize())
		{
			bestIndividual = top;
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
		int numberOfDeaths = (int) (generation.size() * selectionRate + 0.5);

		int size = generation.size();

		for (int i = 0; i < numberOfDeaths; i++)
		{
			generation.remove(size - i - 1);
		}
	}

	public void nextGenerationFromLast()
	{
		EAIndividual[] nextGen = createGeneration(generationSize, mutationRate, mutationChance, generation);
		generation = new ArrayList<EAIndividual>(Arrays.asList(nextGen));
		generationCount++;
	}

	public void nextGenerationFromBest()
	{
		generation = new ArrayList<EAIndividual>(Arrays.asList(createGeneration(generationSize, mutationRate, mutationChance, getBestIndividual())));
		generationCount++;
	}

	public EAIndividual mutate(EAIndividual individual)
	{
		return mutate(individual, mutationRate, mutationChance, macroMutationChance, macroMutationRate);
	}
	
	public EAIndividual mutate(EAIndividual individual, double mutationRate, double mutationChance, double macroMutationChance, double macroMutationRate)
	{
		EAIndividual result = individual.getCopy();
		int width = result.getData().getWidth();
		int height = result.getData().getHeight();
		
		// Normal mutation
		if (rand.nextInt(101) + 1 <= (100 * mutationChance))
		{
			int operationsToExecute = (int) ((((width * height) * 0.5) * mutationRate));

			if (operationsToExecute < 1)
			{
				operationsToExecute = 1;
			}
			
			int x1 = 0;
			int y1 = 0;

			int x2 = 0;
			int y2 = 0;

			for (int i = 0; i < operationsToExecute; i++)
			{
				while (x1 == x2)
				{
					x1 = EACore.rand.nextInt(width);
					x2 = EACore.rand.nextInt(width);
				}
				
				while (y1 == y2)
				{
					y1 = EACore.rand.nextInt(height);
					y2 = EACore.rand.nextInt(height);
				}

				result.getData().swapImage(x1, y1, x2, y2);
			}
		}
		
		// Macro mutation
		if(rand.nextInt(101) + 1 <= (100 * macroMutationChance))
		{
			int operationsToExecute = (int) ((((width * height) * (1.0 / (width * 2))) * macroMutationRate));

			if (operationsToExecute < 1)
			{
				operationsToExecute = 1;
			}
			
			int y1 = 0;
			int y2 = 0;
			
			for (int i = 0; i < operationsToExecute; i++)
			{
				while (y1 == y2)
				{
					y1 = EACore.rand.nextInt(height);
					y2 = EACore.rand.nextInt(height);
				}
				result.getData().swapRow(y1, y2);
			}
		}

		return result;
	}

	public EAIndividual[] createGeneration(int generationSize, double mutationRate, double mutationChance, EAIndividual individualZ)
	{
		if (generationSize < 0 || individualZ == null)
		{
			return null;
		}

		EAIndividual[] newIndividuals = new EAIndividual[generationSize];

		for (int i = 0; i < generationSize; i++)
		{
			newIndividuals[i] = mutate(individualZ); // Mutate the starting individual
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
	public EAIndividual[] createGeneration(int generationSize, double mutationRate, double mutationChance, ArrayList<EAIndividual> generation)
	{
		EAIndividual[] result = new EAIndividual[generationSize];
		int[] overallSlots = new int[generationSize];

		int oldGenSize = generation.size();

		if (oldGenSize <= 0)
		{
			result = createGeneration(generationSize, mutationRate, mutationChance, individualZ);
			Program.LOGGER.warning("Parent generation with null individuals! Generating new generation from starting set.");
		}
		if (oldGenSize < generationSize)
		{
			int overallFileSize = 0; // The quality of the whole generation (all qualities added up)

			// Calculate overall quality
			for (EAIndividual i : generation)
			{
				overallFileSize += i.getFileSize();
			}

			// Calculate slots
			for (int i = 0; i < oldGenSize; i++)
			{
				int slots = (int) (generationSize / oldGenSize + 0.5);

				if (overallFileSize > 0)
				{
					double qualityRelation = 1.0;
					qualityRelation = (1.0 / overallFileSize) * generation.get(i).getFileSize();

					double slotRelation = (1.0 / oldGenSize) * generationSize; // 1 + 1 - (1.0 / generationSize) *
																				// oldGenSize;
					slots = (int) ((generationSize * qualityRelation) * slotRelation + 0.5);
				}

				if (slots == 0)
				{
					slots = 1;
				}

				overallSlots[i] = slots;
			}

			// Check slots
			int slotsum = 0;

			for (int i : overallSlots)
			{
				slotsum += i;
			}

			if (slotsum < generationSize)
			{
				overallSlots[0] += (generationSize - slotsum);
			}

			int resultCursor = 0;
			for (int i = 0; i < oldGenSize; i++)
			{
				int j = 0;
				for (; j < overallSlots[i]; j++)
				{
					if (!(j + resultCursor < generationSize)) // Could get out of bounds otherwise
					{
						break;
					}

					result[j + resultCursor] = mutate(generation.get(i));
				}

				resultCursor += j; // Increase the position by 1 so the last children of this parent will not be overwritten by
									// the first children of the next parent
			}
		}
		else
		{
			for (int i = 0; i < generationSize; i++)
			{
				result[i] = mutate(generation.get(i));
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

	// Getters & Setters
	public EAIndividual getIndividualZ()
	{
		return individualZ;
	}

	public double getSelectionPercentage()
	{
		return selectionRate;
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
