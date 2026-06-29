package com.czqwq.Torcherino.mixins;

import gregtech.api.enums.Mods;

@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public enum TargetMod {

    GregTech("GregTech", Mods.GregTech.ID),
    TecTech("TecTech", Mods.TecTech.ID),
    GigaGramFab("GGFab", Mods.GGFab.ID),
    EnderIO("EnderIO", Mods.EnderIO.ID),
    CropsNH("CropsNH", Mods.CropsNH.ID),
    ForestryMC("ForestryMC", Mods.Forestry.ID);

    private final String modId;
    public final String modName;

    TargetMod(String modName, String modId) {
        this.modName = modName;
        this.modId = modId;
    }

    public String getModId() {
        return modId;
    }

    public String getModName() {
        return modName;
    }
}
