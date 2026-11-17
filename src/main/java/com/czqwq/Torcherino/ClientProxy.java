package com.czqwq.Torcherino;

import net.minecraftforge.common.MinecraftForge;

import com.czqwq.Torcherino.client.render.RenderTimeAccelerator;
import com.czqwq.Torcherino.client.render.RenderTorcherinoRange;
import com.czqwq.Torcherino.client.render.WirelessBeamRenderer;
import com.czqwq.Torcherino.entity.EntityTimeAccelerator;
import com.czqwq.Torcherino.tile.TileTorcherinoBase;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(cpw.mods.fml.common.event.FMLPreInitializationEvent event) {
        super.preInit(event);

        // Register entity renderer
        RenderingRegistry.registerEntityRenderingHandler(EntityTimeAccelerator.class, new RenderTimeAccelerator());

        // Register the working-area preview renderer for the GUI-operated torches
        ClientRegistry.bindTileEntitySpecialRenderer(TileTorcherinoBase.class, new RenderTorcherinoRange());

        // Register wireless torcherino beam renderer
        MinecraftForge.EVENT_BUS.register(new WirelessBeamRenderer());
    }
}
