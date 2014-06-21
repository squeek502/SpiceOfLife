package squeek.spiceoflife.interfaces;

import squeek.spiceoflife.compat.IByteIO;

public interface IPackable
{
	public abstract void pack(IByteIO data);

	public abstract void unpack(IByteIO data);
}
