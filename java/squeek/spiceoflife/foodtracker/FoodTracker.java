package squeek.spiceoflife.foodtracker;

import java.util.EnumSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModInfo;
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
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class FoodTracker implements IPlayerTracker, IConnectionHandler, ITickHandler
{
	/**
	 * Add relevant extended entity data whenever an entity comes into existence
	 */
	@ForgeSubscribe
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
	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
		// server needs to send config settings to the client
		ModConfig.sync((EntityPlayerMP) player);

		// server needs to send food groups to the client
		FoodGroupRegistry.sync((EntityPlayerMP) player);

		// server needs to send any loaded data to the client
		FoodHistory foodHistory = FoodHistory.get(player);
		foodHistory.validate();
		syncFoodHistory(foodHistory);

		// give food journal
		if (!foodHistory.wasGivenFoodJournal && (ModConfig.GIVE_FOOD_JOURNAL_ON_START || (ModConfig.GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS && ModConfig.FOOD_EATEN_THRESHOLD == 0)))
		{
			ItemFoodJournal.giveToPlayer(player);
			foodHistory.wasGivenFoodJournal = true;
		}
	}

	/**
	 * Resync food history whenever a player changes dimensions
	 */
	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		FoodHistory foodHistory = FoodHistory.get(player);
		syncFoodHistory(foodHistory);
	}

	/**
	 * Save death-persistent data to avoid any rollbacks on respawn
	 */
	@ForgeSubscribe
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
	@Override
	public void onPlayerRespawn(EntityPlayer player)
	{
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;

		// load any persistent food history data
		FoodHistory foodHistory = FoodHistory.get(player);
		foodHistory.loadNBTData(null);

		// server needs to send any loaded data to the client
		syncFoodHistory(foodHistory);
	}

	/**
	 * Sync saturation whenever it changes (vanilla MC only syncs when it hits 0)
	 */
	private float lastSaturationLevel = 0;
	private float lastExhaustionLevel = 0;

	@ForgeSubscribe
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

	public static void syncFoodHistory(FoodHistory foodHistory)
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

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			ModConfig.assumeClientOnly();
	}

	@Override
	public void connectionClosed(INetworkManager manager)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			ModConfig.assumeClientOnly();
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
	{
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
	{
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
	{
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
	{
	}

	private int lastDifficultySetting = 0;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		World world = (World) tickData[0];

		if (world instanceof WorldServer)
		{
			if (this.lastDifficultySetting != world.difficultySetting)
			{
				PacketDispatcher.get().sendToAll(new PacketDifficultySetting(world.difficultySetting));
				this.lastDifficultySetting = world.difficultySetting;
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel()
	{
		return ModInfo.MODID + "_World";
	}
}
