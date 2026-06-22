package com.czqwq.Torcherino.api.interfaces;

/**
 * Common interface for all Torcherino tile entities.
 * Provides unified access to torch state regardless of implementation variant (GUI or Classic).
 */
public interface ITorcherinoTile {

    /** Whether this torch is currently active (not disabled by redstone). */
    boolean getActive();

    /** Enable or disable this torch via redstone signal. */
    void setActive(boolean active);

    /** Whether this torch is in stopped mode (paused by player). */
    boolean isStopped();

    /** Get the effective acceleration speed (already multiplied). */
    int getEffectiveSpeed();

    /** X coordinate of this torch in the world. */
    int getTorchX();

    /** Y coordinate of this torch in the world. */
    int getTorchY();

    /** Z coordinate of this torch in the world. */
    int getTorchZ();

    /** Radius on the X axis (0-based). */
    int getXRadius();

    /** Radius on the Y axis (0-based). */
    int getYRadius();

    /** Radius on the Z axis (0-based). */
    int getZRadius();
}
