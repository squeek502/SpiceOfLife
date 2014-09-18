package squeek.spiceoflife.items;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.helpers.*;
import squeek.spiceoflife.helpers.MealPrioritizationHelper.InventoryFoodInfo;
import squeek.spiceoflife.inventory.ContainerFoodContainer;
import squeek.spiceoflife.inventory.FoodContainerInventory;
import squeek.spiceoflife.inventory.INBTInventoryHaver;
import squeek.spiceoflife.inventory.NBTInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFoodContainer extends Item implements INBTInventoryHaver
{
	private Icon iconOpenEmpty;
	private Icon iconOpenFull;
	public int numSlots;
	public String itemName;
	public static final Random random = new Random();

	public static final String TAG_KEY_INVENTORY = "Inventory";
	public static final String TAG_KEY_OPEN = "Open";
	public static final String TAG_KEY_UUID = "UUID";

	public ItemFoodContainer(int itemId, String itemName, int numSlots)
	{
		super(itemId - 256);
		this.itemName = itemName;
		this.numSlots = numSlots;
		setMaxStackSize(1);
		setTextureName(ModInfo.MODID.toLowerCase() + ":" + itemName);
		setUnlocalizedName(ModInfo.MODID.toLowerCase() + "." + itemName);
		setCreativeTab(CreativeTabs.tabMisc);

		// for ItemTossEvent
		MinecraftForge.EVENT_BUS.register(this);
	}

	public boolean isEmpty(ItemStack itemStack)
	{
		return NBTInventory.isInventoryEmpty(getInventoryTag(itemStack));
	}

	public boolean isFull(ItemStack itemStack)
	{
		return getInventory(itemStack).isInventoryFull();
	}

	public boolean isOpen(ItemStack itemStack)
	{
		return itemStack.hasTagCompound() ? itemStack.getTagCompound().getBoolean(TAG_KEY_OPEN) : false;
	}

	public void setIsOpen(ItemStack itemStack, boolean isOpen)
	{
		NBTTagCompound baseTag = getOrInitBaseTag(itemStack);
		baseTag.setBoolean(TAG_KEY_OPEN, isOpen);
	}

	public UUID getUUID(ItemStack itemStack)
	{
		return UUID.fromString(getOrInitBaseTag(itemStack).getString(TAG_KEY_UUID));
	}

	public NBTTagCompound getOrInitBaseTag(ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());

		NBTTagCompound baseTag = itemStack.getTagCompound();

		if (!baseTag.hasKey(TAG_KEY_UUID))
			baseTag.setString(TAG_KEY_UUID, UUID.randomUUID().toString());

		return baseTag;
	}

	public NBTTagCompound getInventoryTag(ItemStack itemStack)
	{
		NBTTagCompound baseTag = getOrInitBaseTag(itemStack);

		if (!baseTag.hasKey(TAG_KEY_INVENTORY))
			baseTag.setCompoundTag(TAG_KEY_INVENTORY, new NBTTagCompound());

		return baseTag.getCompoundTag(TAG_KEY_INVENTORY);
	}

	public FoodContainerInventory getInventory(ItemStack itemStack)
	{
		return new FoodContainerInventory(this, itemStack);
	}

	public void tryDumpFoodInto(ItemStack itemStack, IInventory inventory, EntityPlayer player)
	{
		FoodContainerInventory foodContainerInventory = getInventory(itemStack);
		for (int slotNum = 0; slotNum < foodContainerInventory.getSizeInventory(); slotNum++)
		{
			ItemStack stackInSlot = foodContainerInventory.getStackInSlot(slotNum);

			if (stackInSlot == null)
				continue;

			stackInSlot = InventoryHelper.insertStackIntoInventory(stackInSlot, inventory);
			foodContainerInventory.setInventorySlotContents(slotNum, stackInSlot);
		}
	}

	public void tryPullFoodFrom(ItemStack itemStack, IInventory inventory, EntityPlayer player)
	{
		List<InventoryFoodInfo> foodsToPull = MealPrioritizationHelper.findBestFoodsForPlayerAccountingForVariety(player, inventory);
		if (foodsToPull.size() > 0)
		{
			FoodContainerInventory foodContainerInventory = getInventory(itemStack);
			for (InventoryFoodInfo foodToPull : foodsToPull)
			{
				ItemStack stackInSlot = inventory.getStackInSlot(foodToPull.slotNum);

				if (stackInSlot == null)
					continue;

				stackInSlot = InventoryHelper.insertStackIntoInventoryOnce(stackInSlot, foodContainerInventory);
				inventory.setInventorySlotContents(foodToPull.slotNum, stackInSlot);
			}
		}
	}

	// necessary to catch tossing items while still in an inventory
	@ForgeSubscribe
	public void onItemToss(ItemTossEvent event)
	{
		if (event.entityItem.getEntityItem().getItem() instanceof ItemFoodContainer)
		{
			onDroppedByPlayer(event.entityItem.getEntityItem(), event.player);
		}
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack itemStack, EntityPlayer player)
	{
		if (!player.worldObj.isRemote && player.openContainer != null && player.openContainer instanceof ContainerFoodContainer)
		{
			ContainerFoodContainer openFoodContainer = (ContainerFoodContainer) player.openContainer;
			UUID droppedUUID = getUUID(itemStack);

			if (openFoodContainer.getUUID().equals(droppedUUID))
			{
				// if the cursor item is the open food container, then it will create an infinite loop
				// due to the container dropping the cursor item when it is closed
				ItemStack itemOnTheCursor = player.inventory.getItemStack();
				if (itemOnTheCursor != null && itemOnTheCursor.getItem() instanceof ItemFoodContainer)
				{
					if (((ItemFoodContainer) itemOnTheCursor.getItem()).getUUID(itemOnTheCursor).equals(droppedUUID))
					{
						player.inventory.setItemStack(null);
					}
				}

				player.closeScreen();
			}
		}
		return super.onDroppedByPlayer(itemStack, player);
	}

	public boolean canBeEatenFrom(ItemStack stack)
	{
		return isOpen(stack) && !isEmpty(stack);
	}

	public boolean canPlayerEatFrom(EntityPlayer player, ItemStack stack)
	{
		return canBeEatenFrom(stack) && player.canEat(false);
	}

	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity ownerEntity, int par4, boolean par5)
	{
		if (!world.isRemote && ownerEntity instanceof EntityPlayer && isOpen(itemStack) && !isEmpty(itemStack))
		{
			EntityPlayer player = (EntityPlayer) ownerEntity;
			if (!player.isSneaking() && MovementHelper.getDidJumpLastTick(player))
			{
				float chanceToDrop = ModConfig.FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD;

				if (player.isSprinting())
					chanceToDrop *= 2f;

				ModSpiceOfLife.Log.info(Float.toString(chanceToDrop));
				if (chanceToDrop > 0 && random.nextFloat() <= chanceToDrop)
				{
					ItemStack itemToDrop = InventoryHelper.removeRandomSingleItemFromInventory(getInventory(itemStack), random);
					player.dropPlayerItemWithRandomChoice(itemToDrop, true);
				}
			}
		}

		super.onUpdate(itemStack, world, ownerEntity, par4, par5);
	}

	@Override
	public Icon getIconIndex(ItemStack itemStack)
	{
		if (isOpen(itemStack))
		{
			return isEmpty(itemStack) ? iconOpenEmpty : iconOpenFull;
		}
		return super.getIconIndex(itemStack);
	}

	@Override
	public Icon getIcon(ItemStack itemStack, int renderPass)
	{
		return getIconIndex(itemStack);
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote && isOpen(itemStack))
		{
			IInventory inventoryHit = InventoryHelper.getInventoryAtLocation(world, x, y, z);
			if (inventoryHit != null)
			{
				tryDumpFoodInto(itemStack, inventoryHit, player);
				tryPullFoodFrom(itemStack, inventoryHit, player);

				return true;
			}
		}
		return super.onItemUseFirst(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemStack)
	{
		return EnumAction.eat;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		if (player.isSneaking())
		{
			setIsOpen(itemStack, !isOpen(itemStack));
		}
		else if (canPlayerEatFrom(player, itemStack))
		{
			player.setItemInUse(itemStack, getMaxItemUseDuration(itemStack));
		}
		else if (!isOpen(itemStack))
		{
			GuiHelper.openGuiOfItemStack(player, itemStack);
			setIsOpen(itemStack, true);
		}
		return super.onItemRightClick(itemStack, world, player);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemStack)
	{
		return 32;
	}

	@Override
	public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player)
	{
		IInventory inventory = getInventory(itemStack);
		int slotWithBestFood = MealPrioritizationHelper.findBestFoodForPlayerToEat(player, inventory);
		ItemStack foodToEat = inventory.getStackInSlot(slotWithBestFood);
		if (foodToEat != null)
		{
			foodToEat.onFoodEaten(world, player);

			if (foodToEat.stackSize <= 0)
				foodToEat = null;

			inventory.setInventorySlotContents(slotWithBestFood, foodToEat);
		}
		return super.onEaten(itemStack, world, player);
	}

	public ItemStack getBestFoodForPlayerToEat(ItemStack itemStack, EntityPlayer player)
	{
		IInventory inventory = getInventory(itemStack);
		int slotWithBestFood = MealPrioritizationHelper.findBestFoodForPlayerToEat(player, inventory);
		ItemStack foodToEat = inventory.getStackInSlot(slotWithBestFood);
		return foodToEat;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		iconOpenEmpty = iconRegister.registerIcon(getIconString() + "_open_empty");
		iconOpenFull = iconRegister.registerIcon(getIconString() + "_open_full");
	}

	@Override
	public int getSizeInventory()
	{
		return numSlots;
	}

	@Override
	public String getInvName(NBTInventory inventory)
	{
		return this.getItemDisplayName(null);
	}

	@Override
	public boolean isInvNameLocalized(NBTInventory inventory)
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit(NBTInventory inventory)
	{
		return ModConfig.FOOD_CONTAINERS_MAX_STACKSIZE;
	}

	@Override
	public void onInventoryChanged(NBTInventory inventory)
	{
	}

	@Override
	public boolean isItemValidForSlot(NBTInventory inventory, int slotNum, ItemStack itemStack)
	{
		return FoodHelper.isFood(itemStack);
	}

}
