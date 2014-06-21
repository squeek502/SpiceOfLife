package squeek.spiceoflife.compat;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayerMP;
import squeek.spiceoflife.compat.PacketDispatcher.PacketTarget;
import squeek.spiceoflife.network.PacketBase;

public class PacketDispatcherFML implements IPacketDispatcher
{

	@Override
	public void sendToAll(PacketBase packet)
	{
		PacketDispatcher.sendPacketToAllPlayers(packet.getPacket());
	}

	@Override
	public void sendTo(PacketBase packet, EntityPlayerMP player)
	{
		PacketDispatcher.sendPacketToPlayer(packet.getPacket(), (Player) player);
	}

	@Override
	public void sendToAllAround(PacketBase packet, PacketTarget packetTarget)
	{
		PacketDispatcher.sendPacketToAllAround(packetTarget.x, packetTarget.y, packetTarget.z, packetTarget.range, packetTarget.dimension, packet.getPacket());
	}

	@Override
	public void sendToDimension(PacketBase packet, int dimensionId)
	{
		PacketDispatcher.sendPacketToAllInDimension(packet.getPacket(), dimensionId);
	}

	@Override
	public void sendToServer(PacketBase packet)
	{
		PacketDispatcher.sendPacketToServer(packet.getPacket());
	}

}
