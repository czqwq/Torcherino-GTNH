package com.czqwq.Torcherino.api.interfaces;

/**
 * Marker interface for tile entities that handle deceleration precisely:
 * they tick normally every world tick (so energy flows are NOT affected) while
 * their progress bar is scaled down by the decelerate torches.
 * <p>
 * Implemented on GregTech's {@code BaseMetaTileEntity} via the early
 * {@code BaseMetaTileEntityDeceleration_Mixin}. The deceleration hook in
 * {@code World.updateEntities()} lets these tiles tick normally and relies on
 * the mixin's {@code updateEntityProfiled()} HEAD/TAIL injects to hold back the
 * natural progress gain on the skipped fraction of ticks.
 */
public interface ITileEntityTickDeceleration {
}
