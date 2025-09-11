package dev.xylonity.tooltipoverhaul.client.style.inner;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipFrame;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.awt.Point;

public class GlintInnerOverlay implements ITooltipFrame {

    private final int color1;
    private final int color2;
    private final int color3;
    private final int timesSegmentedColor1;
    private final int timesSegmentedColor2;

    private static final int MIN_SEGMENTS = 2;
    private static final int MAX_SEGMENTS = 24;

    public GlintInnerOverlay(int color1, int color2, int color3, int timesSegmentedColor1, int timesSegmentedColor2) {
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.timesSegmentedColor1 = timesSegmentedColor1;
        this.timesSegmentedColor2 = timesSegmentedColor2;
    }

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        glint(ctx.graphics(), (int) pos.x - 3, (int) pos.y - 3, size.x + 6, size.y + 6);
    }

    private void glint(GuiGraphics graphics, int x, int y, int width, int height) {
        if (width <= 1 || height <= 1) {
            return;
        }

        int lightRange  = Math.max(1, width / Math.max(1, timesSegmentedColor1));
        int mediumRange = Math.max(1, width / Math.max(1, timesSegmentedColor2));

        int segmentWidth = clamp(width / 12);
        int segmentHeight = clamp(height / 12);

        int per = 2 * (width + height) - 4;
        // Central index moved to the top left section of the tooltip
        int center = width / 4;

        // TOP
        drawHorizontalLine(graphics, x, y, width, 0, segmentWidth, per, center, lightRange, mediumRange, false);

        // RIGHT
        drawVerticalLine(graphics, x + width - 1, y + 1, height - 2, width, segmentHeight, per, center, lightRange, mediumRange, false);

        // BOTTOM
        drawHorizontalLine(graphics, x, y + height - 1, width, width + height - 1, segmentWidth, per, center, lightRange, mediumRange, true);

        // LEFT
        drawVerticalLine(graphics, x, y + 1, height - 2, 2 * width + height - 2, segmentHeight, per, center, lightRange, mediumRange, true);
    }

    private void drawHorizontalLine(GuiGraphics g, int x, int y, int length, int startIdx, int segments, int per, int center, int lightRange, int mediumRange, boolean reverse) {
        if (length <= 0) return;

        int segmentLength = Math.max(1, length / segments);
        int drawn = 0;
        int px = x;

        while (drawn < length) {
            int run = Math.min(segmentLength, length - drawn);

            int colorLeft = colorAt(startIdx + (reverse ? (length - 1 - drawn) : drawn), per, center, lightRange, mediumRange);
            int colorRight = colorAt(startIdx + (reverse ? (length - drawn - run) : (drawn + run - 1)), per, center, lightRange, mediumRange);

            makeHorizontalGradient(g, px, y, px + run, y + 1, colorLeft, colorRight);

            px += run;
            drawn += run;
        }

    }

    private void drawVerticalLine(GuiGraphics graphics, int x, int y, int length, int startIdx, int segments, int per, int center, int lightRange, int mediumRange, boolean reverse) {
        if (length <= 0) return;

        int segmentLength = Math.max(1, length / segments);
        int drawn = 0;
        int py = y;

        while (drawn < length) {
            int run = Math.min(segmentLength, length - drawn);

            int colorTop = colorAt(startIdx + (reverse ? (length - 1 - drawn) : drawn), per, center, lightRange, mediumRange);
            int colorBottom = colorAt(startIdx + (reverse ? (length - drawn - run) : (drawn + run - 1)), per, center, lightRange, mediumRange);

            makeVerticalGradient(graphics, x, py, x + 1, py + run, colorTop, colorBottom);

            py += run;
            drawn += run;
        }

    }

    private static void makeHorizontalGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int colorLeft, int colorRight) {
        int alphaLeft = (colorLeft >>> 24) & 0xFF;
        int redLeft = (colorLeft >>> 16) & 0xFF;
        int greenLeft = (colorLeft >>> 8) & 0xFF;
        int blueLeft = colorLeft & 0xFF;
        int alphaRight = (colorRight >>> 24) & 0xFF;
        int redRight = (colorRight >>> 16) & 0xFF;
        int greenRight = (colorRight >>> 8) & 0xFF;
        int blueRight = colorRight & 0xFF;

        PoseStack pose = graphics.pose();
        Matrix4f matrix = pose.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        buf.vertex(matrix, x1, y2, 0).color(redLeft, greenLeft, blueLeft, alphaLeft).endVertex(); // bl
        buf.vertex(matrix, x2, y2, 0).color(redRight, greenRight, blueRight, alphaRight).endVertex(); // br
        buf.vertex(matrix, x2, y1, 0).color(redRight, greenRight, blueRight, alphaRight).endVertex(); // tr
        buf.vertex(matrix, x1, y1, 0).color(redLeft, greenLeft, blueLeft, alphaLeft).endVertex(); // tl

        BufferUploader.drawWithShader(buf.end());
    }

    private static void makeVerticalGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        int alphaTop = (colorTop >>> 24) & 0xFF;
        int redTop = (colorTop >>> 16) & 0xFF;
        int greenTop = (colorTop >>> 8) & 0xFF;
        int blueTop = colorTop & 0xFF;
        int alphaBottom = (colorBottom >>> 24) & 0xFF;
        int redBottom = (colorBottom >>> 16) & 0xFF;
        int greenBottom = (colorBottom >>> 8) & 0xFF;
        int blueBottom = colorBottom & 0xFF;

        PoseStack pose = graphics.pose();
        Matrix4f matrix = pose.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        buf.vertex(matrix, x1, y2, 0).color(redBottom, greenBottom, blueBottom, alphaBottom).endVertex(); // bl
        buf.vertex(matrix, x2, y2, 0).color(redBottom, greenBottom, blueBottom, alphaBottom).endVertex(); // br
        buf.vertex(matrix, x2, y1, 0).color(redTop, greenTop, blueTop, alphaTop).endVertex(); // tr
        buf.vertex(matrix, x1, y1, 0).color(redTop, greenTop, blueTop, alphaTop).endVertex(); // tl

        BufferUploader.drawWithShader(buf.end());
    }

    private int colorAt(int i, int per, int center, int lightRange, int mediumRange) {
        // coordinate compute in clockwise direction
        i = mod(i, per);

        // distances towards the center
        int clockwiseDist = mod(i - center, per);
        int cClockwiseDist = mod(center - i, per);

        // Color amount priority based on the segment amount per color. The prominent one is the darker (which should
        // be transparent or a dark color) (this effect is called glint for a reason)
        if (clockwiseDist <= lightRange) {
            return blend(color1, color2, clockwiseDist / (float) lightRange);
        }
        else if (clockwiseDist <= lightRange + mediumRange) {
            return blend(color2, color3, (clockwiseDist - lightRange) / (float) mediumRange);
        }
        else if (cClockwiseDist <= lightRange) {
            return blend(color1, color2, cClockwiseDist / (float) lightRange);
        }
        else if (cClockwiseDist <= lightRange + mediumRange) {
            return blend(color2, color3, (cClockwiseDist - lightRange) / (float) mediumRange);
        }
        else {
            return color3;
        }

    }

    /**
     * ARGB color blender. Derived from:
     * https://github.com/BenSouchet/color-blend/blob/gh-pages/assets/js/main.js
     */
    private static int blend(int c1, int c2, float t) {
        int a1 = (c1 >>> 24);
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24);
        int r2 = (c2 >>> 16) & 0xFF;
        int g2 = (c2 >>> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a<<24) | (r<<16) | (g<<8) | b;
    }

    private static int mod(int x, int m) {
        int ret = x % m;
        return ret < 0 ? ret + m : ret;
    }

    private static int clamp(int value) {
        return Math.max(MIN_SEGMENTS, Math.min(MAX_SEGMENTS, value));
    }

}