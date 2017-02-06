package squeek.spiceoflife.foodtracker.foodgroups;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.compat.PacketDispatcher;
import squeek.spiceoflife.helpers.OreDictionaryHelper;
import squeek.spiceoflife.network.PacketFoodGroup;

import javax.annotation.Nonnull;
import java.util.*;

public class FoodGroupRegistry
{
	private static Map<String, FoodGroup> foodGroups = new HashMap<String, FoodGroup>();
	/**
	 * Does not deal with food group exclusions; that must be handled separately
	 * See {@link #getFoodGroupsForFood(ItemStack)}
	 */
	private static Map<Integer, Set<FoodGroup>> foodToIncludedFoodGroups = new HashMap<Integer, Set<FoodGroup>>();
	private static boolean hasBlacklist = false;

	public static FoodGroup getFoodGroup(String identifier)
	{
		return foodGroups.get(identifier);
	}

	public static Collection<FoodGroup> getFoodGroups()
	{
		return foodGroups.values();
	}

	public static int numFoodGroups()
	{
		return getFoodGroups().size();
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

	public static Set<FoodGroup> getFoodGroupsForFood(@Nonnull ItemStack food)
	{
		Set<FoodGroup> wildCardFoodGroups = foodToIncludedFoodGroups.get(OreDictionaryHelper.getWildCardItemStackHash(food));
		Set<FoodGroup> exactFoodGroups = foodToIncludedFoodGroups.get(OreDictionaryHelper.getItemStackHash(food));
		Set<FoodGroup> allFoodGroups = new HashSet<FoodGroup>();

		if (wildCardFoodGroups != null)
		{
			for (FoodGroup foodGroup : wildCardFoodGroups)
			{
				if (!foodGroup.isFoodExcluded(food))
					allFoodGroups.add(foodGroup);
			}
		}
		if (exactFoodGroups != null)
		{
			for (FoodGroup foodGroup : exactFoodGroups)
			{
				if (!foodGroup.isFoodExcluded(food))
					allFoodGroups.add(foodGroup);
			}
		}

		return allFoodGroups;
	}

	private static boolean isAnyFoodGroupBlacklist(Collection<FoodGroup> foodGroups)
	{
		for (FoodGroup foodGroup : foodGroups)
		{
			if (foodGroup.blacklist)
				return true;
		}
		return false;
	}

	public static boolean isFoodBlacklisted(@Nonnull ItemStack food)
	{
		if (!hasBlacklist && !ModConfig.USE_FOOD_GROUPS_AS_WHITELISTS)
			return false;

		Set<FoodGroup> foodGroups = getFoodGroupsForFood(food);
		boolean isInAnyFoodGroups = !foodGroups.isEmpty();
		boolean isInBlacklistFoodGroup = isInAnyFoodGroups && isAnyFoodGroupBlacklist(foodGroups);
		return (ModConfig.USE_FOOD_GROUPS_AS_WHITELISTS && !isInAnyFoodGroups) || isInBlacklistFoodGroup;
	}

	public static void sync(EntityPlayerMP player)
	{
		for (FoodGroup foodGroup : foodGroups.values())
		{
			PacketDispatcher.get().sendTo(new PacketFoodGroup(foodGroup), player);
		}
	}

	public static void setInStone()
	{
		foodToIncludedFoodGroups.clear();

		for (FoodGroup foodGroup : getFoodGroups())
		{
			foodGroup.init();
			for (Integer itemHash : foodGroup.getMatchingItemStackHashes())
			{
				if (foodToIncludedFoodGroups.get(itemHash) == null)
				{
					foodToIncludedFoodGroups.put(itemHash, new HashSet<FoodGroup>());
				}
				foodToIncludedFoodGroups.get(itemHash).add(foodGroup);
			}
		}
	}

	public static void clear()
	{
		foodGroups.clear();
		foodToIncludedFoodGroups.clear();
		hasBlacklist = false;
		PacketFoodGroup.resetCount();
	}
}
