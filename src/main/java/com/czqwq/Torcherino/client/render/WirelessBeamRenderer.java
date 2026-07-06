package com.czqwq.Torcherino.client.render;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import com.czqwq.Torcherino.tile.TileWirelessTorcherinoBase;
import com.czqwq.Torcherino.util.BoundMachineEntry;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Renders beam lines from a wireless torcherino to its bound machines.
 * Follows AE2's {@code NetworkVisualiserRender.doRenderWirelessPath} pattern:
 * checks {@code mc.objectMouseOver} each frame and renders lines when the
 * player looks at a {@link TileWirelessTorcherinoBase}.
 * <p>
 * No external trigger needed — completely independent of WAILA/OmniOcular.
 */
public class WirelessBeamRenderer {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        // Check what the player is looking at
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        TileEntity te = mc.theWorld.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
        if (!(te instanceof TileWirelessTorcherinoBase)) return;

        TileWirelessTorcherinoBase torch = (TileWirelessTorcherinoBase) te;
        List<BoundMachineEntry> bound = torch.getBoundMachines();
        if (bound.isEmpty()) return;

        EntityPlayerSP p = mc.thePlayer;
        double viewX = p.lastTickPosX + (p.posX - p.lastTickPosX) * event.partialTicks;
        double viewY = p.lastTickPosY + (p.posY - p.lastTickPosY) * event.partialTicks;
        double viewZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * event.partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-viewX, -viewY, -viewZ);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(3.0f);

        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINES);

        // Torch center
        double tx = torch.xCoord + 0.5d;
        double ty = torch.yCoord + 0.5d;
        double tz = torch.zCoord + 0.5d;

        for (BoundMachineEntry entry : bound) {
            // Skip if different dimension (shouldn't happen, but safety check)
            if (entry.dim != mc.theWorld.provider.dimensionId) continue;

            double dx = entry.x + 0.5d - tx;
            double dy = entry.y + 0.5d - ty;
            double dz = entry.z + 0.5d - tz;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            float alpha = (float) Math.max(0.3, 1.0 - dist / 32.0);
            tess.setColorRGBA_F(0.2f, 0.6f, 1.0f, alpha);
            tess.addVertex(tx, ty, tz);
            tess.addVertex(entry.x + 0.5d, entry.y + 0.5d, entry.z + 0.5d);
        }

        tess.draw();

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}
