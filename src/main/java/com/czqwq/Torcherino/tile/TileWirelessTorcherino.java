package com.czqwq.Torcherino.tile;

/**
 * Standard Wireless Torcherino tile (1x speed, GUI-operated).
 * Binds machines via DataStick and only accelerates bound TE blocks.
 */
public class TileWirelessTorcherino extends TileWirelessTorcherinoBase {

    @Override
    public int getSpeedMultiplier() {
        return 1;
    }

    @Override
    protected String getGuiTitleKey() {
        return "torcherino.gui.wireless.title";
    }

    @Override
    protected String getGuiPanelId() {
        return "wireless_torcherino_gui";
    }
}
