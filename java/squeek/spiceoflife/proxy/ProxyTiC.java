package squeek.spiceoflife.proxy;

import java.lang.reflect.Field;
import cpw.mods.fml.common.Loader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.foodtracker.FoodValues;

public class ProxyTiC
{
	protected static Class<?> specialFood = null;
	protected static Field hunger = null;
	protected static Field saturation = null;
	static
	{
		try
		{
			if (Loader.isModLoaded("TConstruct"))
			{
				specialFood = Class.forName("tconstruct.items.SpecialFood");
			}
		}
		catch(ClassNotFoundException e)
		{
			try
			{
				specialFood = Class.forName("tconstruct.world.items.SpecialFood");
			}
			catch(ClassNotFoundException e2)
			{
			}
		}
		try
		{
			if (specialFood != null)
			{
				hunger = specialFood.getDeclaredField("hunger");
				hunger.setAccessible(true);
				saturation = specialFood.getDeclaredField("saturation");
				saturation.setAccessible(true);
			}
		}
		catch (Exception e)
		{
			ModSpiceOfLife.Log.warn("Unable to properly integrate with Tinkers' Construct (TiC food values will be incorrect): ");
			e.printStackTrace();
		}
	}

	public static boolean isSpecialFood(Item item)
	{
		return specialFood != null && specialFood.isInstance(item);
	}

	public static FoodValues getSpecialFoodValues(ItemStack itemStack)
	{
		if (ProxyTiC.hunger != null && ProxyTiC.saturation != null)
		{
			try
			{
				int hunger = ((int[]) ProxyTiC.hunger.get(itemStack.getItem()))[itemStack.getItemDamage()];
				float saturationModifier = ((float[]) ProxyTiC.saturation.get(itemStack.getItem()))[itemStack.getItemDamage()];
				return new FoodValues(hunger, saturationModifier);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return new FoodValues(0, 0);
	}
}
