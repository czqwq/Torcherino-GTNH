package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.api.interfaces.ITorcherinoTile;
import com.czqwq.Torcherino.util.AccelerationHelper;
import com.google.common.collect.ImmutableSet;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.common.tileentities.machines.basic.MTEWorldAccelerator;

/**
 * Classic Torcherino implementation based on MockTurtle7's original logic.
 * Uses chat-based mode cycling (sneak-click = speed, normal-click = area).
 * Refactored to use {@link AccelerationHelper} for shared acceleration logic.
 */
public class TileTorcherinoClassic extends TileEntity implements ITorcherinoTile {

    private static final ImmutableSet<Block> blacklist = ImmutableSet.of(
        Blocks.air,
        Blocks.bedrock,
        Blocks.obsidian,
        Blocks.stone,
        Blocks.glowstone,
        Blocks.netherrack,
        Blocks.sand,
        Blocks.gravel);

    private static final String[] MODES = new String[] { "Stopped", "Radius: +1, Area: 3x3x3",
        "Radius: +2, Area: 5x3x5", "Radius: +3, Area: 7x3x7", "Radius: +4, Area: 9x3x9" };

    private static final String[] SPEEDS = new String[] { "Stopped", "100% increase", "200% increase", "300% increase",
        "400% increase" };

    private boolean isActive;
    private byte speed;
    private byte mode;
    private byte cachedMode;
    private final Random rand;

    private int xMin, yMin, zMin, xMax, yMax, zMax;

    public TileTorcherinoClassic() {
        this.isActive = true;
        this.cachedMode = -1;
        this.rand = new Random();
    }

    // ========== ITorcherinoTile implementation ==========

    @Override
    public boolean getActive() {
        return this.isActive;
    }

    @Override
    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public boolean isStopped() {
        return this.mode == 0 || this.speed == 0;
    }

    @Override
    public int getEffectiveSpeed() {
        return this.speed * getSpeedMultiplier();
    }

    @Override
    public int getTorchX() {
        return this.xCoord;
    }

    @Override
    public int getTorchY() {
        return this.yCoord;
    }

    @Override
    public int getTorchZ() {
        return this.zCoord;
    }

    @Override
    public int getXRadius() {
        return this.mode;
    }

    @Override
    public int getYRadius() {
        return 1;
    }

    @Override
    public int getZRadius() {
        return this.mode;
    }

    // ========== Speed multiplier ==========

    /**
     * Override in subclasses to provide different speed multipliers.
     * Base: 1x, Compressed: 9x, Double Compressed: 81x
     */
    protected int getSpeedMultiplier() {
        return 1;
    }

    protected byte getSpeed() {
        return this.speed;
    }

    protected void setSpeed(byte speed) {
        this.speed = (byte) Math.max(0, Math.min(speed, Config.maxSpeedLevel));
    }

    // ========== Tick / Acceleration ==========

    @Override
    public void updateEntity() {
        if (this.worldObj.isRemote) return;
        if (!this.isActive || this.mode == 0 || this.speed == 0) return;

        if (this.cachedMode != this.mode) {
            this.xMin = this.xCoord - this.mode;
            this.yMin = this.yCoord - 1;
            this.zMin = this.zCoord - this.mode;
            this.xMax = this.xCoord + this.mode;
            this.yMax = this.yCoord + 1;
            this.zMax = this.zCoord + this.mode;
            this.cachedMode = this.mode;
        }

        int effectiveSpeed = getEffectiveSpeed();

        for (int x = this.xMin; x <= this.xMax; x++) {
            for (int y = this.yMin; y <= this.yMax; y++) {
                for (int z = this.zMin; z <= this.zMax; z++) {
                    final Block block = this.worldObj.getBlock(x, y, z);

                    if (blacklist.contains(block)) continue;

                    if (block.getTickRandomly()) {
                        for (int i = 0; i < effectiveSpeed; i++) {
                            try {
                                block.updateTick(this.worldObj, x, y, z, this.rand);
                            } catch (Exception ignored) {}
                        }
                    }

                    if (block.hasTileEntity(this.worldObj.getBlockMetadata(x, y, z))) {
                        final TileEntity tile = this.worldObj.getTileEntity(x, y, z);
                        if (tile != null && !AccelerationHelper.isTorcherinoTile(tile)
                            && !tile.isInvalid()
                            && !isGTWorldAccelerator(tile)) {
                            for (int i = 0; i < effectiveSpeed; i++) {
                                try {
                                    tile.updateEntity();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isGTWorldAccelerator(TileEntity te) {
        if (!Torcherino.hasGregTech) return false;
        if (te instanceof BaseMetaTileEntity baseMTE) {
            return baseMTE.getMetaTileEntity() instanceof MTEWorldAccelerator;
        }
        return false;
    }

    // ========== Mode / Interaction ==========

    public void changeMode(boolean sneaking, EntityPlayer player) {
        if (sneaking) {
            if (this.speed < Config.maxSpeedLevel) this.speed++;
            else this.speed = 0;
            player.addChatComponentMessage(
                new ChatComponentText(translateToLocal("torcherino.change_mode_speed") + " " + getSpeedDescription()));
        } else {
            int maxMode = Math.min(MODES.length - 1, Config.maxXRadius);
            if (this.mode < maxMode) this.mode++;
            else this.mode = 0;
            player.addChatComponentMessage(
                new ChatComponentText(translateToLocal("torcherino.change_mode_area") + " " + getModeDescription()));
        }
    }

    public String getSpeedDescription() {
        if (speed == 0) return "Stopped";
        int idx = Math.min(this.speed, SPEEDS.length - 1);
        if (idx < SPEEDS.length && !SPEEDS[idx].equals("Stopped")) return SPEEDS[idx];
        // Dynamic fallback for speed levels beyond hardcoded array
        int pct = this.speed * getSpeedMultiplier() * 100;
        return pct + "% increase";
    }

    public String getModeDescription() {
        if (mode == 0) return "Stopped";
        int idx = Math.min(this.mode, MODES.length - 1);
        if (idx < MODES.length && !MODES[idx].equals("Stopped")) return MODES[idx];
        // Dynamic fallback for mode/radius levels beyond hardcoded array
        int area = this.mode * 2 + 1;
        return "Radius: +" + this.mode + ", Area: " + area + "x3x" + area;
    }

    // ========== NBT ==========

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("Speed", speed);
        nbt.setByte("Mode", mode);
        nbt.setBoolean("IsActive", isActive);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.speed = (byte) clampInt(nbt.getByte("Speed"), 0, Config.maxSpeedLevel);
        this.mode = (byte) clampInt(nbt.getByte("Mode"), 0, Config.maxXRadius);
        this.isActive = nbt.getBoolean("IsActive");
    }

    /** Clamp an int value between min (inclusive) and max (inclusive). */
    private static int clampInt(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }
}
