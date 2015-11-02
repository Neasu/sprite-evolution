package core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

public class Program
{
	// Variables
	public final static Logger	LOGGER					= Logger.getLogger("sprite-evolution logger", null);

	private boolean				isInitializedProperly	= false;

	private Options				options					= null;

	private File				sourceDirectory			= null;
	private File				destinationDirectory	= null;
	private static File			optimizerPath			= new File("E:\\Desktop\\sprite-evolution test outputs\\utils\\optipng.exe");	// TODO
	private static File			workingDirectory		= null;
	private static int			columns					= 0;
	private static double		mutationRate			= 0.0;
	private static double		selectionRate			= 0.0;
	private static int			generationSize			= 0;
	private static int			maxGenerationCount		= 0;

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
		parseArgs(args);

		/*
		 * Load images
		 */
		try
		{
			sourceImages = ImgLoader.loadImageSet(sourceFiles);
		}
		catch (Exception e)
		{
			// TODO
		}

		isInitializedProperly = true;
		LOGGER.info("The program has been successfully initialized.");
	}

	public int run()
	{
		if (!isInitializedProperly)
		{
			return 1;
		}

		boolean shouldContinue = true;

		ImageSet2D startingSet = new ImageSet2D(sourceImages, columns);

		ImgWriter.write(startingSet.getFullImage(), new File(destinationDirectory.getAbsolutePath() + "\\startingSet.png"));

		EACore core = new EACore(generationSize, maxGenerationCount, mutationRate, selectionRate, new EAIndividual(startingSet));

		do
		{
			int genCount = core.getGenerationCount();

			if (core.evaluate()) // Evaluate / If a better individual, than the current best one, is found
			{
				ImgWriter.write(core.getGeneration()[0].getData().getFullImage(),
						new File(destinationDirectory.getAbsolutePath() + String.format("\\Generation %04d - NewBest.png", genCount)));

				LOGGER.info(String.format("Generation: %04d | Best Indiv. of Gen.: %08d | Total best: %08d NEW BEST", genCount, core.getGeneration()[0].getFileSize(),
						core.getBestIndividual().getFileSize()));
			}
			else
			{
				LOGGER.info(String.format("Generation: %04d | Best Indiv. of Gen.: %08d | Total best: %08d", genCount, core.getGeneration()[0].getFileSize(),
						core.getBestIndividual().getFileSize()));
			}

			core.select();
			core.nextGenerationFromLast();

			if (genCount >= maxGenerationCount)
			{
				shouldContinue = false;
			}
		}
		while (shouldContinue);

		ImgWriter.write(core.getBestIndividual().getData().getFullImage(), new File(destinationDirectory.getAbsolutePath() + "\\sprite-evolution.png"));
		ImgWriter.writeCSSFile(core.getBestIndividual(), new File(destinationDirectory, "sprite-evolution.css"));

		return 0;
	}

	private void parseArgs(String[] args)
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
			return;
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
				return;
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
			return;
		}

		LOGGER.info("Source directory has been scanned successfully. " + sourceFiles.length + " files found.");

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
		 * Validating mutation rate
		 */
		mutationRate = 0.2;

		if (cmd.hasOption("mutr"))
		{
			try
			{
				mutationRate = Double.parseDouble(cmd.getOptionValue("mutr"));

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

		maxGenerationCount = 500;

		if (cmd.hasOption("mgc"))
		{
			try
			{
				maxGenerationCount = Integer.parseInt(cmd.getOptionValue("mgc"));

				if (maxGenerationCount <= 0)
				{
					maxGenerationCount = 500;
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
	}

	private void createOptions() // Create the list of available command-line options
	{
		options = new Options();

		options.addOption(Option.builder("s").required().longOpt("source").desc("The Source Directory.").argName("SourceDirectory").hasArg().build());

		options.addOption(Option.builder("d").required().longOpt("destination").desc("The destination directory.").argName("DestinationDirectory").hasArg().build());

		options.addOption(Option.builder("wd").longOpt("workingdir").desc("The directory to save temporary files to. Standard: The systems temp directory.").argName("WorkingDirectory")
				.hasArg().build());

		options.addOption(
				Option.builder("ll").longOpt("loglevel").desc("The loglevel to output. Possible: off, severe, warning, info, all Standard: all").argName("LogLevel").hasArg().build());

		options.addOption(Option.builder("lfp").longOpt("logfilepath")
				.desc("The path to save the logfile to. Can be full filepath or folderpath if you want to use the standard filename. If not given, no logfile will be created")
				.argName("LogFilePath").hasArg().build());

		options.addOption(
				Option.builder("c").longOpt("columns").desc("The number of columns").desc("The number of columns the resulting sprite should have.").argName("Columns").hasArg().build());

		options.addOption(Option.builder("mutr").longOpt("mutationrate").desc("The rate of mutation from generation to generation. Float > 0.0 & <= 1.0. Standard: 0.2").argName("MutationRate")
				.hasArg().build());

		options.addOption(Option.builder("selr").longOpt("selectionrate").desc("The rate of selection from generation to generation. Float > 0.0 & <= 1.0. Standard: 0.2")
				.argName("SelectionRate").hasArg().build());

		options.addOption(Option.builder("gens").longOpt("generationsize").desc("The size of a generation. Standard: Number of input images if not bigger than 100.").argName("GenerationSize")
				.hasArg().build());

		options.addOption(Option.builder("mgc").longOpt("maxgenerationcount").desc("The when thios number of generations is reached the algorithem will terminate. Standard: 500")
				.argName("MaxGenerationCount").hasArg().build());

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
