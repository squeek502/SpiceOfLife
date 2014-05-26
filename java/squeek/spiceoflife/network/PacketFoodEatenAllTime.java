package squeek.spiceoflife.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import squeek.spiceoflife.foodtracker.FoodHistory;

public class PacketFoodEatenAllTime extends PacketBase
{
	private int foodEatenAllTime = 0;
	
	public PacketFoodEatenAllTime()
	{
	}
	
	public PacketFoodEatenAllTime(int foodEatenAllTime)
	{
		this.foodEatenAllTime = foodEatenAllTime;
	}

	@Override
	public void pack(DataOutputStream data) throws IOException
	{
		data.writeInt(foodEatenAllTime);
	}

	@Override
	public void unpack(DataInputStream data, INetworkManager manager, EntityPlayer player) throws IOException
	{
		FoodHistory foodHistory = FoodHistory.get(player) == null ? new FoodHistory(player) : FoodHistory.get(player);
		
		foodHistory.totalFoodsEatenAllTime = data.readInt();
	}
}
