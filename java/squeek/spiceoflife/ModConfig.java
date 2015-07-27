package squeek.spiceoflife;

import java.io.File;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import squeek.spiceoflife.compat.IByteIO;
import squeek.spiceoflife.compat.PacketDispatcher;
import squeek.spiceoflife.foodtracker.FoodHistory;
import squeek.spiceoflife.foodtracker.FoodModifier;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupConfig;
import squeek.spiceoflife.foodtracker.foodgroups.FoodGroupRegistry;
import squeek.spiceoflife.interfaces.IPackable;
import squeek.spiceoflife.interfaces.IPacketProcessor;
import squeek.spiceoflife.network.PacketBase;
import squeek.spiceoflife.network.PacketConfigSync;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	private static final String FOOD_MODIFIER_ENABLED_COMMENT = "If false, disables the entire diminishing returns part of the mod";

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

	public static boolean USE_FOOD_GROUPS_AS_WHITELISTS = ModConfig.USE_FOOD_GROUPS_AS_WHITELISTS_DEFAULT;
	private static final String USE_FOOD_GROUPS_AS_WHITELISTS_NAME = "use.food.groups.as.whitelists";
	private static final boolean USE_FOOD_GROUPS_AS_WHITELISTS_DEFAULT = false;
	private static final String USE_FOOD_GROUPS_AS_WHITELISTS_COMMENT =
			"If true, any foods not in a food group will be excluded from diminishing returns";

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

	public static boolean AFFECT_FOOD_HUNGER_VALUES = ModConfig.AFFECT_FOOD_HUNGER_VALUES_DEFAULT;
	private static final String AFFECT_FOOD_HUNGER_VALUES_NAME = "affect.food.hunger.values";
	private static final boolean AFFECT_FOOD_HUNGER_VALUES_DEFAULT = true;
	private static final String AFFECT_FOOD_HUNGER_VALUES_COMMENT =
			"If true, foods' hunger value will be multiplied by the current nutritional value\n"
					+ "Setting this to false and " + ModConfig.AFFECT_FOOD_SATURATION_MODIFIERS_NAME + " to true will make diminishing returns affect saturation only";

	public static boolean AFFECT_NEGATIVE_FOOD_HUNGER_VALUES = ModConfig.AFFECT_NEGATIVE_FOOD_HUNGER_VALUES_DEFAULT;
	private static final String AFFECT_NEGATIVE_FOOD_HUNGER_VALUES_NAME = "affect.negative.food.hunger.values";
	private static final boolean AFFECT_NEGATIVE_FOOD_HUNGER_VALUES_DEFAULT = false;
	private static final String AFFECT_NEGATIVE_FOOD_HUNGER_VALUES_COMMENT =
			"If true, foods with negative hunger values will be made more negative as nutritional value decreases\n"
					+ "NOTE: " + AFFECT_FOOD_HUNGER_VALUES_NAME + " must be true for this to have any affect";

	public static boolean AFFECT_FOOD_SATURATION_MODIFIERS = ModConfig.AFFECT_FOOD_SATURATION_MODIFIERS_DEFAULT;
	private static final String AFFECT_FOOD_SATURATION_MODIFIERS_NAME = "affect.food.saturation.modifiers";
	private static final boolean AFFECT_FOOD_SATURATION_MODIFIERS_DEFAULT = false;
	private static final String AFFECT_FOOD_SATURATION_MODIFIERS_COMMENT =
			"If true, foods' saturation modifier will be multiplied by the current nutritional value\n"
					+ "NOTE: When " + ModConfig.AFFECT_FOOD_HUNGER_VALUES_NAME + " is true, saturation bonuses of foods will automatically decrease as the hunger value of the food decreases\n"
					+ "Setting this to true when " + ModConfig.AFFECT_FOOD_HUNGER_VALUES_NAME + " is true will make saturation bonuses decrease disproportionately more than hunger values\n"
					+ "Setting this to true and " + ModConfig.AFFECT_FOOD_SATURATION_MODIFIERS_NAME + " to false will make diminishing returns affect saturation only";

	public static boolean AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS = ModConfig.AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_DEFAULT;
	private static final String AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_NAME = "affect.negative.food.saturation.modifiers";
	private static final boolean AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_DEFAULT = false;
	private static final String AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_COMMENT =
			"If true, foods with negative saturation modifiers will be made more negative as nutritional value decreases\n"
					+ "NOTE: " + AFFECT_FOOD_SATURATION_MODIFIERS_NAME + " must be true for this to have any affect";

	public static float FOOD_EATING_SPEED_MODIFIER = ModConfig.FOOD_EATING_SPEED_MODIFIER_DEFAULT;
	private static final String FOOD_EATING_SPEED_MODIFIER_NAME = "food.eating.speed.modifier";
	private static final float FOOD_EATING_SPEED_MODIFIER_DEFAULT = 1;
	private static final String FOOD_EATING_SPEED_MODIFIER_COMMENT =
			"If set to greater than zero, food eating speed will be affected by nutritional value\n"
					+ "(meaning the lower the nutrtional value, the longer it will take to eat it)\n"
					+ "Eating duration is calcualted using the formula (eating_duration / (nutritional_value^eating_speed_modifier))";

	public static int FOOD_EATING_DURATION_MAX = ModConfig.FOOD_EATING_DURATION_MAX_DEFAULT;
	private static final String FOOD_EATING_DURATION_MAX_NAME = "food.eating.duration.max";
	private static final int FOOD_EATING_DURATION_MAX_DEFAULT = 0;
	private static final String FOOD_EATING_DURATION_MAX_COMMENT =
			"The maximum time it takes to eat a food after being modified by " + ModConfig.FOOD_EATING_SPEED_MODIFIER_NAME + "\n"
					+ "The default eating duration is 32. Set this to 0 to remove the limit on eating speed.\n"
					+ "Note: If this is set to 0 and " + ModConfig.FOOD_EATING_SPEED_MODIFIER_NAME + " is > 0, a food with 0% nutrtional value will take nearly infinite time to eat";

	public static boolean USE_HUNGER_QUEUE = ModConfig.USE_HUNGER_QUEUE_DEFAULT;
	private static final String USE_HUNGER_QUEUE_NAME = "use.hunger.restored.for.food.history.length";
	private static final boolean USE_HUNGER_QUEUE_DEFAULT = false;
	private static final String USE_HUNGER_QUEUE_COMMENT =
			"If true, " + FOOD_HISTORY_LENGTH_NAME + " will use amount of hunger restored instead of number of foods eaten for its maximum length\n"
					+ "For example, a " + FOOD_HISTORY_LENGTH_NAME + " length of 12 will store a max of 2 foods that restored 6 hunger each, \n"
					+ "3 foods that restored 4 hunger each, 12 foods that restored 1 hunger each, etc\n"
					+ "NOTE: " + FOOD_HISTORY_LENGTH_NAME + " uses hunger units, where 1 hunger unit = 1/2 hunger bar";

	public static boolean USE_TIME_QUEUE = ModConfig.USE_TIME_QUEUE_DEFAULT;
	private static final String USE_TIME_QUEUE_NAME = "use.time.for.food.history.length";
	private static final boolean USE_TIME_QUEUE_DEFAULT = false;
	private static final String USE_TIME_QUEUE_COMMENT =
			"If true, " + FOOD_HISTORY_LENGTH_NAME + " will use time (in Minecraft days) instead of number of foods eaten for its maximum length\n"
					+ "For example, a " + FOOD_HISTORY_LENGTH_NAME + " length of 12 will store all foods eaten in the last 12 Minecraft days.\n"
					+ "Note: On servers, time only advances for each player while they are logged in unless " + ModConfig.PROGRESS_TIME_WHILE_LOGGED_OFF_NAME + " is set to true";

	public static boolean PROGRESS_TIME_WHILE_LOGGED_OFF = ModConfig.PROGRESS_TIME_WHILE_LOGGED_OFF_DEFAULT;
	private static final String PROGRESS_TIME_WHILE_LOGGED_OFF_NAME = "use.time.progress.time.while.logged.off";
	private static final boolean PROGRESS_TIME_WHILE_LOGGED_OFF_DEFAULT = false;
	private static final String PROGRESS_TIME_WHILE_LOGGED_OFF_COMMENT =
			"If true, food history time will still progress for each player while that player is logged out.\n"
					+ "NOTE: " + USE_TIME_QUEUE_NAME + " must be true for this to have any affect";

	public static String FOOD_MODIFIER_FORMULA = ModConfig.FOOD_MODIFIER_FORMULA_STRING_DEFAULT;
	private static final String FOOD_MODIFIER_FORMULA_STRING_NAME = "food.modifier.formula";
	private static final String FOOD_MODIFIER_FORMULA_STRING_DEFAULT = "MAX(0, (1 - count/12))^MIN(8, food_hunger_value)";
	private static final String FOOD_MODIFIER_FORMULA_STRING_COMMENT =
			"Uses the EvalEx expression parser\n"
					+ "See: https://github.com/uklimaschewski/EvalEx for syntax/function documentation\n\n"
					+ "Available variables:\n"
					+ "\tcount : The number of times the food (or its food group) has been eaten within the food history\n"
					+ "\thunger_count : The total amount of hunger that the food (or its food group) has restored within the food history (1 hunger unit = 1/2 hunger bar)\n"
					+ "\tsaturation_count : The total amount of saturation that the food (or its food group) has restored within the food history (1 saturation unit = 1/2 saturation bar)\n"
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

	public static float FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD = ModConfig.FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD_DEFAULT;
	private static final String FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD_NAME = "food.containers.chance.to.drop.food";
	private static final float FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD_DEFAULT = 0.25f;
	private static final String FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD_COMMENT =
			"The chance for food to drop from an open food container when the player jumps\n"
					+ "Temporarily disabled while a better implementation is written (this config option will do nothing)";

	public static int FOOD_CONTAINERS_MAX_STACKSIZE = ModConfig.FOOD_CONTAINERS_MAX_STACKSIZE_DEFAULT;
	private static final String FOOD_CONTAINERS_MAX_STACKSIZE_NAME = "food.containers.max.stacksize";
	private static final int FOOD_CONTAINERS_MAX_STACKSIZE_DEFAULT = 2;
	private static final String FOOD_CONTAINERS_MAX_STACKSIZE_COMMENT =
			"The maximum stacksize per slot in a food container";

	/*
	 * ITEMS
	 */

	public static final String ITEM_FOOD_JOURNAL_NAME = "bookfoodjournal";

	public static int ITEM_LUNCH_BOX_ID = ModConfig.ITEM_LUNCH_BOX_ID_DEFAULT;
	public static final String ITEM_LUNCH_BOX_NAME = "lunchbox";
	public static final int ITEM_LUNCH_BOX_ID_DEFAULT = 6851;

	public static int ITEM_LUNCH_BAG_ID = ModConfig.ITEM_LUNCH_BAG_ID_DEFAULT;
	public static final String ITEM_LUNCH_BAG_NAME = "lunchbag";
	public static final int ITEM_LUNCH_BAG_ID_DEFAULT = 6852;

	/*
	 * FOOD GROUPS
	 */
	@Deprecated
	private static final String CATEGORY_FOODGROUPS = "foodgroups";
	private static final String CATEGORY_FOODGROUPS_COMMENT =
			"Food groups are defined using .json files in /config/SpiceOfLife/\n"
					+ "See /config/SpiceOfLife/example-food-group.json";

	/*
	 * OBSOLETED
	 */
	@Deprecated
	private static final String CATEGORY_CLIENT = "client";

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

		FOOD_HISTORY_LENGTH = config.get(CATEGORY_SERVER, FOOD_HISTORY_LENGTH_NAME, FOOD_HISTORY_LENGTH_DEFAULT, FOOD_HISTORY_LENGTH_COMMENT).getInt(FOOD_HISTORY_LENGTH_DEFAULT);
		FOOD_HISTORY_PERSISTS_THROUGH_DEATH = config.get(CATEGORY_SERVER, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_NAME, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_COMMENT).getBoolean(FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT);
		FOOD_EATEN_THRESHOLD = config.get(CATEGORY_SERVER, FOOD_EATEN_THRESHOLD_NAME, FOOD_EATEN_THRESHOLD_DEFAULT, FOOD_EATEN_THRESHOLD_COMMENT).getInt(FOOD_EATEN_THRESHOLD_DEFAULT);
		CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD = config.get(CATEGORY_SERVER, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_NAME, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_COMMENT).getBoolean(CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT);
		USE_FOOD_GROUPS_AS_WHITELISTS = config.get(CATEGORY_SERVER, USE_FOOD_GROUPS_AS_WHITELISTS_NAME, USE_FOOD_GROUPS_AS_WHITELISTS_DEFAULT, USE_FOOD_GROUPS_AS_WHITELISTS_COMMENT).getBoolean(USE_FOOD_GROUPS_AS_WHITELISTS_DEFAULT);
		AFFECT_FOOD_HUNGER_VALUES = config.get(CATEGORY_SERVER, AFFECT_FOOD_HUNGER_VALUES_NAME, AFFECT_FOOD_HUNGER_VALUES_DEFAULT, AFFECT_FOOD_HUNGER_VALUES_COMMENT).getBoolean(AFFECT_FOOD_HUNGER_VALUES_DEFAULT);
		AFFECT_NEGATIVE_FOOD_HUNGER_VALUES = config.get(CATEGORY_SERVER, AFFECT_NEGATIVE_FOOD_HUNGER_VALUES_NAME, AFFECT_NEGATIVE_FOOD_HUNGER_VALUES_DEFAULT, AFFECT_NEGATIVE_FOOD_HUNGER_VALUES_COMMENT).getBoolean(AFFECT_NEGATIVE_FOOD_HUNGER_VALUES_DEFAULT);
		AFFECT_FOOD_SATURATION_MODIFIERS = config.get(CATEGORY_SERVER, AFFECT_FOOD_SATURATION_MODIFIERS_NAME, AFFECT_FOOD_SATURATION_MODIFIERS_DEFAULT, AFFECT_FOOD_SATURATION_MODIFIERS_COMMENT).getBoolean(AFFECT_FOOD_SATURATION_MODIFIERS_DEFAULT);
		AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS = config.get(CATEGORY_SERVER, AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_NAME, AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_DEFAULT, AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_COMMENT).getBoolean(AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS_DEFAULT);
		FOOD_EATING_SPEED_MODIFIER = (float) config.get(CATEGORY_SERVER, FOOD_EATING_SPEED_MODIFIER_NAME, FOOD_EATING_SPEED_MODIFIER_DEFAULT, FOOD_EATING_SPEED_MODIFIER_COMMENT).getDouble(FOOD_EATING_SPEED_MODIFIER_DEFAULT);
		FOOD_EATING_DURATION_MAX = config.get(CATEGORY_SERVER, FOOD_EATING_DURATION_MAX_NAME, FOOD_EATING_DURATION_MAX_DEFAULT, FOOD_EATING_DURATION_MAX_COMMENT).getInt(FOOD_EATING_DURATION_MAX_DEFAULT);
		USE_HUNGER_QUEUE = config.get(CATEGORY_SERVER, USE_HUNGER_QUEUE_NAME, USE_HUNGER_QUEUE_DEFAULT, USE_HUNGER_QUEUE_COMMENT).getBoolean(USE_HUNGER_QUEUE_DEFAULT);
		USE_TIME_QUEUE = config.get(CATEGORY_SERVER, USE_TIME_QUEUE_NAME, USE_TIME_QUEUE_DEFAULT, USE_TIME_QUEUE_COMMENT).getBoolean(USE_TIME_QUEUE_DEFAULT);
		PROGRESS_TIME_WHILE_LOGGED_OFF = config.get(CATEGORY_SERVER, PROGRESS_TIME_WHILE_LOGGED_OFF_NAME, PROGRESS_TIME_WHILE_LOGGED_OFF_DEFAULT, PROGRESS_TIME_WHILE_LOGGED_OFF_COMMENT).getBoolean(PROGRESS_TIME_WHILE_LOGGED_OFF_DEFAULT);
		GIVE_FOOD_JOURNAL_ON_START = config.get(CATEGORY_SERVER, GIVE_FOOD_JOURNAL_ON_START_NAME, GIVE_FOOD_JOURNAL_ON_START_DEFAULT, GIVE_FOOD_JOURNAL_ON_START_COMMENT).getBoolean(GIVE_FOOD_JOURNAL_ON_START_DEFAULT);
		GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS = config.get(CATEGORY_SERVER, GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_NAME, GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_DEFAULT, GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_COMMENT).getBoolean(GIVE_FOOD_JOURNAL_ON_DIMINISHING_RETURNS_DEFAULT);
		FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD = (float) config.get(CATEGORY_SERVER, FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD_NAME, FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD_DEFAULT, FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD_COMMENT).getDouble(FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD_DEFAULT);
		FOOD_CONTAINERS_MAX_STACKSIZE = config.get(CATEGORY_SERVER, FOOD_CONTAINERS_MAX_STACKSIZE_NAME, FOOD_CONTAINERS_MAX_STACKSIZE_DEFAULT, FOOD_CONTAINERS_MAX_STACKSIZE_COMMENT).getInt(FOOD_CONTAINERS_MAX_STACKSIZE_DEFAULT);

		FOOD_HUNGER_ROUNDING_MODE_STRING = config.get(CATEGORY_SERVER, FOOD_HUNGER_ROUNDING_MODE_NAME, FOOD_HUNGER_ROUNDING_MODE_DEFAULT, FOOD_HUNGER_ROUNDING_MODE_COMMENT).getString();
		setRoundingMode();

		/*
		 * FOOD GROUPS
		 */
		config.getCategory(CATEGORY_FOODGROUPS).setComment(CATEGORY_FOODGROUPS_COMMENT);
		FoodGroupConfig.setup(file.getParentFile());

		// remove obsolete config options
		config.getCategory(CATEGORY_SERVER).remove("use.food.groups");
		config.getCategory(CATEGORY_FOODGROUPS).clear();
		config.removeCategory(config.getCategory(CATEGORY_CLIENT));

		// temporarily disable chance to drop food, needs a better implementation
		FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD = 0;

		save();
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
			data.writeBoolean(USE_FOOD_GROUPS_AS_WHITELISTS);
			data.writeBoolean(AFFECT_FOOD_SATURATION_MODIFIERS);
			data.writeBoolean(AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS);
			data.writeFloat(FOOD_EATING_SPEED_MODIFIER);
			data.writeInt(FOOD_EATING_DURATION_MAX);
			data.writeBoolean(USE_HUNGER_QUEUE);
			data.writeBoolean(USE_TIME_QUEUE);
			data.writeBoolean(PROGRESS_TIME_WHILE_LOGGED_OFF);
			data.writeUTF(FOOD_HUNGER_ROUNDING_MODE_STRING);
		}
		data.writeInt(FOOD_CONTAINERS_MAX_STACKSIZE);
		data.writeFloat(FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD);
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
			USE_FOOD_GROUPS_AS_WHITELISTS = data.readBoolean();
			AFFECT_FOOD_SATURATION_MODIFIERS = data.readBoolean();
			AFFECT_NEGATIVE_FOOD_SATURATION_MODIFIERS = data.readBoolean();
			FOOD_EATING_SPEED_MODIFIER = data.readFloat();
			FOOD_EATING_DURATION_MAX = data.readInt();
			USE_HUNGER_QUEUE = data.readBoolean();
			USE_TIME_QUEUE = data.readBoolean();
			PROGRESS_TIME_WHILE_LOGGED_OFF = data.readBoolean();
			FOOD_HUNGER_ROUNDING_MODE_STRING = data.readUTF();
		}
		FOOD_CONTAINERS_MAX_STACKSIZE = data.readInt();
		FOOD_CONTAINERS_CHANCE_TO_DROP_FOOD = data.readFloat();
	}

	@Override
	public PacketBase processAndReply(Side side, EntityPlayer player)
	{
		if (FOOD_MODIFIER_ENABLED)
		{
			setRoundingMode();
			FoodModifier.onGlobalFormulaChanged();
			FoodHistory.get(player).onHistoryTypeChanged();
			FoodGroupRegistry.clear();
		}


		return null;
	}

	public static void sync(EntityPlayerMP player)
	{
		PacketDispatcher.get().sendTo(new PacketConfigSync(), player);
	}

	@SideOnly(Side.CLIENT)
	public static void assumeClientOnly()
	{
		// assume false until the server syncs
		FOOD_MODIFIER_ENABLED = false;
	}
}
