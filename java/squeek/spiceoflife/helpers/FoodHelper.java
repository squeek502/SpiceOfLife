package squeek.spiceoflife.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import squeek.applecore.api.AppleCoreAPI;

public class FoodHelper
{
	public static boolean isFood(ItemStack itemStack)
	{
		return AppleCoreAPI.accessor.isFood(itemStack);
	}

	public static float getExhaustionLevel(EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getExhaustion(player);
	}

	public static float getMaxExhaustionLevel(EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getMaxExhaustion(player);
	}

	public static float getSaturationModifierFromIncrement(float saturationIncrement, int hunger)
	{
		return hunger != 0 ? saturationIncrement / (hunger * 2f) : 0f;
	}
}
