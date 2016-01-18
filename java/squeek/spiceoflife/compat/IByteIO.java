package squeek.spiceoflife.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IByteIO
{

	boolean readBoolean();

	byte readByte();

	short readUnsignedByte();

	short readShort();

	int readUnsignedShort();

	int readMedium();

	int readUnsignedMedium();

	int readInt();

	long readUnsignedInt();

	long readLong();

	char readChar();

	float readFloat();

	double readDouble();

	ItemStack readItemStack();

	NBTTagCompound readTag();

	String readUTF();

	IByteIO writeBoolean(boolean value);

	IByteIO writeByte(int value);

	IByteIO writeShort(int value);

	IByteIO writeMedium(int value);

	IByteIO writeInt(int value);

	IByteIO writeLong(long value);

	IByteIO writeChar(int value);

	IByteIO writeFloat(float value);

	IByteIO writeDouble(double value);

	IByteIO writeBytes(byte[] src);

	IByteIO writeZero(int length);

	IByteIO writeItemStack(ItemStack itemStack);

	IByteIO writeTag(NBTTagCompound tag);

	IByteIO writeUTF(String str);

	byte[] bytes();

	int readableSize();

	int writableSize();

	IByteIO skipBytes(int length);

	IByteIO clear();

	@Override int hashCode();

	@Override boolean equals(Object obj);

	@Override String toString();

}