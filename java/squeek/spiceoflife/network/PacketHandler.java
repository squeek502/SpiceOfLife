package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.compat.ByteIO;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.compat.PacketDispatcher;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler implements IPacketHandler
{
	public static enum PacketType
	{
		ConfigSync(PacketConfigSync.class),
		FoodHistory(PacketFoodHistory.class),
		FoodEatenAllTime(PacketFoodEatenAllTime.class),
		FoodGroup(PacketFoodGroup.class),
		FoodExhaustion(PacketFoodExhaustion.class),
		DifficultySetting(PacketDifficultySetting.class);

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
	public void onPacketData(INetworkManager manager, Packet250CustomPayload rawPacket, Player player)
	{
		if (rawPacket.channel.equals(ModInfo.NETCHANNEL))
		{
			try
			{
				IByteIO data = ByteIO.get(rawPacket.data);
				int id = data.readByte();

				if (id >= 0 && id < PacketType.values().length)
				{
					Side side = player instanceof EntityPlayerMP ? Side.SERVER : Side.CLIENT;
					PacketBase unpackedPacket = PacketType.values()[id].packet.newInstance();
					unpackedPacket.unpack(data);
					PacketBase reply = unpackedPacket.processAndReply(side, (EntityPlayer) player);
					if (reply != null)
					{
						if (side == Side.SERVER)
							PacketDispatcher.get().sendTo(reply, (EntityPlayerMP) player);
						else
							PacketDispatcher.get().sendToServer(reply);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

}
