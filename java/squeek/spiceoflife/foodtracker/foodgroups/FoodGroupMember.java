package squeek.spiceoflife.foodtracker.foodgroups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.interfaces.IPackable;

public class FoodGroupMember implements IPackable
{
	boolean exactMetadata = false;
	boolean baseItemForRecipes = false;
	int maxRecipeLevel = 0;
	String oredictName = null;
	ItemStack itemStack = null;
	List<ItemStack> matchingItems = null;

	public boolean isFoodIncluded(ItemStack food)
	{
		for (ItemStack itemStack : matchingItems)
		{
			if (OreDictionary.itemMatches(itemStack, food, false))
				return true;
		}

		return false;
	}

	public List<ItemStack> findOutputsFromInputItems(List<ItemStack> inputItems)
	{
		List<ItemStack> outputs = new ArrayList<ItemStack>();

		@SuppressWarnings("unchecked")
		List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();

		for (IRecipe recipe : recipeList)
		{
			List<?> inputList = null;

			if (recipe instanceof ShapedRecipes)
				inputList = Arrays.asList(((ShapedRecipes) recipe).recipeItems);
			else if (recipe instanceof ShapelessRecipes)
				inputList = Arrays.asList(((ShapelessRecipes) recipe).recipeItems);
			else if (recipe instanceof ShapedOreRecipe)
				inputList = Arrays.asList(((ShapedOreRecipe) recipe).getInput());
			else if (recipe instanceof ShapelessOreRecipe)
				inputList = Arrays.asList(((ShapelessOreRecipe) recipe).getInput());

			if (inputList != null
					&& recipe.getRecipeOutput() != null
					&& anyItemsFoundInList(inputItems, inputList)
					&& !anyItemsFoundInList(Arrays.asList(recipe.getRecipeOutput()), inputItems))
			{
				outputs.add(recipe.getRecipeOutput());
			}
		}

		/*
		List<ItemStack> smeltingInputs = new ArrayList<ItemStack>();

		@SuppressWarnings("unchecked")
		Map<Integer, ItemStack> smeltingList = FurnaceRecipes.smelting().getSmeltingList();
		for (Entry<Integer, ItemStack> entry : smeltingList.entrySet())
		{
			if (matches(entry.getValue()))
				smeltingInputs.add(new ItemStack(entry.getKey(), 1, 0));
		}

		Map<List<Integer>, ItemStack> metaSmeltingList = FurnaceRecipes.smelting().getMetaSmeltingList();
		for (Entry<List<Integer>, ItemStack> entry : metaSmeltingList.entrySet())
		{
			if (matches(entry.getValue()))
				smeltingInputs.add(new ItemStack(entry.getKey().get(0), 1, entry.getKey().get(1)));
		}

		if (!smeltingInputs.isEmpty() && isInRecipeInputList(smeltingInputs))
			return true;
		*/

		return outputs;
	}

	public void initMatchingItemsList()
	{
		//ModSpiceOfLife.Log.info((FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT ? "[client] " : "[server] ") + "initMatchingItemsList");

		matchingItems = getBaseItemList();

		/*
		if (baseItemForRecipes)
		{
			// find all recipes with any found recipe's output item(s) as an input
			List<ItemStack> items = matchingItems;
			int level = 0;
			while (!items.isEmpty() && (level <= maxRecipeLevel || maxRecipeLevel == -1))
			{
				items = findOutputsFromInputItems(items);
				boolean didAdd = false;
				for (ItemStack item : items)
				{
					if (!matchingItems.contains(item))
					{
						matchingItems.add(item);
						didAdd = true;
					}
				}
				if (!didAdd)
					items.clear();
				level++;
			}
		}
		*/
	}

	public List<ItemStack> getBaseItemList()
	{
		return itemStack != null ? Arrays.asList(itemStack) : OreDictionary.getOres(oredictName);
	}

	public boolean anyItemsFoundInList(List<ItemStack> inputItems, List<?> inputList)
	{
		for (Object obj : inputList)
		{
			if (obj instanceof ItemStack)
			{
				for (ItemStack inputItem : inputItems)
				{
					if (OreDictionary.itemMatches((ItemStack) obj, inputItem, false))
						return true;
				}
			}
			else if (obj instanceof ArrayList)
			{
				if (anyItemsFoundInList(inputItems, (List<?>) obj))
					return true;
			}
		}

		return false;
	}

	/*
	public boolean matches(ItemStack otherItem)
	{
		if (itemStack != null)
			return (!exactMetadata && itemStack.itemID == otherItem.itemID) || (exactMetadata && itemStack.isItemEqual(otherItem));
		else if (oredictName != null)
			return OreDictionaryHelper.getOreNames(otherItem).contains(oredictName);
		else
			return false;
	}
	*/

	/*
	 * Constructors
	 */
	public FoodGroupMember()
	{
	}

	public FoodGroupMember(String oredictName)
	{
		this(oredictName, false);
	}

	public FoodGroupMember(String oredictName, boolean baseItemForRecipes)
	{
		this.oredictName = oredictName;
		this.baseItemForRecipes = baseItemForRecipes;
	}

	public FoodGroupMember(ItemStack itemStack, boolean exactMetadata)
	{
		this(itemStack, exactMetadata, false);
	}

	public FoodGroupMember(ItemStack itemStack, boolean exactMetadata, boolean baseItemForRecipes)
	{
		this.itemStack = itemStack;
		this.baseItemForRecipes = baseItemForRecipes;
		if (!exactMetadata)
			this.itemStack.setItemDamage(OreDictionary.WILDCARD_VALUE);
	}

	/*
	 * Packet handling
	 */
	@Override
	public void pack(IByteIO data)
	{
		data.writeItemStack(itemStack);
		data.writeUTF(oredictName != null ? oredictName : "");
		data.writeBoolean(exactMetadata);
		data.writeBoolean(baseItemForRecipes);
	}

	@Override
	public void unpack(IByteIO data)
	{
		itemStack = data.readItemStack();
		oredictName = data.readUTF();
		oredictName = oredictName != "" ? oredictName : null;
		exactMetadata = data.readBoolean();
		baseItemForRecipes = data.readBoolean();

		initMatchingItemsList();
	}
}
