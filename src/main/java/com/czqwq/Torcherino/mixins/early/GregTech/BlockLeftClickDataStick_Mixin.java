package com.czqwq.Torcherino.mixins.early.GregTech;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;

import gregtech.api.enums.ItemList;
import gregtech.api.metatileentity.BaseMetaTileEntity;

/**
 * Early mixin into vanilla Block.onBlockClicked to intercept left-clicks with
 * DataStick on non-GT machines. GT machines are handled by a separate late mixin
 * on CommonMetaTileEntity.onLeftclick.
 * <p>
 * This mixin only fires for vanilla/non-GT blocks that do NOT override
 * onBlockClicked. GT's BlockMachines overrides it, so GT machines are
 * excluded automatically.
 */
@Pseudo
@SuppressWarnings("UnusedMixin")
@Mixin(Block.class)
public abstract class BlockLeftClickDataStick_Mixin {

    @Inject(method = "onBlockClicked", at = @At("HEAD"))
    private void torcherino$onBlockClicked(World world, int x, int y, int z, EntityPlayer player, CallbackInfo ci) {
        if (world.isRemote) return;
        if (!Config.enableFlashTorcherino) return;

        ItemStack held = player.getHeldItem();
        if (held == null || held.stackTagCompound == null) return;
        if (!ItemList.Tool_DataStick.isStackEqual(held, false, true)) return;

        // GT machines handle left-click DataStick binding themselves via BlockMachines.onBlockClicked.
        // Since BlockMachines overrides onBlockClicked, this mixin won't fire for GT blocks.
        // But double-check in case some GT block doesn't use BlockMachines.
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof BaseMetaTileEntity) return;
        if (te instanceof TileWirelessTorcherinoBase) return;
        if (te == null || te.isInvalid()) return;

        // Save machine coordinates to DataStick
        NBTTagCompound tag = held.stackTagCompound;
        tag.setString("type", "WirelessTorcherino");
        tag.setInteger("machineX", x);
        tag.setInteger("machineY", y);
        tag.setInteger("machineZ", z);
        tag.setInteger("machineDim", world.provider.dimensionId);

        // Clear any old torch data
        tag.removeTag("torchX");
        tag.removeTag("torchY");
        tag.removeTag("torchZ");

        String machineName = getMachineName(world, x, y, z, te);
        tag.setString("machineName", machineName);

        String displayName = StatCollector.translateToLocalFormatted("torcherino.wireless.datastick.name", machineName);
        if (displayName == null || displayName.isEmpty()) {
            displayName = "Wireless Torcherino Link (" + machineName + ")";
        }
        held.setStackDisplayName(displayName);

        player.addChatMessage(
            new ChatComponentText(
                StatCollector
                    .translateToLocalFormatted("torcherino.wireless.datastick.machine_saved", machineName, x, y, z)));
    }

    private static String getMachineName(World world, int x, int y, int z, TileEntity te) {
        // GT machines shouldn't reach here, but handle gracefully
        if (te instanceof BaseMetaTileEntity) {
            BaseMetaTileEntity bmte = (BaseMetaTileEntity) te;
            if (bmte.getMetaTileEntity() != null) {
                String name = bmte.getMetaTileEntity()
                    .getLocalName();
                if (name != null && !name.isEmpty()) {
                    String translated = StatCollector.translateToLocal(name);
                    if (!translated.isEmpty() && !translated.equals(name)) return translated;
                    return name;
                }
            }
        }

        Block block = world.getBlock(x, y, z);
        if (block != null) {
            String name = block.getLocalizedName();
            if (name != null && !name.isEmpty()) {
                String translated = StatCollector.translateToLocal(name);
                return !translated.isEmpty() ? translated : name;
            }
        }
        return "Unknown";
    }
}
