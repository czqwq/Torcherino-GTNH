package com.czqwq.Torcherino.tile;

/**
 * Standard Torcherino tile (GUI-operated).
 * Speed: 0% / 100% / 200% / 300% / 400% (configurable via maxSpeedLevel).
 * Range: X=0..maxXRadius, Y=0..maxYRadius, Z=0..maxZRadius.
 */
public class TileTorcherinoAccelerated extends TileTorcherinoBase {

    @Override
    protected int getSpeedMultiplier() {
        return 1;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "torcherino_gui";
    }
}
