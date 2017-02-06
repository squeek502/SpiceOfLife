package squeek.spiceoflife.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import squeek.spiceoflife.helpers.InventoryHelper;
import squeek.spiceoflife.interfaces.ISaveable;

import javax.annotation.Nonnull;

import static net.minecraft.util.NonNullList.withSize;

public class NBTInventory implements ISaveable, IInventory
{
	protected NonNullList<ItemStack> inventoryItems;
	protected INBTInventoryHaver inventoryHaver = null;

	public NBTInventory()
	{
		this(0);
	}

	public NBTInventory(int inventorySize)
	{
		this(NonNullList.withSize(inventorySize, ItemStack.EMPTY));
	}

	public NBTInventory(ItemStack[] inventoryItems)
	{
		this(InventoryHelper.itemStackArrayToList(inventoryItems));
	}

	public NBTInventory(NonNullList<ItemStack> inventoryItems)
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

	public void onInventoryChanged()
	{
	}

	public boolean isInventoryEmpty()
	{
		for (ItemStack itemStack : inventoryItems)
		{
			if (itemStack != ItemStack.EMPTY)
				return false;
		}
		return true;
	}

	public static boolean isInventoryEmpty(NBTTagCompound data)
	{
		NBTTagList items = data.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		return items.tagCount() == 0;
	}

	public boolean isInventoryFull()
	{
		for (ItemStack itemStack : inventoryItems)
		{
			if (itemStack == ItemStack.EMPTY || itemStack.getCount() < Math.min(getInventoryStackLimit(), itemStack.getMaxStackSize()))
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
		return inventoryItems.size();
	}

	@Override
	public boolean isEmpty()
	{
		return isInventoryEmpty();
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slotNum)
	{
		if (isValidSlotNum(slotNum))
			return inventoryItems.get(slotNum);
		else
			return ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public ItemStack decrStackSize(int slotNum, int count)
	{
		ItemStack itemStack = getStackInSlot(slotNum);

		if (itemStack != ItemStack.EMPTY)
		{
			if (itemStack.getCount() <= count)
				setInventorySlotContents(slotNum, ItemStack.EMPTY);
			else
			{
				itemStack = itemStack.splitStack(count);
				markDirty();
			}
		}

		return itemStack;
	}

	@Override
	@Nonnull
	public ItemStack removeStackFromSlot(int slotNum)
	{
		ItemStack item = getStackInSlot(slotNum);
		setInventorySlotContents(slotNum, ItemStack.EMPTY);
		return item;
	}

	@Override
	public void setInventorySlotContents(int slotNum, @Nonnull ItemStack itemStack)
	{
		if (!isValidSlotNum(slotNum))
			return;

		boolean wasEmpty = getStackInSlot(slotNum) == ItemStack.EMPTY;
		inventoryItems.set(slotNum, itemStack);

		if (itemStack != ItemStack.EMPTY && itemStack.getCount() > getInventoryStackLimit())
			itemStack.setCount(getInventoryStackLimit());

		if (wasEmpty && itemStack != ItemStack.EMPTY)
			onSlotFilled(slotNum);
		else if (!wasEmpty && itemStack == ItemStack.EMPTY)
			onSlotEmptied(slotNum);

		markDirty();
	}

	@Override
	@Nonnull
	public String getName()
	{
		if (inventoryHaver != null)
			return inventoryHaver.getInvName(this);
		else
			return "unnamed";
	}

	@Override
	public boolean hasCustomName()
	{
		return inventoryHaver != null && inventoryHaver.hasCustomName(this);
	}

	@Override
	@Nonnull
	public ITextComponent getDisplayName()
	{
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
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
	public void markDirty()
	{
		onInventoryChanged();

		if (inventoryHaver != null)
			inventoryHaver.onInventoryChanged(this);
	}

	@Override
	public boolean isItemValidForSlot(int slotNum, @Nonnull ItemStack itemStack)
	{
		return inventoryHaver == null || inventoryHaver.isItemValidForSlot(this, slotNum, itemStack);
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openInventory(@Nonnull EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player)
	{
	}

	@Override
	public void writeToNBTData(NBTTagCompound data)
	{
		NBTTagList items = new NBTTagList();
		for (int slotNum = 0; slotNum < getSizeInventory(); slotNum++)
		{
			ItemStack stack = getStackInSlot(slotNum);

			if (stack != ItemStack.EMPTY)
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
		NBTTagList items = data.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < items.tagCount(); i++)
		{
			NBTTagCompound item = items.getCompoundTagAt(i);
			int slot = item.getByte("Slot");

			if (slot >= 0 && slot < getSizeInventory())
			{
				setInventorySlotContents(slot, new ItemStack(item));
			}
		}
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
	}
}
