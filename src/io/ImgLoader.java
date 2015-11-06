package io;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import core.Image;
import core.Program;

public class ImgLoader
{
	// Variables
	// Constructors
	// Methods

	/**
	 * Loads a set of images
	 * 
	 * @param paths
	 *            The paths to the image files
	 * @return All loaded images
	 * @throws Exception
	 *             When an IOException occurred or images with different resolutions were detected
	 */
	public static Image[] loadImageSet(File[] paths) throws Exception
	{
		Program.LOGGER.info("Loading " + paths.length + " images");

		Image[] imgs = new Image[paths.length];

		try
		{
			for (int i = 0; i < paths.length; i++)
			{
				imgs[i] = loadImage(paths[i]);

				if (i > 0 && (imgs[i].getImage().getHeight() != imgs[i - 1].getImage().getHeight() || imgs[i].getImage().getWidth() != imgs[i - 1].getImage().getWidth()))
				{
					throw new Exception("Images with different resolutions detected.");
				}
			}
		}
		catch (Exception e)
		{
			Program.LOGGER.severe("The loading failed due to: " + e.getMessage());
			throw e;
		}

		Program.LOGGER.info("The loading of " + paths.length + " images has been successfull");

		return imgs;
	}

	public static Image loadImage(File path) throws IOException
	{
		Image img = null;

		try
		{
			img = new Image(ImageIO.read(path), path.getName());
		}
		catch (IOException ioe)
		{
			Program.LOGGER.warning("Image couldn't been loaded: " + path.getName() + " due to " + ioe.getMessage());
			throw ioe;
		}

		Program.LOGGER.fine("Image loaded: " + path.getName());

		return img;
	}

	public static File[] scanDirectory(File dir, String mask) throws Exception
	{
		ArrayList<File> foundFiles = new ArrayList<File>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(), mask))
		{
			for (Path entry : stream)
			{
				foundFiles.add(entry.toFile());
			}
		}
		catch (IOException e)
		{
			Program.LOGGER.severe("Could not scan directory due to: " + e.getMessage());
			throw e;
		}

		File[] files = new File[foundFiles.size()];
		foundFiles.toArray(files);

		return files;
	}

	public static int getImageFileSizeNonOptimized(BufferedImage image)
	{
		BufferedImage img = image;
		int size = 0;

		try (ByteArrayOutputStream tmp = new ByteArrayOutputStream())
		{
			ImageIO.write(img, "png", tmp);
			size = tmp.size();
		}
		catch (Exception e)
		{
			// TODO
		}

		return size;
	}

	public static int getImageFileSize(BufferedImage image)
	{
		File tempFile = new File(Program.getWorkingDirectory(), "a.png");
		int size = 0;
		try
		{
			ImgWriter.writeOptimized(image, tempFile);

			size = (int) tempFile.length();
		}
		catch (Exception e)
		{
			Program.LOGGER.warning("Calculating of optimized image file size failed due to: " + e.getMessage());
		}

		return size;
	}
	
	public static String readTextFile(File path)
	{
		String text = "";
		
		try
		{
			BufferedReader rd = Files.newBufferedReader(path.toPath());
			
			String line;
			
			while((line = rd.readLine()) != null)
			{
				text += line + "\n";
			}
		}
		catch (Exception e)
		{
			Program.LOGGER.warning("Could not read in text file: " + path.getAbsolutePath() + " ERROR: " + e.getMessage());
			return null;
		}
		
		return text;
	}
}
