package dev.xylonity.tooltipoverhaul.client.layout;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class TooltipSizeCalculator {

    private final TooltipContext context;

    public TooltipSizeCalculator(TooltipContext context) {
        this.context = context;
    }

    public Vec2 calculate() {
        if (context.getLayoutStyle() == TooltipLayout.Style.COMPACT) {
            return calculateCompact();
        }

        final boolean hasIcon = TooltipLayout.hasHeaderIcon(context);
        final boolean hasRating = RenderUtils.hasRating(context);
        final boolean hasDividerLine = context.hasDividerLine();

        final List<ClientTooltipComponent> components = context.getComponents();
        final Font font = context.getFont();

        final int paddingX = context.getPaddingX();
        final int paddingY = context.getPaddingY();

        // Offset to the right if the icon is active
        final int iconOffset = TooltipLayout.titleInset(context);

        // Computes approximated width and height using the component amount
        int width = components.get(0).getWidth(font) + paddingX * 2 + iconOffset;
        int height = components.get(0).getHeight() + paddingY * 2;
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                height += components.get(i).getHeight();
                width = Math.max(width, components.get(i).getWidth(font) + paddingX * 2);
            }

        }

        // Computes the new width if the rating is larger than the tooltip's width and adds extra height in case the icon is not present
        if (hasRating) {
            final Component rating = TextUtils.getRatingText(context);
            width = Math.max(width, font.width(rating) + paddingX * 2 + iconOffset);

            if (!hasIcon) {
                height += ClientTooltipComponent.create(rating.getVisualOrderText()).getHeight();
            }

        }

        // If the tooltip has an icon active, the header already holds the title and rating so the title height comes out
        if (hasIcon) {
            height += TooltipLayout.headerHeight(context) - components.get(0).getHeight();
            width = Math.max(width, Constants.getIconSize(context) + paddingX * 2);
        }

        //
        if (hasDividerLine && components.size() > 1) {
            if (hasIcon) {
                height += Constants.getDividerLineFullPadding(context);
            }
            else {
                height += Constants.getDividerLineFullPadding(context);
            }

        }

        return new Vec2(width, height);
    }

    private Vec2 calculateCompact() {
        final List<ClientTooltipComponent> components = context.getComponents();
        final Font font = context.getFont();
        final int padding = context.getPaddingX() * 2;
        int bodyWidth = 0;
        int bodyHeight = 0;
        for (int i = 1; i < components.size(); i++) {
            bodyWidth = Math.max(bodyWidth, components.get(i).getWidth(font));
            bodyHeight += components.get(i).getHeight();
        }

        int width = Math.max(components.get(0).getWidth(font) + TooltipLayout.titleInset(context), bodyWidth) + padding;
        // Lets short names fit naturally without making the panel too wide for long mod names
        int footerWidth = Math.min(font.width(context.getCompactModName()), 96);
        if (RenderUtils.hasRating(context)) {
            if (footerWidth > 0) {
                footerWidth += TooltipLayout.footerTextGap();
            }

            footerWidth += font.width(TextUtils.getRatingText(context));
        }

        width = Math.max(width, footerWidth + padding);

        final int height = TooltipLayout.compactBodyTop(context) + bodyHeight + context.getPaddingY();
        return new Vec2(width, height + TooltipLayout.footerHeight(context));
    }

    public Vec2 adjustSize() {

        Vec2 newSize = context.getTooltipSize();

        final boolean isMainTooltip = context.isMainTooltip();
        final TooltipContext otherContext = context.getOtherTooltipContext();

        final Vec2 otherContextPosition = otherContext != null ? otherContext.getTooltipPosition() : null;
        final Vec2 otherContextSize = otherContext != null ? otherContext.getTooltipSize() : null;

        if (isMainTooltip) {
            if (otherContextPosition != null && otherContextSize != null) {
                if (otherContextPosition.x == context.getPaddingX() && context.getTooltipPosition().x + context.getTooltipSize().x > context.getScreenWidth()) {
                    newSize = new Vec2(context.getScreenWidth() - context.getPaddingX() - context.getTooltipPosition().x, newSize.y);
                }

            }

        }
        else {

        }

        return newSize;
    }

}
