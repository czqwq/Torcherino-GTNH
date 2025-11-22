package com.czqwq.Torcherino.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import com.czqwq.Torcherino.tile.TileTorcherinoAccelerated;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block torcherino;

    public static void init() {
        torcherino = new BlockTorcherino(Material.circuits).setBlockName("torcherino");

        GameRegistry.registerBlock(torcherino, "torcherino");
        // 使用改进的TileTorcherinoAccelerated
        GameRegistry.registerTileEntity(TileTorcherinoAccelerated.class, "tile_torcherino");
    }
}
