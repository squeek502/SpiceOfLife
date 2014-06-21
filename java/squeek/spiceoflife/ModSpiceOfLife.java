package squeek.spiceoflife;

import java.util.logging.Logger;
import net.minecraftforge.common.MinecraftForge;
import squeek.spiceoflife.foodtracker.FoodTracker;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.gui.HUDOverlayHandler;
import squeek.spiceoflife.gui.TooltipOverlayHandler;
import squeek.spiceoflife.gui.TooltipHandler;
import squeek.spiceoflife.network.PacketHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, dependencies = "after:HungerOverhaul;after:TConstruct")
@NetworkMod(channels = {ModInfo.NETCHANNEL}, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class ModSpiceOfLife
{
	public static final Logger Log = Logger.getLogger(ModInfo.MODID);
	static
	{
		Log.setParent(FMLLog.getLogger());
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModConfig.init(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		FoodTracker foodTracker = new FoodTracker();
		MinecraftForge.EVENT_BUS.register(foodTracker);
		GameRegistry.registerPlayerTracker(foodTracker);
		NetworkRegistry.instance().registerConnectionHandler(foodTracker);
		if (event.getSide() == Side.CLIENT)
		{
			MinecraftForge.EVENT_BUS.register(new TooltipHandler());
			MinecraftForge.EVENT_BUS.register(new HUDOverlayHandler());
		    TickRegistry.registerTickHandler(new TooltipOverlayHandler(), Side.CLIENT);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}

	@EventHandler
    public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new Command());
		FoodGroupRegistry.serverInit();
	}
}
