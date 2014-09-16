package squeek.spiceoflife.foodtracker.foodgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.interfaces.IPackable;

public class FoodGroup implements IPackable
{
	transient public String identifier;
	transient private List<FoodGroupMember> foods = new ArrayList<FoodGroupMember>();
	transient private FoodModifier foodModifier;

	public boolean enabled = true;
	public String name = null;
	public int priority = 0;
	public boolean blacklist = false;
	public boolean hidden = false;
	public String formula = null;
	@SerializedName("food")
	public Map<String, List<String>> foodStringsByType;

	public FoodGroup()
	{
	}

	public FoodGroup(String identifier, String name, int priority)
	{
		this.identifier = identifier;
		this.name = name;
		this.priority = priority;
	}

	public void initFromConfig()
	{
		List<String> oredictStrings = foodStringsByType.get("oredict");
		if (oredictStrings != null)
		{
			for (String oredictString : oredictStrings)
			{
				addFood(oredictString);
			}
		}

		List<String> itemStrings = foodStringsByType.get("items");
		if (itemStrings != null)
		{
			for (String itemString : itemStrings)
			{
				addItemFromString(itemString);
			}
		}
	}
	
	public void init()
	{
		for (FoodGroupMember foodMember : foods)
		{
			foodMember.initMatchingItemsList();
		}
		foodModifier = formula != null ? new FoodModifier(formula) : FoodModifier.GLOBAL;
	}

	public boolean isFoodIncluded(ItemStack food)
	{
		for (FoodGroupMember foodMember : foods)
		{
			if (foodMember.isFoodIncluded(food))
				return true;
		}
		return false;
	}

	public String getLocalizedName()
	{
		if (name != null)
			return StatCollector.translateToLocal(name);
		else
			return StatCollector.translateToLocal("spiceoflife.foodgroup." + identifier);
	}

	public FoodModifier getFoodModifier()
	{
		return foodModifier;
	}

	public void addFood(String oredictName)
	{
		addFood(oredictName, false);
	}

	public void addFood(String oredictName, boolean baseItemForRecipes)
	{
		addFood(new FoodGroupMember(oredictName, baseItemForRecipes));
	}

	public void addFood(ItemStack itemStack, boolean exactMetadata)
	{
		addFood(itemStack, exactMetadata, false);
	}
	
	public void addFood(ItemStack itemStack, boolean exactMetadata, boolean baseItemForRecipes)
	{
		addFood(new FoodGroupMember(itemStack, exactMetadata, baseItemForRecipes));
	}
	
	public void addFood(FoodGroupMember foodMember)
	{
		foods.add(foodMember);
	}

	public void addItemFromString(String itemString)
	{
		addItemFromString(itemString, false);
	}

	public void addItemFromString(String itemString, boolean isBaseItem)
	{
		String[] itemStringParts = itemString.split(":");
		if (itemStringParts.length > 0)
		{
			int itemId = Integer.parseInt(itemStringParts[0]);
			boolean exactMetadata = itemStringParts.length > 1 && itemStringParts[1] != "*";
			int metadata = itemStringParts.length > 1 && exactMetadata ? Integer.parseInt(itemStringParts[1]) : 0;
			addFood(new ItemStack(itemId, 1, metadata), exactMetadata, isBaseItem);
		}
	}

	@Override
	public void pack(IByteIO data)
	{
		data.writeUTF(identifier);
		data.writeUTF(name != null ? name : "");
		data.writeUTF(formula != null ? formula : "");
		data.writeShort(priority);
		data.writeBoolean(blacklist);
		data.writeBoolean(hidden);
		data.writeShort(foods.size());

		for (FoodGroupMember foodMember : foods)
		{
			foodMember.pack(data);
		}
	}

	@Override
	public void unpack(IByteIO data)
	{
		identifier = data.readUTF();
		name = data.readUTF();
		name = !name.equals("") ? name : null;
		formula = data.readUTF();
		formula = !formula.equals("") ? formula : null;
		priority = data.readShort();
		blacklist = data.readBoolean();
		hidden = data.readBoolean();
		int size = data.readShort();

		for (int i=0; i<size; i++)
		{
			FoodGroupMember foodMember = new FoodGroupMember();
			foodMember.unpack(data);
			addFood(foodMember);
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
			return true;
		if (obj instanceof FoodGroup)
			return ((FoodGroup) obj).identifier.equals(identifier);

		return false;
	}
}
