package squeek.spiceoflife.foodtracker.foodgroups;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.helpers.FileHelper;
import squeek.spiceoflife.helpers.MiscHelper;

import java.io.*;

public class FoodGroupConfig
{
	private static final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
	private static File[] configFiles;

	public static void setup(File configDirectory)
	{
		File modConfigDirectory = new File(configDirectory, ModInfo.MODID);
		if (modConfigDirectory.exists() || modConfigDirectory.mkdirs())
		{
			writeExampleFoodGroup(modConfigDirectory);
			configFiles = modConfigDirectory.listFiles();
		}
	}

	public static void writeExampleFoodGroup(File configDirectory)
	{
		String exampleFoodGroupFileName = "example-food-group.json";
		String exampleFoodGroupRelativePath = "example/" + exampleFoodGroupFileName;
		File exampleFoodGroupDest = new File(configDirectory, exampleFoodGroupFileName);

		try
		{
			boolean shouldOverwrite = shouldOverwriteExampleFoodGroup(exampleFoodGroupDest);
			if (ModSpiceOfLife.instance.sourceFile != null && ModSpiceOfLife.instance.sourceFile.isDirectory())
			{
				File sourceFile = new File(ModSpiceOfLife.instance.sourceFile, exampleFoodGroupRelativePath);
				FileHelper.copyFile(sourceFile, exampleFoodGroupDest, shouldOverwrite);
			}
			else
			{
				InputStream exampleFoodGroupInputStream = FoodGroupConfig.class.getClassLoader().getResourceAsStream(exampleFoodGroupRelativePath);
				FileHelper.copyFile(exampleFoodGroupInputStream, exampleFoodGroupDest, shouldOverwrite);
				exampleFoodGroupInputStream.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static boolean shouldOverwriteExampleFoodGroup(File exampleFoodGroup) throws IOException
	{
		FileInputStream exampleFoodGroupStream;
		try
		{
			exampleFoodGroupStream = new FileInputStream(exampleFoodGroup);
		}
		catch (FileNotFoundException e)
		{
			return true;
		}
		BufferedReader exampleFoodGroupReader = null;
		try
		{
			exampleFoodGroupReader = new BufferedReader(new InputStreamReader(exampleFoodGroupStream, "UTF8"));
			String firstLine = exampleFoodGroupReader.readLine();
			return !("// Mod Version: " + ModInfo.VERSION).equals(firstLine);
		}
		finally
		{
			MiscHelper.tryCloseStream(exampleFoodGroupReader);
		}
	}

	public static void load()
	{
		for (File configFile : configFiles)
		{
			InputStreamReader reader = null;
			try
			{
				reader = new InputStreamReader(new FileInputStream(configFile), "UTF8");
				FoodGroup foodGroup = gson.fromJson(reader, FoodGroup.class);
				if (foodGroup != null && foodGroup.enabled)
				{
					foodGroup.identifier = FilenameUtils.removeExtension(configFile.getName());
					foodGroup.initFromConfig();
					FoodGroupRegistry.addFoodGroup(foodGroup);
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				MiscHelper.tryCloseStream(reader);
			}
		}
	}

}
