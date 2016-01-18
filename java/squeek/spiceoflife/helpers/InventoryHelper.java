package squeek.spiceoflife.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InventoryHelper
{
	public static final Method hopperInsertIntoInventory = ReflectionHelper.findMethod(TileEntityHopper.class, null, new String[]{"insertStack", "func_174916_c", "c"}, IInventory.class, ItemStack.class, int.class, EnumFacing.class);

	public static IInventory getInventoryAtLocation(World world, int x, int y, int z)
	{
		return TileEntityHopper.getInventoryAtPosition(world, x, y, z);
	}

	public static ItemStack insertStackIntoInventory(ItemStack itemStack, IInventory inventory)
	{
		return insertStackIntoInventory(itemStack, inventory, EnumFacing.UP);
	}

	public static ItemStack insertStackIntoInventory(ItemStack itemStack, IInventory inventory, EnumFacing direction)
	{
		return TileEntityHopper.putStackInInventoryAllSlots(inventory, itemStack, direction);
	}

	public static ItemStack insertStackIntoInventoryOnce(ItemStack itemStack, IInventory inventory)
	{
		return insertStackIntoInventoryOnce(itemStack, inventory, EnumFacing.UP);
	}

	/**
	 * Only fill a maximum of one slot
	 *
	 * @return The remainder
	 */
	public static ItemStack insertStackIntoInventoryOnce(ItemStack itemStack, IInventory inventory, EnumFacing direction)
	{
		int originalStackSize = itemStack.stackSize;

		for (int l = 0; l < inventory.getSizeInventory(); ++l)
		{
			try
			{
				itemStack = (ItemStack) hopperInsertIntoInventory.invoke(null, inventory, itemStack, l, direction);
			}
			catch (RuntimeException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (itemStack == null || itemStack.stackSize != originalStackSize)
				break;
		}

		if (itemStack != null && itemStack.stackSize == 0)
		{
			itemStack = null;
		}

		return itemStack;
	}

	public static List<Integer> getNonEmptySlotsInInventory(IInventory inventory)
	{
		List<Integer> nonEmptySlotIndexes = new ArrayList<Integer>(inventory.getSizeInventory());
		for (int slotNum = 0; slotNum < inventory.getSizeInventory(); slotNum++)
		{
			if (inventory.getStackInSlot(slotNum) != null)
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

	public static ItemStack removeRandomSingleItemFromInventory(IInventory inventory, Random random)
	{
		int randomNonEmptySlotIndex = getRandomNonEmptySlotInInventory(inventory, random);

		if (inventory.getStackInSlot(randomNonEmptySlotIndex) != null)
			return inventory.decrStackSize(randomNonEmptySlotIndex, 1);
		else
			return null;
	}
}
