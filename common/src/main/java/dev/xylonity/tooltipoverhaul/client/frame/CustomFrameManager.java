package dev.xylonity.tooltipoverhaul.client.frame;

import com.mojang.blaze3d.platform.NativeImage;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import net.minecraft.client.Minecraft;
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

    private static final float MIN_DARK_RATIO = 0.30f;
    private static final double LUMINISCE = 0.075;
    private static final int SAMPLEX = 2;
    private static final int ALPHA_RATIO = 48;

    public static void initialize() {
        if (INIT) return;

        try {
            customFrames.clear();
            customFrames.putAll(CustomFrameLoader.loadCustomFrames(Minecraft.getInstance().getResourceManager()));

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
            customFrames.putAll(CustomFrameLoader.loadCustomFrames(resourceManager));

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
    public static void renderCustomFrame(TooltipContext ctx, CustomFrameData frameData, Vec2 pos, Point size) {
        if (frameData.getTexture() == null) return;

        ResourceLocation texture = new ResourceLocation(frameData.getTexture());

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
            ctx.graphics().blit(texture, x - 22 - 4 + 1, y - 22 - 4 + 1, 0, vFrameOffset, 44, 44, texW, texH);

            // TOP RIGHT
            ctx.graphics().blit(texture, x + width - 20 + 1, y - 22 - 4 + 1, 88, vFrameOffset, 44, 44, texW, texH);

            // BOTTOM LEFT
            ctx.graphics().blit(texture, x - 22 - 4 + 1, y + height - 20 + 1, 0, 88 + vFrameOffset, 44, 44, texW, texH);

            // BOTTOM RIGHT
            ctx.graphics().blit(texture, x + width - 20 + 1, y + height - 20 + 1, 88, 88 + vFrameOffset, 44, 44, texW, texH);

            // LEFT
            ctx.graphics().blit(texture, x - 22 - 4 + 1, (y - 22 - 4 + 1) + 3 + height / 2, 0, 44 + vFrameOffset, 44, 44, texW, texH);

            // RIGHT
            ctx.graphics().blit(texture, x + width - 20 + 1, (y - 22 - 4 + 1) + 3 + height / 2, 88, 44 + vFrameOffset, 44, 44, texW, texH);

            // TOP
            ctx.graphics().blit(texture, (x - 22 - 4 + 1) + 4 + width / 2, y - 22 - 4 + 1, 44, vFrameOffset, 44, 44, texW, texH);

            // BOTTOM
            ctx.graphics().blit(texture, (x - 22 - 4 + 1) + 4 + width / 2, y + height - 20 + 1, 44, 88 + vFrameOffset, 44, 44, texW, texH);
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
     * Estimates a hard main color for the actual frame by sampling only the side patches (TOP/LEFT/RIGHT/BOTTOM),
     * skipping corners and extremes, and normalizing per alpha and sat.
     */
    private static int getMainColor(NativeImage img, int frameIdx) {
        int frameNum = frameIdx * FRAME_DIM;
        int marginX = 8;
        int marginY = 8;

        Accumulator acc = new Accumulator();

        // Top
        linearRGB(img, 44, frameNum, marginX, marginY, acc);
        // Left
        linearRGB(img, 0,  frameNum + 44, marginX, marginY, acc);
        // Right
        linearRGB(img, 88, frameNum + 44, marginX, marginY, acc);
        // Bottom
        linearRGB(img, 44, frameNum + 88, marginX, marginY, acc);

        // Fallback if there is much diff between the calculated chucks, so a global avg is computed
        if (acc.alphaTotal < 1e-5) {
            return colorFallback(img, frameIdx);
        }

        float[] hsl = accumulateRGB2HSL(acc);
        hsl[2] = clamp(hsl[2], 0.22f, 0.90f);
        return 0xFF000000 | (hslToRgb(hsl[0], hsl[1], hsl[2]) & 0x00FFFFFF);
    }

    // Patches and accumulates alpha and saturation data
    private static void linearRGB(NativeImage img, int x0, int y0, int mx, int my, Accumulator acc) {
        int x1 = 2 + x0 + 42;
        int y1 = 2 + y0 + 42;
        for (int y = y0 + my; y < y1 - my; y += SAMPLEX) {
            for (int x = x0 + mx; x < x1 - mx; x += SAMPLEX) {
                int colorr = img.getPixelRGBA(x, y);
                int a = (colorr >>> 24) & 0xFF;

                if (a < ALPHA_RATIO) continue;

                int blue = (colorr >>> 16) & 0xFF;
                int green = (colorr >>> 8) & 0xFF;
                int red = (colorr) & 0xFF;

                float[] HSVParser = Color.RGBtoHSB(red, green, blue, null);

                float s = HSVParser[1];
                if (s < 0.15f) continue;

                float v = HSVParser[2];
                if (v < 0.15f || v > 0.92f) continue;

                acc.redTotal += ((a / 255.0) * (0.6 + 0.4 * s)) * srgbToLinear(red / 255.0);
                acc.greenTotal += ((a / 255.0) * (0.6 + 0.4 * s)) * srgbToLinear(green / 255.0);
                acc.blueTotal += ((a / 255.0) * (0.6 + 0.4 * s)) * srgbToLinear(blue / 255.0);
                acc.alphaTotal += ((a / 255.0) * (0.6 + 0.4 * s));
            }
        }

    }

    private static int colorFallback(NativeImage img, int frameIdx) {
        int y0 = frameIdx * FRAME_DIM;
        Accumulator acc = new Accumulator();
        for (int y = 0; y < FRAME_DIM; y += SAMPLEX) {
            for (int x = 0; x < FRAME_DIM; x += SAMPLEX) {
                int abgr = img.getPixelRGBA(x, y0 + y);
                int a = (abgr >>> 24) & 0xFF;

                if (a < ALPHA_RATIO) {
                    continue;
                }

                int red = (abgr) & 0xFF;
                int green = (abgr >>> 8) & 0xFF;
                int blue = (abgr >>> 16) & 0xFF;

                float[] hsv = Color.RGBtoHSB(red, green, blue, null);
                float s = hsv[1], v = hsv[2];

                if (v < 0.12f || v > 0.96f) {
                    continue;
                }

                acc.redTotal += ((a / 255.0) * (0.5 + 0.5 * s)) * srgbToLinear(red / 255.0);
                acc.greenTotal += ((a / 255.0) * (0.5 + 0.5 * s)) * srgbToLinear(green / 255.0);
                acc.blueTotal += ((a / 255.0) * (0.5 + 0.5 * s)) * srgbToLinear(blue / 255.0);
                acc.alphaTotal += ((a / 255.0) * (0.5 + 0.5 * s));
            }

        }

        if (acc.alphaTotal <= 1e-6) {
            return 0xFFFFFFFF;
        }

        float[] hsl = accumulateRGB2HSL(acc);
        hsl[2] = clamp(hsl[2], 0.22f, 0.92f);
        int rgb = hslToRgb(hsl[0], hsl[1], hsl[2]) & 0x00FFFFFF;

        return 0xFF000000 | rgb;
    }

    // Color parser bridge
    private static float[] accumulateRGB2HSL(Accumulator acc) {
        double invAlpha = acc.alphaTotal > 0.0 ? 1.0 / acc.alphaTotal : 0.0;

        int r = (int) Math.round(linearToSrgb(acc.redTotal * invAlpha) * 255.0);
        int g = (int) Math.round(linearToSrgb(acc.greenTotal * invAlpha) * 255.0);
        int b = (int) Math.round(linearToSrgb(acc.blueTotal * invAlpha) * 255.0);

        // converts to HSL and enforces minimum saturation
        float[] hsl = rgbToHsl(r, g, b);
        hsl[1] = Math.max(hsl[1], 0.30f);

        return hsl;
    }

    /**
     * Returns the palette schema from the selected data if there is a frame texture present
     */
    public static int[] getPalette(CustomFrameData frameData) {
        if (frameData == null || frameData.getTexture() == null) return new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};

        ResourceLocation texture = new ResourceLocation(frameData.getTexture());
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
     * Derived from:
     * https://github.com/material-foundation/material-color-utilities/blob/main/java/utils/ColorUtils.java
     */
    private static int[] getFrontColors(NativeImage img, int frameIdx) {
        int base = getMainColor(img, frameIdx);

        // Extracts the base color components
        int alpha = 0xFF;
        int red = (base >>> 16) & 0xFF;
        int green = (base >>> 8) & 0xFF;
        int blue = (base) & 0xFF;

        // For better color manipulation a conversion to hsl is done
        float[] hsl = rgbToHsl(red, green, blue);
        float hue = hsl[0];
        float saturn = Math.max(hsl[1], 0.32f); // saturn :imp:
        float low = hsl[2];

        float lowMid = clamp(low, 0.20f, 0.92f);
        float highMid = clamp(saturn, 0.32f, 1.0f);
        int midColor = (hslToRgb(hue, highMid, lowMid) & 0x00FFFFFF) | (alpha << 24);

        float lowDark = clamp(lowMid - 0.12f, MIN_DARK_RATIO, 0.88f);
        float highDark = clamp(highMid * 1.06f, 0.32f, 1.0f);

        // Avoid overlap between the min and max values of the dark coloring (because sometimes the darker value
        // becomes invisible or gets a darker value than the one it should)
        if (lowDark >= lowMid) {
            lowDark = Math.max(MIN_DARK_RATIO, lowMid - 0.06f);
        }

        int darkCol = (hslToRgb(hue, highDark, lowDark) & 0x00FFFFFF) | (alpha << 24);

        // The darker color is only decreased a bit
        if (luminanceParser(darkCol) < LUMINISCE) {
            int cap = 0;
            while (luminanceParser(darkCol) < LUMINISCE && cap++ < 8) {
                lowDark = clamp(lowDark + 0.02f, MIN_DARK_RATIO, Math.min(0.88f, lowMid - 0.02f));
                darkCol = (hslToRgb(hue, highDark, lowDark) & 0x00FFFFFF) | (alpha << 24);
            }

        }

        // Returns the actual colors
        return new int[]{(
                hslToRgb(
                        hue,
                        clamp(highMid * 0.92f, 0.28f, 1.0f),
                        clamp(lowMid + 0.12f, 0.20f, 0.95f)) & 0x00FFFFFF) | (alpha << 24),
                midColor, darkCol};
    }

    /**
     * Hsx parser
     * https://github.com/gka/chroma.js/blob/main/src/interpolator/_hsx.js
     */
    private static float[] rgbToHsl(int r, int g, int b) {
        float red = r / 255f;
        float green = g / 255f;
        float blue = b / 255f;
        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float hue;
        float sat;
        float l = (max + min) / 2f;
        if (max == min) {
            hue = 0f; sat = 0f;
        } else {
            float dist = max - min;
            sat = l > 0.5f ? dist / (2f - max - min) : dist / (max + min);
            if (max == red) {
                hue = (green - blue) / dist + (green < blue ? 6f : 0f);
            } else if (max == green) {
                hue = (blue - red) / dist + 2f;
            } else {
                hue = (red - green) / dist + 4f;
            }

            hue /= 6f;
        }

        return new float[]{hue, sat, l};
    }

    /**
     * Derived from hsl2rgb npm package
     * https://github.com/Experience-Monks/glsl-hsl2rgb/blob/master/index.glsl
     */
    private static int hslToRgb(float h, float s, float l) {
        float r;
        float g;
        float b;
        if (s == 0f) {
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1f + s) : (l + s - l * s);
            float p = 2f * l - q;
            r = hue2rgb(p, q, h + 1f / 3f);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1f / 3f);
        }

        return ((int) (r * 255 + 0.5f) << 16) | ((int) (g * 255 + 0.5f) << 8) | (int) (b * 255 + 0.5f);
    }

    /**
     * Derived from hsl2rgb npm package
     * https://github.com/Experience-Monks/glsl-hsl2rgb/blob/master/index.glsl
     */
    private static float hue2rgb(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < 1f/6f) return p + (q - p) * 6f * t;
        if (t < 1f/2f) return q;
        if (t < 2f/3f) return p + (q - p) * (2f/3f - t) * 6f;

        return p;
    }

    /**
     * Luminance compute
     * https://github.com/tmcw/wcag-contrast/blob/master/index.js
     */
    private static double luminanceParser(int argb) {
        double rl = srgbToLinear(((argb >>> 16) & 0xFF) / 255.0);
        double gl = srgbToLinear(((argb >>> 8) & 0xFF) / 255.0);
        double bl = srgbToLinear(((argb) & 0xFF) / 255.0);

        return 0.2126 * rl + 0.7152 * gl + 0.0722 * bl;
    }

    private static double srgbToLinear(double x) {
        return x <= 0.04045 ? x / 12.92 : Math.pow((x + 0.055) / 1.055, 2.4);
    }

    private static double linearToSrgb(double x) {
        return x <= 0.0031308 ? x * 12.92 : 1.055 * Math.pow(x, 1.0 / 2.4) - 0.055;
    }

    private static float clamp(float c1, float c2, float hu) {
        return c1 < c2 ? c2 : (Math.min(c1, hu));
    }

    private record TextureInfo(int width, int height, int frames) { ;; }

    private static class Accumulator {
        double redTotal;
        double greenTotal;
        double blueTotal;
        double alphaTotal;
    }

}