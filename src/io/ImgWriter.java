package io;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import algorithm.EAIndividual;
import core.Image;
import core.ImageSet2D;
import core.Program;

public class ImgWriter
{
	// Variables

	// Constructors

	// Methods
	public static void write(BufferedImage image, File path)
	{
		try
		{
			ImageIO.write(image, "png", path);
		}
		catch (Exception e)
		{
			Program.LOGGER.warning("Saving of image failed, Path: " + path.getAbsolutePath());
		}
	}

	public static void writeOptimized(BufferedImage image, File path)
	{
		writeOptimized(image, path, 2);
	}
	
	public static void writeOptimized(BufferedImage image, File path, int level)
	{
		if(level < 1 || level > 6)
		{
			level = 2;
		}
		
		File optimizerPath = Program.getOptimizerPath();

		try
		{
			ImageIO.write(image, "png", path);

			Process p = Runtime.getRuntime().exec(optimizerPath.getAbsolutePath() + " -o" + level + " " + path.getAbsolutePath());
			p.waitFor();

		}
		catch (Exception e)
		{
			Program.LOGGER.warning("Image could not be written optimized! File: " + path.getAbsolutePath() + " Error: " + e.getMessage());
		}
	}
	
	public static void writePositionsFile(ImageSet2D set, File destination)
	{
		String text = ImageSet2D.createPositionsString(set.getPositions());
		
		try
		{
			BufferedWriter w = Files.newBufferedWriter(destination.toPath(), Charset.defaultCharset());
			w.write(text);
			w.flush();
			w.close();
		}
		catch (IOException e)
		{
			Program.LOGGER.warning("Writing of positions file failed! ERROR: " + e.getMessage());
		}
	}

	public static void writeCSSFile(EAIndividual individual, File path)
	{
		if(individual == null || path == null || path.isDirectory())
		{
			return;
		}
		
		String text = "/* Coordinates are relative to the upper left corner! */\n";
		ImageSet2D set = individual.getData();

		for (int i = 0; i < set.getWidth(); i++)
		{
			for (int j = 0; j < set.getHeight(); j++)
			{
				Image tempImg = set.getPartImage(i, j);
				
				if(tempImg == null)
				{
					continue;
				}

				int edge = tempImg.getImage().getWidth();

				int x = edge * i;
				int y = edge * j;

				text += "." + tempImg.getName().replace(' ', '_') + "{background-position:" + (x == 0 ? "0 " : x + "px ") + (y == 0 ? "0 " : y + "px") + ";width:" + edge + ";height:" + edge + ";}\n";
			}
		}
		
		try
		{
			BufferedWriter w = Files.newBufferedWriter(path.toPath(), Charset.defaultCharset());
			w.write(text);
			w.flush();
			w.close();
		}
		catch (IOException e)
		{
			Program.LOGGER.warning("Writing of CSS file failed! ERROR: " + e.getMessage());
		}
	}
}
