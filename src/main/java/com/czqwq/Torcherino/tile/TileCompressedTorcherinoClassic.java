package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

/**
 * Compressed Classic Torcherino - 9x acceleration multiplier compared to base Classic
 */
public class TileCompressedTorcherinoClassic extends TileTorcherinoClassic {

    private static final String[] SPEEDS = new String[] { "Stopped", "900% increase", "1800% increase",
        "2700% increase", "3600% increase" };

    public TileCompressedTorcherinoClassic() {
        super();
    }

    @Override
    protected int getSpeedMultiplier() {
        // 9x multiplier for compressed variant
        return 9;
    }

    @Override
    public String getSpeedDescription() {
        byte currentSpeed = getSpeed();
        if (currentSpeed >= 0 && currentSpeed < SPEEDS.length) {
            return SPEEDS[currentSpeed];
        }
        return SPEEDS[0];
    }

    @Override
    public void changeMode(boolean sneaking, EntityPlayer player) {
        if (sneaking) {
            byte newSpeed = (byte) ((getSpeed() + 1) % SPEEDS.length);
            setSpeed(newSpeed);
            player.addChatComponentMessage(
                new ChatComponentText(translateToLocal("torcherino.change_mode_speed") + " " + getSpeedDescription()));
        } else {
            super.changeMode(sneaking, player);
        }
    }
}
