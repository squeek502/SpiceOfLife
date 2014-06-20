package squeek.spiceoflife.foodtracker.foodgroups;

import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.compat.PacketDispatcher;
import squeek.spiceoflife.network.PacketFoodGroup;

public class FoodGroupRegistry
{
	private static HashMap<String, FoodGroup> foodGroups = new HashMap<String, FoodGroup>();
	
	public static FoodGroup getFoodGroup(String name)
	{
		return foodGroups.get(name);
	}
	
	public static void addFoodGroup(FoodGroup foodGroup)
	{
		foodGroups.put(foodGroup.identifier, foodGroup);
	}
	
	public static boolean foodGroupExists(String name)
	{
		return foodGroups.containsKey(name);
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
