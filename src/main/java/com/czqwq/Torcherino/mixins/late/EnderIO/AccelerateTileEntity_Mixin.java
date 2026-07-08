package com.czqwq.Torcherino.mixins.late.EnderIO;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;
import com.enderio.core.common.TileEntityEnder;

import crazypants.enderio.conduit.AbstractConduitNetwork;
import crazypants.enderio.conduit.IConduit;

/**
 * Mixin to accelerate EnderIO tile entities.
 * <p>
 * For regular EnderIO machines, calling {@code updateEntity()} repeatedly is sufficient.
 * For conduit bundles, which perform item/fluid transfer through <b>networks</b> whose
 * tick logic lives in {@code AbstractConduitNetwork#doNetworkTick()}, this mixin
 * redirects acceleration to tick each unique conduit network.
 * <p>
 * Reflection is used to access {@code TileConduitBundle} because its interface hierarchy
 * (via {@code IConduitBundle}) references optional mod APIs (Mekanism, ImmibisMicroblocks)
 * that may not be on the compile classpath. At runtime, the conduit bundle class is
 * always fully available when EnderIO is loaded.
 * <p>
 * Duplicate acceleration is prevented: if two conduit bundles share the same network,
 * it is only accelerated once per world tick.
 */
@SuppressWarnings("UnusedMixin")
@Mixin(value = TileEntityEnder.class, remap = false)
public abstract class AccelerateTileEntity_Mixin implements ITileEntityTickAcceleration {

    @Shadow(remap = false)
    private long lastUpdate;

    @Shadow(remap = true)
    public abstract void updateEntity();

    @Unique
    private int Torcherino$tickAcceleratedRate = 1;

    // ========== Per-world network acceleration tracking ==========

    /**
     * Tracks which conduit networks have already been accelerated this world tick,
     * keyed by world. Uses {@link IdentityHashMap} because {@link AbstractConduitNetwork}
     * does not override equals/hashCode.
     */
    @Unique
    private static final Map<World, Torcherino$PerWorldTracker> Torcherino$worldTrackers = new WeakHashMap<>();

    @Unique
    private static class Torcherino$PerWorldTracker {

        final World world;
        long lastTick = -1;
        final Map<AbstractConduitNetwork<?, ?>, Boolean> acceleratedNetworks = new IdentityHashMap<>(8);

        Torcherino$PerWorldTracker(World world) {
            this.world = world;
        }
    }

    @Unique
    private static Torcherino$PerWorldTracker Torcherino$getTracker(World world, long currentTick) {
        synchronized (Torcherino$worldTrackers) {
            Torcherino$PerWorldTracker tracker = Torcherino$worldTrackers.get(world);
            if (tracker == null || tracker.world != world) {
                tracker = new Torcherino$PerWorldTracker(world);
                Torcherino$worldTrackers.put(world, tracker);
            }
            if (tracker.lastTick != currentTick) {
                tracker.lastTick = currentTick;
                tracker.acceleratedNetworks.clear();
            }
            return tracker;
        }
    }

    /**
     * Accelerate all unique conduit networks in the given conduit bundle.
     * Uses reflection to access {@code TileConduitBundle} methods, avoiding compile-time
     * dependency on optional mod APIs (Mekanism, ImmibisMicroblocks) referenced by
     * {@code IConduitBundle}'s interface hierarchy.
     *
     * @param bundleObj the TileConduitBundle instance (passed as Object to avoid import)
     * @param rate      number of extra ticks to apply
     * @return true if at least one network was accelerated
     */
    @SuppressWarnings("unchecked")
    @Unique
    private static boolean Torcherino$accelerateBundleNetworks(Object bundleObj, int rate) {
        try {
            // Use reflection to call getWorldObj() and getConduits() — these are on
            // TileConduitBundle / TileEntity but the compiler can't resolve
            // TileConduitBundle's full interface hierarchy.
            World world = ((TileEntity) bundleObj).getWorldObj();
            if (world == null || world.isRemote || rate <= 0) {
                return false;
            }

            Collection<IConduit> conduits = (Collection<IConduit>) bundleObj.getClass()
                .getMethod("getConduits")
                .invoke(bundleObj);
            if (conduits == null || conduits.isEmpty()) {
                return false;
            }

            long currentTick = world.getTotalWorldTime();
            Torcherino$PerWorldTracker tracker = Torcherino$getTracker(world, currentTick);

            boolean accelerated = false;
            for (IConduit conduit : conduits) {
                AbstractConduitNetwork<?, ?> network = conduit.getNetwork();
                if (network == null) {
                    continue;
                }
                // Skip networks already accelerated this world tick
                if (tracker.acceleratedNetworks.containsKey(network)) {
                    continue;
                }
                tracker.acceleratedNetworks.put(network, Boolean.TRUE);

                // Tick the network 'rate' additional times
                for (int i = 0; i < rate; i++) {
                    try {
                        network.doNetworkTick();
                    } catch (Exception ignored) {
                        // Ignore exceptions during acceleration
                    }
                }
                accelerated = true;
            }
            return accelerated;
        } catch (Exception e) {
            // Reflection failed — conduit bundle class structure changed?
            return false;
        }
    }

    // ========== Interface implementation ==========

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public int getTickAcceleratedRate() {
        return this.Torcherino$tickAcceleratedRate;
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean tickAcceleration(int tickAcceleratedRate) {
        this.Torcherino$tickAcceleratedRate = tickAcceleratedRate;

        // Check if this is a conduit bundle at runtime.
        // We use the FQN string to avoid importing TileConduitBundle, since its
        // interface hierarchy references optional mod APIs not on our classpath.
        if (isConduitBundle()) {
            // Run base update once for connection/network maintenance
            this.lastUpdate = -1L;
            this.updateEntity();
            // Accelerate each unique conduit network
            Torcherino$accelerateBundleNetworks(this, tickAcceleratedRate);
        } else {
            // For regular machines: call updateEntity() repeatedly
            for (int i = 0; i < tickAcceleratedRate; i++) {
                this.lastUpdate = -1L;
                this.updateEntity();
            }
        }
        return true;
    }

    /**
     * Check if the target instance is a TileConduitBundle, using the class name
     * to avoid a compile-time dependency on the type.
     */
    @Unique
    private boolean isConduitBundle() {
        // Walk up the class hierarchy to check for TileConduitBundle
        for (Class<?> cls = this.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
            if ("crazypants.enderio.conduit.TileConduitBundle".equals(cls.getName())) {
                return true;
            }
        }
        return false;
    }
}
