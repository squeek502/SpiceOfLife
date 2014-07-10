package squeek.spiceoflife.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet8UpdateHealth;
import squeek.spiceoflife.ModContent;
import cpw.mods.fml.common.network.Player;

public class CompatHelper
{
	public static void sendPlayerHealthUpdatePacket(EntityPlayerMP player)
	{
		cpw.mods.fml.common.network.PacketDispatcher.sendPacketToPlayer(new Packet8UpdateHealth(player.getHealth(), player.getFoodStats().getFoodLevel(), player.getFoodStats().getSaturationLevel()), (Player) player);
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
