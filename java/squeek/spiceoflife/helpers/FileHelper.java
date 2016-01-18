package squeek.spiceoflife.helpers;

import java.io.*;

public class FileHelper
{
	public static void copyFile(File sourceFile, File destFile) throws IOException
	{
		copyFile(sourceFile, destFile, false);
	}

	public static void copyFile(File sourceFile, File destFile, boolean overwrite) throws IOException
	{
		InputStream sourceInput = null;
		try
		{
			sourceInput = new FileInputStream(sourceFile);
			copyFile(sourceInput, destFile, overwrite);
		}
		finally
		{
			if (sourceInput != null)
			{
				sourceInput.close();
			}
		}

	}

	public static void copyFile(InputStream sourceInput, File destFile, boolean overwrite) throws IOException
	{
		if (destFile.exists())
		{
			if (overwrite)
				assert destFile.delete();
			else
				return;
		}
		else
		{
			assert destFile.createNewFile();
		}

		FileOutputStream destOutput = null;
		try
		{
			destOutput = new FileOutputStream(destFile);
			int readBytes;
			byte[] buffer = new byte[4096];
			while ((readBytes = sourceInput.read(buffer)) > 0)
			{
				destOutput.write(buffer, 0, readBytes);
			}
		}
		finally
		{
			if (destOutput != null)
			{
				destOutput.close();
			}
		}
	}
}
