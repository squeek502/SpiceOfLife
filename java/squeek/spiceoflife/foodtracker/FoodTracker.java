package squeek.spiceoflife.foodtracker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.compat.CompatHelper;
import squeek.spiceoflife.compat.PacketDispatcher;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.items.ItemFoodJournal;
import squeek.spiceoflife.network.PacketDifficultySetting;
import squeek.spiceoflife.network.PacketFoodEatenAllTime;
import squeek.spiceoflife.network.PacketFoodExhaustion;
import squeek.spiceoflife.network.PacketFoodHistory;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class FoodTracker
{
	/**
	 * Add relevant extended entity data whenever an entity comes into existence
	 */
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if (event.entity instanceof EntityPlayer)
		{
			FoodHistory.get((EntityPlayer) event.entity);
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
		if (FMLCommonHandler.instance().getEffectiveSide().isClient() || !(event.entity instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer) event.entity;

		FoodHistory foodHistory = FoodHistory.get(player);
		foodHistory.saveNBTData(null);
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
		foodHistory.loadNBTData(null);

		// server needs to send any loaded data to the client
		syncFoodHistory(foodHistory);
	}

	/**
	 * Assume the server doesn't have the mod
	 */
	@SubscribeEvent
	public void onClientConnectedToServer(ClientConnectedToServerEvent event)
	{
		ModConfig.assumeClientOnly();
	}
	
	/**
	 * Sync saturation whenever it changes (vanilla MC only syncs when it hits 0)
	 */
	private float lastSaturationLevel = 0;
	private float lastExhaustionLevel = 0;

	@SubscribeEvent
	public void onLivingUpdateEvent(LivingUpdateEvent event)
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient() || !(event.entity instanceof EntityPlayer))
			return;

		EntityPlayerMP player = (EntityPlayerMP) event.entity;

		if (this.lastSaturationLevel != player.getFoodStats().getSaturationLevel())
		{
			CompatHelper.sendPlayerHealthUpdatePacket(player);
			this.lastSaturationLevel = player.getFoodStats().getSaturationLevel();
		}

		float exhaustionLevel = FoodHelper.getExhaustionLevel(player.getFoodStats());
		if (Math.abs(this.lastExhaustionLevel - exhaustionLevel) >= 0.01f)
		{
			PacketDispatcher.get().sendTo(new PacketFoodExhaustion(exhaustionLevel), player);
			this.lastExhaustionLevel = exhaustionLevel;
		}
	}

	public void syncFoodHistory(FoodHistory foodHistory)
	{
		PacketDispatcher.get().sendTo(new PacketFoodEatenAllTime(foodHistory.totalFoodsEatenAllTime), (EntityPlayerMP) foodHistory.player);
		PacketDispatcher.get().sendTo(new PacketFoodHistory(foodHistory, true), (EntityPlayerMP) foodHistory.player);
	}

	public static boolean addFoodEatenByPlayer(FoodEaten foodEaten, EntityPlayer player)
	{
		// client needs to be told by the server otherwise the client can get out of sync easily
		if (!player.worldObj.isRemote)
			PacketDispatcher.get().sendTo(new PacketFoodHistory(foodEaten), (EntityPlayerMP) player);

		return FoodHistory.get(player).addFood(foodEaten);
	}

	public static int getFoodHistoryCountOf(ItemStack food, EntityPlayer player)
	{
		return FoodHistory.get(player).getFoodCount(food);
	}

	public static int getFoodHistoryCountOfLastEatenBy(EntityPlayer player)
	{
		return FoodHistory.get(player).getFoodCount(getFoodLastEatenBy(player));
	}

	public static int getFoodHistoryLengthInRelevantUnits(EntityPlayer player)
	{
		return FoodHistory.get(player).getHistoryLengthInRelevantUnits();
	}

	public static ItemStack getFoodLastEatenBy(EntityPlayer player)
	{
		return FoodHistory.get(player).getLastEatenFood().itemStack;
	}

	private int lastDifficultySetting = 0;

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
			return;

		if (event.world instanceof WorldServer)
		{
			if (this.lastDifficultySetting != event.world.difficultySetting.getDifficultyId())
			{
				PacketDispatcher.get().sendToAll(new PacketDifficultySetting(event.world.difficultySetting.getDifficultyId()));
				this.lastDifficultySetting = event.world.difficultySetting.getDifficultyId();
			}
		}
	}
}
