package com.armordamagescale.config;

import com.armordamagescale.ArmorDamage;
import com.cupboard.config.ICommonConfig;
import com.ezylang.evalex.Expression;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.util.Map;

public class CommonConfiguration implements ICommonConfig
{
    public static final String FORMULA_ARMOR_ARG      = "armor";
    public static final String FORMULA_TOUGHNESS_ARG  = "toughness";
    public static final String FORMULA_HITPCT_ARG     = "hitpct";
    public static final String FORMULA_DAMAGE_ARG     = "damage";
    public static final String FORMULA_PROTECTION_ARG = "protectionlevel";

    public Expression                       armordamagereduction      = null;
    public String                           armorFormula              = FORMULA_DAMAGE_ARG + "*(15/(" + FORMULA_ARMOR_ARG + "+15))";
    public Expression                       thoughnessdamagereduction = null;
    public String                           toughnessFormula          =
        FORMULA_DAMAGE_ARG + "*(1/(" + FORMULA_TOUGHNESS_ARG + "/10+1)*" + FORMULA_HITPCT_ARG + "+(1-" + FORMULA_HITPCT_ARG + "))";
    public String                           protectionFormula         = FORMULA_DAMAGE_ARG + "*(1.0-(MIN(MAX(" + FORMULA_PROTECTION_ARG + ", 0), 20.0))/25.0)";
    public Expression                       protectionReduction       = null;
    public Expression                       playerdamagereduction     = null;
    public String                           playerdamageFormula       = FORMULA_DAMAGE_ARG + "*(100/(" + FORMULA_DAMAGE_ARG + "+100))";
    public boolean                          debugprint                = false;
    public Object2DoubleOpenHashMap<String> protectionLevels          = new Object2DoubleOpenHashMap();

    public CommonConfiguration()
    {
        protectionLevels.put("minecraft:protection", 1);
        protectionLevels.put("minecraft:fire_protection", 2);
        protectionLevels.put("minecraft:feather_falling", 3);
        protectionLevels.put("minecraft:blast_protection", 2);
        protectionLevels.put("minecraft:projectile_protection", 2);
    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();
        {
            final JsonObject entry = new JsonObject();
            entry.addProperty("desc:",
                "Armor damage reduction formula, calculates the damage after armor, result is the new damage. Default formula, which is adjusted for modpacks:" + armorFormula);
            entry.addProperty(FORMULA_ARMOR_ARG, "Input value, which gets replaced with the entities armor value");
            entry.addProperty(FORMULA_DAMAGE_ARG, "Input value, which gets replaced with the incoming damage value");
            entry.addProperty("armorFormula", armorFormula);
            root.add("armorFormula", entry);
        }

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Armor toughness reduces damage in relation to percent health lost, result is the new damage. Default: " + toughnessFormula);
        entry2.addProperty(FORMULA_TOUGHNESS_ARG, "Input value, which gets replaced with the entities armor toughness value");
        entry2.addProperty(FORMULA_DAMAGE_ARG, "Input value, which gets replaced with the incoming damage value, reduced by armor before");
        entry2.addProperty(FORMULA_HITPCT_ARG, "Input value, which gets replaced with the percentage of entity max health the incoming damage would take(0.0-1.0)");
        entry2.addProperty("toughnessFormula", toughnessFormula);
        root.add("toughnessFormula", entry2);

        final JsonObject entry5 = new JsonObject();
        entry5.addProperty("desc:",
            "Protection enchantment formula, calculates the damage after protection enchantments, result is the new damage. Default formula equals vanilla formula, which results in 4% damage reduction per protectionlevel capped at 80%. Default formula:"
                + protectionFormula);
        entry5.addProperty(FORMULA_PROTECTION_ARG, "Input value, which gets replaced with sum of all protectionlevels, see below");
        entry5.addProperty(FORMULA_DAMAGE_ARG, "Input value, which gets replaced with the incoming damage value");
        entry5.addProperty("protectionFormula", protectionFormula);
        root.add("protectionFormula", entry5);

        final JsonObject entry6 = new JsonObject();
        entry6.addProperty("desc:",
            "Sets the amount of protection level a protection enchantment does give, per enchantment level, whole numbers only. By default these are vanilla levels.");
        final JsonObject protectionLevelsJson = new JsonObject();
        for (final Object2DoubleOpenHashMap.Entry<String> data : protectionLevels.object2DoubleEntrySet())
        {
            protectionLevelsJson.addProperty(data.getKey(), data.getDoubleValue());
        }
        entry6.add("protectionLevels", protectionLevelsJson);
        root.add("protectionLevels", entry6);

        final JsonObject entry4 = new JsonObject();
        entry4.addProperty("desc:",
            "Player damage normalization formula, scales player damage caused to better balance modded weapons and combat, result is the new damage. To disable scaling put just: "
                + FORMULA_DAMAGE_ARG + " Default formula:" + playerdamageFormula);
        entry4.addProperty(FORMULA_DAMAGE_ARG, "Input value, which gets replaced with the incoming damage value");
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

        String armorFormula = data.get("armorFormula").getAsJsonObject().get("armorFormula").getAsString();
        armordamagereduction = new Expression(armorFormula);

        String toughnessFormula = data.get("toughnessFormula").getAsJsonObject().get("toughnessFormula").getAsString();
        thoughnessdamagereduction = new Expression(toughnessFormula);

        if (!armorFormula.contains(FORMULA_DAMAGE_ARG) && !toughnessFormula.contains(FORMULA_DAMAGE_ARG))
        {
            throw new RuntimeException("Outdated config format, resetting config");
        }

        this.armorFormula = armorFormula;
        this.toughnessFormula = toughnessFormula;

        playerdamageFormula = data.get("playerdamageFormula").getAsJsonObject().get("playerdamageFormula").getAsString();
        playerdamagereduction = new Expression(playerdamageFormula);

        debugprint = data.get("debugprint").getAsJsonObject().get("debugprint").getAsBoolean();

        protectionFormula = data.get("protectionFormula").getAsJsonObject().get("protectionFormula").getAsString();
        protectionReduction = new Expression(protectionFormula);

        JsonObject protectLevelsJson = data.get("protectionLevels").getAsJsonObject().get("protectionLevels").getAsJsonObject();
        protectionLevels = new Object2DoubleOpenHashMap<>();
        for (final Map.Entry<String, JsonElement> dataEntry : protectLevelsJson.entrySet())
        {
            protectionLevels.put(dataEntry.getKey(), dataEntry.getValue().getAsDouble());
        }
    }
}
