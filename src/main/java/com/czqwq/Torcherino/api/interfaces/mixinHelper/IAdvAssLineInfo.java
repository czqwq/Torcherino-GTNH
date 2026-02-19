package com.czqwq.Torcherino.api.interfaces.mixinHelper;

/**
 * Interface for MTEAdvAssLine that exposes the current recipe input length (number of slices),
 * enabling the Perfect Time Twister to calculate per-slice acceleration boundaries.
 */
public interface IAdvAssLineInfo {

    /**
     * @return the number of input slices in the currently running recipe,
     *         or a non-positive value ({@code 0} or {@code -1}) if no recipe is running.
     */
    int torcherino$getCurrentInputLength();
}
