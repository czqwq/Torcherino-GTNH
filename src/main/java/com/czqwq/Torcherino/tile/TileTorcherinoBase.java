package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.text.DynamicKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.api.interfaces.ITorcherinoTile;
import com.czqwq.Torcherino.util.AccelerationHelper;

/**
 * Abstract base for GUI-operated Torcherino tiles (Accelerated, Compressed, DoubleCompressed).
 * Provides unified field management, NBT serialization, world tracking, GUI building,
 * and acceleration logic via {@link AccelerationHelper}.
 * <p>
 * Subclasses only need to provide {@link #getSpeedMultiplier()} and {@link #getGuiTitleKey()}.
 */
public abstract class TileTorcherinoBase extends TileEntity implements IGuiHolder<PosGuiData>, ITorcherinoTile {

    // ========== World tracking (shared across all torch types) ==========
    private static final Map<World, Set<WeakReference<TileTorcherinoBase>>> torchesByWorld = new WeakHashMap<>();

    private static long globalCurrentTick = 0;

    // ========== Instance fields ==========
    /** Speed slider level (0 to maxSpeedLevel). Effective speed = speedLevel * getSpeedMultiplier(). */
    protected int speedLevel = 0;

    protected int xRadius = 0;
    protected int yRadius = 0;
    protected int zRadius = 0;

    protected boolean isStopped = false;
    protected boolean is_active = true;

    // Cached bounds
    private byte cachedXRadius = -1;
    private byte cachedYRadius = -1;
    private byte cachedZRadius = -1;

    protected int xMin, yMin, zMin, xMax, yMax, zMax;

    // Recursion prevention
    private long lastTickProcessed = 0;

    // ========== ITorcherinoTile implementation ==========

    @Override
    public boolean getActive() {
        return this.is_active;
    }

    @Override
    public void setActive(boolean active) {
        this.is_active = active;
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
        return this.xRadius;
    }

    @Override
    public int getYRadius() {
        return this.yRadius;
    }

    @Override
    public int getZRadius() {
        return this.zRadius;
    }

    @Override
    public boolean isStopped() {
        return this.isStopped;
    }

    @Override
    public int getEffectiveSpeed() {
        return speedLevel * getSpeedMultiplier();
    }

    // ========== Abstract methods for subclasses ==========

    /**
     * @return the speed multiplier for this torch tier.
     *         1 for normal, 9 for compressed, 81 for double compressed.
     */
    protected abstract int getSpeedMultiplier();

    /**
     * @return the GUI title translation key (e.g. "torcherino.gui.title").
     */
    protected abstract String getGuiTitleKey();

    /**
     * @return the GUI panel ID (unique per torch type).
     */
    protected abstract String getGuiPanelId();

    // ========== Getters / Setters ==========

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void setSpeedLevel(int level) {
        this.speedLevel = Math.max(0, Math.min(level, Config.maxSpeedLevel));
        this.markDirty();
    }

    public void setXRadius(int radius) {
        this.xRadius = Math.max(0, Math.min(radius, Config.maxXRadius));
        this.markDirty();
    }

    public void setYRadius(int radius) {
        this.yRadius = Math.max(0, Math.min(radius, Config.maxYRadius));
        this.markDirty();
    }

    public void setZRadius(int radius) {
        this.zRadius = Math.max(0, Math.min(radius, Config.maxZRadius));
        this.markDirty();
    }

    public void setStopped(boolean stopped) {
        this.isStopped = stopped;
        this.markDirty();
    }

    // ========== GUI (buildUI) ==========

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        ModularPanel panel = new ModularPanel(getGuiPanelId());

        int multiplier = getSpeedMultiplier();

        // Speed sync value (slider 0..maxSpeedLevel → effective speed = slider * multiplier)
        // Getters clamp to config bounds so slider handles reduced configs correctly
        DoubleSyncValue speedValue = new DoubleSyncValue(
            () -> (double) Math.max(0, Math.min(speedLevel, Config.maxSpeedLevel)),
            val -> {
                speedLevel = (int) Math.round(val);
                markDirty();
            }).allowC2S();

        // X radius (clamped to current config max)
        DoubleSyncValue xRadiusValue = new DoubleSyncValue(
            () -> (double) Math.max(0, Math.min(xRadius, Config.maxXRadius)),
            val -> { setXRadius((int) Math.round(val)); }).allowC2S();

        // Y radius (clamped to current config max)
        DoubleSyncValue yRadiusValue = new DoubleSyncValue(
            () -> (double) Math.max(0, Math.min(yRadius, Config.maxYRadius)),
            val -> { setYRadius((int) Math.round(val)); }).allowC2S();

        // Z radius (clamped to current config max)
        DoubleSyncValue zRadiusValue = new DoubleSyncValue(
            () -> (double) Math.max(0, Math.min(zRadius, Config.maxZRadius)),
            val -> { setZRadius((int) Math.round(val)); }).allowC2S();

        Rectangle sliderBg = new Rectangle().color(0xFF3A3A3A);

        // ---- Title ----
        panel.child(
            new TextWidget<>(translateToLocal(getGuiTitleKey())).left(8)
                .top(6));

        // ---- Speed slider ----
        panel.child(
            new TextWidget<>(translateToLocal("torcherino.gui.speed")).left(8)
                .top(22));
        panel.child(
            new SliderWidget().value(speedValue)
                .bounds(0, Config.maxSpeedLevel)
                .background(sliderBg)
                .left(8)
                .top(32)
                .width(160)
                .height(10));
        panel.child(
            new TextWidget<>(
                new DynamicKey(
                    () -> IKey.str(
                        speedValue.getDoubleValue() == 0.0 ? "0%"
                            : ((int) speedValue.getDoubleValue() * multiplier * 100) + "%"))).left(78)
                                .top(44));

        // ---- X Range ----
        panel.child(
            new TextWidget<>(translateToLocal("torcherino.gui.x_range")).left(8)
                .top(58));
        panel.child(
            new SliderWidget().value(xRadiusValue)
                .bounds(0, Config.maxXRadius)
                .background(sliderBg)
                .left(8)
                .top(68)
                .width(160)
                .height(10));
        panel.child(
            new TextWidget<>(new DynamicKey(() -> IKey.str((int) xRadiusValue.getDoubleValue() * 2 + 1 + ""))).left(78)
                .top(80));

        // ---- Y Range ----
        panel.child(
            new TextWidget<>(translateToLocal("torcherino.gui.y_range")).left(8)
                .top(94));
        panel.child(
            new SliderWidget().value(yRadiusValue)
                .bounds(0, Config.maxYRadius)
                .background(sliderBg)
                .left(8)
                .top(104)
                .width(160)
                .height(10));
        panel.child(
            new TextWidget<>(new DynamicKey(() -> IKey.str((int) yRadiusValue.getDoubleValue() * 2 + 1 + ""))).left(78)
                .top(116));

        // ---- Z Range ----
        panel.child(
            new TextWidget<>(translateToLocal("torcherino.gui.z_range")).left(8)
                .top(130));
        panel.child(
            new SliderWidget().value(zRadiusValue)
                .bounds(0, Config.maxZRadius)
                .background(sliderBg)
                .left(8)
                .top(140)
                .width(160)
                .height(10));
        panel.child(
            new TextWidget<>(new DynamicKey(() -> IKey.str((int) zRadiusValue.getDoubleValue() * 2 + 1 + ""))).left(78)
                .top(152));

        // ---- Overall range ----
        panel.child(new TextWidget<>(new DynamicKey(() -> {
            int x = (int) xRadiusValue.getDoubleValue() * 2 + 1;
            int y = (int) yRadiusValue.getDoubleValue() * 2 + 1;
            int z = (int) zRadiusValue.getDoubleValue() * 2 + 1;
            return IKey.str(x + "x" + y + "x" + z);
        })).left(8)
            .top(166));

        return panel;
    }

    // ========== NBT ==========

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        speedLevel = clampInt(compound.getInteger("SpeedLevel"), 0, Config.maxSpeedLevel);
        isStopped = compound.getBoolean("IsStopped");
        is_active = compound.getBoolean("IsActive");
        xRadius = clampInt(compound.getInteger("XRadius"), 0, Config.maxXRadius);
        yRadius = clampInt(compound.getInteger("YRadius"), 0, Config.maxYRadius);
        zRadius = clampInt(compound.getInteger("ZRadius"), 0, Config.maxZRadius);

        // Backwards compatibility: convert old TimeRate to new speedLevel
        if (compound.hasKey("TimeRate") && !compound.hasKey("SpeedLevel")) {
            int oldTimeRate = compound.getInteger("TimeRate");
            speedLevel = clampInt(oldTimeRate / getSpeedMultiplier(), 0, Config.maxSpeedLevel);
        }

        // Backwards compatibility: old Mode field
        if (compound.hasKey("Mode")) {
            byte oldMode = compound.getByte("Mode");
            switch (oldMode) {
                case 0:
                    xRadius = clampInt(1, 0, Config.maxXRadius);
                    yRadius = clampInt(1, 0, Config.maxYRadius);
                    zRadius = clampInt(1, 0, Config.maxZRadius);
                    break;
                case 1:
                    xRadius = clampInt(2, 0, Config.maxXRadius);
                    yRadius = clampInt(1, 0, Config.maxYRadius);
                    zRadius = clampInt(2, 0, Config.maxZRadius);
                    break;
                case 2:
                    xRadius = clampInt(3, 0, Config.maxXRadius);
                    yRadius = clampInt(1, 0, Config.maxYRadius);
                    zRadius = clampInt(3, 0, Config.maxZRadius);
                    break;
                case 3:
                    xRadius = clampInt(4, 0, Config.maxXRadius);
                    yRadius = clampInt(1, 0, Config.maxYRadius);
                    zRadius = clampInt(4, 0, Config.maxZRadius);
                    break;
                case 4:
                    isStopped = true;
                    break;
            }
        }
    }

    /** Clamp an int value between min (inclusive) and max (inclusive). */
    private static int clampInt(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("SpeedLevel", speedLevel);
        compound.setBoolean("IsStopped", isStopped);
        compound.setBoolean("IsActive", is_active);
        compound.setInteger("XRadius", xRadius);
        compound.setInteger("YRadius", yRadius);
        compound.setInteger("ZRadius", zRadius);
    }

    // ========== Tick / Acceleration ==========

    @Override
    public void updateEntity() {
        // Register in world tracking
        addToWorldSet();

        // Update global tick counter and cleanup stale references
        if (globalCurrentTick != this.worldObj.getTotalWorldTime()) {
            globalCurrentTick = this.worldObj.getTotalWorldTime();
            cleanupInvalidReferences();
        }

        // Prevent recursion: only process once per tick
        if (this.lastTickProcessed == globalCurrentTick) {
            return;
        }
        this.lastTickProcessed = globalCurrentTick;

        // Early exit checks
        if (this.worldObj.isRemote || !this.is_active || isStopped || speedLevel == 0) return;

        int effectiveSpeed = getEffectiveSpeed();
        if (effectiveSpeed <= 0) return;

        // Update cached bounds if needed
        if (cachedXRadius != (byte) xRadius || cachedYRadius != (byte) yRadius || cachedZRadius != (byte) zRadius) {
            updateCachedBounds();
        }

        // Accelerate all positions in range
        for (int x = xMin; x <= xMax; ++x) {
            for (int y = yMin; y <= yMax; ++y) {
                for (int z = zMin; z <= zMax; ++z) {
                    AccelerationHelper.accelerateAtPosition(
                        this.worldObj,
                        this.xCoord,
                        this.yCoord,
                        this.zCoord,
                        effectiveSpeed,
                        x,
                        y,
                        z);
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

    // ========== World set tracking ==========

    @Override
    public void invalidate() {
        removeFromWorldSet();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        removeFromWorldSet();
        super.onChunkUnload();
    }

    private void addToWorldSet() {
        if (this.worldObj == null) return;
        synchronized (torchesByWorld) {
            Set<WeakReference<TileTorcherinoBase>> torches = torchesByWorld
                .computeIfAbsent(this.worldObj, k -> new HashSet<>());
            boolean exists = false;
            for (WeakReference<TileTorcherinoBase> ref : torches) {
                if (ref.get() == this) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                torches.add(new WeakReference<>(this));
            }
        }
    }

    private void removeFromWorldSet() {
        if (this.worldObj == null) return;
        synchronized (torchesByWorld) {
            Set<WeakReference<TileTorcherinoBase>> torches = torchesByWorld.get(this.worldObj);
            if (torches != null) {
                torches.removeIf(ref -> ref.get() == this || ref.get() == null);
            }
        }
    }

    private void cleanupInvalidReferences() {
        if (this.worldObj == null) return;
        synchronized (torchesByWorld) {
            Set<WeakReference<TileTorcherinoBase>> torches = torchesByWorld.get(this.worldObj);
            if (torches != null) {
                torches.removeIf(ref -> ref.get() == null);
            }
        }
    }

    /**
     * Get all active GUI-type torches in the given world.
     */
    public static Set<TileTorcherinoBase> getTorchesInWorld(World world) {
        Set<TileTorcherinoBase> result = new HashSet<>();
        synchronized (torchesByWorld) {
            Set<WeakReference<TileTorcherinoBase>> torches = torchesByWorld.get(world);
            if (torches != null) {
                for (Iterator<WeakReference<TileTorcherinoBase>> it = torches.iterator(); it.hasNext();) {
                    TileTorcherinoBase torch = it.next()
                        .get();
                    if (torch != null) {
                        result.add(torch);
                    } else {
                        it.remove();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the current global tick. Used by AccelerationHelper for overlap detection.
     */
    public static long getGlobalTick() {
        return globalCurrentTick;
    }
}
