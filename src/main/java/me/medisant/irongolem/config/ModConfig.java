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
    public double psnr = 3.0;
    public double mssim = 0.75;
    public double histogram_1 = 97.5;
    public double histogram_2 = 90.0;
    public double histogram_3 = 75.0;
    public boolean printResult = false;

    public static void init() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        instance = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

}
