package com.czqwq.Torcherino.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.tile.TileTorcherinoAccelerated;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTorcherino extends BlockContainer {

    public BlockTorcherino(Material material) {
        super(material);
        this.setLightLevel(0.75f);
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
                TileTorcherinoAccelerated torch = (TileTorcherinoAccelerated) tile;

                // 切换激活状态
                torch.setActive(!torch.getActive());
                player.addChatComponentMessage(
                    new ChatComponentText("Torcherino is now " + (torch.getActive() ? "active" : "inactive")));
            }
        }

        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof TileTorcherinoAccelerated) {
                TileTorcherinoAccelerated torch = (TileTorcherinoAccelerated) tile;
                boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
                torch.setActive(!powered);
            }
        }

        super.onNeighborBlockChange(world, x, y, z, neighborBlock);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister registry) {
        this.blockIcon = registry.registerIcon(Torcherino.MODID + ":torcherino");
    }
}
