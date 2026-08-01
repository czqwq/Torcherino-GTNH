package com.czqwq.Torcherino.mixins.early.GregTech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickDeceleration;
import com.czqwq.Torcherino.util.DecelerationHelper;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.tileentities.machines.multi.MTEBrickedBlastFurnace;

/**
 * Precise deceleration for GregTech machines (progress bar only, energy flows unaffected).
 * <p>
 * GT machines must keep ticking normally every world tick so that EU consumption and
 * generation (including generators' negative-EU burn loop in {@code onPostTick}) are not
 * scaled down. Instead, this mixin marks them {@link ITileEntityTickDeceleration} and
 * holds back the natural {@code mProgresstime} gain on the "bank" fraction of ticks:
 * <ul>
 * <li>HEAD: remember the progress at tick start; if the machine sits at max-1 on a
 * bank tick, park it one step so recipe completion can only ever fire on an advance
 * tick (otherwise the finished-and-reset recipe would be reverted into a phantom state).</li>
 * <li>TAIL: the machine has already run with full side effects (EU drained/generated);
 * restore the progress to its pre-tick value. Net effect: 1 progress per {@code rate} ticks.</li>
 * </ul>
 * <p>
 * GregTech generators have no {@code mProgresstime} at all (fuel burns straight into
 * stored EU), so nothing is reverted and they behave completely normally.
 * The write path covers the machine families that own a public {@code mProgresstime}:
 * {@link MTEBasicMachine}, {@link MTEMultiBlockBase} and {@link MTEBrickedBlastFurnace}.
 */
@Pseudo
@SuppressWarnings("UnusedMixin")
@Mixin(targets = "gregtech.api.metatileentity.BaseMetaTileEntity", remap = false)
public abstract class BaseMetaTileEntityDeceleration_Mixin implements ITileEntityTickDeceleration {

    /** Progress captured at the start of the current tick. */
    private int torcherino$prevProgress;

    @Shadow(remap = false)
    public abstract int getProgress();

    @Shadow(remap = false)
    public abstract int getMaxProgress();

    @Shadow(remap = false)
    public abstract IMetaTileEntity getMetaTileEntity();

    @Inject(method = "updateEntityProfiled", at = @At("HEAD"), remap = false)
    private void torcherino$decelHead(CallbackInfo ci) {
        this.torcherino$prevProgress = this.getProgress();
        if (!DecelerationHelper.shouldHoldBackProgress((net.minecraft.tileentity.TileEntity) (Object) this)) return;
        int maxProgress = this.getMaxProgress();
        if (maxProgress >= 2 && this.torcherino$prevProgress >= maxProgress - 1) {
            // About to complete a recipe on a held-back tick — park one step so the
            // completion branch (output + reset) only fires on an advance tick.
            torcherino$setProgressRaw(this.torcherino$prevProgress - 1);
        }
    }

    @Inject(method = "updateEntityProfiled", at = @At("TAIL"), remap = false)
    private void torcherino$decelTail(CallbackInfo ci) {
        if (!DecelerationHelper.shouldHoldBackProgress((net.minecraft.tileentity.TileEntity) (Object) this)) return;
        // The machine already ran this tick with full energy side effects;
        // only the progress bar is held back.
        torcherino$setProgressRaw(this.torcherino$prevProgress);
    }

    private void torcherino$setProgressRaw(int value) {
        IMetaTileEntity metaTileEntity = this.getMetaTileEntity();
        if (metaTileEntity instanceof MTEBasicMachine basicMachine) {
            basicMachine.mProgresstime = value;
        } else if (metaTileEntity instanceof MTEMultiBlockBase multiBlockBase) {
            multiBlockBase.mProgresstime = value;
        } else if (metaTileEntity instanceof MTEBrickedBlastFurnace brickedBlastFurnace) {
            brickedBlastFurnace.mProgresstime = value;
        }
    }
}
