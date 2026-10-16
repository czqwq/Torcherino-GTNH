package com.czqwq.Torcherino.mixins.late.TecTech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;

import tectech.thing.metaTileEntity.multi.base.TTMultiblockBase;

/**
 * Lets Torcherino consume the TecTech research station's computation directly, the same way
 * {@code BaseMetaTileEntityAcceleration_Mixin} delegates to it.
 * <p>
 * <b>NH-Utilities coexistence:</b> NH-Utilities mixes the same class with an identical
 * {@code tickAcceleration(int)} (same field, same formula). Mixin keeps one copy and skips the
 * other, so the outcome is the same either way; what matters is that this mixin stays enabled -
 * it is the only thing attaching Torcherino's {@code ITileEntityTickAcceleration} to the station,
 * which is the marker {@code BaseMetaTileEntityAcceleration_Mixin} tests for. Dropping it while
 * NH-Utilities is installed would silently push research stations onto the generic multiblock
 * progress path.
 */
@Pseudo
@SuppressWarnings("UnusedMixin")
@Mixin(targets = "tectech.thing.metaTileEntity.multi.MTEResearchStation", remap = false)
public abstract class ResearchStationAcceleration_Mixin extends TTMultiblockBase
    implements ITileEntityTickAcceleration {

    @Shadow(remap = false)
    private long computationRemaining;

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean tickAcceleration(int tickAcceleratedRate) {
        if (this.computationRemaining <= 0) return true;
        this.computationRemaining -= tickAcceleratedRate * eAvailableData;
        return true;
    }

    protected ResearchStationAcceleration_Mixin(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }
}
