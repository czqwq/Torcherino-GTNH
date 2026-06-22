package com.czqwq.Torcherino.tile;

/**
 * Double Compressed Torcherino tile (GUI-operated).
 * Speed: 0% / 8100% / 16200% / 24300% / 32400% (81x multiplier, configurable via maxSpeedLevel).
 */
public class TileDoubleCompressedTorcherino extends TileTorcherinoBase {

    @Override
    protected int getSpeedMultiplier() {
        return 81;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "double_compressed_torcherino_gui";
    }
}
