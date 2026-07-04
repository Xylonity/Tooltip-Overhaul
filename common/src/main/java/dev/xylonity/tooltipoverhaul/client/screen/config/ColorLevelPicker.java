package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static dev.xylonity.tooltipoverhaul.client.screen.config.TooltipOverhaulConfigScreen.*;

/**
 * HSV color picker shared by the config screens
 */
final class ColorLevelPicker {

    interface Commit {
        void accept(int argb);
    }

    private static final int SV_WIDTH = 134;
    private static final int SV_HEIGHT = 84;
    private static final int BAR_HEIGHT = 10;
    private static final int PADDING = 8;

    private final Commit commit;
    private final int accent;

    private final boolean alwaysAlphaHex;

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private float hue;
    private float saturation;
    private float value;
    private int alpha;

    private final TooltipOverhaulConfigScreen.StyledEditBox hexBox;
    private boolean syncingHex = false;

    // 0 none, 1 sv square, 2 hue bar, 3 alpha bar
    private int dragZone = 0;

    ColorLevelPicker(int anchorX, int anchorY, int minY, int screenWidth, int screenHeight, int accent, int initialArgb, boolean alwaysAlphaHex, Commit commit) {
        this.commit = commit;
        this.accent = accent;
        this.alwaysAlphaHex = alwaysAlphaHex;
        this.width = PADDING * 2 + SV_WIDTH;
        this.height = PADDING + SV_HEIGHT + 6 + BAR_HEIGHT + 6 + BAR_HEIGHT + 8 + 16 + PADDING;
        this.x = Mth.clamp(anchorX - width - 6, 4, Math.max(4, screenWidth - width - 4));
        this.y = Mth.clamp(anchorY - height / 2, minY, Math.max(minY, screenHeight - height - 4));

        final float[] hsv = rgbToHsv(initialArgb & 0x00FFFFFF);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        this.alpha = initialArgb >>> 24;

        this.hexBox = new TooltipOverhaulConfigScreen.StyledEditBox(Minecraft.getInstance().font, x + PADDING, y + height - PADDING - 16, SV_WIDTH - 24, 16, Component.empty(), accent);
        this.hexBox.setMaxLength(9);
        this.hexBox.setValue(currentHex());
        this.hexBox.setResponder(this::onHexTyped);
    }

    private int svX() {
        return x + PADDING;
    }

    private int svY() {
        return y + PADDING;
    }

    private int hueY() {
        return svY() + SV_HEIGHT + 6;
    }

    private int alphaY() {
        return hueY() + BAR_HEIGHT + 6;
    }

    private int argb() {
        return (alpha << 24) | hsvToRgb(hue, saturation, value);
    }

    private String currentHex() {
        if (alwaysAlphaHex) {
            return String.format("#%08X", argb());
        }

        return alpha == 0xFF ? String.format("#%06X", argb() & 0x00FFFFFF) : String.format("#%08X", argb());
    }

    private void onHexTyped(String raw) {
        if (syncingHex) {
            return;
        }

        String hex = raw.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        else if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }

        if (!hex.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
            return;
        }

        final long value = Long.parseLong(hex, 16);
        if (hex.length() == 8) {
            alpha = (int) ((value >> 24) & 0xFF);
        }

        final float[] hsv = rgbToHsv((int) (value & 0x00FFFFFF));
        // Grays lose their hue in the conversion
        if (hsv[1] > 0f) {
            hue = hsv[0];
        }

        saturation = hsv[1];
        this.value = hsv[2];

        commit();
    }

    private void syncHex() {
        syncingHex = true;
        hexBox.setValue(currentHex());
        syncingHex = false;
    }

    private void commit() {
        commit.accept(argb());
    }

    void render(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);

        drawCard(graphics, x, y, x + width, y + height, 0xF8101012, withAlpha(dimAccent(accent), 0xFF));

        // Color level square
        for (int i = 0; i < SV_WIDTH; i++) {
            final int top = 0xFF000000 | hsvToRgb(hue, (float) i / (SV_WIDTH - 1), 1f);
            graphics.fillGradient(svX() + i, svY(), svX() + i + 1, svY() + SV_HEIGHT, top, 0xFF000000);
        }

        final int svCursorX = svX() + (int) (saturation * (SV_WIDTH - 1));
        final int svCursorY = svY() + (int) ((1f - value) * (SV_HEIGHT - 1));
        graphics.fill(svCursorX - 2, svCursorY - 2, svCursorX + 3, svCursorY + 3, 0xFFFFFFFF);
        graphics.fill(svCursorX - 1, svCursorY - 1, svCursorX + 2, svCursorY + 2, 0xFF000000 | hsvToRgb(hue, saturation, value));

        // Hue bar
        for (int i = 0; i < SV_WIDTH; i++) {
            graphics.fill(svX() + i, hueY(), svX() + i + 1, hueY() + BAR_HEIGHT, 0xFF000000 | hsvToRgb((float) i / (SV_WIDTH - 1) * 360f, 1f, 1f));
        }

        final int hueCursorX = svX() + (int) (hue / 360f * (SV_WIDTH - 1));
        graphics.fill(hueCursorX - 1, hueY() - 1, hueCursorX + 2, hueY() + BAR_HEIGHT + 1, 0xFFFFFFFF);
        graphics.fill(hueCursorX, hueY(), hueCursorX + 1, hueY() + BAR_HEIGHT, 0xFF000000 | hsvToRgb(hue, 1f, 1f));

        // Alpha bar
        final int rgb = hsvToRgb(hue, saturation, value);
        for (int i = 0; i < SV_WIDTH; i++) {
            final float t = (float) i / (SV_WIDTH - 1);
            graphics.fill(svX() + i, alphaY(), svX() + i + 1, alphaY() + BAR_HEIGHT, 0xFF000000 | mixRgb(0x232327, rgb, t));
        }

        final int alphaCursorX = svX() + (int) (alpha / 255f * (SV_WIDTH - 1));
        graphics.fill(alphaCursorX - 1, alphaY() - 1, alphaCursorX + 2, alphaY() + BAR_HEIGHT + 1, 0xFFFFFFFF);
        graphics.fill(alphaCursorX, alphaY(), alphaCursorX + 1, alphaY() + BAR_HEIGHT, 0xFF17171A);

        hexBox.render(graphics, mouseX, mouseY, 0);

        final int swatchX = x + width - PADDING - 16;
        final int swatchY = y + height - PADDING - 16;
        graphics.fill(swatchX, swatchY, swatchX + 16, swatchY + 16, 0xFF2E2E32);
        graphics.fill(swatchX + 1, swatchY + 1, swatchX + 15, swatchY + 15, 0xFF000000 | mixRgb(0x151517, rgb, alpha / 255f));

        graphics.pose().popPose();
    }

    /**
     * Returns false when the click landed outside, which should dismiss this picker
     */
    boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
            return false;
        }

        if (button != 0) {
            return true;
        }

        if (mouseY >= svY() && mouseY < svY() + SV_HEIGHT && mouseX >= svX() && mouseX < svX() + SV_WIDTH) {
            dragZone = 1;
        }
        else if (mouseY >= hueY() - 2 && mouseY < hueY() + BAR_HEIGHT + 2 && mouseX >= svX() && mouseX < svX() + SV_WIDTH) {
            dragZone = 2;
        }
        else if (mouseY >= alphaY() - 2 && mouseY < alphaY() + BAR_HEIGHT + 2 && mouseX >= svX() && mouseX < svX() + SV_WIDTH) {
            dragZone = 3;
        }

        if (dragZone != 0) {
            hexBox.setFocused(false);
            update(mouseX, mouseY);
            return true;
        }

        hexBox.setFocused(hexBox.mouseClicked(mouseX, mouseY, button));

        return true;
    }

    boolean mouseDragged(double mouseX, double mouseY) {
        if (dragZone == 0) {
            return true;
        }

        update(mouseX, mouseY);
        return true;
    }

    void mouseReleased() {
        if (dragZone != 0) {
            dragZone = 0;
            commit();
        }

    }

    private void update(double mouseX, double mouseY) {
        final float tx = Mth.clamp((float) (mouseX - svX()) / (SV_WIDTH - 1), 0f, 1f);

        if (dragZone == 1) {
            saturation = tx;
            value = 1f - Mth.clamp((float) (mouseY - svY()) / (SV_HEIGHT - 1), 0f, 1f);
        }
        else if (dragZone == 2) {
            hue = tx * 360f;
        }
        else if (dragZone == 3) {
            alpha = Math.round(tx * 255f);
        }

        syncHex();
    }

    boolean keyPressed(int key, int scancode, int modifiers) {
        if (hexBox.isFocused()) {
            hexBox.keyPressed(key, scancode, modifiers);
            return true;
        }

        return false;
    }

    boolean charTyped(char chr, int modifiers) {
        if (hexBox.isFocused()) {
            hexBox.charTyped(chr, modifiers);
        }

        return true;
    }

    void tick() {
        ;;
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        final float c = value * saturation;
        final float hh = ((hue % 360f) + 360f) % 360f / 60f;
        final float xx = c * (1f - Math.abs(hh % 2f - 1f));

        float r = 0;
        float g = 0;
        float b = 0;
        if (hh < 1f) {
            r = c;
            g = xx;
        }
        else if (hh < 2f) {
            r = xx;
            g = c;
        }
        else if (hh < 3f) {
            g = c;
            b = xx;
        }
        else if (hh < 4f) {
            g = xx;
            b = c;
        }
        else if (hh < 5f) {
            r = xx;
            b = c;
        }
        else {
            r = c;
            b = xx;
        }

        final float m = value - c;
        final int ri = Math.round((r + m) * 255f);
        final int gi = Math.round((g + m) * 255f);
        final int bi = Math.round((b + m) * 255f);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static float[] rgbToHsv(int rgb) {
        final float r = ((rgb >> 16) & 0xFF) / 255f;
        final float g = ((rgb >> 8) & 0xFF) / 255f;
        final float b = (rgb & 0xFF) / 255f;

        final float max = Math.max(r, Math.max(g, b));
        final float min = Math.min(r, Math.min(g, b));
        final float delta = max - min;

        float hue = 0;
        if (delta > 0) {
            if (max == r) {
                hue = 60f * (((g - b) / delta) % 6f);
            }
            else if (max == g) {
                hue = 60f * ((b - r) / delta + 2f);
            }
            else {
                hue = 60f * ((r - g) / delta + 4f);
            }

        }

        if (hue < 0) {
            hue += 360f;
        }

        return new float[] { hue, max == 0 ? 0 : delta / max, max };
    }

}
