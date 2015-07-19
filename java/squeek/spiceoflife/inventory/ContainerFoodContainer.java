package squeek.spiceoflife.inventory;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.helpers.GuiHelper;
import squeek.spiceoflife.items.ItemFoodContainer;

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

	public void setFoodContainerItemStack(ItemStack itemStack)
	{
		foodContainerInventory.itemStackFoodContainer = itemStack;
	}

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
		if (player.worldObj.isRemote)
		{
			setFoodContainerItemStack(findFoodContainerWithUUID(getUUID()));
		}

		if (getItemStack() != null)
			((ItemFoodContainer) getItemStack().getItem()).setIsOpen(getItemStack(), false);

		super.onContainerClosed(player);
	}

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
		return null;
	}

	@Override
	public ItemStack slotClick(int slotNum, int mouseButton, int modifier, EntityPlayer player)
	{
		// make sure the correct ItemStack instance is always used when the player is moving
		// the food container around while they have it open
		ItemStack putDownStack = player.inventory.getItemStack();
		ItemStack pickedUpStack = super.slotClick(slotNum, mouseButton, modifier, player);

		if (isFoodContainerWithUUID(pickedUpStack, getUUID()))
		{
			setFoodContainerItemStack(pickedUpStack);
		}
		else if (slotNum >= 0 && isFoodContainerWithUUID(putDownStack, getUUID()) && isFoodContainerWithUUID(getSlot(slotNum).getStack(), getUUID()))
		{
			setFoodContainerItemStack(getSlot(slotNum).getStack());
		}

		return pickedUpStack;
	}

	public boolean isFoodContainerWithUUID(ItemStack itemStack, UUID uuid)
	{
		return itemStack != null && itemStack.getItem() instanceof ItemFoodContainer && ((ItemFoodContainer) itemStack.getItem()).getUUID(itemStack).equals(uuid);
	}

	public UUID getUUID()
	{
		return ((ItemFoodContainer) getItemStack().getItem()).getUUID(getItemStack());
	}
}
