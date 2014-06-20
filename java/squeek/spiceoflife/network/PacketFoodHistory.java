package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.FoodHistory;
import cpw.mods.fml.relauncher.Side;

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
		this.foodHistory = new FoodHistory();
		foodHistory.addFood(foodEaten);
	}

	@Override
	public void pack(IByteIO data)
	{
		if (foodHistory == null)
			return;

		data.writeBoolean(shouldOverwrite);
		data.writeShort(foodHistory.getHistory().size());

		for (FoodEaten foodEaten : foodHistory.getHistory())
		{
			foodEaten.pack(data);
		}
	}

	@Override
	public void unpack(IByteIO data)
	{
		this.foodHistory = new FoodHistory();
		shouldOverwrite = data.readBoolean();
		
		short historySize = data.readShort();

		for (int i = 0; i < historySize; i++)
		{
			FoodEaten foodEaten = new FoodEaten();
			foodEaten.unpack(data);
			foodHistory.addFood(foodEaten);
		}
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		FoodHistory foodHistory = FoodHistory.get(player);

		if (shouldOverwrite)
			foodHistory.getHistory().clear();
		
		for (FoodEaten foodEaten : this.foodHistory.getHistory())
		{
			foodHistory.addFood(foodEaten, !shouldOverwrite);
		}
		
		return null;
	}
}
