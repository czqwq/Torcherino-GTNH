package com.czqwq.Torcherino.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import com.czqwq.Torcherino.tile.TileCompressedTorcherino;

public class BlockCompressedTorcherino extends BlockTorcherinoBase {

    public BlockCompressedTorcherino() {
        super("compressed_torcherino", 1.0f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCompressedTorcherino();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileCompressedTorcherino) {
                TileEntityGuiFactory.INSTANCE.open(player, (TileCompressedTorcherino) tile);
            }
        }
        return true;
    }
}
