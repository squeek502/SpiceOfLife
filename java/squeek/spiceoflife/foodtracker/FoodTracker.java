package squeek.spiceoflife.foodtracker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import squeek.applecore.api.food.FoodEvent;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.compat.PacketDispatcher;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.foodtracker.foodqueue.FixedTimeQueue;
import squeek.spiceoflife.items.ItemFoodJournal;
import squeek.spiceoflife.network.PacketFoodEatenAllTime;
import squeek.spiceoflife.network.PacketFoodHistory;

public class FoodTracker
{
	/**
	 * Save food eaten to the history
	 */
	@SubscribeEvent
	public void onFoodEaten(FoodEvent.FoodEaten event)
	{
		if (event.player.worldObj.isRemote)
			return;

		FoodEaten foodEaten = new FoodEaten(event.food, event.player);
		foodEaten.foodValues = event.foodValues;

		FoodTracker.addFoodEatenByPlayer(foodEaten, event.player);
	}

	/**
	 * Add relevant extended entity data whenever an entity comes into existence
	 */
	@SubscribeEvent
	public void onAttachCapability(AttachCapabilitiesEvent.Entity event)
	{
		if (event.getEntity() instanceof EntityPlayer)
		{
			event.addCapability(FoodHistory.CAPABILITY_ID, new FoodHistory((EntityPlayer) event.getEntity()));
		}
	}

	/**
	 * Keep track of how many ticks the player has actively spent on the server,
	 * and make sure the food history prunes expired items
	 */
	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event)
	{
		if (!(event.getEntityLiving() instanceof EntityPlayer))
			return;

		FoodHistory foodHistory = FoodHistory.get((EntityPlayer) event.getEntityLiving());
		foodHistory.deltaTicksActive(1);

		if (ModConfig.USE_TIME_QUEUE && !ModConfig.USE_HUNGER_QUEUE)
		{
			FixedTimeQueue timeQueue = (FixedTimeQueue) foodHistory.getHistory();
			timeQueue.prune(event.getEntityLiving().worldObj.getTotalWorldTime(), foodHistory.ticksActive);
		}
	}

	/**
	 * Sync savedata/config whenever a player joins the server
	 */
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event)
	{
		// server needs to send config settings to the client
		ModConfig.sync((EntityPlayerMP) event.player);

		// server needs to send food groups to the client
		FoodGroupRegistry.sync((EntityPlayerMP) event.player);

		// server needs to send any loaded data to the client
		FoodHistory foodHistory = FoodHistory.get(event.player);
		foodHistory.validate();
		syncFoodHistory(foodHistory);

		// give food journal
		if (!foodHistory.wasGivenFoodJournal && (ModConfig.GIVE_FOOD_JOURNAL_ON_START || (ModConfig.GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS && ModConfig.FOOD_EATEN_THRESHOLD == 0)))
		{
			ItemFoodJournal.giveToPlayer(event.player);
			foodHistory.wasGivenFoodJournal = true;
		}
	}

	/**
	 * Resync food history whenever a player changes dimensions
	 */
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event)
	{
		FoodHistory foodHistory = FoodHistory.get(event.player);
		syncFoodHistory(foodHistory);
	}

	/**
	 * Save death-persistent data to avoid any rollbacks on respawn
	 */
	@SubscribeEvent
	public void onLivingDeathEvent(LivingDeathEvent event)
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient() || !(event.getEntity() instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer) event.getEntity();

		FoodHistory foodHistory = FoodHistory.get(player);
		foodHistory.writeToNBTData(null);
	}

	/**
	 * Load any death-persistent savedata on respawn and sync it
	 */
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;

		// load any persistent food history data
		FoodHistory foodHistory = FoodHistory.get(event.player);
		foodHistory.readFromNBTData(null);

		// server needs to send any loaded data to the client
		syncFoodHistory(foodHistory);
	}

	/**
	 * Assume the server doesn't have the mod
	 */
	@SubscribeEvent
	public void onClientConnectedToServer(ClientConnectedToServerEvent event)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			ModConfig.assumeClientOnly();
	}

	public static void syncFoodHistory(FoodHistory foodHistory)
	{
		PacketDispatcher.get().sendTo(new PacketFoodEatenAllTime(foodHistory.totalFoodsEatenAllTime), (EntityPlayerMP) foodHistory.player);
		PacketDispatcher.get().sendTo(new PacketFoodHistory(foodHistory, true), (EntityPlayerMP) foodHistory.player);
	}

	public static boolean addFoodEatenByPlayer(FoodEaten foodEaten, EntityPlayer player)
	{
		// client needs to be told by the server otherwise the client can get out of sync easily
		if (!player.worldObj.isRemote && player instanceof EntityPlayerMP)
			PacketDispatcher.get().sendTo(new PacketFoodHistory(foodEaten), (EntityPlayerMP) player);

		return FoodHistory.get(player).addFood(foodEaten);
	}

	public static int getFoodHistoryLengthInRelevantUnits(EntityPlayer player)
	{
		return FoodHistory.get(player).getHistoryLengthInRelevantUnits();
	}

	public static ItemStack getFoodLastEatenBy(EntityPlayer player)
	{
		return FoodHistory.get(player).getLastEatenFood().itemStack;
	}
}
