package squeek.spiceoflife.helpers;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.proxy.ProxyAgriculture;
import squeek.spiceoflife.proxy.ProxyMariculture;

public class FoodHelper
{
	public static boolean isFood(ItemStack itemStack)
	{
		return itemStack.getItem() instanceof ItemFood || ProxyAgriculture.isFood(itemStack) || ProxyMariculture.isFood(itemStack);
	}
}
