package squeek.spiceoflife;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.items.ItemFoodJournal;
import squeek.spiceoflife.items.ItemFoodContainer;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModContent
{
	public static ItemFoodJournal foodJournal;
	public static ItemFoodContainer lunchBox;
	public static ItemFoodContainer lunchBag;

	public static void registerItems()
	{
		foodJournal = new ItemFoodJournal(ModConfig.ITEM_FOOD_JOURNAL_ID);
		GameRegistry.registerItem(foodJournal, ModConfig.ITEM_FOOD_JOURNAL_NAME);

		lunchBox = new ItemFoodContainer(ModConfig.ITEM_LUNCH_BOX_ID, ModConfig.ITEM_LUNCH_BOX_NAME, 6);
		GameRegistry.registerItem(lunchBox, ModConfig.ITEM_LUNCH_BOX_NAME);

		lunchBag = new ItemFoodContainer(ModConfig.ITEM_LUNCH_BAG_ID, ModConfig.ITEM_LUNCH_BAG_NAME, 3);
		GameRegistry.registerItem(lunchBag, ModConfig.ITEM_LUNCH_BAG_NAME);
	}

	public static void registerRecipes()
	{
		GameRegistry.addShapelessRecipe(new ItemStack(foodJournal), new ItemStack(Item.wheat), new ItemStack(Item.paper));
		GameRegistry.addShapedRecipe(new ItemStack(lunchBox), "_ _", " _ ", '_', new ItemStack(Block.pressurePlateIron));
		GameRegistry.addShapedRecipe(new ItemStack(lunchBag), "p p", " p ", 'p', new ItemStack(Item.paper));
	}
}
