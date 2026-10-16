package com.czqwq.Torcherino.mixins.late.GregTech;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.czqwq.Torcherino.api.interfaces.mixinHelper.IAccelerationState;
import com.czqwq.Torcherino.api.interfaces.mixinHelper.IAdvAssLineInfo;

/**
 * Gives {@code MTEAdvAssLine} the acceleration state flag consumed by
 * {@code BaseMetaTileEntityAcceleration_Mixin}, and cancels its energy drain while an accelerated
 * tick is running.
 * <p>
 * <b>NH-Utilities coexistence:</b> NH-Utilities mixes the same class with the same members
 * ({@code @Unique isAccelerationState}, {@code getMachineAccelerationState()},
 * {@code setAccelerationState(boolean)} and an equivalent {@code onRunningTick} inject). This is
 * safe without any exclusion: fields are attached for every mixin before any of them is merged, so
 * neither side renames its field, and the second duplicate field is then dropped by name+descriptor
 * lookup - both mods end up reading and writing the <i>same</i> flag. The duplicated methods have
 * identical bodies, and both {@code @Inject} handlers stay in place, so the state stays consistent
 * no matter which copy of the methods Mixin keeps.
 */
@Pseudo
@Mixin(targets = "ggfab.mte.MTEAdvAssLine", remap = false)
@SuppressWarnings({ "UnusedMixin", "AddedMixinMembersNamePattern" })
public abstract class MTEAdvAssLineAcceleration_Mixin implements IAccelerationState, IAdvAssLineInfo {

    @Shadow(remap = false)
    private boolean stuck;

    @Shadow(remap = false)
    private int currentInputLength;

    @Unique
    private boolean isAccelerationState;

    @Override
    public boolean getMachineAccelerationState() {
        return stuck;
    }

    @Override
    public void setAccelerationState(boolean state) {
        isAccelerationState = state;
    }

    @Override
    public int torcherino$getCurrentInputLength() {
        return currentInputLength;
    }

    @Inject(
        method = "onRunningTick",
        at = @At(value = "FIELD", target = "Lggfab/mte/MTEAdvAssLine;baseEUt:J", ordinal = 0),
        cancellable = true)
    private void Torcherino$modifyDrainEnergy(ItemStack aStack, CallbackInfoReturnable<Boolean> cir) {
        if (isAccelerationState) cir.setReturnValue(true);
    }

}
