package squeek.spiceoflife;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import squeek.spiceoflife.items.ItemFoodJournal;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModContent
{
	public static ItemFoodJournal foodJournal;

	public static void registerItems()
	{
		foodJournal = new ItemFoodJournal();

		GameRegistry.registerItem(foodJournal, ModConfig.ITEM_FOOD_JOURNAL_NAME);
	}
	
	public static void registerRecipes()
	{
		GameRegistry.addShapelessRecipe(new ItemStack(foodJournal), new ItemStack(Items.wheat), new ItemStack(Items.paper));
	}
}
