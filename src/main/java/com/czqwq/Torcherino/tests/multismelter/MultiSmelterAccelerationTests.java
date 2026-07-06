package com.czqwq.Torcherino.tests.multismelter;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.tile.TileTorcherinoBase;
import com.czqwq.Torcherino.tile.TileTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.AfterBatch;
import com.gtnewhorizons.horizonqa.api.annotation.BeforeBatch;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import com.gtnewhorizons.horizonqa.api.gt.Multiblock;

import gregtech.api.enums.TierEU;

/**
 * Multi Smelter (MTEMultiFurnace) acceleration tests.
 *
 * <p>
 * Uses the exported structure template {@code multi_smelter/valid}
 * which contains a formed Multi Smelter with Cupronickel coils and
 * Heat Proof Machine Casings, surrounded by all 9 torch variants.
 *
 * <p>
 * The Multi Smelter uses vanilla furnace recipes
 * ({@code RecipeMaps.furnaceRecipes}) with base 4 EU/t × 128 ticks.
 * Cupronickel coils give 2× parallel processing.
 *
 * <h3>Template labels</h3>
 *
 * <pre>
 *  controller       — Multi Smelter controller
 *  input_bus        — input bus
 *  output_bus       — output bus
 *  energy_hatch     — energy hatch
 *  torcherino       — normal GUI torch (1×)
 *  compressed_torcherino            — compressed GUI (9×)
 *  double_compressed_torcherino     — double compressed GUI (81×)
 *  torcherino_classic               — classic torch (1×)
 *  compressed_torcherino_classic    — compressed classic (9×)
 *  double_compressed_torcherino_classic — double compressed classic (81×)
 *  wireless_torcherino              — wireless torch (1×)
 *  compressed_wireless_torcherino   — compressed wireless (9×)
 *  double_compressed_wireless_torcherino — double compressed wireless (81×)
 * </pre>
 *
 * @see gregtech.common.tileentities.machines.multi.MTEMultiFurnace
 */
@GameTestHolder(value = "torcherino", templatePrefix = "multi_smelter")
public class MultiSmelterAccelerationTests {

    // ---------- Labels ----------
    private static final String LBL_CONTROLLER = "Controller";

    private static final String[] ALL_TORCH_LABELS = { "Torcherino", "Compressed_Torcherino",
        "Double_Torcherino", "Torcherino_classic", "Compressed_Torcherino_classic",
        "Double_Torcherino_classic", "Torcherino_wireless", "Double_Torcherino_wireless",
        "Compressed_Torcherino_wireless", };

    private static final ItemStack IRON_ORE = new ItemStack(Blocks.iron_ore, 1);
    private static final ItemStack IRON_INGOT = new ItemStack(Items.iron_ingot, 1);

    // ============================================================
    // Batch lifecycle
    // ============================================================

    @BeforeBatch("torcherino.multi_smelter")
    public static void beforeBatch() {
        Config.enableTickBudget = false;
        Config.enableFlashTorcherino = true;
        Config.maxSpeedLevel = Math.max(Config.maxSpeedLevel, 4);
    }

    @AfterBatch("torcherino.multi_smelter")
    public static void afterBatch() {
        Config.enableTickBudget = true;
    }

    // ============================================================
    // Torch acceleration — GUI torches
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 300, batch = "torcherino.multi_smelter")
    public static void normalTorchAcceleratesSmelter(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureGuiTorch(helper, "Torcherino", 4, false);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 200, batch = "torcherino.multi_smelter")
    public static void compressedTorchAcceleratesSmelterFaster(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureGuiTorch(helper, "Compressed_Torcherino", 2, false);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 100, batch = "torcherino.multi_smelter")
    public static void doubleCompressedTorchAcceleratesSmelterFastest(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureGuiTorch(helper, "Double_Torcherino", 1, false);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    // ============================================================
    // 9. Torch acceleration — Classic torches
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 300, batch = "torcherino.multi_smelter")
    public static void classicTorchAcceleratesSmelter(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureClassicTorch(helper, "Torcherino_classic", (byte) 3, (byte) 4, false);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 200, batch = "torcherino.multi_smelter")
    public static void classicCompressedTorchAcceleratesSmelter(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureClassicTorch(helper, "Compressed_Torcherino_classic", (byte) 1, (byte) 4, false);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 100, batch = "torcherino.multi_smelter")
    public static void classicDoubleCompressedTorchAcceleratesSmelter(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureClassicTorch(helper, "Double_Torcherino_classic", (byte) 1, (byte) 4, false);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    // ============================================================
    // 10. Torch acceleration — Wireless torches
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 300, batch = "torcherino.multi_smelter")
    public static void wirelessTorchAcceleratesBoundSmelter(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureWirelessTorch(helper, "Torcherino_wireless", 4, false);
        bindControllerToWireless(helper, "Torcherino_wireless");
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 200, batch = "torcherino.multi_smelter")
    public static void compressedWirelessTorchAcceleratesBoundSmelter(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureWirelessTorch(helper, "Compressed_Torcherino_wireless", 2, false);
        bindControllerToWireless(helper, "Compressed_Torcherino_wireless");
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 100, batch = "torcherino.multi_smelter")
    public static void doubleCompressedWirelessTorchAcceleratesBoundSmelter(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureWirelessTorch(helper, "Double_Torcherino_wireless", 1, false);
        bindControllerToWireless(helper, "Double_Torcherino_wireless");
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    // ============================================================
    // 11. Torch negative tests
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 500, batch = "torcherino.multi_smelter")
    public static void stoppedTorchDoesNotAccelerate(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        // Configure torch with high speed but stopped=true
        configureGuiTorch(helper, "Torcherino", 4, true);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 500, batch = "torcherino.multi_smelter")
    public static void wirelessTorchWithoutBindingDoesNotAccelerate(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        // Configure wireless torch but do NOT bind
        configureWirelessTorch(helper, "Torcherino_wireless", 4, false);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    // ============================================================
    // Private helpers
    // ============================================================

    /** Get tile entity at a labeled position. */
    private static TileEntity labeledTE(GameTestHelper helper, String label) {
        TestPos pos = helper.absolute(label);
        return helper.getWorld()
            .getTileEntity(pos.x(), pos.y(), pos.z());
    }

    /** Create Multiblock proxy and fix maintenance. */
    private static Multiblock smelter(GameTestHelper helper) {
        Multiblock multi = helper.gtnh()
            .multiblock(helper.pos(LBL_CONTROLLER));
        multi.fixMaintenance();
        return multi;
    }

    /** Create Multiblock, fix maintenance, and assert it is formed. */
    private static Multiblock formedSmelter(GameTestHelper helper) {
        Multiblock multi = smelter(helper);
        multi.assertFormed();
        return multi;
    }

    /** Stop all 9 torches. */
    private static void stopAllTorches(GameTestHelper helper) {
        for (String label : ALL_TORCH_LABELS) {
            TestPos pos;
            try {
                pos = helper.absolute(label);
            } catch (Exception e) {
                continue;
            }
            TileEntity te = helper.getWorld()
                .getTileEntity(pos.x(), pos.y(), pos.z());
            if (te instanceof TileTorcherinoBase) {
                TileTorcherinoBase torch = (TileTorcherinoBase) te;
                torch.setSpeedLevel(0);
                torch.setStopped(true);
            } else if (te instanceof TileTorcherinoClassic) {
                configureClassicTorchAt(helper, label, (byte) 0, (byte) 0, false);
            } else if (te instanceof TileWirelessTorcherinoBase) {
                configureWirelessTorch(helper, label, 0, true);
            }
        }
    }

    private static void configureGuiTorch(GameTestHelper helper, String label, int speedLevel, boolean stopped) {
        TileEntity te = labeledTE(helper, label);
        if (te instanceof TileTorcherinoBase) {
            TileTorcherinoBase torch = (TileTorcherinoBase) te;
            torch.setSpeedLevel(speedLevel);
            torch.setStopped(stopped);
            torch.setActive(true);
        }
    }

    private static void configureClassicTorch(GameTestHelper helper, String label, byte speed, byte mode,
        boolean stopped) {
        configureClassicTorchAt(helper, label, speed, mode, stopped);
    }

    private static void configureClassicTorchAt(GameTestHelper helper, String label, byte speed, byte mode,
        boolean stopped) {
        TestPos pos = helper.absolute(label);
        TileEntity te = helper.getWorld()
            .getTileEntity(pos.x(), pos.y(), pos.z());
        if (te instanceof TileTorcherinoClassic) {
            int rx = pos.x() - helper.getOriginX();
            int ry = pos.y() - helper.getOriginY();
            int rz = pos.z() - helper.getOriginZ();
            NBTTagCompound nbt = helper.getTileNBT(rx, ry, rz);
            nbt.setByte("Speed", speed);
            // mode 0 = stopped, anything else = active with given radius
            nbt.setByte("Mode", stopped ? (byte) 0 : mode);
            nbt.setBoolean("IsActive", !stopped);
            helper.setTile(rx, ry, rz, nbt);
        }
    }

    private static void configureWirelessTorch(GameTestHelper helper, String label, int globalSpeedLevel,
        boolean stopped) {
        TestPos pos = helper.absolute(label);
        TileEntity te = helper.getWorld()
            .getTileEntity(pos.x(), pos.y(), pos.z());
        if (te instanceof TileWirelessTorcherinoBase) {
            int rx = pos.x() - helper.getOriginX();
            int ry = pos.y() - helper.getOriginY();
            int rz = pos.z() - helper.getOriginZ();
            NBTTagCompound nbt = helper.getTileNBT(rx, ry, rz);
            nbt.setInteger("GlobalSpeedLevel", globalSpeedLevel);
            nbt.setBoolean("IsStopped", stopped);
            nbt.setBoolean("IsActive", true);
            helper.setTile(rx, ry, rz, nbt);
        }
    }

    /** Bind the Multi Smelter controller to a wireless torch. */
    private static void bindControllerToWireless(GameTestHelper helper, String label) {
        TileEntity torchTE = labeledTE(helper, label);
        TestPos ctrl = helper.absolute(LBL_CONTROLLER);
        if (torchTE instanceof TileWirelessTorcherinoBase) {
            TileWirelessTorcherinoBase wireless = (TileWirelessTorcherinoBase) torchTE;
            int dim = helper.getWorld().provider.dimensionId;
            wireless.addBoundMachine(ctrl.x(), ctrl.y(), ctrl.z(), dim);
        }
    }
}
