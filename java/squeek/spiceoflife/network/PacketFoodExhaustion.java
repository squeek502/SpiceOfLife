package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.helpers.FoodHelper;
import cpw.mods.fml.relauncher.Side;

public class PacketFoodExhaustion extends PacketBase
{
	private float foodExhaustionLevel = 0f;

	public PacketFoodExhaustion()
	{
	}

	public PacketFoodExhaustion(float foodExhaustionLevel)
	{
		this.foodExhaustionLevel = foodExhaustionLevel;
	}

	@Override
	public void pack(IByteIO data)
	{
		data.writeFloat(foodExhaustionLevel);
	}

	@Override
	public void unpack(IByteIO data)
	{
		this.foodExhaustionLevel = data.readFloat();
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		FoodHelper.setExhaustionLevel(player.getFoodStats(), foodExhaustionLevel);
		return null;
	}
}
