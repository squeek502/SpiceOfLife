package squeek.spiceoflife.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;

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
	
	public PacketFoodHistory(FoodEaten foodEaten)
	{
		this.foodHistory = new FoodHistory(null);
		foodHistory.addFood(foodEaten);
	}

	@Override
	public void pack(DataOutputStream data) throws IOException
	{
		if (foodHistory == null)
			return;

		data.writeBoolean(shouldOverwrite);
		data.writeShort(foodHistory.getHistory().size());

		for (FoodEaten foodEaten : foodHistory.getHistory())
		{
			data.writeShort(foodEaten.hungerRestored);
			data.writeUTF(foodEaten.foodGroup != null ? foodEaten.foodGroup.identifier : "");
			Packet.writeItemStack(foodEaten.itemStack, data);
		}
	}

	@Override
	public void unpack(DataInputStream data, INetworkManager manager, EntityPlayer player) throws IOException
	{
		FoodHistory foodHistory = FoodHistory.get(player) == null ? new FoodHistory(player) : FoodHistory.get(player);

		shouldOverwrite = data.readBoolean();
		if (shouldOverwrite)
			foodHistory.getHistory().clear();
		
		short historySize = data.readShort();

		for (int i = 0; i < historySize; i++)
		{
			FoodEaten foodEaten = new FoodEaten();
			foodEaten.hungerRestored = data.readShort();
			foodEaten.foodGroup = FoodGroupRegistry.getFoodGroup(data.readUTF());
			foodEaten.itemStack = Packet.readItemStack(data);
			foodHistory.addFood(foodEaten, !shouldOverwrite);
		}
	}
}
