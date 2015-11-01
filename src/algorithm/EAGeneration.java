package algorithm;

/*
 * NOT IN USE
 */

public class EAGeneration
{
	// Variables
	private EAIndividual[]	individuals			= null;
	private int				generationNumber	= 0;

	// Constructors
	public EAGeneration(int generationNumber, EAIndividual[] individuals)
	{
		initialize(generationNumber, individuals);
	}

	public EAGeneration(int generationNumber, int generationSize)
	{
		if (!(generationSize > 0 && generationSize > 0))
		{
			return;
		}

		EAIndividual[] individuals = new EAIndividual[generationSize];

		initialize(generationNumber, individuals);
	}

	// Methods
	private void initialize(int generationNumber, EAIndividual[] individuals)
	{
		this.generationNumber = generationNumber;
		this.individuals = individuals;
	}

	public EAIndividual[] getIndividuals()
	{
		return individuals;
	}

	public EAIndividual getIndividual(int index)
	{
		if (index > 0 && index < getGenerationSize())
		{
			return individuals[index];
		}

		return null;
	}

	public int getGenerationSize()
	{
		return individuals.length;
	}

	public int getGenerationNumber()
	{
		return generationNumber;
	}

}
