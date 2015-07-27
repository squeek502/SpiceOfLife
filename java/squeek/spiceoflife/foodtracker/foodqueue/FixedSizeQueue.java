package squeek.spiceoflife.foodtracker.foodqueue;

import squeek.spiceoflife.foodtracker.FoodEaten;

public class FixedSizeQueue extends FoodQueue
{
	private static final long serialVersionUID = 2666900280639735575L;
	protected int limit;

	public FixedSizeQueue(int limit)
	{
		setMaxSize(limit);
	}

	@Override
	public boolean add(FoodEaten o)
	{
		boolean added = super.add(o);
		if (added)
			trimToMaxSize();
		return added;
	}

	public int getMaxSize()
	{
		return limit;
	}

	public void setMaxSize(int limit)
	{
		this.limit = limit;
		trimToMaxSize();
	}

	protected void trimToMaxSize()
	{
		while (size() > limit)
		{
			super.remove();
		}
	}

}