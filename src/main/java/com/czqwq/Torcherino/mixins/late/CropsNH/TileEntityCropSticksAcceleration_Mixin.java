package com.czqwq.Torcherino.mixins.late.CropsNH;

import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;

/**
 * Mixin to add Torcherino acceleration support to CropsNH crop sticks.
 * <p>
 * Instead of calling {@code updateEntity()} repeatedly (which wastes time on
 * NBT save and network sync every call), this mixin directly advances the
 * internal ticker and triggers growth ticks at the correct rate.
 * <p>
 * The acceleration speed matches what would happen if {@code updateEntity()}
 * were called {@code tickAcceleratedRate} extra times — the internal 256-tick
 * counter is advanced, and {@code onGrowthTick()} fires each time it wraps.
 */
@Pseudo
@SuppressWarnings("UnusedMixin")
@Mixin(targets = "com.gtnewhorizon.cropsnh.tileentity.TileEntityCropSticks", remap = false)
public abstract class TileEntityCropSticksAcceleration_Mixin extends TileEntity implements ITileEntityTickAcceleration {

    /** Internal tick counter that drives the 256-tick growth cycle. */
    @Shadow(remap = false)
    private int ticker;

    /** True until the first updateEntity() call completes. */
    @Shadow(remap = false)
    private boolean isFirstTick;

    /** Set to true when the render state changes, triggering NBT save + sync. */
    @Shadow(remap = false)
    private boolean isDirty;

    /** @see com.gtnewhorizon.cropsnh.tileentity.TileEntityCropSticks#TICK_RATE */
    private static final int TICK_RATE = 256;

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean tickAcceleration(int tickAcceleratedRate) {
        // Never accelerate on client or before first tick
        if (this.worldObj == null || this.worldObj.isRemote || this.isFirstTick) {
            return true;
        }

        // Advance the internal ticker and fire growth ticks at the correct rate
        for (int i = 0; i < tickAcceleratedRate; i++) {
            this.ticker++;
            if (this.ticker >= TICK_RATE) {
                this.ticker = 0;
                this.onGrowthTick();
            }
        }

        // Handle dirty state (e.g. sprite index changed after growth).
        // Inline TileEntityCropsNH.markForUpdate() logic since that method
        // lives in the parent class and can't be reached via @Shadow.
        if (this.isDirty) {
            this.isDirty = false;
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }

        return true;
    }

    /** Shadow for {@code TileEntityCropSticks#onGrowthTick()} — defined directly on the target. */
    @Shadow(remap = false)
    public abstract void onGrowthTick();
}
