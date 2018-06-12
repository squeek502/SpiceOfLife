package squeek.spiceoflife.items;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.applecore.api.food.IEdible;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.helpers.GuiHelper;
import squeek.spiceoflife.helpers.InventoryHelper;
import squeek.spiceoflife.helpers.MealPrioritizationHelper;
import squeek.spiceoflife.helpers.MealPrioritizationHelper.InventoryFoodInfo;
import squeek.spiceoflife.helpers.MiscHelper;
import squeek.spiceoflife.helpers.MovementHelper;
import squeek.spiceoflife.inventory.ContainerFoodContainer;
import squeek.spiceoflife.inventory.FoodContainerInventory;
import squeek.spiceoflife.inventory.INBTInventoryHaver;
import squeek.spiceoflife.inventory.NBTInventory;
import squeek.spiceoflife.network.NetworkHelper;
import squeek.spiceoflife.network.PacketHandler;
import squeek.spiceoflife.network.PacketToggleFoodContainer;

import javax.annotation.Nonnull;
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

	public boolean isEmpty(@Nonnull ItemStack itemStack)
	{
		return NBTInventory.isInventoryEmpty(getInventoryTag(itemStack));
	}

	public boolean isFull(@Nonnull ItemStack itemStack)
	{
		return getInventory(itemStack).isInventoryFull();
	}

	public boolean isOpen(@Nonnull ItemStack itemStack)
	{
		return itemStack.hasTagCompound() && itemStack.getTagCompound().getBoolean(TAG_KEY_OPEN);
	}

	public void setIsOpen(@Nonnull ItemStack itemStack, boolean isOpen)
	{
		NBTTagCompound baseTag = getOrInitBaseTag(itemStack);
		baseTag.setBoolean(TAG_KEY_OPEN, isOpen);
	}

	public UUID getUUID(@Nonnull ItemStack itemStack)
	{
		return UUID.fromString(getOrInitBaseTag(itemStack).getString(TAG_KEY_UUID));
	}

	public NBTTagCompound getOrInitBaseTag(@Nonnull ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());

		NBTTagCompound baseTag = itemStack.getTagCompound();

		if (baseTag != null && !baseTag.hasKey(TAG_KEY_UUID))
			baseTag.setString(TAG_KEY_UUID, UUID.randomUUID().toString());

		return baseTag;
	}

	public NBTTagCompound getInventoryTag(@Nonnull ItemStack itemStack)
	{
		NBTTagCompound baseTag = getOrInitBaseTag(itemStack);

		if (!baseTag.hasKey(TAG_KEY_INVENTORY))
			baseTag.setTag(TAG_KEY_INVENTORY, new NBTTagCompound());

		return baseTag.getCompoundTag(TAG_KEY_INVENTORY);
	}

	public FoodContainerInventory getInventory(@Nonnull ItemStack itemStack)
	{
		return new FoodContainerInventory(this, itemStack);
	}

	public void tryDumpFoodInto(@Nonnull ItemStack itemStack, IItemHandler inventory, EntityPlayer player)
	{
		FoodContainerInventory foodContainerInventory = getInventory(itemStack);
		for (int slotNum = 0; slotNum < foodContainerInventory.getSizeInventory(); slotNum++)
		{
			ItemStack stackInSlot = foodContainerInventory.getStackInSlot(slotNum);

			if (stackInSlot.isEmpty())
				continue;

			stackInSlot = InventoryHelper.insertStackIntoInventory(stackInSlot, inventory);
			foodContainerInventory.setInventorySlotContents(slotNum, stackInSlot);
		}
	}

	public void tryPullFoodFrom(@Nonnull ItemStack itemStack, IItemHandlerModifiable inventory, EntityPlayer player)
	{
		List<InventoryFoodInfo> foodsToPull = MealPrioritizationHelper.findBestFoodsForPlayerAccountingForVariety(player, inventory);
		if (!foodsToPull.isEmpty())
		{
			FoodContainerInventory foodContainerInventory = getInventory(itemStack);
			for (InventoryFoodInfo foodToPull : foodsToPull)
			{
				ItemStack stackInSlot = inventory.getStackInSlot(foodToPull.slotNum);

				if (stackInSlot.isEmpty())
					continue;

				stackInSlot = InventoryHelper.insertStackIntoInventoryOnce(stackInSlot, foodContainerInventory.getItemHandler());
				inventory.setStackInSlot(foodToPull.slotNum, stackInSlot);
			}
		}
	}

	public boolean canBeEatenFrom(@Nonnull ItemStack stack)
	{
		return isOpen(stack) && !isEmpty(stack);
	}

	public boolean canPlayerEatFrom(EntityPlayer player, @Nonnull ItemStack stack)
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
					ItemStack itemToDrop = InventoryHelper.removeRandomSingleItemFromInventory(getInventory(itemStack).getItemHandler(), random);
					player.dropItem(itemToDrop, true);
				}
			}
		}

		super.onUpdate(itemStack, world, ownerEntity, par4, par5);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, @Nullable World world, List<String> toolTip, ITooltipFlag tooltipFlag)
	{
		super.addInformation(itemStack, world, toolTip, tooltipFlag);

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
	@Nonnull
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		ItemStack itemStack = player.getHeldItem(hand);
		if (!world.isRemote && isOpen(itemStack))
		{
			IItemHandler inventoryHit = InventoryHelper.getInventoryAtLocation(world, pos);
			if (inventoryHit != null && inventoryHit instanceof IItemHandlerModifiable)
			{
				tryDumpFoodInto(itemStack, inventoryHit, player);
				tryPullFoodFrom(itemStack, (IItemHandlerModifiable) inventoryHit, player);

				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	@Nonnull
	public EnumAction getItemUseAction(ItemStack itemStack)
	{
		if (canBeEatenFrom(itemStack))
			return EnumAction.EAT;
		else
			return EnumAction.NONE;
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
	{
		ItemStack itemStack = player.getHeldItem(hand);
		if (player.isSneaking())
		{
			setIsOpen(itemStack, !isOpen(itemStack));
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
		}
		else if (canPlayerEatFrom(player, itemStack))
		{
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
		}
		else if (!isOpen(itemStack) && hand == EnumHand.MAIN_HAND)
		{
			GuiHelper.openGuiOfItemStack(player, itemStack);
			setIsOpen(itemStack, true);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
		}
		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
	{
		if (entityLiving.world.isRemote && ModConfig.LEFT_CLICK_OPENS_FOOD_CONTAINERS && MiscHelper.isMouseOverNothing())
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

	@Override
	@Nonnull
	public ItemStack onItemUseFinish(@Nonnull ItemStack itemStack, World world, EntityLivingBase entityLiving)
	{
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;
			IItemHandlerModifiable inventory = getInventory(itemStack).getItemHandler();

			int slotWithBestFood = MealPrioritizationHelper.findBestFoodForPlayerToEat(player, inventory);
			ItemStack foodToEat = inventory.getStackInSlot(slotWithBestFood);
			if (!foodToEat.isEmpty())
			{
				ItemStack result = foodToEat.onItemUseFinish(world, player);
				result = ForgeEventFactory.onItemUseFinish(player, foodToEat, 32, result);

				if (result.isEmpty() || result.getCount() <= 0)
					result = ItemStack.EMPTY;

				inventory.setStackInSlot(slotWithBestFood, result);
			}
		}
		return super.onItemUseFinish(itemStack, world, entityLiving);
	}

	public ItemStack getBestFoodForPlayerToEat(ItemStack itemStack, EntityPlayer player)
	{
		IItemHandler inventory = getInventory(itemStack).getItemHandler();
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
	public boolean isItemValidForSlot(NBTInventory inventory, int slotNum, @Nonnull ItemStack itemStack)
	{
		return FoodHelper.isFood(itemStack) && FoodHelper.isDirectlyEdible(itemStack);
	}

	/*
	 * IEdible implementation
	 */
	@Override
	public FoodValues getFoodValues(@Nonnull ItemStack itemStack)
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
