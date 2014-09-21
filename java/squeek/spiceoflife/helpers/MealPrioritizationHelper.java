package squeek.spiceoflife.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.FoodValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class MealPrioritizationHelper
{
	public static class InventoryFoodInfo
	{
		public ItemStack itemStack;
		public FoodValues defaultFoodValues;
		public float diminishingReturnsModifier = 1;
		public FoodValues modifiedFoodValues;
		public int slotNum;

		public InventoryFoodInfo()
		{
		}

		public InventoryFoodInfo(int slotNum, ItemStack itemStack, EntityPlayer player)
		{
			this.itemStack = itemStack;
			this.slotNum = slotNum;
			this.defaultFoodValues = FoodValues.get(itemStack);
			if (FoodHelper.canFoodDiminish(itemStack))
			{
				this.diminishingReturnsModifier = FoodModifier.getFoodModifier(player, itemStack, player.getFoodStats(), defaultFoodValues.hunger, defaultFoodValues.saturationModifier);
				this.modifiedFoodValues = defaultFoodValues.getModified(diminishingReturnsModifier);
			}
			else
			{
				this.diminishingReturnsModifier = Float.NaN;
				this.modifiedFoodValues = defaultFoodValues;
			}
		}
	}

	public static final Comparator<InventoryFoodInfo> hungerComparator = new Comparator<InventoryFoodInfo>()
	{
		@Override
		public int compare(InventoryFoodInfo a, InventoryFoodInfo b)
		{
			return integerCompare(a.modifiedFoodValues.hunger, b.modifiedFoodValues.hunger);
		}
	};

	public static final Comparator<InventoryFoodInfo> diminishedComparator = new Comparator<InventoryFoodInfo>()
	{
		@Override
		public int compare(InventoryFoodInfo a, InventoryFoodInfo b)
		{
			int compareResult = Float.compare(b.diminishingReturnsModifier, a.diminishingReturnsModifier);
			if (compareResult == 0)
			{
				Random random = new Random();
				return integerCompare(random.nextInt(), random.nextInt());
			}
			return compareResult;
		}
	};

	public static class FoodInfoComparator implements Comparator<InventoryFoodInfo>
	{
		public int maxHungerRestored;
		public boolean ignoreHungerRemainder = false;

		public FoodInfoComparator()
		{
			ignoreHungerRemainder = true;
		}

		public FoodInfoComparator(int maxHungerRestored)
		{
			this.maxHungerRestored = maxHungerRestored;
		}

		@Override
		public int compare(InventoryFoodInfo a, InventoryFoodInfo b)
		{
			// undiminished over diminished
			int compareResult = Float.compare(b.diminishingReturnsModifier, a.diminishingReturnsModifier);
			// restore to full over leaving a remainder
			if (compareResult == 0 && !ignoreHungerRemainder)
			{
				int aRemainder = maxHungerRestored - a.modifiedFoodValues.hunger;
				int bRemainder = maxHungerRestored - b.modifiedFoodValues.hunger;
				compareResult = integerCompare(Math.abs(aRemainder), Math.abs(bRemainder));
				if (compareResult == 0 && aRemainder != bRemainder)
				{
					// too low over too high
					compareResult = bRemainder > aRemainder ? 1 : -1;
				}
			}
			// better food over worse food
			if (compareResult == 0)
				compareResult = Float.compare(b.modifiedFoodValues.saturationModifier * b.modifiedFoodValues.hunger, a.modifiedFoodValues.saturationModifier * a.modifiedFoodValues.hunger);

			return compareResult;
		}
	}

	public static int findBestFoodForPlayerToEat(EntityPlayer player, IInventory inventory)
	{
		List<InventoryFoodInfo> allFoodInfo = getFoodInfoFromInventoryForPlayer(player, inventory);
		InventoryFoodInfo bestFoodInfo = null;

		if (allFoodInfo.size() > 0)
		{
			int hungerMissingFromPlayer = 20 - player.getFoodStats().getFoodLevel();
			Collections.sort(allFoodInfo, new FoodInfoComparator(hungerMissingFromPlayer));
			bestFoodInfo = allFoodInfo.get(0);
		}

		return bestFoodInfo != null ? bestFoodInfo.slotNum : 0;
	}

	public static List<InventoryFoodInfo> findBestFoodsForPlayerAccountingForVariety(EntityPlayer player, IInventory inventory)
	{
		List<InventoryFoodInfo> allFoodInfo = getFoodInfoFromInventoryForPlayer(player, inventory);
		Collections.sort(allFoodInfo, diminishedComparator);
		return allFoodInfo;
	}

	public static List<InventoryFoodInfo> findBestFoodsForPlayerAccountingForVariety(EntityPlayer player, IInventory inventory, int limit)
	{
		List<InventoryFoodInfo> bestFoods = findBestFoodsForPlayerAccountingForVariety(player, inventory);
		if (bestFoods.size() > limit)
			bestFoods = bestFoods.subList(0, limit);
		return bestFoods;
	}

	public static List<List<InventoryFoodInfo>> stratifyFoodsByHunger(List<InventoryFoodInfo> allFoods)
	{
		List<List<InventoryFoodInfo>> stratifiedFoods = new ArrayList<List<InventoryFoodInfo>>();
		if (allFoods.size() > 0)
		{
			Collections.sort(allFoods, hungerComparator);
			int strataHunger = allFoods.get(0).modifiedFoodValues.hunger;
			List<InventoryFoodInfo> currentStrata = new ArrayList<InventoryFoodInfo>();
			for (InventoryFoodInfo foodInfo : allFoods)
			{
				if (foodInfo.modifiedFoodValues.hunger != strataHunger)
				{
					stratifiedFoods.add(currentStrata);
					currentStrata = new ArrayList<InventoryFoodInfo>();
					strataHunger = foodInfo.modifiedFoodValues.hunger;
				}
				currentStrata.add(foodInfo);
			}
			stratifiedFoods.add(currentStrata);
		}
		return stratifiedFoods;
	}

	public static List<InventoryFoodInfo> getFoodInfoFromInventoryForPlayer(EntityPlayer player, IInventory inventory)
	{
		List<InventoryFoodInfo> foodInfo = new ArrayList<InventoryFoodInfo>();

		for (int slotNum = 0; slotNum < inventory.getSizeInventory(); slotNum++)
		{
			ItemStack stackInSlot = inventory.getStackInSlot(slotNum);
			if (stackInSlot == null)
				continue;
			if (FoodHelper.isFood(stackInSlot))
				foodInfo.add(new InventoryFoodInfo(slotNum, stackInSlot, player));
		}

		return foodInfo;
	}

	private static int integerCompare(int a, int b)
	{
		return (a < b) ? -1 : ((a == b) ? 0 : 1);
	}
}
