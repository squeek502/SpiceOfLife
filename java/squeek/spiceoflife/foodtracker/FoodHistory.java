package squeek.spiceoflife.foodtracker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.ISaveable;

public class FoodHistory implements IExtendedEntityProperties, ISaveable, IPackable
{
	public static final String TAG_KEY = ModInfo.MODID + "History";
	public final EntityPlayer player;
	protected FixedSizeQueue<FoodEaten> history = ModConfig.USE_HUNGER_QUEUE ? new FixedHungerQueue(ModConfig.FOOD_HISTORY_LENGTH) : new FixedFoodQueue(ModConfig.FOOD_HISTORY_LENGTH);
	public int totalFoodsEatenAllTime = 0;

	public FoodHistory()
	{
		this(null);
	}

	public FoodHistory(EntityPlayer player)
	{
		this.player = player;
		if (player != null)
			player.registerExtendedProperties(FoodHistory.TAG_KEY, this);
	}

	public void onHistoryTypeChanged()
	{
		FixedSizeQueue<FoodEaten> oldHistory = history;
		history = ModConfig.USE_HUNGER_QUEUE ? new FixedHungerQueue(ModConfig.FOOD_HISTORY_LENGTH) : new FixedFoodQueue(ModConfig.FOOD_HISTORY_LENGTH);
		history.addAll(oldHistory);
	}

	public boolean addFood(FoodEaten foodEaten)
	{
		return addFood(foodEaten, true);
	}

	public boolean addFood(FoodEaten foodEaten, boolean countsTowardsAllTime)
	{
		if (countsTowardsAllTime)
			totalFoodsEatenAllTime++;

		if (ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD && countsTowardsAllTime && totalFoodsEatenAllTime == ModConfig.FOOD_EATEN_THRESHOLD)
		{
			history.clear();
			return true;
		}
		else
			return history.add(foodEaten);
	}

	public int getFoodCount(ItemStack food)
	{
		int count = 0;
		FoodGroup foodGroup = null;
		
		if (ModConfig.USE_FOOD_GROUPS)
			foodGroup = FoodGroupRegistry.getFoodGroupForFood(food);

		for (FoodEaten foodEaten : history)
		{
			if ((food.isItemEqual(foodEaten.itemStack) && ItemStack.areItemStackTagsEqual(food, foodEaten.itemStack))
					||
					(ModConfig.USE_FOOD_GROUPS && foodGroup != null && foodGroup.equals(foodEaten.foodGroup)))
			{
				count += 1;
			}
		}
		return count;
	}

	public FixedSizeQueue<FoodEaten> getHistory()
	{
		return history;
	}
	
	public int getHistoryLengthInRelevantUnits()
	{
		return ModConfig.USE_HUNGER_QUEUE ? ((FixedHungerQueue) history).hunger() : history.size();
	}

	public FoodEaten getLastEatenFood()
	{
		return history.peekLast();
	}

	public static FoodHistory get(EntityPlayer player)
	{
		FoodHistory foodHistory = (FoodHistory) player.getExtendedProperties(TAG_KEY);
		if (foodHistory == null)
			foodHistory = new FoodHistory(player);
		return foodHistory;
	}

	@Override
	public void pack(IByteIO data)
	{
		data.writeShort(getHistory().size());

		for (FoodEaten foodEaten : getHistory())
		{
			foodEaten.pack(data);
		}
	}

	@Override
	public void unpack(IByteIO data)
	{
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

		if (history.size() > 0)
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
		}
	}
	
	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		writeToNBTData(compound);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		readFromNBTData(compound);
	}

	@Override
	public void init(Entity entity, World world)
	{
	}
}
