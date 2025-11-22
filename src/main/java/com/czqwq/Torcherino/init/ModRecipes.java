package com.czqwq.Torcherino.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.czqwq.Torcherino.block.ModBlocks;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModRecipes {

    public static void init() {
        GameRegistry.addShapedRecipe(
            new ItemStack(ModBlocks.torcherino),
            "XCX",
            "CTC",
            "XCX",
            'C',
            Items.clock,
            'T',
            Blocks.torch,
            'X',
            Items.redstone);
    }
}
