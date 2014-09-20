package squeek.spiceoflife.inventory;

import net.minecraft.item.ItemStack;

public interface INBTInventoryHaver
{
	public int getSizeInventory();

	public String getInvName(NBTInventory inventory);

	public boolean isInvNameLocalized(NBTInventory inventory);

	public int getInventoryStackLimit(NBTInventory inventory);

	public void onInventoryChanged(NBTInventory inventory);

	public boolean isItemValidForSlot(NBTInventory inventory, int slotNum, ItemStack itemStack);
}
