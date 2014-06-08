package squeek.spiceoflife.foodtracker.foodgroups;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class FoodGroup
{
	public String identifier;
	public String name;
	public int priority;

	private List<FoodGroupMember> foods = new ArrayList<FoodGroupMember>();

	public FoodGroup()
	{
		this(null, null, 0);
	}
	
	public FoodGroup(String identifier, String name)
	{
		this(identifier, name, 0);
	}

	public FoodGroup(String identifier, String name, int priority)
	{
		this.identifier = identifier;
		this.name = name;
		this.priority = priority;
	}

	public void init()
	{
		for (FoodGroupMember foodMember : foods)
		{
			foodMember.initMatchingItemsList();
		}
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
		return StatCollector.translateToLocal(name);
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
		addFood(itemStack, false);
	}
	
	public void addFood(ItemStack itemStack, boolean exactMetadata, boolean baseItemForRecipes)
	{
		addFood(new FoodGroupMember(itemStack, exactMetadata, baseItemForRecipes));
	}
	
	public void addFood(FoodGroupMember foodMember)
	{
		foods.add(foodMember);
	}

	public void pack(DataOutputStream data) throws IOException
	{
		data.writeUTF(identifier);
		data.writeUTF(name);
		data.writeShort(priority);
		data.writeShort(foods.size());
		
		for (FoodGroupMember foodMember : foods)
		{
			foodMember.pack(data);
		}
	}

	public void unpack(DataInputStream data, EntityPlayer player) throws IOException
	{
		identifier = data.readUTF();
		name = data.readUTF();
		priority = data.readShort();
		int size = data.readShort();
		
		for (int i=0; i<size; i++)
		{
			FoodGroupMember foodMember = new FoodGroupMember();
			foodMember.unpack(data, player);
			addFood(foodMember);
		}
	}
}
