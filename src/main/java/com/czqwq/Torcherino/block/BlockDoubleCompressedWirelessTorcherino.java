package com.czqwq.Torcherino.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import com.czqwq.Torcherino.tile.TileDoubleCompressedWirelessTorcherino;
import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;

public class BlockDoubleCompressedWirelessTorcherino extends BlockTorcherinoBase {

    public BlockDoubleCompressedWirelessTorcherino() {
        super("double_compressed_torcherino", 1.0f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileDoubleCompressedWirelessTorcherino();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileWirelessTorcherinoBase)) return true;
        TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) tile;

        if (BlockWirelessTorcherino.handleDataStickInteraction(world, x, y, z, player, torch)) return true;

        TileEntityGuiFactory.INSTANCE.open(player, torch);
        return true;
    }
}
