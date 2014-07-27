package squeek.spiceoflife.asm;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.FoodTracker;
import squeek.spiceoflife.foodtracker.FoodValues;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.proxy.ProxyHungerOverhaul;

public class Hooks
{
	private static EntityPlayer lastEatingPlayer = null;
	private static ItemStack lastFoodEaten = null;
	private static long lastTimeEaten = -1;
	public static int toolTipX, toolTipY, toolTipW, toolTipH;

	/**
	 * Hooks into net.minecraft.util.FoodStats.AddStats
	 * @param foodStats
	 * @param hunger
	 * @param saturationModifier
	 * @return FoodValues object containing the modified food values
	 */
	public static FoodValues getModifiedFoodValues(FoodStats foodStats, int hunger, float saturationModifier)
	{
		if (ProxyHungerOverhaul.initialized && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			if (!ProxyHungerOverhaul.iguanaFoodStats.isInstance(foodStats))
			{
				ModSpiceOfLife.Log.warn("FoodStats is not an instance of IguanaFoodStats");
			}
			try
			{
				boolean shouldRegenHealth = ProxyHungerOverhaul.initialFoodRegensHealthValue;
				boolean currentRegenHealthSetting = ProxyHungerOverhaul.foodRegensHealth.getBoolean(null);
				if (shouldRegenHealth && !currentRegenHealthSetting && !ProxyHungerOverhaul.isDummyFoodStats(foodStats))
				{
					ModSpiceOfLife.Log.warn("Hunger Overhaul's regen health config option is set to false (it was initially true)");
				}
			}
			catch (Exception e)
			{
			}
		}

		if (ModConfig.FOOD_MODIFIER_ENABLED && lastFoodEaten != null && lastEatingPlayer != null && (lastEatingPlayer.worldObj.getWorldTime() - lastTimeEaten) <= 0 && !ProxyHungerOverhaul.isDummyFoodStats(foodStats))
		{
			float modifier = FoodModifier.getFoodModifier(lastEatingPlayer, lastFoodEaten, foodStats, hunger, saturationModifier);
			FoodValues modifiedFoodValues = FoodModifier.getModifiedFoodValues(new FoodValues(hunger, saturationModifier), modifier);

			FoodEaten foodEaten = new FoodEaten(lastFoodEaten);
			foodEaten.hungerRestored = modifiedFoodValues.hunger;
			foodEaten.foodGroup = FoodGroupRegistry.getFoodGroupForFood(lastFoodEaten);

			if (!lastEatingPlayer.worldObj.isRemote)
				FoodTracker.addFoodEatenByPlayer(foodEaten, lastEatingPlayer);

			return modifiedFoodValues;
		}
		else
		{
			if (ModConfig.FOOD_MODIFIER_ENABLED && lastFoodEaten != null && FoodHelper.isFood(lastFoodEaten) && !ProxyHungerOverhaul.isDummyFoodStats(foodStats))
			{
				ModSpiceOfLife.Log.warn(lastFoodEaten.getDisplayName() + " didn't count toward food history (player=" + lastEatingPlayer + ", timedelta=" + (lastEatingPlayer.worldObj.getWorldTime() - lastTimeEaten) + ")");
			}

			return new FoodValues(hunger, saturationModifier);
		}
	}

	/**
	 * Hooks into net.minecraft.item.ItemStack.onFoodEaten
	 * @param itemStack The food being eaten
	 * @param world The world that the food is being eaten in
	 * @param player The player eating the food
	 */
	public static void onFoodEaten(ItemStack itemStack, World world, EntityPlayer player)
	{
		// only react to ItemFood items (because things like TiC chisels use onFoodEaten)
		if (FoodHelper.isFood(itemStack))
		{
			lastEatingPlayer = player;
			lastFoodEaten = itemStack;
			lastTimeEaten = world.getWorldTime();
		}
		else
		{
			lastEatingPlayer = null;
			lastFoodEaten = null;
			lastTimeEaten = -1;
		}
	}
	
	public static void onBlockFoodEaten(Block block, World world, EntityPlayer player)
	{
		if (block instanceof BlockCake)
		{
			onFoodEaten(new ItemStack(Items.cake), world, player);
		}
	}

	public static void onDrawHoveringText(int x, int y, int w, int h)
	{
		toolTipX = x;
		toolTipY = y;
		toolTipW = w;
		toolTipH = h;
	}
}
