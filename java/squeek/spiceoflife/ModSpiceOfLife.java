package squeek.spiceoflife;

import java.util.logging.Logger;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, dependencies = "")
public class ModSpiceOfLife
{
	public static final Logger Log = Logger.getLogger(ModInfo.MODID);
	static
	{
		Log.setParent(FMLLog.getLogger());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
