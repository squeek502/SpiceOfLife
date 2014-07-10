package squeek.spiceoflife.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import squeek.spiceoflife.ModContent;

public class CompatHelper
{
	public static void sendPlayerHealthUpdatePacket(EntityPlayerMP player)
	{
		player.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(player.getHealth(), player.getFoodStats().getFoodLevel(), player.getFoodStats().getSaturationLevel()));
	}

	public static int deregisterItem(Item item)
	{
		Item.itemsList[ModContent.foodJournal.itemID] = null;
		return ModContent.foodJournal.itemID;
	}

	public static void reregisterItem(Item item, int id)
	{
		Item.itemsList[id] = item;
	}
}
