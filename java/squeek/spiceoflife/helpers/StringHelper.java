package squeek.spiceoflife.helpers;

import net.minecraft.util.StatCollector;

public class StringHelper
{
	public static String getQuantityDescriptor(int quantity)
	{
		return quantity == 1 ? StatCollector.translateToLocal("spiceoflife.quantity.one.time")
				: (quantity == 2 ? StatCollector.translateToLocalFormatted("spiceoflife.quantity.two.times", quantity)
						: StatCollector.translateToLocalFormatted("spiceoflife.quantity.x.times", quantity));
	}
}
