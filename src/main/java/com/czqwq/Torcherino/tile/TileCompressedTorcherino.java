package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.text.DynamicKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;

public class TileCompressedTorcherino extends TileEntity implements IGuiHolder<PosGuiData> {

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

    public void setMode(int newMode) {
        this.mode = (byte) newMode;
        this.isStopped = (newMode == 4);
        this.markDirty();
    }

    // Implement IGuiHolder interface
    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        ModularPanel panel = new ModularPanel("compressed_torcherino_gui");

        // Compressed Torcherino: Speed values are 0, 9, 18, 27, 36 (0%, 900%, 1800%, 2700%, 3600%)
        // Sync speed value (convert int to double for slider, scale by 9)
        DoubleSyncValue speedValue = new DoubleSyncValue(() -> (double) (timeRate / 9), val -> {
            timeRate = (int) Math.round(val) * 9;
            markDirty();
        });
        syncManager.syncValue("speed", speedValue);

        // Sync mode value (convert byte to double for slider)
        DoubleSyncValue modeValue = new DoubleSyncValue(
            () -> (double) mode,
            val -> { setMode((int) Math.round(val)); });
        syncManager.syncValue("mode", modeValue);

        // Title
        panel.child(
            new TextWidget(translateToLocal("torcherino.gui.title")).left(8)
                .top(6));

        // Speed slider label
        panel.child(
            new TextWidget(translateToLocal("torcherino.gui.speed")).left(8)
                .top(25));

        // Speed slider (0-4: 0%, 900%, 1800%, 2700%, 3600%)
        panel.child(
            new SliderWidget().value(speedValue)
                .bounds(0, 4)
                .left(8)
                .top(37)
                .width(160)
                .height(10));

        // Speed display
        panel.child(
            new TextWidget(
                new DynamicKey(
                    () -> speedValue.getDoubleValue() == 0.0 ? "0%" : ((int) speedValue.getDoubleValue() * 900) + "%"))
                        .left(78)
                        .top(50));

        // Range slider label
        panel.child(
            new TextWidget(translateToLocal("torcherino.gui.range")).left(8)
                .top(62));

        // Range slider (0-4: 3x3x3, 5x3x5, 7x3x7, 9x3x9, Stopped)
        panel.child(
            new SliderWidget().value(modeValue)
                .bounds(0, 4)
                .left(8)
                .top(74)
                .width(160)
                .height(10));

        return panel;
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
