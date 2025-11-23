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
import com.czqwq.Torcherino.tile.TileCompressedTorcherino;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCompressedTorcherino extends BlockTorch implements ITileEntityProvider {

    public BlockCompressedTorcherino() {
        super();
        this.setLightLevel(1.0f); // 更高的亮度
        this.isBlockContainer = true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCompressedTorcherino();
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

            if (tile instanceof TileCompressedTorcherino) {
                TileCompressedTorcherino torch = (TileCompressedTorcherino) tile;

                // 如果玩家没有潜行，切换X轴范围
                // 如果玩家潜行，根据计数器切换Z轴范围、Y轴范围或加速效果
                torch.onBlockActivated(player);
            }
        }

        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof TileCompressedTorcherino) {
                TileCompressedTorcherino torch = (TileCompressedTorcherino) tile;
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
        this.blockIcon = registry.registerIcon(Torcherino.MODID + ":compressed_torcherino");
    }
}
