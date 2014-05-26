package squeek.spiceoflife;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import net.minecraftforge.common.Configuration;
import squeek.spiceoflife.foodtracker.FoodModifier;

public class ModConfig
{
	private static final String CATEGORY_MAIN = "Main";

	public static String FOOD_MODIFIER_FORMULA = ModConfig.FOOD_MODIFIER_FORMULA_STRING_DEFAULT;
	private static final String FOOD_MODIFIER_FORMULA_STRING_NAME = "food.modifier.formula";
	private static final String FOOD_MODIFIER_FORMULA_STRING_DEFAULT = "MAX(0, (1 - count/12))^MAX(0, food_hunger_value-ROUND(MAX(0, 1 - count/12), 0))";
	private static final String FOOD_MODIFIER_FORMULA_STRING_COMMENT = 
			"Uses the EvalEx expression parser\n"
			+ "See: https://github.com/uklimaschewski/EvalEx for syntax/function documentation\n\n"
			+ "Available variables:\n"
			+ "\tcount : The number of times the food has been eaten (out of the last max_history_length foods)\n"
			+ "\tmax_history_length : The maximum number of foods that are stored in the history at a time (food.history.length)\n"
			+ "\tcur_history_length : The current number of foods that are stored in the history (<= max_history_length)\n"
			+ "\tfood_hunger_value : The default amount of hunger the food would restore in hunger units (note: 1 hunger unit = 1/2 hunger bar)\n"
			+ "\tfood_saturation_mod : The default saturation modifier of the food\n"
			+ "\tcur_hunger : The current hunger value of the player in hunger units (20 = full)\n"
			+ "\tcur_saturation : The current saturation value of the player\n"
			+ "\ttotal_food_eaten : The all-time total number of times any food has been eaten by the player\n"
			;
	
	public static int FOOD_HISTORY_LENGTH = ModConfig.FOOD_HISTORY_LENGTH_DEFAULT;
	private static final String FOOD_HISTORY_LENGTH_NAME = "food.history.length";
	private static final int FOOD_HISTORY_LENGTH_DEFAULT = 12;
	private static final String FOOD_HISTORY_LENGTH_COMMENT = "The maximum amount of eaten foods stored in the history at a time";

	public static boolean FOOD_HISTORY_PERSISTS_THROUGH_DEATH = ModConfig.FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT;
	private static final String FOOD_HISTORY_PERSISTS_THROUGH_DEATH_NAME = "food.history.persists.through.death";
	private static final boolean FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT = false;
	private static final String FOOD_HISTORY_PERSISTS_THROUGH_DEATH_COMMENT = "If true, food history will not get reset after every death";

	public static int FOOD_EATEN_THRESHOLD = ModConfig.FOOD_EATEN_THRESHOLD_DEFAULT;
	private static final String FOOD_EATEN_THRESHOLD_NAME = "new.player.food.eaten.threshold";
	private static final int FOOD_EATEN_THRESHOLD_DEFAULT = ModConfig.FOOD_HISTORY_LENGTH / 2;
	private static final String FOOD_EATEN_THRESHOLD_COMMENT = "The number of times a new player (by World) needs to eat before this mod has any effect";

	public static boolean CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD = ModConfig.CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT;
	private static final String CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_NAME = "clear.history.after.food.eaten.threshold.reached";
	private static final boolean CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT = false;
	private static final String CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_COMMENT = "If true, a player's food history will be empty once they pass the new.player.food.eaten.treshold\nIf false, any food eaten before the threshold is passed will also count after it is passed";

	private static Configuration config;

	public static void init(File file)
	{
		config = new Configuration(file);

		load();

		FOOD_MODIFIER_FORMULA = config.get(CATEGORY_MAIN, FOOD_MODIFIER_FORMULA_STRING_NAME, FOOD_MODIFIER_FORMULA_STRING_DEFAULT, FOOD_MODIFIER_FORMULA_STRING_COMMENT).getString();
		FOOD_HISTORY_LENGTH = config.get(CATEGORY_MAIN, FOOD_HISTORY_LENGTH_NAME, FOOD_HISTORY_LENGTH_DEFAULT, FOOD_HISTORY_LENGTH_COMMENT).getInt();
		FOOD_HISTORY_PERSISTS_THROUGH_DEATH = config.get(CATEGORY_MAIN, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_NAME, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_COMMENT).getBoolean(FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT);
		FOOD_EATEN_THRESHOLD = config.get(CATEGORY_MAIN, FOOD_EATEN_THRESHOLD_NAME, FOOD_EATEN_THRESHOLD_DEFAULT, FOOD_EATEN_THRESHOLD_COMMENT).getInt();
		CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD = config.get(CATEGORY_MAIN, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_NAME, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT, CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_COMMENT).getBoolean(CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD_DEFAULT);
		
		save();
	}

	public static void save()
	{
		config.save();
	}

	public static void load()
	{
		config.load();
	}

	public static void pack(DataOutputStream data) throws IOException
	{
		data.writeUTF(FOOD_MODIFIER_FORMULA);
		data.writeShort(FOOD_HISTORY_LENGTH);
		data.writeBoolean(FOOD_HISTORY_PERSISTS_THROUGH_DEATH);
		data.writeInt(FOOD_EATEN_THRESHOLD);
		data.writeBoolean(CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD);
	}

	public static void unpack(DataInputStream data) throws IOException
	{
		FOOD_MODIFIER_FORMULA = data.readUTF();
		FOOD_HISTORY_LENGTH = data.readShort();
		FOOD_HISTORY_PERSISTS_THROUGH_DEATH = data.readBoolean();
		FOOD_EATEN_THRESHOLD = data.readInt();
		CLEAR_HISTORY_ON_FOOD_EATEN_THRESHOLD = data.readBoolean();
		
		FoodModifier.onFormulaChanged();
	}
}
