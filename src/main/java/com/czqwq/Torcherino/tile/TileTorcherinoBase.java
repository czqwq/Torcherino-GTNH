package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.text.DynamicKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.api.interfaces.ITorcherinoTile;
import com.czqwq.Torcherino.util.AccelerationHelper;

/**
 * Abstract base for GUI-operated Torcherino tiles (Accelerated, Compressed, DoubleCompressed).
 * Provides unified field management, NBT serialization, world tracking, GUI building,
 * and acceleration logic via {@link AccelerationHelper}.
 * <p>
 * The GUI also hosts a toggle for an in-world preview of the working area. The flag is persisted in
 * NBT and synced to the client with the vanilla description packet, so the preview can be drawn by
 * the client-side renderer even before the GUI is opened for the first time.
 * <p>
 * Subclasses only need to provide {@link #getSpeedMultiplier()} and {@link #getGuiTitleKey()}.
 */
public abstract class TileTorcherinoBase extends TileEntity implements IGuiHolder<PosGuiData>, ITorcherinoTile {

    // ========== Instance fields ==========
    /** Speed slider level (0 to maxSpeedLevel). Effective speed = speedLevel * getSpeedMultiplier(). */
    protected int speedLevel = 0;

    protected int xRadius = 0;
    protected int yRadius = 0;
    protected int zRadius = 0;

    protected boolean isStopped = false;
    protected boolean is_active = true;

    /**
     * Whether the working-area preview is drawn in the world. Persisted in NBT and synced to the
     * client through the vanilla description packet.
     */
    protected boolean showRangePreview = false;

    // Cached bounds
    private byte cachedXRadius = -1;
    private byte cachedYRadius = -1;
    private byte cachedZRadius = -1;

    protected int xMin, yMin, zMin, xMax, yMax, zMax;

    // Recursion prevention
    private long lastTickProcessed = 0;

    /**
     * Where the next area sweep starts, as a flat position index (see {@link #applyAreaEffect}).
     * Advanced only when the per-tick time budget runs out, so the work a torch may spend per tick
     * stays capped while every position is still reached within a few ticks. Not persisted: after a
     * reload the sweep simply starts over at the first position.
     */
    private long areaWalkOffset = 0;

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
     * Applies this torch's effect over its full configured area once per tick.
     * <p>
     * The default implementation accelerates every position in range.
     * Subclasses that decelerate override this to register deceleration zones instead.
     * <p>
     * The sweep is <b>round-robin</b>: it starts at {@link #areaWalkOffset} and wraps around, so
     * when the per-torch-tick time budget ({@link Config#tickBudgetNanos}) is exhausted the rest of
     * the area is served on the following ticks instead of being starved. Previously the sweep
     * always restarted at {@code xMin/yMin/zMin} and returned on the first over-budget position,
     * which left the same tail of the area (in x→y→z order) unaccelerated every single tick, with
     * no log line — machines that try to dodge the budget by being cheap (GregTech machines only
     * get their {@code mProgresstime} written) starved behind expensive neighbours such as an
     * Advanced Assembly Line (its own loop is capped at 1 ms), random-ticking crops or re-ticked
     * vanilla tile entities. The budget itself is unchanged: it still caps what one torch spends
     * per tick, the cap is just shared fairly over time.
     * <p>
     * The order of positions within one full sweep is unchanged (x outer, then y, then z), so
     * overlap detection and every per-position behaviour stay exactly as before.
     *
     * @param effectiveSpeed the effective speed (slider level × tier multiplier)
     */
    protected void applyAreaEffect(int effectiveSpeed) {
        // Flat x-major index space of the current bounds. Sizes are ≥ 1 because radii are clamped
        // to [0, config max] when they are read from NBT and set through the GUI; the guards below
        // only exist so a hand-edited config cannot turn the index math into a silent no-op.
        final long sizeY = (long) yMax - yMin + 1L;
        final long sizeZ = (long) zMax - zMin + 1L;
        final long areaSize = ((long) xMax - xMin + 1L) * sizeY * sizeZ;
        if (sizeY <= 0L || sizeZ <= 0L || areaSize <= 0L) return;

        // Time budget for this whole torch tick (NOT per position — see AccelerationHelper).
        // Created lazily: an area whose first positions are all air must not even read the clock.
        long tickDeadline = AccelerationHelper.NO_DEADLINE;
        boolean deadlineArmed = false;

        long index = this.areaWalkOffset % areaSize;
        for (long processed = 0; processed < areaSize; processed++) {
            long slot = index;
            final int z = zMin + (int) (slot % sizeZ);
            slot /= sizeZ;
            final int y = yMin + (int) (slot % sizeY);
            final int x = xMin + (int) (slot / sizeY);

            if (!deadlineArmed) {
                tickDeadline = AccelerationHelper.newTickDeadline();
                deadlineArmed = true;
            } else if (System.nanoTime() > tickDeadline) {
                // Budget spent — resume here next tick. Do NOT return without saving the offset:
                // that is what used to starve everything after this position, forever.
                this.areaWalkOffset = index;
                return;
            }

            AccelerationHelper.accelerateAtPosition(
                this.worldObj,
                this.xCoord,
                this.yCoord,
                this.zCoord,
                effectiveSpeed,
                x,
                y,
                z,
                tickDeadline);

            if (++index >= areaSize) {
                index = 0L;
            }
        }

        // The whole area fitted into this tick's budget — start over from the first position.
        this.areaWalkOffset = 0L;
    }

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

    public boolean isRangePreviewShown() {
        return this.showRangePreview;
    }

    /**
     * Enables/disables the working-area preview. On the server this also persists the flag and
     * pushes it to the clients tracking this tile; the client-side flag is updated by the GUI sync
     * value or, outside of an open GUI, by the description packet.
     */
    public void setRangePreviewShown(boolean shown) {
        if (this.showRangePreview == shown) return;
        this.showRangePreview = shown;
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.markDirty();
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    // ========== GUI (buildUI) ==========

    @Override
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Torcherino.MODID, mainPanel);
    }

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

        // Working-area preview toggle. The flag lives on the tile, so the button state, the
        // persisted NBT and the in-world preview always agree; allowC2S lets the client flip it.
        BooleanSyncValue previewValue = new BooleanSyncValue(this::isRangePreviewShown, this::setRangePreviewShown)
            .allowC2S();
        syncManager.syncValue("rangePreview", previewValue);

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

        // ---- Working-area preview toggle (top-right corner) ----
        panel.child(
            new ToggleButton().value(previewValue)
                .background(false, GuiTextures.MC_BUTTON)
                .background(true, GuiTextures.MC_BUTTON_PRESSED)
                .hoverBackground(false, GuiTextures.MC_BUTTON_HOVERED)
                .hoverBackground(true, GuiTextures.MC_BUTTON_HOVERED_PRESSED)
                .overlay(false, GuiTextures.CHECK_BOX_EMPTY)
                .overlay(true, GuiTextures.CHECK_BOX_FULL)
                .addTooltip(false, translateToLocal("torcherino.gui.range_preview.off"))
                .addTooltip(true, translateToLocal("torcherino.gui.range_preview.on"))
                .size(16, 16)
                .right(6)
                .top(6));

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
        showRangePreview = compound.getBoolean("ShowRangePreview");

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
        compound.setBoolean("ShowRangePreview", showRangePreview);
    }

    // ========== Client sync / rendering ==========

    /**
     * Pushes the tile data (including the preview flag) to the client, so the client-side renderer
     * can draw the preview before the GUI is ever opened and after a world reload.
     */
    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, this.blockMetadata, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    /**
     * @return the world-space box enclosing every block this torch affects. The corners are whole
     *         block corners, so the box wraps the outermost affected blocks as well.
     */
    public AxisAlignedBB getWorkAreaBounds() {
        return AxisAlignedBB.getBoundingBox(
            this.xCoord - this.xRadius,
            this.yCoord - this.yRadius,
            this.zCoord - this.zRadius,
            this.xCoord + this.xRadius + 1,
            this.yCoord + this.yRadius + 1,
            this.zCoord + this.zRadius + 1);
    }

    /**
     * The preview box is much larger than the torch collision box, so the special renderer must not
     * be frustum-culled by the tiny torch shape while the preview is enabled.
     */
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return this.showRangePreview ? getWorkAreaBounds() : super.getRenderBoundingBox();
    }

    // ========== Tick / Acceleration ==========

    @Override
    public void updateEntity() {
        // Prevent recursion: only process once per tick
        final long currentTick = this.worldObj.getTotalWorldTime();
        if (this.lastTickProcessed == currentTick) {
            return;
        }
        this.lastTickProcessed = currentTick;

        // Early exit checks
        if (this.worldObj.isRemote || !this.is_active || isStopped || speedLevel == 0) return;

        int effectiveSpeed = getEffectiveSpeed();
        if (effectiveSpeed <= 0) return;

        // Update cached bounds if needed
        if (cachedXRadius != (byte) xRadius || cachedYRadius != (byte) yRadius || cachedZRadius != (byte) zRadius) {
            updateCachedBounds();
        }

        applyAreaEffect(effectiveSpeed);
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
}
