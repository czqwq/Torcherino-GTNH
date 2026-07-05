package com.czqwq.Torcherino.block;

import net.minecraft.block.Block;

import com.czqwq.Torcherino.Config;
import com.czqwq.Torcherino.item.ItemBlockWirelessTorcherino;
import com.czqwq.Torcherino.tile.TileCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileCompressedTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileCompressedWirelessTorcherino;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherino;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileDoubleCompressedWirelessTorcherino;
import com.czqwq.Torcherino.tile.TileTorcherinoAccelerated;
import com.czqwq.Torcherino.tile.TileTorcherinoClassic;
import com.czqwq.Torcherino.tile.TileWirelessTorcherino;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block torcherino;
    public static Block compressedTorcherino;
    public static Block doubleCompressedTorcherino;
    public static Block torcherinoClassic;
    public static Block compressedTorcherinoClassic;
    public static Block doubleCompressedTorcherinoClassic;
    public static Block wirelessTorcherino;
    public static Block compressedWirelessTorcherino;
    public static Block doubleCompressedWirelessTorcherino;

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

        // Wireless torcherino blocks & tiles
        if (Config.enableFlashTorcherino) {
            wirelessTorcherino = new BlockWirelessTorcherino().setBlockName("wireless_torcherino");
            compressedWirelessTorcherino = new BlockCompressedWirelessTorcherino()
                .setBlockName("compressed_wireless_torcherino");
            doubleCompressedWirelessTorcherino = new BlockDoubleCompressedWirelessTorcherino()
                .setBlockName("double_compressed_wireless_torcherino");

            GameRegistry.registerBlock(wirelessTorcherino, ItemBlockWirelessTorcherino.class, "wireless_torcherino");
            GameRegistry.registerBlock(
                compressedWirelessTorcherino,
                ItemBlockWirelessTorcherino.class,
                "compressed_wireless_torcherino");
            GameRegistry.registerBlock(
                doubleCompressedWirelessTorcherino,
                ItemBlockWirelessTorcherino.class,
                "double_compressed_wireless_torcherino");

            GameRegistry.registerTileEntity(TileWirelessTorcherino.class, "tile_wireless_torcherino");
            GameRegistry
                .registerTileEntity(TileCompressedWirelessTorcherino.class, "tile_compressed_wireless_torcherino");
            GameRegistry.registerTileEntity(
                TileDoubleCompressedWirelessTorcherino.class,
                "tile_double_compressed_wireless_torcherino");
        }
    }
}
