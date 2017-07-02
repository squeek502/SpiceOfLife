package squeek.spiceoflife.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.foodtracker.FoodModifier;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MealPrioritizationHelper
{
	public static class InventoryFoodInfo
	{
		@Nonnull
		public ItemStack itemStack = ItemStack.EMPTY;
		public FoodValues defaultFoodValues;
		public float diminishingReturnsModifier = 1;
		public FoodValues modifiedFoodValues;
		public int slotNum;

		public InventoryFoodInfo()
		{
		}

		public InventoryFoodInfo(int slotNum, @Nonnull ItemStack itemStack, EntityPlayer player)
		{
			this.itemStack = itemStack;
			this.slotNum = slotNum;
			this.defaultFoodValues = FoodValues.get(this.itemStack);
			if (FoodHelper.canFoodDiminish(this.itemStack))
			{
				this.diminishingReturnsModifier = FoodModifier.getFoodModifier(player, itemStack);
				this.modifiedFoodValues = FoodModifier.getModifiedFoodValues(defaultFoodValues, diminishingReturnsModifier);
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
			return Float.compare(b.diminishingReturnsModifier, a.diminishingReturnsModifier);
		}
	};

	public static class FoodInfoComparator implements Comparator<InventoryFoodInfo>, Serializable
	{
		private static final long serialVersionUID = -2142369827782900207L;
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

	public static int findBestFoodForPlayerToEat(EntityPlayer player, IItemHandler inventory)
	{
		List<InventoryFoodInfo> allFoodInfo = getFoodInfoFromInventoryForPlayer(player, inventory);
		InventoryFoodInfo bestFoodInfo = null;

		if (!allFoodInfo.isEmpty())
		{
			int hungerMissingFromPlayer = 20 - player.getFoodStats().getFoodLevel();
			Collections.sort(allFoodInfo, new FoodInfoComparator(hungerMissingFromPlayer));
			bestFoodInfo = allFoodInfo.get(0);
		}

		return bestFoodInfo != null ? bestFoodInfo.slotNum : 0;
	}

	public static List<InventoryFoodInfo> findBestFoodsForPlayerAccountingForVariety(EntityPlayer player, IItemHandler inventory)
	{
		List<InventoryFoodInfo> allFoodInfo = getFoodInfoFromInventoryForPlayer(player, inventory);
		Collections.shuffle(allFoodInfo);
		Collections.sort(allFoodInfo, diminishedComparator);
		return allFoodInfo;
	}

	public static List<InventoryFoodInfo> findBestFoodsForPlayerAccountingForVariety(EntityPlayer player, IItemHandler inventory, int limit)
	{
		List<InventoryFoodInfo> bestFoods = findBestFoodsForPlayerAccountingForVariety(player, inventory);
		if (bestFoods.size() > limit)
			bestFoods = bestFoods.subList(0, limit);
		return bestFoods;
	}

	public static List<List<InventoryFoodInfo>> stratifyFoodsByHunger(List<InventoryFoodInfo> allFoods)
	{
		List<List<InventoryFoodInfo>> stratifiedFoods = new ArrayList<List<InventoryFoodInfo>>();
		if (!allFoods.isEmpty())
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

	public static List<InventoryFoodInfo> getFoodInfoFromInventoryForPlayer(EntityPlayer player, IItemHandler inventory)
	{
		List<InventoryFoodInfo> foodInfo = new ArrayList<InventoryFoodInfo>();

		for (int slotNum = 0; slotNum < inventory.getSlots(); slotNum++)
		{
			ItemStack stackInSlot = inventory.getStackInSlot(slotNum);
			if (stackInSlot.isEmpty())
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
