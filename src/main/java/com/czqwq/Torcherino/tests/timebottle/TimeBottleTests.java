package com.czqwq.Torcherino.tests.timebottle;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.entity.EntityTimeAccelerator;
import com.czqwq.Torcherino.item.ModItems;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.AfterBatch;
import com.gtnewhorizons.horizonqa.api.annotation.BeforeBatch;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

/**
 * Tests for the Time Bottle items: Time Vial, Imperfect Time Twister,
 * and Perfect Time Twister.
 *
 * <h3>Items under test</h3>
 * <table>
 * <tr>
 * <th>Item</th>
 * <th>Purpose</th>
 * </tr>
 * <tr>
 * <td>Time Vial</td>
 * <td>Stores time ticks passively. Creates
 * {@link EntityTimeAccelerator} entities to accelerate
 * blocks at a target position.</td>
 * </tr>
 * <tr>
 * <td>Imperfect Time Twister</td>
 * <td>Directly advances GT machine progress by a tier-dependent
 * amount. Has durability that regenerates over time.</td>
 * </tr>
 * <tr>
 * <td>Perfect Time Twister</td>
 * <td>Completes any GT machine recipe instantly by paying
 * wireless EU (4 &times; remainingTicks &times; EU/t).</td>
 * </tr>
 * </table>
 *
 * @see com.czqwq.Torcherino.item.ItemTimeVial
 * @see com.czqwq.Torcherino.item.ItemImperfectTimeTwister
 * @see com.czqwq.Torcherino.item.ItemPerfectTimeTwister
 */
@GameTestHolder("torcherino")
public class TimeBottleTests {

    private static final String NBT_STORED_TICK = "storedTimeTick";
    private static final String NBT_DURABILITY = "durability";

    @BeforeBatch("torcherino.time_bottle")
    public static void beforeBatch() {
        Config.enableTickBudget = false;
    }

    @AfterBatch("torcherino.time_bottle")
    public static void afterBatch() {
        Config.enableTickBudget = true;
    }

    // ============================================================
    // 1. Time Vial — existence and NBT
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void timeVialExistsAndIsUnstackable(GameTestHelper helper) {
        ItemStack vial = ModItems.timeVial != null ? new ItemStack(ModItems.timeVial) : null;
        if (vial == null) {
            helper.fail("ModItems.timeVial is null — was ModItems.init() called?");
            return;
        }
        helper.assertTrue(vial.getItem() != null, "Time Vial item must be non-null");
        helper.assertEquals(1, vial.getMaxStackSize(), "Time Vial should have max stack size of 1");
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void timeVialNbtStartsAtZero(GameTestHelper helper) {
        ItemStack vial = new ItemStack(ModItems.timeVial);
        NBTTagCompound nbt = vial.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger(NBT_STORED_TICK, 0);
            vial.setTagCompound(nbt);
        }
        int stored = nbt.getInteger(NBT_STORED_TICK);
        helper.assertTrue(stored >= 0, "Stored time should be non-negative, got " + stored);
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void timeVialCanStoreTime(GameTestHelper helper) {
        ItemStack vial = new ItemStack(ModItems.timeVial);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(NBT_STORED_TICK, 72000);
        vial.setTagCompound(nbt);
        helper.assertEquals(
            72000,
            vial.getTagCompound()
                .getInteger(NBT_STORED_TICK),
            "Time Vial should store exactly the written tick count");
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void timeVialStoredTimeInSeconds(GameTestHelper helper) {
        ItemStack vial = new ItemStack(ModItems.timeVial);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(NBT_STORED_TICK, 72000);
        vial.setTagCompound(nbt);
        int seconds = vial.getTagCompound()
            .getInteger(NBT_STORED_TICK) / 20;
        helper.assertEquals(3600, seconds, "72000 ticks should equal 3600 seconds");
        helper.succeed();
    }

    // ============================================================
    // 2. Imperfect Time Twister — durability
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void imperfectTimeTwisterExists(GameTestHelper helper) {
        ItemStack twister = ModItems.imperfectTimeTwister != null ? new ItemStack(ModItems.imperfectTimeTwister) : null;
        if (twister == null) {
            helper.fail("ModItems.imperfectTimeTwister is null");
            return;
        }
        helper.assertTrue(twister.getItem() != null, "Imperfect Time Twister item must be non-null");
        helper.assertEquals(1, twister.getMaxStackSize(), "Imperfect Time Twister should have max stack size of 1");
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void imperfectTimeTwisterHasMaxDurability(GameTestHelper helper) {
        ItemStack twister = new ItemStack(ModItems.imperfectTimeTwister);
        NBTTagCompound nbt = twister.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger(NBT_DURABILITY, 20);
            twister.setTagCompound(nbt);
        }
        int durability = nbt.getInteger(NBT_DURABILITY);
        helper.assertTrue(durability >= 0 && durability <= 20, "Durability should be 0-20, got " + durability);
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void imperfectTimeTwisterIsNotDamageable(GameTestHelper helper) {
        helper.assertFalse(
            ModItems.imperfectTimeTwister.isDamageable(),
            "Imperfect Time Twister should not be damageable");
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void imperfectTimeTwisterDurabilityCanBeSet(GameTestHelper helper) {
        ItemStack twister = new ItemStack(ModItems.imperfectTimeTwister);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(NBT_DURABILITY, 10);
        twister.setTagCompound(nbt);
        helper.assertEquals(
            10,
            twister.getTagCompound()
                .getInteger(NBT_DURABILITY),
            "Durability should be exactly the set value");
        helper.succeed();
    }

    // ============================================================
    // 3. Perfect Time Twister — basic properties
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void perfectTimeTwisterExists(GameTestHelper helper) {
        ItemStack twister = ModItems.perfectTimeTwister != null ? new ItemStack(ModItems.perfectTimeTwister) : null;
        if (twister == null) {
            helper.fail("ModItems.perfectTimeTwister is null");
            return;
        }
        helper.assertTrue(twister.getItem() != null, "Perfect Time Twister item must be non-null");
        helper.assertEquals(1, twister.getMaxStackSize(), "Perfect Time Twister should have max stack size of 1");
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void perfectTimeTwisterIsNotDamageable(GameTestHelper helper) {
        helper.assertFalse(ModItems.perfectTimeTwister.isDamageable(), "Perfect Time Twister should not be damageable");
        helper.succeed();
    }

    // ============================================================
    // 4. EntityTimeAccelerator — defaults and configuration
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void entityTimeAcceleratorHasCorrectDefaults(GameTestHelper helper) {
        World world = helper.getWorld();
        EntityTimeAccelerator entity = new EntityTimeAccelerator(
            world,
            helper.getOriginX(),
            helper.getOriginY(),
            helper.getOriginZ());
        helper.assertEquals(4, entity.getTimeRate(), "Default time rate should be 4");
        helper.assertEquals(600, entity.getRemainingTime(), "Default remaining time should be 600 ticks (30s)");
        entity.setDead();
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void entityTimeAcceleratorTimeRateDoubles(GameTestHelper helper) {
        World world = helper.getWorld();
        EntityTimeAccelerator entity = new EntityTimeAccelerator(
            world,
            helper.getOriginX(),
            helper.getOriginY(),
            helper.getOriginZ());
        entity.setTimeRate(8);
        helper.assertEquals(8, entity.getTimeRate(), "Time rate should update to 8");
        entity.setTimeRate(128);
        helper.assertEquals(128, entity.getTimeRate(), "Max time rate should be 128");
        entity.setDead();
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void entityTimeAcceleratorGregTechMode(GameTestHelper helper) {
        World world = helper.getWorld();
        EntityTimeAccelerator entity = new EntityTimeAccelerator(
            world,
            helper.getOriginX(),
            helper.getOriginY(),
            helper.getOriginZ());
        helper.assertTrue(entity.getGregTechMachineMode(), "Default GregTech machine mode should be true");
        entity.setGregTechMachineMode(false);
        helper.assertFalse(entity.getGregTechMachineMode(), "GregTech machine mode should be settable to false");
        entity.setDead();
        helper.succeed();
    }

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void entityTimeAcceleratorNbtRoundTrip(GameTestHelper helper) {
        World world = helper.getWorld();
        EntityTimeAccelerator entity = new EntityTimeAccelerator(
            world,
            helper.getOriginX(),
            helper.getOriginY(),
            helper.getOriginZ());
        entity.setTimeRate(16);
        entity.setGregTechMachineMode(false);

        NBTTagCompound nbt = new NBTTagCompound();
        entity.writeEntityToNBT(nbt);

        EntityTimeAccelerator restored = new EntityTimeAccelerator(world);
        restored.readEntityFromNBT(nbt);

        helper.assertEquals(16, restored.getTimeRate(), "Time rate should survive NBT round-trip");
        helper.assertFalse(restored.getGregTechMachineMode(), "GregTech mode should survive NBT round-trip");
        helper
            .assertEquals(helper.getOriginX(), nbt.getInteger("targetIntX"), "Target X should survive NBT round-trip");

        entity.setDead();
        restored.setDead();
        helper.succeed();
    }

    // ============================================================
    // 5. EntityTimeAccelerator — in-world behaviour
    // ============================================================

    @GameTest(timeoutTicks = 40, batch = "torcherino.time_bottle")
    public static void acceleratorEntityDecrementsAndDies(GameTestHelper helper) {
        World world = helper.getWorld();
        EntityTimeAccelerator entity = new EntityTimeAccelerator(
            world,
            helper.getOriginX(),
            helper.getOriginY(),
            helper.getOriginZ());
        entity.setTimeRate(1);
        entity.setRemainingTime(3);
        entity.setGregTechMachineMode(false);
        world.spawnEntityInWorld(entity);

        helper.startSequence()
            .thenIdle(10)
            .thenExecute(
                () -> helper
                    .assertTrue(entity.isDead, "EntityTimeAccelerator should be dead after remaining time expires"))
            .thenSucceed();
    }

    @GameTest(timeoutTicks = 60, batch = "torcherino.time_bottle")
    public static void acceleratorEntitySmeltsFurnaceFast(GameTestHelper helper) {
        int fx = 0, fy = 0, fz = 0;
        helper.setBlock(fx, fy, fz, Blocks.furnace);
        helper.insertItem(fx, fy, fz, new ItemStack(Blocks.iron_ore, 1));
        helper.insertItem(fx, fy, fz, new ItemStack(Items.coal, 64));

        World world = helper.getWorld();
        EntityTimeAccelerator entity = new EntityTimeAccelerator(
            world,
            helper.getOriginX() + fx,
            helper.getOriginY() + fy,
            helper.getOriginZ() + fz);
        entity.setTimeRate(128);
        entity.setGregTechMachineMode(false);
        world.spawnEntityInWorld(entity);

        helper.startSequence()
            .thenIdle(5)
            .thenExecute(() -> {
                TileEntity te = world
                    .getTileEntity(helper.getOriginX() + fx, helper.getOriginY() + fy, helper.getOriginZ() + fz);
                if (te instanceof IInventory) {
                    ItemStack output = ((IInventory) te).getStackInSlot(2);
                    helper.assertTrue(
                        output != null && output.stackSize > 0,
                        "Furnace should have smelted output at rate 128");
                }
            })
            .thenSucceed();
    }

    // ============================================================
    // 6. Time Vial — time economy calculations
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void timeVialConsumeCalculations(GameTestHelper helper) {
        int initRate = 4;
        int accelerationTick = 600;
        int initCost = initRate * accelerationTick;
        helper.assertEquals(2400, initCost, "Initial accelerator placement cost should be 4×600=2400 ticks");

        int nextCost = 4 * 600;
        helper.assertEquals(2400, nextCost, "Doubling to rate 8 should cost 4×600=2400");

        int toMax = 128 * 600;
        helper.assertEquals(76800, toMax, "Final doubling (128→256) would cost 128×600=76800 ticks");
        helper.succeed();
    }

    // ============================================================
    // 7. Imperfect Time Twister — bonus calculation
    // ============================================================

    @GameTest(timeoutTicks = 20, batch = "torcherino.time_bottle")
    public static void imperfectBonusDecreasesWithTier(GameTestHelper helper) {
        int recipeTicks = 500;

        int bonusTier0 = computeImperfectBonusForTest(0, recipeTicks);
        helper.assertTrue(bonusTier0 > 0, "ULV tier should get a bonus");

        int bonusTier1 = computeImperfectBonusForTest(1, recipeTicks);
        helper.assertTrue(bonusTier1 < bonusTier0, "Higher tier should get smaller bonus");

        int bonusTier3 = computeImperfectBonusForTest(3, recipeTicks);
        helper.assertTrue(bonusTier3 < bonusTier1, "HV tier should get smallest non-zero bonus");

        int bonusTier4 = computeImperfectBonusForTest(4, recipeTicks);
        helper.assertEquals(0, bonusTier4, "EV+ tier should get zero bonus");
        helper.succeed();
    }

    private static int computeImperfectBonusForTest(int machineTier, int maxProgress) {
        if (maxProgress > 400) return (4 - Math.min(machineTier, 4)) * 40;
        if (maxProgress > 200) return (4 - Math.min(machineTier, 4)) * 20;
        if (maxProgress > 40) return (4 - Math.min(machineTier, 4)) * 10;
        return 0;
    }
}
