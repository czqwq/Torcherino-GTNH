package com.czqwq.Torcherino.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.tile.TileWirelessTorcherino;
import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;

import gregtech.api.enums.ItemList;

public class BlockWirelessTorcherino extends BlockTorcherinoBase {

    public BlockWirelessTorcherino() {
        super("torcherino", 0.75f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileWirelessTorcherino();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileWirelessTorcherinoBase)) return true;
        TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) tile;

        if (handleDataStickInteraction(world, x, y, z, player, torch)) return true;

        TileEntityGuiFactory.INSTANCE.open(player, torch);
        return true;
    }

    /**
     * Shared DataStick interaction handler. Returns true if a DataStick interaction was performed.
     * <p>
     * Binding flow:<br>
     * 1. Left-click a machine with DataStick → GT native or mixin saves machine coords to stick<br>
     * 2. Right-click the torch with that DataStick → this method binds the machine<br>
     */
    public static boolean handleDataStickInteraction(World world, int x, int y, int z, EntityPlayer player,
        TileWirelessTorcherinoBase torch) {
        if (!Torcherino.hasGregTech) return false;

        ItemStack held = player.getHeldItem();
        if (held == null || !ItemList.Tool_DataStick.isStackEqual(held, false, true)) return false;

        NBTTagCompound tag = held.stackTagCompound;
        if (tag == null) return false;

        // Only handle DataSticks that have machine binding data
        if (!"WirelessTorcherino".equals(tag.getString("type"))) return false;

        // Read saved machine coordinates
        if (!tag.hasKey("machineX") || !tag.hasKey("machineY") || !tag.hasKey("machineZ")) return false;

        int machineX = tag.getInteger("machineX");
        int machineY = tag.getInteger("machineY");
        int machineZ = tag.getInteger("machineZ");
        int machineDim = tag.hasKey("machineDim") ? tag.getInteger("machineDim") : world.provider.dimensionId;

        // Verify the machine still exists at saved coordinates
        TileEntity targetTe = world.getTileEntity(machineX, machineY, machineZ);
        if (targetTe == null || targetTe.isInvalid()) {
            player.addChatMessage(
                new ChatComponentText(StatCollector.translateToLocal("torcherino.wireless.datastick.machine_gone")));
            clearDataStick(held, tag);
            return true;
        }

        // Verify dimension matches
        if (machineDim != world.provider.dimensionId) {
            player.addChatMessage(
                new ChatComponentText(StatCollector.translateToLocal("torcherino.wireless.datastick.wrong_dim")));
            clearDataStick(held, tag);
            return true;
        }

        // Check range
        if (!torch.isInRange(machineX, machineY, machineZ)) {
            player.addChatMessage(
                new ChatComponentText(StatCollector.translateToLocal("torcherino.wireless.datastick.out_of_range")));
            clearDataStick(held, tag);
            return true;
        }

        // Don't bind torch to itself
        if (machineX == torch.xCoord && machineY == torch.yCoord && machineZ == torch.zCoord) {
            player.addChatMessage(
                new ChatComponentText(
                    StatCollector.translateToLocal("torcherino.wireless.datastick.cannot_bind_self")));
            clearDataStick(held, tag);
            return true;
        }

        // Bind the machine
        if (torch.addBoundMachine(machineX, machineY, machineZ, machineDim)) {
            String machineName = tag.hasKey("machineName") ? tag.getString("machineName") : "Unknown";
            player.addChatMessage(
                new ChatComponentText(
                    StatCollector.translateToLocalFormatted("torcherino.wireless.datastick.bound", machineName)));
        } else {
            player.addChatMessage(
                new ChatComponentText(StatCollector.translateToLocal("torcherino.wireless.datastick.bind_failed")));
        }

        clearDataStick(held, tag);
        return true;
    }

    /**
     * Safely clear wireless torcherino data from a DataStick and reset its display name.
     */
    private static void clearDataStick(ItemStack held, NBTTagCompound tag) {
        tag.removeTag("type");
        tag.removeTag("machineX");
        tag.removeTag("machineY");
        tag.removeTag("machineZ");
        tag.removeTag("machineDim");
        tag.removeTag("machineName");
        tag.removeTag("torchX");
        tag.removeTag("torchY");
        tag.removeTag("torchZ");

        // Safely clear display name by removing the display.Name NBT tag
        if (held.stackTagCompound != null) {
            if (held.stackTagCompound.hasKey("display")) {
                NBTTagCompound display = held.stackTagCompound.getCompoundTag("display");
                display.removeTag("Name");
                if (display.hasNoTags()) {
                    held.stackTagCompound.removeTag("display");
                }
            }
            // Clean up empty tag
            if (held.stackTagCompound.hasNoTags()) {
                held.stackTagCompound = null;
            }
        }
    }
}
