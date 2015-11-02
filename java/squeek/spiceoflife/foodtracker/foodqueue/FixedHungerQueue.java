package squeek.spiceoflife.foodtracker.foodqueue;

import squeek.spiceoflife.foodtracker.FoodEaten;

public class FixedHungerQueue extends FixedSizeQueue
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
			hunger += foodEaten.foodValues.hunger;
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

	public int totalHunger()
	{
		return hunger + hungerOverflow;
	}

	public FixedHungerQueue sliceUntil(FoodEaten target)
	{
		FixedHungerQueue slice = new FixedHungerQueue(limit);
		for (FoodEaten foodEaten : this)
		{
			if (target.equals(foodEaten))
				break;

			slice.add(foodEaten);
		}
		return slice;
	}

	@Override
	protected void trimToMaxSize()
	{
		while (hunger > limit && peekFirst() != null)
		{
			hunger -= 1;
			hungerOverflow += 1;

			while (hungerOverflow >= peekFirst().foodValues.hunger)
			{
				hungerOverflow -= removeFirst().foodValues.hunger;
			}
		}
	}
}
