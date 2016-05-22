package squeek.spiceoflife.foodtracker.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.FoodTracker;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

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
	public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
	{
		if (args.length > 0)
		{
			if (args[0].equals("reset"))
			{
				EntityPlayerMP playerToReset;
				playerToReset = args.length > 1 ? getPlayer(server, commandSender, args[1]) : getCommandSenderAsPlayer(commandSender);
				FoodHistory foodHistoryToReset = FoodHistory.get(playerToReset);
				foodHistoryToReset.reset();
				FoodTracker.syncFoodHistory(foodHistoryToReset);
				notifyCommandListener(commandSender, this, 0, "Reset all 'The Spice of Life' mod data for " + playerToReset.getDisplayName());
				return;
			}
		}
		throw new WrongUsageException(getCommandName() + " reset [player]");
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] curArgs, @Nullable BlockPos pos)
	{
		if (curArgs.length == 1)
			return Collections.singletonList("reset");
		else if (curArgs.length == 2)
			return getListOfStringsMatchingLastWord(curArgs, server.getAllUsernames());
		else
			return null;
	}
}
