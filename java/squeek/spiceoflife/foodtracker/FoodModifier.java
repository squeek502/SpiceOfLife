package squeek.spiceoflife.foodtracker;

import java.math.BigDecimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import com.udojava.evalex.Expression;

public class FoodModifier
{
	public static final FoodModifier GLOBAL = new FoodModifier();

	public Expression expression;

	public FoodModifier()
	{
		this(ModConfig.FOOD_MODIFIER_FORMULA);
	}

	public FoodModifier(String formula)
	{
		setFormula(formula);
	}

	public void setFormula(String formula)
	{
		expression = new Expression(formula);
	}

	public static void onGlobalFormulaChanged()
	{
		FoodModifier.GLOBAL.setFormula(ModConfig.FOOD_MODIFIER_FORMULA);
	}

	public static float getFoodModifier(EntityPlayer player, FoodStats foodStats, int hunger, float saturationModifier)
	{
		return getFoodModifier(player, FoodTracker.getFoodLastEatenBy(player), foodStats, hunger, saturationModifier);
	}

	public static float getFoodModifier(EntityPlayer player, ItemStack food, FoodStats foodStats, int hunger, float saturationModifier)
	{
		if (!ModConfig.FOOD_MODIFIER_ENABLED)
			return 1f;

		FoodHistory foodHistory = FoodHistory.get(player);
		int totalFoodsEaten = foodHistory.totalFoodsEatenAllTime;

		if (ModConfig.FOOD_EATEN_THRESHOLD > 0 && totalFoodsEaten < ModConfig.FOOD_EATEN_THRESHOLD)
			return 1f;

		int count = foodHistory.getFoodCount(food);
		int historySize = foodHistory.getHistoryLengthInRelevantUnits();
		FoodGroup foodGroup = FoodGroupRegistry.getFoodGroupForFood(food);
		FoodModifier effectiveFoodModifier = foodGroup != null ? foodGroup.getFoodModifier() : FoodModifier.GLOBAL;

		BigDecimal result = effectiveFoodModifier.expression.with("count", new BigDecimal(count))
				.and("cur_history_length", new BigDecimal(historySize))
				.and("food_hunger_value", new BigDecimal(hunger))
				.and("food_saturation_mod", new BigDecimal(saturationModifier))
				.and("cur_hunger", new BigDecimal(foodStats.getFoodLevel()))
				.and("cur_saturation", new BigDecimal(foodStats.getSaturationLevel()))
				.and("total_food_eaten", new BigDecimal(totalFoodsEaten))
				.and("max_history_length", new BigDecimal(ModConfig.FOOD_HISTORY_LENGTH))
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
