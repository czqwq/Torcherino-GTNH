package com.czqwq.Torcherino;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    // ========== Acceleration settings ==========
    public static boolean enableAccelerateGregTechMachine = true;
    public static float accelerateGregTechMachineDiscount = 0.8F;

    // ========== Radius limits ==========
    public static int maxXRadius = 4;
    public static int maxYRadius = 1;
    public static int maxZRadius = 4;

    // ========== Speed limits ==========
    /**
     * Maximum speed slider level (0-based, so 4 means 5 levels: 0-4).
     * Each level applies the multiplier (1x for normal, 9x for compressed, 81x for double).
     */
    public static int maxSpeedLevel = 4;

    // ========== Performance controls ==========
    /**
     * Enable per-tick time budget to prevent server lag from excessive acceleration.
     * When enabled, acceleration stops if the tick budget (in nanoseconds) is exceeded.
     */
    public static boolean enableTickBudget = true;

    /**
     * Per-tick time budget in nanoseconds. Default: 1_000_000 (1 ms).
     * When enableTickBudget is true, each torch will stop accelerating
     * if it spends more than this amount of time in a single tick.
     */
    public static long tickBudgetNanos = 1_000_000L;

    /**
     * Enable stacking acceleration: when enabled, multiple torches can accelerate
     * the same machine, with their effects adding together cumulatively.
     * When disabled, only the fastest torch covering a position accelerates it.
     */
    public static boolean enableStackingAcceleration = false;

    /**
     * Enable overlap detection to prevent multiple torches from double-accelerating
     * the same tile entities. Only used when enableStackingAcceleration is false.
     * When enabled, each position is only accelerated once per world tick,
     * by the fastest torch that covers it.
     */
    public static boolean enableOverlapDetection = true;

    // ========== Mod compatibility ==========
    /**
     * Enable/disable EnderIO machine acceleration support.
     */
    public static boolean enableEnderIOAcceleration = true;

    /**
     * Enable/disable CropsNH crop sticks acceleration support.
     */
    public static boolean enableCropsNHAcceleration = true;

    /**
     * Enable/disable ForestryMC alveary acceleration support.
     */
    public static boolean enableForestryAcceleration = true;

    // ========== Flash-bound Torcherino settings ==========
    /**
     * Enable/disable the flash-bound torcherino feature entirely.
     */
    public static boolean enableFlashTorcherino = true;

    /**
     * Maximum binding range on X axis (per side).
     * Default: 4 → 9 blocks total (4 left + torch + 4 right).
     */
    public static int flashBindingRangeX = 4;

    /**
     * Maximum binding range on Y axis (per side).
     * Default: 1 → 3 blocks total (1 below + torch + 1 above).
     */
    public static int flashBindingRangeY = 1;

    /**
     * Maximum binding range on Z axis (per side).
     * Default: 4 → 9 blocks total (4 front + torch + 4 back).
     */
    public static int flashBindingRangeZ = 4;

    /**
     * Maximum number of machines that can be bound to a single flash torch.
     */
    public static int flashMaxBoundMachines = 64;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        // ---- Acceleration category ----
        enableAccelerateGregTechMachine = configuration.getBoolean(
            "enableAccelerateGregTechMachine",
            "Acceleration",
            enableAccelerateGregTechMachine,
            "Enable advanced acceleration for GregTech machines (requires mixins)");

        accelerateGregTechMachineDiscount = configuration.getFloat(
            "accelerateGregTechMachineDiscount",
            "Acceleration",
            accelerateGregTechMachineDiscount,
            0.0F,
            1.0F,
            "Discount factor for GregTech machine acceleration (0.0 to 1.0)");

        // ---- Radius limits ----
        maxXRadius = configuration.getInt(
            "maxXRadius",
            "Radius",
            maxXRadius,
            0,
            16,
            "Maximum X-axis radius for torches (0 = 1 block, 4 = 9 blocks)");

        maxYRadius = configuration.getInt(
            "maxYRadius",
            "Radius",
            maxYRadius,
            0,
            8,
            "Maximum Y-axis radius for torches (0 = 1 block, 1 = 3 blocks)");

        maxZRadius = configuration.getInt(
            "maxZRadius",
            "Radius",
            maxZRadius,
            0,
            16,
            "Maximum Z-axis radius for torches (0 = 1 block, 4 = 9 blocks)");

        // ---- Speed limits ----
        maxSpeedLevel = configuration.getInt(
            "maxSpeedLevel",
            "Speed",
            maxSpeedLevel,
            0,
            32,
            "Maximum speed slider level (0-based). Higher values allow more acceleration steps.");

        // ---- Performance ----
        enableTickBudget = configuration.getBoolean(
            "enableTickBudget",
            "Performance",
            enableTickBudget,
            "Enable per-tick time budget to prevent server lag. "
                + "When enabled, each torch stops accelerating if it exceeds tickBudgetNanos.");

        tickBudgetNanos = configuration.getInt(
            "tickBudgetNanos",
            "Performance",
            (int) tickBudgetNanos,
            100_000,
            100_000_000,
            "Per-tick time budget in nanoseconds (default: 1_000_000 = 1ms). "
                + "Lower values reduce lag but may limit acceleration throughput.");

        enableStackingAcceleration = configuration.getBoolean(
            "enableStackingAcceleration",
            "Performance",
            enableStackingAcceleration,
            "Enable stacking acceleration: multiple torches can accelerate the same machine cumulatively. "
                + "When disabled, only the fastest torch covering a position will accelerate it."
                + "Warning: Enabling this may reduce performance.");

        enableOverlapDetection = configuration.getBoolean(
            "enableOverlapDetection",
            "Performance",
            enableOverlapDetection,
            "Enable overlap detection to prevent multiple torches from accelerating the same tile "
                + "entity multiple times per tick. Only used when enableStackingAcceleration is false.");

        // ---- Compatibility ----
        enableEnderIOAcceleration = configuration.getBoolean(
            "enableEnderIOAcceleration",
            "Compatibility",
            enableEnderIOAcceleration,
            "Enable EnderIO machine acceleration support via mixins.");

        enableCropsNHAcceleration = configuration.getBoolean(
            "enableCropsNHAcceleration",
            "Compatibility",
            enableCropsNHAcceleration,
            "Enable CropsNH crop sticks acceleration support via mixins.");

        enableForestryAcceleration = configuration.getBoolean(
            "enableForestryAcceleration",
            "Compatibility",
            enableForestryAcceleration,
            "Enable ForestryMC alveary acceleration support via mixins.");

        // ---- Flash Torcherino ----
        enableFlashTorcherino = configuration.getBoolean(
            "enableFlashTorcherino",
            "FlashTorcherino",
            enableFlashTorcherino,
            "Master switch to enable/disable the Flash-Bound Torcherino feature.");

        flashBindingRangeX = configuration.getInt(
            "flashBindingRangeX",
            "FlashTorcherino",
            flashBindingRangeX,
            0,
            16,
            "X-axis binding range per side for flash torches (4 = 9 blocks wide, matching classic max radius).");

        flashBindingRangeY = configuration.getInt(
            "flashBindingRangeY",
            "FlashTorcherino",
            flashBindingRangeY,
            0,
            8,
            "Y-axis binding range per side for flash torches (1 = 3 blocks tall, matching classic max radius).");

        flashBindingRangeZ = configuration.getInt(
            "flashBindingRangeZ",
            "FlashTorcherino",
            flashBindingRangeZ,
            0,
            16,
            "Z-axis binding range per side for flash torches (4 = 9 blocks deep, matching classic max radius).");

        flashMaxBoundMachines = configuration.getInt(
            "flashMaxBoundMachines",
            "FlashTorcherino",
            flashMaxBoundMachines,
            1,
            256,
            "Maximum number of machines that can be bound to a single flash torch.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
