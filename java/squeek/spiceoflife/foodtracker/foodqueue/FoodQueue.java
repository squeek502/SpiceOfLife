package squeek.spiceoflife.foodtracker.foodqueue;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.ISaveable;

import java.util.LinkedList;

public abstract class FoodQueue extends LinkedList<FoodEaten> implements IPackable, ISaveable
{

	private static final long serialVersionUID = -4619224291718876433L;

	@Override
	public void writeToNBTData(NBTTagCompound data)
	{
		NBTTagList nbtHistory = new NBTTagList();
		for (FoodEaten foodEaten : this)
		{
			NBTTagCompound nbtFood = new NBTTagCompound();
			foodEaten.writeToNBTData(nbtFood);
			nbtHistory.appendTag(nbtFood);
		}
		data.setTag("Foods", nbtHistory);
	}

	@Override
	public void readFromNBTData(NBTTagCompound data)
	{
		NBTTagList nbtHistory = data.getTagList("Foods", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < nbtHistory.tagCount(); i++)
		{
			NBTTagCompound nbtFood = nbtHistory.getCompoundTagAt(i);
			FoodEaten foodEaten = FoodEaten.loadFromNBTData(nbtFood);
			add(foodEaten);
		}
	}

	@Override
	public void pack(IByteIO data)
	{
	}

	@Override
	public void unpack(IByteIO data)
	{
	}

}
