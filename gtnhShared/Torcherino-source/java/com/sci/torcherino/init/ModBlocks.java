package com.sci.torcherino.init;

import com.sci.torcherino.block.BlockTorcherino;
import com.sci.torcherino.tile.TileTorcherino;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author sci4me
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public final class ModBlocks
{
    public static BlockTorcherino torcherino;

    public static void init()
    {
        ModBlocks.torcherino = new BlockTorcherino();

        GameRegistry.registerBlock(ModBlocks.torcherino, ModBlocks.torcherino.getUnlocalizedName());
        GameRegistry.registerTileEntity(TileTorcherino.class, "torcherino_tile");
    }

    private ModBlocks()
    {
    }
}