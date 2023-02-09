package com.armordamagescale.config;

import com.armordamagescale.ArmorDamage;
import com.ezylang.evalex.Expression;
import com.google.gson.JsonObject;

public class CommonConfiguration
{
    public static final String FORMULA_ARMOR_ARG = "armor";
    public static final String FORMULA_TOUGHNESS_ARG = "toughness";
    public static final String FORMULA_HITPCT_ARG = "hitpct";
    public static final String FORMULA_DAMAGE_ARG = "damage";

    public Expression armordamagereduction = null;
    public String armorFormula = "15/(" + FORMULA_ARMOR_ARG + "+15)";
    public Expression thoughnessdamagereduction = null;
    public String toughnessFormula = "1/(" + FORMULA_TOUGHNESS_ARG + "/10+1)*" + FORMULA_HITPCT_ARG + "+(1-" + FORMULA_HITPCT_ARG + ")";
    public Expression playerdamagereduction = null;
    public String playerdamageFormula = FORMULA_DAMAGE_ARG + "*(100/(" + FORMULA_DAMAGE_ARG + "+100))";
    public boolean debugprint = false;

    public CommonConfiguration()
    {

    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Armor damage reduction formula, damage is multiplied by the value returned.Input value: " + FORMULA_ARMOR_ARG + " Default:15/(armor+15)");
        entry.addProperty("armorFormula", armorFormula);
        root.add("armorFormula", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Armor toughness now reduces damage in relation to percent health lost." +
                " Armor toughness damage reduction formula, damage is multiplied by the value returned. " +
                "Input values:" + FORMULA_HITPCT_ARG + "(0-1), " + FORMULA_TOUGHNESS_ARG + ". Default: 1/(toughness/10+1)*hitpct+(1-hitpct)");
        entry2.addProperty("toughnessFormula", toughnessFormula);
        root.add("toughnessFormula", entry2);

        final JsonObject entry4 = new JsonObject();
        entry4.addProperty("desc:", "Player damage normalization, reduces too high player damage. Input values:" + FORMULA_DAMAGE_ARG + " . To disable put just: " + FORMULA_DAMAGE_ARG);
        entry4.addProperty("playerdamageFormula", playerdamageFormula);
        root.add("playerdamageFormula", entry4);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty("desc:", "Set to true to enable log debug output, default: false.");
        entry3.addProperty("debugprint", debugprint);
        root.add("debugprint", entry3);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        if (data == null)
        {
            ArmorDamage.LOGGER.error("Config file was empty!");
            return;
        }

        armorFormula = data.get("armorFormula").getAsJsonObject().get("armorFormula").getAsString();
        armordamagereduction = new Expression(armorFormula);

        toughnessFormula = data.get("toughnessFormula").getAsJsonObject().get("toughnessFormula").getAsString();
        thoughnessdamagereduction = new Expression(toughnessFormula);

        playerdamageFormula = data.get("playerdamageFormula").getAsJsonObject().get("playerdamageFormula").getAsString();
        playerdamagereduction = new Expression(playerdamageFormula);

        debugprint = data.get("debugprint").getAsJsonObject().get("debugprint").getAsBoolean();
    }
}
