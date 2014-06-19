package squeek.spiceoflife;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.FoodHistory;

public class Command extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "tsol";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return null;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring)
	{
		EntityPlayer player = getCommandSenderAsPlayer(icommandsender);
		for (FoodEaten foodEaten : FoodHistory.get(player).getHistory())
		{
			String foodEatenString = foodEaten.itemStack.getDisplayName() + ": " + foodEaten.hungerRestored + " / " + (foodEaten.foodGroup == null ? "null" : foodEaten.foodGroup.identifier);
			icommandsender.addChatMessage(new ChatComponentText(foodEatenString));
		}
	}

	@Override
	public int compareTo(Object paramT)
	{
		return 0;
	}

}
