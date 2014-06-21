package squeek.spiceoflife.foodtracker;

import java.util.LinkedList;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.ISaveable;

public abstract class FixedSizeQueue<E> extends LinkedList<E> implements IPackable, ISaveable
{

	private static final long serialVersionUID = 2666900280639735575L;
	protected int limit;

	public FixedSizeQueue(int limit)
	{
		setMaxSize(limit);
	}

	@Override
	public boolean add(E o)
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