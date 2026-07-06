package com.czqwq.Torcherino.tests;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.block.ModBlocks;
import com.czqwq.Torcherino.tile.TileCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileCompressedTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileTorcherinoAccelerated;
import com.czqwq.Torcherino.tile.TileTorcherinoBase;
import com.czqwq.Torcherino.tile.TileTorcherinoClassic;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.BeforeBatch;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

/**
 * Tests for area-based (non-wireless) Torcherino acceleration effects.
 *
 * <h3>Test strategy</h3>
 * Place a furnace with fuel and smeltable items, place a torch nearby,
 * then measure how fast the furnace progresses relative to expected normal
 * speed. A furnace normally takes 200 ticks to smelt one item. With a
 * torch at effective speed {@code S}, the furnace is ticked
 * {@code (1 + S)} times per world tick (1 normal + S accelerated calls).
 *
 * <h3>Torch tier summary</h3>
 * <table>
 * <tr>
 * <th>Tier</th>
 * <th>Multiplier</th>
 * <th>Speed 1 effect</th>
 * </tr>
 * <tr>
 * <td>Normal (Accelerated / Classic)</td>
 * <td>1×</td>
 * <td>2 ticks/wt</td>
 * </tr>
 * <tr>
 * <td>Compressed</td>
 * <td>9×</td>
 * <td>10 ticks/wt</td>
 * </tr>
 * <tr>
 * <td>Double Compressed</td>
 * <td>81×</td>
 * <td>82 ticks/wt</td>
 * </tr>
 * </table>
 * Effective speed = {@code speedLevel × multiplier}.
 *
 * @see TileTorcherinoBase
 * @see TileTorcherinoClassic
 */
@GameTestHolder(Torcherino.MODID)
public class TorchAccelerationTests {

    /**
     * Normal furnace smelt time (ticks).
     */
    private static final int SMELT_TICKS = 200;

    // ---------- Shared test-relative coordinates ----------
    // Furnace at origin, torch 1 block away on X axis
    private static final int FX = 0, FY = 0, FZ = 0; // furnace
    private static final int TX = 1, TY = 0, TZ = 0; // torch

    // ---------- Items ----------
    private static final ItemStack COAL = new ItemStack(Items.coal, 64);
    private static final ItemStack IRON_ORE = new ItemStack(Blocks.iron_ore, 1);

    // ============================================================
    // Batch lifecycle
    // ============================================================

    @BeforeBatch("torch_accel")
    public static void beforeBatch() {
        Config.enableTickBudget = false;
    }

    // ============================================================
    // 1. Tile entity identity tests
    // ============================================================

    @GameTest(timeoutTicks = 40, batch = "torch_accel")
    public static void acceleratedTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.torcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = helper.getWorld()
                    .getTileEntity(helper.getOriginX() + TX, helper.getOriginY() + TY, helper.getOriginZ() + TZ);
                helper.assertTrue(
                    te instanceof TileTorcherinoAccelerated,
                    "Expected TileTorcherinoAccelerated, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 40, batch = "torch_accel")
    public static void compressedTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.compressedTorcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = worldTE(helper, TX, TY, TZ);
                helper.assertTrue(
                    te instanceof TileCompressedTorcherino,
                    "Expected TileCompressedTorcherino, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 40, batch = "torch_accel")
    public static void doubleCompressedTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.doubleCompressedTorcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = worldTE(helper, TX, TY, TZ);
                helper.assertTrue(
                    te instanceof TileDoubleCompressedTorcherino,
                    "Expected TileDoubleCompressedTorcherino, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 40, batch = "torch_accel")
    public static void classicTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.torcherinoClassic);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = worldTE(helper, TX, TY, TZ);
                helper.assertTrue(
                    te instanceof TileTorcherinoClassic,
                    "Expected TileTorcherinoClassic, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 40, batch = "torch_accel")
    public static void classicCompressedTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.compressedTorcherinoClassic);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = worldTE(helper, TX, TY, TZ);
                helper.assertTrue(
                    te instanceof TileCompressedTorcherinoClassic,
                    "Expected TileCompressedTorcherinoClassic, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 40, batch = "torch_accel")
    public static void classicDoubleCompressedTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.doubleCompressedTorcherinoClassic);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = worldTE(helper, TX, TY, TZ);
                helper.assertTrue(
                    te instanceof TileDoubleCompressedTorcherinoClassic,
                    "Expected TileDoubleCompressedTorcherinoClassic, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    // ============================================================
    // 2. Furnace acceleration — positive tests (GUI torches)
    // ============================================================

    /**
     * Baseline: furnace smelts normally without any torch.
     * After 200+ ticks the output slot should contain an iron ingot.
     */
    @GameTest(timeoutTicks = 300, batch = "torch_accel")
    public static void furnaceBaselineNoTorch(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Normal (Accelerated) torch at speed 1 (100%): 2 updates/tick → ~100 ticks.
     */
    @GameTest(timeoutTicks = 200, batch = "torch_accel")
    public static void acceleratedTorchSpeed1SmeltsFaster(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupGuiTorch(helper, TX, TY, TZ, ModBlocks.torcherino, 1, 1, 0, 0);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Normal torch at speed 4 (400%): 5 updates/tick → ~40 ticks.
     */
    @GameTest(timeoutTicks = 100, batch = "torch_accel")
    public static void acceleratedTorchSpeed4SmeltsVeryFast(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupGuiTorch(helper, TX, TY, TZ, ModBlocks.torcherino, 4, 1, 0, 0);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Compressed torch at speed 1 (900%): 10 updates/tick → ~20 ticks.
     */
    @GameTest(timeoutTicks = 60, batch = "torch_accel")
    public static void compressedTorchSpeed1SmeltsFast(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupGuiTorch(helper, TX, TY, TZ, ModBlocks.compressedTorcherino, 1, 1, 0, 0);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Double compressed torch at speed 1 (8100%): 82 updates/tick → ~3 ticks.
     */
    @GameTest(timeoutTicks = 30, batch = "torch_accel")
    public static void doubleCompressedTorchSpeed1SmeltsInstantly(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupGuiTorch(helper, TX, TY, TZ, ModBlocks.doubleCompressedTorcherino, 1, 1, 0, 0);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    // ============================================================
    // 3. Classic torch furnace acceleration
    // ============================================================

    /**
     * Classic torch at speed 1 (100%), mode 1 (radius 1): 2 updates/tick → ~100 ticks.
     */
    @GameTest(timeoutTicks = 200, batch = "torch_accel")
    public static void classicTorchSpeed1AcceleratesFurnace(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupClassicTorch(helper, TX, TY, TZ, ModBlocks.torcherinoClassic, (byte) 1, (byte) 1);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Classic compressed torch at speed 1 (900%): 10 updates/tick → ~20 ticks.
     */
    @GameTest(timeoutTicks = 60, batch = "torch_accel")
    public static void classicCompressedTorchAcceleratesFurnace(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupClassicTorch(helper, TX, TY, TZ, ModBlocks.compressedTorcherinoClassic, (byte) 1, (byte) 1);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Classic double compressed torch at speed 1 (8100%): 82 updates/tick → ~3 ticks.
     */
    @GameTest(timeoutTicks = 30, batch = "torch_accel")
    public static void classicDoubleCompressedTorchAcceleratesFurnace(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupClassicTorch(helper, TX, TY, TZ, ModBlocks.doubleCompressedTorcherinoClassic, (byte) 1, (byte) 1);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    // ============================================================
    // 4. Negative / boundary tests
    // ============================================================

    /**
     * Torch with speed=0 should produce no acceleration.
     * The furnace should still need ~200 ticks to smelt.
     */
    @GameTest(timeoutTicks = 300, batch = "torch_accel")
    public static void speedZeroDoesNotAccelerate(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupGuiTorch(helper, TX, TY, TZ, ModBlocks.torcherino, 0, 1, 0, 0);
        fuelFurnace(helper, FX, FY, FZ);
        // Verify it's NOT done at 60 ticks (would be done if speed 1+)
        helper.startSequence()
            .thenIdle(60)
            .thenExecute(() -> {
                if (furnaceHasOutput(helper, FX, FY, FZ)) {
                    helper.fail("Furnace smelted too fast — speed=0 torch should not accelerate");
                }
            });
        // Should complete by ~200 ticks (normal speed)
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Furnace outside the torch radius should smelt at normal speed.
     * Torch at (TX,TY,TZ) with radius 0 — only accelerates itself.
     * Furnace at (FX,FY,FZ) = (0,0,0) is one block away → outside radius.
     */
    @GameTest(timeoutTicks = 300, batch = "torch_accel")
    public static void furnaceOutsideRadiusSmeltsNormally(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupGuiTorch(helper, TX, TY, TZ, ModBlocks.torcherino, 4, 0, 0, 0);
        fuelFurnace(helper, FX, FY, FZ);
        // At 50 ticks with radius=0, should NOT be done
        helper.startSequence()
            .thenIdle(50)
            .thenExecute(() -> {
                if (furnaceHasOutput(helper, FX, FY, FZ)) {
                    helper.fail("Furnace outside radius was accelerated");
                }
            });
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Stopped torch (isStopped=true) should not accelerate even with high speed.
     */
    @GameTest(timeoutTicks = 300, batch = "torch_accel")
    public static void stoppedTorchDoesNotAccelerate(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupGuiTorch(helper, TX, TY, TZ, ModBlocks.torcherino, 4, 1, 0, 0);
        // Override: set stopped
        setTorchStopped(helper, TX, TY, TZ, true);
        fuelFurnace(helper, FX, FY, FZ);
        helper.startSequence()
            .thenIdle(50)
            .thenExecute(() -> {
                if (furnaceHasOutput(helper, FX, FY, FZ)) {
                    helper.fail("Stopped torch should not accelerate");
                }
            });
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Y-axis radius 1 covers furnace at Y+1.
     */
    @GameTest(timeoutTicks = 200, batch = "torch_accel")
    public static void yRadiusCoversFurnaceAbove(GameTestHelper helper) {
        final int fy = 1; // furnace one block above torch
        setupFurnace(helper, 0, fy, 0);
        setupGuiTorch(helper, 0, 0, 0, ModBlocks.torcherino, 2, 0, 1, 0);
        fuelFurnace(helper, 0, fy, 0);
        // 1 + 2 = 3 updates/tick → ~67 ticks
        helper.succeedWhen(() -> furnaceHasOutput(helper, 0, fy, 0));
    }

    /**
     * Y-axis radius 0 does NOT cover furnace at Y+1.
     */
    @GameTest(timeoutTicks = 300, batch = "torch_accel")
    public static void yRadiusZeroExcludesFurnaceAbove(GameTestHelper helper) {
        final int fy = 1;
        setupFurnace(helper, 0, fy, 0);
        setupGuiTorch(helper, 0, 0, 0, ModBlocks.torcherino, 3, 0, 0, 0);
        fuelFurnace(helper, 0, fy, 0);
        helper.startSequence()
            .thenIdle(50)
            .thenExecute(() -> {
                if (furnaceHasOutput(helper, 0, fy, 0)) {
                    helper.fail("Furnace above Y radius was accelerated");
                }
            });
        helper.succeedWhen(() -> furnaceHasOutput(helper, 0, fy, 0));
    }

    // ============================================================
    // 5. Torch-on-torch: no recursion
    // ============================================================

    /**
     * Two torches next to each other should not recurse infinitely.
     * If AccelerationHelper.isTorcherinoTile works correctly, each torch
     * skips the other.
     */
    @GameTest(timeoutTicks = 60, batch = "torch_accel")
    public static void twoTorchesDoNotRecurse(GameTestHelper helper) {
        setupGuiTorch(helper, 0, 0, 0, ModBlocks.torcherino, 2, 1, 0, 0);
        setupGuiTorch(helper, 1, 0, 0, ModBlocks.torcherino, 2, 1, 0, 0);
        // Just survive 30 ticks without crash/hang
        helper.startSequence()
            .thenIdle(30)
            .thenSucceed();
    }

    // ============================================================
    // 6. Effective speed calculation
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "torch_accel")
    public static void effectiveSpeedIsSpeedLevelTimesMultiplier(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.torcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileTorcherinoBase torch = (TileTorcherinoBase) worldTE(helper, TX, TY, TZ);
                torch.setSpeedLevel(3);
                helper
                    .assertEquals(3, torch.getEffectiveSpeed(), "Speed 3 × multiplier 1 should give effective speed 3");
                torch.setSpeedLevel(0);
                helper
                    .assertEquals(0, torch.getEffectiveSpeed(), "Speed 0 × multiplier 1 should give effective speed 0");
                torch.setSpeedLevel(4);
                helper
                    .assertEquals(4, torch.getEffectiveSpeed(), "Speed 4 × multiplier 1 should give effective speed 4");
            })
            .thenSucceed();
    }

    // ============================================================
    // 7. Config clamping
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "torch_accel")
    public static void speedLevelClampedToConfigMax(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.torcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileTorcherinoBase torch = (TileTorcherinoBase) worldTE(helper, TX, TY, TZ);
                int savedMax = Config.maxSpeedLevel;
                try {
                    Config.maxSpeedLevel = 2;
                    torch.setSpeedLevel(5); // should clamp to 2
                    helper.assertEquals(
                        2,
                        torch.getSpeedLevel(),
                        "Speed level should be clamped to config maxSpeedLevel");
                } finally {
                    Config.maxSpeedLevel = savedMax;
                }
            })
            .thenSucceed();
    }

    // ============================================================
    // Helper methods
    // ============================================================

    /** Get a world-absolute TileEntity at test-relative (x,y,z). */
    private static TileEntity worldTE(GameTestHelper helper, int x, int y, int z) {
        return helper.getWorld()
            .getTileEntity(helper.getOriginX() + x, helper.getOriginY() + y, helper.getOriginZ() + z);
    }

    /** Place a furnace block and wait for TE spawn. */
    private static void setupFurnace(GameTestHelper helper, int x, int y, int z) {
        helper.setBlock(x, y, z, Blocks.furnace);
    }

    /**
     * Put input (slot 0) and fuel (slot 1) into a furnace.
     * Insert order matters: iron ore first occupies slot 0, then coal
     * skips occupied slot 0 and lands in fuel slot 1.
     */
    private static void fuelFurnace(GameTestHelper helper, int x, int y, int z) {
        helper.insertItem(x, y, z, IRON_ORE.copy());
        helper.insertItem(x, y, z, COAL.copy());
    }

    /**
     * Place a GUI-operated torch, configure it via NBT-write then
     * direct field setters for speed/radius/active state.
     */
    private static void setupGuiTorch(GameTestHelper helper, int x, int y, int z, Block torchBlock, int speedLevel,
        int xRadius, int yRadius, int zRadius) {
        helper.setBlock(x, y, z, torchBlock);
        TileEntity te = worldTE(helper, x, y, z);
        if (te instanceof TileTorcherinoBase) {
            TileTorcherinoBase torch = (TileTorcherinoBase) te;
            torch.setSpeedLevel(speedLevel);
            torch.setXRadius(xRadius);
            torch.setYRadius(yRadius);
            torch.setZRadius(zRadius);
            torch.setStopped(false);
            torch.setActive(true);
        }
    }

    /**
     * Configure a classic torch via NBT. Classic torches read "Speed",
     * "Mode", and "IsActive" keys from NBT.
     */
    private static void setupClassicTorch(GameTestHelper helper, int x, int y, int z, Block torchBlock, byte speed,
        byte mode) {
        helper.setBlock(x, y, z, torchBlock);
        NBTTagCompound nbt = helper.getTileNBT(x, y, z);
        nbt.setByte("Speed", speed);
        nbt.setByte("Mode", mode);
        nbt.setBoolean("IsActive", true);
        helper.setTile(x, y, z, nbt);
    }

    /** Mark a GUI torch as stopped. */
    private static void setTorchStopped(GameTestHelper helper, int x, int y, int z, boolean stopped) {
        TileEntity te = worldTE(helper, x, y, z);
        if (te instanceof TileTorcherinoBase) {
            ((TileTorcherinoBase) te).setStopped(stopped);
        }
    }

    /** Check if a furnace output slot (index 2) is non-empty. */
    private static boolean furnaceHasOutput(GameTestHelper helper, int x, int y, int z) {
        TileEntity te = worldTE(helper, x, y, z);
        if (!(te instanceof IInventory)) return false;
        IInventory inv = (IInventory) te;
        ItemStack output = inv.getStackInSlot(2);
        return output != null && output.stackSize > 0;
    }
}
