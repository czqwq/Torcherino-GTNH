package com.czqwq.Torcherino.init;

import static gregtech.api.enums.Mods.*;
import static gregtech.api.enums.TierEU.RECIPE_UEV;
import static gregtech.api.enums.TierEU.RECIPE_UHV;
import static gregtech.api.util.GTRecipeBuilder.HOURS;
import static gregtech.api.util.GTRecipeConstants.*;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.MaterialsUEVplus;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;

public class GTRecipes {

    public static void loadRecipes() {
        GTValues.RA.stdBuilder()
            .metadata(RESEARCH_ITEM, GTModHandler.getModItem("Torcherino", "imperfectTimeTwister"))
            .metadata(SCANNING, new Scanning(HOURS / 2, RECIPE_UHV))
            .itemInputs(
                GTOreDictUnificator.get(OrePrefixes.circuit, Materials.UHV, 64),
                GTOreDictUnificator.get(OrePrefixes.circuit, Materials.UEV, 64),
                GTOreDictUnificator.get(OrePrefixes.circuit, Materials.UIV, 64),
                GTModHandler.getModItem("kekztech", "kekztech_lapotronicenergyunit_block", 1, 9),
                GTModHandler.getModItem("gregtech", "gt.blockmachines", 8, 11107),
                GTModHandler.getModItem("minecraft", "clock", 64, 0),
                GTModHandler.getModItem("Torcherino", "timeVial", 1, 0),
                GregtechItemList.KLEIN_BOTTLE.get(1))
            .fluidInputs((Materials.Infinity.getMolten(144 * 1024)), (MaterialsUEVplus.SpaceTime.getMolten(9216)))
            .itemOutputs(GTModHandler.getModItem("Torcherino", "perfectTimeTwister"))
            .eut(RECIPE_UEV)
            .duration(600 * 20)
            .addTo(AssemblyLine);
    }
}
