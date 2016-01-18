package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.compat.IByteIO;

public class PacketConfigSync extends PacketBase
{
	public PacketConfigSync()
	{
	}

	@Override
	public void pack(IByteIO data)
	{
		ModConfig.instance.pack(data);
	}

	@Override
	public void unpack(IByteIO data)
	{
		ModConfig.instance.unpack(data);
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		return ModConfig.instance.processAndReply(side, player);
	}

}
