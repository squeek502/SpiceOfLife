package squeek.spiceoflife;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.ModConfig.RoundingMode;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.helpers.MealPrioritizationHelper.FoodInfoComparator;
import squeek.spiceoflife.helpers.MealPrioritizationHelper.InventoryFoodInfo;

public class TestMealPrioritization
{
	public static class TestInventoryFoodInfo extends InventoryFoodInfo
	{
		public TestInventoryFoodInfo(FoodValues defaultFoodValues, float modifier)
		{
			this.defaultFoodValues = defaultFoodValues;
			this.diminishingReturnsModifier = modifier;
			this.modifiedFoodValues = modifier != Float.NaN ? FoodModifier.getModifiedFoodValues(defaultFoodValues, modifier) : defaultFoodValues;
		}

		@Override
		public String toString()
		{
			return "(" + defaultFoodValues.hunger + ":" + defaultFoodValues.saturationModifier + ")" +
					"*" + diminishingReturnsModifier + "=" +
					"(" + modifiedFoodValues.hunger + ":" + modifiedFoodValues.saturationModifier + ")";
		}
	}

	@Before
	public void setUp() throws Exception
	{
		ModConfig.FOOD_HUNGER_ROUNDING_MODE = RoundingMode.ROUND;
	}

	@After
	public void tearDown() throws Exception
	{
		ModConfig.FOOD_HUNGER_ROUNDING_MODE = null;
	}

	@Test
	public void testBestFitDiminished()
	{
		InventoryFoodInfo nonDiminishedFood = new TestInventoryFoodInfo(new FoodValues(3, 0.5f), 1f);
		InventoryFoodInfo highHungerDiminishedFood = new TestInventoryFoodInfo(new FoodValues(12, 0.5f), 0.5f);
		InventoryFoodInfo diminishedFood = new TestInventoryFoodInfo(new FoodValues(3, 0.5f), 0.75f);
		InventoryFoodInfo undiminishableFood = new TestInventoryFoodInfo(new FoodValues(1, 0.5f), Float.NaN);

		List<InventoryFoodInfo> testDiminished = Arrays.asList(new InventoryFoodInfo[]{
		nonDiminishedFood, highHungerDiminishedFood, diminishedFood, undiminishableFood
		});
		Collections.sort(testDiminished, new FoodInfoComparator(0));

		assertEquals(undiminishableFood, testDiminished.get(0));
		assertEquals(nonDiminishedFood, testDiminished.get(1));
		assertEquals(diminishedFood, testDiminished.get(2));
		assertEquals(highHungerDiminishedFood, testDiminished.get(3));
	}

	@Test
	public void testBestFitHunger()
	{
		InventoryFoodInfo overkill = new TestInventoryFoodInfo(new FoodValues(12, 0.5f), 1f);
		InventoryFoodInfo tooHighBy1 = new TestInventoryFoodInfo(new FoodValues(6, 0.5f), 1f);
		InventoryFoodInfo justRight = new TestInventoryFoodInfo(new FoodValues(5, 0.5f), 1f);
		InventoryFoodInfo tooLowBy1 = new TestInventoryFoodInfo(new FoodValues(4, 0.5f), 1f);
		InventoryFoodInfo underkill = new TestInventoryFoodInfo(new FoodValues(3, 0.5f), 1f);

		List<InventoryFoodInfo> testHunger = Arrays.asList(new InventoryFoodInfo[]{
		overkill, justRight, underkill, tooHighBy1, tooLowBy1
		});
		Collections.sort(testHunger, new FoodInfoComparator(5));

		assertEquals(justRight, testHunger.get(0));
		assertEquals(tooLowBy1, testHunger.get(1));
		assertEquals(tooHighBy1, testHunger.get(2));
		assertEquals(underkill, testHunger.get(3));
		assertEquals(overkill, testHunger.get(4));
	}

	@Test
	public void testBestFitSaturation()
	{
		InventoryFoodInfo best = new TestInventoryFoodInfo(new FoodValues(5, 1.5f), 1f);
		InventoryFoodInfo good = new TestInventoryFoodInfo(new FoodValues(5, 1.0f), 1f);
		InventoryFoodInfo bad = new TestInventoryFoodInfo(new FoodValues(5, 0.5f), 1f);
		InventoryFoodInfo wayTooGood = new TestInventoryFoodInfo(new FoodValues(12, 1f), 1f);
		InventoryFoodInfo wayTooGoodButWorse = new TestInventoryFoodInfo(new FoodValues(12, 0.5f), 1f);

		List<InventoryFoodInfo> testSaturation = Arrays.asList(new InventoryFoodInfo[]{
		good, best, bad, wayTooGood, wayTooGoodButWorse
		});
		Collections.sort(testSaturation, new FoodInfoComparator(5));

		assertEquals(best, testSaturation.get(0));
		assertEquals(good, testSaturation.get(1));
		assertEquals(bad, testSaturation.get(2));
		assertEquals(wayTooGood, testSaturation.get(3));
		assertEquals(wayTooGoodButWorse, testSaturation.get(4));
	}

}
