package com.czqwq.Torcherino.mixins.early.GregTech;

import static com.czqwq.Torcherino.Config.accelerateGregTechMachineDiscount;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration;
import com.czqwq.Torcherino.api.interfaces.mixinHelper.IAccelerationState;

import ggfab.mte.MTEAdvAssLine;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.CommonBaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.tileentities.machines.basic.MTEWorldAccelerator;
import gregtech.common.tileentities.machines.multi.MTEBrickedBlastFurnace;
import tectech.thing.metaTileEntity.multi.MTEResearchStation;

/**
 * Precise acceleration for every GregTech machine, implemented on the single TileEntity that hosts
 * all of them ({@code BaseMetaTileEntity}): GT machines are never re-ticked (which would multiply
 * EU consumption), only their progress is advanced.
 * <p>
 * <b>NH-Utilities coexistence:</b> NH-Utilities ships a mixin with the same target and the same
 * method ({@code com.xir.NHUtilities.mixins.late.GregTech.BaseMetaTileEntityAcceleration_Mixin}).
 * Mixin keeps only one copy of a duplicated method - the one applied first - and skips the other
 * with a "Method overwrite conflict ... Skipping method." warning. This mixin lives in the early
 * config ({@code mixins.Torcherino.json}, registered from the jar manifest at launch), while the
 * NH-Utilities one is a late mixin (registered at {@code LoaderState.CONSTRUCTING}), so this
 * implementation deterministically owns {@code tickAcceleration} whenever both mods are installed.
 * Interfaces, in contrast, are merged unconditionally, so NH-Utilities' own accelerators keep
 * working: their {@code ITileEntityTickAcceleration} calls resolve to this method.
 * <p>
 * Because the special cases below are reached through <i>Torcherino's</i> marker interfaces, the
 * helper mixins providing them ({@code MTEAdvAssLineAcceleration_Mixin},
 * {@code ResearchStationAcceleration_Mixin}) must NOT be disabled when NH-Utilities is present -
 * doing so would push those machines onto the generic {@code mProgresstime} path below.
 */
@Pseudo
@SuppressWarnings("UnusedMixin")
@Mixin(targets = "gregtech.api.metatileentity.BaseMetaTileEntity", remap = false)
public abstract class BaseMetaTileEntityAcceleration_Mixin extends CommonBaseMetaTileEntity
    implements ITileEntityTickAcceleration {

    private static final Logger LOG = LogManager.getLogger("Torcherino");

    @Shadow(remap = false)
    public abstract int getProgress();

    @Shadow(remap = false)
    public abstract int getMaxProgress();

    @Shadow(remap = false)
    public abstract IMetaTileEntity getMetaTileEntity();

    @Shadow(remap = false)
    public abstract boolean isActive();

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean tickAcceleration(int tickAcceleratedRate) {
        if (this.isActive()) {
            // safely calling
            int currentProgress = this.getProgress();
            int maxProgress = this.getMaxProgress();
            IMetaTileEntity metaTileEntity = this.getMetaTileEntity();

            // Blacklist GT World Accelerator - it must not be accelerated by torches
            if (metaTileEntity instanceof MTEWorldAccelerator) {
                return true;
            }

            // for accelerating Research Station
            if (metaTileEntity instanceof MTEResearchStation researchStation) {
                if (researchStation instanceof ITileEntityTickAcceleration resAte) {
                    resAte.tickAcceleration(tickAcceleratedRate);
                }
                return true;
            }

            // for accelerating Adv Ass Line
            if (metaTileEntity instanceof MTEAdvAssLine advAssLine) {
                if (advAssLine instanceof IAccelerationState accelerationState) {
                    tickAcceleratedRate = (int) (tickAcceleratedRate * accelerateGregTechMachineDiscount);
                    accelerationState.setAccelerationState(true);
                    try {

                        // Referenced GTNH to control the performance in 1ms
                        long tMaxTime = System.nanoTime() + 1000000;

                        for (int i = 0; i < tickAcceleratedRate; i++) {
                            // skip if assLine stuck
                            if (accelerationState.getMachineAccelerationState()) break;
                            this.updateEntity();
                            if (System.nanoTime() > tMaxTime) {
                                break;
                            }
                        }

                    } catch (Exception e) {
                        LOG.warn(
                            "An error occurred accelerating TileEntity at ( {}, {}, {}, {})",
                            this.xCoord,
                            this.yCoord,
                            this.zCoord,
                            e.getMessage());
                    } finally {
                        accelerationState.setAccelerationState(false);
                    }
                    // Stop here: the loop above already ran the machine's own ticks, so falling
                    // through to the generic progress block below would advance this machine a
                    // second time in the same tick. MTEAdvAssLine owns mProgresstime (it resets
                    // and recomputes it per slice), so that second write - computed from the
                    // stale pre-loop 'currentProgress' - both double-applies the discounted rate
                    // and, when the loop broke early on 'stuck' or on the 1 ms guard, invents
                    // progress the line never earned.
                    return true;
                }
            }

            if (maxProgress >= 2) { // obviously
                // discount for accelerating gregtech machines
                tickAcceleratedRate = (int) (tickAcceleratedRate * accelerateGregTechMachineDiscount);
                int Torcherino$modify = Math.min(maxProgress, currentProgress + tickAcceleratedRate);

                // for accelerating basic machine
                if (metaTileEntity instanceof MTEBasicMachine basicMachine) {
                    basicMachine.mProgresstime = Torcherino$modify;
                    return true;
                }

                // for accelerating multi machine
                if (metaTileEntity instanceof MTEMultiBlockBase multiBlockBase) {
                    multiBlockBase.mProgresstime = Torcherino$modify;
                    return true;
                }

                // for accelerating primitive blast furnace
                if (metaTileEntity instanceof MTEBrickedBlastFurnace brickedBlastFurnace) {
                    brickedBlastFurnace.mProgresstime = Torcherino$modify;
                    return true;
                }

                return false; // this for accelerating gt machine by executing TE update method

            }
        }
        return true; // this for not acceleration while machine is shutdown
    }
}
