package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.items.ItemFoodContainer;
import net.minecraftforge.fml.relauncher.Side;

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
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		ItemStack equippedItem = player.getCurrentEquippedItem();

		if (equippedItem != null && FoodHelper.isFoodContainer(equippedItem))
		{
			ItemFoodContainer foodContainerItem = ((ItemFoodContainer) equippedItem.getItem());
			foodContainerItem.setIsOpen(equippedItem, !foodContainerItem.isOpen(equippedItem));
		}

		return null;
	}
}
