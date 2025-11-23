package com.czqwq.Torcherino.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.czqwq.Torcherino.block.ModBlocks;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModRecipes {

    public static void init() {
        // 原始火把合成配方
        // 第一列：aya (a为空气, y为钟)
        // 第二列：yxy (y为钟, x为火把)
        // 第三列：aya (a为空气, y为钟)
        GameRegistry.addShapedRecipe(
            new ItemStack(ModBlocks.torcherino),
            " Y ",
            "YXY",
            " Y ",
            'X',
            Blocks.torch,
            'Y',
            Items.clock);

        // 压缩火把合成配方
        GameRegistry.addShapedRecipe(
            new ItemStack(ModBlocks.compressedTorcherino),
            "TTT",
            "TTT",
            "TTT",
            'T',
            ModBlocks.torcherino);

        // 二重压缩火把合成配方
        GameRegistry.addShapedRecipe(
            new ItemStack(ModBlocks.doubleCompressedTorcherino),
            "TTT",
            "TTT",
            "TTT",
            'T',
            ModBlocks.compressedTorcherino);
    }
}
