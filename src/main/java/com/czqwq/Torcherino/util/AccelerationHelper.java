package com.czqwq.Torcherino.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;
import com.czqwq.Torcherino.api.interfaces.ITorcherinoTile;
import com.czqwq.Torcherino.tile.TileCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileCompressedTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileTorcherinoAccelerated;
import com.czqwq.Torcherino.tile.TileTorcherinoClassic;

/**
 * Shared acceleration logic used by all Torcherino tile entities.
 * Provides tick budget control, overlap detection, and unified acceleration behavior.
 */
public final class AccelerationHelper {

    /** Shared Random instance to avoid allocation in hot loop. */
    private static final Random SHARED_RANDOM = new Random();

    /** Per-world tracking of accelerated positions to prevent overlap. */
    private static final Map<World, TickTracker> worldTrackers = new WeakHashMap<>();

    private AccelerationHelper() {}

    /**
     * Accelerate a single block position. Called from all torch types.
     *
     * @param world    the world
     * @param torchX   torch X coordinate (center, to skip self)
     * @param torchY   torch Y coordinate
     * @param torchZ   torch Z coordinate
     * @param timeRate effective speed (already multiplied)
     * @param x        target block X
     * @param y        target block Y
     * @param z        target block Z
     */
    public static void accelerateAtPosition(World world, int torchX, int torchY, int torchZ, int timeRate, int x, int y,
        int z) {
        // Skip self
        if (x == torchX && y == torchY && z == torchZ) {
            return;
        }

        // Overlap detection: only active when stacking acceleration is disabled.
        // When stacking is enabled (default), multiple torches can accelerate the same
        // machine cumulatively, so we skip all overlap checks.
        // When overlap is active, "fastest wins": a faster torch can override a slower
        // one's claim on a position, ensuring the highest-tier torch always takes effect.
        final boolean useOverlap = !Config.enableStackingAcceleration && Config.enableOverlapDetection;

        if (useOverlap) {
            int existingSpeed = getPositionSpeed(world, x, y, z);
            if (existingSpeed >= timeRate) {
                // Already accelerated by an equally fast or faster torch this tick
                return;
            }
            // Current torch is faster — it will override the slower claim below
        }

        // Tick budget start time
        long budgetEnd = 0;
        if (Config.enableTickBudget) {
            budgetEnd = System.nanoTime() + Config.tickBudgetNanos;
        }

        Block block = world.getBlock(x, y, z);

        // Accelerate block random ticks
        if (block != null && block.getTickRandomly()) {
            for (int i = 0; i < timeRate; i++) {
                try {
                    block.updateTick(world, x, y, z, SHARED_RANDOM);
                } catch (Exception ignored) {
                    // Ignore exceptions during acceleration (e.g. BiomesOPlenty NPE)
                }
                if (Config.enableTickBudget && System.nanoTime() > budgetEnd) {
                    return;
                }
            }
        }

        // Accelerate tile entity
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity == null || isTorcherinoTile(tileEntity) || tileEntity.isInvalid()) {
            return;
        }

        // Allow ITileEntityTickAcceleration implementations to be accelerated even when
        // canUpdate() returns false (e.g. Forestry multiblock parts which are ticked via controller)
        if (tileEntity instanceof ITileEntityTickAcceleration) {
            // Mark position with speed for fastest-wins overlap detection
            if (useOverlap) {
                markPositionAccelerated(world, x, y, z, timeRate);
            }
            try {
                ((ITileEntityTickAcceleration) tileEntity).tickAcceleration(timeRate);
            } catch (Exception ignored) {
                // Ignore acceleration exceptions
            }
            return;
        }

        if (!tileEntity.canUpdate()) {
            return;
        }

        // Mark position with speed for fastest-wins overlap detection
        if (useOverlap) {
            markPositionAccelerated(world, x, y, z, timeRate);
        }

        // Fallback: call updateEntity() repeatedly
        for (int i = 0; i < timeRate; i++) {
            try {
                tileEntity.updateEntity();
            } catch (Exception ignored) {
                // Ignore acceleration exceptions
            }
            if (Config.enableTickBudget && System.nanoTime() > budgetEnd) {
                return;
            }
        }
    }

    /**
     * Returns true if the given TileEntity is any type of Torcherino tile.
     * Used to prevent torches from accelerating each other (infinite recursion).
     */
    public static boolean isTorcherinoTile(TileEntity tile) {
        return tile instanceof ITorcherinoTile || tile instanceof TileTorcherinoAccelerated
            || tile instanceof TileCompressedTorcherino
            || tile instanceof TileDoubleCompressedTorcherino
            || tile instanceof TileTorcherinoClassic
            || tile instanceof TileCompressedTorcherinoClassic
            || tile instanceof TileDoubleCompressedTorcherinoClassic;
    }

    /**
     * Pack block coordinates into a single long for efficient storage in sets.
     * Assumes coordinate range fits in 21 bits each (roughly ±1,000,000).
     */
    private static long packPosition(int x, int y, int z) {
        return ((long) (x) & 0x1FFFFFL) << 42 | ((long) (y) & 0x1FFFFFL) << 21 | ((long) (z) & 0x1FFFFFL);
    }

    /**
     * Returns the speed of the torch that previously accelerated this position this world tick,
     * or 0 if the position hasn't been accelerated yet this tick.
     * Used for "fastest wins" overlap detection.
     */
    private static int getPositionSpeed(World world, int x, int y, int z) {
        synchronized (worldTrackers) {
            TickTracker tracker = worldTrackers.get(world);
            if (tracker == null) return 0;
            long currentTick = world.getTotalWorldTime();
            if (tracker.worldTick != currentTick) {
                // New tick, clear tracking
                tracker.worldTick = currentTick;
                tracker.acceleratedPositions.clear();
                return 0;
            }
            Integer speed = tracker.acceleratedPositions.get(packPosition(x, y, z));
            return speed != null ? speed : 0;
        }
    }

    /**
     * Mark a position as having been accelerated this world tick, recording the speed
     * of the torch that accelerated it. A faster torch can override a slower one's claim.
     */
    private static void markPositionAccelerated(World world, int x, int y, int z, int speed) {
        synchronized (worldTrackers) {
            long currentTick = world.getTotalWorldTime();
            TickTracker tracker = worldTrackers.computeIfAbsent(world, w -> new TickTracker());
            if (tracker.worldTick != currentTick) {
                tracker.worldTick = currentTick;
                tracker.acceleratedPositions.clear();
            }
            tracker.acceleratedPositions.put(packPosition(x, y, z), speed);
        }
    }

    /**
     * Called periodically to clean up world trackers for unloaded worlds.
     * Should be called from updateEntity() when the world tick changes.
     */
    public static void cleanupWorldTrackers() {
        synchronized (worldTrackers) {
            worldTrackers.values()
                .removeIf(tracker -> {
                    // Remove trackers that are more than 2 ticks old
                    // (they'll be recreated if the world is still active)
                    return tracker.lastAccessTick > 0;
                });
        }
    }

    /**
     * Per-world tick state for fastest-wins overlap detection.
     * Maps packed position → speed of the torch that accelerated it.
     * A faster torch can override a slower one's entry; equal or slower is skipped.
     */
    private static class TickTracker {

        long worldTick = -1;
        long lastAccessTick = 0;
        final Map<Long, Integer> acceleratedPositions = new HashMap<>(256);
    }
}
