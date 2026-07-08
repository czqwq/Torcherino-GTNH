package com.czqwq.Torcherino;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/**
 * Manages recipe enable/disable toggles in {@code config/Torcherino/recipe.cfg}.
 * <p>
 * All recipe registrations in {@code ModRecipes} and {@code GTRecipes} check these
 * booleans before registering. This allows pack makers to disable specific recipe
 * groups without touching code.
 * <p>
 * Torch recipes are grouped by <b>type</b> (accelerated, classic, wireless) and
 * <b>tier</b> (compressed, double-compressed). When a tier switch is disabled, only
 * the basic torch of that type is craftable; the compressed upgrade chain is removed.
 */
public final class RecipeConfig {

    // ========== Torch type switches (vanilla) ==========

    /** Basic accelerated torcherino from 4 clocks + 1 torch. */
    public static boolean acceleratedTorcherino = true;

    /** Shapeless accelerated ↔ classic conversions (all tiers, both directions). */
    public static boolean classicTorcherino = true;

    /**
     * Compressed tier recipes (9× basic → 1× compressed) for accelerated and classic.
     * When disabled, the 3×3 upgrade path is removed.
     */
    public static boolean compressedTorcherino = true;

    /**
     * Double-compressed tier recipes (9× compressed → 1× double-compressed) for
     * accelerated and classic. When disabled, the final tier upgrade path is removed.
     */
    public static boolean doubleCompressedTorcherino = true;

    // ========== Item switches (vanilla) ==========

    /** Imperfect time twister recipe. */
    public static boolean timeTwister = true;

    /** Time vial recipe. */
    public static boolean timeVial = true;

    // ========== GregTech machine recipes ==========

    /**
     * All wireless torcherino compressor recipes (3 tiers: basic, compressed,
     * double-compressed). Requires HIP compressor unit.
     */
    public static boolean wirelessTorcherino = true;

    /** Perfect time twister assembly line recipe (UEV tier). */
    public static boolean perfectTimeTwister = true;

    /** Torcherino via GT MV assembler (4 clocks + 1 torch, circuit 4). */
    public static boolean torcherinoAssembler = true;

    /**
     * Neutronium compressor: 9× basic torcherino → 1× compressed torcherino.
     */
    public static boolean gtCompressedTorcherino = true;

    /**
     * Neutronium compressor: 9× compressed torcherino → 1× double-compressed torcherino.
     */
    public static boolean gtDoubleCompressedTorcherino = true;

    /** GT assembler classic conversion recipes (accelerated ↔ classic, all 3 tiers). */
    public static boolean gtClassicConversions = true;

    // ========== Config file ==========

    private RecipeConfig() {}

    public static void synchronizeConfiguration(File recipeFile) {
        Configuration cfg = new Configuration(recipeFile);

        // ---- Vanilla torch types ----
        acceleratedTorcherino = cfg.getBoolean(
            "acceleratedTorcherino",
            "Vanilla",
            acceleratedTorcherino,
            "Basic accelerated torcherino from 4 clocks + 1 torch (vanilla crafting table).");

        classicTorcherino = cfg.getBoolean(
            "classicTorcherino",
            "Vanilla",
            classicTorcherino,
            "Shapeless accelerated ↔ classic conversion recipes (all tiers, both directions).");

        compressedTorcherino = cfg.getBoolean(
            "compressedTorcherino",
            "Vanilla",
            compressedTorcherino,
            "9× basic torch → 1× compressed torch recipes (accelerated and classic).");

        doubleCompressedTorcherino = cfg.getBoolean(
            "doubleCompressedTorcherino",
            "Vanilla",
            doubleCompressedTorcherino,
            "9× compressed torch → 1× double-compressed torch recipes (accelerated and classic).");

        // ---- Vanilla items ----
        timeTwister = cfg.getBoolean("timeTwister", "Vanilla", timeTwister, "Imperfect time twister recipe.");

        timeVial = cfg.getBoolean("timeVial", "Vanilla", timeVial, "Time vial recipe.");

        // ---- GregTech recipes ----
        wirelessTorcherino = cfg.getBoolean(
            "wirelessTorcherino",
            "GregTech",
            wirelessTorcherino,
            "All wireless torcherino compressor recipes (3 tiers: basic, compressed, double-compressed). "
                + "Requires HIP compressor unit.");

        perfectTimeTwister = cfg.getBoolean(
            "perfectTimeTwister",
            "GregTech",
            perfectTimeTwister,
            "Perfect time twister assembly line recipe (UEV tier).");

        torcherinoAssembler = cfg.getBoolean(
            "torcherinoAssembler",
            "GregTech",
            torcherinoAssembler,
            "Torcherino via GT MV assembler (4 clocks + 1 torch, circuit 4).");

        gtCompressedTorcherino = cfg.getBoolean(
            "gtCompressedTorcherino",
            "GregTech",
            gtCompressedTorcherino,
            "Neutronium compressor: 9× basic torcherino → 1× compressed torcherino.");

        gtDoubleCompressedTorcherino = cfg.getBoolean(
            "gtDoubleCompressedTorcherino",
            "GregTech",
            gtDoubleCompressedTorcherino,
            "Neutronium compressor: 9× compressed torcherino → 1× double-compressed torcherino.");

        gtClassicConversions = cfg.getBoolean(
            "gtClassicConversions",
            "GregTech",
            gtClassicConversions,
            "GT assembler accelerated ↔ classic conversion recipes (all 3 tiers, both directions).");

        if (cfg.hasChanged()) {
            cfg.save();
        }
    }
}
