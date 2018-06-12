package squeek.spiceoflife.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.helpers.GuiHelper;
import squeek.spiceoflife.items.ItemFoodContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ContainerFoodContainer extends ContainerGeneric
{
	protected FoodContainerInventory foodContainerInventory;
	public int slotsX;
	public int slotsY;

	public ContainerFoodContainer(InventoryPlayer playerInventory, FoodContainerInventory foodContainerInventory)
	{
		super(foodContainerInventory);
		this.foodContainerInventory = foodContainerInventory;

		slotsX = (int) (GuiHelper.STANDARD_GUI_WIDTH / 2f - (inventory.getSizeInventory() * GuiHelper.STANDARD_SLOT_WIDTH / 2f));
		slotsY = 19;

		this.addSlotsOfType(SlotFiltered.class, inventory, slotsX, slotsY);
		this.addPlayerInventorySlots(playerInventory, 51);
	}

	public void setFoodContainerItemStack(@Nonnull ItemStack itemStack)
	{
		foodContainerInventory.itemStackFoodContainer = itemStack;
	}

	@Override
	protected void addHotbarSlot(InventoryPlayer playerInventory, int slotNum, int x, int y)
	{
		ItemStack stackInSlot = playerInventory.getStackInSlot(slotNum);
		if (isFoodContainerWithUUID(stackInSlot, getUUID()))
		{
			addSlotToContainer(new SlotLocked(playerInventory, slotNum, x, y));
		}
		else
		{
			super.addHotbarSlot(playerInventory, slotNum, x, y);
		}
	}

	@Nonnull
	public ItemStack getItemStack()
	{
		return foodContainerInventory.itemStackFoodContainer;
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		// the client could have a different ItemStack than the one the 
		// container was initialized with (due to server syncing), so
		// we need to find the new one
		if (player.world.isRemote)
		{
			setFoodContainerItemStack(findFoodContainerWithUUID(getUUID()));
		}

		if (!getItemStack().isEmpty())
			foodContainerInventory.itemFoodContainer.setIsOpen(getItemStack(), false);

		super.onContainerClosed(player);
	}

	@Nonnull
	public ItemStack findFoodContainerWithUUID(UUID uuid)
	{
		for (Object inventorySlotObj : this.inventorySlots)
		{
			Slot inventorySlot = (Slot) inventorySlotObj;
			ItemStack itemStack = inventorySlot.getStack();
			if (isFoodContainerWithUUID(itemStack, uuid))
			{
				return itemStack;
			}
		}
		return ItemStack.EMPTY;
	}

	public boolean isFoodContainerWithUUID(@Nonnull ItemStack itemStack, UUID uuid)
	{
		return !itemStack.isEmpty() && itemStack.getItem() instanceof ItemFoodContainer && ((ItemFoodContainer) itemStack.getItem()).getUUID(itemStack).equals(uuid);
	}

	public UUID getUUID()
	{
		return foodContainerInventory.itemFoodContainer.getUUID(getItemStack());
	}
}
