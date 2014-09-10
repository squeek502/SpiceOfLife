package squeek.spiceoflife.foodtracker.commands;

import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.FoodTracker;

public class CommandResetHistory extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "spiceoflife";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender)
	{
		return "/spiceoflife reset [player]";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] args)
	{
		if (args.length > 0)
		{
			if (args[0].equals("reset"))
			{
				EntityPlayerMP playerToReset = args.length > 1 ? getPlayer(commandSender, args[1])
						: getCommandSenderAsPlayer(commandSender);
				FoodHistory foodHistoryToReset = FoodHistory.get(playerToReset);
				foodHistoryToReset.reset();
				FoodTracker.syncFoodHistory(foodHistoryToReset);
				notifyAdmins(commandSender, 0,
						"Reset all 'The Spice of Life' mod data for " + playerToReset.getDisplayName());
				return;
			}
		}
		throw new WrongUsageException(getCommandName() + " reset [player]");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender commandSender, String[] curArgs)
	{
		if (curArgs.length == 1)
			return Arrays.asList(new String[] {"reset"});
		else if (curArgs.length == 2)
			return getListOfStringsMatchingLastWord(curArgs, MinecraftServer.getServer().getAllUsernames());
		else
			return null;
	}

	@Override
	public int compareTo(Object obj)
	{
		if (obj instanceof ICommand)
			return super.compareTo((ICommand) obj);
		else
			return 0;
	}
}
