package core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import algorithm.EACore;
import algorithm.EAIndividual;
import algorithm.Heueristics;
import io.ImgLoader;
import io.ImgWriter;
import io.XMLReport;
import io.XMLReportGenerationEntry;

public class Program
{
	// Variables
	public final static Logger	LOGGER					= Logger.getLogger("sprite-evolution logger", null);

	private boolean				isInitializedProperly	= false;

	private Options				options					= null;

	private File				sourceDirectory			= null;
	private File				destinationDirectory	= null;
	private static File			optimizerPath			= null;
	private static File			workingDirectory		= null;
	private static File			reportPath				= null;
	private static File			initialSetPath			= null;

	private XMLReport			report					= null;

	private static int			columns					= 0;
	private static int			generationSize			= 0;
	private static int			maxGenerationCount		= 0;

	private static double		mutationRate			= 0.0;
	private static double		selectionRate			= 0.0;
	private static double		mutationChance			= 0.0;
	private static double		macroMutationChance		= 0.0;
	private static double		macroMutationRate		= 0.0;

	private static boolean		randomize				= false;
	private static boolean		createReport			= false;
	private static boolean		mindStagnation			= false;
	private static boolean 		writeEveryNewBest		= false;

	private File[]				sourceFiles				= null;
	private Image[]				sourceImages			= null;

	// Constructors
	public Program(String[] args)
	{
		initialize(args);
	}

	// Methods
	private void initialize(String[] args)
	{
		// Very early logging configuration
		LOGGER.setUseParentHandlers(false);

		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new BasicFormatter());

		LOGGER.addHandler(ch);

		// Parse console arguments
		createOptions();
		isInitializedProperly = parseArgs(args);

		// initialize report when set
		if (createReport)
		{
			report = new XMLReport(reportPath);
		}

		/*
		 * Load images
		 */
		try
		{
			sourceImages = ImgLoader.loadImageSet(sourceFiles);
		}
		catch (Exception e)
		{
			isInitializedProperly = false;
		}

		if (isInitializedProperly)
		{
			LOGGER.info("The program has been successfully initialized.");
		}
		else
		{
			LOGGER.warning("The program has not been successfully initialized.");
		}
	}

	public int run()
	{
		if (!isInitializedProperly)
		{
			return 1;
		}

		// Create starting set
		ImageSet2D startingSet = null;

		if (initialSetPath != null)
		{
			int[][] positions = ImageSet2D.parsePositions(ImgLoader.readTextFile(initialSetPath));

			if (positions != null)
			{
				startingSet = new ImageSet2D(sourceImages, positions);
			}
			else
			{
				return 1;
			}
		}
		else
		{
			startingSet = new ImageSet2D(sourceImages, columns);
		}

		// Randomize starting set if option is set
		if (randomize)
		{
			Random rand = new Random(Calendar.getInstance().getTimeInMillis());

			for (int i = 0; i < 1000000; i++)
			{
				int ax = 0;
				int ay = 0;
				int bx = 0;
				int by = 0;

				while (ax == bx || ay == by)
				{
					ax = rand.nextInt(startingSet.getWidth());
					ay = rand.nextInt(startingSet.getHeight());
					bx = rand.nextInt(startingSet.getWidth());
					by = rand.nextInt(startingSet.getHeight());
				}

				startingSet.swapImage(ax, ay, bx, by);
			}
		}

		// Write starting set
		ImgWriter.writeOptimized(startingSet.getFullImage(), new File(destinationDirectory.getAbsolutePath() + "/startingSet.png"), 4);

		// Write starting set positions
		ImgWriter.writePositionsFile(startingSet, new File(destinationDirectory.getAbsolutePath() + "/startingSet.txt"));

		// Calculate starting set file size
		int startingSetFileSize = ImgLoader.getImageFileSize(startingSet.getFullImage());

		// Adding starting set to report
		if (createReport)
		{
			XMLReportGenerationEntry entry = new XMLReportGenerationEntry(0, generationSize, startingSetFileSize, 0);

			report.addEntry(entry);
		}

		// Log configuration
		LOGGER.info("Starting EA with: " + String.format(
				"MaxGenerationCount: %04d GenerationSize: %04d MutationChance: %02.2f MutationRate: %02.2f MacroMutationChance: %02.2f MacroMutationRate: %02.2f SelectionRate: %02.2f Randomize: %b "
						+ "CreateReport: %b MindStagnation: %b",
				maxGenerationCount, generationSize, mutationChance, mutationRate, macroMutationChance, macroMutationRate, selectionRate, randomize, createReport, mindStagnation));

		// Log starting set size
		LOGGER.info("Size of starting set: " + String.format("%08d", startingSetFileSize));

		EACore core = new EACore(generationSize, maxGenerationCount, mutationRate, mutationChance, selectionRate, macroMutationChance, macroMutationRate, new EAIndividual(startingSet));

		// Setting stagnation parameters
		int stagnationAfter = (int) (maxGenerationCount * 0.1 + 0.5); // It stagnates, when not increased in 10% of the
																		// maxGenerationCount. At least 10

		if (stagnationAfter < 10)
		{
			stagnationAfter = 10;
		}

		int generationsWithoutIncrease = 0;
		int lastGenerationsBestFileSize = startingSetFileSize;
		int lastTotalBestFileSize = startingSetFileSize;
		int bestIndividualFileSize = 0;

		boolean shouldContinue = true;

		while (shouldContinue)
		{
			int genCount = core.getGenerationCount(); // Current generations count

			boolean newBestFound = core.evaluate(); // Evaluate and determine if a new best individual was found

			bestIndividualFileSize = core.getGeneration()[0].getFileSize();

			int fileSizeIncrease = lastGenerationsBestFileSize - bestIndividualFileSize;

			if (newBestFound)
			{
				int totalFileSizeIncrease = lastTotalBestFileSize - bestIndividualFileSize;

				if (writeEveryNewBest)
				{
					ImgWriter.writeOptimized(core.getGeneration()[0].getData().getFullImage(),
							new File(destinationDirectory.getAbsolutePath() + String.format("\\Generation %04d - NewBest.png", genCount)), 4);
				}
				
				LOGGER.info(String.format("Generation: %04d | Best Indiv. of Gen.: %08d Increase: %08d | Total best: %08d Increase: %08d NEW BEST", genCount, bestIndividualFileSize,
						fileSizeIncrease, bestIndividualFileSize, totalFileSizeIncrease));

				lastTotalBestFileSize = bestIndividualFileSize;
				generationsWithoutIncrease = 0; // Reset
			}
			else
			{
				LOGGER.info(String.format("Generation: %04d | Best Indiv. of Gen.: %08d Increase: %08d | Total best: %08d", genCount, bestIndividualFileSize, fileSizeIncrease,
						lastTotalBestFileSize));

				generationsWithoutIncrease++; // Increment
			}

			// Add current generation to report
			if (createReport)
			{
				XMLReportGenerationEntry entry = new XMLReportGenerationEntry(genCount, generationSize, bestIndividualFileSize, fileSizeIncrease);

				report.addEntry(entry);
			}

			// EAIndividual[] gen = core.getGeneration();
			// for (int i = 0; i < gen.length; i++)
			// {
			// ImgWriter.writeOptimized(gen[i].getData().getFullImage(),
			// new File(destinationDirectory.getAbsolutePath() + String.format("\\Generation %04d %04d.png", genCount, i)), 2);
			// }

			core.select();
			core.nextGenerationFromLast();

			if (genCount >= maxGenerationCount)
			{
				shouldContinue = false;
				LOGGER.info(String.format("Maximum generation count of %04d reached. Stopping algorithm.", maxGenerationCount));
			}

			if (generationsWithoutIncrease >= stagnationAfter && mindStagnation)
			{
				shouldContinue = false;
				LOGGER.info("Stagnation detected. Stopping algorithm.");
			}

			lastGenerationsBestFileSize = bestIndividualFileSize;
		}

		double decrease = 100 * ((1.0 / startingSetFileSize) * (startingSetFileSize - lastGenerationsBestFileSize));

		LOGGER.info(String.format("Algorithm stopped. Start: %08d End: %08d Difference: %08d Percentage: %03.2f%%", startingSetFileSize, lastTotalBestFileSize,
				startingSetFileSize - lastTotalBestFileSize, decrease));

		LOGGER.info("Writing result files.");

		ImgWriter.writeOptimized(core.getBestIndividual().getData().getFullImage(), new File(destinationDirectory.getAbsolutePath() + "\\sprite-evolution.png"), 4);
		ImgWriter.writeCSSFile(core.getBestIndividual(), new File(destinationDirectory, "sprite-evolution.css"));
		report.generateReport();

		LOGGER.info("Program finished. Terminating.");

		return 0;
	}

	private boolean parseArgs(String[] args)
	{
		/*
		 * Parse command-line arguments
		 */

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try
		{
			cmd = parser.parse(options, args);
		}
		catch (org.apache.commons.cli.ParseException e)
		{
			String header = "This program can be used to create css-sprite-sheets. Have fun!";
			String footer = "Source Code available at https://github.com/Neasu/sprite-evolution. Created by Kevin Sieverding.";

			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar sprite-evolution.jar", header, options, footer, true);
			return false;
		}

		/*
		 * Initializing the logger
		 */

		String loglevel = cmd.getOptionValue("ll");

		switch (loglevel == null ? "info" : loglevel)
		{
			case "off":
			{
				LOGGER.setLevel(Level.OFF);
			}
				break;
			case "severe":
			{
				LOGGER.setLevel(Level.SEVERE);
			}
				break;
			case "warning":
			{
				LOGGER.setLevel(Level.WARNING);
			}
				break;
			case "info":
			{
				LOGGER.setLevel(Level.INFO);
			}
				break;
			case "fine":
			{
				LOGGER.setLevel(Level.FINE);
			}
				break;
			// case "finer":
			// {
			// LOGGER.setLevel(Level.FINER);
			// }
			// break;
			// case "finest":
			// {
			// LOGGER.setLevel(Level.FINEST);
			// }
			// break;
			default:
			{
				LOGGER.setLevel(Level.INFO);
			}
				break;
		}

		if (LOGGER.getLevel() != Level.OFF)
		{
			File tempLogFile = new File((new SimpleDateFormat("yy-MM-dd  HH-mm-ss").format(Calendar.getInstance().getTime()) + " sprite-evolution_log.txt").replace(' ', '_'));
			if (cmd.hasOption("lfp"))
			{
				File tempfile = new File(cmd.getOptionValue("lfp"));

				if (!tempfile.isDirectory())
				{
					tempLogFile = tempfile;
				}
				else
				{
					tempLogFile = new File(tempfile, tempLogFile.getName());
				}
			}
			try
			{
				FileHandler fileHandler = new FileHandler(tempLogFile.getAbsolutePath());
				fileHandler.setFormatter(new BasicFormatter());
				LOGGER.addHandler(fileHandler);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
				return false;
			}
		}

		/*
		 * Validating source folder argument
		 */

		try
		{
			File tempsource = new File(cmd.getOptionValue("s"));

			if (tempsource.isFile())
			{
				throw new Exception();
			}

			sourceDirectory = tempsource;
		}
		catch (Exception e)
		{
			LOGGER.severe("The source path is invalid!");
		}

		/*
		 * Scanning source directory
		 */

		try
		{
			sourceFiles = ImgLoader.scanDirectory(sourceDirectory, "*.png");
		}
		catch (Exception e)
		{
			LOGGER.severe("Scanning directory failed. Exiting.");
			return false;
		}

		if (sourceFiles.length > 0)
		{
			LOGGER.info("Source directory has been scanned successfully. " + sourceFiles.length + " files found.");
		}
		else
		{
			LOGGER.severe("No files found!");
			return false;
		}

		/*
		 * Validating destination folder argument
		 */

		try
		{
			File tempdestination = new File(cmd.getOptionValue("d"));

			if (tempdestination.isFile())
			{
				throw new Exception();
			}

			destinationDirectory = tempdestination;

			if (!destinationDirectory.exists())
			{
				destinationDirectory.mkdirs();
			}
		}
		catch (Exception e)
		{
			LOGGER.severe("The destination path is invalid!");
		}

		/*
		 * Validating column count OPTION: c
		 */
		if (cmd.hasOption("c"))
		{
			try
			{
				Integer.parseInt(cmd.getOptionValue("c"));
			}
			catch (Exception e)
			{
				LOGGER.warning("Invalid value for column count (Argument: -c). Maybe not a number?");
			}
		}

		if (columns <= 0 || columns > sourceFiles.length)
		{
			columns = Heueristics.getColumns(sourceFiles.length);
		}

		/*
		 * Setting working directory
		 */

		String tempPath = System.getProperty("java.io.tmpdir");

		if (tempPath.charAt(tempPath.length() - 1) != '\\')
		{
			tempPath += "\\";
		}

		tempPath += "sprite-evolution";

		workingDirectory = new File(tempPath);

		if (cmd.hasOption("wd"))
		{
			try
			{
				File wdir = new File(cmd.getOptionValue("wd"));

				if (wdir.isFile())
				{
					throw new Exception();
				}
				else
				{
					workingDirectory = wdir;
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Working directory path is invalid! Using systems temp folder instead.");
			}

		}

		workingDirectory.mkdir();

		/*
		 * OptimizerPath
		 */

		optimizerPath = new File("optipng.exe");

		if (cmd.hasOption("op"))
		{
			try
			{
				File dir = new File(cmd.getOptionValue("op"));

				if (!dir.isFile())
				{
					throw new Exception();
				}
				else
				{
					optimizerPath = dir;
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Optimizer path is invalid.");
				return false;
			}

		}

		/*
		 * Validating mutation rate
		 */
		mutationRate = 0.2;

		if (cmd.hasOption("mutrat"))
		{
			try
			{
				mutationRate = Double.parseDouble(cmd.getOptionValue("mutrat"));

				if (mutationRate <= 0.0 || mutationRate > 1.0)
				{
					mutationRate = 0.2;
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("MutationRate is invalid. Using standard of 0.2");
			}
		}

		/*
		 * Validating selectionRate
		 */
		selectionRate = 0.2;

		if (cmd.hasOption("selr"))
		{
			try
			{
				selectionRate = Double.parseDouble(cmd.getOptionValue("selr"));

				if (selectionRate <= 0.0 || selectionRate > 1.0)
				{
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("SelectionRate is invalid. Using standard of 0.2");
			}
		}

		/*
		 * Validating generation size
		 */
		generationSize = sourceFiles.length <= 100 ? sourceFiles.length : 100;

		if (cmd.hasOption("gens"))
		{
			try
			{
				generationSize = Integer.parseInt(cmd.getOptionValue("gens"));

				if (generationSize <= 0)
				{
					generationSize = 100;
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Generation size is invalid. Using standard.");
			}
		}

		/*
		 * Validating max generation count
		 */

		maxGenerationCount = 256;

		if (cmd.hasOption("mgc"))
		{
			try
			{
				maxGenerationCount = Integer.parseInt(cmd.getOptionValue("mgc"));

				if (maxGenerationCount <= 0)
				{
					maxGenerationCount = 256;
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Maximum generation count is invalid! Standard is used!");
			}
		}

		/**
		 * Validating mutationChance
		 */
		mutationChance = 0.3;

		if (cmd.hasOption("mutcha"))
		{
			try
			{
				mutationChance = Double.parseDouble(cmd.getOptionValue("mutcha"));

				if (mutationChance <= 0.0 || mutationChance > 1.0)
				{
					mutationChance = 0.3;
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("MutationChance is invalid. Using standard of 0.01");
			}
		}

		/**
		 * Validating macroMutationChance
		 */
		macroMutationChance = 0.01;

		if (cmd.hasOption("macmutcha"))
		{
			try
			{
				macroMutationChance = Double.parseDouble(cmd.getOptionValue("macmutcha"));

				if (macroMutationChance <= 0.0 || macroMutationChance > 1.0)
				{
					macroMutationChance = 0.01;
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("MacroMutationChance is invalid. Using standard of 0.01");
			}
		}

		/**
		 * Validating macroMutationRate
		 */
		macroMutationRate = 0.1;

		if (cmd.hasOption("macmutcha"))
		{
			try
			{
				macroMutationRate = Double.parseDouble(cmd.getOptionValue("macmutrat"));

				if (macroMutationRate <= 0.0 || macroMutationRate > 1.0)
				{
					macroMutationRate = 0.1;
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("MacroMutationRate is invalid. Using standard of 0.01");
			}
		}

		/*
		 * Validating randomize option
		 */
		if (cmd.hasOption("r"))
		{
			randomize = true;
		}

		/**
		 * Reading report creation
		 */

		if (cmd.hasOption("createreport"))
		{
			createReport = true;
		}

		/*
		 * Reading report path
		 */
		String name = (new SimpleDateFormat("yy-MM-dd  HH-mm-ss").format(Calendar.getInstance().getTime()) + " sprite-evolution report.xml").replace(' ', '_');
		reportPath = new File(destinationDirectory, name);

		if (cmd.hasOption("reportpath"))
		{
			try
			{
				reportPath = new File(cmd.getOptionValue("reportpath"));

				if (reportPath.isDirectory())
				{
					reportPath = new File(reportPath, name);
				}

			}
			catch (Exception e)
			{
				LOGGER.warning("Report path is invalid!");
			}

			if (!createReport)
			{
				LOGGER.warning("Report path has been set but not --createreport! No report will be created!");
			}
		}

		/**
		 * Reading mind stagnation
		 */
		if (cmd.hasOption("mindstagnation"))
		{
			mindStagnation = true;
		}

		/**
		 * Reading initialset
		 */
		if (cmd.hasOption("initialset"))
		{
			try
			{
				initialSetPath = new File(cmd.getOptionValue("initialset"));

				if (initialSetPath.isDirectory() || !initialSetPath.exists())
				{
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("The path to the initial set is invalid!");
			}
		}
		
		/*
		 * Reading writeEveryNewBest
		 */
		if(cmd.hasOption("writeeverynewbest"))
		{
			writeEveryNewBest = true;
		}

		return true;
	}

	private void createOptions() // Create the list of available command-line options
	{
		options = new Options();

		options.addOption(Option.builder("s").required().longOpt("source").desc("The Source Directory.").argName("SourceDirectory").hasArg().build());

		options.addOption(Option.builder("d").required().longOpt("destination").desc("The destination directory.").argName("DestinationDirectory").hasArg().build());

		options.addOption(Option.builder("wd").longOpt("workingdir").desc("The directory to save temporary files to. Standard: The systems temp directory.").argName("WorkingDirectory")
				.hasArg().build());

		options.addOption(Option.builder("op").longOpt("optimizerpath").desc("The path to the optipng.exe").argName("OptimizerPath").hasArg().build());

		options.addOption(
				Option.builder("ll").longOpt("loglevel").desc("The loglevel to output. Possible: off, severe, warning, info, all Standard: all").argName("LogLevel").hasArg().build());

		options.addOption(Option.builder("lfp").longOpt("logfilepath")
				.desc("The path to save the logfile to. Can be full filepath or folderpath if you want to use the standard filename. If not given, no logfile will be created")
				.argName("LogFilePath").hasArg().build());

		options.addOption(
				Option.builder("c").longOpt("columns").desc("The number of columns").desc("The number of columns the resulting sprite should have.").argName("Columns").hasArg().build());

		options.addOption(Option.builder("mutrat").longOpt("mutationrate").desc("The rate of mutation from generation to generation. Float > 0.0 & <= 1.0. Standard: 0.2")
				.argName("MutationRate").hasArg().build());

		options.addOption(Option.builder("selr").longOpt("selectionrate").desc("The rate of selection from generation to generation. Float > 0.0 & <= 1.0. Standard: 0.2")
				.argName("SelectionRate").hasArg().build());

		options.addOption(Option.builder("gens").longOpt("generationsize").desc("The size of a generation. Standard: Number of input images if not bigger than 100.").argName("GenerationSize")
				.hasArg().build());

		options.addOption(Option.builder("mgc").longOpt("maxgenerationcount").desc("The when this number of generations is reached the algorithem will terminate. Standard: 256")
				.argName("MaxGenerationCount").hasArg().build());

		options.addOption(Option.builder("mutcha").longOpt("mutationchance").desc("The chance of an individual to mutate. Standard: 0.3").hasArg().argName("MutationChance").build());

		options.addOption(
				Option.builder("macmutcha").longOpt("macromutationchance").desc("The chance of an individual to macro mutate. Standard: 0.01").hasArg().argName("MacroMutationChance").build());

		options.addOption(Option.builder("macmutrat").longOpt("macromutationrate").desc("The rate of a macro mutation from generation to generation. Standard: 0.1").hasArg()
				.argName("MacroMutationRate").build());

		options.addOption(Option.builder("r").longOpt("randomize").desc("Determines if the input should be randomized before the algorithm starts.").argName("Randomize").build());

		options.addOption(Option.builder().longOpt("createreport").desc("Determines if a xml report is generated.").argName("CreateReport").build());

		options.addOption(Option.builder().longOpt("reportpath").desc("The path to the directory where the report file should be saved.").argName("ReportPath").hasArg().build());

		options.addOption(Option.builder().longOpt("mindstagnation").desc("Determines if the algorithm should be stopped before reaching the max generation count, when stagnation is detected")
				.argName("MindStagnation").build());

		options.addOption(Option.builder().longOpt("initialset").desc("The Path to a text file with position data matching the source pictures, to use as the starting point.")
				.argName("IntitialSet").hasArg().build());

		options.addOption(Option.builder().longOpt("writeeverynewbest").desc("Determines if every time the algorithm finds a new best individual, it is written to the output folder.")
				.argName("WriteEveryNewBest").build());

	}

	public static File getWorkingDirectory()
	{
		return workingDirectory;
	}

	public static int getColumns()
	{
		return columns;
	}

	public static double getMutationRate()
	{
		return mutationRate;
	}

	public static double getSelectionRate()
	{
		return selectionRate;
	}

	public static int getGenerationSize()
	{
		return generationSize;
	}

	public static File getOptimizerPath()
	{
		return optimizerPath;
	}

}
