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

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        ModBlocks.init();
        ModItems.init();

        Torcherino.LOG.info("Ciallo～(∠・ω< )⌒★");
    }

    public void init(FMLInitializationEvent event) {
        ModRecipes.init();

        // Must register WAILA provider during init — IMC event fires before postInit.
        // Sending in postInit would miss the IMCEvent dispatch window.
        if (Config.enableFlashTorcherino) {
            FMLInterModComms.sendMessage(
                "Waila",
                "register",
                "com.czqwq.Torcherino.waila.WirelessTorcherinoWailaProvider.callbackRegister");
        }
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {}
}
