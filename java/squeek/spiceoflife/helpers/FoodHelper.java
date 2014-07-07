package squeek.spiceoflife.helpers;

import java.lang.reflect.Field;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import squeek.spiceoflife.proxy.ProxyAgriculture;
import squeek.spiceoflife.proxy.ProxyHungerOverhaul;
import squeek.spiceoflife.proxy.ProxyMariculture;

public class FoodHelper
{
	public static final Field foodExhaustion = ReflectionHelper.findField(FoodStats.class, "foodExhaustionLevel", "field_75126_c", "c");
	static
	{
		foodExhaustion.setAccessible(true);
	}
	
	public static boolean isFood(ItemStack itemStack)
	{
		return itemStack.getItem() instanceof ItemFood || ProxyAgriculture.isFood(itemStack) || ProxyMariculture.isFood(itemStack);
	}

	public static float getExhaustionLevel(FoodStats foodStats)
	{
		try
		{
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
			foodExhaustion.setFloat(foodStats, val);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static float getMaxExhaustionLevel(World world)
	{
		if (ProxyHungerOverhaul.initialized)
			return ProxyHungerOverhaul.getMaxExhaustionLevel(world);
		else
			return 4f;
	}
}
