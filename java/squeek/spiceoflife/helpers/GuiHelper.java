package squeek.spiceoflife.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.gui.GuiFoodContainer;
import squeek.spiceoflife.inventory.ContainerFoodContainer;
import squeek.spiceoflife.inventory.FoodContainerInventory;
import squeek.spiceoflife.items.ItemFoodContainer;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class GuiHelper implements IGuiHandler
{
	public enum GuiIds
	{
		FOOD_CONTAINER
	}

	public static final int NINE_SLOT_WIDTH = 162;
	public static final int STANDARD_GUI_WIDTH = 176;
	public static final int STANDARD_SLOT_WIDTH = 18;

	public static void init()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(ModSpiceOfLife.instance, new GuiHelper());
	}

	public static boolean openGuiOfItemStack(EntityPlayer player, ItemStack itemStack)
	{
		if (!player.worldObj.isRemote)
		{
			if (itemStack.getItem() instanceof ItemFoodContainer)
			{
				player.openGui(ModSpiceOfLife.instance, GuiIds.FOOD_CONTAINER.ordinal(), player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
				return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z)
	{
		return getSidedGuiElement(false, guiId, player, world, x, y, z);
	}

	@Override
	public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z)
	{
		return getSidedGuiElement(true, guiId, player, world, x, y, z);
	}

	public Object getSidedGuiElement(boolean isClientSide, int guiId, EntityPlayer player, World world, int x, int y, int z)
	{
		switch (GuiIds.values()[guiId])
		{
			case FOOD_CONTAINER:
				ItemStack heldItem = player.getHeldItem();
				if (heldItem != null && heldItem.getItem() instanceof ItemFoodContainer)
				{
					FoodContainerInventory foodContainerInventory = ((ItemFoodContainer) heldItem.getItem()).getInventory(heldItem);
					return isClientSide ? new GuiFoodContainer(player.inventory, foodContainerInventory) : new ContainerFoodContainer(player.inventory, foodContainerInventory);
				}
				break;
			default:
				break;
		}
		return null;
	}
}