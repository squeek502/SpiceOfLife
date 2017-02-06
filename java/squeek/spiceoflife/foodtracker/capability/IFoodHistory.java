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

import javax.annotation.Nonnull;
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
	int getFoodCountIgnoringFoodGroups(@Nonnull ItemStack food);
	int getFoodCountForFoodGroup(@Nonnull ItemStack food, FoodGroup foodGroup);
	FoodValues getTotalFoodValuesIgnoringFoodGroups(@Nonnull ItemStack food);
	FoodValues getTotalFoodValuesForFoodGroup(@Nonnull ItemStack food, FoodGroup foodGroup);
	boolean containsFoodOrItsFoodGroups(@Nonnull ItemStack food);
}
