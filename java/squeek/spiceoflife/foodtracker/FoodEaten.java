package squeek.spiceoflife.foodtracker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.ISaveable;

import java.util.Set;

public class FoodEaten implements IPackable, ISaveable
{
	public FoodValues foodValues = FoodEaten.dummyFoodValues;
	public ItemStack itemStack = null;
	public long worldTimeEaten = 0;
	public long playerTimeEaten = 0;

	public static final FoodValues dummyFoodValues = new FoodValues(0, 0.0f);

	public FoodEaten()
	{
	}

	public FoodEaten(ItemStack food, EntityPlayer eater)
	{
		this.itemStack = food;
		this.playerTimeEaten = FoodHistory.get(eater).ticksActive;
		this.worldTimeEaten = eater.getEntityWorld().getTotalWorldTime();
	}

	public Set<FoodGroup> getFoodGroups()
	{
		return FoodGroupRegistry.getFoodGroupsForFood(itemStack);
	}

	public long elapsedTime(long absoluteTime, long relativeTime)
	{
		if (ModConfig.PROGRESS_TIME_WHILE_LOGGED_OFF)
			return absoluteTime - worldTimeEaten;
		else
			return relativeTime - playerTimeEaten;
	}

	@Override
	public void writeToNBTData(NBTTagCompound nbtFood)
	{
		if (itemStack != null)
			itemStack.writeToNBT(nbtFood);
		if (foodValues != null && foodValues.hunger != 0)
			nbtFood.setShort("Hunger", (short) foodValues.hunger);
		if (foodValues != null && foodValues.saturationModifier != 0)
			nbtFood.setFloat("Saturation", foodValues.saturationModifier);
		if (worldTimeEaten != 0)
			nbtFood.setLong("WorldTime", worldTimeEaten);
		if (playerTimeEaten != 0)
			nbtFood.setLong("PlayerTime", playerTimeEaten);
	}

	@Override
	public void readFromNBTData(NBTTagCompound nbtFood)
	{
		itemStack = ItemStack.loadItemStackFromNBT(nbtFood);
		foodValues = new FoodValues(nbtFood.getShort("Hunger"), nbtFood.getFloat("Saturation"));
		worldTimeEaten = nbtFood.getLong("WorldTime");
		playerTimeEaten = nbtFood.getLong("PlayerTime");
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
		data.writeShort(foodValues != null ? foodValues.hunger : 0);
		data.writeFloat(foodValues != null ? foodValues.saturationModifier : 0);
		data.writeItemStack(itemStack);
		data.writeLong(worldTimeEaten);
		data.writeLong(playerTimeEaten);
	}

	@Override
	public void unpack(IByteIO data)
	{
		int hunger = data.readShort();
		float saturationModifier = data.readFloat();
		foodValues = new FoodValues(hunger, saturationModifier);
		itemStack = data.readItemStack();
		worldTimeEaten = data.readLong();
		playerTimeEaten = data.readLong();
	}
}
