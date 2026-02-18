package com.czqwq.Torcherino.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Torcherino;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.tileentities.machines.multi.MTEBrickedBlastFurnace;

public class ItemImperfectTimeTwister extends Item {

    protected static final int MAX_DURABILITY = 20;
    protected static final String NBT_DURABILITY = "durability";
    protected static final float ACCELERATION_RATE = 0.5F;

    public ItemImperfectTimeTwister() {
        this.setMaxStackSize(1);
        this.setUnlocalizedName("imperfectTimeTwister");
        this.setTextureName("torcherino:TimeVial");
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {

        if (world.isRemote) return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);

        // Check if item has enough durability
        if (!hasDurability(stack)) {
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.imperfectTimeTwister.noDurability")));
            }
            return true;
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);

        // Only works on GregTech machines
        if (Torcherino.hasGregTech && tileEntity instanceof BaseMetaTileEntity baseMetaTileEntity) {
            if (baseMetaTileEntity.isActive()) {
                int currentProgress = baseMetaTileEntity.getProgress();
                int maxProgress = baseMetaTileEntity.getMaxProgress();
                IMetaTileEntity metaTileEntity = baseMetaTileEntity.getMetaTileEntity();

                if (maxProgress >= 2) {
                    // Calculate 50% acceleration (rounded down)
                    int accelerationAmount = (int) ((maxProgress - currentProgress) * ACCELERATION_RATE);

                    if (accelerationAmount > 0) {
                        int newProgress = Math.min(maxProgress, currentProgress + accelerationAmount);

                        // Apply acceleration based on machine type
                        boolean applied = false;
                        if (metaTileEntity instanceof MTEBasicMachine basicMachine) {
                            basicMachine.mProgresstime = newProgress;
                            applied = true;
                        } else if (metaTileEntity instanceof MTEMultiBlockBase multiBlockBase) {
                            multiBlockBase.mProgresstime = newProgress;
                            applied = true;
                        } else if (metaTileEntity instanceof MTEBrickedBlastFurnace brickedBlastFurnace) {
                            brickedBlastFurnace.mProgresstime = newProgress;
                            applied = true;
                        }

                        if (applied) {
                            // Consume 1 durability
                            consumeDurability(stack, 1);

                            // Play sound effect
                            world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "note.pling", 0.5F, 1.0F);

                            if (player != null) {
                                player.addChatMessage(
                                    new ChatComponentText(
                                        StatCollector.translateToLocal("item.imperfectTimeTwister.success")));
                            }
                            return true;
                        }
                    }
                }
            }

            // Machine not active or no progress
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.imperfectTimeTwister.notWorking")));
            }
        } else {
            // Not a GregTech machine
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.imperfectTimeTwister.notGTMachine")));
            }
        }

        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        return true;
    }

    /**
     * Check if the item has durability remaining
     */
    protected boolean hasDurability(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return false;
        }
        return nbt.getInteger(NBT_DURABILITY) > 0;
    }

    /**
     * Consume durability from the item
     */
    protected void consumeDurability(ItemStack stack, int amount) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger(NBT_DURABILITY, MAX_DURABILITY);
        }

        int currentDurability = nbt.getInteger(NBT_DURABILITY);
        nbt.setInteger(NBT_DURABILITY, Math.max(0, currentDurability - amount));
        stack.setTagCompound(nbt);
    }

    /**
     * Regenerate durability over time (1 per second)
     */
    @Override
    public void onUpdate(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean isHeld) {
        if (world.isRemote) return;

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger(NBT_DURABILITY, MAX_DURABILITY);
            stack.setTagCompound(nbt);
            return;
        }

        // Regenerate 1 durability per second (20 ticks)
        if (world.getTotalWorldTime() % 20 == 0) {
            int currentDurability = nbt.getInteger(NBT_DURABILITY);
            if (currentDurability < MAX_DURABILITY) {
                nbt.setInteger(NBT_DURABILITY, currentDurability + 1);
                stack.setTagCompound(nbt);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extraInformation) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger(NBT_DURABILITY, MAX_DURABILITY);
        }

        int durability = nbt.getInteger(NBT_DURABILITY);
        list.add(
            String.format(
                StatCollector.translateToLocal("item.imperfectTimeTwister.durability"),
                durability,
                MAX_DURABILITY));
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger(NBT_DURABILITY, MAX_DURABILITY);
        }

        int durability = nbt.getInteger(NBT_DURABILITY);
        return StatCollector.translateToLocal(
            this.getUnlocalizedNameInefficiently(stack) + ".name") + " §7[" + durability + "/" + MAX_DURABILITY + "]§r";
    }

    /**
     * Prevent the item from breaking when durability reaches 0
     */
    @Override
    public boolean isDamageable() {
        return false;
    }
}
