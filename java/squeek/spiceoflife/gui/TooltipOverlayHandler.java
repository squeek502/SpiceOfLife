package squeek.spiceoflife.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.asm.Hooks;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.FoodValues;
import squeek.spiceoflife.helpers.FoodHelper;
import squeek.spiceoflife.helpers.KeyHelper;
import squeek.spiceoflife.items.ItemFoodContainer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TooltipOverlayHandler implements ITickHandler
{
	//private static final Field guiLeft = ReflectionHelper.findField(GuiContainer.class, ObfuscationReflectionHelper.remapFieldNames(GuiContainer.class.getName(), "guiLeft", "field_74198_m", "p"));
	//private static final Field guiTop = ReflectionHelper.findField(GuiContainer.class, ObfuscationReflectionHelper.remapFieldNames(GuiContainer.class.getName(), "guiTop", "field_74197_n", "q"));
	public static final Field theSlot = ReflectionHelper.findField(GuiContainer.class, ObfuscationReflectionHelper.remapFieldNames(GuiContainer.class.getName(), "theSlot", "field_82320_o", "t"));
	private static Class<?> tinkersContainerGui = null;
	private static Field mainSlot = null;
	private static Method getStackMouseOver = null;
	private static Field itemPanel = null;
	private static boolean neiLoaded = false;
	static
	{
		try
		{
			neiLoaded = Loader.isModLoaded("NotEnoughItems");
			if (neiLoaded)
			{
				Class<?> LayoutManager = Class.forName("codechicken.nei.LayoutManager");
				itemPanel = LayoutManager.getDeclaredField("itemPanel");
				getStackMouseOver = Class.forName("codechicken.nei.ItemPanel").getDeclaredMethod("getStackMouseOver", int.class, int.class);
			}
		}
		catch (Exception e)
		{
			ModSpiceOfLife.Log.warning("Unable to integrate the food values tooltip overlay with NEI: ");
			e.printStackTrace();
		}

		try
		{
			if (Loader.isModLoaded("TConstruct"))
			{
				tinkersContainerGui = ReflectionHelper.getClass(TooltipOverlayHandler.class.getClassLoader(), "tconstruct.client.gui.NewContainerGui");
				mainSlot = ReflectionHelper.findField(tinkersContainerGui, "mainSlot");
			}
		}
		catch (Exception e)
		{
			ModSpiceOfLife.Log.warning("Unable to integrate the food values tooltip overlay with Tinkers Construct: ");
			e.printStackTrace();
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if (ModConfig.SHOW_FOOD_VALUES_IN_TOOLTIP && type.contains(TickType.RENDER))
		{
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.thePlayer;
			GuiScreen curScreen = mc.currentScreen;
			ScaledResolution scale = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
			boolean isTinkersContainerGui = (tinkersContainerGui != null && tinkersContainerGui.isInstance(curScreen));
			boolean isValidContainerGui = curScreen instanceof GuiContainer || isTinkersContainerGui;
			if (isValidContainerGui && KeyHelper.isShiftKeyDown())
			{
				Gui gui = curScreen;
				int mouseX = Mouse.getX() * scale.getScaledWidth() / mc.displayWidth;
				int mouseY = scale.getScaledHeight() - Mouse.getY() * scale.getScaledHeight() / mc.displayHeight;
				ItemStack hoveredStack = null;

				// get the hovered stack from the active container
				try
				{
					// try regular container
					Slot hoveredSlot = !isTinkersContainerGui ? (Slot) TooltipOverlayHandler.theSlot.get(gui) : (Slot) TooltipOverlayHandler.mainSlot.get(gui);

					// get the stack
					if (hoveredSlot != null)
						hoveredStack = hoveredSlot.getStack();

					// try NEI
					if (hoveredStack == null && getStackMouseOver != null)
						hoveredStack = (ItemStack) (getStackMouseOver.invoke(itemPanel.get(null), mouseX, mouseY));

					// try FoodJournal
					if (hoveredStack == null && gui instanceof GuiScreenFoodJournal)
						hoveredStack = ((GuiScreenFoodJournal) gui).hoveredStack;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// if the hovered stack is a food and there is no item being dragged
				if (player.inventory.getItemStack() == null && hoveredStack != null && (FoodHelper.isFood(hoveredStack) || FoodHelper.isFoodContainer(hoveredStack)))
				{
					if (FoodHelper.isFoodContainer(hoveredStack))
					{
						hoveredStack = ((ItemFoodContainer) hoveredStack.getItem()).getBestFoodForPlayerToEat(hoveredStack, player);

						if (hoveredStack == null)
							return;
					}

					FoodValues defaultFoodValues = FoodValues.get(hoveredStack);

					if (defaultFoodValues.hunger == 0 && defaultFoodValues.saturationModifier == 0)
						return;

					FoodValues modifiedFoodValues = FoodModifier.getModifiedFoodValues(defaultFoodValues, FoodModifier.getFoodModifier(player, hoveredStack, player.getFoodStats(), defaultFoodValues.hunger, defaultFoodValues.saturationModifier));

					int barsNeeded = (int) Math.ceil(Math.abs(defaultFoodValues.hunger) / 2f);
					int saturationBarsNeeded = (int) Math.max(1, Math.ceil(Math.abs(defaultFoodValues.getSaturationIncrement()) / 2f));
					boolean saturationOverflow = saturationBarsNeeded > 10;
					String saturationText = saturationOverflow ? ((defaultFoodValues.saturationModifier < 0 ? -1 : 1) * saturationBarsNeeded) + "x " : null;
					if (saturationOverflow)
						saturationBarsNeeded = 1;

					boolean needsCoordinateShift = isTinkersContainerGui || (!isTinkersContainerGui && !neiLoaded) || gui instanceof GuiScreenFoodJournal;
					//int toolTipTopY = Hooks.toolTipY;
					//int toolTipLeftX = Hooks.toolTipX;
					int toolTipBottomY = Hooks.toolTipY + Hooks.toolTipH + 1 + (needsCoordinateShift ? 3 : 0);
					int toolTipRightX = Hooks.toolTipX + Hooks.toolTipW + 1 + (needsCoordinateShift ? 3 : 0);

					boolean shouldDrawBelow = toolTipBottomY + 20 < scale.getScaledHeight() - 3;

					int rightX = toolTipRightX - 3;
					int leftX = rightX - (Math.max(barsNeeded * 9, saturationBarsNeeded * 6 + (int) (mc.fontRenderer.getStringWidth(saturationText) * 0.75f))) - 4;
					int topY = (shouldDrawBelow ? toolTipBottomY : Hooks.toolTipY - 20 + (needsCoordinateShift ? -4 : 0));
					int bottomY = topY + (shouldDrawBelow ? 20 : 20);

					boolean wasLightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
					if (wasLightingEnabled)
						GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);

					// bg
					Gui.drawRect(leftX - 1, topY, rightX + 1, bottomY, 0xF0100010);
					Gui.drawRect(leftX, (shouldDrawBelow ? bottomY : topY - 1), rightX, (shouldDrawBelow ? bottomY + 1 : topY), 0xF0100010);
					Gui.drawRect(leftX, topY, rightX, bottomY, 0x66FFFFFF);

					int x = rightX - 2;
					int startX = x;
					int y = bottomY - 19;

					/*
					// cur out of default saturation bar
					float defaultToModifiedSaturationRatio = modifiedFoodValues.saturationModifier / defaultFoodValues.saturationModifier;

					Gui.drawRect(x - 30, y, x, y + 9, 0xDD000000);
					
					int modifiedSaturationXOffset = (int) Math.max(1, (1 - defaultToModifiedSaturationRatio) * 29);
					Gui.drawRect(x - modifiedSaturationXOffset, y + 1, x - 1, y + 8, 0x55666666);
					Gui.drawRect(x - 29, y + 1, x - modifiedSaturationXOffset, y + 8, ColorHelper.getRelativeColorInt(modifiedFoodValues.saturationModifier, 0, 1));

					GL11.glPushMatrix();
					GL11.glScalef(0.5F, 0.5F, 0.5F);

					String hungerUnitBufferFromSaturation = new DecimalFormat("+#.##;-#").format(modifiedFoodValues.hunger * modifiedFoodValues.saturationModifier);
					mc.fontRenderer.drawStringWithShadow(hungerUnitBufferFromSaturation, x * 2 + 4, y * 2 + 9 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
					
					String defFoodValueStr = new DecimalFormat(".##").format(defaultFoodValues.saturationModifier);
					int defFoodValueStrWidth = mc.fontRenderer.getStringWidth(defFoodValueStr);
					mc.fontRenderer.drawStringWithShadow(defFoodValueStr, x * 2 - defFoodValueStrWidth - 4, y * 2 + 9 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);

					if (modifiedFoodValues.saturationModifier != defaultFoodValues.saturationModifier)
					{
						String modFoodValueStr = new DecimalFormat(".##").format(modifiedFoodValues.saturationModifier);
						mc.fontRenderer.drawStringWithShadow(modFoodValueStr, x * 2 - 30 * 2 + 4, y * 2 + 9 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
						mc.fontRenderer.drawStringWithShadow("of", x * 2 - 15 * 2 - mc.fontRenderer.getStringWidth("of")/2, y * 2 + 9 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
					}

					GL11.glPopMatrix();
					x -= 33;
					*/

					GL11.glColor4f(1f, 1f, 1f, .25f);

					mc.getTextureManager().bindTexture(Gui.icons);

					for (int i = 0; i < barsNeeded * 2; i += 2)
					{
						x -= 9;

						if (modifiedFoodValues.hunger < 0)
							gui.drawTexturedModalRect(x, y, 34, 27, 9, 9);
						else if (modifiedFoodValues.hunger > i + 1 || defaultFoodValues.hunger == modifiedFoodValues.hunger)
							gui.drawTexturedModalRect(x, y, 16, 27, 9, 9);
						else if (modifiedFoodValues.hunger == i + 1)
							gui.drawTexturedModalRect(x, y, 124, 27, 9, 9);
						else
							gui.drawTexturedModalRect(x, y, 34, 27, 9, 9);

						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						gui.drawTexturedModalRect(x, y, defaultFoodValues.hunger - 1 == i ? 115 : 106, 27, 9, 9);
						GL11.glDisable(GL11.GL_BLEND);

						if (modifiedFoodValues.hunger > i)
							gui.drawTexturedModalRect(x, y, modifiedFoodValues.hunger - 1 == i ? 61 : 52, 27, 9, 9);
					}

					y += 11;
					x = startX;
					float modifiedSaturationIncrement = modifiedFoodValues.getSaturationIncrement();
					float absModifiedSaturationIncrement = Math.abs(modifiedSaturationIncrement);

					GL11.glPushMatrix();
					GL11.glScalef(0.75F, 0.75F, 0.75F);
					GL11.glColor4f(1f, 1f, 1f, .5f);
					for (int i = 0; i < saturationBarsNeeded * 2; i += 2)
					{
						float effectiveSaturationOfBar = (absModifiedSaturationIncrement - i) / 2f;

						x -= 6;

						boolean shouldBeFaded = absModifiedSaturationIncrement <= i;
						if (shouldBeFaded)
						{
							GL11.glEnable(GL11.GL_BLEND);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						}

						mc.getTextureManager().bindTexture(Gui.icons);
						gui.drawTexturedModalRect(x * 4 / 3, y * 4 / 3, 16, 27, 9, 9);

						mc.getTextureManager().bindTexture(new ResourceLocation(ModInfo.MODID.toLowerCase(), "textures/icons.png"));
						gui.drawTexturedModalRect(x * 4 / 3, y * 4 / 3, effectiveSaturationOfBar >= 1 ? 27 : effectiveSaturationOfBar > 0.5 ? 18 : effectiveSaturationOfBar > 0.25 ? 9 : effectiveSaturationOfBar > 0 ? 0 : 36, modifiedSaturationIncrement >= 0 ? 0 : 9, 9, 9);

						if (shouldBeFaded)
							GL11.glDisable(GL11.GL_BLEND);
					}
					if (saturationText != null)
					{
						mc.fontRenderer.drawStringWithShadow(saturationText, x * 4 / 3 - mc.fontRenderer.getStringWidth(saturationText) + 2, y * 4 / 3 + 1, 0xFFFF0000);
					}
					GL11.glPopMatrix();

					GL11.glEnable(GL11.GL_DEPTH_TEST);
					if (wasLightingEnabled)
						GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glColor4f(1f, 1f, 1f, 1f);
				}
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel()
	{
		return ModInfo.MODID;
	}

}
