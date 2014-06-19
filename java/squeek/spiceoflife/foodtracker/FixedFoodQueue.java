package squeek.spiceoflife.foodtracker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class FixedFoodQueue extends FixedSizeQueue<FoodEaten>
{

	private static final long serialVersionUID = -1906960830995592577L;

	public FixedFoodQueue(int limit)
	{
		super(limit);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		NBTTagList nbtHistory = new NBTTagList();
		for (FoodEaten foodEaten : this)
		{
			NBTTagCompound nbtFood = new NBTTagCompound();
			foodEaten.writeToNBTData(nbtFood);
			nbtHistory.appendTag(nbtFood);
		}
		tag.setTag("Foods", nbtHistory);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		NBTTagList nbtHistory = tag.getTagList("Foods", Constants.NBT.TAG_LIST);
		for (int i = 0; i < nbtHistory.tagCount(); i++)
		{
			NBTTagCompound nbtFood = (NBTTagCompound) nbtHistory.getCompoundTagAt(i);
			add(FoodEaten.loadFromNBTData(nbtFood));
		}
	}

	@Override
	public void pack(DataOutputStream data) throws IOException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpack(DataInputStream data) throws IOException
	{
		// TODO Auto-generated method stub
		
	}
	
}
