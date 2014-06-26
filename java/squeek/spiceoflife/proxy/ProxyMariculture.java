package squeek.spiceoflife.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.foodtracker.FoodValues;
import cpw.mods.fml.common.Loader;

public class ProxyMariculture
{
	public static boolean initialized = false;

	protected static Class<?> ItemFishyFood = null;
	protected static Class<?> ItemFood = null;
	protected static Method getFoodLevel = null;
	protected static Method getFoodSaturation = null;
	protected static Class<?> ItemBait = null;
	protected static boolean nerfFood = false;

	// don't want to include or build against the api, so just use reflection like a dumbo
	protected static Class<?> apiFishing = null;
	protected static Field fishingBait = null;
	protected static Field fishingFish = null;
	protected static Class<?> apiIBaitHandler = null;
	protected static Method getBaitQuality = null;
	protected static Class<?> apiIFishHelper = null;
	protected static Method getSpecies = null;
	protected static Class<?> apiFishSpecies = null;
	protected static Method FishSpecies_getFoodStat = null;
	protected static Method FishSpecies_getFoodSaturation = null;
	protected static int DEBUG_FOOD = -1;

	static
	{
		try
		{
			if (Loader.isModLoaded("Mariculture"))
			{
				nerfFood = Loader.isModLoaded("HungerOverhaul");

				ItemFishyFood = Class.forName("mariculture.fishery.items.ItemFishyFood");
				ItemBait = Class.forName("mariculture.fishery.items.ItemBait");

				ItemFood = Class.forName("mariculture.core.items.ItemFood");
				getFoodLevel = ItemFood.getDeclaredMethod("getFoodLevel", int.class);
				getFoodLevel.setAccessible(true);
				getFoodSaturation = ItemFood.getDeclaredMethod("getFoodSaturation", int.class);
				getFoodSaturation.setAccessible(true);

				apiFishing = Class.forName("mariculture.api.fishery.Fishing");
				apiIBaitHandler = Class.forName("mariculture.api.fishery.IBaitHandler");
				apiIFishHelper = Class.forName("mariculture.api.fishery.IFishHelper");
				apiFishSpecies = Class.forName("mariculture.api.fishery.fish.FishSpecies");

				fishingBait = apiFishing.getDeclaredField("bait");
				fishingFish = apiFishing.getDeclaredField("fishHelper");

				getSpecies = apiIFishHelper.getDeclaredMethod("getSpecies", int.class);
				getBaitQuality = apiIBaitHandler.getDeclaredMethod("getBaitQuality", ItemStack.class);
				FishSpecies_getFoodStat = apiFishSpecies.getDeclaredMethod("getFoodStat");
				FishSpecies_getFoodSaturation = apiFishSpecies.getDeclaredMethod("getFoodSaturation");

				initialized = true;
			}
		}
		catch (Exception e)
		{
			ModSpiceOfLife.Log.warning("Unable to properly integrate with Mariculture foods: ");
			e.printStackTrace();
		}
		if (initialized)
		{
			try
			{
				DEBUG_FOOD = (Integer) Class.forName("mariculture.core.lib.FoodMeta").getDeclaredField("DEBUG_FOOD").get(null);
			}
			catch (Exception e)
			{
			}
		}
	}

	public static boolean isFood(ItemStack itemStack)
	{
		return initialized && ((ItemFood.isInstance(itemStack.getItem()) && itemStack.getItemDamage() != DEBUG_FOOD) || ItemFishyFood.isInstance(itemStack.getItem()) || ItemBait.isInstance(itemStack.getItem()));
	}

	public static FoodValues getFoodValues(ItemStack itemStack)
	{
		if (initialized)
		{
			try
			{
				if (ItemFood.isInstance(itemStack.getItem()))
				{
					if (itemStack.getItemDamage() == DEBUG_FOOD)
					{
						return new FoodValues(-10, 0F);
					}
					else
					{
						int level = (Integer) getFoodLevel.invoke(itemStack.getItem(), itemStack.getItemDamage());
						float sat = (Float) getFoodSaturation.invoke(itemStack.getItem(), itemStack.getItemDamage());
						if (nerfFood)
						{
							level = (int) Math.max(1, level / 2.5);
							sat = Math.max(0.0F, sat / 10);
						}
						return new FoodValues(level, sat);
					}
				}
				else if (ItemFishyFood.isInstance(itemStack.getItem()))
				{
					Object fish = getSpecies.invoke(fishingFish.get(null), itemStack.getItemDamage());
					if (fish != null)
					{
						int food = (Integer) FishSpecies_getFoodStat.invoke(fish);
						float sat = (Float) FishSpecies_getFoodSaturation.invoke(fish);
						if (nerfFood)
						{
							food = Math.max(1, food / 2);
							sat = Math.max(0.0F, sat / 10);
						}
						return new FoodValues(food, sat);
					}
				}
				else if (ItemBait.isInstance(itemStack.getItem()))
				{
					int quality = (Integer) getBaitQuality.invoke(fishingBait.get(null), itemStack);
					int fill = nerfFood ? 1 : (int) (((double) quality / 100) * 4.0D);
					fill = (fill >= 1) ? fill : 1;
					float sat = 0;

					if (Loader.instance().getIndexedModList().get("Mariculture").getVersion().compareToIgnoreCase("1.2.2f") <= 0)
						sat = -100F;

					return new FoodValues(fill, sat);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return new FoodValues(0, 0);
	}

}
