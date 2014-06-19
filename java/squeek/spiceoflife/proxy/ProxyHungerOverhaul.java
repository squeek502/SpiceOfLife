package squeek.spiceoflife.proxy;

import java.lang.reflect.Field;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.foodtracker.FoodValues;
import cpw.mods.fml.common.Loader;

public class ProxyHungerOverhaul
{
	public static boolean initialized = false;

	protected static Class<?> iguanaFood = null;
	protected static Class<?> iguanaFoodStats = null;
	protected static FoodStats dummyFoodStats = null;
	protected static Field foodStatsPlayer = null;
	protected static boolean modifyFoodValues = false;
	protected static Field foodRegensHealth = null;
	protected static int modFoodValueDivider = 1;
	static
	{
		try
		{
			if (Loader.isModLoaded("HungerOverhaul"))
			{
				iguanaFoodStats = Class.forName("iguanaman.hungeroverhaul.IguanaFoodStats");
				foodStatsPlayer = iguanaFoodStats.getDeclaredField("entityplayer");
				dummyFoodStats = (FoodStats) iguanaFoodStats.getConstructor(int.class).newInstance(0);

				iguanaFood = Class.forName("iguanaman.hungeroverhaul.items.IguanaFood");

				Class<?> iguanaConfig = Class.forName("iguanaman.hungeroverhaul.IguanaConfig");
				modifyFoodValues = iguanaConfig.getDeclaredField("modifyFoodValues").getBoolean(null) && Loader.isModLoaded("pamharvestcraft");
				modFoodValueDivider = iguanaConfig.getDeclaredField("modFoodValueDivider").getInt(null);
				foodRegensHealth = iguanaConfig.getDeclaredField("foodRegensHealth");

				initialized = true;
			}
		}
		catch (Exception e)
		{
			ModSpiceOfLife.Log.warn("Unable to properly integrate with Hunger Overhaul (some food values may be incorrect): " + e.getMessage());
		}
	}

	public static boolean foodValuesWillBeModified(ItemStack food)
	{
		return modifyFoodValues && modFoodValueDivider != 1 && iguanaFood != null && !iguanaFood.isInstance(food.getItem());
	}

	public static FoodValues getModifiedFoodValues(ItemStack food)
	{
		try
		{
			// need to temporarily set this to false to avoid a NPE
			boolean wasRegenHealthEnabled = foodRegensHealth.getBoolean(null);
			foodRegensHealth.setBoolean(null, false);
	
			dummyFoodStats.setFoodLevel(0);
			dummyFoodStats.setFoodSaturationLevel(0);
			dummyFoodStats.func_151686_a((ItemFood) food.getItem(), food);
	
			foodRegensHealth.setBoolean(null, wasRegenHealthEnabled);
			
			int hunger = dummyFoodStats.getFoodLevel();
			float saturationModifier = FoodValues.getSaturationModifierFromIncrement(dummyFoodStats.getSaturationLevel(), hunger);

			return new FoodValues(hunger, saturationModifier);
		}
		catch(Exception e)
		{
			return new FoodValues(0, 0);
		}
	}

	public static boolean isDummyFoodStats(FoodStats foodStats)
	{
		try
		{
			return initialized && iguanaFoodStats.isInstance(foodStats) && foodStatsPlayer.get(foodStats) == null;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
