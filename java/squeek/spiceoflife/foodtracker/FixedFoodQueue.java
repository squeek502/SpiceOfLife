package squeek.spiceoflife.foodtracker;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import squeek.spiceoflife.compat.IByteIO;
import net.minecraftforge.common.util.Constants;

public class FixedFoodQueue extends FixedSizeQueue<FoodEaten>
{

	private static final long serialVersionUID = -1906960830995592577L;

	public FixedFoodQueue(int limit)
	{
		super(limit);
	}

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
			NBTTagCompound nbtFood = (NBTTagCompound) nbtHistory.getCompoundTagAt(i);
			FoodEaten foodEaten = FoodEaten.loadFromNBTData(nbtFood);
			if (foodEaten != null)
				add(foodEaten);
		}
	}

	@Override
	public void pack(IByteIO data)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpack(IByteIO data)
	{
		// TODO Auto-generated method stub
		
	}
	
}
