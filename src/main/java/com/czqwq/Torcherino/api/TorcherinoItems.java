package com.czqwq.Torcherino.api;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Central registry of all Torcherino items and blocks.
 * <p>
 * Use this enum to obtain {@link ItemStack} references instead of
 * hardcoding {@code GTModHandler.getModItem("Torcherino", ...)}
 * strings. Every entry maps directly to a registered item/block name
 * and can produce stacks of any count.
 * <p>
 * Usage in GT recipes:
 *
 * <pre>
 * {@code
 * import static com.czqwq.Torcherino.api.TorcherinoItems.*;
 *
 * GTValues.RA.stdBuilder()
 *     .itemInputs(TORCHERINO.get(256))
 *     .itemOutputs(WIRELESS_TORCHERINO.get())
 *     ...
 * }
 * </pre>
 */
public enum TorcherinoItems {

    // ========== Items ==========
    TIME_VIAL("timeVial", 1),
    IMPERFECT_TIME_TWISTER("imperfectTimeTwister", 1),
    PERFECT_TIME_TWISTER("perfectTimeTwister", 1),

    // ========== Non-wireless blocks ==========
    TORCHERINO("torcherino"),
    COMPRESSED_TORCHERINO("compressed_torcherino"),
    DOUBLE_COMPRESSED_TORCHERINO("double_compressed_torcherino"),
    TORCHERINO_CLASSIC("torcherino_classic"),
    COMPRESSED_TORCHERINO_CLASSIC("compressed_torcherino_classic"),
    DOUBLE_COMPRESSED_TORCHERINO_CLASSIC("double_compressed_torcherino_classic"),

    // ========== Wireless blocks ==========
    WIRELESS_TORCHERINO("wireless_torcherino"),
    COMPRESSED_WIRELESS_TORCHERINO("compressed_wireless_torcherino"),
    DOUBLE_COMPRESSED_WIRELESS_TORCHERINO("double_compressed_wireless_torcherino");

    private static final String MODID = "Torcherino";

    private final String name;
    private final int maxStackSize;

    TorcherinoItems(String name) {
        this(name, 64);
    }

    TorcherinoItems(String name, int maxStackSize) {
        this.name = name;
        this.maxStackSize = maxStackSize;
    }

    /** Single item (count = 1). */
    public ItemStack get() {
        return get(1);
    }

    /** Item stack with the requested count. */
    public ItemStack get(int count) {
        return GameRegistry.findItemStack(MODID, name, count);
    }

    /** Registered item name (e.g. "torcherino"). */
    public String itemName() {
        return name;
    }

    /** Maximum vanilla stack size for this item. */
    public int maxStackSize() {
        return maxStackSize;
    }
}
