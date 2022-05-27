package com.screenscale.mixin;

import com.screenscale.ScreenScale;
import net.minecraft.client.Option;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.screenscale.event.ClientEventHandler.MENU_SCALE;

@Mixin(VideoSettingsScreen.class)
public class VideoSettingsScreenMixin
{
    @Shadow
    @Final
    @Mutable
    private static Option[] OPTIONS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addSettings(final CallbackInfo ci)
    {
        try
        {
            final List<Option> options = new ArrayList<>(Arrays.asList(OPTIONS));
            options.add(options.indexOf(Option.GUI_SCALE) + 1, MENU_SCALE);
            OPTIONS = options.toArray(new Option[0]);
        }
        catch (Throwable e)
        {
            ScreenScale.LOGGER.error("Error trying to add an option Button to video settings, likely optifine is present which removes vanilla functionality required."
                                       + " The mod still works, but you'll need to manually adjust the config to get different UI scalings as the button could not be added.");
        }
    }
}
