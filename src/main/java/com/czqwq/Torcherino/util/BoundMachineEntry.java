package com.czqwq.Torcherino.util;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Torcherino;

import gregtech.api.metatileentity.BaseMetaTileEntity;

/**
 * Lightweight serializable holder for a single flash-bound machine.
 * Identified uniquely by (x, y, z, dim). Equality and hashcode use only these four fields.
 */
public class BoundMachineEntry {

    public final int x, y, z, dim;
    public int perMachineSpeed;

    public BoundMachineEntry(int x, int y, int z, int dim, int perMachineSpeed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.perMachineSpeed = perMachineSpeed;
    }

    public BoundMachineEntry(int x, int y, int z, int dim) {
        this(x, y, z, dim, 0);
    }

    // ========== Localized name ==========

    /**
     * Get the localized display name for the machine at this entry's coordinates.
     * For GT machines, returns the meta tile entity's localized name.
     * For other TEs, returns the block's localized name.
     * Never returns an unlocalized string.
     *
     * @param world the world (must not be null, must match dim)
     * @return localized machine name, or "???" if the TE cannot be found
     */
    public String getLocalizedName(World world) {
        if (world == null || world.provider.dimensionId != this.dim) {
            return "???";
        }

        TileEntity te = world.getTileEntity(x, y, z);
        if (te == null) {
            return "???";
        }

        // GT machines: use meta tile entity's localized name
        if (Torcherino.hasGregTech && te instanceof BaseMetaTileEntity) {
            BaseMetaTileEntity bmte = (BaseMetaTileEntity) te;
            if (bmte.getMetaTileEntity() != null) {
                String name = bmte.getMetaTileEntity()
                    .getLocalName();
                // getLocalName() returns the localized display name
                // but guard against raw translation keys
                if (name != null && !name.isEmpty()) {
                    // Try to translate: if the result differs, it was a valid key
                    String translated = StatCollector.translateToLocal(name);
                    if (!translated.equals(name) || !name.contains(".")) {
                        return translated.isEmpty() ? name : translated;
                    }
                    return name;
                }
            }
        }

        // Fallback: block's localized name
        String blockName = world.getBlock(x, y, z)
            .getLocalizedName();
        if (blockName != null && !blockName.isEmpty()) {
            // translateToLocal ensures it's properly localized
            String translated = StatCollector.translateToLocal(blockName);
            return translated.isEmpty() ? blockName : translated;
        }

        return "???";
    }

    // ========== NBT serialization ==========

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
        tag.setInteger("dim", dim);
        tag.setInteger("speed", perMachineSpeed);
        return tag;
    }

    public static BoundMachineEntry fromNBT(NBTTagCompound tag) {
        int x = tag.getInteger("x");
        int y = tag.getInteger("y");
        int z = tag.getInteger("z");
        int dim = tag.getInteger("dim");
        int speed = tag.getInteger("speed");
        return new BoundMachineEntry(x, y, z, dim, speed);
    }

    // ========== Equality (based on position + dim only) ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoundMachineEntry)) return false;
        BoundMachineEntry that = (BoundMachineEntry) o;
        return x == that.x && y == that.y && z == that.z && dim == that.dim;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, dim);
    }

    @Override
    public String toString() {
        return "BoundMachine{" + x + ", " + y + ", " + z + ", dim=" + dim + ", speed=" + perMachineSpeed + "}";
    }

    /**
     * Pack coordinates into a single long for map key usage.
     */
    public static long packPosition(int x, int y, int z, int dim) {
        return ((long) dim & 0xFF) << 56 | ((long) x & 0x1FFFFFL) << 35
            | ((long) y & 0x1FFL) << 26
            | ((long) z & 0x1FFFFFL);
    }
}
