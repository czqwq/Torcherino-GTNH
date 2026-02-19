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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ggfab.mte.MTEAdvAssLine;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
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

                // Blacklist: advanced assembly line and research station
                if (metaTileEntity instanceof MTEAdvAssLine || metaTileEntity instanceof MTEResearchStation) {
                    if (player != null) {
                        player.addChatMessage(
                            new ChatComponentText(
                                StatCollector.translateToLocal("item.perfectTimeTwister.cannotAccelerate")));
                    }
                    return true;
                }

                if (maxProgress >= 2) {
                    int remainingTicks = maxProgress - currentProgress;
                    if (remainingTicks > 0) {
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
