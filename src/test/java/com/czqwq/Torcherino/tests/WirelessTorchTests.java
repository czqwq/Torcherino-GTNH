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
import com.czqwq.Torcherino.tile.TileCompressedWirelessTorcherino;
import com.czqwq.Torcherino.tile.TileDoubleCompressedWirelessTorcherino;
import com.czqwq.Torcherino.tile.TileWirelessTorcherino;
import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.BeforeBatch;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

/**
 * Tests for wireless (flash-bound) Torcherino acceleration.
 *
 * <h3>How wireless torches differ</h3>
 * Unlike area-based torches, wireless torches only accelerate explicitly
 * bound machines. Binding is done via DataStick in-game, but for tests
 * we use the {@code addBoundMachine()} API directly on the tile entity.
 *
 * <h3>Wireless torch tiers</h3>
 * <table>
 * <tr>
 * <th>Tier</th>
 * <th>Multiplier</th>
 * </tr>
 * <tr>
 * <td>Wireless (normal)</td>
 * <td>1×</td>
 * </tr>
 * <tr>
 * <td>Compressed Wireless</td>
 * <td>9×</td>
 * </tr>
 * <tr>
 * <td>Double Compressed Wireless</td>
 * <td>81×</td>
 * </tr>
 * </table>
 *
 * @see TileWirelessTorcherinoBase
 */
@GameTestHolder(Torcherino.MODID)
public class WirelessTorchTests {

    // Test-relative coordinates
    private static final int FX = 0, FY = 0, FZ = 0; // furnace (target machine)
    private static final int TX = 1, TY = 0, TZ = 0; // wireless torch

    private static final ItemStack COAL = new ItemStack(Items.coal, 64);
    private static final ItemStack IRON_ORE = new ItemStack(Blocks.iron_ore, 1);

    @BeforeBatch("wireless_accel")
    public static void beforeBatch() {
        Config.enableTickBudget = false;
        Config.enableFlashTorcherino = true;
        Config.wirelessTorchRadius = 8;
    }

    // ============================================================
    // 1. Tile entity identity
    // ============================================================

    @GameTest(timeoutTicks = 40, batch = "wireless_accel")
    public static void wirelessTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.wirelessTorcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = worldTE(helper, TX, TY, TZ);
                helper.assertTrue(
                    te instanceof TileWirelessTorcherino,
                    "Expected TileWirelessTorcherino, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 40, batch = "wireless_accel")
    public static void compressedWirelessTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.compressedWirelessTorcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = worldTE(helper, TX, TY, TZ);
                helper.assertTrue(
                    te instanceof TileCompressedWirelessTorcherino,
                    "Expected TileCompressedWirelessTorcherino, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 40, batch = "wireless_accel")
    public static void doubleCompressedWirelessTorchIsCorrectType(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.doubleCompressedWirelessTorcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileEntity te = worldTE(helper, TX, TY, TZ);
                helper.assertTrue(
                    te instanceof TileDoubleCompressedWirelessTorcherino,
                    "Expected TileDoubleCompressedWirelessTorcherino, got " + (te == null ? "null"
                        : te.getClass()
                            .getSimpleName()));
            })
            .thenSucceed();
    }

    // ============================================================
    // 2. Speed multiplier accessors
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "wireless_accel")
    public static void wirelessTorchMultiplierIs1x(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.wirelessTorcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
                helper.assertEquals(1, torch.getSpeedMultiplier(), "Wireless torch multiplier should be 1");
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 20, batch = "wireless_accel")
    public static void compressedWirelessTorchMultiplierIs9x(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.compressedWirelessTorcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
                helper.assertEquals(9, torch.getSpeedMultiplier(), "Compressed wireless torch multiplier should be 9");
            })
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 20, batch = "wireless_accel")
    public static void doubleCompressedWirelessTorchMultiplierIs81x(GameTestHelper helper) {
        helper.setBlock(TX, TY, TZ, ModBlocks.doubleCompressedWirelessTorcherino);
        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
                helper.assertEquals(
                    81,
                    torch.getSpeedMultiplier(),
                    "Double compressed wireless torch multiplier should be 81");
            })
            .thenSucceed();
    }

    // ============================================================
    // 3. Binding and unbinding
    // ============================================================

    /**
     * Binding a machine within range succeeds.
     */
    @GameTest(timeoutTicks = 40, batch = "wireless_accel")
    public static void bindMachineWithinRange(GameTestHelper helper) {
        helper.setBlock(FX, FY, FZ, Blocks.furnace); // target
        helper.setBlock(TX, TY, TZ, ModBlocks.wirelessTorcherino); // torch

        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
                int dim = helper.getWorld().provider.dimensionId;
                boolean added = torch
                    .addBoundMachine(helper.getOriginX() + FX, helper.getOriginY() + FY, helper.getOriginZ() + FZ, dim);
                helper.assertTrue(added, "Should successfully bind furnace within range");
                helper.assertEquals(
                    1,
                    torch.getBoundMachines()
                        .size(),
                    "Should have 1 bound machine");
            })
            .thenSucceed();
    }

    /**
     * Removing a bound machine works.
     */
    @GameTest(timeoutTicks = 40, batch = "wireless_accel")
    public static void unbindMachine(GameTestHelper helper) {
        helper.setBlock(FX, FY, FZ, Blocks.furnace);
        helper.setBlock(TX, TY, TZ, ModBlocks.wirelessTorcherino);

        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
                int dim = helper.getWorld().provider.dimensionId;
                int wx = helper.getOriginX() + FX;
                int wy = helper.getOriginY() + FY;
                int wz = helper.getOriginZ() + FZ;

                torch.addBoundMachine(wx, wy, wz, dim);
                helper.assertEquals(
                    1,
                    torch.getBoundMachines()
                        .size());

                torch.removeBoundMachine(wx, wy, wz, dim);
                helper.assertEquals(
                    0,
                    torch.getBoundMachines()
                        .size(),
                    "Bound list should be empty after removal");
            })
            .thenSucceed();
    }

    /**
     * Binding the same machine twice should deduplicate (no duplicate entries).
     */
    @GameTest(timeoutTicks = 40, batch = "wireless_accel")
    public static void bindDuplicateMachineIsDeduped(GameTestHelper helper) {
        helper.setBlock(FX, FY, FZ, Blocks.furnace);
        helper.setBlock(TX, TY, TZ, ModBlocks.wirelessTorcherino);

        helper.startSequence()
            .thenIdle(1)
            .thenExecute(() -> {
                TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
                int dim = helper.getWorld().provider.dimensionId;
                int wx = helper.getOriginX() + FX;
                int wy = helper.getOriginY() + FY;
                int wz = helper.getOriginZ() + FZ;

                helper.assertTrue(torch.addBoundMachine(wx, wy, wz, dim), "First bind should succeed");
                helper.assertTrue(torch.addBoundMachine(wx, wy, wz, dim), "Second bind should be no-op (true)");
                helper.assertEquals(
                    1,
                    torch.getBoundMachines()
                        .size(),
                    "Duplicate binding should not increase list size");
            })
            .thenSucceed();
    }

    // ============================================================
    // 4. Wireless acceleration — positive tests
    // ============================================================

    /**
     * Wireless torch with speed 1 (100%) accelerates a bound furnace.
     * 2 updates/tick → ~100 ticks to smelt.
     */
    @GameTest(timeoutTicks = 200, batch = "wireless_accel")
    public static void wirelessTorchAcceleratesBoundFurnace(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupWirelessTorch(helper, TX, TY, TZ, ModBlocks.wirelessTorcherino, 1);
        bindFurnaceToTorch(helper);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Compressed wireless torch (9×) at speed 1: 10 updates/tick → ~20 ticks.
     */
    @GameTest(timeoutTicks = 60, batch = "wireless_accel")
    public static void compressedWirelessTorchAcceleratesBoundFurnace(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupWirelessTorch(helper, TX, TY, TZ, ModBlocks.compressedWirelessTorcherino, 1);
        bindFurnaceToTorch(helper);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * Double compressed wireless torch (81×) at speed 1: 82 updates/tick → ~3 ticks.
     */
    @GameTest(timeoutTicks = 30, batch = "wireless_accel")
    public static void doubleCompressedWirelessTorchAcceleratesBoundFurnace(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupWirelessTorch(helper, TX, TY, TZ, ModBlocks.doubleCompressedWirelessTorcherino, 1);
        bindFurnaceToTorch(helper);
        fuelFurnace(helper, FX, FY, FZ);
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    // ============================================================
    // 5. Negative tests — unbound machines not accelerated
    // ============================================================

    /**
     * A wireless torch with no bound machines should not accelerate anything.
     */
    @GameTest(timeoutTicks = 300, batch = "wireless_accel")
    public static void unboundFurnaceNotAccelerated(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        // Place torch with high speed but do NOT bind the furnace
        setupWirelessTorch(helper, TX, TY, TZ, ModBlocks.wirelessTorcherino, 4);
        fuelFurnace(helper, FX, FY, FZ);

        // Should NOT be done at 60 ticks
        helper.startSequence()
            .thenIdle(60)
            .thenExecute(() -> {
                if (furnaceHasOutput(helper, FX, FY, FZ)) {
                    helper.fail("Unbound furnace was accelerated by wireless torch");
                }
            });
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    /**
     * After unbinding a machine, it should no longer be accelerated.
     */
    @GameTest(timeoutTicks = 300, batch = "wireless_accel")
    public static void unboundMachineIsNotAccelerated(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupWirelessTorch(helper, TX, TY, TZ, ModBlocks.wirelessTorcherino, 4);
        bindFurnaceToTorch(helper);

        // Unbind immediately
        TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
        int dim = helper.getWorld().provider.dimensionId;
        torch.removeBoundMachine(helper.getOriginX() + FX, helper.getOriginY() + FY, helper.getOriginZ() + FZ, dim);

        fuelFurnace(helper, FX, FY, FZ);
        helper.startSequence()
            .thenIdle(60)
            .thenExecute(() -> {
                if (furnaceHasOutput(helper, FX, FY, FZ)) {
                    helper.fail("Unbound machine was still accelerated");
                }
            });
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    // ============================================================
    // 6. Per-machine speed override
    // ============================================================

    /**
     * Per-machine speed override takes precedence over global speed.
     * Set global speed=0 (no acceleration) but per-machine speed=2:
     * the machine should still be accelerated.
     */
    @GameTest(timeoutTicks = 200, batch = "wireless_accel")
    public static void perMachineSpeedOverridesGlobal(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        setupWirelessTorch(helper, TX, TY, TZ, ModBlocks.wirelessTorcherino, 0);

        TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
        int dim = helper.getWorld().provider.dimensionId;
        int wx = helper.getOriginX() + FX;
        int wy = helper.getOriginY() + FY;
        int wz = helper.getOriginZ() + FZ;
        torch.addBoundMachine(wx, wy, wz, dim);
        torch.setPerMachineSpeed(wx, wy, wz, dim, 2); // per-machine speed=2 → 2*1=2 effective

        fuelFurnace(helper, FX, FY, FZ);
        // 1 + 2 = 3 updates/tick → ~67 ticks
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    // ============================================================
    // 7. Stopped wireless torch
    // ============================================================

    /**
     * A stopped wireless torch should not accelerate even with bound machines.
     */
    @GameTest(timeoutTicks = 300, batch = "wireless_accel")
    public static void stoppedWirelessTorchDoesNotAccelerate(GameTestHelper helper) {
        setupFurnace(helper, FX, FY, FZ);
        // Place torch with high speed, then override: stopped
        setupWirelessTorch(helper, TX, TY, TZ, ModBlocks.wirelessTorcherino, 4);
        // Re-configure NBT to set stopped=true
        NBTTagCompound nbt = helper.getTileNBT(TX, TY, TZ);
        nbt.setInteger("GlobalSpeedLevel", 4);
        nbt.setBoolean("IsStopped", true);
        nbt.setBoolean("IsActive", true);
        helper.setTile(TX, TY, TZ, nbt);
        bindFurnaceToTorch(helper);

        fuelFurnace(helper, FX, FY, FZ);
        helper.startSequence()
            .thenIdle(60)
            .thenExecute(() -> {
                if (furnaceHasOutput(helper, FX, FY, FZ)) {
                    helper.fail("Stopped wireless torch should not accelerate");
                }
            });
        helper.succeedWhen(() -> furnaceHasOutput(helper, FX, FY, FZ));
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private static TileEntity worldTE(GameTestHelper helper, int x, int y, int z) {
        return helper.getWorld()
            .getTileEntity(helper.getOriginX() + x, helper.getOriginY() + y, helper.getOriginZ() + z);
    }

    private static void setupFurnace(GameTestHelper helper, int x, int y, int z) {
        helper.setBlock(x, y, z, Blocks.furnace);
    }

    private static void fuelFurnace(GameTestHelper helper, int x, int y, int z) {
        helper.insertItem(x, y, z, IRON_ORE.copy());
        helper.insertItem(x, y, z, COAL.copy());
    }

    /** Place a wireless torch and configure its global speed via NBT. */
    private static void setupWirelessTorch(GameTestHelper helper, int x, int y, int z, Block torchBlock,
        int globalSpeedLevel) {
        helper.setBlock(x, y, z, torchBlock);
        NBTTagCompound nbt = helper.getTileNBT(x, y, z);
        nbt.setInteger("GlobalSpeedLevel", globalSpeedLevel);
        nbt.setBoolean("IsStopped", false);
        nbt.setBoolean("IsActive", true);
        helper.setTile(x, y, z, nbt);
    }

    /** Bind the furnace at (FX,FY,FZ) to the wireless torch at (TX,TY,TZ). */
    private static void bindFurnaceToTorch(GameTestHelper helper) {
        TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) worldTE(helper, TX, TY, TZ);
        int dim = helper.getWorld().provider.dimensionId;
        torch.addBoundMachine(helper.getOriginX() + FX, helper.getOriginY() + FY, helper.getOriginZ() + FZ, dim);
    }

    private static boolean furnaceHasOutput(GameTestHelper helper, int x, int y, int z) {
        TileEntity te = worldTE(helper, x, y, z);
        if (!(te instanceof IInventory)) return false;
        ItemStack output = ((IInventory) te).getStackInSlot(2);
        return output != null && output.stackSize > 0;
    }
}
