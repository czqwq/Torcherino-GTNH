package com.czqwq.Torcherino.item;

import static com.czqwq.Torcherino.entity.EntityTimeAccelerator.ACCELERATION_TICK;

import java.util.List;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.czqwq.Torcherino.entity.EntityTimeAccelerator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemTimeVial extends Item {

    protected static final int TIME_INIT_RATE = 4;
    protected static final float[] SOUND_ARRAY_F = new float[] { 0.749154F, 0.793701F, 0.890899F, 1.059463F, 0.943874F,
        0.890899F, 0.690899F };
    protected static final int MAX_ACCELERATION = 128;
    protected int storedTimeTick = 0;

    protected static final double tHalfSize = 0.01D; // 实体一半的大小
    protected static final String NBT_STORED_TICK = "storedTimeTick";

    public ItemTimeVial() {
        this.setMaxStackSize(1);
        this.setUnlocalizedName("timeVial");
        this.setTextureName("torcherino:TimeVial");
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
        int side, float hitX, float hitY, float hitZ) {

        if (world.isRemote) return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);

        // 实体中心坐标
        double targetPosX = x + 0.5D;
        double targetPosY = y + 0.5D;
        double targetPosZ = z + 0.5D;

        // 计算碰撞箱大小
        double minX = targetPosX - tHalfSize;
        double minY = targetPosY - tHalfSize;
        double minZ = targetPosZ - tHalfSize;
        double maxX = targetPosX + tHalfSize;
        double maxY = targetPosY + tHalfSize;
        double maxZ = targetPosZ + tHalfSize;

        // 获取碰撞箱对应实体
        Optional<EntityTimeAccelerator> box = world
            .getEntitiesWithinAABB(
                EntityTimeAccelerator.class,
                AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ))
            .stream()
            .findFirst();

        EntityTimeAccelerator eta = box.orElseGet(() -> new EntityTimeAccelerator(world, x, y, z));
        if (box.isPresent()) {
            if (player.isSneaking()) recyclingTime(stack, eta);
            else applyNextAcceleration(stack, eta);
        } else if (consumeTimeData(stack, TIME_INIT_RATE * 600)) {
            // set the GregTechMachineMode
            if (player.isSneaking()) eta.setGregTechMachineMode(false);
            world.spawnEntityInWorld(eta);
        }
        etaInteract(eta, world, targetPosX, targetPosY, targetPosZ);
        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        return true;
    }

    protected void applyNextAcceleration(ItemStack stack, EntityTimeAccelerator eta) {
        int currentRate = eta.getTimeRate();
        if (currentRate < MAX_ACCELERATION) {
            int remained = currentRate * eta.getRemainingTime();
            int nextRateTimeRequired = remained;
            if (consumeTimeData(stack, nextRateTimeRequired)) {
                eta.setTimeRate(currentRate * 2);
            }
        }
    }

    protected static void recyclingTime(ItemStack stack, EntityTimeAccelerator eta) {
        NBTTagCompound nbtTagCompound = stack.getTagCompound();
        if (nbtTagCompound != null) {
            nbtTagCompound.setInteger(
                NBT_STORED_TICK,
                nbtTagCompound.getInteger(NBT_STORED_TICK) + eta.getTimeRate() * eta.getRemainingTime());
            stack.setTagCompound(nbtTagCompound);
        }
        eta.setDead();
    }

    protected void etaInteract(EntityTimeAccelerator eta, World world, double targetPosX, double targetPosY,
        double targetPosZ) {
        int i = (int) (Math.log(eta.getTimeRate()) / Math.log(2)) - 2;
        // security considerations
        if (i < 0 || i >= SOUND_ARRAY_F.length) i = 0;
        world.playSoundEffect(
            targetPosX,
            targetPosY,
            targetPosZ,
            "note.harp",
            0.5F,
            SOUND_ARRAY_F[i]);
    }

    protected boolean consumeTimeData(ItemStack stack, int consumedTick) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            tagCompound.setInteger(NBT_STORED_TICK, 0);
            stack.setTagCompound(tagCompound);
            return false;
        }
        int timeTick = tagCompound.getInteger(NBT_STORED_TICK);
        if (timeTick >= consumedTick) {
            tagCompound.setInteger(NBT_STORED_TICK, timeTick - consumedTick);
            return true;
        }
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, net.minecraft.entity.Entity entityIn, int slot, boolean isHeld) {
        if (worldIn.isRemote) return;
        NBTTagCompound nbtTagCompound = stack.getTagCompound();
        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setInteger(NBT_STORED_TICK, storedTimeTick);
        } else if (worldIn.getTotalWorldTime() % 20 == 0) {
            int t = nbtTagCompound.getInteger(NBT_STORED_TICK);
            nbtTagCompound.setInteger(NBT_STORED_TICK, t + 20);
        }
        stack.setTagCompound(nbtTagCompound);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extraInformation) {
        getInfoFromNBT(stack, list);
    }

    @SideOnly(Side.CLIENT)
    protected void getInfoFromNBT(ItemStack stack, List<String> list) {
        NBTTagCompound nbtTagCompound = stack.getTagCompound();
        if (nbtTagCompound == null) nbtTagCompound = new NBTTagCompound();
        int storedTimeSeconds = nbtTagCompound.getInteger(NBT_STORED_TICK) / 20;
        int hours = storedTimeSeconds / 3600;
        int minutes = (storedTimeSeconds % 3600) / 60;
        int seconds = storedTimeSeconds % 60;
        list.add(
            StatCollector.translateToLocal("item.timeVial.time") + ": " + hours + "h " + minutes + "m " + seconds
                + "s");
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        NBTTagCompound nbtTagCompound = stack.getTagCompound();
        if (nbtTagCompound == null) nbtTagCompound = new NBTTagCompound();
        int storedTimeSeconds = nbtTagCompound.getInteger(NBT_STORED_TICK) / 20;
        int hours = storedTimeSeconds / 3600;
        int minutes = (storedTimeSeconds % 3600) / 60;
        int seconds = storedTimeSeconds % 60;
        return StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name") + " (" + hours
            + "h " + minutes + "m " + seconds + "s)";
    }
}
