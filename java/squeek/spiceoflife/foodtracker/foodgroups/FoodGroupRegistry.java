package squeek.spiceoflife.foodtracker.foodgroups;

import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.compat.PacketDispatcher;
import squeek.spiceoflife.network.PacketFoodGroup;

public class FoodGroupRegistry
{
	private static HashMap<String, FoodGroup> foodGroups = new HashMap<String, FoodGroup>();
	private static boolean hasBlacklist = false;

	public static FoodGroup getFoodGroup(String identifier)
	{
		return foodGroups.get(identifier);
	}

	public static void addFoodGroup(FoodGroup foodGroup)
	{
		foodGroups.put(foodGroup.identifier, foodGroup);

		if (foodGroup.blacklist)
			hasBlacklist = true;
	}

	public static boolean foodGroupExists(String identifier)
	{
		return foodGroups.containsKey(identifier);
	}

	public static FoodGroup getFoodGroupForFood(ItemStack food)
	{
		FoodGroup highestPriorityFoodGroup = null;
		for (FoodGroup foodGroup : foodGroups.values())
		{
			if ((highestPriorityFoodGroup == null || foodGroup.priority > highestPriorityFoodGroup.priority) && foodGroup.isFoodIncluded(food))
			{
				highestPriorityFoodGroup = foodGroup;
			}
		}
		return highestPriorityFoodGroup;
	}

	public static boolean isFoodBlacklisted(ItemStack food)
	{
		if (!hasBlacklist && !ModConfig.USE_FOOD_GROUPS_AS_WHITELISTS)
			return false;

		FoodGroup foodGroup = getFoodGroupForFood(food);
		return (ModConfig.USE_FOOD_GROUPS_AS_WHITELISTS && foodGroup == null) || (foodGroup != null && foodGroup.blacklist);
	}

	public static void sync(EntityPlayerMP player)
	{
		for (FoodGroup foodGroup : foodGroups.values())
		{
			PacketDispatcher.get().sendTo(new PacketFoodGroup(foodGroup), player);
		}
	}
	
	public static void serverInit()
	{
		for (FoodGroup foodGroup : foodGroups.values())
		{
			foodGroup.init();
		}
	}
	
	public static void clear()
	{
		foodGroups.clear();
	}
}
