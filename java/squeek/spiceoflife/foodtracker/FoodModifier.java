package squeek.spiceoflife.foodtracker;

import java.math.BigDecimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import squeek.spiceoflife.ModConfig;
import com.udojava.evalex.Expression;

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
		if (!ModConfig.FOOD_MODIFIER_ENABLED)
			return 1f;
		
		/*
		// debug only
		int warningToRememberToRemoveThis;
		if (food.getItem() == Item.appleRed)
			return -1;
		*/
		
		FoodHistory foodHistory = FoodHistory.get(player);
		int count = foodHistory.getFoodCount(food);
		int historySize = foodHistory.getHistoryLengthInRelevantUnits();
		int totalFoodsEaten = foodHistory.totalFoodsEatenAllTime;

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
	
	public static FoodValues getModifiedFoodValues(FoodValues foodValues, float modifier)
	{
		return foodValues.getModified(modifier);
	}
	
	public static FoodValues modifyFoodValues(FoodValues foodValues, float modifier)
	{
		return foodValues.modify(modifier);
	}
}
