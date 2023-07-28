package com.armordamagescale;

import com.armordamagescale.config.CommonConfiguration;
import com.cupboard.config.CupboardConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArmorDamage.MODID)
public class ArmorDamage
{
    public static final String MODID = "armordamagescale";
    public static final Logger LOGGER = LogManager.getLogger();
    public static CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MODID, new CommonConfiguration());
    public static Random rand = new Random();

    public ArmorDamage()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }


    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info(MODID + " mod initialized");
    }
}
