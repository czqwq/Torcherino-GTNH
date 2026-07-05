package com.czqwq.Torcherino.tile;

/**
 * Compressed Wireless Torcherino tile (9x speed).
 */
public class TileCompressedWirelessTorcherino extends TileWirelessTorcherinoBase {

    @Override
    protected int getSpeedMultiplier() {
        return 9;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.wireless_compressed.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "wireless_compressed_torcherino_gui";
    }
}
