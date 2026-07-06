package com.czqwq.Torcherino.waila;

import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;

import mcp.mobius.waila.api.IWailaRegistrar;

/**
 * WAILA registration callback for wireless torcherino.
 * Registered via FMLInterModComms in CommonProxy.
 * <p>
 * Follows AE2's {@code Waila} module pattern: a standalone
 * {@link WirelessTorcherinoWailaDataProvider} is registered directly
 * for {@link TileWirelessTorcherinoBase}.
 */
public class WirelessTorcherinoWailaProvider {

    public static void callbackRegister(IWailaRegistrar registrar) {
        final WirelessTorcherinoWailaDataProvider provider = new WirelessTorcherinoWailaDataProvider();

        registrar.registerBodyProvider(provider, TileWirelessTorcherinoBase.class);
        registrar.registerNBTProvider(provider, TileWirelessTorcherinoBase.class);
    }
}
