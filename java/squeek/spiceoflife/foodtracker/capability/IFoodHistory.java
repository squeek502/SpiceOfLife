package squeek.spiceoflife.foodtracker.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodqueue.FoodQueue;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.ISaveable;

import java.util.Set;

public interface IFoodHistory extends ISaveable, IPackable, INBTSerializable<NBTTagCompound>
{
	FoodQueue getHistory();
	void reset();
	void validate();
	void onHistoryTypeChanged();
	boolean addFood(FoodEaten foodEaten);
	boolean addFood(FoodEaten foodEaten, boolean countsTowardsAllTime);
	void deltaTicksActive(long delta);
	int getHistoryLengthInRelevantUnits();
	FoodEaten getLastEatenFood();
	Set<FoodGroup> getDistinctFoodGroups();
	int getFoodCountIgnoringFoodGroups(ItemStack food);
	int getFoodCountForFoodGroup(ItemStack food, FoodGroup foodGroup);
	FoodValues getTotalFoodValuesIgnoringFoodGroups(ItemStack food);
	FoodValues getTotalFoodValuesForFoodGroup(ItemStack food, FoodGroup foodGroup);
	boolean containsFoodOrItsFoodGroups(ItemStack food);
}
