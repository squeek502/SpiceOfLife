package squeek.spiceoflife.network;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.compat.ByteIO;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.IPacketProcessor;

public abstract class PacketBase implements IPackable, IPacketProcessor
{
	public PacketBase()
	{
	}

	public void header(IByteIO data)
	{
		data.writeByte(PacketHandler.PacketType.getIdOf(this));
	}

	public Packet getPacket()
	{
		Packet250CustomPayload packet = new Packet250CustomPayload();

		IByteIO data = ByteIO.get();

		try
		{
			header(data);
			pack(data);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		packet.channel = ModInfo.NETCHANNEL;
		packet.data = data.bytes();
		packet.length = data.writableSize();

		return packet;
	}
}
