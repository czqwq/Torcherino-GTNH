package com.czqwq.Torcherino;

import net.minecraftforge.common.MinecraftForge;

import com.czqwq.Torcherino.client.render.RenderTimeAccelerator;
import com.czqwq.Torcherino.client.render.WirelessBeamRenderer;
import com.czqwq.Torcherino.entity.EntityTimeAccelerator;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(cpw.mods.fml.common.event.FMLPreInitializationEvent event) {
        super.preInit(event);

        // Register entity renderer
        RenderingRegistry.registerEntityRenderingHandler(EntityTimeAccelerator.class, new RenderTimeAccelerator());

        // Register wireless torcherino beam renderer
        MinecraftForge.EVENT_BUS.register(new WirelessBeamRenderer());
    }
}
