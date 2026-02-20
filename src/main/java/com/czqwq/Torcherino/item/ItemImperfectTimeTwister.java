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
import ggfab.mte.MTEAdvAssLine;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicGenerator;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.tileentities.machines.multi.MTEBrickedBlastFurnace;
import tectech.thing.metaTileEntity.multi.MTEResearchStation;

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

                // Check for specialized machines that cannot be accelerated
                if (metaTileEntity instanceof MTEAdvAssLine || metaTileEntity instanceof MTEResearchStation) {
                    if (player != null) {
                        player.addChatMessage(
                            new ChatComponentText(
                                StatCollector.translateToLocal("item.imperfectTimeTwister.cannotAccelerate")));
                    }
                    return true;
                }

                // Block GT generators (single-block and multi-block)
                if (isGTGenerator(metaTileEntity)) {
                    if (player != null) {
                        player.addChatMessage(
                            new ChatComponentText(
                                StatCollector.translateToLocal("item.imperfectTimeTwister.cannotAccelerateGenerator")));
                    }
                    return true;
                }

                if (maxProgress >= 2) {
                    // Compute bonus ticks from tier/recipe-length restrictions.
                    // These are added directly to accelerationAmount (same mechanism as
                    // PerfectTimeTwister: advance mProgresstime, not extend mMaxProgresstime).
                    int bonusTicks = 0;
                    if (metaTileEntity instanceof MTEBrickedBlastFurnace) {
                        // Primitive blast furnace: fixed +100 ticks bonus
                        bonusTicks = 100;
                    } else if (metaTileEntity instanceof MTEBasicMachine bm) {
                        if (bm.mTier >= 4) {
                            if (player != null) {
                                player.addChatMessage(
                                    new ChatComponentText(
                                        StatCollector.translateToLocal("item.imperfectTimeTwister.tierTooHigh")));
                            }
                            return true;
                        }
                        if (bm.mEUt > 0) {
                            bonusTicks = computeImperfectBonus(bm.mTier, maxProgress);
                        }
                    } else if (metaTileEntity instanceof MTEMultiBlockBase mb) {
                        int mbTier = (int) mb.getInputVoltageTier();
                        if (mbTier >= 4) {
                            if (player != null) {
                                player.addChatMessage(
                                    new ChatComponentText(
                                        StatCollector.translateToLocal("item.imperfectTimeTwister.tierTooHigh")));
                            }
                            return true;
                        }
                        if (mb.mEUt < 0) {
                            bonusTicks = computeImperfectBonus(mbTier, maxProgress);
                        }
                    }

                    // Calculate 50% acceleration of REMAINING time (rounded down) + bonus ticks
                    // Formula: (total work time - current time) * 50% + bonusTicks
                    int accelerationAmount = (int) ((maxProgress - currentProgress) * ACCELERATION_RATE) + bonusTicks;

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

                            if (player != null) {
                                player.addChatMessage(
                                    new ChatComponentText(
                                        String.format(
                                            StatCollector.translateToLocal("item.imperfectTimeTwister.success"),
                                            accelerationAmount)));
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

    /**
     * Computes the imperfect time twister bonus ticks for a machine based on its tier and current recipe duration.
     * Lower-tier machines receive a larger bonus (more acceleration). The bonus is added directly to the
     * accelerationAmount so that mProgresstime is advanced further (same mechanism as PerfectTimeTwister).
     *
     * @param machineTier the voltage tier of the machine (0=ULV, 1=LV, 2=MV, 3=HV)
     * @param maxProgress the current total recipe duration in ticks
     * @return the number of bonus ticks to add to the acceleration amount
     */
    private static int computeImperfectBonus(int machineTier, int maxProgress) {
        if (maxProgress > 400) return (4 - machineTier) * 40;
        if (maxProgress > 200) return (4 - machineTier) * 20;
        if (maxProgress > 40) return (4 - machineTier) * 10;
        return 0;
    }

    /**
     * Returns true if the given IMetaTileEntity is a GT generator (single-block or multi-block).
     * Single-block generators extend {@link MTEBasicGenerator}.
     * Multi-block generators set mEUt > 0 (or lEUt > 0 for extended-power machines) while running.
     */
    private static boolean isGTGenerator(IMetaTileEntity metaTileEntity) {
        if (metaTileEntity instanceof MTEBasicGenerator) return true;
        if (metaTileEntity instanceof MTEExtendedPowerMultiBlockBase<?>extMulti) return extMulti.lEUt > 0;
        if (metaTileEntity instanceof MTEMultiBlockBase multiBlock) return multiBlock.mEUt > 0;
        return false;
    }
}
