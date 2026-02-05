package com.czqwq.Torcherino.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.czqwq.Torcherino.block.ModBlocks;
import com.czqwq.Torcherino.item.ModItems;

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

        // Shapeless recipes to convert normal Torcherino to Classic
        GameRegistry
            .addShapelessRecipe(new ItemStack(ModBlocks.torcherinoClassic), new ItemStack(ModBlocks.torcherino));

        GameRegistry.addShapelessRecipe(
            new ItemStack(ModBlocks.compressedTorcherinoClassic),
            new ItemStack(ModBlocks.compressedTorcherino));

        GameRegistry.addShapelessRecipe(
            new ItemStack(ModBlocks.doubleCompressedTorcherinoClassic),
            new ItemStack(ModBlocks.doubleCompressedTorcherino));

        // Shapeless recipes to convert Classic back to normal Torcherino
        GameRegistry
            .addShapelessRecipe(new ItemStack(ModBlocks.torcherino), new ItemStack(ModBlocks.torcherinoClassic));

        GameRegistry.addShapelessRecipe(
            new ItemStack(ModBlocks.compressedTorcherino),
            new ItemStack(ModBlocks.compressedTorcherinoClassic));

        GameRegistry.addShapelessRecipe(
            new ItemStack(ModBlocks.doubleCompressedTorcherino),
            new ItemStack(ModBlocks.doubleCompressedTorcherinoClassic));

        // 时间瓶配方
        GameRegistry.addShapedRecipe(
            new ItemStack(ModItems.timeVial),
            "ADA",
            "ETE",
            "GGG",
            'E',
            new ItemStack(Items.dye, 1, 4),
            'B',
            Items.glass_bottle,
            'G',
            Items.gold_ingot,
            'A',
            Items.diamond,
            'D',
            ModBlocks.torcherino,
            'T',
            Items.clock);
    }
}
