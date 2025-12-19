package dev.xylonity.tooltipoverhaul.client.style.overlay;

import com.mojang.blaze3d.platform.NativeImage;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.layer.impl.OverlayLayer;
import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.Vec2;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultOverlay implements OverlayLayer {

    private static final int BLOCK_DIMENSION = 44;

    private static final Map<ResourceLocation, TextureMetadata> TEXTURES = new ConcurrentHashMap<>();

    @Override
    public void render(TooltipContext context, Vec2 position) {

        String rawTexturePath = RenderUtils.getOverlayLocation(context);
        if (rawTexturePath == null || rawTexturePath.isBlank() || rawTexturePath.isEmpty()) {
            return;
        }

        ResourceLocation textureLocation = TooltipOverhaul.rawPathOf(rawTexturePath);
        TextureMetadata textureMetadata = getTextureMetadata(textureLocation);

        int textureWidth = textureMetadata.width();
        int textureHeight = textureMetadata.height();
        int frameAmount = textureMetadata.frames();

        int idx = frameAmount > 1 ? (int) ((System.currentTimeMillis() / Constants.getOverlayFrameTime()) % frameAmount) : 0;
        int frameOffset = idx * Constants.getOverlayFrameDimension();

        int x = (int) position.x;
        int y = (int) position.y;
        int width = (int) context.getTooltipSize().x;
        int height = (int) context.getTooltipSize().y;

        int baseOffset = -25;

        int leftX = x + baseOffset;
        int rightX = x + width - 20;
        int topY = y + baseOffset + 1;
        int bottomY = y + height - 20;

        int centerX = leftX + (width / 2) + 2;
        int centerY = topY + (height / 2) + 3;

        // TOP LEFT
        context.getGraphics().blit(textureLocation, leftX, topY, 0, frameOffset, BLOCK_DIMENSION, BLOCK_DIMENSION, textureWidth, textureHeight);

        // TOP RIGHT
        context.getGraphics().blit(textureLocation, rightX, topY, BLOCK_DIMENSION * 2, frameOffset, BLOCK_DIMENSION, BLOCK_DIMENSION, textureWidth, textureHeight);

        // BOTTOM LEFT
        context.getGraphics().blit(textureLocation, leftX, bottomY, 0, BLOCK_DIMENSION * 2 + frameOffset, BLOCK_DIMENSION, BLOCK_DIMENSION, textureWidth, textureHeight);

        // BOTTOM RIGHT
        context.getGraphics().blit(textureLocation, rightX, bottomY, BLOCK_DIMENSION * 2, BLOCK_DIMENSION * 2 + frameOffset, BLOCK_DIMENSION, BLOCK_DIMENSION, textureWidth, textureHeight);

        // LEFT
        context.getGraphics().blit(textureLocation, leftX, centerY, 0, BLOCK_DIMENSION + frameOffset, BLOCK_DIMENSION, BLOCK_DIMENSION, textureWidth, textureHeight);

        // RIGHT
        context.getGraphics().blit(textureLocation, rightX, centerY, BLOCK_DIMENSION * 2, BLOCK_DIMENSION + frameOffset, BLOCK_DIMENSION, BLOCK_DIMENSION, textureWidth, textureHeight);

        // TOP
        context.getGraphics().blit(textureLocation, centerX, topY, BLOCK_DIMENSION, frameOffset, BLOCK_DIMENSION, BLOCK_DIMENSION, textureWidth, textureHeight);

        // BOTTOM
        context.getGraphics().blit(textureLocation, centerX, bottomY, BLOCK_DIMENSION, BLOCK_DIMENSION * 2 + frameOffset, BLOCK_DIMENSION, BLOCK_DIMENSION, textureWidth, textureHeight);

    }

    private static TextureMetadata getTextureMetadata(ResourceLocation texture) {
        int frameDimension = Constants.getOverlayFrameDimension();
        return TEXTURES.computeIfAbsent(texture, tex -> {
            try {
                Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(tex);
                if (resource.isEmpty()) {
                    return new TextureMetadata(frameDimension, frameDimension, 1);
                }

                try (InputStream inputStream = resource.get().open(); NativeImage img = NativeImage.read(inputStream)) {
                    int width = img.getWidth();
                    int heigth = img.getHeight();

                    if (width < frameDimension) {
                        TooltipOverhaul.LOGGER.warn("Texture width {} is smaller than expected {} for {}", width, frameDimension, tex);
                    }

                    if (heigth % frameDimension != 0) {
                        TooltipOverhaul.LOGGER.warn("Texture height {} is not a multiple of {} for {} (animation may look off)", heigth, frameDimension, tex);
                    }

                    return new TextureMetadata(width, heigth, Math.max(1, heigth / frameDimension));
                }

            }
            catch (Exception exception) {
                TooltipOverhaul.LOGGER.error("Failed to read texture {}: {}", tex, exception.toString());
                return new TextureMetadata(frameDimension, frameDimension, 1);
            }

        });

    }

    private record TextureMetadata(int width, int height, int frames) { ;; }

}