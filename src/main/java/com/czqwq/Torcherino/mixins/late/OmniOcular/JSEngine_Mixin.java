package com.czqwq.Torcherino.mixins.late.OmniOcular;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents OmniOcular's JS engine from processing any tile whose
 * registration ID contains "torcherino" (case-insensitive).
 * Returns empty list immediately, skipping JS evaluation entirely.
 */
@SuppressWarnings("UnusedMixin")
@org.spongepowered.asm.mixin.Pseudo
@Mixin(targets = "me.exz.omniocular.waila.JSEngine", remap = false)
public class JSEngine_Mixin {

    @Inject(method = "getBody", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void torcherino$filterTileEntity(Map<java.util.regex.Pattern, org.w3c.dom.Node> patternMap,
        NBTTagCompound n, String id, EntityPlayer player, CallbackInfoReturnable<List<String>> cir) {
        if (id != null && id.toLowerCase()
            .contains("torcherino")) {
            cir.setReturnValue(new ArrayList<String>());
        }
    }
}
