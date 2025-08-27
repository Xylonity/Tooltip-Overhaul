package dev.xylonity.tooltipoverhaul.client;

import dev.xylonity.tooltipoverhaul.client.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.layer.impl.*;
import dev.xylonity.tooltipoverhaul.client.style.Styles;
import dev.xylonity.tooltipoverhaul.client.style.TooltipStyle;
import dev.xylonity.tooltipoverhaul.compat.apotheosis.ApotheosisHook;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core renderer bridge. To clarify, the tooltips are built using different layers which serve as an abstraction call to the
 * actual rendering methods, which actually render the content. These layers do computee the general settings of where
 * the rendering should be casted on, while the renderers should just care about rendering the component
 */
@SuppressWarnings("unchecked")
public final class TooltipRenderer {

    // Default extra padding for the main tooltip (components aren't aligned automatically)
    public static final int PADDING_X = TooltipsConfig.MAIN_PANEL_PADDING_X;
    public static final int PADDING_Y = TooltipsConfig.MAIN_PANEL_PADDING_Y;

    // Main chrono for animations and such
    public static float ELAPSED;

    // Default tooltip style, which will be defaulted to a rarity one if a custom frame definition is not present in the stack
    private static @Nullable TooltipStyle style;
    private static ItemStack lastStack = ItemStack.EMPTY;
    private static long startMs;

    // Main tooltip (and second panel) layers
    private static final List<ITooltipLayer> LAYERS_MAIN = new ArrayList<>();
    // Empty stack tooltip layers
    private static final List<ITooltipLayer> LAYERS_EMPTY = new ArrayList<>();
    // Second panel layers
    private static final List<ITooltipLayer> LAYERS_SECOND = new ArrayList<>();

    // Scrolling predicates
    public static int LAST_HEADER_ABS;
    public static int LAST_POS_YI;

    static {
        // Main panel
        LAYERS_MAIN.add(new BackgroundLayer());
        LAYERS_MAIN.add(new PreviewBackgroundLayer());
        LAYERS_MAIN.add(new IconLayer());
        LAYERS_MAIN.add(new TextLayer());
        LAYERS_MAIN.add(new DividerLineLayer());
        LAYERS_MAIN.add(new InnerFrameLayer());
        LAYERS_MAIN.add(new EffectLayer());
        LAYERS_MAIN.add(new OverlayLayer());
    }

    static {
        // Second panel
        LAYERS_SECOND.add(new SecondBackgroundLayer());
        LAYERS_SECOND.add(new SecondInnerFrameLayer());
        LAYERS_SECOND.add(new ArmorStandLayer());
        LAYERS_SECOND.add(new RotatingItemLayer());
    }

    static {
        // Main panel for tooltips without a dedicated stack
        LAYERS_EMPTY.add(new BackgroundLayer());
        LAYERS_EMPTY.add(new TextLayer());
        LAYERS_EMPTY.add(new InnerFrameLayer());
    }

    /**
     * Core renderer for the tooltips, for both main and empty stack tooltips
     * @param ctx contains all the relevant info from the rendering context
     * @return true if the custom tooltip should be shown, false if not
     */
    public static boolean render(TooltipContext ctx) {
        if (ApotheosisHook.isActive()) return false;

        // Passes if there is no text present
        List<?> raw = ctx.getComponents();
        if (raw.isEmpty()) return false;

        List<ClientTooltipComponent> components = (List<ClientTooltipComponent>) raw;

        // Is there any stack being detected
        boolean hasIcon = !ctx.stack().isEmpty();
        if (!hasIcon && !TooltipsConfig.SHOW_TOOLTIP_WITHOUT_STACK) return false;

        // Defines custom properties if the current stack is present in a custom_frames configuration file
        Optional<CustomFrameData> customFrame = hasIcon ? CustomFrameManager.of(ctx.stack()) : Optional.empty();

        // Doesn't render the current tooltip if specified
        if (customFrame.isPresent()) {
            if (customFrame.get().shouldDisableTooltip()) return false;
        }

        // Updates the current tooltip style based on the stack rarity (or the customFrameData if present)
        updateStyle(hasIcon ? ctx.stack() : ItemStack.EMPTY, customFrame.orElse(null));
        // Passes the rendering context if there is any error computing the style
        if (hasIcon && style == null) return false;

        Font font = Minecraft.getInstance().font;
        // The item rating qualifier. Defaults to the stack's rarity unless a custom itemRating is specified
        Component rating = hasIcon ? computeRating(customFrame, ctx) : Component.empty();

        // Approximation of the tooltip size (knowing there could be or not an icon)
        Point size = calculateSize(font, components, rating, hasIcon);

        int height = Math.min(size.y, ctx.height() - 8);

        // Start position of the tooltip
        Vec2 pos = new Vec2(Math.min(ctx.mouseX() + 12, ctx.width() - size.x - 4), Math.max(4, Math.min(ctx.mouseY() - 12, ctx.height() - height - 4)));

        LAST_POS_YI = Math.round(pos.y);

        if (size.y > height) {
            int content = 0;
            for (int i = 1; i < components.size(); i++) {
                content += components.get(i).getHeight();
            }

            TooltipScrollState.begin(content, Math.max(0, height - LAST_HEADER_ABS - (PADDING_Y + 3) - 7));
            TooltipScrollState.tick();
        } else {
            TooltipScrollState.reset();
        }

        Point ttSize = new Point(size.x, height);

        // Renders a default non-stack tooltip and passes
        if (!hasIcon) {
            for (ITooltipLayer layer : LAYERS_EMPTY) {
                layer.render(ctx, pos, ttSize, Styles.COMMON.build(), rating, font, null);
            }

            ctx.flush();

            TooltipScrollState.resetIfInactive();
            return true;
        }

        // Renders the main tooltip (and the second panel if specified inside the render layers)
        for (ITooltipLayer layer : LAYERS_MAIN) {
            layer.render(ctx, pos, ttSize, style, rating, font, customFrame.orElse(null));
        }

        // Renders the second panel
        for (ITooltipLayer layer : LAYERS_SECOND) {
            layer.render(ctx, pos, ttSize, style, rating, font, customFrame.orElse(null));
        }

        ctx.flush();

        TooltipScrollState.resetIfInactive();
        return true;
    }

    /**
     * Calculates the item rating qualifier based on the presence of a customFrame definition. Defaults to the
     * stack's rarity
     */
    private static Component computeRating(Optional<CustomFrameData> customFrame, TooltipContext ctx) {
        if (customFrame.isPresent()) {
            CustomFrameData data = customFrame.get();

            String raw = String.valueOf(data.getItemRating(ctx.stack()));
            MutableComponent base = raw.startsWith("key.tooltipoverhaul") ? Component.translatable(raw) : Component.literal(raw);

            // If the frame doesn't provide a color, uses rarity color by default
            if (data.hasCustomColorItemRating()) {
                return base.withStyle(Style.EMPTY.withColor(data.getItemRatingColor(ctx.stack())));
            } else {
                ChatFormatting color = switch (ctx.stack().getRarity()) {
                    case COMMON -> ChatFormatting.GRAY;
                    case UNCOMMON -> ChatFormatting.YELLOW;
                    case RARE -> ChatFormatting.BLUE;
                    case EPIC -> ChatFormatting.DARK_PURPLE;
                    // Defaults to a simulated legendary rarity
                    default -> ChatFormatting.GOLD;
                };
                return base.withStyle(color);
            }

        }

        // If there is no custom frame present, defaults to the rarity
        ChatFormatting color = switch (ctx.stack().getRarity()) {
            case COMMON -> ChatFormatting.GRAY;
            case UNCOMMON -> ChatFormatting.YELLOW;
            case RARE -> ChatFormatting.BLUE;
            case EPIC -> ChatFormatting.DARK_PURPLE;
            // Defaults to a simulated legendary rarity
            default -> ChatFormatting.GOLD;
        };

        String s = ctx.stack().getRarity().name();
        return Component.literal(s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).withStyle(color);
    }

    /**
     * Computes the main tooltip size. Most of the proportions (in general) are hardcoded
     */
    private static Point calculateSize(Font font, List<ClientTooltipComponent> components, Component rarity, boolean hasIcon) {
        // Is there an icon (stack) present
        int iconOffset = hasIcon ? 26 : 0;
        int width = PADDING_X * 2 + iconOffset + components.get(0).getWidth(font);

        for (ClientTooltipComponent c : components) {
            width = Math.max(width, PADDING_X * 2 + c.getWidth(font));
        }

        if (hasIcon) {
            width = Math.max(width, PADDING_X * 2 + iconOffset + font.width(rarity));
        }

        int y0 = PADDING_Y + 3;
        int y = y0;

        for (int i = 0; i < components.size(); i++) {
            // Extra padding if there is an icon present
            if (hasIcon && i == 1) {
                y += 12;
            }

            y += components.get(i).getHeight();

            if (hasIcon && i == 0 && components.size() > 1) {
                y += 6;
            }
        }

        int topPadding = PADDING_Y + 3;
        int yAfterTitle = topPadding + components.get(0).getHeight();

        if (hasIcon && components.size() > 1) {
            yAfterTitle += 6;
        }

        int headAbs = components.size() > 1 ? (yAfterTitle + 3 + (hasIcon ? 12 : 0)) : yAfterTitle;
        if (hasIcon) {
            int headBlock = headAbs - topPadding;
            if (headBlock < 26) headAbs = topPadding + 26;
        }

        LAST_HEADER_ABS = headAbs;

        int height = (PADDING_Y + (y - y0)) + PADDING_Y + 3;
        int minHeight = topPadding + 18 + (PADDING_Y + 3);
        if (hasIcon && height < minHeight) height = minHeight;

        return new Point(width, height);
    }

    private static void updateStyle(ItemStack stack, @Nullable CustomFrameData data) {
        if (!((stack.isEmpty() && lastStack.isEmpty()) || (!stack.isEmpty() && !lastStack.isEmpty() && ItemStack.isSameItemSameTags(stack, lastStack)))) {
            style = Styles.of(stack, data).orElse(null);
            // Overrides the previous stack if the user starts hovering over another item
            lastStack = stack.copy();
            startMs = System.currentTimeMillis();

            TooltipScrollState.reset();
        }

        ELAPSED = (System.currentTimeMillis() - startMs) / 1000f;
    }

}