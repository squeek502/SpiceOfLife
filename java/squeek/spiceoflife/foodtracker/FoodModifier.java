package squeek.spiceoflife.foodtracker;

import java.math.BigDecimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import squeek.spiceoflife.ModConfig;
import com.udojava.evalex.Expression;
import cpw.mods.fml.common.FMLCommonHandler;

public class FoodModifier
{
	public static Expression expression = getNewExpression();

	private static Expression getNewExpression()
	{
		return new Expression(ModConfig.FOOD_MODIFIER_FORMULA).with("max_history_length", new BigDecimal(ModConfig.FOOD_HISTORY_LENGTH));
	}

	public static void onFormulaChanged()
	{
		expression = getNewExpression();
	}

	public static float getFoodModifier(EntityPlayer player, FoodStats foodStats, int hunger, float saturationModifier)
	{
		return getFoodModifier(player, FoodTracker.getFoodLastEatenBy(player), foodStats, hunger, saturationModifier);
	}

	public static float getFoodModifier(EntityPlayer player, ItemStack food, FoodStats foodStats, int hunger, float saturationModifier)
	{
		FoodHistory foodHistory = FoodHistory.get(player);
		int count = foodHistory.getFoodCount(food);
		int historySize = foodHistory.getHistorySize();
		int totalFoodsEaten = foodHistory.totalFoodsEatenAllTime;

		// server adds it to the food tracker immediately, so get the count before the last food was eaten
		if (FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			count -= 1;
			historySize -= 1;
			totalFoodsEaten -= 1;
		}

		if (ModConfig.FOOD_EATEN_THRESHOLD > 0 && totalFoodsEaten < ModConfig.FOOD_EATEN_THRESHOLD)
			return 1f;

		BigDecimal result = expression.with("count", new BigDecimal(count))
				.and("cur_history_length", new BigDecimal(historySize))
				.and("food_hunger_value", new BigDecimal(hunger))
				.and("food_saturation_mod", new BigDecimal(saturationModifier))
				.and("cur_hunger", new BigDecimal(foodStats.getFoodLevel()))
				.and("cur_saturation", new BigDecimal(foodStats.getSaturationLevel()))
				.and("total_food_eaten", new BigDecimal(totalFoodsEaten))
				.eval();

		return result.floatValue();
	}
}
