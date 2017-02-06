package squeek.spiceoflife.inventory;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface INBTInventoryHaver
{
	int getSizeInventory();

	String getInvName(NBTInventory inventory);

	boolean hasCustomName(NBTInventory inventory);

	int getInventoryStackLimit(NBTInventory inventory);

	void onInventoryChanged(NBTInventory inventory);

	boolean isItemValidForSlot(NBTInventory inventory, int slotNum, @Nonnull ItemStack itemStack);
}
