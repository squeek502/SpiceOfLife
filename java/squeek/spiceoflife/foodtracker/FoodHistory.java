package squeek.spiceoflife.foodtracker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.foodtracker.capability.IFoodHistory;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.foodtracker.foodqueue.FixedHungerQueue;
import squeek.spiceoflife.foodtracker.foodqueue.FixedSizeQueue;
import squeek.spiceoflife.foodtracker.foodqueue.FixedTimeQueue;
import squeek.spiceoflife.foodtracker.foodqueue.FoodQueue;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.helpers.MiscHelper;
import squeek.spiceoflife.items.ItemFoodJournal;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FoodHistory implements IFoodHistory, ICapabilitySerializable<NBTTagCompound>
{
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(ModInfo.MODID, "History");
	public static final String TAG_KEY = ModInfo.MODID + "History";

	@CapabilityInject(IFoodHistory.class)
	public static final Capability<IFoodHistory> CAPABILITY = null;

	public final EntityPlayer player;
	protected FoodQueue history = FoodHistory.getNewFoodQueue();
	public int totalFoodsEatenAllTime = 0;
	public boolean wasGivenFoodJournal = false;
	public long ticksActive = 0;

	public FoodHistory()
	{
		this(null);
	}

	public FoodHistory(EntityPlayer player)
	{
		this.player = player;
	}

	@Override
	public void onHistoryTypeChanged()
	{
		FoodQueue oldHistory = history;
		history = FoodHistory.getNewFoodQueue();
		history.addAll(oldHistory);
	}

	public static FoodQueue getNewFoodQueue()
	{
		if (ModConfig.USE_HUNGER_QUEUE)
			return new FixedHungerQueue(ModConfig.FOOD_HISTORY_LENGTH);
		else if (ModConfig.USE_TIME_QUEUE)
			return new FixedTimeQueue((long) ModConfig.FOOD_HISTORY_LENGTH * MiscHelper.TICKS_PER_DAY);
		else
			return new FixedSizeQueue(ModConfig.FOOD_HISTORY_LENGTH);
	}

	@Override
	public void deltaTicksActive(long delta)
	{
		this.ticksActive += delta;
	}

	@Override
	public boolean addFood(FoodEaten foodEaten)
	{
		return addFood(foodEaten, true);
	}

	@Override
	public boolean addFood(FoodEaten foodEaten, boolean countsTowardsAllTime)
	{
		if (countsTowardsAllTime)
			totalFoodsEatenAllTime++;

		boolean isAtThreshold = countsTowardsAllTime && totalFoodsEatenAllTime == ModConfig.FOOD_EATEN_THRESHOLD;
		if (player != null && !player.world.isRemote)
		{
			if (ModConfig.GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS && !wasGivenFoodJournal && isAtThreshold)
			{
				ItemFoodJournal.giveToPlayer(player);
				wasGivenFoodJournal = true;
			}
			if (ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD && isAtThreshold)
			{
				history.clear();
				return true;
			}
		}
		return history.add(foodEaten);
	}

	@Override
	public int getFoodCountIgnoringFoodGroups(@Nonnull ItemStack food)
	{
		return getFoodCountForFoodGroup(food, null);
	}

	@Override
	public int getFoodCountForFoodGroup(@Nonnull ItemStack food, FoodGroup foodGroup)
	{
		int count = 0;

		for (FoodEaten foodEaten : history)
		{
			if (foodEaten.itemStack == ItemStack.EMPTY)
				continue;

			if (food.isItemEqual(foodEaten.itemStack) || foodEaten.getFoodGroups().contains(foodGroup))
			{
				count += 1;
			}
		}
		return count;
	}

	@Override
	public boolean containsFoodOrItsFoodGroups(@Nonnull ItemStack food)
	{
		Set<FoodGroup> foodGroups = FoodGroupRegistry.getFoodGroupsForFood(food);
		for (FoodEaten foodEaten : history)
		{
			if (foodEaten.itemStack == ItemStack.EMPTY)
				continue;

			if (food.isItemEqual(foodEaten.itemStack) || MiscHelper.collectionsOverlap(foodGroups, foodEaten.getFoodGroups()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * See {@link #getTotalFoodValuesForFoodGroup}
	 */
	@Override
	public FoodValues getTotalFoodValuesIgnoringFoodGroups(@Nonnull ItemStack food)
	{
		return getTotalFoodValuesForFoodGroup(food, null);
	}

	/**
	 * Note: the returned FoodValues is not a standard FoodValues.
	 * The saturationModifier is set to the total, not to a modifier
	 */
	@Override
	public FoodValues getTotalFoodValuesForFoodGroup(@Nonnull ItemStack food, FoodGroup foodGroup)
	{
		int totalHunger = 0;
		float totalSaturation = 0f;

		for (FoodEaten foodEaten : history)
		{
			if (foodEaten.itemStack == ItemStack.EMPTY)
				continue;

			if (food.isItemEqual(foodEaten.itemStack) || foodEaten.getFoodGroups().contains(foodGroup))
			{
				totalHunger += foodEaten.foodValues.hunger;
				totalSaturation += foodEaten.foodValues.getSaturationIncrement();
			}
		}

		if (totalHunger == 0)
			return new FoodValues(0, 0f);
		else
			return new FoodValues(totalHunger, totalSaturation);
	}

	@Override
	public FoodQueue getHistory()
	{
		return history;
	}

	@Override
	public int getHistoryLengthInRelevantUnits()
	{
		return ModConfig.USE_HUNGER_QUEUE ? ((FixedHungerQueue) history).hunger() : history.size();
	}

	@Override
	public FoodEaten getLastEatenFood()
	{
		return history.peekLast();
	}

	@Override
	public Set<FoodGroup> getDistinctFoodGroups()
	{
		Set<FoodGroup> distinctFoodGroups = new HashSet<FoodGroup>();
		for (FoodEaten foodEaten : history)
		{
			if (foodEaten.itemStack == ItemStack.EMPTY)
				continue;

			distinctFoodGroups.addAll(foodEaten.getFoodGroups());
		}
		return distinctFoodGroups;
	}

	@Override
	public void reset()
	{
		history.clear();
		totalFoodsEatenAllTime = 0;
		wasGivenFoodJournal = false;
		ticksActive = 0;
	}

	@Override
	public void validate()
	{
		List<FoodEaten> invalidFoods = new ArrayList<FoodEaten>();
		for (FoodEaten foodEaten : history)
		{
			if (!FoodHelper.isValidFood(foodEaten.itemStack))
			{
				invalidFoods.add(foodEaten);
			}
		}
		history.removeAll(invalidFoods);
		totalFoodsEatenAllTime -= invalidFoods.size();
	}

	@Nonnull
	public static FoodHistory get(EntityPlayer player)
	{
		if (player.hasCapability(CAPABILITY, null))
		{
			FoodHistory history = (FoodHistory) player.getCapability(CAPABILITY, null);
			if (history != null)
				return history;
		}
		throw new RuntimeException("[The Spice Of Life] Food history capability not found for " + player.toString());
	}

	@Override
	public void pack(IByteIO data)
	{
		data.writeLong(ticksActive);
		data.writeShort(getHistory().size());

		for (FoodEaten foodEaten : getHistory())
		{
			foodEaten.pack(data);
		}
	}

	@Override
	public void unpack(IByteIO data)
	{
		ticksActive = data.readLong();
		short historySize = data.readShort();

		for (int i = 0; i < historySize; i++)
		{
			FoodEaten foodEaten = new FoodEaten();
			foodEaten.unpack(data);
			addFood(foodEaten);
		}
	}

	@Override
	// null compound parameter means save persistent data only
	public void writeToNBTData(NBTTagCompound data)
	{
		NBTTagCompound rootPersistentCompound = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		NBTTagCompound nonPersistentCompound = new NBTTagCompound();
		NBTTagCompound persistentCompound = new NBTTagCompound();

		if (!history.isEmpty())
		{
			if (data != null || ModConfig.FOOD_HISTORY_PERSISTS_THROUGH_DEATH)
			{
				NBTTagCompound nbtHistory = new NBTTagCompound();

				history.writeToNBTData(nbtHistory);

				if (ModConfig.FOOD_HISTORY_PERSISTS_THROUGH_DEATH)
					persistentCompound.setTag("History", nbtHistory);
				else
					nonPersistentCompound.setTag("History", nbtHistory);
			}
		}
		if (totalFoodsEatenAllTime > 0)
		{
			persistentCompound.setInteger("Total", totalFoodsEatenAllTime);
		}
		if (wasGivenFoodJournal)
		{
			persistentCompound.setBoolean("FoodJournal", wasGivenFoodJournal);
		}
		if (ticksActive > 0)
		{
			persistentCompound.setLong("Ticks", ticksActive);
		}

		if (data != null && !nonPersistentCompound.hasNoTags())
			data.setTag(TAG_KEY, nonPersistentCompound);

		if (!persistentCompound.hasNoTags())
			rootPersistentCompound.setTag(TAG_KEY, persistentCompound);

		if (!player.getEntityData().hasKey(EntityPlayer.PERSISTED_NBT_TAG))
			player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, rootPersistentCompound);
	}

	@Override
	// null compound parameter means load persistent data only
	public void readFromNBTData(NBTTagCompound data)
	{
		NBTTagCompound rootPersistentCompound = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

		if ((data != null && data.hasKey(TAG_KEY)) || rootPersistentCompound.hasKey(TAG_KEY))
		{
			NBTTagCompound nonPersistentCompound = data != null ? data.getCompoundTag(TAG_KEY) : new NBTTagCompound();
			NBTTagCompound persistentCompound = rootPersistentCompound.getCompoundTag(TAG_KEY);

			NBTTagCompound nbtHistory = ModConfig.FOOD_HISTORY_PERSISTS_THROUGH_DEATH ? persistentCompound.getCompoundTag("History") : nonPersistentCompound.getCompoundTag("History");

			history.readFromNBTData(nbtHistory);

			totalFoodsEatenAllTime = persistentCompound.getInteger("Total");
			wasGivenFoodJournal = persistentCompound.getBoolean("FoodJournal");
			ticksActive = persistentCompound.getLong("Ticks");
		}
	}

	@Override public NBTTagCompound serializeNBT()
	{
		NBTTagCompound compound = new NBTTagCompound();
		writeToNBTData(compound);
		return compound;
	}

	@Override public void deserializeNBT(NBTTagCompound nbt)
	{
		readFromNBTData(nbt);
	}

	@Override public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing)
	{
		return capability == CAPABILITY;
	}

	@Override public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
	{
		return capability == CAPABILITY ? (T) this : null;
	}
}
