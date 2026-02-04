package com.czqwq.Torcherino.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.czqwq.Torcherino.entity.EntityTimeAccelerator;

/**
 * Renderer for EntityTimeAccelerator
 * Displays floating visual indicator showing the acceleration rate
 */
public class RenderTimeAccelerator extends Render {

    private static final ResourceLocation[] TEXTURE_ARRAY = new ResourceLocation[6];
    private static final double ROTATE_SPEED = 7.12d;
    private static final double RADIUS = 0.34d;
    private static final double OFFSET = 0.51d;

    static {
        final String pathURL = "torcherino:textures/entity/Circle/";
        for (int i = 0; i < TEXTURE_ARRAY.length; i++) {
            TEXTURE_ARRAY[i] = new ResourceLocation(pathURL + "time_" + i + ".png");
        }
    }

    private void doRender(EntityTimeAccelerator entityTimeAccelerator, double x, double y, double z) {

        double angle = (ROTATE_SPEED * entityTimeAccelerator.worldObj.getTotalWorldTime()) % 360;
        int i = (int) (Math.log(entityTimeAccelerator.getTimeRateForRender()) / Math.log(2)) - 2;

        // Security check
        if (i >= TEXTURE_ARRAY.length || i < 0) i = 0;

        this.bindTexture(TEXTURE_ARRAY[i]);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().entityRenderer.disableLightmap(1);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        Tessellator tessellator = Tessellator.instance;

        drawAllSides(tessellator, x, y, z, angle);

        Minecraft.getMinecraft().entityRenderer.enableLightmap(1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    /**
     * Draw the acceleration indicator on all 6 sides
     */
    private static void drawAllSides(Tessellator tessellator, double x, double y, double z, double angle) {

        // Top face
        GL11.glPushMatrix();
        tessellator.startDrawingQuads();
        GL11.glTranslated(x, y + OFFSET, z);
        GL11.glRotated(angle, 0.0d, -1.0d, 0.0d);
        tessellator.addVertexWithUV(RADIUS, 0, RADIUS, 0.0d, 0.0d);
        tessellator.addVertexWithUV(-RADIUS, 0, RADIUS, 1.0d, 0.0d);
        tessellator.addVertexWithUV(-RADIUS, 0, -RADIUS, 1.0d, 1.0d);
        tessellator.addVertexWithUV(RADIUS, 0, -RADIUS, 0.0d, 1.0d);
        tessellator.draw();
        GL11.glPopMatrix();

        // Bottom face
        GL11.glPushMatrix();
        tessellator.startDrawingQuads();
        GL11.glTranslated(x, y - OFFSET, z);
        GL11.glRotated(angle, 0.0d, 1.0d, 0.0d);
        tessellator.addVertexWithUV(RADIUS, 0, -RADIUS, 0.0d, 0.0d);
        tessellator.addVertexWithUV(-RADIUS, 0, -RADIUS, 1.0d, 0.0d);
        tessellator.addVertexWithUV(-RADIUS, 0, RADIUS, 1.0d, 1.0d);
        tessellator.addVertexWithUV(RADIUS, 0, RADIUS, 0.0d, 1.0d);
        tessellator.draw();
        GL11.glPopMatrix();

        // East face
        GL11.glPushMatrix();
        tessellator.startDrawingQuads();
        GL11.glTranslated(x + OFFSET, y, z);
        GL11.glRotated(angle, -1.0d, 0.0d, 0.0d);
        tessellator.addVertexWithUV(0, RADIUS, RADIUS, 0.0d, 0.0d);
        tessellator.addVertexWithUV(0, RADIUS, -RADIUS, 1.0d, 0.0d);
        tessellator.addVertexWithUV(0, -RADIUS, -RADIUS, 1.0d, 1.0d);
        tessellator.addVertexWithUV(0, -RADIUS, RADIUS, 0.0d, 1.0d);
        tessellator.draw();
        GL11.glPopMatrix();

        // West face
        GL11.glPushMatrix();
        tessellator.startDrawingQuads();
        GL11.glTranslated(x - OFFSET, y, z);
        GL11.glRotated(angle, 1.0d, 0.0d, 0.0d);
        tessellator.addVertexWithUV(0, RADIUS, -RADIUS, 0.0d, 0.0d);
        tessellator.addVertexWithUV(0, RADIUS, RADIUS, 1.0d, 0.0d);
        tessellator.addVertexWithUV(0, -RADIUS, RADIUS, 1.0d, 1.0d);
        tessellator.addVertexWithUV(0, -RADIUS, -RADIUS, 0.0d, 1.0d);
        tessellator.draw();
        GL11.glPopMatrix();

        // South face
        GL11.glPushMatrix();
        tessellator.startDrawingQuads();
        GL11.glTranslated(x, y, z + OFFSET);
        GL11.glRotated(angle, 0.0d, 0.0d, -1.0d);
        tessellator.addVertexWithUV(-RADIUS, RADIUS, 0, 0.0d, 0.0d);
        tessellator.addVertexWithUV(RADIUS, RADIUS, 0, 1.0d, 0.0d);
        tessellator.addVertexWithUV(RADIUS, -RADIUS, 0, 1.0d, 1.0d);
        tessellator.addVertexWithUV(-RADIUS, -RADIUS, 0, 0.0d, 1.0d);
        tessellator.draw();
        GL11.glPopMatrix();

        // North face
        GL11.glPushMatrix();
        tessellator.startDrawingQuads();
        GL11.glTranslated(x, y, z - OFFSET);
        GL11.glRotated(angle, 0.0d, 0.0d, 1.0d);
        tessellator.addVertexWithUV(RADIUS, RADIUS, 0, 0.0d, 0.0d);
        tessellator.addVertexWithUV(-RADIUS, RADIUS, 0, 1.0d, 0.0d);
        tessellator.addVertexWithUV(-RADIUS, -RADIUS, 0, 1.0d, 1.0d);
        tessellator.addVertexWithUV(RADIUS, -RADIUS, 0, 0.0d, 1.0d);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        doRender((EntityTimeAccelerator) entity, x, y, z);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}
