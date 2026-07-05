package com.czqwq.Torcherino.tile;

/**
 * Double Compressed Wireless Torcherino tile (81x speed).
 */
public class TileDoubleCompressedWirelessTorcherino extends TileWirelessTorcherinoBase {

    @Override
    protected int getSpeedMultiplier() {
        return 81;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.wireless_double.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "wireless_double_compressed_torcherino_gui";
    }
}
