package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.text.DynamicKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.api.interfaces.ITorcherinoTile;
import com.czqwq.Torcherino.util.AccelerationHelper;
import com.czqwq.Torcherino.util.BoundMachineEntry;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

/**
 * Abstract base for wireless (flash-bound) Torcherino tiles.
 * Accelerates only explicitly bound TE blocks, not area-based.
 * Supports per-machine speed overrides via GUI.
 * <p>
 * Subclasses only need to provide {@link #getSpeedMultiplier()}, {@link #getGuiTitleKey()},
 * and {@link #getGuiPanelId()}.
 */
public abstract class TileWirelessTorcherinoBase extends TileEntity
    implements IGuiHolder<PosGuiData>, ITorcherinoTile, IWailaDataProvider {

    // ========== Constants ==========
    private static final int VALIDATE_INTERVAL = 100; // ticks between bound machine validation

    // ========== Instance fields ==========
    protected final List<BoundMachineEntry> boundMachines = new ArrayList<>();
    protected int globalSpeedLevel = 0;
    protected boolean isStopped = false;
    protected boolean is_active = true;
    protected int selectedMachineIndex = 0;

    // Cached for tick loop
    private long lastTickProcessed = 0;
    private int tickCounter = 0;

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
        return Config.flashBindingRangeX;
    }

    @Override
    public int getYRadius() {
        return Config.flashBindingRangeY;
    }

    @Override
    public int getZRadius() {
        return Config.flashBindingRangeZ;
    }

    @Override
    public boolean isStopped() {
        return this.isStopped;
    }

    @Override
    public int getEffectiveSpeed() {
        return globalSpeedLevel * getSpeedMultiplier();
    }

    // ========== Abstract methods ==========

    /** @return speed multiplier: 1 for normal, 9 for compressed, 81 for double compressed. */
    protected abstract int getSpeedMultiplier();

    /** @return GUI title translation key. */
    protected abstract String getGuiTitleKey();

    /** @return unique GUI panel ID. */
    protected abstract String getGuiPanelId();

    // ========== Bound machine management ==========

    public List<BoundMachineEntry> getBoundMachines() {
        return Collections.unmodifiableList(boundMachines);
    }

    /**
     * Check if a position is within the configured binding range of this torch.
     */
    public boolean isInRange(int x, int y, int z) {
        return Math.abs(x - this.xCoord) <= Config.flashBindingRangeX
            && Math.abs(y - this.yCoord) <= Config.flashBindingRangeY
            && Math.abs(z - this.zCoord) <= Config.flashBindingRangeZ;
    }

    /**
     * Add a machine to the bound list. Validates range and deduplicates.
     *
     * @return true if added, false if out of range or list full
     */
    public boolean addBoundMachine(int x, int y, int z, int dim) {
        if (!isInRange(x, y, z)) return false;

        // Dedup
        for (BoundMachineEntry entry : boundMachines) {
            if (entry.x == x && entry.y == y && entry.z == z && entry.dim == dim) {
                return true; // already bound
            }
        }

        // Cap at max
        if (boundMachines.size() >= Config.flashMaxBoundMachines) return false;

        boundMachines.add(new BoundMachineEntry(x, y, z, dim));
        this.markDirty();
        return true;
    }

    /**
     * Remove a machine from the bound list by coordinates.
     */
    public void removeBoundMachine(int x, int y, int z, int dim) {
        boundMachines.removeIf(e -> e.x == x && e.y == y && e.z == z && e.dim == dim);
        this.markDirty();
    }

    /**
     * Set per-machine speed override. 0 means "use global speed".
     */
    public void setPerMachineSpeed(int x, int y, int z, int dim, int speed) {
        for (BoundMachineEntry entry : boundMachines) {
            if (entry.x == x && entry.y == y && entry.z == z && entry.dim == dim) {
                entry.perMachineSpeed = Math.max(0, Math.min(speed, Config.maxSpeedLevel));
                this.markDirty();
                return;
            }
        }
    }

    /**
     * Get the effective speed for a bound machine (per-machine override or global).
     */
    private int getEffectiveMachineSpeed(BoundMachineEntry entry) {
        int level = entry.perMachineSpeed > 0 ? entry.perMachineSpeed : this.globalSpeedLevel;
        return level * getSpeedMultiplier();
    }

    /**
     * Periodically check bound machines and remove any that are no longer valid.
     */
    private void validateBoundMachines() {
        if (this.worldObj == null) return;
        Iterator<BoundMachineEntry> it = boundMachines.iterator();
        while (it.hasNext()) {
            BoundMachineEntry entry = it.next();
            if (entry.dim != this.worldObj.provider.dimensionId) {
                it.remove();
                this.markDirty();
                continue;
            }
            TileEntity te = this.worldObj.getTileEntity(entry.x, entry.y, entry.z);
            if (te == null || te.isInvalid()) {
                it.remove();
                this.markDirty();
            }
        }
    }

    // ========== GUI (buildUI) ==========

    @Override
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(Torcherino.MODID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings uiSettings) {
        // Panel sized to fit all content: title~6 + speed~44 + bound header~60 + list area~90 + controls~40 = ~210
        ModularPanel panel = new ModularPanel(getGuiPanelId()).size(220, 210);
        int multiplier = getSpeedMultiplier();

        // ---- Sync values ----
        // Global speed (C2S: slider changes synced to server)
        DoubleSyncValue globalSpeedValue = new DoubleSyncValue(
            () -> (double) Math.max(0, Math.min(globalSpeedLevel, Config.maxSpeedLevel)),
            val -> {
                globalSpeedLevel = (int) Math.round(val);
                markDirty();
            }).allowC2S();

        // Bound machines list (S2C only — server builds string from TE data)
        // Format per entry: "name|x|y|z|dim|speed", entries separated by ";"
        StringSyncValue boundListString = new StringSyncValue(() -> {
            if (this.worldObj == null) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < boundMachines.size(); i++) {
                BoundMachineEntry bm = boundMachines.get(i);
                if (i > 0) sb.append(";");
                String name = bm.getLocalizedName(this.worldObj);
                sb.append(name)
                    .append("|")
                    .append(bm.x)
                    .append("|")
                    .append(bm.y)
                    .append("|")
                    .append(bm.z)
                    .append("|")
                    .append(bm.dim)
                    .append("|")
                    .append(bm.perMachineSpeed);
            }
            return sb.toString();
        });
        // No allowC2S: boundListString is server→client only

        // Command sync: client sends remove/speed commands to server
        // Uses separate client/server setters to avoid client-side TE mutation
        StringSyncValue commandSync = new StringSyncValue(
            () -> "", // client getter
            cmd -> {}, // client setter: NO-OP — commands only processed on server
            () -> "", // server getter
            cmd -> { // server setter: process commands
                if (cmd == null || cmd.isEmpty()) return;
                if (cmd.startsWith("remove:")) {
                    String[] parts = cmd.substring(7)
                        .split(",");
                    if (parts.length == 4) {
                        try {
                            removeBoundMachine(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[3]));
                            markDirty();
                            boundListString.notifyUpdate();
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (cmd.startsWith("speed:")) {
                    String[] parts = cmd.substring(6)
                        .split(",");
                    if (parts.length == 5) {
                        try {
                            setPerMachineSpeed(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[3]),
                                Integer.parseInt(parts[4]));
                            markDirty();
                            boundListString.notifyUpdate();
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }).allowC2S();

        // Selected machine index (C2S: persisted in TE field)
        IntSyncValue selectedIdx = new IntSyncValue(() -> clampIdx(selectedMachineIndex), v -> {
            selectedMachineIndex = v;
            markDirty();
        }).allowC2S();

        // Per-machine speed (S2C only — value derived from boundListString)
        // Client getter reads from synced boundListString; server getter reads from TE list directly
        IntSyncValue perMachineSpeed = new IntSyncValue(
            // Client getter: parse from synced boundListString
            () -> {
                ParsedEntry e = ParsedEntry.tryParse(boundListString.getValue(), selectedIdx.getIntValue());
                return e != null ? e.speed : 0;
            },
            null, // client setter: not used — speed changes go through commandSync
            // Server getter: read directly from TE boundMachines list
            () -> {
                if (boundMachines.isEmpty()) return 0;
                int idx = clampIdx(selectedMachineIndex);
                if (idx >= boundMachines.size()) return 0;
                return boundMachines.get(idx).perMachineSpeed;
            },
            null // server setter: not used
        );
        // No allowC2S: speed changes flow through commandSync, S2C feedback through detectAndSendChanges

        syncManager.syncValue("globalSpeed", globalSpeedValue);
        syncManager.syncValue("boundListString", boundListString);
        syncManager.syncValue("command", commandSync);
        syncManager.syncValue("selectedIdx", selectedIdx);
        syncManager.syncValue("perMachineSpeed", perMachineSpeed);

        // ---- GUI Layout ----
        Rectangle sliderBg = new Rectangle().color(0xFF3A3A3A);

        // Title
        panel.child(
            new TextWidget<>(translateToLocal(getGuiTitleKey())).left(8)
                .top(6));

        // Global speed slider
        panel.child(
            new TextWidget<>(translateToLocal("torcherino.gui.wireless.global_speed")).left(8)
                .top(22));
        panel.child(
            new SliderWidget().value(globalSpeedValue)
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
                        globalSpeedValue.getDoubleValue() == 0.0 ? "0%"
                            : ((int) globalSpeedValue.getDoubleValue() * multiplier * 100) + "%"))).left(78)
                                .top(44));

        // Bound machines header
        panel.child(
            new TextWidget<>(translateToLocal("torcherino.gui.wireless.bound_machines")).left(8)
                .top(60));

        // Scroll area dimensions
        int scrollTop = 72;
        int scrollHeight = 83;
        int ctrlY = scrollTop + scrollHeight + 7; // controls start 7px below scroll area

        // Scrollable bound machines list with vertical scrollbar
        ScrollWidget<?> listScroll = new ScrollWidget<>(new VerticalScrollData());
        listScroll.left(8)
            .top(scrollTop)
            .width(200)
            .height(scrollHeight);

        // Inner text widget (all entries, no truncation — scrollbar handles overflow)
        TextWidget<?> innerListText = new TextWidget<>(new DynamicKey(() -> {
            String s = boundListString.getValue();
            if (s == null || s.isEmpty()) {
                return IKey.str(translateToLocal("torcherino.gui.wireless.no_machines"));
            }
            String[] entries = s.split(";");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < entries.length; i++) {
                String[] p = entries[i].split("\\|", 6);
                if (p.length < 6) continue;
                int spd = Integer.parseInt(p[5]);
                int eff = spd > 0 ? spd : (int) globalSpeedValue.getDoubleValue();
                String pct = eff == 0 ? "0%" : (eff * multiplier * 100) + "%";
                if (i > 0) sb.append("\n");
                sb.append("[")
                    .append(i + 1)
                    .append("] ")
                    .append(p[0])
                    .append(" (")
                    .append(p[1])
                    .append(",")
                    .append(p[2])
                    .append(",")
                    .append(p[3])
                    .append(") [")
                    .append(pct)
                    .append("]");
            }
            return IKey.str(sb.toString());
        }));
        innerListText.left(0)
            .top(0)
            .width(185) // account for scrollbar width (~15px) inside the 200px widget
            .textAlign(Alignment.CenterLeft);
        listScroll.addChild(innerListText, 0);

        // Dynamically adjust inner text height and scroll size when bound list changes
        boundListString.setChangeListener(() -> {
            String s = boundListString.getValue();
            int lineCount = (s == null || s.isEmpty()) ? 1 : Math.max(1, s.split(";").length);
            int textHeight = lineCount * 10; // ~10px per text line
            innerListText.height(textHeight);
            if (listScroll.getScrollArea()
                .getScrollY() != null) {
                listScroll.getScrollArea()
                    .getScrollY()
                    .setScrollSize(textHeight);
            }
        });

        panel.child(listScroll);

        // --- Bottom controls ---

        // Machine selector button ([N] name, truncated to fit)
        panel.child(new ButtonWidget<>().child(new TextWidget<>(new DynamicKey(() -> {
            ParsedEntry e = ParsedEntry.tryParse(boundListString.getValue(), selectedIdx.getIntValue());
            if (e == null) return IKey.str("");
            String prefix = "[" + (selectedIdx.getIntValue() + 1) + "] ";
            String label = prefix + e.name;
            // Truncate to fit 100px button: ~16 chars with prefix; clip name if needed
            if (label.length() > 16) {
                label = prefix + e.name.substring(0, Math.min(e.name.length(), 14 - prefix.length())) + "..";
            }
            return IKey.str(label);
        })))
            .onMousePressed(a -> {
                String s = boundListString.getValue();
                if (s == null || s.isEmpty()) return false;
                String[] entries = s.split(";");
                int idx = clampIdx(selectedIdx.getIntValue(), entries.length);
                selectedIdx.setIntValue((idx + 1) % entries.length, true, true);
                return true;
            })
            .left(8)
            .top(ctrlY)
            .width(100)
            .height(12)
            .tooltip(t -> t.addLine(translateToLocal("torcherino.gui.wireless.select_machine"))));

        // Per-machine speed override slider (no allowC2S: changes flow through commandSync)
        panel.child(new SliderWidget().value(new DoubleSyncValue(() -> (double) perMachineSpeed.getIntValue(), v -> {
            int val = (int) Math.round(v);
            ParsedEntry e = ParsedEntry.tryParse(boundListString.getValue(), selectedIdx.getIntValue());
            if (e == null) return;
            commandSync.setStringValue("speed:" + e.x + "," + e.y + "," + e.z + "," + e.dim + "," + val, true, true);
        }))
            .bounds(0, Config.maxSpeedLevel)
            .background(sliderBg)
            .left(8)
            .top(ctrlY + 16)
            .width(160)
            .height(10));
        panel.child(new TextWidget<>(new DynamicKey(() -> {
            int v = perMachineSpeed.getIntValue();
            return IKey
                .str(v == 0 ? translateToLocal("torcherino.gui.wireless.global_label") : (v * multiplier * 100) + "%");
        })).left(78)
            .top(ctrlY + 28));

        // Remove button
        panel.child(
            new ButtonWidget<>().child(new TextWidget<>(translateToLocal("torcherino.gui.wireless.remove")))
                .onMousePressed(a -> {
                    ParsedEntry e = ParsedEntry.tryParse(boundListString.getValue(), selectedIdx.getIntValue());
                    if (e == null) return false;
                    commandSync.setStringValue("remove:" + e.x + "," + e.y + "," + e.z + "," + e.dim, true, true);
                    return true;
                })
                .left(170)
                .top(ctrlY)
                .width(40)
                .height(12)
                .tooltip(t -> t.addLine(translateToLocal("torcherino.gui.wireless.remove"))));

        return panel;
    }

    /** Clamp index to [0, boundMachines.size()-1], returning 0 if list is empty. */
    private int clampIdx(int idx) {
        int max = boundMachines.size() - 1;
        return max < 0 ? 0 : Math.max(0, Math.min(idx, max));
    }

    /** Clamp index to [0, size-1], returning 0 if size is 0. */
    private static int clampIdx(int idx, int size) {
        return size <= 0 ? 0 : Math.max(0, Math.min(idx, size - 1));
    }

    /**
     * Parsed fields from one entry in the boundListString.
     * Decouples widget handlers from the raw string parsing format.
     */
    private static final class ParsedEntry {

        final String name;
        final int x, y, z, dim, speed;

        ParsedEntry(String[] parts) {
            this.name = parts[0];
            this.x = Integer.parseInt(parts[1]);
            this.y = Integer.parseInt(parts[2]);
            this.z = Integer.parseInt(parts[3]);
            this.dim = Integer.parseInt(parts[4]);
            this.speed = Integer.parseInt(parts[5]);
        }

        static ParsedEntry tryParse(String boundListStr, int idx) {
            if (boundListStr == null || boundListStr.isEmpty()) return null;
            String[] entries = boundListStr.split(";");
            int clamped = clampIdx(idx, entries.length);
            if (clamped >= entries.length) return null;
            String[] parts = entries[clamped].split("\\|", 6);
            if (parts.length < 6) return null;
            try {
                return new ParsedEntry(parts);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    // ========== NBT ==========

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        globalSpeedLevel = clampInt(compound.getInteger("GlobalSpeedLevel"), 0, Config.maxSpeedLevel);
        isStopped = compound.getBoolean("IsStopped");
        is_active = !compound.hasKey("IsActive") || compound.getBoolean("IsActive");
        selectedMachineIndex = clampInt(compound.getInteger("SelectedMachineIndex"), 0, Integer.MAX_VALUE);

        boundMachines.clear();
        if (compound.hasKey("BoundMachines")) {
            NBTTagList list = compound.getTagList("BoundMachines", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                boundMachines.add(BoundMachineEntry.fromNBT(tag));
            }
        }

        // Backwards compat: convert old TimeRate to speedLevel
        if (compound.hasKey("TimeRate") && !compound.hasKey("GlobalSpeedLevel")) {
            int oldTimeRate = compound.getInteger("TimeRate");
            globalSpeedLevel = clampInt(oldTimeRate / getSpeedMultiplier(), 0, Config.maxSpeedLevel);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("GlobalSpeedLevel", globalSpeedLevel);
        compound.setBoolean("IsStopped", isStopped);
        compound.setBoolean("IsActive", is_active);
        compound.setInteger("SelectedMachineIndex", selectedMachineIndex);

        NBTTagList list = new NBTTagList();
        for (BoundMachineEntry entry : boundMachines) {
            list.appendTag(entry.toNBT());
        }
        compound.setTag("BoundMachines", list);
    }

    private static int clampInt(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    // ========== Tick / Acceleration ==========

    @Override
    public void updateEntity() {
        // Prevent recursion
        long currentTick = this.worldObj.getTotalWorldTime();
        if (this.lastTickProcessed == currentTick) return;
        this.lastTickProcessed = currentTick;

        // Early exit
        if (this.worldObj.isRemote || !this.is_active || isStopped || globalSpeedLevel == 0) return;

        int effectiveSpeed = getEffectiveSpeed();
        if (effectiveSpeed <= 0) return;

        // Periodic validation
        tickCounter++;
        if (tickCounter >= VALIDATE_INTERVAL) {
            tickCounter = 0;
            validateBoundMachines();
        }

        // Accelerate each bound machine
        for (BoundMachineEntry entry : boundMachines) {
            // Dimension check
            if (entry.dim != this.worldObj.provider.dimensionId) continue;

            int machineSpeed = getEffectiveMachineSpeed(entry);
            if (machineSpeed <= 0) continue;

            AccelerationHelper.accelerateAtPosition(
                this.worldObj,
                this.xCoord,
                this.yCoord,
                this.zCoord,
                machineSpeed,
                entry.x,
                entry.y,
                entry.z);
        }
    }

    // ========== Cleanup ==========

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
    }

    // ========== WAILA (IWailaDataProvider) ==========

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null; // use default stack
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();
        int count = tag.getInteger("waila_bm_count");
        int globalSpeed = tag.getInteger("waila_global_speed");
        int multiplier = getSpeedMultiplier();

        currenttip.add(
            translateToLocal("torcherino.waila.wireless.global_speed") + ": " + (globalSpeed * multiplier * 100) + "%");
        currenttip.add(translateToLocal("torcherino.waila.wireless.bound_count") + ": " + count);

        if (count > 0) {
            NBTTagList entries = tag.getTagList("waila_bm_entries", 10);
            int show = Math.min(entries.tagCount(), 5);
            for (int i = 0; i < show; i++) {
                NBTTagCompound entryTag = entries.getCompoundTagAt(i);
                String name = entryTag.getString("name");
                int x = entryTag.getInteger("x");
                int y = entryTag.getInteger("y");
                int z = entryTag.getInteger("z");
                int speed = entryTag.getInteger("speed");
                String speedStr = speed > 0 ? (speed * multiplier * 100) + "%"
                    : translateToLocal("torcherino.waila.wireless.use_global");
                currenttip.add("  " + name + " (" + x + ", " + y + ", " + z + ") [" + speedStr + "]");
            }
            if (entries.tagCount() > 5) {
                currenttip.add("  ... +" + (entries.tagCount() - 5) + " more");
            }
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        tag.setInteger("waila_bm_count", boundMachines.size());
        tag.setInteger("waila_global_speed", globalSpeedLevel);

        NBTTagList entries = new NBTTagList();
        int limit = Math.min(boundMachines.size(), 6); // 5 shown + 1 for "more" check
        for (int i = 0; i < limit; i++) {
            BoundMachineEntry entry = boundMachines.get(i);
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setString("name", entry.getLocalizedName(world));
            entryTag.setInteger("x", entry.x);
            entryTag.setInteger("y", entry.y);
            entryTag.setInteger("z", entry.z);
            entryTag.setInteger("speed", entry.perMachineSpeed > 0 ? entry.perMachineSpeed : 0);
            entries.appendTag(entryTag);
        }
        tag.setTag("waila_bm_entries", entries);
        return tag;
    }
}
