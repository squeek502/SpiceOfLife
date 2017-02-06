package squeek.spiceoflife.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InventoryHelper
{
	public static final Method hopperInsertIntoInventory = ReflectionHelper.findMethod(TileEntityHopper.class, null, new String[]{"insertStack", "func_174916_c", "c"}, IInventory.class, IInventory.class, ItemStack.class, int.class, EnumFacing.class);

	public static IInventory getInventoryAtLocation(World world, int x, int y, int z)
	{
		return TileEntityHopper.getInventoryAtPosition(world, x, y, z);
	}

	@Nonnull
	public static ItemStack insertStackIntoInventory(@Nonnull ItemStack itemStack, IInventory inventory)
	{
		return insertStackIntoInventory(itemStack, inventory, EnumFacing.UP);
	}

	@Nonnull
	public static ItemStack insertStackIntoInventory(@Nonnull ItemStack itemStack, IInventory inventory, EnumFacing direction)
	{
		return TileEntityHopper.putStackInInventoryAllSlots(null, inventory, itemStack, direction);
	}

	@Nonnull
	public static ItemStack insertStackIntoInventoryOnce(@Nonnull ItemStack itemStack, IInventory inventory)
	{
		return insertStackIntoInventoryOnce(itemStack, inventory, EnumFacing.UP);
	}

	/**
	 * Only fill a maximum of one slot
	 *
	 * @return The remainder
	 */
	@Nonnull
	public static ItemStack insertStackIntoInventoryOnce(@Nonnull ItemStack itemStack, IInventory inventory, EnumFacing direction)
	{
		int originalStackSize = itemStack.getCount();

		for (int l = 0; l < inventory.getSizeInventory(); ++l)
		{
			try
			{
				itemStack = (ItemStack) hopperInsertIntoInventory.invoke(null, null, inventory, itemStack, l, direction);
			}
			catch (RuntimeException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (itemStack == ItemStack.EMPTY || itemStack.getCount() != originalStackSize)
				break;
		}

		if (itemStack.isEmpty())
		{
			itemStack = ItemStack.EMPTY;
		}

		return itemStack;
	}

	public static List<Integer> getNonEmptySlotsInInventory(IInventory inventory)
	{
		List<Integer> nonEmptySlotIndexes = new ArrayList<Integer>(inventory.getSizeInventory());
		for (int slotNum = 0; slotNum < inventory.getSizeInventory(); slotNum++)
		{
			if (!inventory.getStackInSlot(slotNum).isEmpty())
				nonEmptySlotIndexes.add(slotNum);
		}
		return nonEmptySlotIndexes;
	}

	public static int getRandomNonEmptySlotInInventory(IInventory inventory, Random random)
	{
		List<Integer> nonEmptySlots = getNonEmptySlotsInInventory(inventory);

		if (!nonEmptySlots.isEmpty())
			return nonEmptySlots.get(random.nextInt(nonEmptySlots.size()));
		else
			return 0;
	}

	@Nonnull
	public static ItemStack removeRandomSingleItemFromInventory(IInventory inventory, Random random)
	{
		int randomNonEmptySlotIndex = getRandomNonEmptySlotInInventory(inventory, random);

		if (inventory.getStackInSlot(randomNonEmptySlotIndex) != ItemStack.EMPTY)
			return inventory.decrStackSize(randomNonEmptySlotIndex, 1);
		else
			return ItemStack.EMPTY;
	}

	public static NonNullList<ItemStack> itemStackArrayToList(ItemStack[] array)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(array.length, ItemStack.EMPTY);
		for (int i=0; i<array.length; i++)
		{
			if (!array[i].isEmpty())
				list.set(i, array[i]);
		}
		return list;
	}
}
