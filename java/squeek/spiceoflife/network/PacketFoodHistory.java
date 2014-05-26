package squeek.spiceoflife.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import squeek.spiceoflife.foodtracker.FoodHistory;

public class PacketFoodHistory extends PacketBase
{
	private FoodHistory foodHistory = null;
	private boolean shouldOverwrite = false;

	public PacketFoodHistory()
	{
	}
	
	public PacketFoodHistory(FoodHistory foodHistory)
	{
		this.foodHistory = foodHistory;
	}
	
	public PacketFoodHistory(FoodHistory foodHistory, boolean shouldOverwrite)
	{
		this(foodHistory);
		this.shouldOverwrite = shouldOverwrite;
	}
	
	public PacketFoodHistory(ItemStack food)
	{
		this.foodHistory = new FoodHistory(null);
		foodHistory.addFood(food);
	}

	@Override
	public void pack(DataOutputStream data) throws IOException
	{
		if (foodHistory == null)
			return;

		data.writeBoolean(shouldOverwrite);
		data.writeShort(foodHistory.getHistorySize());

		for (ItemStack food : foodHistory.history)
		{
			Packet.writeItemStack(food, data);
		}
	}

	@Override
	public void unpack(DataInputStream data, INetworkManager manager, EntityPlayer player) throws IOException
	{
		FoodHistory foodHistory = FoodHistory.get(player) == null ? new FoodHistory(player) : FoodHistory.get(player);

		shouldOverwrite = data.readBoolean();
		if (shouldOverwrite)
			foodHistory.history.clear();
		
		short historySize = data.readShort();

		for (int i = 0; i < historySize; i++)
		{
			foodHistory.addFood(Packet.readItemStack(data), !shouldOverwrite);
		}
	}
}
