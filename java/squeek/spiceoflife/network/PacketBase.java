package squeek.spiceoflife.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import squeek.spiceoflife.compat.ByteIO;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.IPacketProcessor;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public abstract class PacketBase implements IMessage, IPackable, IPacketProcessor
{
	public PacketBase()
	{
	}

	public Packet getPacket()
	{
		return PacketHandler.channel.getPacketFrom(this);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		unpack(ByteIO.get(buf));
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		pack(ByteIO.get(buf));
	}
}
