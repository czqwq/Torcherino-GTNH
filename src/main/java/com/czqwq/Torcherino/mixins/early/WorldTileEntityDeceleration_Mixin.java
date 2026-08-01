package com.czqwq.Torcherino.mixins.early;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.czqwq.Torcherino.util.DecelerationHelper;

/**
 * Early mixin into {@link World#updateEntities()} to implement deceleration.
 * <p>
 * In 1.7.10 {@code World.updateEntities()} calls {@code tileEntity.updateEntity()}
 * unconditionally — {@code TileEntity.canUpdate()} is never consulted by the
 * vanilla tick loop, so the tile's own tick must be intercepted at this single
 * call site instead. Redirecting the call covers every TileEntity in the world,
 * including all GregTech machines: both single-block machines and multiblock
 * controllers are hosted in a single {@code BaseMetaTileEntity}, and GT pipes in
 * {@code BaseMetaPipeEntity} — all of them are ticked through this one call.
 * <p>
 * On skipped ticks the whole {@code updateEntity()} never runs, so EU
 * consumption/generation and all other side effects are slowed together.
 */
@Mixin(World.class)
public abstract class WorldTileEntityDeceleration_Mixin {

    @Redirect(
        method = "updateEntities",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;updateEntity()V"))
    private void torcherino$decelerateTileTick(TileEntity tile) {
        if (!DecelerationHelper.shouldSkipUpdate(tile)) {
            tile.updateEntity();
        }
    }
}
