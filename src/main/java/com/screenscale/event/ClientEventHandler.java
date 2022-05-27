package com.screenscale.event;

import com.screenscale.ScreenScale;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClientEventHandler
{
    public static final CycleOption MENU_SCALE = CycleOption.create("Menu Scale", () -> {
        return IntStream.rangeClosed(0, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode())).boxed().collect(Collectors.toList());
    }, (integer) -> {

        if (integer == 0)
        {
            return new TextComponent("Auto");
        }

        return new TextComponent("" + integer);
    }, (options) -> {
        return ScreenScale.config.getCommonConfig().menuScale;
    }, (options, option, value) -> {
        int guiScale = ScreenScale.config.getCommonConfig().menuScale;
        guiScale = value;
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
    });

    static int oldScale = -1;

    public static void onScreenSet(Screen screen)
    {
        if (Minecraft.getInstance().screen == null && screen != null)
        {
            Minecraft.getInstance()
              .getWindow()
              .setGuiScale(ScreenScale.config.getCommonConfig().menuScale != 0
                             ? ScreenScale.config.getCommonConfig().menuScale
                             : Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()));
            if (oldScale == -1)
            {
                oldScale = Minecraft.getInstance().options.guiScale;
                Minecraft.getInstance().options.guiScale = ScreenScale.config.getCommonConfig().menuScale;
            }
        }

        if (screen == null)
        {
            if (oldScale != -1)
            {
                if (Minecraft.getInstance().options.guiScale == ScreenScale.config.getCommonConfig().menuScale)
                {
                    Minecraft.getInstance().options.guiScale = oldScale;
                }
                oldScale = -1;
            }
            Minecraft.getInstance().resizeDisplay();
        }
    }
}