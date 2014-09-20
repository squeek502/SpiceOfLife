package squeek.spiceoflife;

import static org.junit.Assert.*;
import org.junit.Test;
import squeek.spiceoflife.foodtracker.FixedFoodQueue;
import squeek.spiceoflife.foodtracker.FixedHungerQueue;
import squeek.spiceoflife.foodtracker.FoodEaten;

public class TestFixedQueues
{
	protected final FixedHungerQueue hungerQueue = new FixedHungerQueue(12);
	protected final FixedFoodQueue fixedQueue = new FixedFoodQueue(12);

	@Test
	public void testFixedHungerQueue()
	{
		for (int i=1; i<30; i++)
		{
			FoodEaten foodEaten = new FoodEaten(null);
			foodEaten.hungerRestored = i;
			hungerQueue.add(foodEaten);
			assertTrue(hungerQueue.hunger() <= 12);
		}
	}

	@Test
	public void testFixedSizeQueue()
	{
		for (int i=1; i<30; i++)
		{
			FoodEaten foodEaten = new FoodEaten(null);
			foodEaten.hungerRestored = i;
			fixedQueue.add(foodEaten);
			assertTrue(fixedQueue.size() <= 12);
		}
	}

}
