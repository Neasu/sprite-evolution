package core;

import java.io.File;

import com.sun.xml.internal.bind.v2.TODO;

import io.ImgLoader;
import sun.management.ExtendedPlatformComponent;

public class Program
{
	// Variables
	boolean isInit = false;
	

	File sourceDirectory = null;
	File destinationDirectory = null;
	
	File[] sourceFiles = null;
	
	// Constructors
	public Program(String[] args)
	{
		initialize(args);
	}
	
	// Methods
	private void initialize(String[] args)
	{
		parseArgs(args);
		
		// Scanning the Directory for PNG's
		try
		{
			sourceFiles = ImgLoader.scanDirectory(sourceDirectory, "*.png");
		}
		catch(Exception e) {}
		
		//TODO
		
		
			
		isInit = checkInitialization();
	}
	
	private void parseArgs(String[] args)
	{
		// TODO
		
		// Checking first argument: the source folder
		try
		{
			File tempsource = new File(args[0]);
			
			if(!tempsource.isDirectory())
			{
				throw new Exception();
			}
				
			sourceDirectory = tempsource;
		}
		catch (Exception e)
		{
			System.out.println("The source path is invalid");
		}
		
		// Checking the second argument: the destination folder
		try
		{
			File tempdestination = new File(args[1]);
			
			if(!tempdestination.isDirectory())
			{
				throw new Exception();
			}
		}
		catch(Exception e)
		{
			System.out.println("The destination path is invalid");
		}
		
	}
	
	private boolean checkInitialization()
	{
		try
		{
			
		}
		catch(Exception e)
		{
			System.out.println("The initialization is invalid due to: " + e.getMessage() + "\n Maybe check your arguments");
			return false;
		}
		
		return true; // TODO
	}
	
	public int run()
	{
		if(!isInit)
		{
			return 1;
		}
		
		
		return 0;
	}
	
}
