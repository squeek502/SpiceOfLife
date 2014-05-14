package squeek.spiceoflife.hooks;

import squeek.spiceoflife.ModSpiceOfLife;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class HookOnFoodEaten
{
	public static void onFoodEaten(ItemStack itemStack, World world, EntityPlayer player)
	{
		ModSpiceOfLife.Log.info((world.isRemote ? "[client] " : "[server] ") + player.getDisplayName() + " ate " + itemStack.getDisplayName());
	}
}
