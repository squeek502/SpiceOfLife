package squeek.spiceoflife.network;

import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.network.simpleimpl.BetterSimpleNetworkWrapper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler implements IMessageHandler<PacketBase, PacketBase>
{
	public static final BetterSimpleNetworkWrapper channel = new BetterSimpleNetworkWrapper(ModInfo.NETCHANNEL);
	
	public static enum PacketType
	{
		ConfigSync(PacketConfigSync.class),
		FoodHistory(PacketFoodHistory.class),
		FoodEatenAllTime(PacketFoodEatenAllTime.class),
		FoodGroup(PacketFoodGroup.class);

		public Class<? extends PacketBase> packet = null;

		PacketType(Class<? extends PacketBase> clazz)
		{
			this(clazz, Side.CLIENT);
		}
		
		PacketType(Class<? extends PacketBase> clazz, Side side)
		{
			packet = clazz;
			channel.registerMessage(PacketHandler.class, clazz, ordinal(), side);
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
	public PacketBase onMessage(PacketBase message, MessageContext ctx)
	{
		return message.processAndReply(ctx.side, ctx.side == Side.SERVER ? ctx.getServerHandler().playerEntity : FMLClientHandler.instance().getClientPlayerEntity());
	}

}
