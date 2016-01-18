package squeek.spiceoflife.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import squeek.spiceoflife.compat.PacketDispatcher.PacketTarget;
import squeek.spiceoflife.network.PacketBase;

public interface IPacketDispatcher
{
	void sendToAll(PacketBase packet);

	void sendTo(PacketBase packet, EntityPlayerMP player);

	void sendToAllAround(PacketBase packet, PacketTarget packetTarget);

	void sendToDimension(PacketBase packet, int dimensionId);

	void sendToServer(PacketBase packet);
}
