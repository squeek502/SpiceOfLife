package squeek.spiceoflife.compat;

import java.lang.reflect.Field;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import squeek.spiceoflife.ModContent;

public class CompatHelper
{
	public static void sendPlayerHealthUpdatePacket(EntityPlayerMP player)
	{
		player.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(player.getHealth(), player.getFoodStats().getFoodLevel(), player.getFoodStats().getSaturationLevel()));
	}

	private static final Field underlyingIntegerMap = ReflectionHelper.findField(RegistryNamespaced.class, ObfuscationReflectionHelper.remapFieldNames(RegistryNamespaced.class.getName(), "underlyingIntegerMap", "field_148759_a", "a"));

	public static int deregisterItem(Item item)
	{
		int id = Item.getIdFromItem(ModContent.foodJournal);
		try
		{
			((ObjectIntIdentityMap) underlyingIntegerMap.get(Item.itemRegistry)).func_148746_a(null, id);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 0;
		}
		return id;
	}

	public static void reregisterItem(Item item, int id)
	{
		if (id > 0)
		{
			try
			{
				((ObjectIntIdentityMap) underlyingIntegerMap.get(Item.itemRegistry)).func_148746_a(ModContent.foodJournal, id);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
