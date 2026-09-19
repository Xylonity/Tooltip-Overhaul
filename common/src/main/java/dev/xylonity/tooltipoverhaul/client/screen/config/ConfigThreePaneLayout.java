package dev.xylonity.tooltipoverhaul.client.screen.config;

import net.minecraft.util.Mth;

/**
 * Responsive left/center/right screen layout with independently resizable side panes
 */
final class ConfigThreePaneLayout {

    private final int outerMargin;
    private final int gap;
    private final int top;
    private final int leftBottomInset;
    private final int contentBottomInset;
    private final int minimumLeft;
    private final int minimumCenter;
    private final int minimumRight;

    private int screenWidth;
    private int screenHeight;
    private int leftWidth;
    private int rightWidth = -1;

    ConfigThreePaneLayout(int outerMargin, int gap, int top, int leftBottomInset, int contentBottomInset, int defaultLeftWidth, int minimumLeft, int minimumCenter, int minimumRight) {
        this.outerMargin = outerMargin;
        this.gap = gap;
        this.top = top;
        this.leftBottomInset = leftBottomInset;
        this.contentBottomInset = contentBottomInset;
        this.leftWidth = defaultLeftWidth;
        this.minimumLeft = minimumLeft;
        this.minimumCenter = minimumCenter;
        this.minimumRight = minimumRight;
    }

    void resize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        clampWidths();
    }

    int left() {
        return outerMargin;
    }

    int top() {
        return top;
    }

    int leftBottom() {
        return screenHeight - leftBottomInset;
    }

    int contentBottom() {
        return screenHeight - contentBottomInset;
    }

    int leftWidth() {
        return leftWidth;
    }

    int rightWidth() {
        return rightWidth;
    }

    int rightLeft() {
        return screenWidth - rightWidth - outerMargin;
    }

    int centerLeft() {
        return left() + leftWidth + gap;
    }

    int centerRight() {
        return rightLeft() - gap;
    }

    int leftSplitter() {
        return left() + leftWidth + gap / 2;
    }

    int rightSplitter() {
        return rightLeft() - gap / 2;
    }

    boolean overLeftSplitter(double mouseX, double mouseY) {
        return Math.abs(mouseX - leftSplitter()) <= 3 && mouseY >= top && mouseY < contentBottom();
    }

    boolean overRightSplitter(double mouseX, double mouseY) {
        return Math.abs(mouseX - rightSplitter()) <= 3 && mouseY >= top && mouseY < contentBottom();
    }

    void resizeLeft(double mouseX) {
        int requested = (int) Math.round(mouseX - left() - gap / 2.0);
        leftWidth = Mth.clamp(requested, effectiveMinimumLeft(), Math.max(effectiveMinimumLeft(), availableWidth() - effectiveMinimumCenter() - rightWidth));
        clampWidths();
    }

    void resizeRight(double mouseX) {
        final int requested = (int) Math.round(screenWidth - outerMargin - mouseX - gap / 2.0);
        rightWidth = Mth.clamp(requested, effectiveMinimumRight(), Math.max(effectiveMinimumRight(), availableWidth() - effectiveMinimumCenter() - leftWidth));
        clampWidths();
    }

    private void clampWidths() {
        final int available = availableWidth();
        final int minLeft = effectiveMinimumLeft();
        final int minRight = effectiveMinimumRight();
        final int minCenter = effectiveMinimumCenter();

        if (rightWidth < 0) {
            rightWidth = Mth.clamp((int) (screenWidth * 0.36f), 200, 300);
        }

        leftWidth = Mth.clamp(leftWidth, minLeft, Math.max(minLeft, available - minCenter - minRight));
        rightWidth = Mth.clamp(rightWidth, minRight, Math.max(minRight, available - minCenter - leftWidth));
    }

    private int availableWidth() {
        return Math.max(3, screenWidth - outerMargin * 2 - gap * 2);
    }

    private int effectiveMinimumLeft() {
        return Math.min(minimumLeft, Math.max(1, availableWidth() / 4));
    }

    private int effectiveMinimumRight() {
        return Math.min(minimumRight, Math.max(1, availableWidth() / 3));
    }

    private int effectiveMinimumCenter() {
        return Math.min(minimumCenter, Math.max(1, availableWidth() - effectiveMinimumLeft() - effectiveMinimumRight()));
    }

}
