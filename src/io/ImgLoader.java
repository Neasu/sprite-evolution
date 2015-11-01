package io;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	 * @param paths	The paths to the image files
	 * @return All loaded images
	 * @throws Exception When an IOException occurred or images with different resolutions were detected
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
			img = new Image(ImageIO.read(path));
		}
		catch (IOException ioe)
		{
			Program.LOGGER.warning("Image couldn't been loaded: " + path.getName() + " due to " + ioe.getMessage());
			throw ioe;
		}

		Program.LOGGER.fine("Image loaded: " + path.getName() + " File size: " + ImgLoader.getImageFileSize(img.getImage()));

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


	public static int getImageFileSize(BufferedImage image)
		{
			BufferedImage img = image;
			File workingDirectory = Program.getWorkingDirectory();
			File a = new File(workingDirectory, "a.png");
			File prog = new File("E:\\Desktop\\sprite-evolution test outputs\\utils");
			
		    int size = 0;
	
		    try
			{
				ImageIO.write(img, "png", a);
				
				//String[] cmd = {"E:\\Desktop\\sprite-evolution test outputs\\utils\\pngcrush_1_7_87_w64.exe", "-reduce", "-rem allb",  "-force " + a.getAbsolutePath() + " " + b.getAbsolutePath()};
				String cmd = "E:\\Desktop\\sprite-evolution test outputs\\utils\\optipng.exe -force " + a.getAbsolutePath();
				
				Process p = Runtime.getRuntime().exec(cmd);
				
				p.waitFor();
	
				size = (int) a.length();
				
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    
	//		try(ByteArrayOutputStream tmp = new ByteArrayOutputStream())
	//		{
	//		    ImageIO.write(img, "png", tmp);
	//		    contentLength = tmp.size();
	//		}
	//		catch(Exception e)
	//		{
	//			// TODO
	//		}
		    
		    return size;
		}
}
