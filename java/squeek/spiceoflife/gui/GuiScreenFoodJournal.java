package squeek.spiceoflife.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.foodqueue.FixedHungerQueue;
import squeek.spiceoflife.foodtracker.foodqueue.FixedSizeQueue;
import squeek.spiceoflife.foodtracker.foodqueue.FixedTimeQueue;
import squeek.spiceoflife.gui.widget.WidgetButtonNextPage;
import squeek.spiceoflife.gui.widget.WidgetButtonSortDirection;
import squeek.spiceoflife.gui.widget.WidgetFoodEaten;
import squeek.spiceoflife.helpers.MiscHelper;
import squeek.spiceoflife.helpers.StringHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiScreenFoodJournal extends GuiContainer
{
	private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	public static final DecimalFormat dfOne = new DecimalFormat("#.#");

	private int bookImageWidth = 192;
	private int bookImageHeight = 192;

	protected List<WidgetFoodEaten> foodEatenWidgets = new ArrayList<WidgetFoodEaten>();
	protected int pageNum = 0;
	protected static final int numPerPage = 5;
	protected int numPages;
	@Nonnull public ItemStack hoveredStack = ItemStack.EMPTY;

	protected GuiButton buttonNextPage;
	protected GuiButton buttonPrevPage;
	protected WidgetButtonSortDirection buttonSortDirection;

	public GuiScreenFoodJournal()
	{
		super(Minecraft.getMinecraft().player.inventoryContainer);
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.buttonList.add(buttonPrevPage = new WidgetButtonNextPage(1, (this.width - this.bookImageWidth) / 2 + 38, 2 + 154, false));
		this.buttonList.add(buttonNextPage = new WidgetButtonNextPage(2, (this.width - this.bookImageWidth) / 2 + 120, 2 + 154, true));

		this.buttonList.add(buttonSortDirection = new WidgetButtonSortDirection(3, this.width / 2 - 55, 2 + 16, false));

		foodEatenWidgets.clear();
		FoodHistory foodHistory = FoodHistory.get(mc.player);

		if (!ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD || foodHistory.totalFoodsEatenAllTime >= ModConfig.FOOD_EATEN_THRESHOLD)
		{
			for (FoodEaten foodEaten : foodHistory.getHistory())
			{
				foodEatenWidgets.add(new WidgetFoodEaten(foodEaten));
			}
		}

		numPages = (int) Math.max(1, Math.ceil((float) foodEatenWidgets.size() / numPerPage));

		updateButtons();
	}

	private void updateButtons()
	{
		this.buttonNextPage.visible = this.pageNum < this.numPages - 1;
		this.buttonPrevPage.visible = this.pageNum > 0;
	}

	public static String getTimeEatenString(FoodEaten foodEaten)
	{
		Minecraft mc = Minecraft.getMinecraft();
		long elapsedTime = foodEaten.elapsedTime(mc.world.getTotalWorldTime(), FoodHistory.get(mc.player).ticksActive);
		double daysElapsed = elapsedTime / (double) MiscHelper.TICKS_PER_DAY;
		String numDays = dfOne.format(daysElapsed);
		String singularOrPlural = numDays.equals("1") ? "spiceoflife.gui.x.day" : "spiceoflife.gui.x.days";
		String daysAgo = I18n.format(singularOrPlural, numDays);
		return I18n.format("spiceoflife.gui.time.elapsed.since.food.eaten", daysAgo);
	}

	public static String getExpiresInString(FoodEaten foodEaten)
	{
		Minecraft mc = Minecraft.getMinecraft();
		FoodHistory foodHistory = FoodHistory.get(mc.player);

		if (ModConfig.USE_HUNGER_QUEUE)
		{
			FixedHungerQueue queue = (FixedHungerQueue) foodHistory.getHistory();
			FixedHungerQueue slice = queue.sliceUntil(foodEaten);
			int hungerOverflow = queue.totalHunger() - queue.hunger();
			int hungerNeededIfThisWereFirst = foodEaten.foodValues.hunger - hungerOverflow;
			int spaceInQueue = queue.getMaxSize() - queue.hunger();
			int sliceHunger = slice.totalHunger();
			int hungerUntilExpire = Math.max(1, spaceInQueue + hungerNeededIfThisWereFirst + sliceHunger);
			return I18n.format("spiceoflife.gui.expires.in.hunger", StringHelper.hungerHistoryLength(hungerUntilExpire));
		}
		else if (ModConfig.USE_TIME_QUEUE)
		{
			FixedTimeQueue queue = (FixedTimeQueue) foodHistory.getHistory();
			long elapsedTime = foodEaten.elapsedTime(mc.world.getTotalWorldTime(), foodHistory.ticksActive);
			long maxTime = queue.getMaxTime();
			long timeUntilExpire = maxTime - elapsedTime;
			double daysUntilExpire = timeUntilExpire / (double) MiscHelper.TICKS_PER_DAY;
			String numDays = dfOne.format(daysUntilExpire);
			String singularOrPlural = numDays.equals("1") ? "spiceoflife.gui.x.day" : "spiceoflife.gui.x.days";
			String value = I18n.format(singularOrPlural, numDays);
			return I18n.format("spiceoflife.gui.expires.in.time", value);
		}
		else
		{
			FixedSizeQueue queue = (FixedSizeQueue) foodHistory.getHistory();
			int spaceInQueue = queue.getMaxSize() - queue.size();
			int foodsUntilExpire = spaceInQueue + queue.indexOf(foodEaten) + 1;
			String singularOrPlural = foodsUntilExpire == 1 ? I18n.format("spiceoflife.tooltip.times.singular") : I18n.format("spiceoflife.tooltip.times.plural");
			return I18n.format("spiceoflife.gui.expires.in.food", dfOne.format(foodsUntilExpire), singularOrPlural);
		}
	}

	public static boolean isMouseInsideBox(int mouseX, int mouseY, int x, int y, int w, int h)
	{
		return mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(bookGuiTextures);
		int x = (this.width - this.bookImageWidth) / 2;
		int y = 2;
		this.drawTexturedModalRect(x, y, 0, 0, this.bookImageWidth, this.bookImageHeight);

		boolean sortedDescending = buttonSortDirection.sortDesc;
		int startIndex = Math.max(0, pageNum * numPerPage);
		int endIndex = startIndex + numPerPage;
		int totalNum = foodEatenWidgets.size();
		if (totalNum > 0)
		{
			int firstItemNum = sortedDescending ? totalNum - startIndex : startIndex + 1;
			int lastItemNum = sortedDescending ? Math.max(1, totalNum - endIndex + 1) : Math.min(totalNum, endIndex);
			String pageIndicator = I18n.format("spiceoflife.gui.items.on.page", firstItemNum, lastItemNum, totalNum);
			fontRendererObj.drawString(pageIndicator, x + this.bookImageWidth - this.fontRendererObj.getStringWidth(pageIndicator) - 44, y + 16, 0);
		}

		String numFoodsEatenAllTime = Integer.toString(FoodHistory.get(mc.player).totalFoodsEatenAllTime);
		int allTimeW = fontRendererObj.getStringWidth(numFoodsEatenAllTime);
		int allTimeX = width / 2 - allTimeW / 2 - 5;
		int allTimeY = y + 158;
		fontRendererObj.drawString(numFoodsEatenAllTime, allTimeX, allTimeY, 0xa0a0a0);

		for (Object objButton : this.buttonList)
		{
			((GuiButton) objButton).drawButton(mc, mouseX, mouseY);
		}

		if (!ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD || FoodHistory.get(mc.player).totalFoodsEatenAllTime >= ModConfig.FOOD_EATEN_THRESHOLD)
		{
			if (!foodEatenWidgets.isEmpty())
			{
				GlStateManager.pushMatrix();
				RenderHelper.enableGUIStandardItemLighting();
				int foodEatenIndex = startIndex;
				while (foodEatenIndex < foodEatenWidgets.size() && foodEatenIndex < endIndex)
				{
					WidgetFoodEaten foodEatenWidget = foodEatenWidgets.get(foodEatenIndex);
					int localX = x + 36;
					int localY = y + 32 + (int) ((foodEatenIndex - startIndex) * fontRendererObj.FONT_HEIGHT * 2.5f);
					foodEatenWidget.draw(localX, localY);
					if (foodEatenWidget.foodEaten.itemStack != ItemStack.EMPTY)
						drawItemStack(foodEatenWidget.foodEaten.itemStack, localX, localY);

					foodEatenIndex++;
				}
				GlStateManager.popMatrix();

				hoveredStack = ItemStack.EMPTY;
				foodEatenIndex = startIndex;
				while (foodEatenIndex < foodEatenWidgets.size() && foodEatenIndex < endIndex)
				{
					WidgetFoodEaten foodEatenWidget = foodEatenWidgets.get(foodEatenIndex);

					int localX = x + 36;
					int localY = y + 32 + (int) ((foodEatenIndex - startIndex) * fontRendererObj.FONT_HEIGHT * 2.5f);

					if (isMouseInsideBox(mouseX, mouseY, localX, localY, 16, 16))
					{
						hoveredStack = foodEatenWidget.foodEaten.itemStack;
						if (hoveredStack != ItemStack.EMPTY)
							this.renderToolTip(hoveredStack, mouseX, mouseY);
					}
					else if (isMouseInsideBox(mouseX, mouseY, localX + WidgetFoodEaten.PADDING_LEFT, localY, foodEatenWidget.width(), 16))
					{
						List<String> toolTipStrings = new ArrayList<String>();
						int foodIndex = sortedDescending ? Math.max(1, totalNum - foodEatenIndex) : foodEatenIndex + 1;
						toolTipStrings.add(I18n.format("spiceoflife.gui.food.num", foodIndex));
						toolTipStrings.add(TextFormatting.GRAY + getTimeEatenString(foodEatenWidget.foodEaten));
						List<String> splitExpiresIn = fontRendererObj.listFormattedStringToWidth(TextFormatting.DARK_AQUA.toString() + TextFormatting.ITALIC + getExpiresInString(foodEatenWidget.foodEaten), 150);
						toolTipStrings.addAll(splitExpiresIn);
						this.drawHoveringText(toolTipStrings, mouseX, mouseY, fontRendererObj);
					}

					foodEatenIndex++;
				}
			}
			else
			{
				this.fontRendererObj.drawSplitString(I18n.format("spiceoflife.gui.no.recent.food.eaten"), x + 36, y + 16 + 16, 116, 0x404040);
			}
		}
		else
		{
			this.fontRendererObj.drawSplitString(I18n.format("spiceoflife.gui.no.food.history.yet"), x + 36, y + 16 + 16, 116, 0x404040);
		}

		if (isMouseInsideBox(mouseX, mouseY, allTimeX, allTimeY, allTimeW, fontRendererObj.FONT_HEIGHT))
		{
			this.drawHoveringText(Collections.singletonList(I18n.format("spiceoflife.gui.alltime.food.eaten")), mouseX, mouseY, fontRendererObj);
		}

		GlStateManager.disableLighting();
	}

	protected void drawItemStack(@Nonnull ItemStack stack, int x, int y)
	{
		zLevel = 100.0F;
		itemRender.zLevel = 100.0F;
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		zLevel = 0.0F;
		itemRender.zLevel = 0.0F;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException
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
