package squeek.spiceoflife.foodtracker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import net.minecraft.nbt.NBTTagCompound;

public abstract class FixedSizeQueue<E> extends LinkedList<E>
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
	
	public abstract void writeToNBT(NBTTagCompound tag);
	public abstract void readFromNBT(NBTTagCompound tag);
	public abstract void pack(DataOutputStream data) throws IOException;
	public abstract void unpack(DataInputStream data) throws IOException;

}