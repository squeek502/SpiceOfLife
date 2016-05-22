package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import squeek.spiceoflife.compat.IByteIO;
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
	public void pack(IByteIO data)
	{
		data.writeInt(foodEatenAllTime);
	}

	@Override
	public void unpack(IByteIO data)
	{
		this.foodEatenAllTime = data.readInt();
	}

	@Override
	public void processInWorldThread(Side side, EntityPlayer player)
	{
		FoodHistory foodHistory = FoodHistory.get(player);

		foodHistory.totalFoodsEatenAllTime = this.foodEatenAllTime;
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		return null;
	}
}
