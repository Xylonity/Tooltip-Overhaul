package dev.xylonity.tooltipoverhaul.client.frame;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the loading context of the custom_frames.json files
 */
public class CustomFrameManager {

    private static final Map<ResourceLocation, CustomFrameData> customFrames = new ConcurrentHashMap<>();
    private static boolean INIT = false;

    private static final int FRAME_DIM = 132;
    private static final int FRAME_TIME = 120; // In MS

    private static final Map<ResourceLocation, TextureInfo> TEXTURE_CACHE = new ConcurrentHashMap<>();

    // Color palette per frame (3 colors for the gradient inner overlay)
    private static final Map<ResourceLocation, int[][]> ACCENT_CACHE = new ConcurrentHashMap<>();

    private static final int SAMPLEX = 2;

    public static void initialize() {
        if (INIT) return;

        try {
            customFrames.clear();
            customFrames.putAll(CustomFrameLoader.loadCustomFrames(Minecraft.getInstance().getResourceManager(), TooltipOverhaul.PLATFORM.getConfigPath()));

            INIT = true;

            TooltipOverhaul.LOGGER.info("{} frames have been loaded!", customFrames.size());
        }
        catch (Exception e) {
            TooltipOverhaul.LOGGER.error("Failed to initialize custom frames loader: {}", e.getMessage());
        }

    }

    public static void initialize(ResourceManager resourceManager) {
        if (INIT) return;

        try {
            customFrames.clear();
            customFrames.putAll(CustomFrameLoader.loadCustomFrames(resourceManager, TooltipOverhaul.PLATFORM.getConfigPath()));

            INIT = true;

            TooltipOverhaul.LOGGER.info("{} frames have been loaded!", customFrames.size());
        }
        catch (Exception e) {
            TooltipOverhaul.LOGGER.error("Failed to initialize custom frames loader: {}", e.getMessage());
        }

    }

    /**
     * Per resource reload
     */
    public static void reset() {
        customFrames.clear();
        TEXTURE_CACHE.clear();
        ACCENT_CACHE.clear();
        INIT = false;
    }

    /**
     * Returns the custom frame data for the specified stack (if it's present anywhere)
     */
    public static Optional<CustomFrameData> of(ItemStack stack) {
        if (!INIT) initialize();

        return customFrames.values().stream().filter(cfg -> cfg.matches(stack)).findFirst();
    }

    /**
     * Core renderer for custom frames
     * Gone through hard times trying to sync the positions correctly :skull:
     */
    public static void renderCustomFrame(TooltipContext ctx, Vec2 pos, Point size) {
        String textureLocation = ctx.data().map(CustomFrameData::getTexture).orElse(TooltipsConfig.GLOBAL_FRAME_OVERLAY_LOCATION);
        if (textureLocation == null || textureLocation.isEmpty() || textureLocation.isBlank()) return;

        ResourceLocation texture = ResourceLocation.parse(textureLocation);

        // Computes the exact texture dimensions, thus automatically handling animated frames. The textures have a fixed
        // dimension of 132x132n, as n being the number of frames. Albeit this doesn't require much computation, meta info
        // is memoized in a map upon texture load to avoid recalculating things every tick
        TextureInfo meta = getTexMeta(texture);
        int texW = meta.width;
        int texH = meta.height;
        int frames = meta.frames;

        int idx = frames > 1 ? (int)((System.currentTimeMillis() / FRAME_TIME) % frames) : 0;
        int vFrameOffset = idx * FRAME_DIM;

        int x = (int) pos.x;
        int y = (int) pos.y;
        int width = size.x;
        int height = size.y;

        ctx.push(() -> {
            ctx.translate(0, 0, LayerDepth.BACKGROUND_OVERLAY.getZ());

            // TOP LEFT
            ctx.graphics().blit(RenderType::guiTextured, texture, x - 22 - 4 + 1, y - 22 - 4 + 1, 0, vFrameOffset, 44, 44, texW, texH);

            // TOP RIGHT
            ctx.graphics().blit(RenderType::guiTextured, texture, x + width - 20 + 1, y - 22 - 4 + 1, 88, vFrameOffset, 44, 44, texW, texH);

            // BOTTOM LEFT
            ctx.graphics().blit(RenderType::guiTextured, texture, x - 22 - 4 + 1, y + height - 20 + 1, 0, 88 + vFrameOffset, 44, 44, texW, texH);

            // BOTTOM RIGHT
            ctx.graphics().blit(RenderType::guiTextured, texture, x + width - 20 + 1, y + height - 20 + 1, 88, 88 + vFrameOffset, 44, 44, texW, texH);

            // LEFT
            ctx.graphics().blit(RenderType::guiTextured, texture, x - 22 - 4 + 1, (y - 22 - 4 + 1) + 3 + height / 2, 0, 44 + vFrameOffset, 44, 44, texW, texH);

            // RIGHT
            ctx.graphics().blit(RenderType::guiTextured, texture, x + width - 20 + 1, (y - 22 - 4 + 1) + 3 + height / 2, 88, 44 + vFrameOffset, 44, 44, texW, texH);

            // TOP
            ctx.graphics().blit(RenderType::guiTextured, texture, (x - 22 - 4 + 1) + 4 + width / 2, y - 22 - 4 + 1, 44, vFrameOffset, 44, 44, texW, texH);

            // BOTTOM
            ctx.graphics().blit(RenderType::guiTextured, texture, (x - 22 - 4 + 1) + 4 + width / 2, y + height - 20 + 1, 44, 88 + vFrameOffset, 44, 44, texW, texH);
        });

    }

    private static TextureInfo getTexMeta(ResourceLocation texture) {
        return TEXTURE_CACHE.computeIfAbsent(texture, tex -> {
            try {
                Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(tex);
                if (resource.isEmpty()) {
                    return new TextureInfo(FRAME_DIM, FRAME_DIM, 1);
                }

                try (InputStream is = resource.get().open(); NativeImage img = NativeImage.read(is)) {
                    int width = img.getWidth();
                    int heigth = img.getHeight();

                    if (width < FRAME_DIM) TooltipOverhaul.LOGGER.warn("Texture width {} is smaller than expected {} for {}", width, FRAME_DIM, tex);
                    if (heigth % FRAME_DIM != 0) TooltipOverhaul.LOGGER.warn("Texture height {} is not a multiple of {} for {} (anim may look off)", heigth, FRAME_DIM, tex);

                    return new TextureInfo(width, heigth, Math.max(1, heigth / FRAME_DIM));
                }
            }
            catch (Exception e) {
                TooltipOverhaul.LOGGER.error("Failed to read texture {}: {}", tex, e.toString());
                return new TextureInfo(FRAME_DIM, FRAME_DIM, 1);
            }
        });

    }

    /**
     * Samples a specific region of the frame and calculates its average color
     */
    private static Integer sampleRegionColor(NativeImage img, int x0, int y0, int width, int height) {
        Accumulator acc = new Accumulator();

        int x1 = x0 + width;
        int y1 = y0 + height;

        int marginX = 8;
        int marginY = 8;
        for (int y = y0 + marginY; y < y1 - marginY; y += SAMPLEX) {
            for (int x = x0 + marginX; x < x1 - marginX; x += SAMPLEX) {
                int abgr = img.getPixel(x, y);

                // Minecraft NativeImage uses abgr format
                int a = (abgr >> 24) & 0xFF;
                int b = (abgr >> 16) & 0xFF;
                int g = (abgr >> 8) & 0xFF;
                int r = abgr & 0xFF;

                // Alpha ratio
                if (a < 48) {
                    continue;
                }

                float[] hsv = Color.RGBtoHSB(r, g, b, null);
                float s = hsv[1];
                float v = hsv[2];

                // Minimum saturation
                if (s < 0.05) {
                    continue;
                }

                // min/max brightness margins
                if (v < 0.1 || v > 0.98f) {
                    continue;
                }

                float weight = (a / 255.0f) * (0.2f + 0.8f * s);

                acc.redTotal += weight * srgbToLinear(r / 255.0);
                acc.greenTotal += weight * srgbToLinear(g / 255.0);
                acc.blueTotal += weight * srgbToLinear(b / 255.0);
                acc.alphaTotal += weight;
            }

        }

        if (acc.alphaTotal < 1e-5) {
            return null;
        }

        double invAlpha = 1.0 / acc.alphaTotal;
        int r = (int) Math.round(linearToSrgb(acc.redTotal * invAlpha) * 255.0);
        int g = (int) Math.round(linearToSrgb(acc.greenTotal * invAlpha) * 255.0);
        int b = (int) Math.round(linearToSrgb(acc.blueTotal * invAlpha) * 255.0);

        r = clamp(r);
        g = clamp(g);
        b = clamp(b);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Returns the palette schema from the selected data if there is a frame texture present
     */
    public static int[] getPalette(CustomFrameData frameData) {
        if (frameData == null || frameData.getTexture() == null) return new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};

        ResourceLocation texture = ResourceLocation.tryParse(frameData.getTexture());
        TextureInfo meta = getTexMeta(texture);
        int idx = meta.frames > 1 ? (int)((System.currentTimeMillis() / FRAME_TIME) % meta.frames) : 0;

        return getPalette(texture, idx);
    }

    public static int[] getPalette(ResourceLocation texture, int frameIndex) {
        int[][] palette = ACCENT_CACHE.computeIfAbsent(texture, t -> accent(t, getTexMeta(texture)));

        if (palette.length == 0) {
            return new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
        }

        return palette[Math.max(0, Math.min(frameIndex, palette.length - 1))];
    }

    private static int[][] accent(ResourceLocation tex, TextureInfo meta) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(tex);
            if (resource.isEmpty()) return new int[0][];

            try (InputStream input = resource.get().open(); NativeImage img = NativeImage.read(input)) {
                int[][] ret = new int[meta.frames][3];
                for (int f = 0; f < meta.frames; f++) {
                    ret[f] = getFrontColors(img, f);
                }

                return ret;
            }
        }
        catch (Exception ignore) {
            return new int[0][];
        }

    }

    /**
     * Returns the 3 front colors from the given img
     * Derived from (altho not much is similar):
     * https://github.com/material-foundation/material-color-utilities/blob/main/java/utils/ColorUtils.java
     */
    private static int[] getFrontColors(NativeImage img, int frameIdx) {
        int frameOffset = frameIdx * FRAME_DIM;

        // Samples top patch
        Integer topColor = sampleRegionColor(img, 44, frameOffset, 44, 44);

        // Samples middle sections
        Integer leftColor = sampleRegionColor(img, 0, frameOffset + 44, 44, 44);
        Integer rightColor = sampleRegionColor(img, 88, frameOffset + 44, 44, 44);

        Integer midColor = null;
        if (leftColor != null && rightColor != null) {
            midColor = blendColors(leftColor, rightColor);
        }
        else if (leftColor != null) {
            midColor = leftColor;
        }
        else if (rightColor != null) {
            midColor = rightColor;
        }

        // Samples bottom section
        Integer bottomColor = sampleRegionColor(img, 44, frameOffset + 88, 44, 44);

        int validColors = 0;
        if (topColor != null) validColors++;
        if (midColor != null) validColors++;
        if (bottomColor != null) validColors++;

        // If the section is empty (like in the silver frame)
        if (validColors == 0) {
            int neutral = 0xFFC0C0C0;
            return new int[]{neutral, neutral, neutral};
        }

        // Fills missing colors
        if (topColor == null) {
            topColor = midColor != null ? midColor : bottomColor;
        }
        if (midColor == null) {
            midColor = topColor;
        }
        if (bottomColor == null) {
            bottomColor = midColor;
        }

        return new int[]{invertRGB(topColor), invertRGB(midColor), invertRGB(bottomColor)};
    }

    /**
     * Blends two colors by averaging them in linear RGB space (srgb)
     * https://www.nayuki.io/res/srgb-transform-library/SrgbTransform.java
     * https://alexanderhoughton.co.uk/blog/visualising-srgb-gamma-correction/
     */
    private static int blendColors(int color1, int color2) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) Math.round(linearToSrgb((srgbToLinear(r1 / 255.0) + srgbToLinear(r2 / 255.0)) * 0.5) * 255.0);
        int g = (int) Math.round(linearToSrgb((srgbToLinear(g1 / 255.0) + srgbToLinear(g2 / 255.0)) * 0.5) * 255.0);
        int b = (int) Math.round(linearToSrgb((srgbToLinear(b1 / 255.0) + srgbToLinear(b2 / 255.0)) * 0.5) * 255.0);

        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static double srgbToLinear(double x) {
        return x <= 0.04045 ? x / 12.92 : Math.pow((x + 0.055) / 1.055, 2.4);
    }

    private static double linearToSrgb(double x) {
        return x <= 0.0031308 ? x * 12.92 : 1.055 * Math.pow(x, 1.0 / 2.4) - 0.055;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static int invertRGB(int c) {
        int r = 255 - ((c >> 16) & 0xFF);
        int g = 255 - ((c >> 8) & 0xFF);
        int b = 255 - (c & 0xFF);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private record TextureInfo(int width, int height, int frames) { ;; }

    private static class Accumulator {
        double redTotal;
        double greenTotal;
        double blueTotal;
        double alphaTotal;
    }

}