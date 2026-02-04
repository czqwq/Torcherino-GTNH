package com.czqwq.Torcherino.mixins;

import static com.czqwq.Torcherino.Config.enableAccelerateGregTechMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public enum Mixins {

    GregTech_Accelerations(
        MixinClass.newMixinClass("Modify_BaseMTE_With_Acceleration")
            .setClass("BaseMetaTileEntityAcceleration_Mixin")
            .setPackagePath(PackagePath.GregTech)
            .setPhase(Phase.LATE)
            .addTargetMod(TargetMod.GregTech)
            .addCondition(enableAccelerateGregTechMachine),
        MixinClass.newMixinClass("Modify_ResearchStation_Acceleration")
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
            .addCondition(enableAccelerateGregTechMachine)
    ),

    EnderIO_Accelerations(
        MixinClass.newMixinClass("To_Accelerate_EnderIO_Machine")
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
            .addCondition(true)
    ),

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
                        .collect(Collectors.toSet())
                )) continue;
                mixins.add(mixinClass.getMixinClassPath());
            }
        }
        return mixins;
    }

    enum PackagePath {
        GregTech,
        TecTech,
        EnderIO;

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
