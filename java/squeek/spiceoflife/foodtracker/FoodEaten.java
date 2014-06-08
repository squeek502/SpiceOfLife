package squeek.spiceoflife.foodtracker;

import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class FoodEaten
{
	public int hungerRestored = 0;
	public ItemStack itemStack = null;
	public FoodGroup foodGroup = null;
	
	public FoodEaten()
	{
	}

	public FoodEaten(ItemStack food)
	{
		this.itemStack = food;
	}

	public void writeToNBTData(NBTTagCompound nbtFood)
	{
		itemStack.writeToNBT(nbtFood);
		if (hungerRestored != 0)
			nbtFood.setShort("Hunger", (short) hungerRestored);
	}
	
	public void readFromNBTData(NBTTagCompound nbtFood)
	{
		itemStack = ItemStack.loadItemStackFromNBT(nbtFood);
		hungerRestored = nbtFood.getShort("Hunger");
		foodGroup = FoodGroupRegistry.getFoodGroupForFood(itemStack);
	}

	public static FoodEaten loadFromNBTData(NBTTagCompound nbtFood)
	{
		FoodEaten foodEaten = new FoodEaten();
		foodEaten.readFromNBTData(nbtFood);
		return foodEaten;
	}
}
