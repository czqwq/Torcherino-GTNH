package com.czqwq.Torcherino.mixins;

public enum TargetMod {
    GregTech("gregtech"),
    TecTech("tectech"),
    GigaGramFab("ggfab"),
    EnderIO("EnderIO");

    private final String modId;

    TargetMod(String modId) {
        this.modId = modId;
    }

    public String getModId() {
        return modId;
    }
}
