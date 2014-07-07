package squeek.spiceoflife.network;

import net.minecraft.entity.player.EntityPlayer;
import squeek.spiceoflife.compat.IByteIO;
import cpw.mods.fml.relauncher.Side;

public class PacketDifficultySetting extends PacketBase
{
	int difficultySetting;

	public PacketDifficultySetting()
	{
	}

	public PacketDifficultySetting(int difficultySetting)
	{
		this.difficultySetting = difficultySetting;
	}

	@Override
	public void pack(IByteIO data)
	{
		data.writeByte(difficultySetting);
	}

	@Override
	public void unpack(IByteIO data)
	{
		difficultySetting = data.readByte();
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		player.worldObj.difficultySetting = difficultySetting;
		return null;
	}
}
