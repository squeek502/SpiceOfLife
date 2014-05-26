package squeek.spiceoflife.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import squeek.spiceoflife.ModInfo;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler
{
	public static enum PacketType
	{
		ConfigSync(PacketConfigSync.class),
		FoodHistory(PacketFoodHistory.class),
		FoodEatenAllTime(PacketFoodEatenAllTime.class);

		public Class<? extends PacketBase> packet = null;

		PacketType(Class<? extends PacketBase> clazz)
		{
			packet = clazz;
		}

		public static int getIdOf(PacketBase packet)
		{
			for (PacketType packetType : PacketType.values())
			{
				if (packetType.packet.isInstance(packet))
					return packetType.ordinal();
			}
			return -1;
		}
	}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		if (packet.channel.equals(ModInfo.NETCHANNEL))
		{
			try
			{
				DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
				int id = inputStream.readByte();

				if (id >= 0 && id < PacketType.values().length)
				{
					PacketType.values()[id].packet.newInstance().unpack(inputStream, manager, (EntityPlayer) player);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

}
