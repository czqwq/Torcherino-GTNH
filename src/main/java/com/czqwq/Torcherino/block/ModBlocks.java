package com.czqwq.Torcherino.block;

import net.minecraft.block.Block;

import com.czqwq.Torcherino.tile.TileCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileTorcherinoAccelerated;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block torcherino;
    public static Block compressedTorcherino;

    public static void init() {
        torcherino = new BlockTorcherino().setBlockName("torcherino");
        compressedTorcherino = new BlockCompressedTorcherino().setBlockName("compressed_torcherino");

        GameRegistry.registerBlock(torcherino, "torcherino");
        GameRegistry.registerBlock(compressedTorcherino, "compressed_torcherino");
        // 使用改进的TileTorcherinoAccelerated
        GameRegistry.registerTileEntity(TileTorcherinoAccelerated.class, "tile_torcherino");
        GameRegistry.registerTileEntity(TileCompressedTorcherino.class, "tile_compressed_torcherino");
    }
}
