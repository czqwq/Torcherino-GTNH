package com.czqwq.Torcherino.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickDeceleration;
import com.czqwq.Torcherino.api.interfaces.ITorcherinoTile;

/**
 * Shared deceleration logic for the Decelerate Torcherino family.
 * <p>
 * Deceleration works by suppressing the target tile entity's own tick:
 * a rate of N (e.g. 400% → N=4) allows the machine's {@code updateEntity()}
 * to run on only 1 out of every N world ticks, so the machine takes N times
 * as long to complete the same progress (1 real second only advances 1/N of
 * a machine second).
 * <p>
 * Two mechanisms:
 * <ul>
 * <li><b>Whole tick skip</b> (default): the {@code WorldTileEntityDeceleration_Mixin}
 * (early) redirects the single {@code tileEntity.updateEntity()} call site in
 * {@code World.updateEntities()} and skips it on the held-back ticks. All side
 * effects are slowed together. 1.7.10's {@code World} never calls
 * {@code TileEntity.canUpdate()}, so the call site redirect is the only way to
 * gate arbitrary tile entities (vanilla machines, other mods).</li>
 * <li><b>Progress-only</b> ({@link ITileEntityTickDeceleration} tiles, i.e. GregTech
 * machines): they are never skipped — EU consumption/generation (incl. generator
 * EU/t output) stays normal. Instead the {@code BaseMetaTileEntityDeceleration_Mixin}
 * consults {@link #shouldHoldBackProgress} every tick and holds back the natural
 * progress gain on the held-back ticks.</li>
 * </ul>
 * Every decel torch re-registers its zone every tick. Entries that stop being
 * refreshed (torch broken or unloaded) are lazily dropped after one tick.
 * <p>
 * <b>Performance contract:</b> {@link #shouldSkipUpdate} is called from the
 * {@code World.updateEntities()} redirect for <i>every</i> tile entity of
 * <i>every</i> loaded dimension (tens of thousands per tick on a GTNH server),
 * so deceleration must be free while the feature is unused. That is what the
 * {@link #hasZones} volatile gate is for: with no decel torch placed it reduces
 * the whole check to a single volatile read, and none of the
 * {@code instanceof}/map work below is ever reached.
 */
public final class DecelerationHelper {

    /** Per-world registry: packed position → current deceleration zone entry. */
    private static final Map<World, Map<Long, ZoneEntry>> zones = new WeakHashMap<>();

    /**
     * Fast-path gate for the mixin hot path: {@code false} whenever no deceleration
     * zone is registered for any world, so the caller can bail out with a single
     * volatile read instead of touching {@link #zones}.
     * <p>
     * Set by {@link #registerZone} when the first zone is added, cleared by
     * {@link #pruneStaleZones} when the last entry is dropped. Both transitions
     * happen while holding the {@link #zones} monitor, so the flag can never be
     * stale in the "active" direction for longer than a single pruning pass.
     */
    private static volatile boolean hasZones = false;

    private DecelerationHelper() {}

    /**
     * Register (or refresh) the deceleration zone of a single torch for the current tick.
     * Called from the torch's {@code updateEntity()} every tick it is active.
     * Only positions that actually contain a tile entity are registered, keeping the
     * map small. Overlapping torches: the highest rate wins.
     *
     * @param world  the world
     * @param torchX torch X (skipped as the torch itself is never decelerated)
     * @param torchY torch Y
     * @param torchZ torch Z
     * @param rate   deceleration rate (effective speed): N = machine runs 1/N as fast
     */
    public static void registerZone(World world, int torchX, int torchY, int torchZ, int xMin, int yMin, int zMin,
        int xMax, int yMax, int zMax, int rate) {
        if (world == null || world.isRemote || rate <= 1) return;

        long tick = world.getTotalWorldTime();
        synchronized (zones) {
            Map<Long, ZoneEntry> worldZones = zones.computeIfAbsent(world, w -> new HashMap<>());
            for (int x = xMin; x <= xMax; ++x) {
                for (int y = yMin; y <= yMax; ++y) {
                    for (int z = zMin; z <= zMax; ++z) {
                        if (x == torchX && y == torchY && z == torchZ) continue;
                        if (world.getTileEntity(x, y, z) == null) continue;
                        long key = packPosition(x, y, z);
                        ZoneEntry entry = worldZones.get(key);
                        if (entry == null) {
                            worldZones.put(key, new ZoneEntry(rate, tick));
                            // Arm the volatile gate in shouldSkipUpdate/shouldHoldBackProgress.
                            // Cheap volatile store, and only on the first zone of a world map.
                            hasZones = true;
                        } else {
                            entry.rate = Math.max(entry.rate, rate);
                            entry.lastRefreshTick = tick;
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns true if the given tile entity should be prevented from updating this world tick.
     * Called from the {@code WorldTileEntityDeceleration_Mixin} redirect
     * (server side only, once per tile per tick — keep it cheap).
     * Tiles handling deceleration precisely ({@link ITileEntityTickDeceleration},
     * e.g. GT machines) are never skipped — they tick normally so energy flows are
     * unaffected, and their progress is held back by {@link #shouldHoldBackProgress}.
     */
    public static boolean shouldSkipUpdate(TileEntity tile) {
        // Fast path FIRST: called once per tile entity per tick from the
        // World.updateEntities() redirect (30k–50k tiles/tick on a GTNH server).
        // With no decel torch placed this single volatile read is the entire cost —
        // do not move any other work above it.
        if (!hasZones) return false;
        if (tile == null || tile.getWorldObj() == null || tile.getWorldObj().isRemote) return false;
        // Torches never slow each other down (mirrors AccelerationHelper.isTorcherinoTile)
        if (tile instanceof ITorcherinoTile) return false;
        // Precise deceleration (progress only, energy untouched) handled by the GT mixin
        if (tile instanceof ITileEntityTickDeceleration) return false;

        return lookupAndCheck(tile);
    }

    /**
     * Returns true if the given tile entity is inside a deceleration zone and this world
     * tick is a "held-back" tick — its natural progress gain of this tick must be held
     * back. Called from the GT deceleration mixin after every machine tick (server side).
     */
    public static boolean shouldHoldBackProgress(TileEntity tile) {
        // Same volatile fast path as shouldSkipUpdate: GT machines hit this twice per
        // tick (HEAD + TAIL) each, so keep it free while no decel torch is placed.
        if (!hasZones) return false;
        if (tile == null || tile.getWorldObj() == null || tile.getWorldObj().isRemote) return false;
        if (tile instanceof ITorcherinoTile) return false;
        return lookupAndCheck(tile);
    }

    /** Shared zone lookup: true when decelerated AND this is a held-back tick. */
    private static boolean lookupAndCheck(TileEntity tile) {
        World world = tile.getWorldObj();
        long key = packPosition(tile.xCoord, tile.yCoord, tile.zCoord);
        synchronized (zones) {
            Map<Long, ZoneEntry> worldZones = zones.get(world);
            if (worldZones == null || worldZones.isEmpty()) return false;
            ZoneEntry entry = worldZones.get(key);
            if (entry == null) return false;
            long tick = world.getTotalWorldTime();
            if (tick - entry.lastRefreshTick > 1) {
                // The torch no longer refreshes this position (broken/unloaded) — drop the entry.
                // Tolerance of 1 tick covers both update orders within the tile entity list.
                worldZones.remove(key);
                // Once the last zone is gone, re-arm the volatile fast path so the
                // per-tile cost drops back to a single read without waiting for a restart.
                if (worldZones.isEmpty()) {
                    if (pruneStaleZones(tick)) {
                        // Another world still has live zones; nothing left to clean up here.
                        return false;
                    }
                    // This was the last zone — drop the now-useless map.
                    zones.remove(world);
                }
                return false;
            }
            // Run on only 1 out of `rate` ticks
            return tick % entry.rate != 0;
        }
    }

    /**
     * Drops every zone entry whose torch stopped refreshing it. Called (while holding the
     * {@link #zones} monitor) when a world's zone map has just become empty — i.e. while the
     * last deceleration zone is disappearing, which is exactly when the {@link #hasZones}
     * fast path has to be re-evaluated.
     * <p>
     * Entries refreshed on this tick by a torch of any world are kept, so a passing pruning
     * can never drop a zone that is still in use; the tolerance matches {@link #lookupAndCheck}.
     * Only reached from the path that has just emptied a world's map (i.e. while the
     * deceleration feature is being torn down), and it re-arms itself as soon as a live zone
     * is found, so it costs nothing while deceleration is idle.
     *
     * @param tick current total world time of the world being ticked
     * @return true if any other world still holds a live zone
     */
    private static boolean pruneStaleZones(long tick) {
        boolean anyZoneActive = false;
        for (Iterator<Map<Long, ZoneEntry>> worldIter = zones.values()
            .iterator(); worldIter.hasNext();) {
            Map<Long, ZoneEntry> worldZones = worldIter.next();
            // A torch that still exists has re-registered its zone during this tick.
            worldZones.values()
                .removeIf(entry -> tick - entry.lastRefreshTick > 1);
            if (worldZones.isEmpty()) {
                // Fully stale world — drop its map so the registry shrinks back to nothing.
                worldIter.remove();
            } else {
                anyZoneActive = true;
            }
        }
        hasZones = anyZoneActive;
        return anyZoneActive;
    }

    /**
     * Pack block coordinates into a single long for efficient storage in maps.
     * Assumes coordinate range fits in 21 bits each (roughly ±1,000,000).
     */
    private static long packPosition(int x, int y, int z) {
        return ((long) (x) & 0x1FFFFFL) << 42 | ((long) (y) & 0x1FFFFFL) << 21 | ((long) (z) & 0x1FFFFFL);
    }

    /** Per-position deceleration state: rate and the tick it was last refreshed. */
    private static final class ZoneEntry {

        int rate;
        long lastRefreshTick;

        ZoneEntry(int rate, long lastRefreshTick) {
            this.rate = rate;
            this.lastRefreshTick = lastRefreshTick;
        }
    }
}
