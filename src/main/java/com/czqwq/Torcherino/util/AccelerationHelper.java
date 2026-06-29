package com.czqwq.Torcherino.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

        // Overlap detection: skip if already accelerated this tick by another torch
        if (Config.enableOverlapDetection && isPositionAcceleratedThisTick(world, x, y, z)) {
            return;
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
            // Mark position as accelerated for overlap detection
            if (Config.enableOverlapDetection) {
                markPositionAccelerated(world, x, y, z);
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

        // Mark position as accelerated for overlap detection
        if (Config.enableOverlapDetection) {
            markPositionAccelerated(world, x, y, z);
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
     * Check if a position has already been accelerated this world tick.
     */
    private static boolean isPositionAcceleratedThisTick(World world, int x, int y, int z) {
        synchronized (worldTrackers) {
            TickTracker tracker = worldTrackers.get(world);
            if (tracker == null) return false;
            long currentTick = world.getTotalWorldTime();
            if (tracker.worldTick != currentTick) {
                // New tick, clear tracking
                tracker.worldTick = currentTick;
                tracker.acceleratedPositions.clear();
                return false;
            }
            return tracker.acceleratedPositions.contains(packPosition(x, y, z));
        }
    }

    /**
     * Mark a position as having been accelerated this world tick.
     */
    private static void markPositionAccelerated(World world, int x, int y, int z) {
        synchronized (worldTrackers) {
            long currentTick = world.getTotalWorldTime();
            TickTracker tracker = worldTrackers.computeIfAbsent(world, w -> new TickTracker());
            if (tracker.worldTick != currentTick) {
                tracker.worldTick = currentTick;
                tracker.acceleratedPositions.clear();
            }
            tracker.acceleratedPositions.add(packPosition(x, y, z));
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
     * Per-world tick state for overlap detection.
     */
    private static class TickTracker {

        long worldTick = -1;
        long lastAccessTick = 0;
        final Set<Long> acceleratedPositions = new HashSet<>(256);
    }
}
