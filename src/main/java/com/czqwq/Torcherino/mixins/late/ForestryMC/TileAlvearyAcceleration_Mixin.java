package com.czqwq.Torcherino.mixins.late.ForestryMC;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;

import forestry.api.apiculture.IBeekeepingLogic;

/**
 * Mixin to add Torcherino acceleration support to ForestryMC Alveary multiblocks.
 * <p>
 * The Alveary is a 3×3×3 multiblock. Each block in the structure has a
 * {@code TileAlveary} tile entity, but ticking is driven centrally by the
 * {@code AlvearyController}. All alveary parts share the same controller,
 * which provides a single {@link IBeekeepingLogic} instance via
 * {@code TileAlveary#getBeekeepingLogic()}.
 * <p>
 * Because multiple alveary blocks may be in range of the same torch, we use
 * per-world deduplication (tracked by beekeepingLogic identity hash) to
 * ensure each assembled controller is accelerated only once per world tick.
 * <p>
 * Acceleration directly drives {@link IBeekeepingLogic#canWork()} /
 * {@link IBeekeepingLogic#doWork()}, the same core bee-progression calls
 * made by {@code AlvearyController.updateServer()}. Unassembled alvearies
 * use a fake beekeeping logic whose {@code canWork()} always returns false,
 * so no explicit assembly check is needed.
 */
@Pseudo
@SuppressWarnings("UnusedMixin")
@Mixin(targets = "forestry.apiculture.multiblock.TileAlveary", remap = false)
public abstract class TileAlvearyAcceleration_Mixin extends TileEntity
    implements ITileEntityTickAcceleration {

    /** Per-world dedup trackers (by beekeepingLogic identity) to prevent multi-acceleration. */
    private static final Map<World, AlvearyAccelTracker> worldTrackers = new WeakHashMap<>();

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean tickAcceleration(int tickAcceleratedRate) {
        // Only run on server
        if (this.worldObj == null || this.worldObj.isRemote) {
            return true;
        }

        IBeekeepingLogic beekeepingLogic = this.getBeekeepingLogic();
        if (beekeepingLogic == null) {
            return true;
        }

        // Dedup: each assembled Alveary has a unique beekeepingLogic instance.
        // FakeAlvearyController returns FakeBeekeepingLogic (singleton) whose
        // canWork() always returns false, so unassembled alvearies are harmless.
        int controllerId = System.identityHashCode(beekeepingLogic);
        synchronized (worldTrackers) {
            AlvearyAccelTracker tracker = worldTrackers.computeIfAbsent(
                this.worldObj,
                w -> new AlvearyAccelTracker());
            long currentTick = this.worldObj.getTotalWorldTime();
            if (tracker.worldTick != currentTick) {
                tracker.worldTick = currentTick;
                tracker.accelerated.clear();
            }
            if (!tracker.accelerated.add(controllerId)) {
                return true; // Already accelerated this tick via another alveary block
            }
        }

        // Accelerate: drive the beekeeping work loop extra times
        for (int i = 0; i < tickAcceleratedRate; i++) {
            if (beekeepingLogic.canWork()) {
                beekeepingLogic.doWork();
            }
        }

        return true;
    }

    /** Shadow for {@code TileAlveary#getBeekeepingLogic()} — defined directly on the target class. */
    @Shadow(remap = false)
    public abstract IBeekeepingLogic getBeekeepingLogic();

    // ---- Private types ----

    /** Per-world tick state for deduplication. Tracks controllers by beekeepingLogic identity. */
    private static class AlvearyAccelTracker {

        long worldTick = -1;
        final Set<Integer> accelerated = new HashSet<>();
    }
}
