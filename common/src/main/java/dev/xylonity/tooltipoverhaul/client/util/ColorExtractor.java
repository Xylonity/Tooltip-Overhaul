package dev.xylonity.tooltipoverhaul.client.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.awt.*;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ColorExtractor {

    private static final int SAMPLEX = 2;

    private static final int HUE_BINS = 36;
    private static final int MIN_ALPHA = 48;
    private static final int INNER_MARGIN = 14;

    // Per-frame gradient cache
    private static final Map<ResourceLocation, int[][]> GRADIENT_CACHE = new ConcurrentHashMap<>();

    public static void reset() {
        GRADIENT_CACHE.clear();
    }

    public static int[] getOverlayGradient(ResourceLocation texture) {
        int[][] allColors = GRADIENT_CACHE.computeIfAbsent(texture, ColorExtractor::computeAllFrames);
        if (allColors.length == 0) {
            return new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
        }

        int idx = frameIndex(texture, allColors.length);
        return allColors[Math.max(0, Math.min(idx, allColors.length - 1))];
    }

    private static int frameIndex(ResourceLocation texture, int frames) {
        if (frames <= 1) {
            return 0;
        }

        long now = System.currentTimeMillis();
        return (int) ((now / Constants.getOverlayFrameTime()) % frames);
    }

    private static int[][] computeAllFrames(ResourceLocation texture) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
            if (resource.isEmpty()) {
                return new int[0][];
            }

            try (InputStream inputStream = resource.get().open(); NativeImage img = NativeImage.read(inputStream)) {
                int width = img.getWidth();
                int height = img.getHeight();

                int frames = Math.max(1, height / Constants.getOverlayFrameDimension());
                // if it's not a clean multiple still do the best-effort with integer division
                if (frames <= 0) {
                    frames = 1;
                }

                // Extracts the gradient for each frame
                int[][] out = new int[frames][3];
                for (int i = 0; i < frames; i++) {
                    int frameOffset = i * Constants.getOverlayFrameDimension();
                    out[i] = extractDominantGradient(img, width, height, frameOffset);
                }

                return out;
            }

        }
        catch (Exception ignored) {
            return new int[0][];
        }

    }

    private static int[] extractDominantGradient(NativeImage image, int imageWidth, int imageHeight, int frameOffsetY) {
        // Defining a sampling area here within the frame itself (avoiding edges)
        int x0 = INNER_MARGIN;
        int y0 = frameOffsetY + INNER_MARGIN;
        int x1 = Math.min(Constants.getOverlayFrameDimension() - INNER_MARGIN, imageWidth);
        int y1 = Math.min(frameOffsetY + Constants.getOverlayFrameDimension() - INNER_MARGIN, imageHeight);

        Integer base = extractDominantColorByHue(image, x0, y0, x1, y1);
        if (base == null) {
            int neutral = 0xFFC0C0C0;
            return new int[]{neutral, neutral, neutral};
        }

        int dark = ColorUtils.tweakHSV(base, 1.06f, 0.82f);
        int mid = ColorUtils.tweakHSV(base, 1.00f, 1.00f);
        int light = ColorUtils.tweakHSV(base, 0.92f, 1.18f);

        return new int[]{light, mid, dark};
    }

    /**
     * Dominant hue via histogram, then averages in linear rgb for bins near the dominant one.
     * The image pixels follow the rgba schema (Minecraft format).
     */
    private static Integer extractDominantColorByHue(NativeImage image, int x0, int y0, int x1, int y1) {
        // Histogram for hue distribution (10º each)
        double[] histogram = new double[HUE_BINS];

        // Hue histogram
        for (int y = y0; y < y1; y += SAMPLEX) {
            for (int x = x0; x < x1; x += SAMPLEX) {
                int abgr = image.getPixelRGBA(x, y);

                int alpha = (abgr >>> 24) & 0xFF;
                if (alpha < MIN_ALPHA) {
                    continue;
                }

                int blue = (abgr >>> 16) & 0xFF;
                int green = (abgr >>> 8) & 0xFF;
                int red = (abgr) & 0xFF;

                float[] hsv = Color.RGBtoHSB(red, green, blue, null);
                float saturation = hsv[1];
                float brightness = hsv[2];

                // Filters out undesirable colors
                if (saturation < 0.08f) {
                    continue;
                }
                if (brightness < 0.10f || brightness > 0.98f) {
                    continue;
                }

                int bin = (int) (hsv[0] * HUE_BINS);
                if (bin < 0) {
                    bin = 0;
                }
                if (bin >= HUE_BINS) {
                    bin = HUE_BINS - 1;
                }

                // Calculates the pixel weight based on the alpha and the saturation. Higher weight means more saturated pixels
                double weight = (alpha / 255.0) * (0.25 + 0.75 * saturation);
                histogram[bin] += weight;
            }

        }

        // To find dominant hue bin
        int bestBin = -1;
        double bestWeight = 0.0;
        for (int i = 0; i < HUE_BINS; i++) {
            if (histogram[i] > bestWeight) {
                bestWeight = histogram[i];
                bestBin = i;
            }

        }

        if (bestBin < 0 || bestWeight < 1e-6) {
            return null;
        }

        int leftBin = (bestBin - 1 + HUE_BINS) % HUE_BINS;
        int rightBin = (bestBin + 1) % HUE_BINS;

        // linear rgb average for dominant bin neighborhood
        double totalRed = 0;
        double totalGreen = 0;
        double totalBlue = 0;
        double totalWeight = 0;

        for (int y = y0; y < y1; y += SAMPLEX) {
            for (int x = x0; x < x1; x += SAMPLEX) {
                int abgr = image.getPixelRGBA(x, y);

                int alpha = (abgr >>> 24) & 0xFF;
                if (alpha < MIN_ALPHA) {
                    continue;
                }

                int blue = (abgr >>> 16) & 0xFF;
                int green = (abgr >>> 8) & 0xFF;
                int red = (abgr) & 0xFF;

                float[] hsv = Color.RGBtoHSB(red, green, blue, null);
                float saturation = hsv[1];
                float brightness = hsv[2];

                if (saturation < 0.08f) {
                    continue;
                }
                if (brightness < 0.10f || brightness > 0.98f) {
                    continue;
                }

                int bin = (int) (hsv[0] * HUE_BINS);
                if (bin < 0) {
                    bin = 0;
                }
                if (bin >= HUE_BINS) {
                    bin = HUE_BINS - 1;
                }

                if (!(bin == bestBin || bin == leftBin || bin == rightBin)) {
                    continue;
                }

                double weight = (alpha / 255.0) * (0.25 + 0.75 * saturation);

                // Accumulates linear rgb values (weighted)
                totalRed += weight * ColorUtils.srgbToLinear(red / 255.0);
                totalGreen += weight * ColorUtils.srgbToLinear(green / 255.0);
                totalBlue += weight * ColorUtils.srgbToLinear(blue / 255.0);
                totalWeight += weight;
            }

        }

        // If no pixels pass the filters
        if (totalWeight < 1e-6) {
            return null;
        }

        // Converts back linear rgb average to sRGB
        int red = AnimationUtils.clamp255((int) Math.round(ColorUtils.linearToSrgb(totalRed / totalWeight) * 255.0));
        int green = AnimationUtils.clamp255((int) Math.round(ColorUtils.linearToSrgb(totalGreen / totalWeight) * 255.0));
        int blue = AnimationUtils.clamp255((int) Math.round(ColorUtils.linearToSrgb(totalBlue / totalWeight) * 255.0));

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

}