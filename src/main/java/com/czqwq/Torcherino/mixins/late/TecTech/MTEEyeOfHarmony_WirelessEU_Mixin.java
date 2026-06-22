package com.czqwq.Torcherino.mixins.late.TecTech;

import java.math.BigInteger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.czqwq.Torcherino.api.interfaces.mixinHelper.IWirelessEUMachineInfo;

@Pseudo
@Mixin(targets = "tectech.thing.metaTileEntity.multi.MTEEyeOfHarmony", remap = false)
@SuppressWarnings({ "UnusedMixin", "AddedMixinMembersNamePattern" })
public abstract class MTEEyeOfHarmony_WirelessEU_Mixin implements IWirelessEUMachineInfo {

    @Shadow(remap = false)
    private BigInteger usedEU;

    @Override
    public BigInteger torcherino$getConsumedWirelessEU() {
        if (usedEU == null) return BigInteger.ZERO;
        return usedEU.abs();
    }
}
