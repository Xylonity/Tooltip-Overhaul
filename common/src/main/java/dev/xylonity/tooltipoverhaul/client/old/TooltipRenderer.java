package dev.xylonity.tooltipoverhaul.client.old;

import dev.xylonity.tooltipoverhaul.client.old.layer.ITooltipLayer;
import dev.xylonity.tooltipoverhaul.client.old.layer.impl.*;
import dev.xylonity.tooltipoverhaul.client.old.style.Styles;
import dev.xylonity.tooltipoverhaul.client.old.style.TooltipStyle;
import dev.xylonity.tooltipoverhaul.compat.apotheosis.ApotheosisHook;
import dev.xylonity.tooltipoverhaul.config.TooltipsConfig;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameData;
import dev.xylonity.tooltipoverhaul.client.old.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.util.TextAxis;
import dev.xylonity.tooltipoverhaul.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core renderer bridge. To clarify, the tooltips are built using different layers which serve as an abstraction call to the
 * actual rendering methods, which actually render the content. These layers do computee the general settings of where
 * the rendering should be cast on, while the renderers should just care about rendering the component
 */
@SuppressWarnings("unchecked")
public final class TooltipRenderer {

    // Default extra padding for the main tooltip (components aren't aligned automatically)
    public static int PADDING_X = TooltipsConfig.MAIN_PANEL_PADDING_X;
    public static int PADDING_Y = TooltipsConfig.MAIN_PANEL_PADDING_Y;

    // Main chrono for animations and such
    public static float ELAPSED;

    // Default tooltip style, which will be defaulted to a rarity one if a custom frame definition is not present in the stack
    private static @Nullable TooltipStyle style;
    private static ItemStack lastStack = ItemStack.EMPTY;
    private static long startMs;

    // Caches the stack again to prevent the style from the hovered stack to inherit values from the comparation tooltip
    private static ItemStack styleStack = ItemStack.EMPTY;

    // Shared clock to prevent stutter when rendering two diff icons
    private static boolean COMPARISON_PAIR_ACTIVE = false;

    // Main tooltip (and second panel) layers
    private static final List<ITooltipLayer> LAYERS_MAIN = new ArrayList<>();
    // Empty stack tooltip layers
    private static final List<ITooltipLayer> LAYERS_EMPTY = new ArrayList<>();
    // Second panel layers
    private static final List<ITooltipLayer> LAYERS_SECOND = new ArrayList<>();
    // Equipment comparison
    private static final List<ITooltipLayer> LAYERS_EQUIPPED_BADGE = new ArrayList<>();

    // Scrolling predicates
    public static int LAST_HEADER_ABS;
    public static int LAST_POS_YI;

    private static Rectangle LAST_MAIN_RECT = null;
    private static int LAST_SHARED_Y = -1;

    static {
        // Main panel
        LAYERS_MAIN.add(new BackgroundLayer());
        LAYERS_MAIN.add(new IconBackgroundLayer());
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

    static {
        // Equipped badge above the comparison tooltip
        LAYERS_EQUIPPED_BADGE.add(new EquippedBadgeBackgroundLayer());
        LAYERS_EQUIPPED_BADGE.add(new EquippedBadgeTextLayer());
        LAYERS_EQUIPPED_BADGE.add(new EquippedBadgeInnerFrameLayer());
    }

    /**
     * Core renderer for the tooltips, for both main and empty stack tooltips
     * @param ctx contains all the relevant info from the rendering context
     * @return true if the custom tooltip should be shown, false if not
     */
    public static boolean render(TooltipContext ctx) {
        if (ApotheosisHook.isActive()) return false;

        // Reassign padding
        PADDING_X = Util.getMainPanelPadding(ctx, TextAxis.X);
        PADDING_Y = Util.getMainPanelPadding(ctx, TextAxis.Y);

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

        if (ctx.isMainTooltip() && ctx.getOtherTooltipContext() != null) {
            COMPARISON_PAIR_ACTIVE = true;
        }

        // Updates the current tooltip style based on the stack rarity (or the customFrameData if present)
        updateStyle(ctx, customFrame.orElse(null));
        // Passes the rendering context if there is any error computing the style
        if (hasIcon && style == null) return false;

        Font font = Minecraft.getInstance().font;
        // The item rating qualifier. Defaults to the stack's rarity unless a custom itemRating is specified
        Component rating = hasIcon ? computeRating(customFrame, ctx) : Component.empty();

        // Approximation of the tooltip size (knowing there could be or not an icon)
        Point size = calculateSize(font, components, rating, hasIcon, ctx);

        int margin = 4;
        int spacing = 16;
        int half = ctx.width() / 2;

        // margin right
        int xRight = ctx.mouseX() + 12;
        // margin left
        int xLeft = ctx.mouseX() - 16 - size.x;

        boolean comparisonActive = ctx.getOtherTooltipContext() != null;

        int height = Math.min(size.y, ctx.height() - 8);

        int sharedY = -1;
        if (comparisonActive) {
            TooltipContext other = ctx.getOtherTooltipContext();
            int maxHeight = Math.max(height, Math.min(calculateSize(font, (List<ClientTooltipComponent>) other.getComponents(), Component.empty(), !other.stack().isEmpty(), other).y, other.height() - 8));
            sharedY = Math.max(margin, Math.min(ctx.mouseY() - 12, ctx.height() - maxHeight - margin));
        }

        boolean thisHalf = ctx.isHalfWrapped();
        boolean mainHalf = comparisonActive && (ctx.isMainTooltip() ? ctx.isHalfWrapped() : ctx.getOtherTooltipContext().isHalfWrapped());

        // Hardly moves the second tooltip to the left (so it doesn't overlap with the main tooltip in case it's too large)
        int x;
        if (ctx.isMainTooltip()) {
            // Main tooltip positioning
            if (thisHalf) {
                // If half-wrapped, constrains to the right half of the screen
                int minX = half + margin;
                int possibleX = xRight;
                if (possibleX < minX) {
                    possibleX = minX;
                }

                x = Math.max(minX, Math.min(possibleX, ctx.width() - margin - size.x));
            }
            else {
                // Normal positioning (right -> left -> clamps)
                if (xRight + size.x <= ctx.width() - margin) {
                    x = xRight;
                }
                else if (xLeft >= margin) {
                    x = xLeft;
                }
                else {
                    x = Math.max(margin, ctx.width() - size.x - margin);
                }

            }

            // Adjusts the main tooltip position if it'd overlap with the secondary tooltip
            if (comparisonActive) {
                TooltipContext secondaryCtx = ctx.getOtherTooltipContext();

                int secondaryCtxWidth = calculateSize(font, (List<ClientTooltipComponent>) secondaryCtx.getComponents(), Component.empty(), !secondaryCtx.stack().isEmpty(), secondaryCtx).x;

                // Ensures minimum space for the secondary tooltip
                if (!(thisHalf && secondaryCtx.isHalfWrapped())) {
                    if ((x - spacing - secondaryCtxWidth) < margin) {
                        // Moves the main tooltip to make room
                        int newMainX = margin + secondaryCtxWidth + spacing;
                        if (thisHalf) {
                            newMainX = Math.max(newMainX, half + margin);
                        }

                        newMainX = Math.min(newMainX, ctx.width() - margin - size.x);
                        x = newMainX;
                    }
                }
            }
        }
        else if (comparisonActive && LAST_MAIN_RECT != null) {
            // Secondary tooltip positioning
            if (thisHalf && mainHalf) {
                int desiredLeft = LAST_MAIN_RECT.x - spacing - size.x;
                int maxLeftX = (half - margin) - size.x;
                x = Math.max(margin, Math.min(desiredLeft, maxLeftX));
            }
            else {
                // Position to the left of the main tooltip
                x = Math.max(LAST_MAIN_RECT.x - spacing - size.x, margin);
            }

        }
        else {
            if (xRight + size.x <= ctx.width() - margin) {
                x = xRight;
            }
            else if (xLeft >= margin) {
                x = xLeft;
            }
            else {
                x = Math.max(margin, ctx.width() - size.x - margin);
            }

        }

        Vec2 pos;
        if (comparisonActive) {
            if (ctx.isMainTooltip()) {
                LAST_SHARED_Y = sharedY;
                pos = new Vec2(x, LAST_SHARED_Y);
            }
            else {
                pos = new Vec2(x, (LAST_SHARED_Y >= 0) ? LAST_SHARED_Y : Math.max(margin, Math.min(ctx.mouseY() - 12, ctx.height() - Math.min(size.y, ctx.height() - 8) - margin)));
            }
        }
        else {
            // Clamps to screen (if it doesn't fit on either side)
            int y = Math.max(margin, Math.min(ctx.mouseY() - 12, ctx.height() - height - margin));
            pos = new Vec2(x, y);
            LAST_SHARED_Y = -1;
        }

        LAST_POS_YI = Math.round(pos.y);

        if (size.y > height) {
            int content = 0;
            for (int i = 1; i < components.size(); i++) {
                content += components.get(i).getHeight();
            }

            if (Util.isScrollingDisabled(ctx)) {
                TooltipScrollState.reset();
            }
            else {
                TooltipScrollState.begin(content, Math.max(0, height - LAST_HEADER_ABS - (PADDING_Y + 3) - 7));
                TooltipScrollState.tick();
            }
        }
        else {
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

        // Renders the equipped badge qualifier above the compared stack's tooltip
        if (ctx.getOtherTooltipContext() != null && !ctx.isMainTooltip()) {
            for (ITooltipLayer layer : LAYERS_EQUIPPED_BADGE) {
                layer.render(ctx, pos, ttSize, style, rating, font, customFrame.orElse(null));
            }

        }

        ctx.flush();

        if (ctx.isMainTooltip()) {
            LAST_MAIN_RECT = new Rectangle(Math.round(pos.x), Math.round(pos.y), ttSize.x, ttSize.y);
        }
        else {
            if (COMPARISON_PAIR_ACTIVE) {
                COMPARISON_PAIR_ACTIVE = false;
            }

        }

        TooltipScrollState.resetIfInactive();
        return true;
    }

    /**
     * Calculates the item rating qualifier based on the presence of a customFrame definition. Defaults to the
     * stack's rarity
     */
    private static Component computeRating(Optional<CustomFrameData> customFrame, TooltipContext ctx) {
        final Rarity r = ctx.stack().getRarity();
        // Computes the default color per rarity
        // Defaults to a simulated legendary rarity
        ChatFormatting color = ChatFormatting.GOLD;
        if (r == Rarity.COMMON) color = ChatFormatting.GRAY;
        if (r == Rarity.UNCOMMON) color = ChatFormatting.YELLOW;
        if (r == Rarity.RARE) color = ChatFormatting.BLUE;
        if (r == Rarity.EPIC) color = ChatFormatting.DARK_PURPLE;

        // If the curent stack is declared in a custom frame format
        if (customFrame.isPresent()) {
            CustomFrameData data = customFrame.get();

            // If the stack has a custom rating
            if (data.hasCustomItemRating()) {

                // Computes the rating, either as a translatable key or a literal component
                String raw = String.valueOf(data.getItemRating(ctx.stack()));
                MutableComponent base = raw.startsWith("key.tooltipoverhaul") ? Component.translatable(raw) : Component.literal(raw);

                if (data.hasCustomColorItemRating()) {

                }
                // If the frame doesn't provide a color, uses rarity color by default
                else {
                    return base.withStyle(color);
                }

            }

        }

        return Util.getDefaultRarity(ctx.stack()).copy().withStyle(color);
    }

    /**
     * Computes the main tooltip size. Most of the proportions (in general) are hardcoded
     * TODO: Rewrite this whole method and compute modulus positions in a different way
     */
    private static Point calculateSize(Font font, List<ClientTooltipComponent> components, Component rarity, boolean hasIcon, TooltipContext ctx) {
        // Is there an icon (stack) present
        int iconOffset = hasIcon && !Util.shouldDisableIcon(ctx.stack()) ? 26 : 0;
        int width = PADDING_X * 2 + iconOffset + components.get(0).getWidth(font);

        for (ClientTooltipComponent component : components) {
            width = Math.max(width, PADDING_X * 2 + component.getWidth(font));
        }

        if (hasIcon) {
            width = Math.max(width, PADDING_X * 2 + iconOffset + (Util.shouldShowRating(ctx.stack()) ? font.width(rarity) : 0));
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

        if (Util.shouldDisableDividerLine(ctx) && components.size() > 1) {
            height -= 6;
        }

        return new Point(width, height);
    }

    private static void updateStyle(TooltipContext ctx, @Nullable CustomFrameData data) {
        ItemStack stack = ctx.stack();
        if (!((stack.isEmpty() && lastStack.isEmpty()) || (!stack.isEmpty() && !lastStack.isEmpty() && ItemStack.isSameItemSameTags(stack, lastStack)))) {
            style = Styles.of(stack, data).orElse(null);
            styleStack = stack.copy();

            if (!(COMPARISON_PAIR_ACTIVE && !ctx.isMainTooltip())) {
                lastStack = stack.copy();
                startMs = System.currentTimeMillis();
                TooltipScrollState.reset();
            }
        }
        else {
            if (style == null || (styleStack.isEmpty() && !stack.isEmpty()) || (!styleStack.isEmpty() && !ItemStack.isSameItemSameTags(styleStack, stack))) {
                style = Styles.of(stack, data).orElse(null);
                styleStack = stack.copy();
            }

        }

        ELAPSED = (System.currentTimeMillis() - startMs) / 1000f;
    }

}
