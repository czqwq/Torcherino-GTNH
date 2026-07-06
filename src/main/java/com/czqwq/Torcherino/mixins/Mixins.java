package com.czqwq.Torcherino.mixins;

import static com.czqwq.Torcherino.Config.enableAccelerateGregTechMachine;
import static com.czqwq.Torcherino.Config.enableCropsNHAcceleration;
import static com.czqwq.Torcherino.Config.enableFlashTorcherino;
import static com.czqwq.Torcherino.Config.enableForestryAcceleration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public enum Mixins {

    GregTech_Accelerations(MixinClass.newMixinClass("Modify_ResearchStation_Acceleration")
        .setClass("ResearchStationAcceleration_Mixin")
        .setPackagePath(PackagePath.TecTech)
        .setPhase(Phase.LATE)
        .addTargetMod(TargetMod.TecTech)
        .addTargetMod(TargetMod.GregTech)
        .addCondition(enableAccelerateGregTechMachine),
        MixinClass.newMixinClass("Modify_AdvAssLine_Acceleration")
            .setClass("MTEAdvAssLineAcceleration_Mixin")
            .setPackagePath(PackagePath.GregTech)
            .setPhase(Phase.LATE)
            .addTargetMod(TargetMod.GregTech)
            .addTargetMod(TargetMod.GigaGramFab)
            .addCondition(enableAccelerateGregTechMachine),
        MixinClass.newMixinClass("Modify_EOH_WirelessEU")
            .setClass("MTEEyeOfHarmony_WirelessEU_Mixin")
            .setPackagePath(PackagePath.TecTech)
            .setPhase(Phase.LATE)
            .addTargetMod(TargetMod.TecTech)
            .addTargetMod(TargetMod.GregTech)
            .addCondition(enableAccelerateGregTechMachine),
        MixinClass.newMixinClass("Modify_TPM_WirelessEU")
            .setClass("MTETranscendentPlasmaMixer_WirelessEU_Mixin")
            .setPackagePath(PackagePath.GregTech)
            .setPhase(Phase.LATE)
            .addTargetMod(TargetMod.GregTech)
            .addCondition(enableAccelerateGregTechMachine)),

    EnderIO_Accelerations(MixinClass.newMixinClass("To_Accelerate_EnderIO_Machine")
        .setClass("AccelerateTileEntity_Mixin")
        .setPackagePath(PackagePath.EnderIO)
        .setPhase(Phase.LATE)
        .addTargetMod(TargetMod.EnderIO)
        .addCondition(true),
        MixinClass.newMixinClass("Modify_Acceleration_Energy_Receive")
            .setClass("AccelerateEnergyReceive_Mixin")
            .setPackagePath(PackagePath.EnderIO)
            .setPhase(Phase.LATE)
            .addTargetMod(TargetMod.EnderIO)
            .addCondition(true)),

    CropsNH_Accelerations(MixinClass.newMixinClass("Modify_CropSticks_Acceleration")
        .setClass("TileEntityCropSticksAcceleration_Mixin")
        .setPackagePath(PackagePath.CropsNH)
        .setPhase(Phase.LATE)
        .addTargetMod(TargetMod.CropsNH)
        .addCondition(enableCropsNHAcceleration)),

    ForestryMC_Accelerations(MixinClass.newMixinClass("Modify_TileAlveary_Acceleration")
        .setClass("TileAlvearyAcceleration_Mixin")
        .setPackagePath(PackagePath.ForestryMC)
        .setPhase(Phase.LATE)
        .addTargetMod(TargetMod.ForestryMC)
        .addCondition(enableForestryAcceleration)),

    GregTech_WirelessGTDataStick(MixinClass.newMixinClass("Add_GT_LeftClick_DataStick")
        .setClass("GTMachineLeftClickDataStick_Mixin")
        .setPackagePath(PackagePath.GregTech)
        .setPhase(Phase.LATE)
        .addTargetMod(TargetMod.GregTech)
        .addCondition(enableFlashTorcherino)),

    OmniOcular_FilterXML(MixinClass.newMixinClass("Filter_OmniOcular_Torcherino_XML")
        .setClass("XMLConfigHandler_Mixin")
        .setPackagePath(PackagePath.OmniOcular)
        .setPhase(Phase.LATE)
        .addTargetMod(TargetMod.OmniOcular)
        .addCondition(true),
        MixinClass.newMixinClass("Filter_OmniOcular_JSEngine_Torcherino")
            .setClass("JSEngine_Mixin")
            .setPackagePath(PackagePath.OmniOcular)
            .setPhase(Phase.LATE)
            .addTargetMod(TargetMod.OmniOcular)
            .addCondition(true)),

    ;

    private final MixinClass[] MIXIN_CLASS;
    private final Supplier<Boolean> shouldApplyThisMixinGroup;

    Mixins(MixinClass... MIXIN_CLASS) {
        this(() -> true, MIXIN_CLASS);
    }

    Mixins(Supplier<Boolean> shouldApplyThisMixinGroup, MixinClass... MIXIN_CLASS) {
        this.MIXIN_CLASS = MIXIN_CLASS;
        this.shouldApplyThisMixinGroup = shouldApplyThisMixinGroup;
    }

    public static List<String> getLateMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        for (Mixins value : Mixins.values()) {
            if (!value.shouldApplyThisMixinGroup.get()) continue;
            for (MixinClass mixinClass : value.MIXIN_CLASS) {
                if (mixinClass.mClass.equals(MixinClass.ERROR)) continue;
                if (!mixinClass.phase.equals(Phase.LATE)) continue;
                if (!mixinClass.classPredicate.test(mixinClass)) continue;
                if (!loadedMods.containsAll(
                    mixinClass.targetMods.stream()
                        .map(TargetMod::getModId)
                        .collect(Collectors.toSet())))
                    continue;
                if (mixinClass.excludedMods.stream()
                    .map(TargetMod::getModId)
                    .anyMatch(loadedMods::contains)) continue;
                mixins.add(mixinClass.getMixinClassPath());
            }
        }
        return mixins;
    }

    enum PackagePath {

        GregTech,
        TecTech,
        EnderIO,
        CropsNH,
        ForestryMC,
        OmniOcular;

        private final String path;

        PackagePath() {
            this.path = this.toString();
        }
    }

    static class MixinClass {

        static final String ERROR = "TORCHERINO_MIXIN_ERROR";

        final String id;
        String mClass = ERROR;
        String packagePath;
        Phase phase = Phase.ERROR_PHASE;
        List<TargetMod> targetMods = new ArrayList<>();
        List<TargetMod> excludedMods = new ArrayList<>();
        Predicate<MixinClass> classPredicate = mixinClass -> true;

        public MixinClass(String id) {
            this.id = id;
        }

        static MixinClass newMixinClass(String aIdentifier) {
            return new MixinClass(aIdentifier);
        }

        MixinClass setClass(String mClass) {
            this.mClass = mClass;
            return this;
        }

        public MixinClass setPhase(Phase phase) {
            this.phase = phase;
            return this;
        }

        public String getMixinClassPath() {
            if (this.packagePath == null) return this.mClass;
            return this.packagePath + this.mClass;
        }

        public MixinClass setPackagePath(PackagePath... packagePath) {
            if (packagePath == null || packagePath.length == 0) return this;
            this.packagePath = Arrays.stream(packagePath)
                .map(p -> p.path)
                .collect(Collectors.joining(".", "", "."));
            return this;
        }

        MixinClass addTargetMod(TargetMod... targetMod) {
            targetMods.addAll(Arrays.asList(targetMod));
            return this;
        }

        MixinClass addCondition(boolean condition) {
            classPredicate = classPredicate.and(mixinClass -> condition);
            return this;
        }
    }

    enum Phase {
        LATE,
        EARLY,
        ERROR_PHASE
    }
}
