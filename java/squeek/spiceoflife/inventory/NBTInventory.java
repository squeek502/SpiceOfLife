package squeek.spiceoflife.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import squeek.spiceoflife.helpers.InventoryHelper;
import squeek.spiceoflife.interfaces.ISaveable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NBTInventory implements ISaveable, IInventory, ICapabilityProvider
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
	 * IItemHandler compat
	 */
	public IItemHandlerModifiable getItemHandler()
	{
		return new InvWrapper(this);
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
			if (!itemStack.isEmpty())
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
			if (itemStack.isEmpty() || itemStack.getCount() < Math.min(getInventoryStackLimit(), itemStack.getMaxStackSize()))
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

		if (!itemStack.isEmpty())
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

		boolean wasEmpty = getStackInSlot(slotNum).isEmpty();
		inventoryItems.set(slotNum, itemStack);

		if (!itemStack.isEmpty() && itemStack.getCount() > getInventoryStackLimit())
			itemStack.setCount(getInventoryStackLimit());

		if (wasEmpty && !itemStack.isEmpty())
			onSlotFilled(slotNum);
		else if (!wasEmpty && itemStack.isEmpty())
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

			if (!stack.isEmpty())
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

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getItemHandler());
		}
		return null;
	}
}
