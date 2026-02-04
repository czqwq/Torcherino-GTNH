package com.czqwq.Torcherino;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Hello World";
    
    // Acceleration settings
    public static boolean enableAccelerateGregTechMachine = true;
    public static float accelerateGregTechMachineDiscount = 0.8F;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");
        
        enableAccelerateGregTechMachine = configuration.getBoolean(
            "enableAccelerateGregTechMachine",
            "Acceleration",
            enableAccelerateGregTechMachine,
            "Enable advanced acceleration for GregTech machines (requires mixins)");
        
        accelerateGregTechMachineDiscount = configuration.getFloat(
            "accelerateGregTechMachineDiscount",
            "Acceleration",
            accelerateGregTechMachineDiscount,
            0.0F,
            1.0F,
            "Discount factor for GregTech machine acceleration (0.0 to 1.0)");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
