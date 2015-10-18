package core;

public class ImageSet2D
{
	// Variables
	private Image[][] set = null;
	
	// Constructors
	public ImageSet2D(Image[] images, int columns)
	{
		initialize(images, columns);
	}
	
	public ImageSet2D(Image[][] images)
	{
		initialize(images);
	}
	
	// Methods
	private void initialize(Image[] images, int columns)
	{
		Image[][] tempImages = new Image[(images.length % 2 == 0?(images.length / columns):(images.length / columns + 1))][columns];
		initialize(tempImages);
	}
	
	private void initialize(Image[][] images)
	{
		this.set = images.clone();
	}
	
	public void swapImage(int ax, int ay, int bx, int by)
	{
		if(!(ax == bx && ay == by) && ax >= 0 && ax <= set.length && ay >= 0 && ay <= set[0].length && bx >= 0 && bx <= set.length && by >= 0 && by <= set[0].length) // If not equal and in boundaries
		{
			Image buffer = getImage(ax, ay);
			setImage(ax, ay, getImage(bx, by));
			setImage(bx, by, buffer);
		}
		
		return;
	}
	
	public Image getImage(int x, int y)
	{
		return set[x][y];
	}
	
	private boolean setImage(int x, int y, Image image)
	{
		if(x >= 0 && x <= set.length && y >= 0 && y <= set[0].length)
		{
			set[x][y] = image;
			return true;
		}
		
		return false;
	}
}
