package squeek.spiceoflife.compat;

import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet8UpdateHealth;

public class CompatHelper
{
	public static void sendPlayerHealthUpdatePacket(EntityPlayerMP player)
	{
		cpw.mods.fml.common.network.PacketDispatcher.sendPacketToPlayer(new Packet8UpdateHealth(player.getHealth(), player.getFoodStats().getFoodLevel(), player.getFoodStats().getSaturationLevel()), (Player) player);
	}
}
