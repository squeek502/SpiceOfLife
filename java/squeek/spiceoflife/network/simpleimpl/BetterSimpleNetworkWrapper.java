package squeek.spiceoflife.network.simpleimpl;

import io.netty.channel.ChannelFutureListener;
import java.util.EnumMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleIndexedCodec;
import cpw.mods.fml.relauncher.Side;

/**
 * Exact copy of FML's SimpleNetworkWrapper implementation with added support for message handlers handling multiple message types
 * See FML's SimpleNetworkWrapper for general documentation
 */
public class BetterSimpleNetworkWrapper
{
	private EnumMap<Side, FMLEmbeddedChannel> channels;
	private SimpleIndexedCodec packetCodec;

	public BetterSimpleNetworkWrapper(String channelName)
	{
		packetCodec = new SimpleIndexedCodec();
		channels = NetworkRegistry.INSTANCE.newChannel(channelName, packetCodec);
	}

	/**
	 * Register a message and it's associated handler. The message will have the supplied discriminator byte. The message handler will
	 * be registered on the supplied side (this is the side where you want the message to be processed and acted upon).
	 *
	 * @param messageHandler the message handler type
	 * @param messageType the message type
	 * @param discriminator a discriminator byte
	 * @param side the side for the handler
	 */
	public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<? extends REQ> messageType, int discriminator, Side side)
	{
		packetCodec.addDiscriminator(discriminator, messageType);
		FMLEmbeddedChannel channel = channels.get(side);
		String type = channel.findChannelHandlerNameForType(SimpleIndexedCodec.class);
		if (side == Side.SERVER)
		{
			addServerHandlerAfter(channel, type, messageHandler, messageType);
		}
		else
		{
			addClientHandlerAfter(channel, type, messageHandler, messageType);
		}
	}

	private <REQ extends IMessage, REPLY extends IMessage, NH extends INetHandler> void addServerHandlerAfter(FMLEmbeddedChannel channel, String type, Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<? extends REQ> messageType)
	{
		BetterSimpleChannelHandlerWrapper<REQ, REPLY> handler = getHandlerWrapper(messageHandler, Side.SERVER, messageType);
		channel.pipeline().addAfter(type, messageHandler.getName() + messageType.getName() + Side.SERVER.name(), handler);
	}

	private <REQ extends IMessage, REPLY extends IMessage, NH extends INetHandler> void addClientHandlerAfter(FMLEmbeddedChannel channel, String type, Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<? extends REQ> messageType)
	{
		BetterSimpleChannelHandlerWrapper<REQ, REPLY> handler = getHandlerWrapper(messageHandler, Side.CLIENT, messageType);
		channel.pipeline().addAfter(type, messageHandler.getName() + messageType.getName() + Side.CLIENT.name(), handler);
	}

	private <REPLY extends IMessage, REQ extends IMessage> BetterSimpleChannelHandlerWrapper<REQ, REPLY> getHandlerWrapper(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Side side, Class<? extends REQ> messageType)
	{
		return new BetterSimpleChannelHandlerWrapper<REQ, REPLY>(messageHandler, side, messageType);
	}

	/**
	 * Construct a minecraft packet from the supplied message. Can be used where minecraft packets are required.
	 *
	 * @param message The message to translate into packet form
	 * @return A minecraft {@link Packet} suitable for use in minecraft APIs
	 */
	public Packet getPacketFrom(IMessage message)
	{
		return channels.get(Side.SERVER).generatePacketFrom(message);
	}

	/**
	 * Send this message to everyone.
	 * The {@link IMessageHandler} for this message type should be on the CLIENT side.
	 *
	 * @param message The message to send
	 */
	public void sendToAll(IMessage message)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	/**
	 * Send this message to the specified player.
	 * The {@link IMessageHandler} for this message type should be on the CLIENT side.
	 *
	 * @param message The message to send
	 * @param player The player to send it to
	 */
	public void sendTo(IMessage message, EntityPlayerMP player)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	/**
	 * Send this message to everyone within a certain range of a point.
	 * The {@link IMessageHandler} for this message type should be on the CLIENT side.
	 *
	 * @param message The message to send
	 * @param point The {@link TargetPoint} around which to send
	 */
	public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
		channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	/**
	 * Send this message to everyone within the supplied dimension.
	 * The {@link IMessageHandler} for this message type should be on the CLIENT side.
	 *
	 * @param message The message to send
	 * @param dimensionId The dimension id to target
	 */
	public void sendToDimension(IMessage message, int dimensionId)
	{
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
		channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	/**
	 * Send this message to the server.
	 * The {@link IMessageHandler} for this message type should be on the SERVER side.
	 *
	 * @param message The message to send
	 */
	public void sendToServer(IMessage message)
	{
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}
}
