package squeek.spiceoflife.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import squeek.spiceoflife.network.PacketBase;

public interface IPacketProcessor
{
	PacketBase processAndReply(Side side, EntityPlayer player);
}
