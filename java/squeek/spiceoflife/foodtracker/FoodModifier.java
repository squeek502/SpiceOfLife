package squeek.spiceoflife.foodtracker;

import java.math.BigDecimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.ModConfig;
import com.udojava.evalex.Expression;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FoodModifier
{
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void getFoodValues(FoodEvent.GetPlayerFoodValues event)
	{
		if (ModConfig.FOOD_MODIFIER_ENABLED)
		{
			float modifier = FoodModifier.getFoodModifier(event.player, event.food, event.foodValues);
			FoodValues modifiedFoodValues = FoodModifier.getModifiedFoodValues(event.foodValues, modifier);

			event.foodValues = modifiedFoodValues;
		}
	}

	public static Expression expression = getNewExpression();

	private static Expression getNewExpression()
	{
		return new Expression(ModConfig.FOOD_MODIFIER_FORMULA).with("max_history_length", new BigDecimal(ModConfig.FOOD_HISTORY_LENGTH));
	}

	public static void onFormulaChanged()
	{
		expression = getNewExpression();
	}

	public static float getFoodModifier(EntityPlayer player, ItemStack food, FoodValues foodValues)
	{
		if (!ModConfig.FOOD_MODIFIER_ENABLED)
			return 1f;

		FoodHistory foodHistory = FoodHistory.get(player);
		int count = foodHistory.getFoodCount(food);
		int historySize = foodHistory.getHistoryLengthInRelevantUnits();
		int totalFoodsEaten = foodHistory.totalFoodsEatenAllTime;

		if (ModConfig.FOOD_EATEN_THRESHOLD > 0 && totalFoodsEaten < ModConfig.FOOD_EATEN_THRESHOLD)
			return 1f;

		BigDecimal result = expression.with("count", new BigDecimal(count))
				.and("cur_history_length", new BigDecimal(historySize))
				.and("food_hunger_value", new BigDecimal(foodValues.hunger))
				.and("food_saturation_mod", new BigDecimal(foodValues.saturationModifier))
				.and("cur_hunger", new BigDecimal(player.getFoodStats().getFoodLevel()))
				.and("cur_saturation", new BigDecimal(player.getFoodStats().getSaturationLevel()))
				.and("total_food_eaten", new BigDecimal(totalFoodsEaten))
				.eval();

		return result.floatValue();
	}

	public static FoodValues getModifiedFoodValues(FoodValues foodValues, float modifier)
	{
		int hunger = (int) ModConfig.FOOD_HUNGER_ROUNDING_MODE.round(foodValues.hunger * modifier);
		float saturationModifier = foodValues.saturationModifier;

		if (ModConfig.AFFECT_FOOD_SATURATION_MODIFIERS)
		{
			if (saturationModifier < 0 && ModConfig.AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS)
				saturationModifier *= 2 - modifier;
			else if (saturationModifier > 0)
				saturationModifier *= modifier;
		}
		return new FoodValues(hunger, saturationModifier);
	}
}
