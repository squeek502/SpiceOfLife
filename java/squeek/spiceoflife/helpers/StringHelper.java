package squeek.spiceoflife.helpers;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Locale;

public class StringHelper
{
	public static String getQuantityDescriptor(int quantity)
	{
		return quantity == 1 ? I18n.format("spiceoflife.quantity.one.time")
			: (quantity == 2 ? I18n.format("spiceoflife.quantity.two.times", quantity)
			: I18n.format("spiceoflife.quantity.x.times", quantity));
	}

	public static String join(Collection<?> values, String delimiter)
	{
		if (values == null || values.isEmpty())
			return "";

		boolean first = true;
		StringBuilder strbuf = new StringBuilder();
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

	public static String decapitalize(String string, Locale locale)
	{
		if (string == null || string.isEmpty())
			return string;
		else
			return string.substring(0, 1).toLowerCase(locale) + string.substring(1);
	}

	public static Locale getMinecraftLocale()
	{
		String currentLanguage = FMLCommonHandler.instance().getCurrentLanguage();
		if(currentLanguage==null) return new Locale("en","us");
		String[] parts = currentLanguage.split("_");
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
