package com.czqwq.Torcherino.api;

/**
 * Bridge interface used by mixins to expose acceleration state to Torcherino and other code.
 */
public interface IAccelerationState {
    boolean getMachineAccelerationState();
    void setAccelerationState(boolean state);
}