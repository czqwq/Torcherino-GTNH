package com.czqwq.Torcherino;

import com.czqwq.Torcherino.block.ModBlocks;
import com.czqwq.Torcherino.init.ModRecipes;
import com.czqwq.Torcherino.item.ModItems;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        ModBlocks.init();
        ModItems.init();

        Torcherino.LOG.info("Ciallo～(∠・ω< )⌒★");
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        ModRecipes.init();
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        // Register WAILA providers for wireless torcherino
        if (Config.enableFlashTorcherino) {
            FMLInterModComms.sendMessage(
                "Waila",
                "register",
                "com.czqwq.Torcherino.waila.WirelessTorcherinoWailaProvider.callbackRegister");
        }
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
