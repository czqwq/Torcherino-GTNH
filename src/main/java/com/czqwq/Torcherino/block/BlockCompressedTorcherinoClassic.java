package com.czqwq.Torcherino.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.tile.TileCompressedTorcherinoClassic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Compressed Classic Torcherino block
 */
public class BlockCompressedTorcherinoClassic extends BlockTorch implements ITileEntityProvider {

    public BlockCompressedTorcherinoClassic() {
        super();
        this.setLightLevel(0.75f);
        this.isBlockContainer = true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCompressedTorcherinoClassic();
    }

    @Override
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        super.onBlockAdded(worldIn, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof TileCompressedTorcherinoClassic) {
                TileCompressedTorcherinoClassic torch = (TileCompressedTorcherinoClassic) tile;
                torch.changeMode(player.isSneaking(), player);
            }
        }

        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof TileCompressedTorcherinoClassic) {
                TileCompressedTorcherinoClassic torch = (TileCompressedTorcherinoClassic) tile;
                boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
                torch.setActive(!powered);
            }
        }

        super.onNeighborBlockChange(world, x, y, z, neighborBlock);
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
        worldIn.removeTileEntity(x, y, z);
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World worldIn, int x, int y, int z, Random random) {
        Blocks.torch.randomDisplayTick(worldIn, x, y, z, random);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister registry) {
        // Use the same texture as compressed Torcherino
        this.blockIcon = registry.registerIcon(Torcherino.MODID + ":compressed_torcherino");
    }
}
