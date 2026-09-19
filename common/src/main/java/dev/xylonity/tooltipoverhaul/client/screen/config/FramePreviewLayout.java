package dev.xylonity.tooltipoverhaul.client.screen.config;

import java.util.ArrayList;
import java.util.List;

final class FramePreviewLayout {

    static Layout arrange(List<Size> sizes, float width, float height, boolean comparison, double zoom) {
        if (sizes.isEmpty()) {
            return new Layout(List.of(), 1, 0, 0);
        }

        Layout best = null;
        for (int columns = 1; columns <= (comparison ? Math.min(2, sizes.size()) : 1); columns++) {
            final int rows = (sizes.size() + columns - 1) / columns;
            final float[] columnWidths = new float[columns];
            final float[] rowHeights = new float[rows];
            for (int i = 0; i < sizes.size(); i++) {
                columnWidths[i % columns] = Math.max(columnWidths[i % columns], sizes.get(i).width());
                rowHeights[i / columns] = Math.max(rowHeights[i / columns], sizes.get(i).height());
            }

            float gap = comparison ? 28 : 0, contentWidth = gap * (columns - 1), contentHeight = gap * (rows - 1);
            for (float value : columnWidths) {
                contentWidth += value;
            }
            for (float value : rowHeights) {
                contentHeight += value;
            }

            float fit = Math.min(comparison ? 1 : 1.25f, Math.min(Math.max(1, width - 36) / Math.max(1, contentWidth), Math.max(1, height - 36) / Math.max(1, contentHeight)));
            final float scale = fit * (float) zoom;
            if (best != null && best.scale() >= scale) {
                continue;
            }

            final List<Slot> slots = new ArrayList<>();
            float top = (height - contentHeight * scale) * 0.5f;
            for (int row = 0; row < rows; row++) {
                float left = (width - contentWidth * scale) * 0.5f;
                for (int col = 0; col < columns; col++) {
                    final int i = row * columns + col;
                    if (i < sizes.size()) {
                        slots.add(new Slot(left + (columnWidths[col] - sizes.get(i).width()) * scale * 0.5f, top + (rowHeights[row] - sizes.get(i).height()) * scale * 0.5f));
                    }

                    left += (columnWidths[col] + gap) * scale;
                }

                top += (rowHeights[row] + gap) * scale;
            }

            best = new Layout(List.copyOf(slots), scale, contentWidth * scale, contentHeight * scale);
        }

        return best;
    }

    record Size(
            float width,
            float height
    ) {
        ;;
    }

    record Slot(
            float x,
            float y
    ) {
        ;;
    }

    record Layout(
            List<Slot> slots,
            float scale,
            float width,
            float height
    ) {
        ;;
    }

}
