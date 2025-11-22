package com.czqwq.Torcherino.tile;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileTorcherinoAccelerated extends TileEntity {

    private int timeRate = 4;
    private boolean is_active = true;

    // 加速区域范围
    private static final int RADIUS = 2;
    private static final int HEIGHT = 1;

    public void setTimeRate(int rate) {
        this.timeRate = rate;
    }

    public int getTimeRate() {
        return this.timeRate;
    }

    public void setActive(boolean active) {
        this.is_active = active;
    }

    public boolean getActive() {
        return this.is_active;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj.isRemote || !this.is_active) return;

        // 定义加速区域
        int minX = this.xCoord - RADIUS;
        int maxX = this.xCoord + RADIUS;
        int minY = this.yCoord - HEIGHT;
        int maxY = this.yCoord + HEIGHT;
        int minZ = this.zCoord - RADIUS;
        int maxZ = this.zCoord + RADIUS;

        // 加速区域内的方块和TileEntity
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    accelerateAtPosition(this.worldObj, x, y, z);
                }
            }
        }
    }

    private void accelerateAtPosition(World world, int x, int y, int z) {
        // 避免加速自身
        if (x == this.xCoord && y == this.yCoord && z == this.zCoord) {
            return;
        }

        Block block = world.getBlock(x, y, z);

        // 加速方块随机刻
        if (block != null && block.getTickRandomly()) {
            for (int i = 0; i < timeRate; i++) {
                block.updateTick(world, x, y, z, new Random());
            }
        }

        // 加速TileEntity
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null && !(tileEntity instanceof TileTorcherinoAccelerated)
            && !tileEntity.isInvalid()
            && tileEntity.canUpdate()) {

            for (int i = 0; i < timeRate; i++) {
                try {
                    tileEntity.updateEntity();
                } catch (Exception e) {
                    // 忽略加速过程中可能发生的异常
                }
            }
        }
    }
}
