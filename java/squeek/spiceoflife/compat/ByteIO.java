package squeek.spiceoflife.compat;

import io.netty.buffer.ByteBuf;

public class ByteIO
{
	public static IByteIO get()
	{
		return new ByteIONetty();
	}

	public static IByteIO get(byte[] bytes)
	{
		return new ByteIONetty(bytes);
	}

	public static IByteIO get(ByteBuf buf)
	{
		return new ByteIONetty(buf);
	}
}
