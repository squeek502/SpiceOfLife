package squeek.spiceoflife.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class ContainerGeneric extends Container
{
	protected IInventory inventory;
	protected int nextSlotIndex = 0;
	protected boolean allowShiftClickToMultipleSlots = false;

	public ContainerGeneric(IInventory inventory)
	{
		this.inventory = inventory;
	}

	protected void addSlot(IInventory inventory, int xStart, int yStart)
	{
		addSlotOfType(Slot.class, inventory, xStart, yStart);
	}

	protected void addSlots(IInventory inventory, int xStart, int yStart)
	{
		addSlotsOfType(Slot.class, inventory, xStart, yStart, 1);
	}

	protected void addSlots(IInventory inventory, int xStart, int yStart, int rows)
	{
		addSlotsOfType(Slot.class, inventory, xStart, yStart, rows);
	}

	protected void addSlots(IInventory inventory, int xStart, int yStart, int numSlots, int rows)
	{
		addSlotsOfType(Slot.class, inventory, xStart, yStart, numSlots, rows);
	}

	protected void addSlotOfType(Class<? extends Slot> slotClass, IInventory inventory, int xStart, int yStart)
	{
		addSlotsOfType(slotClass, inventory, xStart, yStart, 1, 1);
	}

	protected void addSlotsOfType(Class<? extends Slot> slotClass, IInventory inventory, int xStart, int yStart)
	{
		addSlotsOfType(slotClass, inventory, xStart, yStart, inventory.getSizeInventory(), 1);
	}

	protected void addSlotsOfType(Class<? extends Slot> slotClass, IInventory inventory, int xStart, int yStart, int rows)
	{
		addSlotsOfType(slotClass, inventory, xStart, yStart, inventory.getSizeInventory(), rows);
	}

	protected void addSlotsOfType(Class<? extends Slot> slotClass, IInventory inventory, int xStart, int yStart, int numSlots, int rows)
	{
		int numSlotsPerRow = numSlots / rows;
		for (int i = 0, col = 0, row = 0; i < numSlots; ++i, ++col)
		{
			if (col >= numSlotsPerRow)
			{
				row++;
				col = 0;
			}

			try
			{
				this.addSlotToContainer(slotClass.getConstructor(IInventory.class, int.class, int.class, int.class).newInstance(inventory, getNextSlotIndex(), xStart + col * 18, yStart + row * 18));
			}
			catch (RuntimeException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	protected int getNextSlotIndex()
	{
		nextSlotIndex++;
		return nextSlotIndex - 1;
	}

	protected void addPlayerInventorySlots(InventoryPlayer playerInventory, int yStart)
	{
		addPlayerInventorySlots(playerInventory, 8, yStart);
	}

	protected void addPlayerInventorySlots(InventoryPlayer playerInventory, int xStart, int yStart)
	{
		// inventory
		for (int row = 0; row < 3; ++row)
		{
			for (int col = 0; col < 9; ++col)
			{
				this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, xStart + col * 18, yStart + row * 18));
			}
		}

		// hotbar
		for (int col = 0; col < 9; ++col)
		{
			this.addHotbarSlot(playerInventory, col, xStart + col * 18, yStart + 58);
		}
	}

	protected void addHotbarSlot(InventoryPlayer playerInventory, int slotNum, int x, int y)
	{
		this.addSlotToContainer(new Slot(playerInventory, slotNum, x, y));
	}

	@Override
	@Nonnull
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
	{
		// prevent swapping using number keys
		if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9)
		{
			int hotbarSlotIndex = this.inventorySlots.size() - 9 + dragType;
			Slot hotbarSlot = getSlot(hotbarSlotIndex);
			Slot swapSlot = getSlot(slotId);
			if (hotbarSlot instanceof SlotLocked || swapSlot instanceof SlotLocked)
			{
				return ItemStack.EMPTY;
			}
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	@Nonnull
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
	{
		Slot slot = this.inventorySlots.get(slotNum);

		if (slot != null && slot.getHasStack())
		{
			ItemStack stackToTransfer = slot.getStack();

			// transferring from the container to the player inventory
			if (slotNum < this.inventory.getSizeInventory())
			{
				if (!this.mergeItemStack(stackToTransfer, this.inventory.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			// transferring from the player inventory into the container
			else
			{
				if (!this.mergeItemStack(stackToTransfer, 0, this.inventory.getSizeInventory(), false))
				{
					return ItemStack.EMPTY;
				}
			}

			if (stackToTransfer.getCount() == 0)
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}

			// returning the remainder will attempt to fill any other valid slots with it
			if (allowShiftClickToMultipleSlots)
				return stackToTransfer;
		}

		// returning null stops it from attempting to fill consecutive slots with the remaining stack
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer player)
	{
		return inventory.isUsableByPlayer(player);
	}
}