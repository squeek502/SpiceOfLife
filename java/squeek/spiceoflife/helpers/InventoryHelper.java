package squeek.spiceoflife.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InventoryHelper
{
	@Nullable
	public static IItemHandler getInventoryAtLocation(World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		return getInventoryFromTile(tile, EnumFacing.UP);
	}

	@Nullable
	public static IItemHandler getInventoryFromTile(@Nullable TileEntity tile, @Nullable EnumFacing side)
	{
		if (tile == null)
			return null;

		if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
			return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);

		if (tile instanceof ISidedInventory)
			return new SidedInvWrapper((ISidedInventory) tile, side);

		if (tile instanceof IInventory)
			return new InvWrapper((IInventory) tile);

		return null;
	}

	@Nonnull
	public static ItemStack insertStackIntoInventory(@Nonnull ItemStack itemStack, IItemHandler inventory)
	{
		return ItemHandlerHelper.insertItemStacked(inventory, itemStack, false);
	}

	/**
	 * Only fill a maximum of one slot
	 *
	 * @return The remainder
	 */
	@Nonnull
	public static ItemStack insertStackIntoInventoryOnce(@Nonnull ItemStack itemStack, IItemHandler inventory)
	{
		int originalStackSize = itemStack.getCount();

		for (int slotIndex = 0; slotIndex < inventory.getSlots(); ++slotIndex)
		{
			itemStack = inventory.insertItem(slotIndex, itemStack, false);

			if (itemStack.isEmpty() || itemStack.getCount() != originalStackSize)
				break;
		}

		if (itemStack.isEmpty())
		{
			itemStack = ItemStack.EMPTY;
		}

		return itemStack;
	}

	public static List<Integer> getNonEmptySlotsInInventory(IItemHandler inventory)
	{
		List<Integer> nonEmptySlotIndexes = new ArrayList<Integer>(inventory.getSlots());
		for (int slotNum = 0; slotNum < inventory.getSlots(); slotNum++)
		{
			if (!inventory.getStackInSlot(slotNum).isEmpty())
				nonEmptySlotIndexes.add(slotNum);
		}
		return nonEmptySlotIndexes;
	}

	public static int getRandomNonEmptySlotInInventory(IItemHandler inventory, Random random)
	{
		List<Integer> nonEmptySlots = getNonEmptySlotsInInventory(inventory);

		if (!nonEmptySlots.isEmpty())
			return nonEmptySlots.get(random.nextInt(nonEmptySlots.size()));
		else
			return 0;
	}

	@Nonnull
	public static ItemStack removeRandomSingleItemFromInventory(IItemHandler inventory, Random random)
	{
		int randomNonEmptySlotIndex = getRandomNonEmptySlotInInventory(inventory, random);

		if (!inventory.getStackInSlot(randomNonEmptySlotIndex).isEmpty())
			return inventory.extractItem(randomNonEmptySlotIndex, 1, false);
		else
			return ItemStack.EMPTY;
	}

	public static NonNullList<ItemStack> itemStackArrayToList(ItemStack[] array)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(array.length, ItemStack.EMPTY);
		for (int i = 0; i < array.length; i++)
		{
			if (!array[i].isEmpty())
				list.set(i, array[i]);
		}
		return list;
	}
}
