package com.czqwq.Torcherino.waila;

import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

/**
 * WAILA registration callback for wireless torcherino.
 * Registered via FMLInterModComms in CommonProxy.
 */
public class WirelessTorcherinoWailaProvider {

    public static void callbackRegister(IWailaRegistrar registrar) {
        // The tile itself implements IWailaDataProvider
        // We register it as both body and NBT provider for all wireless torch tile classes
        final IWailaDataProvider provider = new WailaProxyProvider();

        registrar.registerBodyProvider(provider, TileWirelessTorcherinoBase.class);
        registrar.registerNBTProvider(provider, TileWirelessTorcherinoBase.class);
    }

    /**
     * Proxy that delegates to the tile entity's own IWailaDataProvider implementation.
     * Since TileWirelessTorcherinoBase implements IWailaDataProvider, we just cast.
     */
    private static class WailaProxyProvider implements IWailaDataProvider {

        @Override
        public net.minecraft.item.ItemStack getWailaStack(mcp.mobius.waila.api.IWailaDataAccessor accessor,
            mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (accessor.getTileEntity() instanceof TileWirelessTorcherinoBase) {
                return ((TileWirelessTorcherinoBase) accessor.getTileEntity()).getWailaStack(accessor, config);
            }
            return null;
        }

        @Override
        public java.util.List<String> getWailaHead(net.minecraft.item.ItemStack itemStack,
            java.util.List<String> currenttip, mcp.mobius.waila.api.IWailaDataAccessor accessor,
            mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (accessor.getTileEntity() instanceof TileWirelessTorcherinoBase) {
                return ((TileWirelessTorcherinoBase) accessor.getTileEntity())
                    .getWailaHead(itemStack, currenttip, accessor, config);
            }
            return currenttip;
        }

        @Override
        public java.util.List<String> getWailaBody(net.minecraft.item.ItemStack itemStack,
            java.util.List<String> currenttip, mcp.mobius.waila.api.IWailaDataAccessor accessor,
            mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (accessor.getTileEntity() instanceof TileWirelessTorcherinoBase) {
                return ((TileWirelessTorcherinoBase) accessor.getTileEntity())
                    .getWailaBody(itemStack, currenttip, accessor, config);
            }
            return currenttip;
        }

        @Override
        public java.util.List<String> getWailaTail(net.minecraft.item.ItemStack itemStack,
            java.util.List<String> currenttip, mcp.mobius.waila.api.IWailaDataAccessor accessor,
            mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (accessor.getTileEntity() instanceof TileWirelessTorcherinoBase) {
                return ((TileWirelessTorcherinoBase) accessor.getTileEntity())
                    .getWailaTail(itemStack, currenttip, accessor, config);
            }
            return currenttip;
        }

        @Override
        public net.minecraft.nbt.NBTTagCompound getNBTData(net.minecraft.entity.player.EntityPlayerMP player,
            net.minecraft.tileentity.TileEntity tile, net.minecraft.nbt.NBTTagCompound tag,
            net.minecraft.world.World world, int x, int y, int z) {
            if (tile instanceof TileWirelessTorcherinoBase) {
                return ((TileWirelessTorcherinoBase) tile).getNBTData(player, tile, tag, world, x, y, z);
            }
            return tag;
        }
    }
}
