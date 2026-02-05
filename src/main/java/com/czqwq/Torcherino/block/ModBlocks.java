package com.czqwq.Torcherino.block;

import net.minecraft.block.Block;

import com.czqwq.Torcherino.tile.TileCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileCompressedTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileTorcherinoAccelerated;
import com.czqwq.Torcherino.tile.TileTorcherinoClassic;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block torcherino;
    public static Block compressedTorcherino;
    public static Block doubleCompressedTorcherino;
    public static Block torcherinoClassic;
    public static Block compressedTorcherinoClassic;
    public static Block doubleCompressedTorcherinoClassic;

    public static void init() {
        torcherino = new BlockTorcherino().setBlockName("torcherino");
        compressedTorcherino = new BlockCompressedTorcherino().setBlockName("compressed_torcherino");
        doubleCompressedTorcherino = new BlockDoubleCompressedTorcherino().setBlockName("double_compressed_torcherino");
        torcherinoClassic = new BlockTorcherinoClassic().setBlockName("torcherino_classic");
        compressedTorcherinoClassic = new BlockCompressedTorcherinoClassic()
            .setBlockName("compressed_torcherino_classic");
        doubleCompressedTorcherinoClassic = new BlockDoubleCompressedTorcherinoClassic()
            .setBlockName("double_compressed_torcherino_classic");

        GameRegistry.registerBlock(torcherino, "torcherino");
        GameRegistry.registerBlock(compressedTorcherino, "compressed_torcherino");
        GameRegistry.registerBlock(doubleCompressedTorcherino, "double_compressed_torcherino");
        GameRegistry.registerBlock(torcherinoClassic, "torcherino_classic");
        GameRegistry.registerBlock(compressedTorcherinoClassic, "compressed_torcherino_classic");
        GameRegistry.registerBlock(doubleCompressedTorcherinoClassic, "double_compressed_torcherino_classic");

        // 使用改进的TileTorcherinoAccelerated
        GameRegistry.registerTileEntity(TileTorcherinoAccelerated.class, "tile_torcherino");
        GameRegistry.registerTileEntity(TileCompressedTorcherino.class, "tile_compressed_torcherino");
        GameRegistry.registerTileEntity(TileDoubleCompressedTorcherino.class, "tile_double_compressed_torcherino");
        GameRegistry.registerTileEntity(TileTorcherinoClassic.class, "tile_torcherino_classic");
        GameRegistry.registerTileEntity(TileCompressedTorcherinoClassic.class, "tile_compressed_torcherino_classic");
        GameRegistry.registerTileEntity(
            TileDoubleCompressedTorcherinoClassic.class,
            "tile_double_compressed_torcherino_classic");
    }
}
