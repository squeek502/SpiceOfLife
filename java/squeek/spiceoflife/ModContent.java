package squeek.spiceoflife;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.spiceoflife.items.ItemFoodContainer;
import squeek.spiceoflife.items.ItemFoodJournal;

public class ModContent
{
	public static ItemFoodJournal foodJournal;
	public static ItemFoodContainer lunchBox;
	public static ItemFoodContainer lunchBag;

	public static void registerItems()
	{
		foodJournal = new ItemFoodJournal();
		GameRegistry.register(foodJournal);

		lunchBox = new ItemFoodContainer(ModConfig.ITEM_LUNCH_BOX_NAME, 6);
		GameRegistry.register(lunchBox);

		lunchBag = new ItemFoodContainer(ModConfig.ITEM_LUNCH_BAG_NAME, 3);
		GameRegistry.register(lunchBag);
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels()
	{
		foodJournal.registerModels();
		lunchBox.registerModels();
		lunchBag.registerModels();
	}
}
