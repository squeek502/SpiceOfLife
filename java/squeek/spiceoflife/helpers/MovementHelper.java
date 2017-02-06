package squeek.spiceoflife.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class MovementHelper
{
	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(new MovementHelper());
	}

	public static class MovementInfo
	{
		public long lastJump;
	}

	public static HashMap<EntityPlayer, MovementInfo> movementInfoByPlayer = new HashMap<EntityPlayer, MovementInfo>();

	public static boolean getDidJumpLastTick(EntityPlayer player)
	{
		MovementInfo movementInfo = movementInfoByPlayer.get(player);
		if (movementInfo != null)
		{
			return player.world.getWorldTime() - movementInfo.lastJump <= 2;
		}
		else
			return false;
	}

	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event)
	{
		if (event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			MovementInfo movementInfo = movementInfoByPlayer.get(player);

			if (movementInfo == null)
				movementInfo = new MovementInfo();

			movementInfo.lastJump = event.getEntityLiving().world.getWorldTime();
			movementInfoByPlayer.put(player, movementInfo);
		}
	}
}
