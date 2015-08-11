package squeek.spiceoflife.gui;

import java.util.Locale;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.helpers.GuiHelper;
import squeek.spiceoflife.inventory.ContainerFoodContainer;
import squeek.spiceoflife.inventory.FoodContainerInventory;

public class GuiFoodContainer extends GuiContainer
{
	protected IInventory playerInventory = null;
	protected IInventory inventory = null;
	public static final ResourceLocation guiTexture = new ResourceLocation(ModInfo.MODID.toLowerCase(Locale.ROOT), "textures/gui/foodcontainer.png");
	public int xStart;
	public int yStart;

	public GuiFoodContainer(InventoryPlayer playerInventory, FoodContainerInventory foodContainerInventory)
	{
		super(new ContainerFoodContainer(playerInventory, foodContainerInventory));
		this.inventory = foodContainerInventory;
		this.playerInventory = playerInventory;
		this.ySize = 133;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.xStart = (this.width - this.xSize) / 2;
		this.yStart = (this.height - this.ySize) / 2;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.fontRendererObj.drawString(this.inventory.hasCustomInventoryName() ? this.inventory.getInventoryName() : I18n.format(this.inventory.getInventoryName()), 8, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.hasCustomInventoryName() ? this.playerInventory.getInventoryName() : I18n.format(this.playerInventory.getInventoryName()), 8, this.ySize - 96 + 3, 4210752);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(guiTexture);
		this.drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

		int slotsX = ((ContainerFoodContainer) inventorySlots).slotsX - 1;
		int slotsY = ((ContainerFoodContainer) inventorySlots).slotsY - 1;
		for (int slotNum = 0; slotNum < inventory.getSizeInventory(); slotNum++)
		{
			int x = slotsX + slotNum * GuiHelper.STANDARD_SLOT_WIDTH;
			drawTexturedModalRect(xStart + x, yStart + slotsY, GuiHelper.STANDARD_GUI_WIDTH, 0, GuiHelper.STANDARD_SLOT_WIDTH, GuiHelper.STANDARD_SLOT_WIDTH);
		}
	}
}
