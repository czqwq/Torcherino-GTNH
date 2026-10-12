package com.czqwq.Torcherino.tile;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

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

/**
 * Abstract base for wireless (flash-bound) Torcherino tiles.
 * Accelerates only explicitly bound TE blocks, not area-based.
 * Supports per-machine speed overrides via GUI.
 * <p>
 * Subclasses only need to provide {@link #getSpeedMultiplier()}, {@link #getGuiTitleKey()},
 * and {@link #getGuiPanelId()}.
 */
public abstract class TileWirelessTorcherinoBase extends TileEntity implements IGuiHolder<PosGuiData>, ITorcherinoTile {

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
        return Config.wirelessTorchRadius;
    }

    @Override
    public int getYRadius() {
        return 128; // Full world height (0–255), actual bounds ignore Y
    }

    @Override
    public int getZRadius() {
        return Config.wirelessTorchRadius;
    }

    @Override
    public boolean isStopped() {
        return this.isStopped;
    }

    @Override
    public int getEffectiveSpeed() {
        return Math.min(globalSpeedLevel, Config.maxSpeedLevel) * getSpeedMultiplier();
    }

    // ========== Abstract methods ==========

    /** @return speed multiplier: 1 for normal, 9 for compressed, 81 for double compressed. */
    public abstract int getSpeedMultiplier();

    /** @return the global speed slider level (0-based, for WAILA NBT sync). */
    public int getGlobalSpeedLevel() {
        return globalSpeedLevel;
    }

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
     * Y axis is not checked — wireless torches cover full world height (0–255).
     */
    public boolean isInRange(int x, int y, int z) {
        return Math.abs(x - this.xCoord) <= Config.wirelessTorchRadius
            && Math.abs(z - this.zCoord) <= Config.wirelessTorchRadius;
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

        BoundMachineEntry entry = new BoundMachineEntry(x, y, z, dim);
        // Resolve the display name once, up front, so the recurring GUI/WAILA reads are free.
        entry.refreshDisplayName(this.worldObj);
        boundMachines.add(entry);
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
     * Values are always clamped to the current Config.maxSpeedLevel to handle
     * config hot-reloads where the limit may have been reduced since last save.
     */
    private int getEffectiveMachineSpeed(BoundMachineEntry entry) {
        int max = Config.maxSpeedLevel;
        int level;
        if (entry.perMachineSpeed > 0) {
            level = Math.min(entry.perMachineSpeed, max);
            // Persist the clamp so the GUI and NBT stay consistent
            if (entry.perMachineSpeed > max) {
                entry.perMachineSpeed = max;
                this.markDirty();
            }
        } else {
            level = Math.min(this.globalSpeedLevel, max);
        }
        return level * getSpeedMultiplier();
    }

    /**
     * Periodically check bound machines and remove any that are no longer valid.
     * <p>
     * Note the {@code blockExists} guard: {@code World.getTileEntity} returns {@code null} both
     * for "the machine is gone" and for "the machine's chunk is not loaded right now". Without
     * the guard a player whose machines sit in unloaded chunks would have them silently unbound
     * on every sweep (once every 100 ticks), each removal dropping a description-packet rebuild
     * on them and forcing a manual re-bind. A chunk that is loaded but genuinely empty still
     * fails the {@code getTileEntity} test below, so real removals are unaffected.
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
            if (!this.worldObj.blockExists(entry.x, entry.y, entry.z)) {
                // Chunk unloaded: cannot tell whether the machine still exists, so keep the
                // binding and re-check it on a later sweep.
                continue;
            }
            TileEntity te = this.worldObj.getTileEntity(entry.x, entry.y, entry.z);
            if (te == null || te.isInvalid()) {
                it.remove();
                this.markDirty();
                continue;
            }
            // Still bound: this is also the periodic refresh point for the cached display name,
            // so a machine swapped at the same coordinates is picked up within one sweep.
            entry.refreshDisplayName(this.worldObj);
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
                // getCachedName: this getter is polled once per server tick while the panel is
                // open, so resolving the name here (TE lookup + i18n) would repeat permanently.
                String name = bm.getCachedName(this.worldObj);
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

        // Command sync: client sends remove commands to server
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
                }
            }).allowC2S();

        // ---- Bound-list parse cache ----
        // The widgets below read the bound list through DynamicKey suppliers, and ModularUI2
        // evaluates those suppliers on EVERY frame (TextWidget.draw -> checkString ->
        // IKey.getFormatted) — the widget's lastText field only avoids the re-layout, not the
        // supplier call. Splitting the raw string there meant several String.split calls per
        // frame (each allocating a fresh Pattern, even for a single-character separator) plus a
        // parse per entry. Parse once per change instead; the change listener below already
        // fires exactly when the string changes.
        final List<ParsedEntry> parsedBoundList = new ArrayList<>();
        final Runnable reparseBoundList = () -> {
            parsedBoundList.clear();
            parsedBoundList.addAll(parseAllEntries(boundListString.getValue()));
        };
        reparseBoundList.run(); // seed from the current (possibly already synced) value

        // Selected machine index (C2S: persisted in TE field)
        IntSyncValue selectedIdx = new IntSyncValue(() -> clampIdx(selectedMachineIndex), v -> {
            selectedMachineIndex = v;
            markDirty();
        }).allowC2S();

        // Per-machine speed (C2S: slider directly updates the selected bound machine on server)
        DoubleSyncValue perMachineSpeed = new DoubleSyncValue(
            // Client getter: read from the parsed cache
            () -> {
                ParsedEntry e = entryAt(parsedBoundList, selectedIdx.getIntValue());
                return e != null ? (double) e.speed : 0.0;
            },
            v -> {}, // client setter: NO-OP — value set by slider sync; must be non-null
                     // to prevent fallback to server setter on the client side
            // Server getter: read directly from TE boundMachines list
            () -> {
                if (boundMachines.isEmpty()) return 0.0;
                int idx = clampIdx(selectedMachineIndex);
                if (idx >= boundMachines.size()) return 0.0;
                return (double) boundMachines.get(idx).perMachineSpeed;
            },
            // Server setter: update the selected bound machine's speed
            v -> {
                if (boundMachines.isEmpty()) return;
                int idx = clampIdx(selectedMachineIndex);
                if (idx >= boundMachines.size()) return;
                int val = Math.max(0, Math.min((int) Math.round(v), Config.maxSpeedLevel));
                boundMachines.get(idx).perMachineSpeed = val;
                this.markDirty();
                boundListString.notifyUpdate();
            }).allowC2S();

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
            if (parsedBoundList.isEmpty()) {
                return IKey.str(translateToLocal("torcherino.gui.wireless.no_machines"));
            }
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (int i = 0; i < parsedBoundList.size(); i++) {
                ParsedEntry e = parsedBoundList.get(i);
                if (e == null) continue; // malformed entry: holds its slot, renders nothing
                int spd = e.speed;
                int eff = spd > 0 ? spd : (int) globalSpeedValue.getDoubleValue();
                String pct = eff == 0 ? "0%" : (eff * multiplier * 100) + "%";
                if (!first) sb.append("\n");
                first = false;
                sb.append("[")
                    .append(i + 1)
                    .append("] ")
                    .append(e.name)
                    .append(" (")
                    .append(e.x)
                    .append(",")
                    .append(e.y)
                    .append(",")
                    .append(e.z)
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
            reparseBoundList.run();
            final int lineCount = Math.max(1, countParsed(parsedBoundList));
            final int textHeight = lineCount * 10; // ~10px per text line
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
            ParsedEntry e = entryAt(parsedBoundList, selectedIdx.getIntValue());
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
                int size = parsedBoundList.size();
                if (size == 0) return false;
                int idx = clampIdx(selectedIdx.getIntValue(), size);
                selectedIdx.setIntValue((idx + 1) % size, true, true);
                return true;
            })
            .left(8)
            .top(ctrlY)
            .width(100)
            .height(12)
            .tooltip(t -> t.addLine(translateToLocal("torcherino.gui.wireless.select_machine"))));

        // Per-machine speed override slider (C2S: updates selected bound machine directly)
        panel.child(
            new SliderWidget().value(perMachineSpeed)
                .bounds(0, Config.maxSpeedLevel)
                .background(sliderBg)
                .left(8)
                .top(ctrlY + 16)
                .width(160)
                .height(10));
        panel.child(new TextWidget<>(new DynamicKey(() -> {
            int v = (int) perMachineSpeed.getDoubleValue();
            return IKey
                .str(v == 0 ? translateToLocal("torcherino.gui.wireless.global_label") : (v * multiplier * 100) + "%");
        })).left(78)
            .top(ctrlY + 28));

        // Remove button
        panel.child(
            new ButtonWidget<>().child(new TextWidget<>(translateToLocal("torcherino.gui.wireless.remove")))
                .onMousePressed(a -> {
                    ParsedEntry e = entryAt(parsedBoundList, selectedIdx.getIntValue());
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
     * Parse the whole bound-list string once, in raw-entry order.
     * <p>
     * Malformed entries are kept as {@code null} placeholders rather than skipped, so that the
     * index of every surviving entry still matches its index in the raw string — the selector
     * button, the per-machine slider and the remove button all address machines by that index,
     * and a dropped entry would silently shift them onto the wrong machine.
     */
    private static List<ParsedEntry> parseAllEntries(String boundListStr) {
        List<ParsedEntry> result = new ArrayList<>();
        if (boundListStr == null || boundListStr.isEmpty()) return result;
        for (String entry : boundListStr.split(";")) {
            String[] parts = entry.split("\\|", 6);
            if (parts.length < 6) {
                result.add(null);
                continue;
            }
            try {
                result.add(new ParsedEntry(parts));
            } catch (NumberFormatException ignored) {
                result.add(null);
            }
        }
        return result;
    }

    /**
     * Index into the parsed entry list, applying the same clamping the raw-string lookups used
     * (an out-of-range index yields the last entry, an empty list yields null). Malformed
     * entries are null and therefore yield null, exactly as {@code tryParse} did.
     */
    private static ParsedEntry entryAt(List<ParsedEntry> entries, int idx) {
        if (entries.isEmpty()) return null;
        return entries.get(clampIdx(idx, entries.size()));
    }

    /** Number of entries that parsed successfully (the number of lines the GUI renders). */
    private static int countParsed(List<ParsedEntry> entries) {
        int n = 0;
        for (ParsedEntry e : entries) {
            if (e != null) n++;
        }
        return n;
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

    // ========== Client sync (vanilla description packet) ==========

    /**
     * Sync bound-machine data to the client via the vanilla description-packet
     * mechanism. {@link #markDirty()} triggers this automatically whenever the
     * bound list or speed changes.
     * <p>
     * This is what lets {@code WirelessBeamRenderer} read
     * {@link #getBoundMachines()} on the client — without this the client-side
     * TE always sees an empty list.
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

    // ========== Tick / Acceleration ==========

    @Override
    public void updateEntity() {
        // Prevent recursion
        long currentTick = this.worldObj.getTotalWorldTime();
        if (this.lastTickProcessed == currentTick) return;
        this.lastTickProcessed = currentTick;

        // Early exit
        if (this.worldObj.isRemote || !this.is_active || isStopped) return;

        // Clamp global speed to config max (handles config hot-reload shrinking the limit)
        if (globalSpeedLevel > Config.maxSpeedLevel) {
            globalSpeedLevel = Config.maxSpeedLevel;
            this.markDirty();
        }
        if (globalSpeedLevel == 0) return;

        int effectiveSpeed = getEffectiveSpeed();
        if (effectiveSpeed <= 0) return;

        // Periodic validation
        tickCounter++;
        if (tickCounter >= VALIDATE_INTERVAL) {
            tickCounter = 0;
            validateBoundMachines();
        }

        // Time budget for this whole torch tick (NOT per bound machine — see AccelerationHelper)
        final long tickDeadline = AccelerationHelper.newTickDeadline();

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
                entry.z,
                tickDeadline);
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
}
