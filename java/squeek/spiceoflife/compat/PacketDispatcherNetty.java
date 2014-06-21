package squeek.spiceoflife.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import squeek.spiceoflife.compat.PacketDispatcher.PacketTarget;
import squeek.spiceoflife.network.PacketBase;
import squeek.spiceoflife.network.PacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class PacketDispatcherNetty implements IPacketDispatcher
{

	@Override
	public void sendToAll(PacketBase packet)
	{
		PacketHandler.channel.sendToAll(packet);
	}

	@Override
	public void sendTo(PacketBase packet, EntityPlayerMP player)
	{
		PacketHandler.channel.sendTo(packet, player);
	}

	@Override
	public void sendToAllAround(PacketBase packet, PacketTarget packetTarget)
	{
		NetworkRegistry.TargetPoint targetPoint = new NetworkRegistry.TargetPoint(packetTarget.dimension, packetTarget.x, packetTarget.y, packetTarget.z, packetTarget.range);
		PacketHandler.channel.sendToAllAround(packet, targetPoint);
	}

	@Override
	public void sendToDimension(PacketBase packet, int dimensionId)
	{
		PacketHandler.channel.sendToDimension(packet, dimensionId);
	}

	@Override
	public void sendToServer(PacketBase packet)
	{
		PacketHandler.channel.sendToServer(packet);
	}

}
