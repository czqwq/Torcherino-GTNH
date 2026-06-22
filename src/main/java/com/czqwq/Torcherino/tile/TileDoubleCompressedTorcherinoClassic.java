package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

/**
 * Double Compressed Classic Torcherino - 81x acceleration multiplier compared to base Classic.
 */
public class TileDoubleCompressedTorcherinoClassic extends TileTorcherinoClassic {

    @Override
    protected int getSpeedMultiplier() {
        return 81;
    }

    @Override
    public String getSpeedDescription() {
        byte currentSpeed = getSpeed();
        if (currentSpeed == 0) return "Stopped";
        int pct = currentSpeed * 8100;
        return pct + "% increase";
    }

    @Override
    public void changeMode(boolean sneaking, EntityPlayer player) {
        if (sneaking) {
            byte newSpeed = (byte) ((getSpeed() + 1) % (com.czqwq.Torcherino.Config.maxSpeedLevel + 1));
            setSpeed(newSpeed);
            player.addChatComponentMessage(
                new ChatComponentText(translateToLocal("torcherino.change_mode_speed") + " " + getSpeedDescription()));
        } else {
            super.changeMode(sneaking, player);
        }
    }
}
