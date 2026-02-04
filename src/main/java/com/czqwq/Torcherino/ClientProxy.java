package com.czqwq.Torcherino;

import com.czqwq.Torcherino.client.render.RenderTimeAccelerator;
import com.czqwq.Torcherino.entity.EntityTimeAccelerator;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void preInit(cpw.mods.fml.common.event.FMLPreInitializationEvent event) {
        super.preInit(event);

        // Register entity renderer
        RenderingRegistry.registerEntityRenderingHandler(EntityTimeAccelerator.class, new RenderTimeAccelerator());
    }
}
