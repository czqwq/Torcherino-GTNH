package com.czqwq.Torcherino.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.utils.ICopy;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.utils.serialization.IEquals;

/**
 * Lightweight GUI-only data for syncing bound machine info to client.
 * Contains NO live TileEntity references — only primitives and strings.
 * Safe for network serialization and client-side rendering.
 */
public class WirelessTorcherinoGuiEntry {

    public final int x, y, z, dim;
    public int perMachineSpeed;
    public final String machineName;

    public WirelessTorcherinoGuiEntry(int x, int y, int z, int dim, int perMachineSpeed, String machineName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.perMachineSpeed = perMachineSpeed;
        this.machineName = machineName;
    }

    /**
     * Create a GUI entry from a BoundMachineEntry, computing the localized name server-side.
     */
    public static WirelessTorcherinoGuiEntry fromBoundEntry(BoundMachineEntry entry, String machineName) {
        return new WirelessTorcherinoGuiEntry(entry.x, entry.y, entry.z, entry.dim, entry.perMachineSpeed, machineName);
    }

    // ========== NBT serialization (used by PacketBuffer) ==========

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
        tag.setInteger("dim", dim);
        tag.setInteger("speed", perMachineSpeed);
        tag.setString("name", machineName != null ? machineName : "");
        return tag;
    }

    public static WirelessTorcherinoGuiEntry fromNBT(NBTTagCompound tag) {
        int x = tag.getInteger("x");
        int y = tag.getInteger("y");
        int z = tag.getInteger("z");
        int dim = tag.getInteger("dim");
        int speed = tag.getInteger("speed");
        String name = tag.getString("name");
        return new WirelessTorcherinoGuiEntry(x, y, z, dim, speed, name);
    }

    // ========== Serialization helpers for GenericListSyncHandler ==========

    public static final IByteBufSerializer<WirelessTorcherinoGuiEntry> SERIALIZER = new IByteBufSerializer<WirelessTorcherinoGuiEntry>() {

        @Override
        public void serialize(PacketBuffer buf, WirelessTorcherinoGuiEntry entry) {
            try {
                buf.writeNBTTagCompoundToBuffer(entry.toNBT());
            } catch (Exception ignored) {}
        }
    };

    public static final IByteBufDeserializer<WirelessTorcherinoGuiEntry> DESERIALIZER = new IByteBufDeserializer<WirelessTorcherinoGuiEntry>() {

        @Override
        public WirelessTorcherinoGuiEntry deserialize(PacketBuffer buf) {
            try {
                return fromNBT(buf.readNBTTagCompoundFromBuffer());
            } catch (Exception e) {
                return new WirelessTorcherinoGuiEntry(0, 0, 0, 0, 0, "???");
            }
        }
    };

    public static final IEquals<WirelessTorcherinoGuiEntry> EQUALS = (a, b) -> a.x == b.x && a.y == b.y
        && a.z == b.z
        && a.dim == b.dim
        && a.perMachineSpeed == b.perMachineSpeed
        && a.machineName.equals(b.machineName);

    public static final ICopy<WirelessTorcherinoGuiEntry> COPY = entry -> new WirelessTorcherinoGuiEntry(
        entry.x,
        entry.y,
        entry.z,
        entry.dim,
        entry.perMachineSpeed,
        entry.machineName);
}
