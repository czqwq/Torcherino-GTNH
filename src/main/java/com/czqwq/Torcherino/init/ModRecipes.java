package com.czqwq.Torcherino.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.czqwq.Torcherino.RecipeConfig;
import com.czqwq.Torcherino.block.ModBlocks;
import com.czqwq.Torcherino.item.ModItems;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModRecipes {

    public static void init() {
        // ========== Accelerated Torcherino (basic) ==========
        if (RecipeConfig.acceleratedTorcherino) {
            GameRegistry.addShapedRecipe(
                new ItemStack(ModBlocks.torcherino),
                " Y ",
                "YXY",
                " Y ",
                'X',
                Blocks.torch,
                'Y',
                Items.clock);
        }

        // ========== Compressed Torcherino (9× → 1×, accelerated + classic) ==========
        if (RecipeConfig.compressedTorcherino) {
            GameRegistry.addShapedRecipe(
                new ItemStack(ModBlocks.compressedTorcherino),
                "TTT",
                "TTT",
                "TTT",
                'T',
                ModBlocks.torcherino);

            GameRegistry.addShapedRecipe(
                new ItemStack(ModBlocks.compressedTorcherinoClassic),
                "TTT",
                "TTT",
                "TTT",
                'T',
                ModBlocks.torcherinoClassic);
        }

        // ========== Double-Compressed Torcherino (9× compressed → 1×, accelerated + classic) ==========
        if (RecipeConfig.doubleCompressedTorcherino) {
            GameRegistry.addShapedRecipe(
                new ItemStack(ModBlocks.doubleCompressedTorcherino),
                "TTT",
                "TTT",
                "TTT",
                'T',
                ModBlocks.compressedTorcherino);

            GameRegistry.addShapedRecipe(
                new ItemStack(ModBlocks.doubleCompressedTorcherinoClassic),
                "TTT",
                "TTT",
                "TTT",
                'T',
                ModBlocks.compressedTorcherinoClassic);
        }

        // ========== Classic Conversions (all 3 tiers, both directions) ==========
        if (RecipeConfig.classicTorcherino) {
            // Normal → Classic
            GameRegistry
                .addShapelessRecipe(new ItemStack(ModBlocks.torcherinoClassic), new ItemStack(ModBlocks.torcherino));
            GameRegistry.addShapelessRecipe(
                new ItemStack(ModBlocks.compressedTorcherinoClassic),
                new ItemStack(ModBlocks.compressedTorcherino));
            GameRegistry.addShapelessRecipe(
                new ItemStack(ModBlocks.doubleCompressedTorcherinoClassic),
                new ItemStack(ModBlocks.doubleCompressedTorcherino));

            // Classic → Normal
            GameRegistry
                .addShapelessRecipe(new ItemStack(ModBlocks.torcherino), new ItemStack(ModBlocks.torcherinoClassic));
            GameRegistry.addShapelessRecipe(
                new ItemStack(ModBlocks.compressedTorcherino),
                new ItemStack(ModBlocks.compressedTorcherinoClassic));
            GameRegistry.addShapelessRecipe(
                new ItemStack(ModBlocks.doubleCompressedTorcherino),
                new ItemStack(ModBlocks.doubleCompressedTorcherinoClassic));
        }

        // ========== Time Twister ==========
        if (RecipeConfig.timeTwister) {
            GameRegistry.addShapedRecipe(
                new ItemStack(ModItems.imperfectTimeTwister),
                "ADA",
                "EBE",
                "GTG",
                'A',
                Items.emerald,
                'D',
                Items.clock,
                'E',
                Items.redstone,
                'B',
                Items.glass_bottle,
                'G',
                Items.diamond,
                'T',
                Items.nether_star);
        }

        // ========== Time Vial ==========
        if (RecipeConfig.timeVial) {
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
}
