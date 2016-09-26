package squeek.spiceoflife.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
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
		List<String> stringsToJoin = new ArrayList<String>(foodGroups.size());
		for (FoodGroup foodGroup : foodGroups)
		{
			stringsToJoin.add(foodGroup.formatString(TextFormatting.ITALIC.toString() + foodGroup) + resetFormatting);
		}
		return StringHelper.join(stringsToJoin, delimiter);
	}

	public String getNutritionalValueString(float foodModifier)
	{
		return ColorHelper.getRelativeColor(foodModifier, 0D, 1D) + df.format(foodModifier * 100f) + '%';
	}

	public String getEatenRecentlyTooltip(FoodHistory foodHistory, ItemStack itemStack, FoodGroup foodGroup, boolean shouldShowNutritionalValue)
	{
		int count = foodHistory.getFoodCountForFoodGroup(itemStack, foodGroup);
		String prefix = (foodGroup != null ? foodGroup.formatString(TextFormatting.ITALIC.toString() + foodGroup) + ' ' : "") + TextFormatting.RESET.toString() + TextFormatting.DARK_AQUA.toString() + TextFormatting.ITALIC;
		String eatenRecently;
		String nutritionalValue = shouldShowNutritionalValue ? TextFormatting.DARK_GRAY + " [" + getNutritionalValueString(FoodModifier.getFoodGroupModifier(foodHistory, itemStack, foodGroup)) + TextFormatting.DARK_GRAY + ']' : "";
		if (count > 0)
			eatenRecently = I18n.format("spiceoflife.tooltip.eaten.recently" + (ModConfig.USE_HUNGER_QUEUE ? ".hunger" : (ModConfig.USE_TIME_QUEUE ? ".time" : "")), StringHelper.getQuantityDescriptor(count), ModConfig.USE_HUNGER_QUEUE ? StringHelper.hungerHistoryLength(ModConfig.FOOD_HISTORY_LENGTH) : ModConfig.FOOD_HISTORY_LENGTH);
		else
			eatenRecently = I18n.format("spiceoflife.tooltip.not.eaten.recently");
		return prefix + (foodGroup != null ? StringHelper.decapitalize(eatenRecently, StringHelper.getMinecraftLocale()) : eatenRecently) + nutritionalValue;
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (ModConfig.FOOD_MODIFIER_ENABLED && event.getEntityPlayer() != null && event.getItemStack() != null && FoodHelper.isValidFood(event.getItemStack()))
		{
			int totalFoodEaten = FoodHistory.get(event.getEntityPlayer()).totalFoodsEatenAllTime;
			List<String> toolTipStringsToAdd = new ArrayList<String>();
			Set<FoodGroup> foodGroups = FoodGroupRegistry.getFoodGroupsForFood(event.getItemStack());
			Set<FoodGroup> visibleFoodGroups = getFoodGroupsForDisplay(foodGroups);
			boolean canDiminish = FoodHelper.canFoodDiminish(event.getItemStack());

			if (!visibleFoodGroups.isEmpty())
			{
				String foodGroupString = visibleFoodGroups.size() > 1 ? I18n.format("spiceoflife.tooltip.food.groups") : I18n.format("spiceoflife.tooltip.food.group");
				String joinedFoodGroups = joinFoodGroupsForDisplay(visibleFoodGroups, ", ", TextFormatting.GRAY.toString());
				toolTipStringsToAdd.add(TextFormatting.DARK_AQUA.toString() + TextFormatting.ITALIC + foodGroupString + TextFormatting.GRAY + TextFormatting.ITALIC + joinedFoodGroups);
			}
			if (ModConfig.FOOD_EATEN_THRESHOLD > 0 && totalFoodEaten < ModConfig.FOOD_EATEN_THRESHOLD)
			{
				int timesUntilMeetsThreshold = ModConfig.FOOD_EATEN_THRESHOLD - totalFoodEaten;
				toolTipStringsToAdd.add(TextFormatting.DARK_AQUA.toString() + TextFormatting.ITALIC + I18n.format("spiceoflife.tooltip.food.until.enabled.1"));
				toolTipStringsToAdd.add(TextFormatting.DARK_AQUA.toString() + TextFormatting.ITALIC + I18n.format("spiceoflife.tooltip.food.until.enabled.2", timesUntilMeetsThreshold, timesUntilMeetsThreshold == 1 ? I18n.format("spiceoflife.tooltip.times.singular") : I18n.format("spiceoflife.tooltip.times.plural")));
			}
			else
			{
				FoodHistory foodHistory = FoodHistory.get(event.getEntityPlayer());
				float foodModifier = FoodModifier.getFoodModifier(foodHistory, event.getItemStack());
				FoodValues foodValues = FoodValues.get(event.getItemStack(), event.getEntityPlayer());
				boolean foodOrItsFoodGroupsEatenRecently = foodHistory.containsFoodOrItsFoodGroups(event.getItemStack());

				if (canDiminish && (foodOrItsFoodGroupsEatenRecently || foodModifier != 1))
					toolTipStringsToAdd.add(0, TextFormatting.GRAY + I18n.format("spiceoflife.tooltip.nutritional.value") + getNutritionalValueString(foodModifier) + (foodValues.hunger == 0 && foodModifier != 0f ? TextFormatting.DARK_RED + " (" + foodValues.hunger + ' ' + I18n.format("spiceoflife.tooltip.hunger") + ')' : ""));

				boolean shouldShowPressShift = visibleFoodGroups.size() > 1 && !KeyHelper.isShiftKeyDown();
				boolean shouldShowFoodGroupDetails = visibleFoodGroups.size() <= 1 || KeyHelper.isShiftKeyDown();
				String bulletPoint = TextFormatting.DARK_GRAY + "- " + TextFormatting.GRAY;

				if (shouldShowPressShift)
					toolTipStringsToAdd.add(bulletPoint + TextFormatting.DARK_GRAY + I18n.format("spiceoflife.tooltip.hold.key.for.details", TextFormatting.YELLOW.toString() + TextFormatting.ITALIC + "Shift" + TextFormatting.RESET + TextFormatting.DARK_GRAY));

				if (shouldShowFoodGroupDetails)
				{
					int foodGroupsToShow = Math.max(1, visibleFoodGroups.size());
					FoodGroup[] visibleFoodGroupsArray = visibleFoodGroups.toArray(new FoodGroup[visibleFoodGroups.size()]);

					for (int i = 0; i < foodGroupsToShow; i++)
					{
						FoodGroup foodGroup = i < visibleFoodGroupsArray.length ? visibleFoodGroupsArray[i] : null;
						boolean shouldShowNutritionalValue = foodGroupsToShow > 1;
						String prefix = (foodGroupsToShow > 1 ? bulletPoint : "");
						toolTipStringsToAdd.add(prefix + getEatenRecentlyTooltip(foodHistory, event.getItemStack(), foodGroup, shouldShowNutritionalValue));
					}
				}
			}

			event.getToolTip().addAll(toolTipStringsToAdd);
		}
	}
}
