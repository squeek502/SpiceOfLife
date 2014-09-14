package squeek.spiceoflife.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileHelper
{
	public static void copyFile(File sourceFile, File destFile) throws IOException
	{
		copyFile(sourceFile, destFile, false);
	}

	// from https://gist.github.com/donaldmunro/2959131
	public static void copyFile(File sourceFile, File destFile, final boolean overwrite) throws IOException
	{
		if (destFile.isDirectory())
			destFile = new File(destFile, sourceFile.getName());

		if (destFile.exists())
		{
			if (overwrite)
				destFile.delete();
		}
		else
		{
			destFile.createNewFile();
		}

		FileInputStream fIn = null;
		FileOutputStream fOut = null;
		FileChannel source = null;
		FileChannel destination = null;
		try
		{
			fIn = new FileInputStream(sourceFile);
			source = fIn.getChannel();
			fOut = new FileOutputStream(destFile);
			destination = fOut.getChannel();
			long transfered = 0;
			long bytes = source.size();
			while (transfered < bytes)
			{
				transfered += destination.transferFrom(source, 0, source.size());
				destination.position(transfered);
			}
		}
		finally
		{
			if (source != null)
			{
				source.close();
			}
			if (fIn != null)
			{
				fIn.close();
			}
			if (destination != null)
			{
				destination.close();
			}
			if (fOut != null)
			{
				fOut.close();
			}
		}
	}
}
