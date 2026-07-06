package com.czqwq.Torcherino.mixins.late.OmniOcular;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks OmniOcular from processing any Torcherino-related XML.
 * <p>
 * 1. {@code releasePreConfigFiles}: creates empty placeholder to prevent JAR extraction.
 * 2. {@code mergeConfig}: deletes the file, then filters it from the directory listing
 * so it can never be included in OmniOcular's merged JS config.
 */
@SuppressWarnings("UnusedMixin")
@org.spongepowered.asm.mixin.Pseudo
@Mixin(targets = "me.exz.omniocular.handler.XMLConfigHandler", remap = false)
public class XMLConfigHandler_Mixin {

    // ========== releasePreConfigFiles ==========

    @Inject(method = "releasePreConfigFiles", at = @At("HEAD"), remap = false, require = 0)
    private static void torcherino$blockExtraction(CallbackInfo ci) {
        File f = getTorcherinoXml();
        if (f != null && !f.exists()) {
            try {
                f.getParentFile()
                    .mkdirs();
                f.createNewFile();
            } catch (Exception ignored) {}
        }
    }

    // ========== mergeConfig ==========

    /**
     * Delete Torcherino.xml before the directory listing. Belt.
     */
    @Inject(method = "mergeConfig", at = @At("HEAD"), remap = false, require = 0)
    private static void torcherino$deleteBeforeMerge(CallbackInfo ci) {
        File f = getTorcherinoXml();
        if (f != null && f.exists()) {
            f.delete();
        }
    }

    /**
     * Filter {@code configDir.listFiles()} to exclude any "torcherino" XML. Suspenders.
     */
    @Redirect(
        method = "mergeConfig",
        at = @At(value = "INVOKE", target = "Ljava/io/File;listFiles()[Ljava/io/File;"),
        remap = false,
        require = 0)
    private static File[] torcherino$filterFileList(File configDir) {
        File[] all = configDir.listFiles();
        if (all == null) return null;
        List<File> filtered = new ArrayList<>(all.length);
        for (File f : all) {
            if (f != null && !f.getName()
                .toLowerCase()
                .contains("torcherino")) {
                filtered.add(f);
            }
        }
        return filtered.toArray(new File[0]);
    }

    // ========== Helper ==========

    private static File getTorcherinoXml() {
        try {
            Class<?> clz = Class.forName("me.exz.omniocular.handler.XMLConfigHandler");
            Field f = clz.getDeclaredField("configDir");
            f.setAccessible(true);
            File dir = (File) f.get(null);
            return dir != null ? new File(dir, "Torcherino.xml") : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
