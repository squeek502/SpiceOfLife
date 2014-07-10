package squeek.spiceoflife;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.compat.PacketDispatcher;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroup;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.IPacketProcessor;
import squeek.spiceoflife.network.PacketBase;
import squeek.spiceoflife.network.PacketConfigSync;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

public class ModConfig implements IPackable, IPacketProcessor
{
	public static final ModConfig instance = new ModConfig();

	protected ModConfig()
	{
	}

	private static Configuration config;

	private static final String COMMENT_SERVER_SIDE_OPTIONS =
			"These config settings are server-side only\n"
					+ "Their values will get synced to all clients on the server";

	/*
	 * MAIN
	 */
	private static final String CATEGORY_MAIN = " main ";
	private static final String CATEGORY_MAIN_COMMENT =
			COMMENT_SERVER_SIDE_OPTIONS;

	// whether or not food modifier is actually enabled (we either are the server or know the server has it enabled)
	public static boolean FOOD_MODIFIER_ENABLED = false;
	// the value written in the config file
	public static boolean FOOD_MODIFIER_ENABLED_CONFIG_VAL = ModConfig.FOOD_MODIFIER_ENABLED_DEFAULT;
	private static final String FOOD_MODIFIER_ENABLED_NAME = "food.modifier.enabled";
	private static final boolean FOOD_MODIFIER_ENABLED_DEFAULT = true;
	private static final String FOOD_MODIFIER_ENABLED_COMMENT = "If false, disables the entire diminishing returns part of the mod\nSet this to false if you only want the client-side tooltip/HUD additions";

	/*
	 * SERVER
	 */
	private static final String CATEGORY_SERVER = "server";
	private static final String CATEGORY_SERVER_COMMENT =
			COMMENT_SERVER_SIDE_OPTIONS;

	public static int FOOD_HISTORY_LENGTH = ModConfig.FOOD_HISTORY_LENGTH_DEFAULT;
	private static final String FOOD_HISTORY_LENGTH_NAME = "food.history.length";
	private static final int FOOD_HISTORY_LENGTH_DEFAULT = 12;
	private static final String FOOD_HISTORY_LENGTH_COMMENT =
			"The maximum amount of eaten foods stored in the history at a time";

	public static boolean FOOD_HISTORY_PERSISTS_THROUGH_DEATH = ModConfig.FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT;
	private static final String FOOD_HISTORY_PERSISTS_THROUGH_DEATH_NAME = "food.history.persists.through.death";
	private static final boolean FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT = false;
	private static final String FOOD_HISTORY_PERSISTS_THROUGH_DEATH_COMMENT =
			"If true, food history will not get reset after every death";

	public static int FOOD_EATEN_THRESHOLD = ModConfig.FOOD_EATEN_THRESHOLD_DEFAULT;
	private static final String FOOD_EATEN_THRESHOLD_NAME = "new.player.food.eaten.threshold";
	private static final int FOOD_EATEN_THRESHOLD_DEFAULT = ModConfig.FOOD_HISTORY_LENGTH / 2;
	private static final String FOOD_EATEN_THRESHOLD_COMMENT =
			"The number of times a new player (by World) needs to eat before this mod has any effect";

	public static boolean CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD = ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT;
	private static final String CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_NAME = "clear.history.after.food.eaten.threshold.reached";
	private static final boolean CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT = false;
	private static final String CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_COMMENT =
			"If true, a player's food history will be empty once they pass the " + FOOD_EATEN_THRESHOLD_NAME + "\n"
					+ "If false, any food eaten before the threshold is passed will also count after it is passed";

	public static boolean USE_FOOD_GROUPS = ModConfig.USE_FOOD_GROUPS_DEFAULT;
	private static final String USE_FOOD_GROUPS_NAME = "use.food.groups";
	private static final boolean USE_FOOD_GROUPS_DEFAULT = false;
	private static final String USE_FOOD_GROUPS_COMMENT =
			"See 'foodgroups' settings category";

	public static RoundingMode FOOD_HUNGER_ROUNDING_MODE = null;
	public static String FOOD_HUNGER_ROUNDING_MODE_STRING = ModConfig.FOOD_HUNGER_ROUNDING_MODE_DEFAULT;
	private static final String FOOD_HUNGER_ROUNDING_MODE_NAME = "food.hunger.rounding.mode";
	private static final String FOOD_HUNGER_ROUNDING_MODE_DEFAULT = "round";
	private static final String FOOD_HUNGER_ROUNDING_MODE_COMMENT =
			"Rounding mode used on the hunger value of foods\n"
					+ "Valid options: 'round', 'floor', 'ceiling'";

	public static enum RoundingMode
	{
		ROUND("round")
		{
			@Override
			public double round(double val)
			{
				return Math.round(val);
			}
		},
		FLOOR("floor")
		{
			@Override
			public double round(double val)
			{
				return Math.floor(val);
			}
		},
		CEILING("ceiling")
		{
			@Override
			public double round(double val)
			{
				return Math.ceil(val);
			}
		};

		public String id;

		private RoundingMode(String id)
		{
			this.id = id;
		}

		public abstract double round(double val);
	}

	public static boolean AFFECT_FOOD_SATURATION_MODIFIERS = ModConfig.AFFECT_FOOD_SATURATION_MODIFIERS_DEFAULT;
	private static final String AFFECT_FOOD_SATURATION_MODIFIERS_NAME = "affect.food.saturation.modifiers";
	private static final boolean AFFECT_FOOD_SATURATION_MODIFIERS_DEFAULT = false;
	private static final String AFFECT_FOOD_SATURATION_MODIFIERS_COMMENT =
			"If true, foods' saturation modifier will also be multiplied by the nutritional value\n"
					+ "NOTE: Saturation bonuses of foods will automatically decrease as the hunger value of the food decreases\n"
					+ "Setting this to true will make saturation bonuses decrease disproportionately more than hunger values";

	public static boolean AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS = ModConfig.AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_DEFAULT;
	private static final String AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_NAME = "affect.negative.food.saturation.modifiers";
	private static final boolean AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_DEFAULT = false;
	private static final String AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_COMMENT =
			"If true, foods with negative saturation modifiers will be made more negative as nutritional value decreases\n"
					+ "NOTE: " + AFFECT_FOOD_SATURATION_MODIFIERS_NAME + " must be true for this to have any affect";

	public static boolean USE_HUNGER_QUEUE = ModConfig.USE_HUNGER_QUEUE_DEFAULT;
	private static final String USE_HUNGER_QUEUE_NAME = "use.hunger.restored.for.food.history.length";
	private static final boolean USE_HUNGER_QUEUE_DEFAULT = false;
	private static final String USE_HUNGER_QUEUE_COMMENT =
			"If true, " + FOOD_HISTORY_LENGTH_NAME + " will use amount of hunger restored instead of number of foods eaten for its maximum length\n"
					+ "For example, a " + FOOD_HISTORY_LENGTH_NAME + " length of 12 will store a max of 2 foods that restored 6 hunger each, \n"
					+ "3 foods that restored 4 hunger each, 12 foods that restored 1 hunger each, etc\n"
					+ "NOTE: " + FOOD_HISTORY_LENGTH_NAME + " uses hunger units, where 1 hunger unit = 1/2 hunger bar";

	public static String FOOD_MODIFIER_FORMULA = ModConfig.FOOD_MODIFIER_FORMULA_STRING_DEFAULT;
	private static final String FOOD_MODIFIER_FORMULA_STRING_NAME = "food.modifier.formula";
	private static final String FOOD_MODIFIER_FORMULA_STRING_DEFAULT = "MAX(0, (1 - count/12))^MIN(8, food_hunger_value)";
	private static final String FOOD_MODIFIER_FORMULA_STRING_COMMENT =
			"Uses the EvalEx expression parser\n"
					+ "See: https://github.com/uklimaschewski/EvalEx for syntax/function documentation\n\n"
					+ "Available variables:\n"
					+ "\tcount : The number of times the food (or its food group) has been eaten within the food history\n"
					+ "\tmax_history_length : The maximum length of the food history (see " + FOOD_HISTORY_LENGTH_NAME + ")\n"
					+ "\tcur_history_length : The current length of the food history (<= max_history_length)\n"
					+ "\tfood_hunger_value : The default amount of hunger the food would restore in hunger units (1 hunger unit = 1/2 hunger bar)\n"
					+ "\tfood_saturation_mod : The default saturation modifier of the food\n"
					+ "\tcur_hunger : The current hunger value of the player in hunger units (20 = full)\n"
					+ "\tcur_saturation : The current saturation value of the player\n"
					+ "\ttotal_food_eaten : The all-time total number of times any food has been eaten by the player\n";

	public static boolean GIVE_FOOD_JOURNAL_ON_START = ModConfig.GIVE_FOOD_JOURNAL_ON_START_DEFAULT;
	private static final String GIVE_FOOD_JOURNAL_ON_START_NAME = "give.food.journal.as.starting.item";
	private static final boolean GIVE_FOOD_JOURNAL_ON_START_DEFAULT = false;
	private static final String GIVE_FOOD_JOURNAL_ON_START_COMMENT =
			"If true, a food journal will be given to each player as a starting item";

	public static boolean GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS = ModConfig.GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_DEFAULT;
	private static final String GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_NAME = "give.food.journal.on.dimishing.returns.start";
	private static final boolean GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_DEFAULT = false;
	private static final String GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_COMMENT =
			"If true, a food journal will be given to each player once diminishing returns start for them\n"
					+ "Not given if a player was given a food journal by " + ModConfig.GIVE_FOOD_JOURNAL_ON_START_NAME;

	/*
	 * CLIENT
	 */
	private static final String CATEGORY_CLIENT = "client";
	private static final String CATEGORY_CLIENT_COMMENT =
			"These config settings are client-side only";

	public static boolean SHOW_FOOD_VALUES_IN_TOOLTIP = true;
	private static final String SHOW_FOOD_VALUES_IN_TOOLTIP_NAME = "show.food.values.in.tooltip";
	private static final String SHOW_FOOD_VALUES_IN_TOOLTIP_COMMENT =
			"If true, shows the hunger and saturation values of food in its tooltip while holding SHIFT";

	public static boolean SHOW_SATURATION_OVERLAY = true;
	private static final String SHOW_SATURATION_OVERLAY_NAME = "show.saturation.hud.overlay";
	private static final String SHOW_SATURATION_OVERLAY_COMMENT =
			"If true, shows your current saturation level overlayed on the hunger bar";

	public static boolean SHOW_FOOD_VALUES_OVERLAY = true;
	private static final String SHOW_FOOD_VALUES_OVERLAY_NAME = "show.food.values.hud.overlay";
	private static final String SHOW_FOOD_VALUES_OVERLAY_COMMENT =
			"If true, shows the hunger (and saturation if " + SHOW_SATURATION_OVERLAY_NAME + " is true) that would be restored by food you are currently holding";

	// whether or not food exhaustion is actually enabled (we either are the server or know the server has the mod)
	public static boolean SHOW_FOOD_EXHAUSTION_OVERLAY = false;
	// the value written in the config file
	public static boolean SHOW_FOOD_EXHAUSTION_OVERLAY_CONFIG_VAL = ModConfig.FOOD_MODIFIER_ENABLED_DEFAULT;
	private static final String SHOW_FOOD_EXHAUSTION_OVERLAY_NAME = "show.food.exhaustion.hud.overlay";
	private static final String SHOW_FOOD_EXHAUSTION_OVERLAY_COMMENT =
			"If true, shows your food exhaustion as a progress bar behind the hunger bars";

	/*
	 * ITEMS
	 */

	public static int ITEM_FOOD_JOURNAL_ID = ModConfig.ITEM_FOOD_JOURNAL_ID_DEFAULT;
	public static final String ITEM_FOOD_JOURNAL_NAME = "bookfoodjournal";
	public static final int ITEM_FOOD_JOURNAL_ID_DEFAULT = 6850;

	/*
	 * FOOD GROUPS
	 */
	private static final String CATEGORY_FOODGROUPS = "foodgroups";
	private static final String CATEGORY_FOODGROUPS_COMMENT =
			COMMENT_SERVER_SIDE_OPTIONS + "\n"
					+ "NOTE: Food groups are a work-in-progress; not all features have been implemented and/or tested";

	public static void init(File file)
	{
		config = new Configuration(file);

		load();

		/*
		 * MAIN
		 */
		config.getCategory(CATEGORY_MAIN).setComment(CATEGORY_MAIN_COMMENT);
		FOOD_MODIFIER_ENABLED_CONFIG_VAL = config.get(CATEGORY_MAIN, FOOD_MODIFIER_ENABLED_NAME, FOOD_MODIFIER_ENABLED_DEFAULT, FOOD_MODIFIER_ENABLED_COMMENT).getBoolean(FOOD_MODIFIER_ENABLED_DEFAULT);

		// only use the config value immediately when server-side; the client assumes false until the server syncs the config
		if (FMLCommonHandler.instance().getSide() == Side.SERVER)
			FOOD_MODIFIER_ENABLED = FOOD_MODIFIER_ENABLED_CONFIG_VAL;

		/*
		 * SERVER
		 */
		config.getCategory(CATEGORY_SERVER).setComment(CATEGORY_SERVER_COMMENT);

		Property FOOD_MODIFIER_PROPERTY = config.get(CATEGORY_SERVER, FOOD_MODIFIER_FORMULA_STRING_NAME, FOOD_MODIFIER_FORMULA_STRING_DEFAULT, FOOD_MODIFIER_FORMULA_STRING_COMMENT);

		// enforce the new default if the config has the old default
		if (FOOD_MODIFIER_PROPERTY.getString().equals("MAX(0, (1 - count/12))^MAX(0, food_hunger_value-ROUND(MAX(0, 1 - count/12), 0))"))
			FOOD_MODIFIER_PROPERTY.set(FOOD_MODIFIER_FORMULA_STRING_DEFAULT);

		FOOD_MODIFIER_FORMULA = FOOD_MODIFIER_PROPERTY.getString();

		FOOD_HISTORY_LENGTH = config.get(CATEGORY_SERVER, FOOD_HISTORY_LENGTH_NAME, FOOD_HISTORY_LENGTH_DEFAULT, FOOD_HISTORY_LENGTH_COMMENT).getInt();
		FOOD_HISTORY_PERSISTS_THROUGH_DEATH = config.get(CATEGORY_SERVER, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_NAME, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_COMMENT).getBoolean(FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT);
		FOOD_EATEN_THRESHOLD = config.get(CATEGORY_SERVER, FOOD_EATEN_THRESHOLD_NAME, FOOD_EATEN_THRESHOLD_DEFAULT, FOOD_EATEN_THRESHOLD_COMMENT).getInt();
		CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD = config.get(CATEGORY_SERVER, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_NAME, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_COMMENT).getBoolean(CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT);
		USE_FOOD_GROUPS = config.get(CATEGORY_SERVER, USE_FOOD_GROUPS_NAME, USE_FOOD_GROUPS_DEFAULT, USE_FOOD_GROUPS_COMMENT).getBoolean(USE_FOOD_GROUPS_DEFAULT);
		AFFECT_FOOD_SATURATION_MODIFIERS = config.get(CATEGORY_SERVER, AFFECT_FOOD_SATURATION_MODIFIERS_NAME, AFFECT_FOOD_SATURATION_MODIFIERS_DEFAULT, AFFECT_FOOD_SATURATION_MODIFIERS_COMMENT).getBoolean(AFFECT_FOOD_SATURATION_MODIFIERS_DEFAULT);
		AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS = config.get(CATEGORY_SERVER, AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_NAME, AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_DEFAULT, AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_COMMENT).getBoolean(AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_DEFAULT);
		USE_HUNGER_QUEUE = config.get(CATEGORY_SERVER, USE_HUNGER_QUEUE_NAME, USE_HUNGER_QUEUE_DEFAULT, USE_HUNGER_QUEUE_COMMENT).getBoolean(USE_HUNGER_QUEUE_DEFAULT);
		GIVE_FOOD_JOURNAL_ON_START = config.get(CATEGORY_SERVER, GIVE_FOOD_JOURNAL_ON_START_NAME, GIVE_FOOD_JOURNAL_ON_START_DEFAULT, GIVE_FOOD_JOURNAL_ON_START_COMMENT).getBoolean(GIVE_FOOD_JOURNAL_ON_START_DEFAULT);
		GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS = config.get(CATEGORY_SERVER, GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_NAME, GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_DEFAULT, GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_COMMENT).getBoolean(GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_DEFAULT);

		FOOD_HUNGER_ROUNDING_MODE_STRING = config.get(CATEGORY_SERVER, FOOD_HUNGER_ROUNDING_MODE_NAME, FOOD_HUNGER_ROUNDING_MODE_DEFAULT, FOOD_HUNGER_ROUNDING_MODE_COMMENT).getString();
		setRoundingMode();

		/*
		 * CLIENT
		 */
		config.getCategory(CATEGORY_CLIENT).setComment(CATEGORY_CLIENT_COMMENT);

		SHOW_FOOD_VALUES_IN_TOOLTIP = config.get(CATEGORY_CLIENT, SHOW_FOOD_VALUES_IN_TOOLTIP_NAME, true, SHOW_FOOD_VALUES_IN_TOOLTIP_COMMENT).getBoolean(true);
		SHOW_SATURATION_OVERLAY = config.get(CATEGORY_CLIENT, SHOW_SATURATION_OVERLAY_NAME, true, SHOW_SATURATION_OVERLAY_COMMENT).getBoolean(true);
		SHOW_FOOD_VALUES_OVERLAY = config.get(CATEGORY_CLIENT, SHOW_FOOD_VALUES_OVERLAY_NAME, true, SHOW_FOOD_VALUES_OVERLAY_COMMENT).getBoolean(true);
		SHOW_FOOD_EXHAUSTION_OVERLAY_CONFIG_VAL = config.get(CATEGORY_CLIENT, SHOW_FOOD_EXHAUSTION_OVERLAY_NAME, true, SHOW_FOOD_EXHAUSTION_OVERLAY_COMMENT).getBoolean(true);

		// only use the config value immediately when server-side; the client assumes false until the server syncs the config
		if (FMLCommonHandler.instance().getSide() == Side.SERVER)
			FOOD_MODIFIER_ENABLED = FOOD_MODIFIER_ENABLED_CONFIG_VAL;

		/*
		 * ITEMS
		 */

		ITEM_FOOD_JOURNAL_ID = config.getItem(ITEM_FOOD_JOURNAL_NAME, ITEM_FOOD_JOURNAL_ID_DEFAULT).getInt(ITEM_FOOD_JOURNAL_ID_DEFAULT);

		/*
		 * FOOD GROUPS
		 */
		config.getCategory(CATEGORY_FOODGROUPS).setComment(CATEGORY_FOODGROUPS_COMMENT);

		writeExampleFoodGroup();

		if (USE_FOOD_GROUPS)
			loadFoodGroups();

		save();
	}

	public static void loadFoodGroups()
	{
		List<String> enabledFoodGroups = new ArrayList<String>();

		ConfigCategory categoryFoodGroups = config.getCategory(CATEGORY_FOODGROUPS);

		for (String configKey : categoryFoodGroups.keySet())
		{
			if (configKey.endsWith(".enabled") && config.get(CATEGORY_FOODGROUPS, configKey, false).getBoolean(false))
				enabledFoodGroups.add(configKey.substring(0, configKey.length() - ".enabled".length()));
		}

		for (String foodGroupIdent : enabledFoodGroups)
		{
			String name = config.get(CATEGORY_FOODGROUPS, foodGroupIdent + ".name", foodGroupIdent).getString();
			int priority = config.get(CATEGORY_FOODGROUPS, foodGroupIdent + ".priority", 0).getInt();

			FoodGroup foodGroup = new FoodGroup(foodGroupIdent, name, priority);

			String[] items = config.get(CATEGORY_FOODGROUPS, foodGroupIdent + ".items", new String[]{}).getStringList();
			String[] oredicts = config.get(CATEGORY_FOODGROUPS, foodGroupIdent + ".oredicts", new String[]{}).getStringList();
			//String[] baseItems = config.get(CATEGORY_FOODGROUPS, foodGroupIdent + ".item.recipe.bases", new String[]{}).getStringList();
			//String[] baseOredicts = config.get(CATEGORY_FOODGROUPS, foodGroupIdent + ".oredict.recipe.bases", new String[]{}).getStringList();

			for (String itemString : items)
			{
				addItemToFoodGroup(foodGroup, itemString, false);
			}
			/*
			for (String itemString : baseItems)
			{
				addItemToFoodGroup(foodGroup, itemString, true);
			}
			*/
			for (String oredictString : oredicts)
			{
				foodGroup.addFood(oredictString, false);
			}
			/*
			for (String oredictString : baseOredicts)
			{
				foodGroup.addFood(oredictString, true);
			}
			*/

			FoodGroupRegistry.addFoodGroup(foodGroup);
		}
	}

	public static void addItemToFoodGroup(FoodGroup foodGroup, String itemString, boolean isBaseItem)
	{
		String[] itemStringParts = itemString.split(":");
		if (itemStringParts.length > 1)
		{
			Item item = GameRegistry.findItem(itemStringParts[0], itemStringParts[1]);
			boolean exactMetadata = itemStringParts.length > 2 && itemStringParts[2] != "*";
			int metadata = itemStringParts.length > 2 && exactMetadata ? Integer.parseInt(itemStringParts[2]) : 0;
			foodGroup.addFood(new ItemStack(item, 1, metadata), exactMetadata, isBaseItem);
		}
	}

	public static void writeExampleFoodGroup()
	{
		config.get(CATEGORY_FOODGROUPS, "example.enabled", false);
		config.get(CATEGORY_FOODGROUPS, "example.name", "Example");
		config.get(CATEGORY_FOODGROUPS, "example.priority", 0, "Food can only belong to one food group\nin the case of conflicting food groups, the food group with the highest priority will be selected\nExample: A food group with priority 3 will take precedence over a food group with priority 1");
		config.get(CATEGORY_FOODGROUPS, "example.items", new String[]{"minecraft:apple", "minecraft:golden_apple:0"}, "A list of items in mod:name:meta format\nThis example adds red apples and golden apples (metadata 0), thereby excluding enchanted golden apples (metadata 1)");
		//config.get(CATEGORY_FOODGROUPS, "example.item.recipe.bases", new String[]{"minecraft:apple", "minecraft:golden_apple:0"}, "A list of items in mod:name:meta format\nEach item in this list will also include any item derived from it (meaning any item where the base item is used in some part of its crafting recipe)\nNote: To have an item work as either a direct match or a recipe base, it needs to be in both lists");
		config.get(CATEGORY_FOODGROUPS, "example.oredicts", new String[]{"listAllfruit", "listAllberry"}, "A list of ore dictionary entries\nThis example adds two oredictionary entries created by Pam's HarvestCraft, including all fruit and all berries");
		//config.get(CATEGORY_FOODGROUPS, "example.oredict.recipe.bases", new String[]{"listAllfruit", "listAllberry"}, "A list of ore dictionary entries\nEach entry in this list will also include any item derived from it (meaning any item where the base oredictionary entry is used in some part of its crafting recipe)\nNote: To have an entry work as either a direct match or a recipe base, it needs to be in both lists");
	}

	public static void setRoundingMode()
	{
		for (RoundingMode roundingMode : RoundingMode.values())
		{
			if (roundingMode.id.equals(FOOD_HUNGER_ROUNDING_MODE_STRING.toLowerCase()))
			{
				FOOD_HUNGER_ROUNDING_MODE = roundingMode;
				break;
			}
		}
		if (FOOD_HUNGER_ROUNDING_MODE == null)
		{
			ModSpiceOfLife.Log.warn("Rounding mode '" + FOOD_HUNGER_ROUNDING_MODE_STRING + "' not recognized; defaulting to 'round'");
			FOOD_HUNGER_ROUNDING_MODE_STRING = "round";
			FOOD_HUNGER_ROUNDING_MODE = RoundingMode.ROUND;
		}
	}

	public static void save()
	{
		config.save();
	}

	public static void load()
	{
		config.load();
	}

	@Override
	public void pack(IByteIO data)
	{
		data.writeBoolean(FOOD_MODIFIER_ENABLED_CONFIG_VAL);
		if (FOOD_MODIFIER_ENABLED_CONFIG_VAL)
		{
			data.writeUTF(FOOD_MODIFIER_FORMULA);
			data.writeShort(FOOD_HISTORY_LENGTH);
			data.writeBoolean(FOOD_HISTORY_PERSISTS_THROUGH_DEATH);
			data.writeInt(FOOD_EATEN_THRESHOLD);
			data.writeBoolean(CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD);
			data.writeBoolean(USE_FOOD_GROUPS);
			data.writeBoolean(AFFECT_FOOD_SATURATION_MODIFIERS);
			data.writeBoolean(AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS);
			data.writeBoolean(USE_HUNGER_QUEUE);
			data.writeUTF(FOOD_HUNGER_ROUNDING_MODE_STRING);
		}
	}

	@Override
	public void unpack(IByteIO data)
	{
		FOOD_MODIFIER_ENABLED = data.readBoolean();
		if (FOOD_MODIFIER_ENABLED)
		{
			FOOD_MODIFIER_FORMULA = data.readUTF();
			FOOD_HISTORY_LENGTH = data.readShort();
			FOOD_HISTORY_PERSISTS_THROUGH_DEATH = data.readBoolean();
			FOOD_EATEN_THRESHOLD = data.readInt();
			CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD = data.readBoolean();
			USE_FOOD_GROUPS = data.readBoolean();
			AFFECT_FOOD_SATURATION_MODIFIERS = data.readBoolean();
			AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS = data.readBoolean();
			USE_HUNGER_QUEUE = data.readBoolean();
			FOOD_HUNGER_ROUNDING_MODE_STRING = data.readUTF();
		}
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		if (FOOD_MODIFIER_ENABLED)
		{
			setRoundingMode();
			FoodModifier.onFormulaChanged();
			FoodHistory.get(player).onHistoryTypeChanged();
			FoodGroupRegistry.clear();
		}

		SHOW_FOOD_EXHAUSTION_OVERLAY = SHOW_FOOD_EXHAUSTION_OVERLAY_CONFIG_VAL;
		Item.itemsList[ModContent.foodJournal.itemID] = ModContent.foodJournal;

		return null;
	}

	public static void sync(EntityPlayerMP player)
	{
		PacketDispatcher.get().sendTo(new PacketConfigSync(), player);
	}

	public static void assumeClientOnly()
	{
		// assume false until the server syncs
		FOOD_MODIFIER_ENABLED = false;
		SHOW_FOOD_EXHAUSTION_OVERLAY = false;
		Item.itemsList[ModContent.foodJournal.itemID] = null;
	}
}
