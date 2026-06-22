package com.czqwq.Torcherino.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import com.czqwq.Torcherino.tile.TileTorcherinoAccelerated;

public class BlockTorcherino extends BlockTorcherinoBase {

    public BlockTorcherino() {
        super("torcherino", 0.75f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileTorcherinoAccelerated();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileTorcherinoAccelerated) {
                TileEntityGuiFactory.INSTANCE.open(player, (TileTorcherinoAccelerated) tile);
            }
        }
        return true;
    }
}
