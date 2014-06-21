package squeek.spiceoflife.compat;

import java.io.*;
import java.lang.reflect.Method;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;

public class ByteIOStream implements IByteIO
{
	public DataInputStream in = null;
	public DataOutputStream out = null;
	public ByteArrayOutputStream bos = null;
	public ByteArrayInputStream bis = null;

	public ByteIOStream()
	{
		bos = new ByteArrayOutputStream();
		out = new DataOutputStream(bos);
	}

	public ByteIOStream(byte[] inputBytes)
	{
		bis = new ByteArrayInputStream(inputBytes);
		in = new DataInputStream(bis);
	}

	@Override
	public boolean readBoolean()
	{
		try
		{
			return in.readBoolean();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public byte readByte()
	{
		try
		{
			return in.readByte();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public short readUnsignedByte()
	{
		try
		{
			return (short) in.readUnsignedByte();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public short readShort()
	{
		try
		{
			return in.readShort();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int readUnsignedShort()
	{
		try
		{
			return in.readUnsignedShort();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int readMedium()
	{
		try
		{
			return in.readInt();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int readUnsignedMedium()
	{
		try
		{
			return in.readInt();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int readInt()
	{
		try
		{
			return in.readInt();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public long readUnsignedInt()
	{
		try
		{
			return in.readInt();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public long readLong()
	{
		try
		{
			return in.readLong();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public char readChar()
	{
		try
		{
			return in.readChar();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public float readFloat()
	{
		try
		{
			return in.readFloat();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public double readDouble()
	{
		try
		{
			return in.readDouble();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public ItemStack readItemStack()
	{
		try
		{
			return Packet.readItemStack(in);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public NBTTagCompound readTag()
	{
		try
		{
			return Packet.readNBTTagCompound(in);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String readUTF()
	{
		try
		{
			return in.readUTF();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IByteIO writeBoolean(boolean value)
	{
		try
		{
			out.writeBoolean(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeByte(int value)
	{
		try
		{
			out.writeByte(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeShort(int value)
	{
		try
		{
			out.writeShort(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeMedium(int value)
	{
		try
		{
			out.writeInt(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeInt(int value)
	{
		try
		{
			out.writeInt(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeLong(long value)
	{
		try
		{
			out.writeLong(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeChar(int value)
	{
		try
		{
			out.writeChar(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeFloat(float value)
	{
		try
		{
			out.writeFloat(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeDouble(double value)
	{
		try
		{
			out.writeDouble(value);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeBytes(byte[] src)
	{
		try
		{
			out.write(src);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	@Deprecated
	public IByteIO writeZero(int length)
	{
		return this;
	}

	@Override
	public IByteIO writeItemStack(ItemStack itemStack)
	{
		try
		{
			Packet.writeItemStack(itemStack, out);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public IByteIO writeTag(NBTTagCompound tag)
	{
		Method writeNBTTagCompound = ReflectionHelper.findMethod(Packet.class, null, new String[] {"writeNBTTagCompound", "func_73275_a", "a"}, NBTTagCompound.class, DataOutput.class);
		if (writeNBTTagCompound != null)
		{
			writeNBTTagCompound.setAccessible(true);
			try
			{
				writeNBTTagCompound.invoke(null, tag, out);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return this;
	}

	@Override
	public IByteIO writeUTF(String str)
	{
		try
		{
			out.writeUTF(str);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public byte[] bytes()
	{
		if (in != null)
		{
			try
			{
				byte[] bytes = new byte[in.available()];
				in.readFully(bytes);
				return bytes;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		if (out != null)
		{
			return bos.toByteArray();
		}
		
		return new byte[0];
	}

	@Override
	public IByteIO skipBytes(int length)
	{
		if (in != null)
		{
			try
			{
				in.skip(length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return this;
	}

	@Override
	@Deprecated
	public IByteIO clear()
	{
		return this;
	}

	@Override
	public int readableSize()
	{
		try
		{
			return in.available();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int writableSize()
	{
		return out.size();
	}

}
