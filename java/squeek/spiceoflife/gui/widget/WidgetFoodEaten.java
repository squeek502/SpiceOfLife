package squeek.spiceoflife.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import squeek.applecore.api.food.FoodValues;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.helpers.ColorHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WidgetFoodEaten extends Gui
{
	public final FoodEaten foodEaten;
	protected Minecraft mc = Minecraft.getMinecraft();
	public static final int PADDING_LEFT = 18;
	public static final int HUNGER_BAR_WIDTH = 9;

	public WidgetFoodEaten(FoodEaten foodEaten)
	{
		this.foodEaten = foodEaten;
	}

	public String getDisplayName()
	{
		// truncate name if necessary
		String displayName = foodEaten.itemStack.getDisplayName();
		boolean truncated = false;
		while (mc.fontRenderer.getStringWidth(displayName) > 93)
		{
			displayName = displayName.substring(0, displayName.length() - 1);
			truncated = true;
		}
		if (truncated)
			displayName += "...";

		return displayName;
	}

	public int textWidth()
	{
		return mc.fontRenderer.getStringWidth(getDisplayName());
	}

	public int hungerBarsWidth()
	{
		return hungerBarsNeeded() * HUNGER_BAR_WIDTH;
	}

	public int hungerBarsNeeded()
	{
		FoodValues defaultFoodValues = FoodValues.get(foodEaten.itemStack);

		if (defaultFoodValues == null)
			return 0;

		return (int) Math.max(1, Math.ceil(Math.abs(Math.max(foodEaten.foodValues.hunger, defaultFoodValues.hunger)) / 2f));
	}

	public int width()
	{
		return Math.max(textWidth(), hungerBarsWidth());
	}

	public void draw(int x, int y)
	{
		x += PADDING_LEFT;

		if (foodEaten.itemStack == null)
			return;

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_LIGHTING);

		FoodValues defaultFoodValues = FoodValues.get(foodEaten.itemStack);

		if (defaultFoodValues == null)
			return;

		mc.fontRenderer.drawString(getDisplayName(), x, y, ColorHelper.getRelativeColorInt(foodEaten.foodValues.hunger, 0, defaultFoodValues.hunger));

		int barsNeeded = hungerBarsNeeded();

		GL11.glColor4f(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(Gui.icons);
		y += mc.fontRenderer.FONT_HEIGHT;
		for (int i = 0; i < barsNeeded * 2; i += 2)
		{
			this.drawTexturedModalRect(x, y, 16, 27, 9, 9);

			if (foodEaten.foodValues.hunger < 0)
				drawTexturedModalRect(x, y, 34, 27, 9, 9);
			else if (foodEaten.foodValues.hunger > i + 1 || defaultFoodValues.hunger == foodEaten.foodValues.hunger)
				drawTexturedModalRect(x, y, 16, 27, 9, 9);
			else if (foodEaten.foodValues.hunger == i + 1)
				drawTexturedModalRect(x, y, 124, 27, 9, 9);
			else
				drawTexturedModalRect(x, y, 34, 27, 9, 9);

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_DST_COLOR);
			drawTexturedModalRect(x, y, defaultFoodValues.hunger - 1 == i ? 115 : 106, 27, 9, 9);
			GL11.glDisable(GL11.GL_BLEND);

			if (foodEaten.foodValues.hunger > i)
				drawTexturedModalRect(x, y, foodEaten.foodValues.hunger - 1 == i ? 61 : 52, 27, 9, 9);

			x += HUNGER_BAR_WIDTH;
		}
	}
}
