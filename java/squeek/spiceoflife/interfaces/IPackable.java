package squeek.spiceoflife.interfaces;

import squeek.spiceoflife.compat.IByteIO;

public interface IPackable
{
	void pack(IByteIO data);

	void unpack(IByteIO data);
}
