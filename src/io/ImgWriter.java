package io;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImgWriter
{
	// Variables
	
	// Constructors
	
	// Methods
	public static void saveImage(BufferedImage image, File path)
	{
		try
		{
			ImageIO.write(image, "png", path);
		}
		catch (Exception e)
		{
			// TODO handle io exception saving images
		}
	}
}
