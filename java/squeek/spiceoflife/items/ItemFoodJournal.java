package squeek.spiceoflife.items;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import squeek.spiceoflife.ModConfig;
import squeek.spiceoflife.ModContent;
import squeek.spiceoflife.ModInfo;
import squeek.spiceoflife.gui.GuiScreenFoodJournal;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemFoodJournal extends Item
{

	public ItemFoodJournal()
	{
		super();
		setMaxStackSize(1);
		setTextureName(ModInfo.MODID.toLowerCase(Locale.ROOT) + ":" + ModConfig.ITEM_FOOD_JOURNAL_NAME);
		setUnlocalizedName(ModInfo.MODID.toLowerCase(Locale.ROOT) + "." + ModConfig.ITEM_FOOD_JOURNAL_NAME);
		setCreativeTab(CreativeTabs.tabMisc);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		if (world.isRemote)
			Minecraft.getMinecraft().displayGuiScreen(new GuiScreenFoodJournal());
		return super.onItemRightClick(itemStack, world, player);
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
