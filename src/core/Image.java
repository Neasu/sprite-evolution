package core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class Image
{
	// Variables
	private BufferedImage image = null;
	private String name = "";

	// Constructors
	public Image(BufferedImage image)
	{
		initialize(image, "");
	}
	
	public Image(BufferedImage image, String name)
	{
		initialize(image, name);
	}

	// Methods
	private void initialize(BufferedImage image, String name)
	{
		this.image = image;
		this.name = name;
	}
	
	public BufferedImage getImage()
	{
		return image;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
