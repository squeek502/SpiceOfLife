package squeek.spiceoflife.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S06PacketUpdateHealth;

public class CompatHelper
{
	public static void sendPlayerHealthUpdatePacket(EntityPlayerMP player)
	{
		player.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(player.getHealth(), player.getFoodStats().getFoodLevel(), player.getFoodStats().getSaturationLevel()));
	}
}
