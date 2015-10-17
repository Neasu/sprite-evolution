package io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ImgLoader
{
	// Variables
	// Constructors
	// Methods
	public static BufferedImage[] loadImageSet(File[] paths) throws IOException
	{
		System.out.println("Loading " + paths.length + "images");
		
		BufferedImage[] imgs = new BufferedImage[paths.length];
		
		try
		{
			for(int i = 0; i < paths.length; i++)
			{
				imgs[i] = loadImage(paths[i]);
			}
		}
		catch(IOException ioe)
		{
			throw ioe;
		}
		
		System.out.println("The loading of " + paths.length + " has been successfull");
		
		return imgs;
	}
	
	public static BufferedImage loadImage(File path) throws IOException
	{
		BufferedImage img = null;
		
		try
		{
			img = ImageIO.read(path);	
		}
		catch(IOException ioe)
		{
			// TODO
			System.out.println("Image couldn't been loaded: " + path.getName() + " due to " + ioe.getMessage());
			throw ioe;
		}
		
		System.out.println("Image loaded: " + path.getName());
		
		return img;
	}
	
	public static File[] scanDirectory(File dir, String mask) throws Exception
	{
		ArrayList<File> foundFiles = new ArrayList<File>();
		
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(), mask))
		{
			for(Path entry : stream)
			{
				foundFiles.add(entry.toFile());
			}
		}
		catch(IOException e)
		{
			System.out.println("Could not load files due to: " + e.getMessage());
			throw new IOException(e);
		}
		
		return (File[]) foundFiles.toArray();
	}
}
