package squeek.spiceoflife.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import squeek.spiceoflife.ModInfo;

public abstract class PacketBase
{
	public PacketBase()
	{
	}

	public PacketBase(byte[] data)
	{
		this(new DataInputStream(new ByteArrayInputStream(data)), null, null);
	}

	public PacketBase(DataInputStream data, INetworkManager manager, EntityPlayer player)
	{
		try
		{
			unpack(data, manager, player);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void header(DataOutputStream data) throws IOException
	{
		data.writeByte(PacketHandler.PacketType.getIdOf(this));
	}

	public abstract void pack(DataOutputStream data) throws IOException;

	public abstract void unpack(DataInputStream data, INetworkManager manager, EntityPlayer player) throws IOException;

	public Packet getPacket()
	{
		Packet250CustomPayload packet = new Packet250CustomPayload();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(bos);

		try
		{
			header(outputStream);
			pack(outputStream);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		packet.channel = ModInfo.NETCHANNEL;
		packet.data = bos.toByteArray();
		packet.length = bos.size();

		return packet;
	}
}
