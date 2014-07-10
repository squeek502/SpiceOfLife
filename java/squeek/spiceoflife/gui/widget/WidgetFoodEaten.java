package squeek.spiceoflife.gui.widget;

import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.FoodValues;
import squeek.spiceoflife.helpers.ColorHelper;

@SideOnly(Side.CLIENT)
public class WidgetFoodEaten extends Gui
{
	public final FoodEaten foodEaten;
	protected Minecraft mc = Minecraft.getMinecraft();

	public WidgetFoodEaten(FoodEaten foodEaten)
	{
		this.foodEaten = foodEaten;
	}

	public void draw(int x, int y)
	{
		x += 18;

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_LIGHTING);
		
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

		FoodValues defaultFoodValues = FoodValues.get(foodEaten.itemStack);
		
		mc.fontRenderer.drawString(displayName, x, y, ColorHelper.getRelativeColorInt(foodEaten.hungerRestored, 0, defaultFoodValues.hunger));

		int barsNeeded = (int) Math.max(1, Math.ceil(Math.abs(defaultFoodValues.hunger) / 2f));

		GL11.glColor4f(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(Gui.icons);
		y += mc.fontRenderer.FONT_HEIGHT;
		for (int i = 0; i < barsNeeded * 2; i += 2)
		{
			this.drawTexturedModalRect(x, y, 16, 27, 9, 9);

			if (foodEaten.hungerRestored < 0)
				drawTexturedModalRect(x, y, 34, 27, 9, 9);
			else if (foodEaten.hungerRestored > i + 1 || defaultFoodValues.hunger ==foodEaten.hungerRestored)
				drawTexturedModalRect(x, y, 16, 27, 9, 9);
			else if (foodEaten.hungerRestored == i + 1)
				drawTexturedModalRect(x, y, 124, 27, 9, 9);
			else
				drawTexturedModalRect(x, y, 34, 27, 9, 9);

			GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_DST_COLOR);
			drawTexturedModalRect(x, y, defaultFoodValues.hunger - 1 == i ? 115 : 106, 27, 9, 9);
			GL11.glDisable(GL11.GL_BLEND);

			if (foodEaten.hungerRestored > i)
				drawTexturedModalRect(x, y, foodEaten.hungerRestored - 1 == i ? 61 : 52, 27, 9, 9);

			x += 9;
		}
	}
}
