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

	public static int FOOD_HISTORY_LENGTH = ModConfig.FOOD_HISTORY_LENGTH_DEFAULT;
	private static final String FOOD_HISTORY_LENGTH_NAME = "food.history.length";
	private static final int FOOD_HISTORY_LENGTH_DEFAULT = 12;

	public static boolean FOOD_HISTORY_PERSISTS_THROUGH_DEATH = ModConfig.FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT;
	private static final String FOOD_HISTORY_PERSISTS_THROUGH_DEATH_NAME = "food.history.persists.through.death";
	private static final boolean FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT = true;

	public static int FOOD_EATEN_THRESHOLD = ModConfig.FOOD_EATEN_THRESHOLD_DEFAULT;
	private static final String FOOD_EATEN_THRESHOLD_NAME = "new.player.food.eaten.threshold";
	private static final int FOOD_EATEN_THRESHOLD_DEFAULT = ModConfig.FOOD_HISTORY_LENGTH / 2;
	

	private static Configuration config;

	public static void init(File file)
	{
		config = new Configuration(file);

		load();

		FOOD_MODIFIER_FORMULA = config.get(CATEGORY_MAIN, FOOD_MODIFIER_FORMULA_STRING_NAME, FOOD_MODIFIER_FORMULA_STRING_DEFAULT).getString();
		FOOD_HISTORY_LENGTH = config.get(CATEGORY_MAIN, FOOD_HISTORY_LENGTH_NAME, FOOD_HISTORY_LENGTH_DEFAULT).getInt();
		FOOD_HISTORY_PERSISTS_THROUGH_DEATH = config.get(CATEGORY_MAIN, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_NAME, FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT).getBoolean(FOOD_HISTORY_PERSISTS_THROUGH_DEATH_DEFAULT);
		FOOD_EATEN_THRESHOLD = config.get(CATEGORY_MAIN, FOOD_EATEN_THRESHOLD_NAME, FOOD_EATEN_THRESHOLD_DEFAULT).getInt();

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
	}

	public static void unpack(DataInputStream data) throws IOException
	{
		FOOD_MODIFIER_FORMULA = data.readUTF();
		FOOD_HISTORY_LENGTH = data.readShort();
		FOOD_HISTORY_PERSISTS_THROUGH_DEATH = data.readBoolean();
		FOOD_EATEN_THRESHOLD = data.readInt();
		
		FoodModifier.onFormulaChanged();
	}
}
