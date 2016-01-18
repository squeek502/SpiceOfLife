package squeek.spiceoflife.inventory;

import net.minecraft.item.ItemStack;

public interface INBTInventoryHaver
{
	int getSizeInventory();

	String getInvName(NBTInventory inventory);

	boolean hasCustomName(NBTInventory inventory);

	int getInventoryStackLimit(NBTInventory inventory);

	void onInventoryChanged(NBTInventory inventory);

	boolean isItemValidForSlot(NBTInventory inventory, int slotNum, ItemStack itemStack);
}
