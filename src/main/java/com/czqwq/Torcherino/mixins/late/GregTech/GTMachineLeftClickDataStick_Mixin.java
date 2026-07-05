package com.czqwq.Torcherino.mixins.late.GregTech;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.czqwq.Torcherino.Config;

import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

/**
 * Late mixin into CommonMetaTileEntity.onLeftclick to save machine coordinates
 * to DataStick on left-click. This fires for ALL GT machines (single-block and
 * multi-block) whose onLeftclick calls super (the default behavior).
 * <p>
 * Machines that override onLeftclick WITHOUT calling super (PCB Factory,
 * Purification Plant, InputBusCompressed) are excluded — they already handle
 * DataStick binding for their own purposes.
 */
@Pseudo
@SuppressWarnings("UnusedMixin")
@Mixin(targets = "gregtech.api.metatileentity.CommonMetaTileEntity", remap = false)
public abstract class GTMachineLeftClickDataStick_Mixin {

    @Inject(method = "onLeftclick", at = @At("HEAD"), cancellable = true, remap = false)
    private void torcherino$onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer,
        CallbackInfo ci) {
        if (aBaseMetaTileEntity == null || aBaseMetaTileEntity.isClientSide()) return;
        if (aPlayer == null) return;
        if (!Config.enableFlashTorcherino) return;

        ItemStack held = aPlayer.getHeldItem();
        if (held == null || !ItemList.Tool_DataStick.isStackEqual(held, false, true)) return;

        NBTTagCompound tag = held.stackTagCompound;
        if (tag == null) {
            tag = new NBTTagCompound();
            held.stackTagCompound = tag;
        }

        int x = aBaseMetaTileEntity.getXCoord();
        int y = aBaseMetaTileEntity.getYCoord();
        int z = aBaseMetaTileEntity.getZCoord();
        int dim = aBaseMetaTileEntity.getWorld().provider.dimensionId;

        // Save coordinates
        tag.setString("type", "WirelessTorcherino");
        tag.setInteger("machineX", x);
        tag.setInteger("machineY", y);
        tag.setInteger("machineZ", z);
        tag.setInteger("machineDim", dim);

        // Clear old torch data
        tag.removeTag("torchX");
        tag.removeTag("torchY");
        tag.removeTag("torchZ");

        // Get machine name from meta tile
        String machineName = getGTMachineName(aBaseMetaTileEntity);
        tag.setString("machineName", machineName);

        String displayName = StatCollector.translateToLocalFormatted("torcherino.wireless.datastick.name", machineName);
        if (displayName == null || displayName.isEmpty()) {
            displayName = "Wireless Torcherino Link (" + machineName + ")";
        }
        held.setStackDisplayName(displayName);

        aPlayer.addChatMessage(
            new ChatComponentText(
                StatCollector
                    .translateToLocalFormatted("torcherino.wireless.datastick.machine_saved", machineName, x, y, z)));
    }

    private static String getGTMachineName(IGregTechTileEntity bmte) {
        if (bmte.getMetaTileEntity() != null) {
            String name = bmte.getMetaTileEntity()
                .getLocalName();
            if (name != null && !name.isEmpty()) {
                String translated = StatCollector.translateToLocal(name);
                if (!translated.isEmpty() && !translated.equals(name)) {
                    return translated;
                }
                return name;
            }
        }
        return "GT Machine";
    }
}
