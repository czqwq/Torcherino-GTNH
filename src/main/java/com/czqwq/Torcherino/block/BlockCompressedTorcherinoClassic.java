package com.czqwq.Torcherino.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.czqwq.Torcherino.tile.TileCompressedTorcherinoClassic;

/**
 * Compressed Classic Torcherino block - chat-based mode cycling.
 */
public class BlockCompressedTorcherinoClassic extends BlockTorcherinoBase {

    public BlockCompressedTorcherinoClassic() {
        super("compressed_torcherino", 0.75f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCompressedTorcherinoClassic();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileCompressedTorcherinoClassic) {
                ((TileCompressedTorcherinoClassic) tile).changeMode(player.isSneaking(), player);
            }
        }
        return true;
    }
}
