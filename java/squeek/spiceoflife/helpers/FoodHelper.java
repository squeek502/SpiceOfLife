package squeek.spiceoflife.helpers;

import java.lang.reflect.Field;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.items.ItemFoodContainer;
import squeek.spiceoflife.proxy.ProxyAgriculture;
import squeek.spiceoflife.proxy.ProxyMariculture;

public class FoodHelper
{
	public static final Field foodExhaustion = ReflectionHelper.findField(FoodStats.class, "foodExhaustionLevel", "field_75126_c", "c");
	public static Field harderPeacefulExhaustion = null;
	static
	{
		if (Loader.isModLoaded("wuppy29_harderpeaceful"))
		{
			Class<?> FoodStatsHP = null;
			try
			{
				FoodStatsHP = Class.forName("harderpeaceful.FoodStatsHP");
				harderPeacefulExhaustion = FoodStatsHP.getDeclaredField("field_75126_c");
			}
			catch (NoSuchFieldException e)
			{
				try
				{
					harderPeacefulExhaustion = FoodStatsHP.getDeclaredField("foodExhaustionLevel");
				}
				catch (Exception e1)
				{
					e.printStackTrace();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (harderPeacefulExhaustion != null)
				harderPeacefulExhaustion.setAccessible(true);
		}
		foodExhaustion.setAccessible(true);
	}

	public static boolean isValidFood(ItemStack itemStack)
	{
		return isFood(itemStack) && canFoodDiminish(itemStack);
	}

	public static boolean canFoodDiminish(ItemStack itemStack)
	{
		return !FoodGroupRegistry.isFoodBlacklisted(itemStack);
	}

	public static boolean isFood(ItemStack itemStack)
	{
		return (itemStack.getItem() instanceof ItemFood && isEdible(itemStack)) || ProxyAgriculture.isFood(itemStack) || ProxyMariculture.isFood(itemStack) || itemStack.getItem() == Items.cake;
	}

	public static boolean isFoodContainer(ItemStack itemStack)
	{
		return itemStack.getItem() instanceof ItemFoodContainer;
	}

	public static boolean isEdible(ItemStack itemStack)
	{
		EnumAction useAction = itemStack.getItem().getItemUseAction(itemStack);
		return useAction == EnumAction.eat || useAction == EnumAction.drink;
	}

	public static boolean isDirectlyEdible(ItemStack itemStack)
	{
		return !(itemStack.getItem() == Items.cake || isFoodContainer(itemStack));
	}

	public static float getExhaustionLevel(FoodStats foodStats)
	{
		try
		{
			if (harderPeacefulExhaustion != null)
				return harderPeacefulExhaustion.getFloat(foodStats);
			else
				return foodExhaustion.getFloat(foodStats);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0f;
		}
	}

	public static void setExhaustionLevel(FoodStats foodStats, float val)
	{
		try
		{
			if (harderPeacefulExhaustion != null)
				harderPeacefulExhaustion.setFloat(foodStats, val);
			else
				foodExhaustion.setFloat(foodStats, val);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static float getMaxExhaustionLevel(World world)
	{
		return 4f;
	}
}
