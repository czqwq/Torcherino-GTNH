package com.czqwq.Torcherino.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

import com.czqwq.Torcherino.tile.TileTorcherinoBase;

/**
 * Draws the working area of a Torcherino while the GUI toggle is enabled: a colored outline plus
 * translucent faces. The box is depth tested like normal world geometry, so blocks occlude it.
 */
public class RenderTorcherinoRange extends TileEntitySpecialRenderer {

    /** Line thickness of the outline. */
    private static final float LINE_WIDTH = 2.0F;
    /** How far the outline is pushed out of the block faces, to avoid z-fighting with them. */
    private static final double OUTLINE_INFLATE = 0.002D;
    /** Same for the translucent faces; kept slightly inside the outline so the edges stay visible. */
    private static final double FACE_INFLATE = 0.001D;
    private static final int OUTLINE_ALPHA = 128;
    private static final int FACE_ALPHA = 76;

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof TileTorcherinoBase)) return;
        TileTorcherinoBase torch = (TileTorcherinoBase) tile;
        if (!torch.isRangePreviewShown()) return;

        AxisAlignedBB area = torch.getWorkAreaBounds();
        // The renderer is called with camera-relative coordinates: translate to the torch and draw
        // the box relative to it.
        double ox = torch.xCoord;
        double oy = torch.yCoord;
        double oz = torch.zCoord;
        int color = getAreaColor(torch);

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glTranslated(x, y, z);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glLineWidth(LINE_WIDTH);

        drawFaces(
            area.minX - ox - FACE_INFLATE,
            area.minY - oy - FACE_INFLATE,
            area.minZ - oz - FACE_INFLATE,
            area.maxX - ox + FACE_INFLATE,
            area.maxY - oy + FACE_INFLATE,
            area.maxZ - oz + FACE_INFLATE,
            color);
        drawOutline(
            area.minX - ox - OUTLINE_INFLATE,
            area.minY - oy - OUTLINE_INFLATE,
            area.minZ - oz - OUTLINE_INFLATE,
            area.maxX - ox + OUTLINE_INFLATE,
            area.maxY - oy + OUTLINE_INFLATE,
            area.maxZ - oz + OUTLINE_INFLATE,
            color);

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    /**
     * Coordinate based color, so the previews of different torches stay distinguishable and keep
     * their color while the player walks around.
     */
    private static int getAreaColor(TileTorcherinoBase torch) {
        int r = Math.abs(torch.xCoord % 255);
        int g = Math.abs(torch.yCoord % 255);
        int b = Math.abs(torch.zCoord % 255);
        return (r << 16) | (g << 8) | b;
    }

    private static void drawFaces(double x0, double y0, double z0, double x1, double y1, double z1, int color) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_I(color, FACE_ALPHA);

        // -Z
        tess.addVertex(x0, y0, z0);
        tess.addVertex(x1, y0, z0);
        tess.addVertex(x1, y1, z0);
        tess.addVertex(x0, y1, z0);

        // +Z
        tess.addVertex(x1, y0, z1);
        tess.addVertex(x0, y0, z1);
        tess.addVertex(x0, y1, z1);
        tess.addVertex(x1, y1, z1);

        // -X
        tess.addVertex(x0, y0, z1);
        tess.addVertex(x0, y0, z0);
        tess.addVertex(x0, y1, z0);
        tess.addVertex(x0, y1, z1);

        // +X
        tess.addVertex(x1, y0, z0);
        tess.addVertex(x1, y0, z1);
        tess.addVertex(x1, y1, z1);
        tess.addVertex(x1, y1, z0);

        // -Y
        tess.addVertex(x0, y0, z1);
        tess.addVertex(x1, y0, z1);
        tess.addVertex(x1, y0, z0);
        tess.addVertex(x0, y0, z0);

        // +Y
        tess.addVertex(x0, y1, z0);
        tess.addVertex(x1, y1, z0);
        tess.addVertex(x1, y1, z1);
        tess.addVertex(x0, y1, z1);

        tess.draw();
    }

    private static void drawOutline(double x0, double y0, double z0, double x1, double y1, double z1, int color) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINES);
        tess.setColorRGBA_I(color, OUTLINE_ALPHA);

        // Bottom rectangle.
        addLine(tess, x0, y0, z0, x1, y0, z0);
        addLine(tess, x1, y0, z0, x1, y0, z1);
        addLine(tess, x1, y0, z1, x0, y0, z1);
        addLine(tess, x0, y0, z1, x0, y0, z0);

        // Top rectangle.
        addLine(tess, x0, y1, z0, x1, y1, z0);
        addLine(tess, x1, y1, z0, x1, y1, z1);
        addLine(tess, x1, y1, z1, x0, y1, z1);
        addLine(tess, x0, y1, z1, x0, y1, z0);

        // Verticals.
        addLine(tess, x0, y0, z0, x0, y1, z0);
        addLine(tess, x1, y0, z0, x1, y1, z0);
        addLine(tess, x1, y0, z1, x1, y1, z1);
        addLine(tess, x0, y0, z1, x0, y1, z1);

        tess.draw();
    }

    private static void addLine(Tessellator tess, double x0, double y0, double z0, double x1, double y1, double z1) {
        tess.addVertex(x0, y0, z0);
        tess.addVertex(x1, y1, z1);
    }
}
