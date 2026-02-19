package com.czqwq.Torcherino.mixins.late.GregTech;

import java.math.BigInteger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.czqwq.Torcherino.api.interfaces.mixinHelper.IWirelessEUMachineInfo;

import gregtech.common.tileentities.machines.multi.MTETranscendentPlasmaMixer;

@Pseudo
@Mixin(value = MTETranscendentPlasmaMixer.class, remap = false)
@SuppressWarnings({ "UnusedMixin", "AddedMixinMembersNamePattern" })
public abstract class MTETranscendentPlasmaMixer_WirelessEU_Mixin implements IWirelessEUMachineInfo {

    @Shadow
    BigInteger finalConsumption;

    @Override
    public BigInteger torcherino$getConsumedWirelessEU() {
        if (finalConsumption == null) return BigInteger.ZERO;
        return finalConsumption.abs();
    }
}
