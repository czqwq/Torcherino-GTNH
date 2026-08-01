package com.czqwq.Torcherino.tile;

import com.czqwq.Torcherino.util.DecelerationHelper;

/**
 * Abstract base for GUI-operated Decelerate Torcherino tiles (Decelerate, Compressed, DoubleCompressed).
 * <p>
 * Mirrors {@link TileTorcherinoBase} (same GUI, same field/NBT handling) but the area effect
 * registers deceleration zones instead of accelerating: a rate of N means the covered machines
 * only tick 1 out of N world ticks, so they take N times as long to complete the same progress.
 * <p>
 * Subclasses only need to provide {@link #getSpeedMultiplier()} and {@link #getGuiTitleKey()}.
 */
public abstract class TileDecelerateTorcherinoBase extends TileTorcherinoBase {

    @Override
    protected void applyAreaEffect(int effectiveSpeed) {
        DecelerationHelper.registerZone(
            this.worldObj,
            this.xCoord,
            this.yCoord,
            this.zCoord,
            this.xMin,
            this.yMin,
            this.zMin,
            this.xMax,
            this.yMax,
            this.zMax,
            effectiveSpeed);
    }
}
