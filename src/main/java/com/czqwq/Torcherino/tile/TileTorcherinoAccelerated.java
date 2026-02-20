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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.text.DynamicKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;

public class TileTorcherinoAccelerated extends TileEntity implements IGuiHolder<PosGuiData> {

    // 用于存储每个世界的火把TileEntity的弱引用集合
    private static final Map<World, Set<WeakReference<TileTorcherinoAccelerated>>> torcherinosByWorld = new WeakHashMap<World, Set<WeakReference<TileTorcherinoAccelerated>>>();

    // 加速效果：0%、100%、200%、300%、400%
    private int timeRate = 0; // 0表示0%，1表示100%，2表示200%，以此类推

    // 独立的X、Y、Z范围（半径）
    private int xRadius = 0; // 默认1格范围 (半径0)
    private int yRadius = 0; // 默认1格范围 (半径0)
    private int zRadius = 0; // 默认1格范围 (半径0)

    private byte cachedXRadius = -1;
    private byte cachedYRadius = -1;
    private byte cachedZRadius = -1;

    // 停止工作模式
    private boolean isStopped = false;

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

    public int getXRadius() {
        return this.xRadius;
    }

    public void setXRadius(int radius) {
        this.xRadius = Math.max(0, Math.min(radius, 4)); // X最大半径4 (9格)
        this.markDirty();
    }

    public int getYRadius() {
        return this.yRadius;
    }

    public void setYRadius(int radius) {
        this.yRadius = Math.max(0, Math.min(radius, 1)); // Y最大半径1 (3格)
        this.markDirty();
    }

    public int getZRadius() {
        return this.zRadius;
    }

    public void setZRadius(int radius) {
        this.zRadius = Math.max(0, Math.min(radius, 4)); // Z最大半径4 (9格)
        this.markDirty();
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public void setStopped(boolean stopped) {
        this.isStopped = stopped;
        this.markDirty();
    }

    // Implement IGuiHolder interface
    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        ModularPanel panel = new ModularPanel("torcherino_gui");

        // Sync speed value (convert int to double for slider)
        DoubleSyncValue speedValue = new DoubleSyncValue(() -> (double) timeRate, val -> {
            timeRate = (int) Math.round(val);
            markDirty();
        });
        // Note: SliderWidget will auto-sync, so we don't call syncManager.syncValue() manually

        // Sync X radius value
        DoubleSyncValue xRadiusValue = new DoubleSyncValue(
            () -> (double) xRadius,
            val -> { setXRadius((int) Math.round(val)); });

        // Sync Y radius value
        DoubleSyncValue yRadiusValue = new DoubleSyncValue(
            () -> (double) yRadius,
            val -> { setYRadius((int) Math.round(val)); });

        // Sync Z radius value
        DoubleSyncValue zRadiusValue = new DoubleSyncValue(
            () -> (double) zRadius,
            val -> { setZRadius((int) Math.round(val)); });

        // Gray background for sliders
        Rectangle sliderBg = new Rectangle().setColor(0xFF3A3A3A);

        // Title
        panel.child(
            new TextWidget(translateToLocal("torcherino.gui.title")).left(8)
                .top(6));

        // Speed slider label
        panel.child(
            new TextWidget(translateToLocal("torcherino.gui.speed")).left(8)
                .top(22));

        // Speed slider (0-4: 0%, 100%, 200%, 300%, 400%)
        panel.child(
            new SliderWidget().value(speedValue)
                .bounds(0, 4)
                .background(sliderBg)
                .left(8)
                .top(32)
                .width(160)
                .height(10));

        // Speed display
        panel.child(
            new TextWidget(
                new DynamicKey(
                    () -> speedValue.getDoubleValue() == 0.0 ? "0%" : ((int) speedValue.getDoubleValue() * 100) + "%"))
                        .left(78)
                        .top(44));

        // X Range slider label
        panel.child(
            new TextWidget(translateToLocal("torcherino.gui.x_range")).left(8)
                .top(58));

        // X Range slider (0-4: 1-9 blocks)
        panel.child(
            new SliderWidget().value(xRadiusValue)
                .bounds(0, 4)
                .background(sliderBg)
                .left(8)
                .top(68)
                .width(160)
                .height(10));

        // X Range display
        panel.child(
            new TextWidget(new DynamicKey(() -> (int) xRadiusValue.getDoubleValue() * 2 + 1 + "")).left(78)
                .top(80));

        // Y Range slider label
        panel.child(
            new TextWidget(translateToLocal("torcherino.gui.y_range")).left(8)
                .top(94));

        // Y Range slider (0-1: 1-3 blocks)
        panel.child(
            new SliderWidget().value(yRadiusValue)
                .bounds(0, 1)
                .background(sliderBg)
                .left(8)
                .top(104)
                .width(160)
                .height(10));

        // Y Range display
        panel.child(
            new TextWidget(new DynamicKey(() -> (int) yRadiusValue.getDoubleValue() * 2 + 1 + "")).left(78)
                .top(116));

        // Z Range slider label
        panel.child(
            new TextWidget(translateToLocal("torcherino.gui.z_range")).left(8)
                .top(130));

        // Z Range slider (0-4: 1-9 blocks)
        panel.child(
            new SliderWidget().value(zRadiusValue)
                .bounds(0, 4)
                .background(sliderBg)
                .left(8)
                .top(140)
                .width(160)
                .height(10));

        // Z Range display
        panel.child(
            new TextWidget(new DynamicKey(() -> (int) zRadiusValue.getDoubleValue() * 2 + 1 + "")).left(78)
                .top(152));

        // Overall range display
        panel.child(new TextWidget(new DynamicKey(() -> {
            int x = (int) xRadiusValue.getDoubleValue() * 2 + 1;
            int y = (int) yRadiusValue.getDoubleValue() * 2 + 1;
            int z = (int) zRadiusValue.getDoubleValue() * 2 + 1;
            return x + "x" + y + "x" + z;
        })).left(8)
            .top(166));

        return panel;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        timeRate = compound.getInteger("TimeRate");
        isStopped = compound.getBoolean("IsStopped");
        is_active = compound.getBoolean("IsActive");
        xRadius = compound.getInteger("XRadius");
        yRadius = compound.getInteger("YRadius");
        zRadius = compound.getInteger("ZRadius");

        // Backwards compatibility: if old Mode exists, convert it
        if (compound.hasKey("Mode")) {
            byte oldMode = compound.getByte("Mode");
            switch (oldMode) {
                case 0:
                    xRadius = 1;
                    yRadius = 1;
                    zRadius = 1;
                    break;
                case 1:
                    xRadius = 2;
                    yRadius = 1;
                    zRadius = 2;
                    break;
                case 2:
                    xRadius = 3;
                    yRadius = 1;
                    zRadius = 3;
                    break;
                case 3:
                    xRadius = 4;
                    yRadius = 1;
                    zRadius = 4;
                    break;
                case 4:
                    isStopped = true;
                    break;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("TimeRate", timeRate);
        compound.setBoolean("IsStopped", isStopped);
        compound.setBoolean("IsActive", is_active);
        compound.setInteger("XRadius", xRadius);
        compound.setInteger("YRadius", yRadius);
        compound.setInteger("ZRadius", zRadius);
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

        // 只有当范围改变时才更新缓存的边界值
        if (cachedXRadius != xRadius || cachedYRadius != yRadius || cachedZRadius != zRadius) {
            updateCachedBounds();
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

    private void updateCachedBounds() {
        xMin = this.xCoord - xRadius;
        yMin = this.yCoord - yRadius;
        zMin = this.zCoord - zRadius;
        xMax = this.xCoord + xRadius;
        yMax = this.yCoord + yRadius;
        zMax = this.zCoord + zRadius;
        cachedXRadius = (byte) xRadius;
        cachedYRadius = (byte) yRadius;
        cachedZRadius = (byte) zRadius;
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
                try {
                    block.updateTick(world, x, y, z, new Random());
                } catch (Exception e) {
                    // 忽略加速过程中可能发生的异常，包括BiomesOPlenty中的空指针异常
                }
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
                try {
                    // 对于GregTech机器，mixin会处理能量和进度加速
                    acceleratedTile.tickAcceleration(timeRate);
                } catch (Exception e) {
                    // 忽略加速过程中可能发生的异常
                }
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
}
