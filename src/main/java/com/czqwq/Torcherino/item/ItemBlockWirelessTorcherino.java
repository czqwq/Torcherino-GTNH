package com.czqwq.Torcherino.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.czqwq.Torcherino.Config;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockWirelessTorcherino extends ItemBlock {

    public ItemBlockWirelessTorcherino(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extraInformation) {
        list.add("§7§m--------------------§r");
        list.add(StatCollector.translateToLocal("torcherino.tooltip.wireless.line1"));
        list.add(StatCollector.translateToLocal("torcherino.tooltip.wireless.line2"));
        list.add(StatCollector.translateToLocal("torcherino.tooltip.wireless.line3"));
        // Range hint: 范围:XxYxZ
        int rangeX = Config.wirelessTorchRadius * 2 + 1;
        int rangeZ = Config.wirelessTorchRadius * 2 + 1;
        list.add(StatCollector.translateToLocalFormatted("torcherino.tooltip.wireless.range", rangeX, 255, rangeZ));
        list.add("§7§m--------------------§r");
    }
}
