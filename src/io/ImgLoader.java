package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ImgLoader
{
	// Variables
	// Constructors
	// Methods
	public static File[] scanDirectory(File dir, String mask) throws Exception
	{
		ArrayList<File> foundFiles = new ArrayList<File>();
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(), mask))
		{
			for(Path entry : stream)
			{
				foundFiles.add(entry.toFile());
			}
		}
		catch(IOException e)
		{
			System.out.println("Could not load files due to: " + e.getMessage());
			throw new Exception();
		}
		
		return (File[]) foundFiles.toArray();
	}
}
