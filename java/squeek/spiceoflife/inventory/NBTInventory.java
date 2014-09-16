package squeek.spiceoflife.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import squeek.spiceoflife.interfaces.ISaveable;

public class NBTInventory implements ISaveable, IInventory
{
	protected ItemStack[] inventoryItems;
	protected INBTInventoryHaver inventoryHaver = null;
	protected NBTTagCompound nbt = null;

	public NBTInventory()
	{
		this(0);
	}

	public NBTInventory(int inventorySize)
	{
		this(new ItemStack[inventorySize]);
	}

	public NBTInventory(ItemStack[] inventoryItems)
	{
		this.inventoryItems = inventoryItems;
	}

	public NBTInventory(INBTInventoryHaver inventoryHaver)
	{
		this(inventoryHaver.getSizeInventory());
		this.inventoryHaver = inventoryHaver;
	}

	/*
	 * Inventory utility
	 */
	protected void onSlotFilled(int slotNum)
	{
	}

	protected void onSlotEmptied(int slotNum)
	{
	}

	public void markDirty()
	{
		onInventoryChanged();
	}

	public boolean isInventoryEmpty()
	{
		for (ItemStack itemStack : inventoryItems)
		{
			if (itemStack != null)
				return false;
		}
		return true;
	}

	public static boolean isInventoryEmpty(NBTTagCompound data)
	{
		NBTTagList items = data.getTagList("Items");
		return items.tagCount() == 0;
	}

	public boolean isInventoryFull()
	{
		for (ItemStack itemStack : inventoryItems)
		{
			if (itemStack == null || itemStack.stackSize < Math.min(getInventoryStackLimit(), itemStack.getMaxStackSize()))
				return false;
		}
		return true;
	}

	public boolean isValidSlotNum(int slotNum)
	{
		return slotNum < getSizeInventory() && slotNum >= 0;
	}

	/*
	 * IInventory implementation
	 */
	@Override
	public int getSizeInventory()
	{
		return inventoryItems.length;
	}

	@Override
	public ItemStack getStackInSlot(int slotNum)
	{
		if (isValidSlotNum(slotNum))
			return inventoryItems[slotNum];
		else
			return null;
	}

	@Override
	public ItemStack decrStackSize(int slotNum, int count)
	{
		ItemStack itemStack = getStackInSlot(slotNum);

		if (itemStack != null)
		{
			if (itemStack.stackSize <= count)
				setInventorySlotContents(slotNum, null);
			else
			{
				itemStack = itemStack.splitStack(count);
				markDirty();
			}
		}

		return itemStack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotNum)
	{
		ItemStack item = getStackInSlot(slotNum);
		setInventorySlotContents(slotNum, null);
		return item;
	}

	@Override
	public void setInventorySlotContents(int slotNum, ItemStack itemStack)
	{
		if (!isValidSlotNum(slotNum))
			return;

		boolean wasEmpty = getStackInSlot(slotNum) == null;
		inventoryItems[slotNum] = itemStack;

		if (itemStack != null && itemStack.stackSize > getInventoryStackLimit())
			itemStack.stackSize = getInventoryStackLimit();

		if (wasEmpty && itemStack != null)
			onSlotFilled(slotNum);
		else if (!wasEmpty && itemStack == null)
			onSlotEmptied(slotNum);

		markDirty();
	}

	@Override
	public String getInvName()
	{
		if (inventoryHaver != null)
			return inventoryHaver.getInvName(this);
		else
			return null;
	}

	@Override
	public boolean isInvNameLocalized()
	{
		if (inventoryHaver != null)
			return inventoryHaver.isInvNameLocalized(this);
		else
			return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		if (inventoryHaver != null)
			return inventoryHaver.getInventoryStackLimit(this);
		else
			return 64;
	}

	@Override
	public void onInventoryChanged()
	{
		if (inventoryHaver != null)
			inventoryHaver.onInventoryChanged(this);
	}

	@Override
	public boolean isItemValidForSlot(int slotNum, ItemStack itemStack)
	{
		if (inventoryHaver != null)
			return inventoryHaver.isItemValidForSlot(this, slotNum, itemStack);
		else
			return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public void writeToNBTData(NBTTagCompound data)
	{
		NBTTagList items = new NBTTagList();
		for (int slotNum = 0; slotNum < getSizeInventory(); slotNum++)
		{
			ItemStack stack = getStackInSlot(slotNum);

			if (stack != null)
			{
				NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot", (byte) slotNum);
				stack.writeToNBT(item);
				items.appendTag(item);
			}
		}
		data.setTag("Items", items);
	}

	@Override
	public void readFromNBTData(NBTTagCompound data)
	{
		NBTTagList items = data.getTagList("Items");
		for (int slotNum = 0; slotNum < items.tagCount(); slotNum++)
		{
			NBTTagCompound item = (NBTTagCompound) items.tagAt(slotNum);
			int slot = item.getByte("Slot");

			if (slot >= 0 && slot < getSizeInventory())
			{
				setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
			}
		}
	}
}
