package squeek.spiceoflife.proxy;

import java.lang.reflect.Field;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.foodtracker.FoodValues;
import cpw.mods.fml.common.Loader;

public class ProxyNatura
{
	protected static boolean initialized = false;

	protected static Class<?> specialFood = null;
	protected static Field hunger = null;
	protected static Field saturation = null;
	static
	{
		try
		{
			if (Loader.isModLoaded("Natura"))
			{
				specialFood = Class.forName("mods.natura.items.NSpecialFood");
				hunger = specialFood.getDeclaredField("hunger");
				hunger.setAccessible(true);
				saturation = specialFood.getDeclaredField("saturation");
				saturation.setAccessible(true);
				
				initialized = true;
			}
		}
		catch (Exception e)
		{
			ModSpiceOfLife.Log.warn("Unable to properly integrate with Natura (Natura food values will be incorrect): ");
			e.printStackTrace();
		}
	}

	public static boolean isSpecialFood(Item item)
	{
		return initialized && specialFood.isInstance(item);
	}

	public static FoodValues getSpecialFoodValues(ItemStack itemStack)
	{
		if (initialized)
		{
			try
			{
				int hunger = ((int[]) ProxyNatura.hunger.get(itemStack.getItem()))[itemStack.getItemDamage()];
				float saturationModifier = ((float[]) ProxyNatura.saturation.get(itemStack.getItem()))[itemStack.getItemDamage()];
				return new FoodValues(hunger, saturationModifier);
			}
			catch (Exception e)
			{
			}
		}

		return new FoodValues(0, 0);
	}
}
