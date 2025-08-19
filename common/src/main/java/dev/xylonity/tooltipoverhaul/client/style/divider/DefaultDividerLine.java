package dev.xylonity.tooltipoverhaul.client.style.divider;

import dev.xylonity.tooltipoverhaul.client.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.TooltipRenderer;
import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;
import dev.xylonity.tooltipoverhaul.client.layer.bridge.ITooltipDividerLine;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.util.List;

@SuppressWarnings("unchecked")
public class DefaultDividerLine implements ITooltipDividerLine {

    @Override
    public void render(LayerDepth depth, TooltipContext ctx, Vec2 pos, Point size) {
        if (ctx.getComponents().size() > 1) {
            int y = (int) pos.y + 4 + TooltipRenderer.PADDING_Y + ((List<ClientTooltipComponent>) ctx.getComponents()).get(0).getHeight() + 4 + 10;
            int x = (int) ((int) pos.x + size.x * 0.1f);
            int width = (int) (size.x - size.x * 0.2f);

            float fade = 0.025f;

            ctx.push(() -> {
                ctx.translate(0, 0, depth.getZ());
                // Cuadratic curve that derives color per pixel
                for (int i = 0; i < width; i++) {
                    float center = width / 2.0f;
                    float distance = Math.abs(i - center) / center;

                    int alpha;
                    if (distance <= fade) {
                        alpha = 255;
                    } else {
                        float z = (distance - fade) / (1.0f - fade);
                        alpha = (int) (255 * (1.0f - z * z));
                    }

                    alpha = Math.max(0, Math.min(255, alpha));

                    ctx.graphics().fill(x + i, y, x + i + 1, y + 1, (alpha << 24) | 0x00EFEFEF);
                }
            });

        }

    }

}