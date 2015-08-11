package squeek.spiceoflife.compat;

import squeek.spiceoflife.compat.PacketDispatcher.PacketTarget;
import squeek.spiceoflife.network.PacketBase;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IPacketDispatcher
{
	public void sendToAll(PacketBase packet);

	public void sendTo(PacketBase packet, EntityPlayerMP player);

	public void sendToAllAround(PacketBase packet, PacketTarget packetTarget);

	public void sendToDimension(PacketBase packet, int dimensionId);

	public void sendToServer(PacketBase packet);
}
