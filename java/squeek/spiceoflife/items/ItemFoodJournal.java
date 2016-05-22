package squeek.spiceoflife.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModContent;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.gui.GuiScreenFoodJournal;

import java.util.Locale;

public class ItemFoodJournal extends Item
{
	public ItemFoodJournal()
	{
		super();
		setMaxStackSize(1);
		setRegistryName(ModConfig.ITEM_FOOD_JOURNAL_NAME);
		setUnlocalizedName(ModInfo.MODID.toLowerCase(Locale.ROOT) + '.' + ModConfig.ITEM_FOOD_JOURNAL_NAME);
		setCreativeTab(CreativeTabs.MISC);

		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
		{
			registerModels();
		}
	}

	@SideOnly(Side.CLIENT)
	public void registerModels()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand)
	{
		if (world.isRemote)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiScreenFoodJournal());
			return new ActionResult(EnumActionResult.SUCCESS, itemStack);
		}
		return super.onItemRightClick(itemStack, world, player, hand);
	}

	public static void giveToPlayer(EntityPlayer player)
	{
		if (player != null && !player.worldObj.isRemote)
		{
			ItemStack itemStack = new ItemStack(ModContent.foodJournal);
			// try add, otherwise spawn in the world
			if (!player.inventory.addItemStackToInventory(itemStack))
			{
				EntityItem entityItem = new EntityItem(player.worldObj, player.posX + 0.5f, player.posY + 0.5f, player.posZ + 0.5f, itemStack);
				player.worldObj.spawnEntityInWorld(entityItem);
			}
		}
	}
}
