package squeek.spiceoflife.compat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.ByteBufUtils;

public class ByteIONetty implements IByteIO
{
	public ByteBuf buf;

	public ByteIONetty()
	{
		buf = Unpooled.buffer();
	}

	public ByteIONetty(byte[] inputBytes)
	{
		buf = Unpooled.wrappedBuffer(inputBytes);
	}
	
	public ByteIONetty(ByteBuf buf)
	{
		this.buf = buf;
	}

	@Override
	public boolean readBoolean()
	{
		return buf.readBoolean();
	}

	@Override
	public byte readByte()
	{
		return buf.readByte();
	}

	@Override
	public short readUnsignedByte()
	{
		return buf.readUnsignedByte();
	}

	@Override
	public short readShort()
	{
		return buf.readShort();
	}

	@Override
	public int readUnsignedShort()
	{
		return buf.readUnsignedShort();
	}

	@Override
	public int readMedium()
	{
		return buf.readMedium();
	}

	@Override
	public int readUnsignedMedium()
	{
		return buf.readUnsignedMedium();
	}

	@Override
	public int readInt()
	{
		return buf.readInt();
	}

	@Override
	public long readUnsignedInt()
	{
		return buf.readUnsignedInt();
	}

	@Override
	public long readLong()
	{
		return buf.readLong();
	}

	@Override
	public char readChar()
	{
		return buf.readChar();
	}

	@Override
	public float readFloat()
	{
		return buf.readFloat();
	}

	@Override
	public double readDouble()
	{
		return buf.readDouble();
	}

	@Override
	public ItemStack readItemStack()
	{
		return ByteBufUtils.readItemStack(buf);
	}

	@Override
	public NBTTagCompound readTag()
	{
		return ByteBufUtils.readTag(buf);
	}

	@Override
	public String readUTF()
	{
		return ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public IByteIO writeBoolean(boolean value)
	{
		buf.writeBoolean(value);
		return this;
	}

	@Override
	public IByteIO writeByte(int value)
	{
		buf.writeByte(value);
		return this;
	}

	@Override
	public IByteIO writeShort(int value)
	{
		buf.writeShort(value);
		return this;
	}

	@Override
	public IByteIO writeMedium(int value)
	{
		buf.writeMedium(value);
		return this;
	}

	@Override
	public IByteIO writeInt(int value)
	{
		buf.writeInt(value);
		return this;
	}

	@Override
	public IByteIO writeLong(long value)
	{
		buf.writeLong(value);
		return this;
	}

	@Override
	public IByteIO writeChar(int value)
	{
		buf.writeChar(value);
		return this;
	}

	@Override
	public IByteIO writeFloat(float value)
	{
		buf.writeFloat(value);
		return this;
	}

	@Override
	public IByteIO writeDouble(double value)
	{
		buf.writeDouble(value);
		return this;
	}

	@Override
	public IByteIO writeBytes(byte[] src)
	{
		buf.writeBytes(src);
		return this;
	}

	@Override
	public IByteIO writeZero(int length)
	{
		buf.writeZero(length);
		return this;
	}

	@Override
	public IByteIO writeItemStack(ItemStack itemStack)
	{
		ByteBufUtils.writeItemStack(buf, itemStack);
		return this;
	}

	@Override
	public IByteIO writeTag(NBTTagCompound tag)
	{
		ByteBufUtils.writeTag(buf, tag);
		return this;
	}

	@Override
	public IByteIO writeUTF(String str)
	{
		ByteBufUtils.writeUTF8String(buf, str);
		return this;
	}

	@Override
	public byte[] bytes()
	{
		return buf.array();
	}

	@Override
	public IByteIO clear()
	{
		buf.clear();
		return this;
	}

	@Override
	public IByteIO skipBytes(int length)
	{
		buf.skipBytes(length);
		return this;
	}

	@Override
	public int hashCode()
	{
		return buf.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return buf.equals(obj);
	}

	@Override
	public String toString()
	{
		return buf.toString();
	}

	@Override
	public int readableSize()
	{
		return buf.readableBytes();
	}

	@Override
	public int writableSize()
	{
		return buf.writableBytes();
	}
}
