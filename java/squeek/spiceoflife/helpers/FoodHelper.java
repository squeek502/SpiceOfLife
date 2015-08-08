package squeek.spiceoflife.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import squeek.applecore.api.AppleCoreAPI;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.items.ItemFoodContainer;

public class FoodHelper
{
	public static boolean isValidFood(ItemStack itemStack)
	{
		return isFood(itemStack) && !isFoodContainer(itemStack);
	}

	public static boolean canFoodDiminish(ItemStack itemStack)
	{
		return !FoodGroupRegistry.isFoodBlacklisted(itemStack);
	}

	public static boolean isFood(ItemStack itemStack)
	{
		return AppleCoreAPI.accessor.isFood(itemStack);
	}

	public static boolean isFoodContainer(ItemStack itemStack)
	{
		return itemStack.getItem() instanceof ItemFoodContainer;
	}

	public static float getExhaustionLevel(EntityPlayer player)
	{
		return AppleCoreAPI.accessor.getExhaustion(player);
	}

	public static boolean isDirectlyEdible(ItemStack itemStack)
	{
		return !(itemStack.getItem() == Items.cake || isFoodContainer(itemStack));
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
