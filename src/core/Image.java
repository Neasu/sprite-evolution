package core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import io.ImgLoader;

public class Image
{
	// Variables
	private BufferedImage image = null;
	
	// Constructors
	public Image(BufferedImage image)
	{
		initialize(image);
	}
	
	// Methods
	private void initialize(BufferedImage image)
	{
		this.image = image;
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
}
