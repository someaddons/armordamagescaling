package com.armordamagescale;

import com.armordamagescale.config.Configuration;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
public class ArmorDamage implements ModInitializer {
    public static final String MODID = "armordamagescale";
    public static final Logger LOGGER = LogManager.getLogger();
    public static Configuration config = new Configuration();
    public static Random rand = new Random();

    public ArmorDamage() {
    }

    @Override
    public void onInitialize() {
        config.load();
        LOGGER.info(MODID + " mod initialized");
    }
}
