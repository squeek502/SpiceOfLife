package squeek.spiceoflife.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.helpers.ColorHelper;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.helpers.KeyHelper;
import squeek.spiceoflife.helpers.StringHelper;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;

@SideOnly(Side.CLIENT)
public class TooltipHandler
{
	private static final DecimalFormat df = new DecimalFormat("##.##");
	private static final FoodGroupComparator foodGroupComparator = new FoodGroupComparator();

	static class FoodGroupComparator implements Comparator<FoodGroup>, Serializable
	{
		private static final long serialVersionUID = -4556648064321616158L;

		@Override
		public int compare(FoodGroup a, FoodGroup b)
		{
			return a.getLocalizedName().compareToIgnoreCase(b.getLocalizedName());
		}
	}

	public static Set<FoodGroup> getFoodGroupsForDisplay(Set<FoodGroup> foodGroups)
	{
		Set<FoodGroup> visibleFoodGroups = new TreeSet<FoodGroup>(foodGroupComparator);
		for (FoodGroup foodGroup : foodGroups)
		{
			if (!foodGroup.hidden())
				visibleFoodGroups.add(foodGroup);
		}
		return visibleFoodGroups;
	}

	public static String joinFoodGroupsForDisplay(Set<FoodGroup> foodGroups, String delimiter, String resetFormatting)
	{
		List<String> stringsToJoin = new ArrayList<String>();
		for (FoodGroup foodGroup : foodGroups)
		{
			stringsToJoin.add(foodGroup.formatString(EnumChatFormatting.ITALIC.toString() + foodGroup) + resetFormatting);
		}
		return StringHelper.join(stringsToJoin, delimiter);
	}

	public String getNutritionalValueString(float foodModifier)
	{
		return ColorHelper.getRelativeColor(foodModifier, 0D, 1D) + df.format(foodModifier * 100f) + "%";
	}

	public String getEatenRecentlyTooltip(FoodHistory foodHistory, ItemStack itemStack, FoodGroup foodGroup, boolean shouldShowNutritionalValue)
	{
		int count = foodHistory.getFoodCountForFoodGroup(itemStack, foodGroup);
		String prefix = (foodGroup != null ? foodGroup.formatString(EnumChatFormatting.ITALIC.toString() + foodGroup) + " " : "") + EnumChatFormatting.RESET.toString() + EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC;
		String eatenRecently;
		String nutritionalValue = shouldShowNutritionalValue ? EnumChatFormatting.DARK_GRAY + " [" + getNutritionalValueString(FoodModifier.getFoodGroupModifier(foodHistory, itemStack, foodGroup)) + EnumChatFormatting.DARK_GRAY + "]" : "";
		if (count > 0)
			eatenRecently = StatCollector.translateToLocalFormatted("spiceoflife.tooltip.eaten.recently" + (ModConfig.USE_HUNGER_QUEUE ? ".hunger" : (ModConfig.USE_TIME_QUEUE ? ".time" : "")), StringHelper.getQuantityDescriptor(count), ModConfig.USE_HUNGER_QUEUE ? StringHelper.hungerHistoryLength(ModConfig.FOOD_HISTORY_LENGTH) : ModConfig.FOOD_HISTORY_LENGTH);
		else
			eatenRecently = StatCollector.translateToLocal("spiceoflife.tooltip.not.eaten.recently");
		return prefix + (foodGroup != null ? StringHelper.decapitalize(eatenRecently, StringHelper.getMinecraftLocale()) : eatenRecently) + nutritionalValue;
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (ModConfig.FOOD_MODIFIER_ENABLED && event.itemStack != null && FoodHelper.isValidFood(event.itemStack))
		{
			int totalFoodEaten = FoodHistory.get(event.entityPlayer).totalFoodsEatenAllTime;
			float foodModifier = 1f;
			List<String> toolTipStringsToAdd = new ArrayList<String>();
			Set<FoodGroup> foodGroups = FoodGroupRegistry.getFoodGroupsForFood(event.itemStack);
			Set<FoodGroup> visibleFoodGroups = getFoodGroupsForDisplay(foodGroups);
			boolean canDiminish = FoodHelper.canFoodDiminish(event.itemStack);

			if (!visibleFoodGroups.isEmpty())
			{
				String foodGroupString = visibleFoodGroups.size() > 1 ? StatCollector.translateToLocal("spiceoflife.tooltip.food.groups") : StatCollector.translateToLocal("spiceoflife.tooltip.food.group");
				String joinedFoodGroups = joinFoodGroupsForDisplay(visibleFoodGroups, ", ", EnumChatFormatting.GRAY.toString());
				toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + foodGroupString + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + joinedFoodGroups);
			}
			if (ModConfig.FOOD_EATEN_THRESHOLD > 0 && totalFoodEaten < ModConfig.FOOD_EATEN_THRESHOLD)
			{
				int timesUntilMeetsThreshold = ModConfig.FOOD_EATEN_THRESHOLD - totalFoodEaten;
				toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("spiceoflife.tooltip.food.until.enabled.1"));
				toolTipStringsToAdd.add(EnumChatFormatting.DARK_AQUA.toString() + EnumChatFormatting.ITALIC + StatCollector.translateToLocalFormatted("spiceoflife.tooltip.food.until.enabled.2", timesUntilMeetsThreshold, timesUntilMeetsThreshold == 1 ? StatCollector.translateToLocal("spiceoflife.tooltip.times.singular") : StatCollector.translateToLocal("spiceoflife.tooltip.times.plural")));
			}
			else
			{
				FoodHistory foodHistory = FoodHistory.get(event.entityPlayer);
				foodModifier = FoodModifier.getFoodModifier(foodHistory, event.itemStack);
				FoodValues foodValues = FoodValues.get(event.itemStack, event.entityPlayer);
				boolean foodOrItsFoodGroupsEatenRecently = foodHistory.containsFoodOrItsFoodGroups(event.itemStack);

				if (canDiminish && (foodOrItsFoodGroupsEatenRecently || foodModifier != 1))
					toolTipStringsToAdd.add(0, EnumChatFormatting.GRAY + StatCollector.translateToLocal("spiceoflife.tooltip.nutritional.value") + getNutritionalValueString(foodModifier) + (foodValues.hunger == 0 && foodModifier != 0f ? EnumChatFormatting.DARK_RED + " (" + foodValues.hunger + " " + StatCollector.translateToLocal("spiceoflife.tooltip.hunger") + ")" : ""));

				boolean shouldShowPressShift = visibleFoodGroups.size() > 1 && !KeyHelper.isShiftKeyDown();
				boolean shouldShowFoodGroupDetails = visibleFoodGroups.size() <= 1 || KeyHelper.isShiftKeyDown();
				String bulletPoint = EnumChatFormatting.DARK_GRAY + "- " + EnumChatFormatting.GRAY;

				if (shouldShowPressShift)
					toolTipStringsToAdd.add(bulletPoint + EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocalFormatted("spiceoflife.tooltip.hold.key.for.details", EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.ITALIC + "Shift" + EnumChatFormatting.RESET + EnumChatFormatting.DARK_GRAY));

				if (shouldShowFoodGroupDetails)
				{
					int foodGroupsToShow = Math.max(1, visibleFoodGroups.size());
					FoodGroup[] visibleFoodGroupsArray = visibleFoodGroups.toArray(new FoodGroup[visibleFoodGroups.size()]);

					for (int i = 0; i < foodGroupsToShow; i++)
					{
						FoodGroup foodGroup = i < visibleFoodGroupsArray.length ? visibleFoodGroupsArray[i] : null;
						boolean shouldShowNutritionalValue = foodGroupsToShow > 1;
						String prefix = (foodGroupsToShow > 1 ? bulletPoint : "");
						toolTipStringsToAdd.add(prefix + getEatenRecentlyTooltip(foodHistory, event.itemStack, foodGroup, shouldShowNutritionalValue));
					}
				}
			}

			event.toolTip.addAll(toolTipStringsToAdd);
		}
	}
}
