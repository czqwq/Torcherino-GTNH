package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

import com.google.common.collect.ImmutableSet;

/**
 * Classic Torcherino implementation based on MockTurtle7's original logic
 */
public class TileTorcherinoClassic extends TileEntity {

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
    private Random rand;

    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax;
    private int zMax;

    public TileTorcherinoClassic() {
        this.isActive = true;
        this.cachedMode = -1;
        this.rand = new Random();
    }

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

        for (int x = this.xMin; x <= this.xMax; x++) {
            for (int y = this.yMin; y <= this.yMax; y++) {
                for (int z = this.zMin; z <= this.zMax; z++) {
                    final Block block = this.worldObj.getBlock(x, y, z);

                    if (blacklist.contains(block)) continue;

                    if (block.getTickRandomly()) {
                        for (int i = 0; i < this.speed; i++) {
                            try {
                                block.updateTick(this.worldObj, x, y, z, this.rand);
                            } catch (Exception e) {
                                // Ignore exceptions during acceleration
                            }
                        }
                    }

                    if (block.hasTileEntity(this.worldObj.getBlockMetadata(x, y, z))) {
                        final TileEntity tile = this.worldObj.getTileEntity(x, y, z);
                        if (tile != null && !(tile instanceof TileTorcherinoClassic)
                            && !(tile instanceof TileCompressedTorcherinoClassic)
                            && !(tile instanceof TileDoubleCompressedTorcherinoClassic)
                            && !tile.isInvalid()) {
                            for (int i = 0; i < this.speed; i++) {
                                try {
                                    tile.updateEntity();
                                } catch (Exception e) {
                                    // Ignore exceptions during acceleration
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void changeMode(boolean sneaking, EntityPlayer player) {
        if (sneaking) {
            if (this.speed < SPEEDS.length - 1) this.speed++;
            else this.speed = 0;
            player.addChatComponentMessage(
                new ChatComponentText(translateToLocal("torcherino.change_mode_speed") + " " + getSpeedDescription()));
        } else {
            if (this.mode < MODES.length - 1) this.mode++;
            else this.mode = 0;
            player.addChatComponentMessage(
                new ChatComponentText(translateToLocal("torcherino.change_mode_area") + " " + getModeDescription()));
        }
    }

    public String getSpeedDescription() {
        return SPEEDS[this.speed];
    }

    public String getModeDescription() {
        return MODES[this.mode];
    }

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
        this.speed = nbt.getByte("Speed");
        this.mode = nbt.getByte("Mode");
        this.isActive = nbt.getBoolean("IsActive");
    }
}
