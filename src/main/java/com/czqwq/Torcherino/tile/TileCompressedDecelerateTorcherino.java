package com.czqwq.Torcherino.tile;

/**
 * Compressed Decelerate Torcherino tile (GUI-operated).
 * Deceleration: 0% / 900% / 1800% / 2700% / 3600% (9x multiplier, configurable via maxSpeedLevel).
 */
public class TileCompressedDecelerateTorcherino extends TileDecelerateTorcherinoBase {

    @Override
    protected int getSpeedMultiplier() {
        return 9;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.decelerate.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "compressed_decelerate_torcherino_gui";
    }
}
