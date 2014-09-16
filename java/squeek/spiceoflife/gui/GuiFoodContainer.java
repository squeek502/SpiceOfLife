package squeek.spiceoflife.gui;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.helpers.GuiHelper;
import squeek.spiceoflife.inventory.ContainerFoodContainer;

public class GuiFoodContainer extends GuiContainer
{
	protected IInventory playerInventory = null;
	protected IInventory inventory = null;
	public static final ResourceLocation guiTexture = new ResourceLocation(ModInfo.MODID.toLowerCase(), "textures/gui/foodcontainer.png");
	public int xStart;
	public int yStart;

	public GuiFoodContainer(InventoryPlayer playerInventory, IInventory foodContainerInventory, ItemStack itemStack)
	{
		super(new ContainerFoodContainer(playerInventory, foodContainerInventory, itemStack));
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
		this.fontRenderer.drawString(this.inventory.isInvNameLocalized() ? this.inventory.getInvName() : I18n.getString(this.inventory.getInvName()), 8, 6, 4210752);
		this.fontRenderer.drawString(this.playerInventory.isInvNameLocalized() ? this.playerInventory.getInvName() : I18n.getString(this.playerInventory.getInvName()), 8, this.ySize - 96 + 3, 4210752);

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
