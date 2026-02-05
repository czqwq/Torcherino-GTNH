package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

/**
 * Double Compressed Classic Torcherino - 81x acceleration multiplier compared to base Classic
 */
public class TileDoubleCompressedTorcherinoClassic extends TileTorcherinoClassic {

    private static final String[] SPEEDS = new String[] { "Stopped", "8100% increase", "16200% increase",
        "24300% increase", "32400% increase" };

    public TileDoubleCompressedTorcherinoClassic() {
        super();
    }

    @Override
    protected int getSpeedMultiplier() {
        // 81x multiplier for double compressed variant (9 * 9)
        return 81;
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
