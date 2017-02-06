package squeek.spiceoflife.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.spiceoflife.items.ItemFoodContainer;

import javax.annotation.Nonnull;

public class FoodContainerInventory extends NBTInventory
{
	protected ItemFoodContainer itemFoodContainer;
	@Nonnull protected ItemStack itemStackFoodContainer = ItemStack.EMPTY;

	public FoodContainerInventory(ItemFoodContainer itemFoodContainer, @Nonnull ItemStack itemStackFoodContainer)
	{
		super(itemFoodContainer);
		this.itemFoodContainer = itemFoodContainer;
		this.itemStackFoodContainer = itemStackFoodContainer;

		readFromNBTData(itemFoodContainer.getInventoryTag(itemStackFoodContainer));
	}

	@Override
	public void onInventoryChanged()
	{
		// the itemstack on the client can change, so make sure we always get the
		// new itemstack when making changes to the nbt tag
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			findMatchingClientItemStack();
		}

		this.writeToNBTData(itemFoodContainer.getInventoryTag(itemStackFoodContainer));

		super.onInventoryChanged();
	}

	@SideOnly(Side.CLIENT)
	public void findMatchingClientItemStack()
	{
		EntityPlayer player = FMLClientHandler.instance().getClient().player;
		if (player.openContainer != null && player.openContainer instanceof ContainerFoodContainer)
		{
			ContainerFoodContainer openFoodContainer = (ContainerFoodContainer) player.openContainer;
			ItemStack matchingFoodContainer = openFoodContainer.findFoodContainerWithUUID(itemFoodContainer.getUUID(itemStackFoodContainer));
			if (matchingFoodContainer != ItemStack.EMPTY)
				itemStackFoodContainer = matchingFoodContainer;
		}
	}
}
