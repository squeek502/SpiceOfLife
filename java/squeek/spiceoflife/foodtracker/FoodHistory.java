package squeek.spiceoflife.foodtracker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModInfo;

public class FoodHistory implements IExtendedEntityProperties
{
	public static final String TAG_KEY = ModInfo.MODID + "History";
	public final EntityPlayer player;
	public final FixedSizeQueue<ItemStack> history = new FixedSizeQueue<ItemStack>(ModConfig.FOOD_HISTORY_LENGTH);
	public int totalFoodsEatenAllTime = 0;

	public FoodHistory(EntityPlayer player)
	{
		this.player = player;
		if (player != null)
			player.registerExtendedProperties(FoodHistory.TAG_KEY, this);
	}

	public boolean addFood(ItemStack food)
	{
		return addFood(food, true);
	}

	public boolean addFood(ItemStack food, boolean countsTowardsAllTime)
	{
		if (countsTowardsAllTime)
			totalFoodsEatenAllTime++;

		if (ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD && countsTowardsAllTime && totalFoodsEatenAllTime == ModConfig.FOOD_EATEN_THRESHOLD)
		{
			history.clear();
			return true;
		}
		else
			return history.add(food);
	}

	public int getFoodCount(ItemStack food)
	{
		int count = 0;
		for (ItemStack foodInHistory : history)
		{
			if (food.isItemEqual(foodInHistory) && ItemStack.areItemStackTagsEqual(food, foodInHistory))
				count += 1;
		}
		return count;
	}

	public int getHistorySize()
	{
		return history.size();
	}

	public ItemStack getLastEatenFood()
	{
		return history.peekLast();
	}

	public static FoodHistory get(EntityPlayer player)
	{
		return (FoodHistory) player.getExtendedProperties(TAG_KEY);
	}

	@Override
	// null compound parameter means save persistent data only
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound rootPersistentCompound = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		NBTTagCompound nonPersistentCompound = new NBTTagCompound();
		NBTTagCompound persistentCompound = new NBTTagCompound();

		if (history.size() > 0)
		{
			if (compound != null || ModConfig.FOOD_HISTORY_PERSISTS_THROUGH_DEATH)
			{
				NBTTagList nbtHistory = new NBTTagList();
				for (ItemStack food : history)
				{
					NBTTagCompound nbtFood = new NBTTagCompound();
					food.writeToNBT(nbtFood);
					nbtHistory.appendTag(nbtFood);
				}

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

		if (compound != null && !nonPersistentCompound.hasNoTags())
			compound.setCompoundTag(TAG_KEY, nonPersistentCompound);

		if (!persistentCompound.hasNoTags())
			rootPersistentCompound.setCompoundTag(TAG_KEY, persistentCompound);

		if (!player.getEntityData().hasKey(EntityPlayer.PERSISTED_NBT_TAG))
			player.getEntityData().setCompoundTag(EntityPlayer.PERSISTED_NBT_TAG, rootPersistentCompound);
	}

	@Override
	// null compound parameter means load persistent data only
	public void loadNBTData(NBTTagCompound compound)
	{
		NBTTagCompound rootPersistentCompound = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

		if ((compound != null && compound.hasKey(TAG_KEY)) || rootPersistentCompound.hasKey(TAG_KEY))
		{
			NBTTagCompound nonPersistentCompound = compound != null ? compound.getCompoundTag(TAG_KEY) : new NBTTagCompound();
			NBTTagCompound persistentCompound = rootPersistentCompound.getCompoundTag(TAG_KEY);

			NBTTagList nbtHistory = ModConfig.FOOD_HISTORY_PERSISTS_THROUGH_DEATH ? persistentCompound.getTagList("History") : nonPersistentCompound.getTagList("History");

			for (int i = 0; i < nbtHistory.tagCount(); i++)
			{
				NBTTagCompound nbtFood = (NBTTagCompound) nbtHistory.tagAt(i);

				addFood(ItemStack.loadItemStackFromNBT(nbtFood), false);
			}

			totalFoodsEatenAllTime = persistentCompound.getInteger("Total");
		}
	}

	@Override
	public void init(Entity entity, World world)
	{
	}
}
