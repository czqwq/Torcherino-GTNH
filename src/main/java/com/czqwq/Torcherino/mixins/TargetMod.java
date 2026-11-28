package com.czqwq.Torcherino.mixins;

@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public enum TargetMod {

    GregTech("GregTech", "gregtech"),
    TecTech("TecTech", "tectech"),
    GigaGramFab("GGFab", "ggfab"),
    EnderIO("EnderIO", "EnderIO"),
    CropsNH("CropsNH", "cropsnh"),
    ForestryMC("ForestryMC", "Forestry"),
    OmniOcular("OmniOcularUnofficial", "OmniOcularUnofficial");

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
