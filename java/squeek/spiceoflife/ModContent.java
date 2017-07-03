package squeek.spiceoflife;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.spiceoflife.items.ItemFoodContainer;
import squeek.spiceoflife.items.ItemFoodJournal;

@Mod.EventBusSubscriber
public class ModContent
{
	public static ItemFoodJournal foodJournal;
	public static ItemFoodContainer lunchBox;
	public static ItemFoodContainer lunchBag;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		foodJournal = new ItemFoodJournal();
		event.getRegistry().register(foodJournal);

		lunchBox = new ItemFoodContainer(ModConfig.ITEM_LUNCH_BOX_NAME, 6);
		event.getRegistry().register(lunchBox);

		lunchBag = new ItemFoodContainer(ModConfig.ITEM_LUNCH_BAG_NAME, 3);
		event.getRegistry().register(lunchBag);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		foodJournal.registerModels();
		lunchBox.registerModels();
		lunchBag.registerModels();
	}
}
