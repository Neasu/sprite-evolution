package core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import algorithm.Heueristics;
import io.ImgLoader;

public class Program
{
	// Variables
	public final static Logger LOGGER 		= Logger.getLogger("sprite-evolution logger", null);
	
	private boolean isInitializedProperly 	= false;
	
	Options options 						= null;
	
	File	sourceDirectory					= null;
	File	destinationDirectory			= null;
	int 	columns							= 0;
	
	File[] sourceFiles 						= null;
	Image[] sourceImages					= null;

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

	private void createOptions() // Create the list of available command-line options
	{
		options = new Options();

		options.addOption(Option.builder("s")
				.required()
				.longOpt("source")
				.desc("The Source Directory.")
				.argName("SourceDirectory")
				.hasArg()
				.build());

		options.addOption(Option.builder("d")
				.required().
				longOpt("destination")
				.desc("The destination directory.")
				.argName("DestinationDirectory")
				.hasArg()
				.build());

		options.addOption(Option.builder("ll")
				.longOpt("loglevel")
				.desc("The loglevel to output. Possible: off, severe, warning, info, all Standard: all")
				.argName("LogLevel")
				.hasArg()
				.build());

		options.addOption(Option.builder("lfp")
				.longOpt("logfilepath")
				.desc("The path to save the logfile to. Can be full filepath or folderpath if you want to use the standard filename.")
				.argName("LogFilePath")
				.hasArg()
				.build());
		
		options.addOption(Option.builder("c")
				.longOpt("columns")
				.desc("The number of columns")
				.desc("The number of columns the resulting sprite should have.")
				.argName("Columns")
				.hasArg()
				.build());
	}

	private void parseArgs(String[] args)
	{
		// TODO
		if (args.length == 0)
		{
			// TODO only for testing
			args = new String[6];

			args[0] = "--source";
			args[1] = "E:\\Desktop\\testicons";

			args[2] = "--destination";
			args[3] = "E:\\Desktop";

			args[4] = "--logfilepath";
			args[5] = "E:\\Desktop\\abc.txt";
		}

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

		switch (loglevel == null ? "all" : loglevel)
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
			default:
			{
				LOGGER.setLevel(Level.ALL);
			}
				break;
		}

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
		 * Validating column count
		 */
		if(cmd.hasOption("c"))
		{
			try
			{
				Integer.parseInt(cmd.getOptionValue("c"));
			}
			catch(Exception e) 
			{
				LOGGER.warning("Invalid value for column count (Argument: -c). Maybe not a number?");
			}
		}
		
		if(columns == 0 || columns > sourceFiles.length)
		{
			columns = Heueristics.getColumns(sourceFiles.length);
		}

	}

	public int run()
	{
		if (!isInitializedProperly)
		{
			return 1;
		}

		return 0;
	}

}
