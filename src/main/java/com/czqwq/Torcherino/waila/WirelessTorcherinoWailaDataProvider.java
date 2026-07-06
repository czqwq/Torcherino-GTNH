package com.czqwq.Torcherino.waila;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;
import com.czqwq.Torcherino.util.BoundMachineEntry;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

/**
 * WAILA data provider for wireless torcherino tiles.
 * Follows the same pattern as AE2's {@code WirelessDataProvider}:
 * standalone class, direct import of client renderer, instanceof checks on the TE.
 * <p>
 * Registered via {@link WirelessTorcherinoWailaProvider#callbackRegister}.
 */
public class WirelessTorcherinoWailaDataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (!(te instanceof TileWirelessTorcherinoBase)) {
            return currenttip;
        }
        final TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) te;

        NBTTagCompound tag = accessor.getNBTData();
        int count = tag.getInteger("waila_bm_count");
        int globalSpeed = tag.getInteger("waila_global_speed");
        int multiplier = torch.getSpeedMultiplier();
        boolean isSneaking = tag.getBoolean("isSneaking");

        // Beam rendering is handled independently by WirelessBeamRenderer
        // which checks mc.objectMouseOver each frame (no trigger needed)

        currenttip.add(
            translateToLocal("torcherino.waila.wireless.global_speed") + ": " + (globalSpeed * multiplier * 100) + "%");

        if (count == 0) {
            currenttip.add(translateToLocal("torcherino.waila.wireless.no_bound"));
            return currenttip;
        }

        NBTTagList entries = tag.getTagList("waila_bm_entries", 10);

        if (isSneaking) {
            currenttip.add(translateToLocal("torcherino.waila.wireless.bound_count") + ": " + count);
            for (int i = 0; i < entries.tagCount(); i++) {
                NBTTagCompound entryTag = entries.getCompoundTagAt(i);
                String name = entryTag.getString("name");
                int bx = entryTag.getInteger("x");
                int by = entryTag.getInteger("y");
                int bz = entryTag.getInteger("z");
                int speed = entryTag.getInteger("speed");
                String speedStr = speed > 0 ? (speed * multiplier * 100) + "%"
                    : translateToLocal("torcherino.waila.wireless.use_global");
                currenttip.add("  " + name + " (" + bx + ", " + by + ", " + bz + ") [" + speedStr + "]");
            }
        } else {
            currenttip.add(
                translateToLocal("torcherino.waila.wireless.bound_count") + ": "
                    + count
                    + "  §7["
                    + translateToLocal("torcherino.waila.wireless.shift_hint")
                    + "]");
            int show = Math.min(entries.tagCount(), 3);
            for (int i = 0; i < show; i++) {
                NBTTagCompound entryTag = entries.getCompoundTagAt(i);
                String name = entryTag.getString("name");
                int speed = entryTag.getInteger("speed");
                String speedStr = speed > 0 ? (speed * multiplier * 100) + "%"
                    : translateToLocal("torcherino.waila.wireless.use_global");
                currenttip.add("  " + name + " [" + speedStr + "]");
            }
            if (entries.tagCount() > 3) {
                currenttip.add("  ... +" + (entries.tagCount() - 3) + " more");
            }
        }
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
        int y, int z) {
        if (!(te instanceof TileWirelessTorcherinoBase)) return tag;
        final TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) te;

        List<BoundMachineEntry> boundMachines = torch.getBoundMachines();

        tag.setInteger("waila_bm_count", boundMachines.size());
        tag.setInteger("waila_global_speed", torch.getGlobalSpeedLevel());
        tag.setBoolean("isSneaking", player.isSneaking());
        tag.setInteger("torchX", te.xCoord);
        tag.setInteger("torchY", te.yCoord);
        tag.setInteger("torchZ", te.zCoord);

        NBTTagList entries = new NBTTagList();
        for (BoundMachineEntry entry : boundMachines) {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setString("name", entry.getLocalizedName(world));
            entryTag.setInteger("x", entry.x);
            entryTag.setInteger("y", entry.y);
            entryTag.setInteger("z", entry.z);
            entryTag.setInteger("speed", entry.perMachineSpeed > 0 ? entry.perMachineSpeed : 0);
            entries.appendTag(entryTag);
        }
        tag.setTag("waila_bm_entries", entries);
        return tag;
    }
}
