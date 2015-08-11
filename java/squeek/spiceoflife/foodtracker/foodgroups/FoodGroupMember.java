package squeek.spiceoflife.foodtracker.foodgroups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.interfaces.IPackable;

public class FoodGroupMember implements IPackable
{
	boolean exactMetadata = false;
	boolean baseItemForRecipes = false;
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

	public void initMatchingItemsList()
	{
		matchingItems = getBaseItemList();
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
		oredictName = !oredictName.equals("") ? oredictName : null;
		exactMetadata = data.readBoolean();
		baseItemForRecipes = data.readBoolean();
	}
}
