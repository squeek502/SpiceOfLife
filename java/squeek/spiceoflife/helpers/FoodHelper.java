package squeek.spiceoflife.helpers;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.proxy.ProxyAgriculture;

public class FoodHelper
{
	public static boolean isFood(ItemStack itemStack)
	{
		return itemStack.getItem() instanceof ItemFood || ProxyAgriculture.isFood(itemStack);
	}
}
