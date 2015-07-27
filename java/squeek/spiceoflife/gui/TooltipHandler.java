package squeek.spiceoflife.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.FoodTracker;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.helpers.ColorHelper;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.helpers.StringHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TooltipHandler
{
	private static final DecimalFormat df = new DecimalFormat("##.##");

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (ModConfig.FOOD_MODIFIER_ENABLED && event.itemStack != null && FoodHelper.isValidFood(event.itemStack))
		{
			int totalFoodEaten = FoodHistory.get(event.entityPlayer).totalFoodsEatenAllTime;
			float foodModifier = 1f;
			List<String> toolTipStringsToAdd = new ArrayList<String>();
			FoodGroup foodGroup = FoodGroupRegistry.getFoodGroupForFood(event.itemStack);

			if (foodGroup != null && !foodGroup.hidden)
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
				foodModifier = FoodModifier.getFoodModifier(event.entityPlayer, event.itemStack, FoodValues.get(event.itemStack));
				FoodValues foodValues = FoodValues.get(event.itemStack, event.entityPlayer);

				if (count > 0 || foodModifier != 1)
					toolTipStringsToAdd.add(0, EnumChatFormatting.GRAY + StatCollector.translateToLocal("spiceoflife.tooltip.nutritional.value") + ColorHelper.getRelativeColor(foodModifier, 0D, 1D) + df.format(foodModifier * 100f) + "%" + (foodValues.hunger == 0 && foodModifier != 0f ? EnumChatFormatting.DARK_RED + " (" + foodValues.hunger + " " + StatCollector.translateToLocal("spiceoflife.tooltip.hunger") + ")" : ""));

				if (count > 0)
					toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("spiceoflife.tooltip.eaten.recently" + (ModConfig.USE_TIME_QUEUE ? ".time" : (ModConfig.USE_HUNGER_QUEUE ? ".hunger" : "")), StringHelper.getQuantityDescriptor(count), ModConfig.USE_HUNGER_QUEUE ? df.format(ModConfig.FOOD_HISTORY_LENGTH / 2f) : ModConfig.FOOD_HISTORY_LENGTH));
				else
					toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("spiceoflife.tooltip.not.eaten.recently"));
			}

			event.toolTip.addAll(toolTipStringsToAdd);
		}
	}

}
