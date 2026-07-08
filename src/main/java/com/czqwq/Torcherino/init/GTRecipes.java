package com.czqwq.Torcherino.init;

import static com.czqwq.Torcherino.api.TorcherinoItems.*;
import static gregtech.api.enums.TierEU.RECIPE_LV;
import static gregtech.api.enums.TierEU.RECIPE_MV;
import static gregtech.api.enums.TierEU.RECIPE_UEV;
import static gregtech.api.enums.TierEU.RECIPE_UHV;
import static gregtech.api.enums.TierEU.RECIPE_ZPM;
import static gregtech.api.recipe.RecipeMaps.assemblerRecipes;
import static gregtech.api.recipe.RecipeMaps.compressorRecipes;
import static gregtech.api.recipe.RecipeMaps.neutroniumCompressorRecipes;
import static gregtech.api.util.GTRecipeBuilder.HOURS;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeConstants.*;
import static gregtech.api.util.GTUtility.copyAmountUnsafe;

import com.czqwq.Torcherino.RecipeConfig;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;

public class GTRecipes {

    public static void loadRecipes() {
        // ========== Perfect Time Twister (Assembly Line) ==========
        if (RecipeConfig.perfectTimeTwister) {
            GTValues.RA.stdBuilder()
                .metadata(RESEARCH_ITEM, IMPERFECT_TIME_TWISTER.get())
                .metadata(SCANNING, new Scanning(HOURS / 2, RECIPE_UHV))
                .itemInputs(
                    new Object[] { OrePrefixes.circuit.get(Materials.UHV), 64 },
                    new Object[] { OrePrefixes.circuit.get(Materials.UEV), 64 },
                    new Object[] { OrePrefixes.circuit.get(Materials.UIV), 64 },
                    GTModHandler.getModItem("kekztech", "kekztech_lapotronicenergyunit_block", 1, 8),
                    GTModHandler.getModItem("gregtech", "gt.blockmachines", 8, 11107),
                    GTModHandler.getModItem("minecraft", "clock", 64, 0),
                    TIME_VIAL.get(),
                    GregtechItemList.KLEIN_BOTTLE.get(1))
                .fluidInputs((Materials.Infinity.getMolten(144 * 1024)), (Materials.SpaceTime.getMolten(9216)))
                .itemOutputs(PERFECT_TIME_TWISTER.get())
                .eut(RECIPE_UEV)
                .duration(600 * 20)
                .addTo(AssemblyLine);
        }

        // ========== Torcherino Assembler Recipe ==========
        if (RecipeConfig.torcherinoAssembler) {
            GTValues.RA.stdBuilder()
                .itemInputs(
                    GTModHandler.getModItem("minecraft", "clock", 4, 0),
                    GTModHandler.getModItem("minecraft", "torch", 1, 0))
                .circuit(4)
                .itemOutputs(TORCHERINO.get())
                .duration(10 * SECONDS)
                .eut(RECIPE_MV)
                .addTo(assemblerRecipes);
        }

        // ========== Wireless Torcherino Compressor Recipes (all 3 tiers) ==========
        if (RecipeConfig.wirelessTorcherino) {
            // 256 regular torcherinos → 1 wireless torcherino
            GTValues.RA.stdBuilder()
                .itemInputsUnsafe(copyAmountUnsafe(256, TORCHERINO.get()))
                .itemOutputs(WIRELESS_TORCHERINO.get())
                .metadata(COMPRESSION_TIER, 1)
                .duration(30 * SECONDS)
                .eut(RECIPE_ZPM)
                .addTo(compressorRecipes);

            // 9 wireless torcherinos → 1 compressed wireless torcherino
            GTValues.RA.stdBuilder()
                .itemInputs(WIRELESS_TORCHERINO.get(9))
                .itemOutputs(COMPRESSED_WIRELESS_TORCHERINO.get())
                .metadata(COMPRESSION_TIER, 1)
                .duration(30 * SECONDS)
                .eut(RECIPE_UHV)
                .addTo(compressorRecipes);

            // 9 compressed wireless torcherinos → 1 double compressed wireless torcherino
            GTValues.RA.stdBuilder()
                .itemInputs(COMPRESSED_WIRELESS_TORCHERINO.get(9))
                .itemOutputs(DOUBLE_COMPRESSED_WIRELESS_TORCHERINO.get())
                .metadata(COMPRESSION_TIER, 1)
                .duration(30 * SECONDS)
                .eut(RECIPE_UEV)
                .addTo(compressorRecipes);
        }

        // ========== Neutronium Compressor: Basic → Compressed ==========
        if (RecipeConfig.gtCompressedTorcherino) {
            GTValues.RA.stdBuilder()
                .itemInputsUnsafe(TORCHERINO.get(9))
                .itemOutputs(COMPRESSED_TORCHERINO.get())
                .duration(10 * SECONDS)
                .eut(RECIPE_MV)
                .addTo(neutroniumCompressorRecipes);
        }

        // ========== Neutronium Compressor: Compressed → Double-Compressed ==========
        if (RecipeConfig.gtDoubleCompressedTorcherino) {
            GTValues.RA.stdBuilder()
                .itemInputsUnsafe(COMPRESSED_TORCHERINO.get(9))
                .itemOutputs(DOUBLE_COMPRESSED_TORCHERINO.get())
                .duration(10 * SECONDS)
                .eut(RECIPE_MV)
                .addTo(neutroniumCompressorRecipes);
        }

        // ========== GT Assembler Classic Conversions (all 3 tiers, both directions) ==========
        if (RecipeConfig.gtClassicConversions) {
            // NC circuit 1: accelerated ↔ classic for each tier

            // Regular: torcherino → classic
            GTValues.RA.stdBuilder()
                .itemInputs(TORCHERINO.get())
                .circuit(1)
                .itemOutputs(TORCHERINO_CLASSIC.get())
                .duration(SECONDS)
                .eut(RECIPE_LV)
                .addTo(assemblerRecipes);

            // Regular: classic → torcherino
            GTValues.RA.stdBuilder()
                .itemInputs(TORCHERINO_CLASSIC.get())
                .circuit(1)
                .itemOutputs(TORCHERINO.get())
                .duration(SECONDS)
                .eut(RECIPE_LV)
                .addTo(assemblerRecipes);

            // Compressed: torcherino → classic
            GTValues.RA.stdBuilder()
                .itemInputs(COMPRESSED_TORCHERINO.get())
                .circuit(1)
                .itemOutputs(COMPRESSED_TORCHERINO_CLASSIC.get())
                .duration(SECONDS)
                .eut(RECIPE_LV)
                .addTo(assemblerRecipes);

            // Compressed: classic → torcherino
            GTValues.RA.stdBuilder()
                .itemInputs(COMPRESSED_TORCHERINO_CLASSIC.get())
                .circuit(1)
                .itemOutputs(COMPRESSED_TORCHERINO.get())
                .duration(SECONDS)
                .eut(RECIPE_LV)
                .addTo(assemblerRecipes);

            // Double: torcherino → classic
            GTValues.RA.stdBuilder()
                .itemInputs(DOUBLE_COMPRESSED_TORCHERINO.get())
                .circuit(1)
                .itemOutputs(DOUBLE_COMPRESSED_TORCHERINO_CLASSIC.get())
                .duration(SECONDS)
                .eut(RECIPE_LV)
                .addTo(assemblerRecipes);

            // Double: classic → torcherino
            GTValues.RA.stdBuilder()
                .itemInputs(DOUBLE_COMPRESSED_TORCHERINO_CLASSIC.get())
                .circuit(1)
                .itemOutputs(DOUBLE_COMPRESSED_TORCHERINO.get())
                .duration(SECONDS)
                .eut(RECIPE_LV)
                .addTo(assemblerRecipes);
        }
    }
}
