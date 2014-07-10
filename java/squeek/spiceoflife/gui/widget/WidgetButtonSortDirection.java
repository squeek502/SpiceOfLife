package squeek.spiceoflife.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import squeek.spiceoflife.ModInfo;

public class WidgetButtonSortDirection extends GuiButton
{
	private static final ResourceLocation modIcons = new ResourceLocation(ModInfo.MODID.toLowerCase(), "textures/icons.png");
	
    /**
     * True for pointing right (next page), false for pointing left (previous page).
     */
    public boolean sortDesc;

    public WidgetButtonSortDirection(int id, int x, int y, boolean sortDesc)
    {
        super(id, x, y, 11, 8, "");
        this.sortDesc = sortDesc;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.drawButton)
        {
            boolean isHovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(modIcons);
            int x = 0;
            int y = 63;

            if (isHovered)
            {
                x += this.width;
            }

            if (!sortDesc)
            {
                x += this.width*2;
            }

            this.drawTexturedModalRect(this.xPosition, this.yPosition, x, y, width, height);
        }
    }
}
