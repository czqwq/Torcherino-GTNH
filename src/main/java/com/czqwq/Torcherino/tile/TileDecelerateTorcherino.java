package com.czqwq.Torcherino.tile;

/**
 * Decelerate Torcherino tile (GUI-operated).
 * Deceleration: 0% / 100% / 200% / 300% / 400% (configurable via maxSpeedLevel).
 * 400% means covered machines take 4x as long (1s of machine progress needs 4s real time).
 * Range: X=0..maxXRadius, Y=0..maxYRadius, Z=0..maxZRadius.
 */
public class TileDecelerateTorcherino extends TileDecelerateTorcherinoBase {

    @Override
    protected int getSpeedMultiplier() {
        return 1;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.decelerate.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "decelerate_torcherino_gui";
    }
}
