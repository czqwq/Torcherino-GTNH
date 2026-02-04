package com.czqwq.Torcherino.mixins.late.EnderIO;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;

import crazypants.enderio.machine.AbstractPoweredMachineEntity;
import crazypants.enderio.power.ICapacitor;

@SuppressWarnings("UnusedMixin")
@Mixin(value = AbstractPoweredMachineEntity.class, remap = false)
public class AccelerateEnergyReceive_Mixin {

    @Redirect(
        method = "getMaxEnergyRecieved",
        at = @At(value = "INVOKE", target = "Lcrazypants/enderio/power/ICapacitor;getMaxEnergyReceived()I"))
    private int Torcherino$modifyMaxEnergyReceivedValue(ICapacitor instance) {
        if (this instanceof ITileEntityTickAcceleration tileEntityITEA) {
            int tickAcceleratedRate = tileEntityITEA.getTickAcceleratedRate();
            return instance.getMaxEnergyReceived() * tickAcceleratedRate;
        }
        return instance.getMaxEnergyReceived();
    }
}
