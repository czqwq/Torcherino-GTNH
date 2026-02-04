package com.czqwq.Torcherino.api.interfaces;

/**
 * ITileEntityTickAcceleration interface is used to support time acceleration for specific TileEntities
 * Only TileEntities that implement this interface can enjoy precise time acceleration without affecting energy consumption speed
 */
public interface ITileEntityTickAcceleration {

    /**
     * <li>true if the tickAcceleration logic should be executed.</li>
     * <li>false if the default TileEntity update method should proceed.</li>
     */
    boolean tickAcceleration(int tickAcceleratedRate);

    /**
     * adaptation to other aspects of the tileEntity
     */
    default int getTickAcceleratedRate() {
        return 1;
    }
}
