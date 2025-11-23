//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.sci.torcherino.tile;

import cofh.api.energy.IEnergyHandler;
import com.sci.torcherino.Torcherino;
import com.sci.torcherino.TorcherinoRegistry;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;

@Interface(
    iface = "cofh.api.energy.IEnergyHandler",
    modid = "CoFHCore"
)
public class TileTorcherino extends TileEntity implements IEnergyHandler {
    private static final String[] MODES = new String[]{"Stopped", "Radius: +1, Area: 3x3x3", "Radius: +2, Area: 5x3x5", "Radius: +3, Area: 7x3x7", "Radius: +4, Area: 9x3x9"};
    private static final int SPEEDS = 4;
    private static final int MAX_ENERGY_STORED = 1024;
    private int redstoneFlux;
    private boolean requiredRedstoneState;
    private boolean poweredByRedstone;
    private byte speed;
    private byte mode;
    private byte cachedMode;
    private Random rand;
    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax;
    private int zMax;

    public TileTorcherino() {
        this(false);
    }

    public TileTorcherino(boolean requiredRedstoneState) {
        this.requiredRedstoneState = requiredRedstoneState;
        this.cachedMode = -1;
        this.rand = new Random();
    }

    protected int speed(int base) {
        return base;
    }

    public void func_145845_h() {
        if (!this.field_145850_b.field_72995_K) {
            if (this.poweredByRedstone == this.requiredRedstoneState && this.mode != 0 && this.speed != 0) {
                if (!Torcherino.useRF || this.redstoneFlux > 1) {
                    this.updateCachedModeIfNeeded();
                    this.tickNeighbors();
                }
            }
        }
    }

    private void updateCachedModeIfNeeded() {
        if (this.cachedMode != this.mode) {
            this.xMin = this.field_145851_c - this.mode;
            this.yMin = this.field_145848_d - 1;
            this.zMin = this.field_145849_e - this.mode;
            this.xMax = this.field_145851_c + this.mode;
            this.yMax = this.field_145848_d + 1;
            this.zMax = this.field_145849_e + this.mode;
            this.cachedMode = this.mode;
        }

    }

    private void tickNeighbors() {
        for(int x = this.xMin; x <= this.xMax; ++x) {
            for(int y = this.yMin; y <= this.yMax; ++y) {
                for(int z = this.zMin; z <= this.zMax; ++z) {
                    this.tickBlock(x, y, z);
                }
            }
        }

    }

    private void tickBlock(int x, int y, int z) {
        Block block = this.field_145850_b.func_147439_a(x, y, z);
        if (block != null) {
            if (!TorcherinoRegistry.isBlockBlacklisted(block)) {
                if (!(block instanceof BlockFluidBase)) {
                    if (block.func_149653_t()) {
                        for(int i = 0; i < this.speed; ++i) {
                            if (Torcherino.useRF) {
                                if (this.useEnergy(1)) {
                                    block.func_149674_a(this.field_145850_b, x, y, z, this.rand);
                                }
                            } else {
                                block.func_149674_a(this.field_145850_b, x, y, z, this.rand);
                            }
                        }
                    }

                    if (block.hasTileEntity(this.field_145850_b.func_72805_g(x, y, z))) {
                        TileEntity tile = this.field_145850_b.func_147438_o(x, y, z);
                        if (tile != null && !tile.func_145837_r()) {
                            if (TorcherinoRegistry.isTileBlacklisted(tile.getClass())) {
                                return;
                            }

                            for(int i = 0; i < this.speed(this.speed) && !tile.func_145837_r(); ++i) {
                                if (Torcherino.useRF) {
                                    if (this.useEnergy(1)) {
                                        tile.func_145845_h();
                                    }
                                } else {
                                    tile.func_145845_h();
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private boolean useEnergy(int amt) {
        if (this.redstoneFlux >= amt) {
            this.redstoneFlux -= amt;
            return true;
        } else {
            return false;
        }
    }

    public void setPoweredByRedstone(boolean poweredByRedstone) {
        this.poweredByRedstone = poweredByRedstone;
    }

    public void changeMode(boolean sneaking) {
        if (sneaking) {
            if (this.speed < 4) {
                ++this.speed;
            } else {
                this.speed = 0;
            }
        } else if (this.mode < MODES.length - 1) {
            ++this.mode;
        } else {
            this.mode = 0;
        }

    }

    public String getSpeedDescription() {
        return this.speed(this.speed) * 100 + "% increase";
    }

    public String getModeDescription() {
        return MODES[this.mode];
    }

    public void func_145841_b(NBTTagCompound tag) {
        super.func_145841_b(tag);
        tag.func_74757_a("RequiredRedstoneState", this.requiredRedstoneState);
        tag.func_74774_a("Speed", this.speed);
        tag.func_74774_a("Mode", this.mode);
        tag.func_74757_a("PoweredByRedstone", this.poweredByRedstone);
        tag.func_74768_a("EnergyStored", this.redstoneFlux);
    }

    public void func_145839_a(NBTTagCompound tag) {
        super.func_145839_a(tag);
        this.requiredRedstoneState = tag.func_74767_n("RequiredRedstoneState");
        this.speed = tag.func_74771_c("Speed");
        this.mode = tag.func_74771_c("Mode");
        this.poweredByRedstone = tag.func_74767_n("PoweredByRedstone");
        this.redstoneFlux = tag.func_74762_e("EnergyStored");
    }

    @Method(
        modid = "CoFHCore"
    )
    public final int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        if (!Torcherino.useRF) {
            return 0;
        } else {
            int energyReceived = Math.min(1024 - this.redstoneFlux, maxReceive);
            if (!simulate) {
                this.redstoneFlux += energyReceived;
            }

            return energyReceived;
        }
    }

    @Method(
        modid = "CoFHCore"
    )
    public final int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Method(
        modid = "CoFHCore"
    )
    public final int getEnergyStored(ForgeDirection from) {
        return !Torcherino.useRF ? 0 : this.redstoneFlux;
    }

    @Method(
        modid = "CoFHCore"
    )
    public final int getMaxEnergyStored(ForgeDirection from) {
        return !Torcherino.useRF ? 0 : 1024;
    }

    @Method(
        modid = "CoFHCore"
    )
    public final boolean canConnectEnergy(ForgeDirection from) {
        return Torcherino.useRF;
    }
}
