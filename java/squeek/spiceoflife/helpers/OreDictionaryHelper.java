package squeek.spiceoflife.helpers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictionaryHelper
{
	// taken from OreDictionary
	public static int getItemStackHash(ItemStack itemStack)
	{
		int hash = getWildCardItemStackHash(itemStack);
		if (itemStack.getItemDamage() != OreDictionary.WILDCARD_VALUE)
		{
			hash |= ((itemStack.getItemDamage() + 1) << 16); // +1 so 0 is significant
		}
		return hash;
	}

	public static int getWildCardItemStackHash(ItemStack itemStack)
	{
		return getItemHash(itemStack.getItem());
	}

	public static int getItemHash(Item item)
	{
		return Item.getIdFromItem(item);
	}

}
