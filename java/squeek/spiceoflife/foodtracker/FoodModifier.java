package squeek.spiceoflife.foodtracker;

import java.math.BigDecimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.food.FoodEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.items.ItemFoodContainer;
import com.udojava.evalex.Expression;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class FoodModifier
{
	public static final FoodModifier GLOBAL = new FoodModifier();

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void getFoodValues(FoodEvent.GetPlayerFoodValues event)
	{
		if (ModConfig.FOOD_MODIFIER_ENABLED)
		{
			ItemStack actualFood = event.food;
			if (FoodHelper.isFoodContainer(event.food))
			{
				actualFood = ((ItemFoodContainer) event.food.getItem()).getBestFoodForPlayerToEat(event.food, event.player);
			}

			float modifier = FoodModifier.getFoodModifier(event.player, actualFood, event.foodValues);
			FoodValues modifiedFoodValues = FoodModifier.getModifiedFoodValues(event.foodValues, modifier);

			event.foodValues = modifiedFoodValues;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void getFoodEatingSpeed(PlayerUseItemEvent.Start event)
	{
		if (ModConfig.FOOD_EATING_SPEED_MODIFIER > 0 && AppleCoreAPI.accessor.isFood(event.item))
		{
			ItemStack actualFood = event.item;
			if (FoodHelper.isFoodContainer(event.item))
			{
				actualFood = ((ItemFoodContainer) event.item.getItem()).getBestFoodForPlayerToEat(event.item, event.entityPlayer);
			}

			float nutritionalValue = FoodModifier.getFoodModifier(event.entityPlayer, actualFood, AppleCoreAPI.accessor.getFoodValues(actualFood));
			float denominator = (float) Math.pow(nutritionalValue, ModConfig.FOOD_EATING_SPEED_MODIFIER);

			if (denominator > 0)
				event.duration = (int) (event.duration / denominator);
			else
				event.duration = Short.MAX_VALUE;

			if (ModConfig.FOOD_EATING_DURATION_MAX > 0)
				event.duration = Math.max(event.duration, ModConfig.FOOD_EATING_DURATION_MAX);
		}
	}

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

	public static float getFoodModifier(EntityPlayer player, ItemStack food, FoodValues foodValues)
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
		FoodValues totalFoodValues = foodHistory.getTotalFoodValues(food);

		BigDecimal result = effectiveFoodModifier.expression.with("count", new BigDecimal(count))
				.and("cur_history_length", new BigDecimal(historySize))
				.and("food_hunger_value", new BigDecimal(foodValues.hunger))
				.and("food_saturation_mod", new BigDecimal(foodValues.saturationModifier))
				.and("cur_hunger", new BigDecimal(player.getFoodStats().getFoodLevel()))
				.and("cur_saturation", new BigDecimal(player.getFoodStats().getSaturationLevel()))
				.and("total_food_eaten", new BigDecimal(totalFoodsEaten))
				.and("max_history_length", new BigDecimal(ModConfig.FOOD_HISTORY_LENGTH))
				.and("hunger_count", new BigDecimal(totalFoodValues.hunger))
				.and("saturation_count", new BigDecimal(totalFoodValues.saturationModifier))
				.eval();

		return result.floatValue();
	}

	public static FoodValues getModifiedFoodValues(FoodValues foodValues, float modifier)
	{
		int hunger = foodValues.hunger;
		float saturationModifier = foodValues.saturationModifier;

		if (ModConfig.AFFECT_FOOD_HUNGER_VALUES)
		{
			if (hunger < 0 && ModConfig.AFFECT_NEGATIVE_FOOD_HUNGER_VALUES)
				hunger = (int) ModConfig.FOOD_HUNGER_ROUNDING_MODE.round(hunger * (2 - modifier));
			else if (hunger > 0)
				hunger = (int) ModConfig.FOOD_HUNGER_ROUNDING_MODE.round(hunger * modifier);
		}

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
