package squeek.spiceoflife.foodtracker;

import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.ISaveable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class FoodEaten implements IPackable, ISaveable
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

	@Override
	public void pack(IByteIO data)
	{
		data.writeShort(hungerRestored);
		data.writeUTF(foodGroup != null ? foodGroup.identifier : "");
		data.writeItemStack(itemStack);
	}

	@Override
	public void unpack(IByteIO data)
	{
		hungerRestored = data.readShort();
		foodGroup = FoodGroupRegistry.getFoodGroup(data.readUTF());
		itemStack = data.readItemStack();
	}
}
