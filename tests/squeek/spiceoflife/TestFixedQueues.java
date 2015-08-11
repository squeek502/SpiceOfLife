package squeek.spiceoflife;

import static org.junit.Assert.*;
import org.junit.Test;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.foodqueue.FixedHungerQueue;
import squeek.spiceoflife.foodtracker.foodqueue.FixedSizeQueue;
import squeek.spiceoflife.foodtracker.foodqueue.FixedTimeQueue;
import squeek.spiceoflife.helpers.MiscHelper;

public class TestFixedQueues
{
	protected final FixedHungerQueue hungerQueue = new FixedHungerQueue(12);
	protected final FixedSizeQueue fixedQueue = new FixedSizeQueue(12);
	protected final FixedTimeQueue timeQueue = new FixedTimeQueue(12);

	@Test
	public void testFixedHungerQueue()
	{
		for (int i = 1; i < 30; i++)
		{
			FoodEaten foodEaten = new FoodEaten();
			foodEaten.foodValues = new FoodValues(i, 0f);
			hungerQueue.add(foodEaten);
			assertTrue(hungerQueue.hunger() <= 12);
		}
	}

	@Test
	public void testFixedSizeQueue()
	{
		for (int i = 1; i < 30; i++)
		{
			FoodEaten foodEaten = new FoodEaten();
			foodEaten.foodValues = new FoodValues(i, 0f);
			fixedQueue.add(foodEaten);
			assertTrue(fixedQueue.size() <= 12);
		}
	}

	@Test
	public void testFixedTimeQueue()
	{
		ModConfig.PROGRESS_TIME_WHILE_LOGGED_OFF = false;

		for (int i = 1; i < 30; i++)
		{
			FoodEaten foodEaten = new FoodEaten();
			foodEaten.foodValues = new FoodValues(i, 0f);
			timeQueue.add(foodEaten);
			assertTrue(timeQueue.size() == i);
		}

		timeQueue.prune(0, 0);
		assertEquals(29, timeQueue.size());
		timeQueue.prune(12 * MiscHelper.TICKS_PER_DAY, 0);
		assertEquals(29, timeQueue.size());
		timeQueue.prune(0, 12 * MiscHelper.TICKS_PER_DAY);
		assertEquals(0, timeQueue.size());

		ModConfig.PROGRESS_TIME_WHILE_LOGGED_OFF = true;

		for (int i = 1; i < 30; i++)
		{
			FoodEaten foodEaten = new FoodEaten();
			foodEaten.foodValues = new FoodValues(i, 0f);
			timeQueue.add(foodEaten);
			assertTrue(timeQueue.size() == i);
		}

		timeQueue.prune(0, 0);
		assertEquals(29, timeQueue.size());
		timeQueue.prune(0, 12 * MiscHelper.TICKS_PER_DAY);
		assertEquals(29, timeQueue.size());
		timeQueue.prune(12 * MiscHelper.TICKS_PER_DAY, 0);
		assertEquals(0, timeQueue.size());
	}

}
