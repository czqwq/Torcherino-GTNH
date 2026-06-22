package com.czqwq.Torcherino.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.api.interfaces.ITorcherinoTile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Abstract base class for all Torcherino blocks.
 * Provides common behavior: neighbor-change (redstone), block break cleanup,
 * particle display, and icon registration.
 * <p>
 * Subclasses only need to provide {@link #createNewTileEntity} and {@link #onBlockActivated}.
 */
public abstract class BlockTorcherinoBase extends BlockTorch implements ITileEntityProvider {

    protected final String iconName;

    protected BlockTorcherinoBase(String iconName, float lightLevel) {
        super();
        this.iconName = iconName;
        this.setLightLevel(lightLevel);
        this.isBlockContainer = true;
    }

    @Override
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        super.onBlockAdded(worldIn, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof ITorcherinoTile) {
                boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
                ((ITorcherinoTile) tile).setActive(!powered);
            }
        }
        super.onNeighborBlockChange(world, x, y, z, neighborBlock);
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
        worldIn.removeTileEntity(x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World worldIn, int x, int y, int z, Random random) {
        Blocks.torch.randomDisplayTick(worldIn, x, y, z, random);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister registry) {
        this.blockIcon = registry.registerIcon(Torcherino.MODID + ":" + iconName);
    }
}
