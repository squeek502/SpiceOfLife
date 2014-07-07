package squeek.spiceoflife.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IByteIO
{

	public abstract boolean readBoolean();

	public abstract byte readByte();

	public abstract short readUnsignedByte();

	public abstract short readShort();

	public abstract int readUnsignedShort();

	public abstract int readMedium();

	public abstract int readUnsignedMedium();

	public abstract int readInt();

	public abstract long readUnsignedInt();

	public abstract long readLong();

	public abstract char readChar();

	public abstract float readFloat();

	public abstract double readDouble();

	public abstract ItemStack readItemStack();

	public abstract NBTTagCompound readTag();

	public abstract String readUTF();

	public abstract IByteIO writeBoolean(boolean value);

	public abstract IByteIO writeByte(int value);

	public abstract IByteIO writeShort(int value);

	public abstract IByteIO writeMedium(int value);

	public abstract IByteIO writeInt(int value);

	public abstract IByteIO writeLong(long value);

	public abstract IByteIO writeChar(int value);

	public abstract IByteIO writeFloat(float value);

	public abstract IByteIO writeDouble(double value);

	public abstract IByteIO writeBytes(byte[] src);

	public abstract IByteIO writeZero(int length);

	public abstract IByteIO writeItemStack(ItemStack itemStack);

	public abstract IByteIO writeTag(NBTTagCompound tag);

	public abstract IByteIO writeUTF(String str);

	public abstract byte[] bytes();

	public abstract int readableSize();

	public abstract int writableSize();

	public abstract IByteIO skipBytes(int length);

	public abstract IByteIO clear();

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract String toString();

}