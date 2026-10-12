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
     * Returns the {@link System#nanoTime()} deadline for a single torch tick, or
     * {@link #NO_DEADLINE} when the time budget is disabled.
     * <p>
     * This must be called <b>once per torch tick</b> (not per position): the budget
     * documented in {@link Config#tickBudgetNanos} is a per-torch-tick cap, and creating
     * it per position meant one torch covering a 9×3×9 area could legitimately spend
     * 243 × tickBudgetNanos in a single tick. A cutoff also lets the caller stop walking
     * its area entirely once the budget is spent, instead of starting futile work on every
     * remaining position.
     */
    public static long newTickDeadline() {
        return Config.enableTickBudget ? System.nanoTime() + Config.tickBudgetNanos : NO_DEADLINE;
    }

    /**
     * Deadline value meaning "no time budget".
     * <p>
     * Must be {@code Long.MAX_VALUE}, not {@code Long.MIN_VALUE}: {@link System#nanoTime()} has
     * an unspecified origin and may legitimately be <b>negative</b>, so it is not guaranteed to
     * be greater than {@code Long.MIN_VALUE}. A {@code MIN_VALUE} sentinel would trip the
     * "budget spent" branch on the very first position and silently disable all acceleration.
     */
    public static final long NO_DEADLINE = Long.MAX_VALUE;

    /**
     * Accelerate a single block position. Called from all torch types.
     *
     * @param world        the world
     * @param torchX       torch X coordinate (center, to skip self)
     * @param torchY       torch Y coordinate
     * @param torchZ       torch Z coordinate
     * @param timeRate     effective speed (already multiplied)
     * @param x            target block X
     * @param y            target block Y
     * @param z            target block Z
     * @param tickDeadline per-torch-tick deadline from {@link #newTickDeadline()}, or
     *                     {@link #NO_DEADLINE} to run without a budget
     */
    public static void accelerateAtPosition(World world, int torchX, int torchY, int torchZ, int timeRate, int x, int y,
        int z, long tickDeadline) {
        // Skip self
        if (x == torchX && y == torchY && z == torchZ) {
            return;
        }

        // Skip air before anything else. Inside World.updateEntities() the added-tile-entity
        // list is being iterated (World.field_147481_N == true), so World.getTileEntity() on
        // an air position costs a linear scan of that list, a chunk lookup and then *another*
        // linear scan before returning null — for a position that provably has no tile entity.
        // Since most of a torch's area is air, this is the single cheapest big win here.
        Block block = world.getBlock(x, y, z);
        if (block.isAir(world, x, y, z)) {
            return;
        }

        // Overlap detection: only active when stacking acceleration is disabled.
        // When stacking is enabled, multiple torches can accelerate the same
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

        // Accelerate block random ticks
        if (block.getTickRandomly()) {
            for (int i = 0; i < timeRate; i++) {
                try {
                    block.updateTick(world, x, y, z, SHARED_RANDOM);
                } catch (Exception ignored) {
                    // Ignore exceptions during acceleration (e.g. BiomesOPlenty NPE)
                }
                if (System.nanoTime() > tickDeadline) {
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
            if (System.nanoTime() > tickDeadline) {
                return;
            }
        }
    }

    /**
     * Returns true if the given TileEntity is any type of Torcherino tile.
     * Uses ITorcherinoTile interface — all torch tile types implement it.
     * Used to prevent torches from accelerating each other (infinite recursion).
     */
    public static boolean isTorcherinoTile(TileEntity tile) {
        return tile instanceof ITorcherinoTile;
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
