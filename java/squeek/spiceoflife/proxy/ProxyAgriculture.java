package squeek.spiceoflife.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.foodtracker.FoodValues;
import cpw.mods.fml.common.Loader;

public class ProxyAgriculture
{
	public static boolean initialized = false;

	protected static Class<?> SuperItem = null;
	protected static Field subItems = null;
	protected static Class<?> SubItemFood = null;
	protected static Method getHealAmount = null;
	protected static Method getSaturationModifier = null;
	static
	{
		try
		{
			if (Loader.isModLoaded("Agriculture"))
			{
				SuperItem = Class.forName("com.teammetallurgy.agriculture.SuperItem");
				subItems = SuperItem.getDeclaredField("subItems");
				subItems.setAccessible(true);
				SubItemFood = Class.forName("com.teammetallurgy.agriculture.SubItemFood");
				getHealAmount = SubItemFood.getDeclaredMethod("getHealAmount");
				getSaturationModifier = SubItemFood.getDeclaredMethod("getSaturationModifier");

				initialized = true;
			}
		}
		catch (Exception e)
		{
			ModSpiceOfLife.Log.warning("Unable to properly integrate with Agriculture (tooltips won't work): " + e.getMessage());
		}
	}

	public static boolean isFood(ItemStack itemStack)
	{
		return getSubItemFood(itemStack) != null;
	}

	public static Object getSubItemFood(ItemStack itemStack)
	{
		if (initialized && SuperItem.isInstance(itemStack.getItem()))
		{
			try
			{
				@SuppressWarnings("unchecked")
				Map<Integer, Object> subItems = (Map<Integer, Object>) ProxyAgriculture.subItems.get(itemStack.getItem());
				if (subItems.containsKey(itemStack.getItemDamage()) && SubItemFood.isInstance(subItems.get(itemStack.getItemDamage())))
				{
					return SubItemFood.cast(subItems.get(itemStack.getItemDamage()));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public static FoodValues getFoodValues(ItemStack itemStack)
	{
		if (initialized)
		{
			Object subItemFood = getSubItemFood(itemStack);
			if (subItemFood != null)
			{
				try
				{
					int hunger = (Integer) getHealAmount.invoke(subItemFood);
					float saturationModifier = (Float) getSaturationModifier.invoke(subItemFood);
					return new FoodValues(hunger, saturationModifier);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return new FoodValues(0, 0);
	}
}
