package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.items.ItemFoodContainer;

public class PacketToggleFoodContainer extends PacketBase
{
	public PacketToggleFoodContainer()
	{
	}

	@Override
	public void pack(IByteIO data)
	{
	}

	@Override
	public void unpack(IByteIO data)
	{
	}

	@Override
	public void processInWorldThread(Side side, EntityPlayer player)
	{
		ItemStack equippedItem = player.getHeldItemMainhand();

		if (equippedItem != null && FoodHelper.isFoodContainer(equippedItem))
		{
			ItemFoodContainer foodContainerItem = ((ItemFoodContainer) equippedItem.getItem());
			foodContainerItem.setIsOpen(equippedItem, !foodContainerItem.isOpen(equippedItem));
		}
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		return null;
	}
}
