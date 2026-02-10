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

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.tile.TileDoubleCompressedTorcherino;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockDoubleCompressedTorcherino extends BlockTorch implements ITileEntityProvider {

    public BlockDoubleCompressedTorcherino() {
        super();
        this.setLightLevel(1.0f); // 更高的亮度
        this.isBlockContainer = true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileDoubleCompressedTorcherino();
    }

    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        // 确保基础的火把行为正常工作
        super.onBlockAdded(worldIn, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof TileDoubleCompressedTorcherino) {
                TileEntityGuiFactory.INSTANCE.open(player, (TileDoubleCompressedTorcherino) tile);
            }
        }

        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof TileDoubleCompressedTorcherino) {
                TileDoubleCompressedTorcherino torch = (TileDoubleCompressedTorcherino) tile;
                boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
                torch.setActive(!powered);
            }
        }

        super.onNeighborBlockChange(world, x, y, z, neighborBlock);
    }

    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
        worldIn.removeTileEntity(x, y, z);
    }

    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World worldIn, int x, int y, int z, Random random) {
        // 使用更亮的火把粒子效果
        Blocks.torch.randomDisplayTick(worldIn, x, y, z, random);
    }

    /**
     * Registers block icons. This is called from the main thread while the dedicated server is not running, and from
     * a worker thread when the dedicated server is running.
     * 
     * @param registry The icon registry
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister registry) {
        this.blockIcon = registry.registerIcon(Torcherino.MODID + ":double_compressed_torcherino");
    }
}
