package com.czqwq.Torcherino.tile;

/**
 * Double Compressed Decelerate Torcherino tile (GUI-operated).
 * Deceleration: 0% / 8100% / 16200% / 24300% / 32400% (81x multiplier, configurable via maxSpeedLevel).
 */
public class TileDoubleCompressedDecelerateTorcherino extends TileDecelerateTorcherinoBase {

    @Override
    protected int getSpeedMultiplier() {
        return 81;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.decelerate.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "double_compressed_decelerate_torcherino_gui";
    }
}
