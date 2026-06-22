package com.czqwq.Torcherino.tile;

/**
 * Compressed Torcherino tile (GUI-operated).
 * Speed: 0% / 900% / 1800% / 2700% / 3600% (9x multiplier, configurable via maxSpeedLevel).
 */
public class TileCompressedTorcherino extends TileTorcherinoBase {

    @Override
    protected int getSpeedMultiplier() {
        return 9;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "compressed_torcherino_gui";
    }
}
