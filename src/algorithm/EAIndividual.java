package algorithm;

import core.ImageSet2D;

public class EAIndividual
{
	// Variables
	private ImageSet2D data = null;
	private int fileSize = 0;

	// Constructors
	public EAIndividual(ImageSet2D data)
	{
		this.data = data;
	}

	// Methods
	public ImageSet2D getData()
	{
		return data;
	}
	
	public EAIndividual getCopy()
	{
		return new EAIndividual(new ImageSet2D(data.getImages(), data.getColumns()));
	}
	
	public void setFileSize(int quality)
	{
		this.fileSize = quality;
	}
	
	public int getFileSize()
	{
		return fileSize;
	}
}
