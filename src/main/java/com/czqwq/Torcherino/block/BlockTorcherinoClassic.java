package com.czqwq.Torcherino.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.czqwq.Torcherino.tile.TileTorcherinoClassic;

/**
 * Classic Torcherino block - uses chat-based mode cycling (no GUI).
 */
public class BlockTorcherinoClassic extends BlockTorcherinoBase {

    public BlockTorcherinoClassic() {
        super("torcherino", 0.75f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileTorcherinoClassic();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileTorcherinoClassic) {
                ((TileTorcherinoClassic) tile).changeMode(player.isSneaking(), player);
            }
        }
        return true;
    }
}
