package com.screenscale;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
public class ScreenScale implements ModInitializer
{
    public static final String                               MODID  = "screenscale";
    public static final Logger                               LOGGER = LogManager.getLogger();
    public static       com.ScreenScale.config.Configuration config = new com.ScreenScale.config.Configuration();
    public static       Random                               rand   = new Random();

    public ScreenScale()
    {
    }

    @Override
    public void onInitialize()
    {
        config.load();
        LOGGER.info(MODID + " mod initialized");
    }
}
