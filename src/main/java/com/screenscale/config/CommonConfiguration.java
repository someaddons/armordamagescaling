package com.screenscale.config;

import com.google.gson.JsonObject;
import com.screenscale.ScreenScale;

public class CommonConfiguration
{
    public int menuScale = 0;

    public CommonConfiguration()
    {

    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "UI scale of menus, default = 0(Auto)");
        entry.addProperty("menuScale", menuScale);
        root.add("menuScale", entry);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        if (data == null)
        {
            ScreenScale.LOGGER.error("Config file was empty!");
            return;
        }

        try
        {
            menuScale = data.get("menuScale").getAsJsonObject().get("menuScale").getAsInt();
        }
        catch (Exception e)
        {
            ScreenScale.LOGGER.error("Could not parse config file", e);
        }
    }
}
