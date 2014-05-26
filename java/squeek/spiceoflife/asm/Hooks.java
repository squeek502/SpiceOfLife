package squeek.spiceoflife.asm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.FoodTracker;

public class Hooks
{
	private static EntityPlayer lastEatingPlayer = null;
	private static ItemStack lastFoodEaten = null;

	/**
	 * Hooks into net.minecraft.util.FoodStats.AddStats
	 * @param foodStats
	 * @param hunger
	 * @param saturationModifier
	 * @return Value to be multiplied by foodLevel and foodSaturationModifier
	 */
	public static float getFoodModifier(FoodStats foodStats, int hunger, float saturationModifier)
	{
		if (lastFoodEaten != null && lastEatingPlayer != null)
		{
			float modifier = FoodModifier.getFoodModifier(lastEatingPlayer, lastFoodEaten, foodStats, hunger, saturationModifier);
			
			// set these to null so that they are set only for the getFoodModifier call after an onFoodEaten call
			lastFoodEaten = null;
			lastEatingPlayer = null;

			return modifier;
		}
		else
			return 1f;
	}

	/**
	 * Hooks into net.minecraft.item.ItemStack.onFoodEaten
	 * @param itemStack The food being eaten
	 * @param world The world that the food is being eaten in
	 * @param player The player eating the food
	 */
	public static void onFoodEaten(ItemStack itemStack, World world, EntityPlayer player)
	{
		lastEatingPlayer = player;
		lastFoodEaten = itemStack;
		
		if (!player.worldObj.isRemote)
			FoodTracker.addFoodEatenByPlayer(itemStack, player);
	}
}
