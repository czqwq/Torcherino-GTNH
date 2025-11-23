package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class TileCompressedTorcherino extends TileEntity {

    // 加速效果：0%(停止)、900%、1800%、2700%、3600%
    private int timeRate = 0; // 0表示停止，9表示900%，18表示1800%，以此类推

    // 预设模式：
    // 模式0: 3x3x3 (x=1, y=1, z=1)
    // 模式1: 5x3x5 (x=2, y=1, z=2)
    // 模式2: 7x3x7 (x=3, y=1, z=3)
    // 模式3: 9x3x9 (x=4, y=1, z=4)
    // 模式4: 停止工作
    private byte mode;
    private byte cachedMode = -1;

    // 停止工作模式
    private boolean isStopped = true; // 默认停止工作

    private boolean is_active = true;

    // 缓存的边界值，用于提高性能
    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax;
    private int zMax;

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

    public int getCurrentMode() {
        return this.mode;
    }

    public int getXRadius() {
        return switch (mode) {
            case 0 -> 1; // 3格范围
            case 1 -> 2; // 5格范围
            case 2 -> 3; // 7格范围
            case 3 -> 4; // 9格范围
            default -> 1;
        };
    }

    public int getYRadius() {
        // Y轴范围固定为3格（半径1）
        return 1;
    }

    public int getZRadius() {
        switch (mode) {
            case 0:
                return 1; // 3格范围
            case 1:
                return 2; // 5格范围
            case 2:
                return 3; // 7格范围
            case 3:
                return 4; // 9格范围
            default:
                return 1;
        }
    }

    // 处理右键点击事件
    public void onBlockActivated(EntityPlayer player) {
        if (!player.isSneaking()) {
            // 普通右击：切换预设模式
            if (isStopped) {
                // 如果当前是停止状态，切换回第一个范围模式
                isStopped = false;
                mode = 0;
                player.addChatComponentMessage(
                    new ChatComponentText(translateToLocal("torcherino.change_mode_area") + " 3x3x3"));
            } else {
                mode = (byte) ((mode + 1) % 5);

                // 检查是否是停止模式（模式4）
                if (mode == 4) {
                    isStopped = true;
                    player.addChatComponentMessage(new ChatComponentText(translateToLocal("torcherino.stopped")));
                } else {
                    String modeName = switch (mode) {
                        case 0 -> "3x3x3";
                        case 1 -> "5x3x5";
                        case 2 -> "7x3x7";
                        case 3 -> "9x3x9";
                        default -> "";
                    };

                    player.addChatComponentMessage(
                        new ChatComponentText(translateToLocal("torcherino.change_mode_area") + " " + modeName));
                }
            }
        } else {
            // Shift右击：切换加速效果 (0%、900%、1800%、2700%、3600%)
            timeRate = (timeRate + 9) % 45; // 0, 9, 18, 27, 36循环，对应0%、900%、1800%、2700%、3600%
            if (timeRate == 0) {
                player.addChatComponentMessage(
                    new ChatComponentText(
                        translateToLocal("torcherino.change_mode_speed") + " "
                            + translateToLocal("torcherino.stopped")));
            } else {
                player.addChatComponentMessage(
                    new ChatComponentText(
                        translateToLocal("torcherino.change_mode_speed") + " " + (timeRate * 100) + "%"));
            }
        }
    }

    @Override
    public void updateEntity() {
        if (this.worldObj.isRemote || !this.is_active || isStopped || timeRate == 0) return;

        // 只有当模式改变时才更新缓存的边界值
        if (cachedMode != mode) {
            updateCachedMode();
        }

        // 加速区域内的方块和TileEntity
        for (int x = xMin; x <= xMax; ++x) {
            for (int y = yMin; y <= yMax; ++y) {
                for (int z = zMin; z <= zMax; ++z) {
                    accelerateAtPosition(this.worldObj, x, y, z);
                }
            }
        }
    }

    private void updateCachedMode() {
        xMin = this.xCoord - getXRadius();
        yMin = this.yCoord - getYRadius();
        zMin = this.zCoord - getZRadius();
        xMax = this.xCoord + getXRadius();
        yMax = this.yCoord + getYRadius();
        zMax = this.zCoord + getZRadius();
        cachedMode = mode;
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

        // 加速TileEntity，但避免加速其他Torcherino方块以防止无限递归
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null && !(tileEntity instanceof TileTorcherinoAccelerated)
            && !(tileEntity instanceof TileCompressedTorcherino)
            && !tileEntity.isInvalid()
            && tileEntity.canUpdate()) {

            // 特殊处理GregTech机器
            if (isGregTechMachine(tileEntity)) {
                // 尝试通过反射直接加速GregTech机器的加工时间
                // 如果反射加速失败，不进行任何操作以避免跳电问题
                accelerateGregTechMachine(tileEntity, timeRate);
            } else {
                // 对于非GregTech机器，使用原有的加速方式
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

    /**
     * 检查是否为GregTech机器
     */
    private boolean isGregTechMachine(TileEntity tileEntity) {
        // 检查是否为GregTech机器
        String className = tileEntity.getClass()
            .getName();
        return className.contains("gregtech")
            && (className.contains("BaseMetaTileEntity") || className.contains("MetaTileEntity"));
    }

    /**
     * 通过反射加速GregTech机器
     * 
     * @param tileEntity GregTech机器的TileEntity
     * @param rate       加速倍率
     * @return 是否成功加速
     */
    private boolean accelerateGregTechMachine(TileEntity tileEntity, int rate) {
        try {
            // 获取mMetaTileEntity字段，这是GregTech机器的核心
            Object metaTileEntity = getMetaTileEntity(tileEntity);
            if (metaTileEntity != null) {
                // 尝试获取并修改mProgresstime字段（加工时间）
                try {
                    Field progressTimeField = getField(metaTileEntity.getClass(), "mProgresstime");
                    if (progressTimeField != null) {
                        progressTimeField.setAccessible(true);

                        int progressTime = progressTimeField.getInt(metaTileEntity);
                        // 只有当机器正在工作时才加速（progressTime > 0）
                        if (progressTime > 0) {
                            // 加速加工时间，直接增加进度值
                            // 注意：我们增加的是已用时间，而不是减少剩余时间
                            int newProgressTime = progressTime + rate;

                            progressTimeField.setInt(metaTileEntity, newProgressTime);
                            return true;
                        }
                    }
                } catch (Exception ignored) {
                    // 如果没有mProgresstime字段或操作失败，继续尝试其他方式
                }
            }
        } catch (Exception e) {
            // 如果反射失败
            return false;
        }

        return false;
    }

    /**
     * 获取MetaTileEntity对象
     */
    private Object getMetaTileEntity(TileEntity tileEntity) {
        try {
            Field metaTileEntityField = getField(tileEntity.getClass(), "mMetaTileEntity");
            if (metaTileEntityField != null) {
                metaTileEntityField.setAccessible(true);
                return metaTileEntityField.get(tileEntity);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * 递归查找字段（包括父类）
     */
    private Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null && !clazz.equals(Object.class)) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (Exception ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
