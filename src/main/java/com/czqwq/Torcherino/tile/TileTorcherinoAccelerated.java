package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Torcherino;

/**
 * ITileEntityTickAcceleration接口用于支持特定TileEntity的时间加速功能
 * 只有实现了此接口的TileEntity才能享受精准的时间加速，而不影响能耗速度
 */
interface ITileEntityTickAcceleration {

    /**
     * <li>true if the tickAcceleration logic should be executed.</li>
     * <li>false if the default TileEntity update method should proceed.</li>
     */
    boolean tickAcceleration(int tickAcceleratedRate);

    /**
     * adaptation to other aspects of the tileEntity
     */
    default int getTickAcceleratedRate() {
        return 1;
    }
}

public class TileTorcherinoAccelerated extends TileEntity {

    // 用于存储每个世界的火把TileEntity的弱引用集合
    private static final Map<World, Set<WeakReference<TileTorcherinoAccelerated>>> torcherinosByWorld = new WeakHashMap<World, Set<WeakReference<TileTorcherinoAccelerated>>>();

    // 加速效果：0%、100%、200%、300%、400%
    private int timeRate = 0; // 0表示0%，1表示100%，2表示200%，以此类推

    // 预设模式：
    // 模式0: 3x3x3 (x=1, y=1, z=1)
    // 模式1: 5x3x5 (x=2, y=1, z=2)
    // 模式2: 7x3x7 (x=3, y=1, z=3)
    // 模式3: 9x3x9 (x=4, y=1, z=4)
    // 模式4: 停止工作
    private byte mode;
    private byte cachedMode = -1;

    // 停止工作模式
    private boolean isStopped = true;

    private boolean is_active = true;

    // 缓存的边界值，用于提高性能
    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax;
    private int zMax;

    // 添加防止递归调用的字段
    private static long currentTick = 0;
    private long lastTickProcessed = 0;

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

    public int getYRadius() {
        // Y轴范围固定为3格（半径1）
        return 1;
    }

    public int getZRadius() {
        return switch (mode) {
            case 0 -> 1; // 3格范围
            case 1 -> 2; // 5格范围
            case 2 -> 3; // 7格范围
            case 3 -> 4; // 9格范围
            default -> 1;
        };
    }

    public boolean isStopped() {
        return this.isStopped;
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
            // Shift右击：切换加速效果 (0%、100%、200%、300%、400%)
            timeRate = (timeRate + 1) % 5; // 0-4循环，对应0%、100%、200%、300%、400%
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
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        mode = compound.getByte("Mode");
        timeRate = compound.getInteger("TimeRate");
        isStopped = compound.getBoolean("IsStopped");
        is_active = compound.getBoolean("IsActive");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setByte("Mode", mode);
        compound.setInteger("TimeRate", timeRate);
        compound.setBoolean("IsStopped", isStopped);
        compound.setBoolean("IsActive", is_active);
    }

    @Override
    public void updateEntity() {
        // 将当前TileEntity添加到世界集合中
        this.addToWorldSet();

        // 更新当前tick计数
        if (currentTick != this.worldObj.getTotalWorldTime()) {
            currentTick = this.worldObj.getTotalWorldTime();
            // 清理无效的弱引用
            this.cleanupInvalidReferences();
        }

        // 检查是否已经在这个tick处理过，防止递归调用
        if (this.lastTickProcessed == currentTick) {
            return;
        }

        // 标记这个tick已经处理过
        this.lastTickProcessed = currentTick;

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

    @Override
    public void invalidate() {
        // 当TileEntity失效时（如被破坏），从世界集合中移除
        this.removeFromWorldSet();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        // 当区块卸载时，从世界集合中移除
        this.removeFromWorldSet();
        super.onChunkUnload();
    }

    /**
     * 将当前TileEntity添加到世界集合中
     */
    private void addToWorldSet() {
        if (this.worldObj == null) return;

        synchronized (torcherinosByWorld) {
            Set<WeakReference<TileTorcherinoAccelerated>> torcherinos = torcherinosByWorld
                .computeIfAbsent(this.worldObj, k -> new HashSet<>());

            // 检查是否已经存在该TileEntity的引用
            boolean exists = false;
            for (WeakReference<TileTorcherinoAccelerated> ref : torcherinos) {
                TileTorcherinoAccelerated torcherino = ref.get();
                if (torcherino == this) {
                    exists = true;
                    break;
                }
            }

            // 如果不存在，则添加新的引用
            if (!exists) {
                torcherinos.add(new WeakReference<TileTorcherinoAccelerated>(this));
            }
        }
    }

    /**
     * 从世界集合中移除当前TileEntity
     */
    private void removeFromWorldSet() {
        if (this.worldObj == null) return;

        synchronized (torcherinosByWorld) {
            Set<WeakReference<TileTorcherinoAccelerated>> torcherinos = torcherinosByWorld.get(this.worldObj);
            if (torcherinos != null) {
                Iterator<WeakReference<TileTorcherinoAccelerated>> iterator = torcherinos.iterator();
                while (iterator.hasNext()) {
                    WeakReference<TileTorcherinoAccelerated> ref = iterator.next();
                    TileTorcherinoAccelerated torcherino = ref.get();
                    if (torcherino == this || torcherino == null) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * 清理无效的弱引用
     */
    private void cleanupInvalidReferences() {
        if (this.worldObj == null) return;

        synchronized (torcherinosByWorld) {
            Set<WeakReference<TileTorcherinoAccelerated>> torcherinos = torcherinosByWorld.get(this.worldObj);
            if (torcherinos != null) {
                Iterator<WeakReference<TileTorcherinoAccelerated>> iterator = torcherinos.iterator();
                while (iterator.hasNext()) {
                    WeakReference<TileTorcherinoAccelerated> ref = iterator.next();
                    if (ref.get() == null) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * 获取指定世界中的所有有效火把TileEntity
     */
    public static Set<TileTorcherinoAccelerated> getTorcherinosInWorld(World world) {
        Set<TileTorcherinoAccelerated> result = new HashSet<TileTorcherinoAccelerated>();

        synchronized (torcherinosByWorld) {
            Set<WeakReference<TileTorcherinoAccelerated>> torcherinos = torcherinosByWorld.get(world);
            if (torcherinos != null) {
                Iterator<WeakReference<TileTorcherinoAccelerated>> iterator = torcherinos.iterator();
                while (iterator.hasNext()) {
                    WeakReference<TileTorcherinoAccelerated> ref = iterator.next();
                    TileTorcherinoAccelerated torcherino = ref.get();
                    if (torcherino != null) {
                        result.add(torcherino);
                    } else {
                        iterator.remove();
                    }
                }
            }
        }

        return result;
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

            // 检查TileEntity是否实现了ITileEntityTickAcceleration接口
            // 如果实现了该接口，优先使用tickAcceleration方法进行加速
            if (tileEntity instanceof ITileEntityTickAcceleration) {
                ITileEntityTickAcceleration acceleratedTile = (ITileEntityTickAcceleration) tileEntity;
                // 直接调用加速方法，不重复调用updateEntity
                acceleratedTile.tickAcceleration(timeRate);
            }
            // 特殊处理GregTech机器
            else if (Torcherino.hasGregTech && isGregTechMachine(tileEntity)) {
                // 直接加速GregTech机器，而不是使用反射
                accelerateGregTechMachine(tileEntity, timeRate);
            } else {
                // 对于没有实现ITileEntityTickAcceleration接口的传统TileEntity，使用原有的加速方式
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
        if (Torcherino.hasGregTech) {
            try {
                // 尝试使用反射检查是否为GregTech机器
                Class<?> baseMetaTileEntityClass = Class.forName("gregtech.api.metatileentity.BaseMetaTileEntity");
                boolean result = baseMetaTileEntityClass.isInstance(tileEntity);
                // if (result) {
                // Torcherino.LOG.debug(
                // "Identified GregTech machine: {}",
                // tileEntity.getClass()
                // .getName());
                // }
                return result;
            } catch (ClassNotFoundException e) {
                // 如果找不到类，则使用原来的字符串匹配方式
                String className = tileEntity.getClass()
                    .getName();
                boolean result = className.contains("gregtech")
                    && (className.contains("BaseMetaTileEntity") || className.contains("MetaTileEntity"));
                if (result) {
                    // Torcherino.LOG.debug("Identified GregTech machine by name matching: {}", className);
                }
                return result;
            }
        }
        return false;
    }

    /**
     * 加速GregTech机器
     *
     * @param tileEntity GregTech机器的TileEntity
     * @param rate       加速倍率
     * @return 是否成功加速
     */
    private boolean accelerateGregTechMachine(TileEntity tileEntity, int rate) {
        if (Torcherino.hasGregTech) {
            try {
                // 使用反射处理GregTech机器
                Class<?> baseMetaTileEntityClass = Class.forName("gregtech.api.metatileentity.BaseMetaTileEntity");
                Class<?> commonMetaTileEntityClass = Class.forName("gregtech.api.metatileentity.CommonMetaTileEntity");
                Class<?> multiBlockBaseClass = Class
                    .forName("gregtech.api.metatileentity.implementations.MTEMultiBlockBase");

                if (baseMetaTileEntityClass.isInstance(tileEntity)) {
                    // 获取getMetaTileEntity方法
                    java.lang.reflect.Method getMetaTileEntityMethod = baseMetaTileEntityClass
                        .getMethod("getMetaTileEntity");
                    Object metaTileEntity = getMetaTileEntityMethod.invoke(tileEntity);

                    if (metaTileEntity == null) {
                        // Torcherino.LOG.debug(
                        // "MetaTileEntity is null for tile entity: {}",
                        // tileEntity.getClass()
                        // .getName());
                        return false;
                    }

                    // 检查是否为CommonMetaTileEntity及其子类
                    if (commonMetaTileEntityClass.isInstance(metaTileEntity)) {
                        // 获取getProgresstime方法
                        java.lang.reflect.Method getProgresstimeMethod = commonMetaTileEntityClass
                            .getMethod("getProgresstime");
                        int progressTime = (Integer) getProgresstimeMethod.invoke(metaTileEntity);

                        // Torcherino.LOG.debug("GregTech machine progress time: {}", progressTime);

                        // 只有当机器正在工作时才加速（progressTime > 0）
                        if (progressTime > 0) {
                            // 检查是否为多方块机器
                            if (multiBlockBaseClass.isInstance(metaTileEntity)) {
                                // 直接增加mProgresstime字段值（在metaTileEntity的实际类中）
                                java.lang.reflect.Field progressTimeField = metaTileEntity.getClass()
                                    .getField("mProgresstime");
                                int newProgressTime = progressTime + rate;
                                progressTimeField.setInt(metaTileEntity, newProgressTime);
                                // Torcherino.LOG.debug("Accelerated multi-block GregTech machine by {} ticks", rate);
                            } else {
                                // 其他机器使用increaseProgress方法
                                try {
                                    java.lang.reflect.Method increaseProgressMethod = commonMetaTileEntityClass
                                        .getMethod("increaseProgress", int.class);
                                    increaseProgressMethod.invoke(metaTileEntity, rate);
                                    // Torcherino.LOG
                                    // .debug("Accelerated GregTech machine using increaseProgress by {} ticks", rate);
                                } catch (NoSuchMethodException e) {
                                    // 如果找不到increaseProgress方法，直接增加进度（在metaTileEntity的实际类中）
                                    // Torcherino.LOG.debug(
                                    // "increaseProgress method not found, falling back to direct field access");
                                    java.lang.reflect.Field progressTimeField = metaTileEntity.getClass()
                                        .getField("mProgresstime");
                                    int newProgressTime = progressTime + rate;
                                    progressTimeField.setInt(metaTileEntity, newProgressTime);
                                    // Torcherino.LOG
                                    // .debug("Accelerated GregTech machine by direct field access by {} ticks", rate);
                                }
                            }
                            return true;
                        } else {
                            // Torcherino.LOG.debug("GregTech machine is not currently working (progressTime <= 0)");
                        }
                    } else {
                        // Torcherino.LOG.debug(
                        // "MetaTileEntity is not a CommonMetaTileEntity: {}",
                        // metaTileEntity.getClass()
                        // .getName());
                    }
                } else {
                    // Torcherino.LOG.debug(
                    // "TileEntity is not a BaseMetaTileEntity: {}",
                    // tileEntity.getClass()
                    // .getName());
                }
            } catch (Exception e) {
                // 如果反射失败，记录错误并返回false
                // Torcherino.LOG.error("Error accelerating GregTech machine: ", e);
                return false;
            }
        } else {
            // Torcherino.LOG.debug("GregTech is not loaded");
        }

        return false;
    }
}
