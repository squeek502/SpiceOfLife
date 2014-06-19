package squeek.spiceoflife.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.FoodTracker;
import squeek.spiceoflife.foodtracker.FoodValues;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.helpers.ColorHelper;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.helpers.StringHelper;

public class TooltipHandler
{
	private static final DecimalFormat df = new DecimalFormat("##.##");

	@ForgeSubscribe
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (ModConfig.FOOD_MODIFIER_ENABLED && event.itemStack != null && FoodHelper.isFood(event.itemStack))
		{
			int totalFoodEaten = FoodHistory.get(event.entityPlayer).totalFoodsEatenAllTime;
			float foodModifier = 1f;
			List<String> toolTipStringsToAdd = new ArrayList<String>();
			FoodGroup foodGroup = ModConfig.USE_FOOD_GROUPS ? FoodGroupRegistry.getFoodGroupForFood(event.itemStack) : null;

			if (ModConfig.USE_FOOD_GROUPS && foodGroup != null)
			{
				toolTipStringsToAdd.add(StatCollector.translateToLocal("spiceoflife.tooltip.food.group") + EnumChatFormatting.ITALIC + foodGroup.getLocalizedName());
			}
			if (ModConfig.FOOD_EATEN_THRESHOLD > 0 && totalFoodEaten < ModConfig.FOOD_EATEN_THRESHOLD)
			{
				int timesUntilMeetsThreshold = ModConfig.FOOD_EATEN_THRESHOLD - totalFoodEaten;
				toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("spiceoflife.tooltip.food.until.enabled.1"));
				toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("spiceoflife.tooltip.food.until.enabled.2", timesUntilMeetsThreshold, timesUntilMeetsThreshold == 1 ? StatCollector.translateToLocal("spiceoflife.tooltip.times.singular") : StatCollector.translateToLocal("spiceoflife.tooltip.times.plural")));
			}
			else
			{
				int count = FoodTracker.getFoodHistoryCountOf(event.itemStack, event.entityPlayer);
				FoodValues defaultFoodValues = FoodValues.get(event.itemStack);
				foodModifier = FoodModifier.getFoodModifier(event.entityPlayer, event.itemStack, event.entityPlayer.getFoodStats(), defaultFoodValues.hunger, defaultFoodValues.saturationModifier);
				FoodValues foodValues = foodModifier != 1 ? defaultFoodValues.getModified(foodModifier) : defaultFoodValues;

				if (count > 0 || foodModifier != 1)
					toolTipStringsToAdd.add(0, EnumChatFormatting.GRAY + StatCollector.translateToLocal("spiceoflife.tooltip.nutritional.value") + ColorHelper.getRelativeColor(foodModifier, 0D, 1D) + df.format(foodModifier * 100f) + "%" + (foodValues.hunger == 0 && foodModifier != 0f ? EnumChatFormatting.DARK_RED + " (" + foodValues.hunger + " " + StatCollector.translateToLocal("spiceoflife.tooltip.hunger") + ")" : ""));

				if (count > 0)
					toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("spiceoflife.tooltip.eaten.recently" + (ModConfig.USE_HUNGER_QUEUE ? ".hunger" : ""), StringHelper.getQuantityDescriptor(count), ModConfig.USE_HUNGER_QUEUE ? df.format(ModConfig.FOOD_HISTORY_LENGTH/2f) : ModConfig.FOOD_HISTORY_LENGTH));
				else
					toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("spiceoflife.tooltip.not.eaten.recently"));
			}

			event.toolTip.addAll(toolTipStringsToAdd);
		}
	}

}
