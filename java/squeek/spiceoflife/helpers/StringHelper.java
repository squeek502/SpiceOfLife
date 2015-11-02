package squeek.spiceoflife.helpers;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.common.FMLCommonHandler;

public class StringHelper
{
	public static String getQuantityDescriptor(int quantity)
	{
		return quantity == 1 ? StatCollector.translateToLocal("spiceoflife.quantity.one.time")
				: (quantity == 2 ? StatCollector.translateToLocalFormatted("spiceoflife.quantity.two.times", quantity)
						: StatCollector.translateToLocalFormatted("spiceoflife.quantity.x.times", quantity));
	}

	public static String join(Collection<?> values, String delimiter)
	{
		if (values == null || values.size() == 0)
			return "";

		boolean first = true;
		StringBuffer strbuf = new StringBuffer();
		for (Object value : values)
		{
			if (value == null)
				continue;
			if (!first)
				strbuf.append(delimiter);
			strbuf.append(value.toString());
			first = false;
		}
		return strbuf.toString();
	}

	public static String decapitalize(final String string, final Locale locale)
	{
		if (string == null || string.isEmpty())
			return string;
		else
			return string.substring(0, 1).toLowerCase(locale) + string.substring(1);
	}

	public static Locale getMinecraftLocale()
	{
		String[] parts = FMLCommonHandler.instance().getCurrentLanguage().split("_");
		String langCode = parts[0];
		String regionCode = parts.length > 1 ? parts[1] : null;
		return regionCode != null ? new Locale(langCode, regionCode) : new Locale(langCode);
	}

	public static DecimalFormat df = new DecimalFormat("#.#");

	public static String hungerHistoryLength(int length)
	{
		return df.format(length / 2f);
	}
}
