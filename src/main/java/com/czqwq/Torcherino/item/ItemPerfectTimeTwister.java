package com.czqwq.Torcherino.item;

import java.math.BigInteger;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.api.interfaces.mixinHelper.IAccelerationState;
import com.czqwq.Torcherino.api.interfaces.mixinHelper.IAdvAssLineInfo;
import com.czqwq.Torcherino.api.interfaces.mixinHelper.IWirelessEUMachineInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ggfab.mte.MTEAdvAssLine;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.misc.WirelessNetworkManager;
import gregtech.common.tileentities.machines.multi.MTEBrickedBlastFurnace;
import tectech.thing.metaTileEntity.multi.MTEResearchStation;

public class ItemPerfectTimeTwister extends Item {

    public ItemPerfectTimeTwister() {
        this.setMaxStackSize(1);
        this.setUnlocalizedName("perfectTimeTwister");
        this.setTextureName("torcherino:EternityVial");
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {

        if (world.isRemote) return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);

        TileEntity tileEntity = world.getTileEntity(x, y, z);

        // Only works on GregTech machines
        if (Torcherino.hasGregTech && tileEntity instanceof BaseMetaTileEntity baseMetaTileEntity) {
            if (baseMetaTileEntity.isActive()) {
                int currentProgress = baseMetaTileEntity.getProgress();
                int maxProgress = baseMetaTileEntity.getMaxProgress();
                IMetaTileEntity metaTileEntity = baseMetaTileEntity.getMetaTileEntity();

                // Blacklist: research station only (advanced assembly line now supported)
                if (metaTileEntity instanceof MTEResearchStation) {
                    if (player != null) {
                        player.addChatMessage(
                            new ChatComponentText(
                                StatCollector.translateToLocal("item.perfectTimeTwister.cannotAccelerate")));
                    }
                    return true;
                }

                // Handle Advanced Assembly Line (slice-based structure)
                if (metaTileEntity instanceof MTEAdvAssLine) {
                    return handleAdvAssLine(player, baseMetaTileEntity, metaTileEntity, currentProgress, maxProgress);
                }

                if (maxProgress >= 2) {
                    int remainingTicks = maxProgress - currentProgress;
                    if (remainingTicks > 0) {
                        // Check if this is a wireless-EU machine (mEUt == 0, EU paid upfront)
                        if (metaTileEntity instanceof IWirelessEUMachineInfo wirelessMachine) {
                            return handleWirelessEUMachine(
                                player,
                                baseMetaTileEntity,
                                metaTileEntity,
                                wirelessMachine,
                                remainingTicks,
                                maxProgress);
                        }

                        long mEUt = 0;
                        boolean canAccelerate = false;

                        if (metaTileEntity instanceof MTEBasicMachine basicMachine) {
                            mEUt = Math.abs(basicMachine.mEUt);
                            canAccelerate = true;
                        } else if (metaTileEntity instanceof MTEMultiBlockBase multiBlockBase) {
                            mEUt = Math.abs(multiBlockBase.mEUt);
                            canAccelerate = true;
                        }

                        if (canAccelerate) {
                            // euCost = 4 * remainingTicks * InputEUt
                            BigInteger euCost = BigInteger.valueOf(4L)
                                .multiply(BigInteger.valueOf(remainingTicks))
                                .multiply(BigInteger.valueOf(mEUt));

                            // Ensure user exists in wireless network
                            WirelessNetworkManager.strongCheckOrAddUser(player.getUniqueID());

                            if (WirelessNetworkManager.addEUToGlobalEnergyMap(player.getUniqueID(), euCost.negate())) {
                                // Success: accelerate to 100% completion
                                if (metaTileEntity instanceof MTEBasicMachine basicMachine) {
                                    basicMachine.mProgresstime = maxProgress;
                                } else if (metaTileEntity instanceof MTEMultiBlockBase multiBlockBase) {
                                    multiBlockBase.mProgresstime = maxProgress;
                                } else if (metaTileEntity instanceof MTEBrickedBlastFurnace brickedBlastFurnace) {
                                    brickedBlastFurnace.mProgresstime = maxProgress;
                                }

                                if (player != null) {
                                    player.addChatMessage(
                                        new ChatComponentText(
                                            String.format(
                                                StatCollector.translateToLocal("item.perfectTimeTwister.success"),
                                                euCost,
                                                remainingTicks)));
                                }
                            } else {
                                // Insufficient wireless EU
                                if (player != null) {
                                    player.addChatMessage(
                                        new ChatComponentText(
                                            String.format(
                                                StatCollector
                                                    .translateToLocal("item.perfectTimeTwister.insufficientEU"),
                                                euCost)));
                                }
                            }
                            return true;
                        }
                    }
                }
            }

            // Machine not active or no progress
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.perfectTimeTwister.notWorking")));
            }
        } else {
            // Not a GregTech machine
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.perfectTimeTwister.notGTMachine")));
            }
        }

        return true;
    }

    /**
     * Handles the Perfect Time Twister for wireless-EU machines (e.g. MTEEyeOfHarmony,
     * MTETranscendentPlasmaMixer). These machines pay their EU upfront and have mEUt == 0 during
     * recipe execution, so we charge proportionally to the remaining time.
     */
    private boolean handleWirelessEUMachine(EntityPlayer player, BaseMetaTileEntity baseMetaTileEntity,
        IMetaTileEntity metaTileEntity, IWirelessEUMachineInfo wirelessMachine, int remainingTicks, int maxProgress) {

        BigInteger totalConsumedEU = wirelessMachine.torcherino$getConsumedWirelessEU();
        if (totalConsumedEU.compareTo(BigInteger.ZERO) <= 0) {
            // No EU recorded (recipe just started or not running) - fall back to blacklist behavior
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.perfectTimeTwister.notWorking")));
            }
            return true;
        }

        // Cost = totalConsumedEU * remainingTicks / maxProgress (proportional to time skipped)
        BigInteger euCost = totalConsumedEU.multiply(BigInteger.valueOf(remainingTicks))
            .divide(BigInteger.valueOf(maxProgress));

        // Ensure user exists in wireless network
        WirelessNetworkManager.strongCheckOrAddUser(player.getUniqueID());

        if (WirelessNetworkManager.addEUToGlobalEnergyMap(player.getUniqueID(), euCost.negate())) {
            // Success: advance progress to completion
            if (metaTileEntity instanceof MTEMultiBlockBase multiBlockBase) {
                multiBlockBase.mProgresstime = maxProgress;
            }

            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(
                        String.format(
                            StatCollector.translateToLocal("item.perfectTimeTwister.success"),
                            euCost,
                            remainingTicks)));
            }
        } else {
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(
                        String
                            .format(StatCollector.translateToLocal("item.perfectTimeTwister.insufficientEU"), euCost)));
            }
        }
        return true;
    }

    /**
     * Handles the Perfect Time Twister for MTEAdvAssLine (Advanced Assembly Line).
     * Completes the last (currently active) working slice of the assembly line.
     * Each right-click completes exactly one slice. Cannot accelerate stuck slices.
     */
    private boolean handleAdvAssLine(EntityPlayer player, BaseMetaTileEntity baseMetaTileEntity,
        IMetaTileEntity metaTileEntity, int currentProgress, int maxProgress) {

        if (maxProgress <= 0) {
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.perfectTimeTwister.notWorking")));
            }
            return true;
        }

        IAccelerationState state = (IAccelerationState) metaTileEntity;

        // getMachineAccelerationState() returns the 'stuck' field - slice is stuck waiting for inputs
        if (state.getMachineAccelerationState()) {
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(
                        StatCollector.translateToLocal("item.perfectTimeTwister.advAssLineSliceStuck")));
            }
            return true;
        }

        IAdvAssLineInfo assLineInfo = (IAdvAssLineInfo) metaTileEntity;
        int inputLength = assLineInfo.torcherino$getCurrentInputLength();
        if (inputLength <= 0) {
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.perfectTimeTwister.notWorking")));
            }
            return true;
        }

        // Calculate the boundary for the current (last working) slice.
        // mProgresstime = (slice.id + 1) * sliceDuration - slice.progress
        // So the current slice index = floor(mProgresstime / sliceDuration)
        // and the completion boundary = (currentSliceIndex + 1) * sliceDuration
        int sliceDuration = maxProgress / inputLength;
        if (sliceDuration <= 0) {
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.perfectTimeTwister.notWorking")));
            }
            return true;
        }

        int currentSliceIndex = currentProgress / sliceDuration;
        int nextBoundary = (currentSliceIndex + 1) * sliceDuration;
        if (nextBoundary > maxProgress) nextBoundary = maxProgress;
        int remainingTicks = nextBoundary - currentProgress;

        if (remainingTicks <= 0) {
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(StatCollector.translateToLocal("item.perfectTimeTwister.notWorking")));
            }
            return true;
        }

        // EU cost: use lEUt (per-slice EU/t) from MTEExtendedPowerMultiBlockBase, which is set
        // to baseEUt in MTEAdvAssLine.onRunningTick when exactly 1 slice is working.
        long euPerTick = 0;
        if (metaTileEntity instanceof MTEExtendedPowerMultiBlockBase<?>extendedMulti) {
            euPerTick = Math.abs(extendedMulti.lEUt);
        }
        if (euPerTick == 0 && metaTileEntity instanceof MTEMultiBlockBase multiBlockBase) {
            euPerTick = Math.abs(multiBlockBase.mEUt);
        }

        BigInteger euCost = BigInteger.valueOf(euPerTick)
            .multiply(BigInteger.valueOf(remainingTicks));

        // Ensure user exists in wireless network
        WirelessNetworkManager.strongCheckOrAddUser(player.getUniqueID());

        if (!WirelessNetworkManager.addEUToGlobalEnergyMap(player.getUniqueID(), euCost.negate())) {
            if (player != null) {
                player.addChatMessage(
                    new ChatComponentText(
                        String
                            .format(StatCollector.translateToLocal("item.perfectTimeTwister.insufficientEU"), euCost)));
            }
            return true;
        }

        // Complete the slice by calling updateEntity() in a loop with energy skip.
        // The mixin's isAccelerationState flag suppresses energy drain in onRunningTick.
        state.setAccelerationState(true);
        final int finalNextBoundary = nextBoundary;
        try {
            long tMaxTime = System.nanoTime() + 50_000_000L; // 50 ms safety cap
            while (baseMetaTileEntity.getProgress() < finalNextBoundary && !state.getMachineAccelerationState()
                && baseMetaTileEntity.isActive()) {
                baseMetaTileEntity.updateEntity();
                if (System.nanoTime() > tMaxTime) break;
            }
        } catch (Exception e) {
            Torcherino.LOG.warn(
                "Error during AdvAssLine slice acceleration at ({}, {}, {}): {}",
                baseMetaTileEntity.xCoord,
                baseMetaTileEntity.yCoord,
                baseMetaTileEntity.zCoord,
                e.getMessage());
        } finally {
            state.setAccelerationState(false);
        }

        if (player != null) {
            player.addChatMessage(
                new ChatComponentText(
                    String.format(
                        StatCollector.translateToLocal("item.perfectTimeTwister.success"),
                        euCost,
                        remainingTicks)));
        }
        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extraInformation) {
        list.add(StatCollector.translateToLocal("item.perfectTimeTwister.description"));
    }

    @Override
    public boolean isDamageable() {
        return false;
    }
}
