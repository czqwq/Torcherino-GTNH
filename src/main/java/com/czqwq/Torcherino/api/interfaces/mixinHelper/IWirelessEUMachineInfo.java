package com.czqwq.Torcherino.api.interfaces.mixinHelper;

import java.math.BigInteger;

/**
 * Interface for GT machines that consume all EU upfront from the wireless EU network,
 * resulting in mEUt == 0 during recipe execution (e.g. MTEEyeOfHarmony, MTETranscendentPlasmaMixer).
 * Exposes the total EU consumed at recipe start so the Perfect Time Twister can charge correctly.
 */
public interface IWirelessEUMachineInfo {

    /**
     * @return the absolute value of the EU consumed from the wireless network when the current recipe started.
     *         Returns {@link BigInteger#ZERO} if no recipe is running.
     */
    BigInteger torcherino$getConsumedWirelessEU();
}
