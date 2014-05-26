package squeek.spiceoflife.foodtracker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemFood;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.helpers.ColorHelper;
import squeek.spiceoflife.helpers.StringHelper;

public class TooltipHandler
{
	private static final DecimalFormat df = new DecimalFormat("##.##");

	@ForgeSubscribe
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (event.itemStack != null && event.itemStack.getItem() instanceof ItemFood)
		{
			ItemFood itemFood = (ItemFood) event.itemStack.getItem();
			int totalFoodEaten = FoodHistory.get(event.entityPlayer).totalFoodsEatenAllTime;
			float foodModifier = 1f;
			List<String> toolTipStringsToAdd = new ArrayList<String>();

			if (ModConfig.FOOD_EATEN_THRESHOLD > 0 && totalFoodEaten < ModConfig.FOOD_EATEN_THRESHOLD)
			{
				int timesUntilMeetsThreshold = ModConfig.FOOD_EATEN_THRESHOLD - totalFoodEaten;
				toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("spiceoflife.tooltip.food.until.enabled.1"));
				toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("spiceoflife.tooltip.food.until.enabled.2", timesUntilMeetsThreshold, timesUntilMeetsThreshold == 1 ? StatCollector.translateToLocal("spiceoflife.tooltip.times.singular") : StatCollector.translateToLocal("spiceoflife.tooltip.times.plural")));
			}
			else
			{
				int count = FoodTracker.getFoodHistoryCountOf(event.itemStack, event.entityPlayer);
				foodModifier = FoodModifier.getFoodModifier(event.entityPlayer, event.itemStack, event.entityPlayer.getFoodStats(), itemFood.getHealAmount(), itemFood.getSaturationModifier());
				int actualHunger = (int) (itemFood.getHealAmount() * foodModifier);
				//float effectiveModifier = actualHunger / (float) itemFood.getHealAmount();

				if (count > 0 || foodModifier != 1)
					toolTipStringsToAdd.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("spiceoflife.tooltip.nutritional.value") + ColorHelper.getRelativeColor(foodModifier, 0D, 1D) + df.format(foodModifier * 100f) + "%" + (actualHunger == 0 && foodModifier != 0f ? EnumChatFormatting.DARK_RED + " (" + actualHunger + " " + StatCollector.translateToLocal("spiceoflife.tooltip.hunger") + ")" : ""));

				if (count > 0)
					toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("spiceoflife.tooltip.eaten.recently", StringHelper.getQuantityDescriptor(count), ModConfig.FOOD_HISTORY_LENGTH));
				else
					toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("spiceoflife.tooltip.not.eaten.recently"));
			}

			if (event.showAdvancedItemTooltips)
			{
				float hungerRestored = (int) (foodModifier * itemFood.getHealAmount()) / 2f;
				float saturationModifier = itemFood.getSaturationModifier();
				saturationModifier = saturationModifier * (saturationModifier > 0 ? foodModifier : 1);
				toolTipStringsToAdd.add(StatCollector.translateToLocalFormatted("spiceoflife.tooltip.advanced.hunger.restored", df.format(hungerRestored), df.format(itemFood.getHealAmount() / 2f)));
				toolTipStringsToAdd.add(StatCollector.translateToLocalFormatted("spiceoflife.tooltip.advanced.saturation.modifier", df.format(saturationModifier), df.format(itemFood.getSaturationModifier())));
			}

			event.toolTip.addAll(toolTipStringsToAdd);
		}
	}

}
