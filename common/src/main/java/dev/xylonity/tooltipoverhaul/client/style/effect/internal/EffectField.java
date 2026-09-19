package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

public enum EffectField {

    OPAL(0.9f, 24000, 320, false),
    PRISM(0.85f, 30000, 360, false),
    AURORA(1, 18000, 300, true),
    FLUORITE(1, 18000, 300, true),
    ASTRAL(1, 18000, 300, true);

    private final float minimumStep;
    private final int cells, longestSide;
    private final boolean uniform;

    EffectField(float minimumStep, int cells, int longestSide, boolean uniform) {
        this.minimumStep = minimumStep;
        this.cells = cells;
        this.longestSide = longestSide;
        this.uniform = uniform;
    }

    public Grid grid(float width, float height) {
        final float step = Math.max(minimumStep, Math.max((float) Math.sqrt(width * height / cells), Math.max(width, height) / longestSide));
        return new Grid(width, height, step, (int) Math.ceil(width / step), (int) Math.ceil(height / step), uniform, minimumStep / step);
    }

    public record Grid(
            float width,
            float height,
            float step,
            int columns,
            int rows,
            boolean uniform,
            float scale
    ) {

        public float x(int column) {
            return uniform ? width * column / columns : Math.min(width, column * step);
        }

        public float y(int row) {
            return uniform ? row / (float) rows * height : Math.min(height, row * step);
        }

        public float pixelSize() {
            return Math.max(width / columns, height / rows) / 144f;
        }

    }

}