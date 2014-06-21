package squeek.spiceoflife.compat;

public class ByteIO
{
	public static IByteIO get()
	{
		return new ByteIOStream();
	}

	public static IByteIO get(byte[] bytes)
	{
		return new ByteIOStream(bytes);
	}
}
