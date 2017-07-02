package squeek.spiceoflife;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.FoodTracker;
import squeek.spiceoflife.foodtracker.capability.IFoodHistory;
import squeek.spiceoflife.foodtracker.commands.CommandResetHistory;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupConfig;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.gui.TooltipHandler;
import squeek.spiceoflife.helpers.GuiHelper;
import squeek.spiceoflife.helpers.MovementHelper;
import squeek.spiceoflife.network.PacketHandler;

import java.io.File;
import java.util.concurrent.Callable;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, dependencies = "required-after:applecore")
public class ModSpiceOfLife
{
	public static final Logger Log = LogManager.getLogger(ModInfo.MODID);

	@Instance(ModInfo.MODID)
	public static ModSpiceOfLife instance;
	public File sourceFile;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		sourceFile = event.getSourceFile();
		ModConfig.init(event.getSuggestedConfigurationFile());
		ModContent.registerItems();
		if (event.getSide() == Side.CLIENT)
		{
			ModContent.registerModels();
		}
		CapabilityManager.INSTANCE.register(IFoodHistory.class, new Capability.IStorage<IFoodHistory>()
		{
			@Override
			public NBTBase writeNBT(Capability<IFoodHistory> capability, IFoodHistory instance, EnumFacing side)
			{
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<IFoodHistory> capability, IFoodHistory instance, EnumFacing side, NBTBase nbt)
			{
				instance.deserializeNBT((NBTTagCompound) nbt);
			}
		}, new Callable<IFoodHistory>()
		{
			@Override
			public IFoodHistory call() throws Exception
			{
				return new FoodHistory();
			}
		});
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		GuiHelper.init();
		MovementHelper.init();
		FoodTracker foodTracker = new FoodTracker();
		MinecraftForge.EVENT_BUS.register(foodTracker);
		MinecraftForge.EVENT_BUS.register(new FoodModifier());

		if (event.getSide() == Side.CLIENT)
		{
			MinecraftForge.EVENT_BUS.register(new TooltipHandler());
		}

		// need to make sure that the packet types get registered before packets are received
		PacketHandler.PacketType.values();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		FoodGroupConfig.load();
		FMLInterModComms.sendRuntimeMessage(ModInfo.MODID, "VersionChecker", "addVersionCheck", "http://www.ryanliptak.com/minecraft/versionchecker/squeek502/SpiceOfLife");
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		FoodGroupRegistry.setInStone();
		event.registerServerCommand(new CommandResetHistory());
	}
}
