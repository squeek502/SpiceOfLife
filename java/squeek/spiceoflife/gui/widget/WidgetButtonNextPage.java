package squeek.spiceoflife.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WidgetButtonNextPage extends GuiButton
{
	private static final ResourceLocation bookGui = new ResourceLocation("textures/gui/book.png");
	/**
	 * True for pointing right (next page), false for pointing left (previous page).
	 */
	private final boolean nextPage;

	public WidgetButtonNextPage(int par1, int par2, int par3, boolean par4)
	{
		super(par1, par2, par3, 23, 13, "");
		this.nextPage = par4;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		if (this.drawButton)
		{
			boolean flag = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			par1Minecraft.getTextureManager().bindTexture(bookGui);
			int k = 0;
			int l = 192;

			if (flag)
			{
				k += 23;
			}

			if (!this.nextPage)
			{
				l += 13;
			}

			this.drawTexturedModalRect(this.xPosition, this.yPosition, k, l, 23, 13);
		}
	}
}
