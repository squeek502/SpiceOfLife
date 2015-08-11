package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import cpw.mods.fml.relauncher.Side;

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
	public void pack(IByteIO data)
	{
		if (foodGroup == null)
			return;

		foodGroup.pack(data);
	}

	@Override
	public void unpack(IByteIO data)
	{
		foodGroup = new FoodGroup();
		foodGroup.unpack(data);
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		foodGroup.init();
		FoodGroupRegistry.addFoodGroup(foodGroup);
		return null;
	}
}