package squeek.spiceoflife.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;

public class PacketFoodGroup extends PacketBase
{
	private FoodGroup foodGroup = null;

	public PacketFoodGroup()
	{
	}
	
	public PacketFoodGroup(FoodGroup foodGroup)
	{
		this.foodGroup = foodGroup;
	}

	@Override
	public void pack(DataOutputStream data) throws IOException
	{
		if (foodGroup == null)
			return;
		
		foodGroup.pack(data);
	}

	@Override
	public void unpack(DataInputStream data, INetworkManager manager, EntityPlayer player) throws IOException
	{
		FoodGroup foodGroup = new FoodGroup();
		foodGroup.unpack(data, player);
		FoodGroupRegistry.addFoodGroup(foodGroup);
	}
}