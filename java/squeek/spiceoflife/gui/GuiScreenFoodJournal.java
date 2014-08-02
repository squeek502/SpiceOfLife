package squeek.spiceoflife.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.gui.widget.WidgetButtonNextPage;
import squeek.spiceoflife.gui.widget.WidgetButtonSortDirection;
import squeek.spiceoflife.gui.widget.WidgetFoodEaten;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScreenFoodJournal extends GuiContainer
{
	private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");

	private int bookImageWidth = 192;
	private int bookImageHeight = 192;

	protected List<WidgetFoodEaten> foodEatenWidgets = new ArrayList<WidgetFoodEaten>();
	protected int pageNum = 0;
	protected final int numPerPage = 5;
	protected int numPages;
	public ItemStack hoveredStack = null;
	
	protected GuiButton buttonNextPage;
	protected GuiButton buttonPrevPage;
	protected WidgetButtonSortDirection buttonSortDirection;

	public GuiScreenFoodJournal()
	{
		super(new DummyContainer());
	}

	private static class DummyContainer extends Container
	{

		@Override
		public boolean canInteractWith(EntityPlayer entityplayer)
		{
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();

		this.buttonList.add(buttonPrevPage = new WidgetButtonNextPage(1, (this.width - this.bookImageWidth) / 2 + 38, 2 + 154, false));
		this.buttonList.add(buttonNextPage = new WidgetButtonNextPage(2, (this.width - this.bookImageWidth) / 2 + 120, 2 + 154, true));

		this.buttonList.add(buttonSortDirection = new WidgetButtonSortDirection(3, this.width / 2 - 55, 2 + 16, false));

		FoodHistory foodHistory = FoodHistory.get(mc.thePlayer);
		if (!ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD || foodHistory.totalFoodsEatenAllTime >= ModConfig.FOOD_EATEN_THRESHOLD)
		{
			for (FoodEaten foodEaten : foodHistory.getHistory())
			{
				foodEatenWidgets.add(new WidgetFoodEaten(foodEaten));
			}
		}
		
		numPages = (int) Math.max(1, Math.ceil((float)foodEatenWidgets.size()/numPerPage));
		
		updateButtons();
	}

    private void updateButtons()
    {
        this.buttonNextPage.drawButton = this.pageNum < this.numPages - 1;
        this.buttonPrevPage.drawButton = this.pageNum > 0;
    }

	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(bookGuiTextures);
		int x = (this.width - this.bookImageWidth) / 2;
		int y = 2;
		this.drawTexturedModalRect(x, y, 0, 0, this.bookImageWidth, this.bookImageHeight);

		String pageIndicator = String.format(I18n.getString("book.pageIndicator"), new Object[]{Integer.valueOf(pageNum + 1), Integer.valueOf(numPages)});
		fontRenderer.drawString(pageIndicator, x + this.bookImageWidth - this.fontRenderer.getStringWidth(pageIndicator) - 44, y + 16, 0);

		String numFoodsEatenAllTime = Integer.toString(FoodHistory.get(mc.thePlayer).totalFoodsEatenAllTime);
		int allTimeW = fontRenderer.getStringWidth(numFoodsEatenAllTime);
		int allTimeX = width / 2 - allTimeW / 2 - 5;
		int allTimeY = y + 158;
		fontRenderer.drawString(numFoodsEatenAllTime, allTimeX, allTimeY, 0xa0a0a0);

		GL11.glDisable(GL11.GL_LIGHTING);
		for (Object objButton : this.buttonList)
		{
			((GuiButton) objButton).drawButton(mc, par1, par2);
		}
		
		if (!ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD || FoodHistory.get(mc.thePlayer).totalFoodsEatenAllTime >= ModConfig.FOOD_EATEN_THRESHOLD)
		{
			if (foodEatenWidgets.size() > 0)
			{
				GL11.glPushMatrix();
				int startIndex = Math.max(0, pageNum * numPerPage);
				int endIndex = startIndex + numPerPage;
				int foodEatenIndex = startIndex;
				while (foodEatenIndex < foodEatenWidgets.size() && foodEatenIndex < endIndex)
				{
					WidgetFoodEaten foodEatenWidget = foodEatenWidgets.get(foodEatenIndex);
					int localX = x + 36;
					int localY = y + 32 + (int) ((foodEatenIndex - startIndex) * fontRenderer.FONT_HEIGHT * 2.5f);
					foodEatenWidget.draw(localX, localY);
					if (foodEatenWidget.foodEaten.itemStack != null)
						drawItemStack(foodEatenWidget.foodEaten.itemStack, localX, localY);
		
					foodEatenIndex++;
				}
				GL11.glPopMatrix();
		
				hoveredStack = null;
				foodEatenIndex = startIndex;
				while (foodEatenIndex < foodEatenWidgets.size() && foodEatenIndex < endIndex)
				{
					WidgetFoodEaten foodEatenWidget = foodEatenWidgets.get(foodEatenIndex);
		
					int localX = x + 36;
					int localY = y + 32 + (int) ((foodEatenIndex - startIndex) * fontRenderer.FONT_HEIGHT * 2.5f);
		
					if (par1 >= localX && par1 < localX + 16 && par2 >= localY && par2 < localY + 16)
					{
						hoveredStack = foodEatenWidget.foodEaten.itemStack;
						if (hoveredStack != null)
							this.drawItemStackTooltip(hoveredStack, par1, par2);
					}
		
					foodEatenIndex++;
				}
			}
			else
			{
	            this.fontRenderer.drawSplitString(StatCollector.translateToLocal("spiceoflife.gui.no.recent.food.eaten"), x + 36, y + 16 + 16, 116, 0x404040);
			}
		}
		else
		{
            this.fontRenderer.drawSplitString(StatCollector.translateToLocal("spiceoflife.gui.no.food.history.yet"), x + 36, y + 16 + 16, 116, 0x404040);
		}

		if (par1 >= allTimeX && par2 >= allTimeY && par1 < allTimeX+allTimeW && par2 < allTimeY+fontRenderer.FONT_HEIGHT)
		{
			this.drawHoveringText(Arrays.asList(new String[] {StatCollector.translateToLocal("spiceoflife.gui.alltime.food.eaten")}), par1, par2, fontRenderer);
		}

		GL11.glDisable(GL11.GL_LIGHTING);
	}

	protected void drawItemStack(ItemStack par1ItemStack, int par2, int par3)
	{
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		this.zLevel = 200.0F;
		itemRenderer.zLevel = 200.0F;
		FontRenderer font = null;
		if (par1ItemStack != null)
			font = par1ItemStack.getItem().getFontRenderer(par1ItemStack);
		if (font == null)
			font = fontRenderer;
		itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), par1ItemStack, par2, par3);
		this.zLevel = 0.0F;
		itemRenderer.zLevel = 0.0F;
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);

		if (button.enabled)
		{
			if (button.id == 1)
			{
				this.pageNum--;
			}
			else if (button.id == 2)
			{
				this.pageNum++;
			}
			else if (button.id == 3)
			{
				Collections.reverse(foodEatenWidgets);
				buttonSortDirection.sortDesc = !buttonSortDirection.sortDesc;
			}

			updateButtons();
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
	{

	}
}
