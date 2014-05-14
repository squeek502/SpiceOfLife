package squeek.spiceoflife.hooks;

import squeek.spiceoflife.ModSpiceOfLife;
import cpw.mods.fml.common.FMLCommonHandler;

public class HookFoodStats
{
	public static float getFoodModifier()
	{
		ModSpiceOfLife.Log.info((FMLCommonHandler.instance().getEffectiveSide().isClient() ? "[client] " : "[server] ") + "foodModifier");
		return 0f;
	}
}
