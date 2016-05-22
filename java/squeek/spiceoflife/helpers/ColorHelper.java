package squeek.spiceoflife.helpers;

import net.minecraft.util.text.TextFormatting;

public class ColorHelper
{
	private static final TextFormatting colorRange[] = {
		TextFormatting.DARK_RED,
		TextFormatting.RED,
		TextFormatting.GOLD,
		TextFormatting.YELLOW,
		TextFormatting.DARK_GREEN,
		TextFormatting.GREEN,
		TextFormatting.AQUA
	};

	private static final TextFormatting booleanColorRange[] = {
		TextFormatting.DARK_RED,
		TextFormatting.RED,
		TextFormatting.DARK_GREEN,
		TextFormatting.GREEN
	};

	public static String getRelativeColor(double val, double min, double max)
	{
		if (min == max)
			return TextFormatting.RESET.toString();
		else if ((max > min && val > max) || (min > max && val < max))
			return TextFormatting.WHITE.toString() + TextFormatting.BOLD;
		else if ((max > min && val < min) || (min > max && val > min))
			return colorRange[0].toString() + TextFormatting.BOLD;

		int index = (int) (((val - min) / (max - min)) * (colorRange.length - 1));
		return colorRange[Math.max(0, Math.min(colorRange.length - 1, index))].toString();
	}

	public static int fromRGBA(int r, int g, int b, int a)
	{
		return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | b & 255;
	}

	public static int getRelativeColorInt(double val, double min, double max)
	{
		if (min == max)
			return 0x000000;

		if (val > max)
			return 0x1ED6B1;

		double full, f1, f2;
		full = max - min;
		f1 = (max - val) / full;
		f2 = (val - min) / full;
		int[] minColor = new int[]{150, 0, 0};
		int[] maxColor = new int[]{0, 100, 0};
		int[] color = new int[]{
			(int) (maxColor[0] * f2 + minColor[0] * f1),
			(int) (maxColor[1] * f2 + minColor[1] * f1),
			(int) (maxColor[2] * f2 + minColor[2] * f1)
		};
		return fromRGBA(color[0], color[1], color[2], 255);
	}

	public static String getBooleanColor(boolean val)
	{
		return getBooleanColor(val, false);
	}

	public static String getBooleanColor(boolean val, boolean modified)
	{
		return booleanColorRange[(val ? 2 : 0) + (modified ? 1 : 0)].toString();
	}
}