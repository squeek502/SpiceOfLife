package squeek.spiceoflife.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import squeek.spiceoflife.ModConfig;

public class PacketConfigSync extends PacketBase
{	
	public PacketConfigSync()
	{
	}

	@Override
	public void pack(DataOutputStream data) throws IOException
	{
		ModConfig.pack(data);
	}

	@Override
	public void unpack(DataInputStream data, INetworkManager manager, EntityPlayer player) throws IOException
	{
		ModConfig.unpack(data);
	}

}
