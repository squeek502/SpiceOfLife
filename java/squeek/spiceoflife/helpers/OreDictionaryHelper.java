package squeek.spiceoflife.helpers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictionaryHelper
{
	private static HashMap<Integer, ArrayList<ItemStack>> oreStacks = null;
	static
	{
		try
		{
			Field field = OreDictionary.class.getDeclaredField("oreStacks");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<Integer, ArrayList<ItemStack>> oreStacksTemp = (HashMap<Integer, ArrayList<ItemStack>>) field.get(null);
			oreStacks = oreStacksTemp;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static int[] getOreIDs(ItemStack itemStack)
	{
		if (itemStack == null)
			return new int[0];

		List<Integer> ids = new ArrayList<Integer>();
		for (Entry<Integer, ArrayList<ItemStack>> ore : oreStacks.entrySet())
		{
			for (ItemStack target : ore.getValue())
			{
				if (OreDictionary.itemMatches(target, itemStack, false))
				{
					ids.add(ore.getKey());
				}
			}
		}
		int[] ret = new int[ids.size()];
		for (int x = 0; x < ids.size(); x++)
			ret[x] = ids.get(x);
		return ret;
	}

	public static List<String> getOreNames(ItemStack itemStack)
	{
		List<String> oreNames = new ArrayList<String>();
		for (int oreID : getOreIDs(itemStack))
		{
			oreNames.add(OreDictionary.getOreName(oreID));
		}
		return oreNames;
	}
}
