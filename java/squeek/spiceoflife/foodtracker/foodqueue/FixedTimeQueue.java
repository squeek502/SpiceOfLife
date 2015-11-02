package squeek.spiceoflife.foodtracker.foodqueue;

import net.minecraft.nbt.NBTTagCompound;

public class FixedTimeQueue extends FoodQueue
{
	private static final long serialVersionUID = 4071948082682614961L;
	protected long tickLimit;

	public FixedTimeQueue(long tickLimit)
	{
		this.tickLimit = tickLimit;
	}

	/**
	 * Called every update tick. See {@link squeek.spiceoflife.foodtracker.FoodTracker#onLivingUpdate}
	 */
	public void prune(long absoluteTime, long relativeTime)
	{
		while (hasHeadExpired(absoluteTime, relativeTime))
		{
			super.remove();
		}
	}

	public boolean hasHeadExpired(long absoluteTime, long relativeTime)
	{
		if (peekFirst() == null)
			return false;

		return peekFirst().elapsedTime(absoluteTime, relativeTime) >= getMaxTime();
	}

	public long getMaxTime()
	{
		return tickLimit;
	}

	@Override
	public void readFromNBTData(NBTTagCompound data)
	{
		super.readFromNBTData(data);
	}

	@Override
	public void writeToNBTData(NBTTagCompound data)
	{
		super.writeToNBTData(data);
	}
}
