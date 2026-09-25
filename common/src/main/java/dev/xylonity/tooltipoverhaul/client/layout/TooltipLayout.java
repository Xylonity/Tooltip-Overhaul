package dev.xylonity.tooltipoverhaul.client.layout;

import dev.xylonity.tooltipoverhaul.client.render.TooltipContext;
import dev.xylonity.tooltipoverhaul.client.util.Constants;
import dev.xylonity.tooltipoverhaul.client.util.RenderUtils;
import dev.xylonity.tooltipoverhaul.client.util.TextUtils;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.Locale;

/**
 * Shared geometry for the icon, header and the space outside the main panel
 */
public final class TooltipLayout {

    public enum Style {

        CLASSIC,
        BADGE,
        FLOATING,
        COMPACT;

        public static Style fromString(String value) {
            if (value == null) {
                return CLASSIC;
            }

            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e) {
                return CLASSIC;
            }

        }

    }

    public static Style resolveStyle(String value, boolean hasIcon, boolean hasTitle) {
        final Style style = Style.fromString(value);
        return hasTitle && (hasIcon || style == Style.COMPACT) ? style : Style.CLASSIC;
    }

    public static int iconX(TooltipContext context) {
        final int size = Constants.getIconSize(context);
        return switch (context.getLayoutStyle()) {
            case BADGE -> -3 - size / 2;
            case FLOATING -> context.getFloatingLayout().iconX();
            default -> context.getPaddingX() - 1;
        };

    }

    public static int leftOverflow(TooltipContext context) {
        return switch (context.getLayoutStyle()) {
            case BADGE -> -iconX(context) + 2;
            case FLOATING -> context.getOtherTooltipContext() != null ? Constants.getIconSize(context) + 13 : 0;
            default -> 0;
        };

    }

    public static int titleInset(TooltipContext context) {
        if (!context.hasIcon() || context.getLayoutStyle() == Style.FLOATING) {
            return 0;
        }

        if (context.getLayoutStyle() != Style.BADGE) {
            return Constants.getIconSize(context) + Constants.getIconTitleSeparation(context);
        }

        return Math.max(0, iconX(context) + Constants.getIconSize(context) + 1 + Constants.getIconTitleSeparation(context) - context.getPaddingX());
    }

    public static boolean hasExternalIcon(TooltipContext context) {
        return context.getLayoutStyle() == Style.BADGE || context.getLayoutStyle() == Style.FLOATING;
    }

    public static boolean hasHeaderIcon(TooltipContext context) {
        return context.hasIcon() && context.getLayoutStyle() != Style.FLOATING;
    }

    public static int ratingHeight(TooltipContext context) {
        return RenderUtils.hasRating(context) ? ClientTooltipComponent.create(TextUtils.getRatingText(context).getVisualOrderText()).getHeight() : 0;
    }

    public static int footerHeight(TooltipContext context) {
        final int textHeight = footerTextHeight(context);
        return textHeight > 0 ? footerTopGap(context) + textHeight + context.getPaddingY() * 2 : 0;
    }

    public static int footerTopGap(TooltipContext context) {
        return context.getComponents().size() <= 1 ? 1 : 0;
    }

    public static int footerTextHeight(TooltipContext context) {
        if (context.getLayoutStyle() != Style.COMPACT) {
            return 0;
        }

        final int modNameHeight = context.getCompactModName().isEmpty() ? 0 : context.getFont().lineHeight + 1;
        return Math.max(ratingHeight(context), modNameHeight);
    }

    public static int footerTextGap() {
        return 12;
    }

    /**
     * Compact keeps a single row header and reserves a separate footer for item info
     */
    public static int compactBodyTop(TooltipContext context) {
        if (context.getComponents().size() <= 1) {
            return context.getPaddingY() + headerHeight(context);
        }

        final int gap = context.hasDividerLine() ? Constants.getDividerLineFullPadding(context) : 3;
        return context.getPaddingY() + headerHeight(context) + gap + 2;
    }

    public static int iconY(TooltipContext context) {
        return switch (context.getLayoutStyle()) {
            case FLOATING -> context.getFloatingLayout().iconY();
            default -> context.getPaddingY() + (headerHeight(context) - Constants.getIconSize(context)) / 2;
        };

    }

    public static int headerHeight(TooltipContext context) {
        if (context.getLayoutStyle() == Style.COMPACT) {
            return Math.max(context.hasIcon() ? Constants.getIconSize(context) : 0, context.getComponents().get(0).getHeight());
        }

        if (!context.hasIcon() || context.getLayoutStyle() == Style.FLOATING) {
            return context.getComponents().get(0).getHeight() + ratingHeight(context);
        }

        // Small icons can end up shorter than the title and rating so the header grows and the icon gets centered in it
        return Math.max(Constants.getIconSize(context), context.getComponents().get(0).getHeight() + ratingHeight(context));
    }

}