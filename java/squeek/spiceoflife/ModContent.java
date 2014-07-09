package squeek.spiceoflife;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.items.ItemFoodJournal;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModContent
{
	public static ItemFoodJournal foodJournal;

	public static void registerItems()
	{
		foodJournal = new ItemFoodJournal(ModConfig.ITEM_FOOD_JOURNAL_ID);

		GameRegistry.registerItem(foodJournal, ModConfig.ITEM_FOOD_JOURNAL_NAME);
	}
	
	public static void registerRecipes()
	{
		GameRegistry.addShapelessRecipe(new ItemStack(foodJournal), new ItemStack(Item.wheat), new ItemStack(Item.paper));
	}
}
