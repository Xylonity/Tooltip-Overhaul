package dev.xylonity.tooltipoverhaul.client.screen.config;

import java.util.List;

final class ConfigReorderLayout {

    static int destinationIndex(List<Integer> heights, int draggedIndex, double contentY, int topPadding) {
        int y = topPadding;
        int destination = 0;
        for (int index = 0; index < heights.size(); index++) {
            if (index == draggedIndex) {
                continue;
            }

            final int height = heights.get(index);
            if (contentY < y + height / 2.0) {
                return destination;
            }

            y += height;
            destination++;
        }

        return Math.max(0, heights.size() - 1);
    }

    static int itemOffset(List<Integer> heights, int itemIndex, int draggedIndex, int destinationIndex, int topPadding) {
        int y = topPadding;
        int position = 0;
        final int draggedHeight = heights.get(draggedIndex);
        for (int index = 0; index < heights.size(); index++) {
            if (index == draggedIndex) {
                continue;
            }

            if (position == destinationIndex) {
                y += draggedHeight;
            }

            if (index == itemIndex) {
                return y;
            }

            y += heights.get(index);
            position++;
        }

        return y;
    }

    static int gapOffset(List<Integer> heights, int draggedIndex, int destinationIndex, int topPadding) {
        int y = topPadding;
        int position = 0;
        for (int index = 0; index < heights.size(); index++) {
            if (index == draggedIndex) {
                continue;
            }

            if (position == destinationIndex) {
                return y;
            }

            y += heights.get(index);
            position++;
        }

        return y;
    }

}
