package me.medisant.irongolem.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "irongolem")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public static ModConfig instance;

    @ConfigEntry.Gui.Tooltip
    public double psnr = 0.5;
    @ConfigEntry.Gui.Tooltip
    public double mssim = 0.75;
    @ConfigEntry.Gui.Tooltip
    public double histogram_a = 99.99;
    @ConfigEntry.Gui.Tooltip
    public double histogram_b = 99.0;
    @ConfigEntry.Gui.Tooltip
    public double histogram_c = 90.0;
    @ConfigEntry.Gui.Tooltip
    public boolean logMatches = true;
    @ConfigEntry.Gui.Tooltip
    public boolean chatMessages = true;

    public static void init() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        instance = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

}
