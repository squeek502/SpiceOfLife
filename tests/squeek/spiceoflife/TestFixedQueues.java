package squeek.spiceoflife;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.foodqueue.FixedHungerQueue;
import squeek.spiceoflife.foodtracker.foodqueue.FixedSizeQueue;

public class TestFixedQueues
{
	protected final FixedHungerQueue hungerQueue = new FixedHungerQueue(12);
	protected final FixedSizeQueue fixedQueue = new FixedSizeQueue(12);

	@Test
	public void testFixedHungerQueue()
	{
		for (int i=1; i<30; i++)
		{
			FoodEaten foodEaten = new FoodEaten();
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
			FoodEaten foodEaten = new FoodEaten();
			foodEaten.hungerRestored = i;
			fixedQueue.add(foodEaten);
			assertTrue(fixedQueue.size() <= 12);
		}
	}

}
