package com.screenscale.mixin;

import com.screenscale.ScreenScale;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public class SodiumGameOptionsMixin
{
    @Shadow(remap = false)
    @Final
    private static MinecraftOptionsStorage vanillaOpts;

    @Redirect(method = "general", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder;build()Lme/jellysquid/mods/sodium/client/gui/options/OptionGroup;", ordinal = 1), remap = false)
    private static OptionGroup on(final OptionGroup.Builder instance)
    {
        instance.add(
          OptionImpl.createBuilder(Integer.TYPE, vanillaOpts)
            .setName(new TextComponent("Menu Scale"))
            .setTooltip(new TextComponent("Set the scale of inventory UIs"))
            .setControl(option -> new SliderControl(option, 0, 4, 1, ControlValueFormatter.guiScale()))
            .setBinding((opts, guiScale) -> {
                ScreenScale.config.getCommonConfig().menuScale = guiScale;
                ScreenScale.config.save();

                Minecraft.getInstance()
                  .getWindow()
                  .setGuiScale(guiScale != 0 ? guiScale : Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()));
                if (Minecraft.getInstance().screen != null)
                {
                    Minecraft.getInstance().screen.resize(Minecraft.getInstance(),
                      Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                      Minecraft.getInstance().getWindow().getGuiScaledHeight());
                }
            }, opts -> ScreenScale.config.getCommonConfig().menuScale)
            .build()
        );
        return instance.build();
    }
}
