package core;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;
import com.sun.media.jfxmedia.logging.Logger;

public class ImageSet2D
{
	// Variables
	private Image[]	images		= null;
	private int[][]	positions	= null;

	// Constructors
	public ImageSet2D(Image[] images, int columns)
	{
		initialize(images, columns);
	}

	public ImageSet2D(Image[] images, int[][] positions)
	{
		initialize(images, positions);
	}

	// Methods
	private void initialize(Image[] images, int columns)
	{
		int imageCount = images.length;
		int rows = 0;
		
		if(imageCount % 2 == 0)
		{
			rows = (int) (imageCount / columns + 1 + 0.5);
		}
		else
		{
			rows = (int) (imageCount / columns + 1 + 0.5);
		}

		int[][] tempPositions = new int[rows][columns];

		// Fill positions with no image value -1
		for (int[] i : tempPositions)
		{
			Arrays.fill(i, -1);
		}

		for (int i = 0; i < imageCount; i++)
		{
			int x = i % columns;
			int y = (int) i / columns;

			tempPositions[y][x] = i;
		}
		
		initialize(images, tempPositions);
	}

	private void initialize(Image[] images, int[][] positions)
	{
		this.images = images;
		this.positions = positions.clone();
	}

	public void swapImage(int ax, int ay, int bx, int by)
	{
		if (!(ax == bx && ay == by) && ax >= 0 && ax <= getWidth() && ay >= 0 && ay <= getHeight() && bx >= 0 && bx <= getWidth() && by >= 0 && by <= getHeight()) // If
																																									// not
																																									// equal
																																									// and
																																									// in
																																									// boundaries
		{
			int buffer = getPartImageIndex(ax, ay);
			setImageIndex(ax, ay, getPartImageIndex(bx, by));
			setImageIndex(bx, by, buffer);
		}
		else
		{
			Program.LOGGER.warning("Swapping of images failed! A("+ax+";"+ay+") B("+bx+";"+by+")");
		}

		return;
	}
	
	public void swapRow(int ay, int by)
	{
		if((ay == by) || ay < 0 || ay >= getHeight() || by < 0 || by >= getHeight())
		{
			Program.LOGGER.warning("Swapping of rows failed! Row A: " + ay + " Row B: " + by);
		}
		else
		{
			for(int i = 0; i < getWidth(); i++)
			{
				swapImage(i, ay, i, by);
			}
		}
		
		return;
	}

	public BufferedImage getFullImage()
	{
		int imageWidth = images[0].getImage().getWidth(); // width and height of part images
		int columns = getWidth();
		int rows = getHeight();

		BufferedImage result = new BufferedImage(imageWidth * columns, imageWidth * rows, BufferedImage.TYPE_INT_ARGB);

		Graphics g = result.getGraphics();

		for (int x = 0; x < columns; x++)
		{
			for (int y = 0; y < rows; y++)
			{
				if (isImageSet(x, y))
				{
					g.drawImage(getPartImage(x, y).getImage(), x * imageWidth, y * imageWidth, null);
				}
			}
		}

		return result;
	}
	
	public boolean isEqual(ImageSet2D o)
	{	
		if(o == null || o.getWidth() != getWidth() || o.getHeight() != getHeight())
		{
			return false;
		}
		
		for(int i = 0; i < getWidth(); i++)
		{
			for(int j = 0; j < getHeight(); j++)
			{
				if(o.getPartImageIndex(i, j) != getPartImageIndex(i, j))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public int getPartImageIndex(int x, int y)
	{
		if (areCoordinatesValid(x, y))
		{
			return positions[y][x];
		}
		return 0;
	}

	public Image getPartImage(int x, int y)
	{
		if (isImageSet(x, y))
		{
			return images[positions[y][x]];
		}
		return null;
	}

	private boolean setImage(int x, int y, Image image)
	{
		if (areCoordinatesValid(x, y))
		{
			images[positions[y][x]] = image;
			return true;
		}

		return false;
	}
	
	private boolean setImageIndex(int x, int y, int index)
	{
		if (areCoordinatesValid(x, y) && index >= -1)
		{
			positions[y][x] = index;
			return true;
		}

		return false;
	}
	
	public boolean isImageSet(int x, int y)
	{
		if(areCoordinatesValid(x, y) && positions[y][x] != -1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean areCoordinatesValid(int x, int y)
	{
		if (x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public Image[] getImages()
	{
		return images;
	}
	
	public int[][] getPositions()
	{
		int[][] result = new int[positions.length][];
		
		for (int i = 0; i < positions.length; i++)
		{
			int[] column = positions[i];
			
			result[i] = Arrays.copyOf(column, column.length);
		}
		
		return result;
	}
	
	public int getImageCount()
	{
		return images.length;
	}
	
	public int getColumns()
	{
		return positions[0].length;
	}

	public int getWidth()
	{
		return positions[0].length;
	}

	public int getHeight()
	{
		return positions.length;
	}
}
