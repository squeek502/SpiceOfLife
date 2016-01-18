package squeek.spiceoflife.helpers;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class MiscHelper
{
	public static final int TICKS_PER_SEC = 20;
	public static final int TICKS_PER_DAY = 24000; // 20 minutes realtime

	@SideOnly(Side.CLIENT)
	public static boolean isMouseOverNothing()
	{
		Minecraft mc = Minecraft.getMinecraft();
		MovingObjectPosition mouseOver = mc.objectMouseOver;

		if (mouseOver == null || mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS)
			return true;
		else if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
		{
			BlockPos pos = mc.objectMouseOver.getBlockPos();
			return mc.theWorld.getBlockState(pos).getBlock().getMaterial() == Material.air;
		}
		return false;
	}

	public static void tryCloseStream(Closeable stream)
	{
		try
		{
			if (stream != null)
				stream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static boolean collectionsOverlap(Collection<?> a, Collection<?> b)
	{
		return !Collections.disjoint(a, b);
	}
}
