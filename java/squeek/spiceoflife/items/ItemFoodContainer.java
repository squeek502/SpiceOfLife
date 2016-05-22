package squeek.spiceoflife.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdible;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.helpers.*;
import squeek.spiceoflife.helpers.MealPrioritizationHelper.InventoryFoodInfo;
import squeek.spiceoflife.inventory.ContainerFoodContainer;
import squeek.spiceoflife.inventory.FoodContainerInventory;
import squeek.spiceoflife.inventory.INBTInventoryHaver;
import squeek.spiceoflife.inventory.NBTInventory;
import squeek.spiceoflife.network.NetworkHelper;
import squeek.spiceoflife.network.PacketHandler;
import squeek.spiceoflife.network.PacketToggleFoodContainer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class ItemFoodContainer extends Item implements INBTInventoryHaver, IEdible
{
	public int numSlots;
	public String itemName;
	public static final Random random = new Random();

	public static final String TAG_KEY_INVENTORY = "Inventory";
	public static final String TAG_KEY_OPEN = "Open";
	public static final String TAG_KEY_UUID = "UUID";

	public ItemFoodContainer(String itemName, int numSlots)
	{
		super();
		this.itemName = itemName;
		this.numSlots = numSlots;
		setMaxStackSize(1);
		setRegistryName(this.itemName);
		setUnlocalizedName(ModInfo.MODID.toLowerCase(Locale.ROOT) + '.' + this.itemName);
		setCreativeTab(CreativeTabs.MISC);

		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
		{
			registerModels();
		}

		// for ItemTossEvent
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SideOnly(Side.CLIENT)
	public void registerModels()
	{
		final ModelResourceLocation closed = new ModelResourceLocation(getRegistryName(), "inventory");
		final ModelResourceLocation openEmpty = new ModelResourceLocation(getRegistryName() + "_open_empty", "inventory");
		final ModelResourceLocation openFull = new ModelResourceLocation(getRegistryName() + "_open_full", "inventory");

		ModelLoader.registerItemVariants(this, closed, openEmpty, openFull);

		ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition()
		{
			@Override
			public ModelResourceLocation getModelLocation(ItemStack itemStack)
			{
				if (isOpen(itemStack))
				{
					return isEmpty(itemStack) ? openEmpty : openFull;
				}
				return closed;
			}
		});
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
		return itemStack.hasTagCompound() && itemStack.getTagCompound().getBoolean(TAG_KEY_OPEN);
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
			baseTag.setTag(TAG_KEY_INVENTORY, new NBTTagCompound());

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
		if (!foodsToPull.isEmpty())
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
	@SubscribeEvent
	public void onItemToss(ItemTossEvent event)
	{
		if (event.getEntityItem().getEntityItem().getItem() instanceof ItemFoodContainer)
		{
			onDroppedByPlayer(event.getEntityItem().getEntityItem(), event.getPlayer());
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

				if (chanceToDrop > 0 && random.nextFloat() <= chanceToDrop)
				{
					ItemStack itemToDrop = InventoryHelper.removeRandomSingleItemFromInventory(getInventory(itemStack), random);
					player.dropItem(itemToDrop, true);
				}
			}
		}

		super.onUpdate(itemStack, world, ownerEntity, par4, par5);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List toolTip, boolean isAdvanced)
	{
		super.addInformation(itemStack, player, toolTip, isAdvanced);

		String openCloseLineColor = TextFormatting.GRAY.toString();
		if (isOpen(itemStack))
		{
			toolTip.add(openCloseLineColor + I18n.format("spiceoflife.tooltip.to.close.food.container"));

			if (ModConfig.FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD > 0)
				toolTip.add(TextFormatting.GOLD.toString() + TextFormatting.ITALIC + I18n.format("spiceoflife.tooltip.can.spill.food"));
		}
		else
			toolTip.add(openCloseLineColor + I18n.format("spiceoflife.tooltip.to.open.food.container"));
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if (!world.isRemote && isOpen(itemStack))
		{
			IInventory inventoryHit = InventoryHelper.getInventoryAtLocation(world, pos.getX(), pos.getY(), pos.getZ());
			if (inventoryHit != null && inventoryHit.isUseableByPlayer(player))
			{
				tryDumpFoodInto(itemStack, inventoryHit, player);
				tryPullFoodFrom(itemStack, inventoryHit, player);

				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUseFirst(itemStack, player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemStack)
	{
		if (canBeEatenFrom(itemStack))
			return EnumAction.EAT;
		else
			return EnumAction.NONE;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand)
	{
		if (player.isSneaking())
		{
			setIsOpen(itemStack, !isOpen(itemStack));
			return new ActionResult(EnumActionResult.SUCCESS, itemStack);
		}
		else if (canPlayerEatFrom(player, itemStack))
		{
			player.setActiveHand(hand);
			return new ActionResult(EnumActionResult.SUCCESS, itemStack);
		}
		else if (!isOpen(itemStack) && hand == EnumHand.MAIN_HAND)
		{
			GuiHelper.openGuiOfItemStack(player, itemStack);
			setIsOpen(itemStack, true);
			return new ActionResult(EnumActionResult.SUCCESS, itemStack);
		}
		return super.onItemRightClick(itemStack, world, player, hand);
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
	{
		if (entityLiving.worldObj.isRemote && ModConfig.LEFT_CLICK_OPENS_FOOD_CONTAINERS && MiscHelper.isMouseOverNothing())
		{
			PacketHandler.channel.sendToServer(new PacketToggleFoodContainer());
			return true;
		}

		return super.onEntitySwing(entityLiving, stack);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemStack)
	{
		return 32;
	}

	@Nullable
	@Override
	public ItemStack onItemUseFinish(ItemStack itemStack, World world, EntityLivingBase entityLiving)
	{
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;
			IInventory inventory = getInventory(itemStack);

			int slotWithBestFood = MealPrioritizationHelper.findBestFoodForPlayerToEat(player, inventory);
			ItemStack foodToEat = inventory.getStackInSlot(slotWithBestFood);
			if (foodToEat != null)
			{
				foodToEat.onItemUseFinish(world, player);

				if (foodToEat.stackSize <= 0)
					foodToEat = null;

				inventory.setInventorySlotContents(slotWithBestFood, foodToEat);
			}
		}
		return super.onItemUseFinish(itemStack, world, entityLiving);
	}

	public ItemStack getBestFoodForPlayerToEat(ItemStack itemStack, EntityPlayer player)
	{
		IInventory inventory = getInventory(itemStack);
		int slotWithBestFood = MealPrioritizationHelper.findBestFoodForPlayerToEat(player, inventory);
		return inventory.getStackInSlot(slotWithBestFood);
	}

	@Override
	public int getSizeInventory()
	{
		return numSlots;
	}

	@Override
	public String getInvName(NBTInventory inventory)
	{
		return this.getUnlocalizedName() + ".name";
	}

	@Override
	public boolean hasCustomName(NBTInventory inventory)
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
		return FoodHelper.isFood(itemStack) && FoodHelper.isDirectlyEdible(itemStack);
	}

	/*
	 * IEdible implementation
	 */
	@Override
	public FoodValues getFoodValues(ItemStack itemStack)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			// the client uses the food values for tooltips/etc, so it should
			// inherit them from the food that will be eaten
			return FoodValues.get(getBestFoodForPlayerToEat(itemStack, NetworkHelper.getClientPlayer()));
		}
		else
		{
			// the server only needs to know that food values are non-null
			// this is used for the isFood check
			return new FoodValues(0, 0f);
		}
	}

	// necessary to stop food containers themselves being modified
	// for example, HO's modFoodDivider was being applied to the values
	// shown in the tooltips/overlay
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void getFoodValues(FoodEvent.GetFoodValues event)
	{
		if (FoodHelper.isFoodContainer(event.food))
		{
			event.foodValues = event.unmodifiedFoodValues;
		}
	}

}
