package com.czqwq.Torcherino.mixins.late.EnderIO;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;

import crazypants.enderio.machine.AbstractPoweredMachineEntity;
import crazypants.enderio.power.ICapacitor;

/**
 * Scales the energy a machine may receive per tick by its current acceleration rate.
 * <p>
 * <b>NH-Utilities coexistence:</b> NH-Utilities redirects the exact same
 * {@code ICapacitor.getMaxEnergyReceived()} call with an equivalent handler. Mixin lets only one
 * {@code @Redirect} own an instruction ("first applied wins", the loser is skipped with a warning
 * and, because the default {@code require} is 0, without failing the build-up of the target class),
 * so either handler may end up in the transformed class. Both behave identically, and both call
 * {@code getTickAcceleratedRate()} virtually on the machine, which resolves to
 * {@link com.czqwq.Torcherino.mixins.late.EnderIO.AccelerateTileEntity_Mixin} because that mixin
 * deliberately outranks the NH-Utilities one. No deduplication is therefore required here.
 */
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
