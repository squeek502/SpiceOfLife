package squeek.spiceoflife.foodtracker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.nbt.NBTTagCompound;

public class FixedHungerQueue extends FixedFoodQueue
{
	private static final long serialVersionUID = -1347372098150405272L;
	protected int hunger;
	protected int hungerOverflow;

	public FixedHungerQueue(int limit)
	{
		super(limit);
	}
	
	@Override
	public boolean add(FoodEaten foodEaten)
	{
		boolean added = super.add(foodEaten);
		if (added)
		{
			hunger += foodEaten.hungerRestored;
			trimToMaxSize();
		}
		return added;
	}
	
	@Override
	public void clear()
	{
		super.clear();
		hunger = hungerOverflow = 0;
	}
	
	public int hunger()
	{
		return hunger;
	}
	
	@Override
	protected void trimToMaxSize()
	{
		while (hunger > limit)
		{
			hunger -= 1;
			hungerOverflow += 1;
			
			while (hungerOverflow >= peekFirst().hungerRestored)
			{
				hungerOverflow -= removeFirst().hungerRestored;
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
	}

	@Override
	public void pack(DataOutputStream data) throws IOException
	{
		super.pack(data);
	}

	@Override
	public void unpack(DataInputStream data) throws IOException
	{
		super.unpack(data);
	}
}
