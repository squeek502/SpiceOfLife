package squeek.spiceoflife.compat;

import squeek.spiceoflife.compat.PacketDispatcher.PacketTarget;
import squeek.spiceoflife.network.PacketBase;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IPacketDispatcher
{
	void sendToAll(PacketBase packet);

	void sendTo(PacketBase packet, EntityPlayerMP player);

	void sendToAllAround(PacketBase packet, PacketTarget packetTarget);

	void sendToDimension(PacketBase packet, int dimensionId);

	void sendToServer(PacketBase packet);
}
