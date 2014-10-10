package squeek.spiceoflife.inventory;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.helpers.GuiHelper;
import squeek.spiceoflife.items.ItemFoodContainer;

public class ContainerFoodContainer extends ContainerGeneric
{
	protected ItemStack itemStack;
	public int slotsX;
	public int slotsY;

	public ContainerFoodContainer(InventoryPlayer playerInventory, IInventory foodContainerInventory, ItemStack itemStack)
	{
		super(foodContainerInventory);
		this.itemStack = itemStack;

		slotsX = (int) (GuiHelper.STANDARD_GUI_WIDTH / 2f - (inventory.getSizeInventory() * GuiHelper.STANDARD_SLOT_WIDTH / 2f));
		slotsY = 19;

		this.addSlotsOfType(SlotFiltered.class, inventory, slotsX, slotsY);
		this.addPlayerInventorySlots(playerInventory, 51);
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		// the client could have a different ItemStack than the one the 
		// container was initialized with (due to server syncing), so
		// we need to find the new one
		if (player.worldObj.isRemote)
		{
			itemStack = findFoodContainerWithUUID(getUUID());
		}

		if (itemStack != null)
			((ItemFoodContainer) itemStack.getItem()).setIsOpen(itemStack, false);

		super.onContainerClosed(player);
	}

	public ItemStack findFoodContainerWithUUID(UUID uuid)
	{
		for (Object inventorySlotObj : this.inventorySlots)
		{
			Slot inventorySlot = (Slot) inventorySlotObj;
			ItemStack itemStack = inventorySlot.getStack();
			if (itemStack != null && itemStack.getItem() instanceof ItemFoodContainer)
			{
				if (((ItemFoodContainer) itemStack.getItem()).getUUID(itemStack).equals(uuid))
					return itemStack;
			}
		}
		return null;
	}

	public UUID getUUID()
	{
		return ((ItemFoodContainer) itemStack.getItem()).getUUID(itemStack);
	}
}
