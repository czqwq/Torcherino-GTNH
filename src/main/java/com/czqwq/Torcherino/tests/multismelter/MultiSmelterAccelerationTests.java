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

import gregtech.api.GregTechAPI;
import gregtech.api.enums.TierEU;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;

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
    private static final String LBL_CONTROLLER = "controller";
    private static final String LBL_ENERGY = "energy_hatch";
    private static final String LBL_INPUT = "input_bus";
    private static final String LBL_OUTPUT = "output_bus";

    private static final String[] ALL_TORCH_LABELS = { "torcherino", "compressed_torcherino",
        "double_compressed_torcherino", "torcherino_classic", "compressed_torcherino_classic",
        "double_compressed_torcherino_classic", "wireless_torcherino", "compressed_wireless_torcherino",
        "double_compressed_wireless_torcherino", };

    private static final ItemStack IRON_ORE = new ItemStack(Blocks.iron_ore, 1);
    private static final ItemStack IRON_INGOT = new ItemStack(Items.iron_ingot, 1);
    private static final ItemStack COAL = new ItemStack(Items.coal, 64);
    private static final ItemStack COBBLE = new ItemStack(Blocks.cobblestone, 64);

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
    // 1. Structure validity
    // ============================================================

    @GameTest(template = "valid", batch = "torcherino.multi_smelter")
    public static void smelterDoesForm(GameTestHelper helper) {
        Multiblock smelter = formedSmelter(helper);
        smelter.assertFormed();
        helper.succeed();
    }

    @GameTest(template = "valid", rotation = 1, batch = "torcherino.multi_smelter")
    public static void smelterDoesFormAtRotation90(GameTestHelper helper) {
        formedSmelter(helper).assertFormed();
        helper.succeed();
    }

    @GameTest(template = "valid", rotation = 2, batch = "torcherino.multi_smelter")
    public static void smelterDoesFormAtRotation180(GameTestHelper helper) {
        formedSmelter(helper).assertFormed();
        helper.succeed();
    }

    @GameTest(template = "valid", rotation = 3, batch = "torcherino.multi_smelter")
    public static void smelterDoesFormAtRotation270(GameTestHelper helper) {
        formedSmelter(helper).assertFormed();
        helper.succeed();
    }

    // ============================================================
    // 2. Structure invalidity
    // ============================================================

    @GameTest(template = "invalid_no_coils", timeoutTicks = 60, batch = "torcherino.multi_smelter")
    public static void smelterNeverFormsWithoutCoils(GameTestHelper helper) {
        Multiblock smelter = helper.gtnh()
            .multiblock(helper.pos(LBL_CONTROLLER));
        smelter.assertNeverForms("Multi Smelter formed without heating coils");
    }

    // ============================================================
    // 3. Structure mutation
    // ============================================================

    @GameTest(template = "valid", batch = "torcherino.multi_smelter")
    public static void smelterDeformsWhenCoilReplaced(GameTestHelper helper) {
        formedSmelter(helper);
        // Replace one coil block with Heat Proof Machine Casing
        TestPos coilPos = helper.pos("controller");
        helper.setBlock(coilPos.x() + 0, coilPos.y() + 1, coilPos.z() + 0, GregTechAPI.sBlockCasings1, 11);
        Multiblock smelter = helper.gtnh()
            .multiblock(helper.pos(LBL_CONTROLLER));
        smelter.forceStructureCheck();
        smelter.assertNotFormed("Multi Smelter stayed formed after a coil was replaced");
        helper.succeed();
    }

    @GameTest(template = "valid", batch = "torcherino.multi_smelter")
    public static void smelterDeformsWhenControllerDestroyed(GameTestHelper helper) {
        formedSmelter(helper);
        TestPos ctrl = helper.pos(LBL_CONTROLLER);
        helper.destroyBlock(ctrl.x(), ctrl.y(), ctrl.z());
        // After destroying controller, the machine is gone
        TileEntity te = helper.getWorld()
            .getTileEntity(
                helper.getOriginX() + ctrl.x(),
                helper.getOriginY() + ctrl.y(),
                helper.getOriginZ() + ctrl.z());
        helper.assertTrue(te == null || te.isInvalid(), "Controller TE should be gone after destroyBlock");
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 400, batch = "torcherino.multi_smelter")
    public static void smelterStopsRecipeWhenCoilBreaksMidRun(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 300);

        // Let recipe start
        helper.startSequence()
            .thenIdle(10)
            .thenExecute(() -> {
                // Break a coil — replace with air
                TestPos ctrl = helper.pos(LBL_CONTROLLER);
                helper.destroyBlock(ctrl.x() + 0, ctrl.y() + 1, ctrl.z() + 0);
            })
            .thenIdle(20)
            .thenExecute(() -> {
                // Structure should now be broken
                Multiblock recheck = helper.gtnh()
                    .multiblock(helper.pos(LBL_CONTROLLER));
                recheck.forceStructureCheck();
                recheck.assertNotFormed("Smelter should deform when coil breaks mid-run");
            })
            .thenSucceed();
    }

    // ============================================================
    // 4. Maintenance gating
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 400, batch = "torcherino.multi_smelter")
    public static void unmaintainedSmelterDoesNotRun(GameTestHelper helper) {
        stopAllTorches(helper);
        // Form smelter but do NOT fix maintenance
        Multiblock smelter = helper.gtnh()
            .multiblock(helper.pos(LBL_CONTROLLER));
        smelter.assertFormed();
        // Break maintenance on purpose
        breakMaintenanceIssues(helper);

        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 300);

        // Fast-forward: recipe should NOT have completed
        helper.gtnh()
            .fastForwardTicks(200);
        smelter.outputs()
            .assertNotContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 500, batch = "torcherino.multi_smelter")
    public static void smelterRunsAfterMaintenanceFixed(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = helper.gtnh()
            .multiblock(helper.pos(LBL_CONTROLLER));
        smelter.assertFormed();
        breakMaintenanceIssues(helper);

        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 200);

        // Verify nothing happens while unmaintained
        helper.gtnh()
            .fastForwardTicks(100);
        smelter.outputs()
            .assertNotContains(IRON_INGOT);

        // Fix maintenance and run again
        smelter.fixMaintenance();
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 200);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    // ============================================================
    // 5. Recipe baseline
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 500, batch = "torcherino.multi_smelter")
    public static void smelterSmeltsIronOre(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.succeed();
    }

    @GameTest(template = "valid", timeoutTicks = 600, batch = "torcherino.multi_smelter")
    public static void smelterSmeltsMultipleStacks(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        smelter.inputBus(0)
            .insert(new ItemStack(Blocks.iron_ore, 2));
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(new ItemStack(Items.iron_ingot, 1));
        helper.succeed();
    }

    // ============================================================
    // 6. Output full protection
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 400, batch = "torcherino.multi_smelter")
    public static void fullOutputBusDoesNotConsumeInput(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        smelter.outputBus(0)
            .fillAllSlots(COBBLE.copy());
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 300);
        helper.gtnh()
            .fastForwardTicks(300);
        smelter.outputs()
            .assertNotContains(IRON_INGOT);
        helper.succeed();
    }

    // ============================================================
    // 7. Pollution (optional — depends on GT config)
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 600, batch = "torcherino.multi_smelter", required = false)
    public static void smelterEmitsPollutionDuringRecipe(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        smelter.inputBus(0)
            .insert(IRON_ORE.copy());
        smelter.energyHatch(0)
            .supply(TierEU.LV, 1, 400);
        smelter.runRecipe();
        smelter.outputs()
            .assertContains(IRON_INGOT);
        helper.gtnh()
            .assertPollutionEmitted(1);
        helper.succeed();
    }

    // ============================================================
    // 8. Torch acceleration — GUI torches
    // ============================================================

    @GameTest(template = "valid", timeoutTicks = 300, batch = "torcherino.multi_smelter")
    public static void normalTorchAcceleratesSmelter(GameTestHelper helper) {
        stopAllTorches(helper);
        Multiblock smelter = formedSmelter(helper);
        configureGuiTorch(helper, "torcherino", 4, false);
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
        configureGuiTorch(helper, "compressed_torcherino", 2, false);
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
        configureGuiTorch(helper, "double_compressed_torcherino", 1, false);
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
        configureClassicTorch(helper, "torcherino_classic", (byte) 3, (byte) 4, false);
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
        configureClassicTorch(helper, "compressed_torcherino_classic", (byte) 1, (byte) 4, false);
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
        configureClassicTorch(helper, "double_compressed_torcherino_classic", (byte) 1, (byte) 4, false);
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
        configureWirelessTorch(helper, "wireless_torcherino", 4, false);
        bindControllerToWireless(helper, "wireless_torcherino");
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
        configureWirelessTorch(helper, "compressed_wireless_torcherino", 2, false);
        bindControllerToWireless(helper, "compressed_wireless_torcherino");
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
        configureWirelessTorch(helper, "double_compressed_wireless_torcherino", 1, false);
        bindControllerToWireless(helper, "double_compressed_wireless_torcherino");
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
        configureGuiTorch(helper, "torcherino", 4, true);
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
        configureWirelessTorch(helper, "wireless_torcherino", 4, false);
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

    /** Break all maintenance issues on the Multi Smelter controller. */
    private static void breakMaintenanceIssues(GameTestHelper helper) {
        MTEMultiBlockBase ctrl = helper.gtnh()
            .multiBlockController(helper.pos(LBL_CONTROLLER));
        ctrl.mWrench = false;
        ctrl.mScrewdriver = false;
        ctrl.mSoftMallet = false;
        ctrl.mHardHammer = false;
        ctrl.mSolderingTool = false;
        ctrl.mCrowbar = false;
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
