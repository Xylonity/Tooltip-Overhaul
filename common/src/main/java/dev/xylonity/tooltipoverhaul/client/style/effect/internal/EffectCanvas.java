package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Main utility class for geometry creation, where all primitives append to the same triangle batch
 */
public final class EffectCanvas {

    public static final float TAU = (float) (Math.PI * 2);

    public static int color(int channel, int original) {
        return EffectRuntime.color(channel, original);
    }

    public static int effectCount(int reference) {
        return Math.max(0, Math.round(reference * EffectRuntime.density()));
    }

    public static float parameter(EffectParameter parameter) {
        return EffectRuntime.value(parameter);
    }

    public static float patternPhase(int channel) {
        return parameter(EffectParameter.VARIATION) == 0 ? 0 : seed(7919, channel) * TAU;
    }

    private final VertexConsumer buffer;
    private final Matrix4f pose;
    private final Vector4f vertexPosition = new Vector4f();
    private Knot[] ribbonKnots;
    private float[] ribbonNX, ribbonNY;

    public final float left;
    public final float top;
    public final float width;
    public final float height;
    public final float right;
    public final float bottom;
    public final float cx;
    public final float cy;
    public final double time;

    private final float interiorVisibility;
    private final float intensity;
    private final float size;
    private final float glow;
    private final float thickness;
    private final boolean clipped;

    private static final float[] BAND_OFFSETS = {-1, -0.5f, 0, 0.5f, 1};
    private static final float[] BAND_ALPHA = {0, 0.42f, 1, 0.42f, 0};
    private static final GlowMesh SMALL_GLOW = new GlowMesh(12, 3);
    private static final GlowMesh LARGE_GLOW = new GlowMesh(24, 5);
    private static final float[] DOT_X = new float[13], DOT_Y = new float[13];

    static {
        for (int i = 0; i <= 12; i++) {
            final float angle = TAU * i / 12;
            DOT_X[i] = cos(angle);
            DOT_Y[i] = sin(angle);
        }

    }

    public EffectCanvas(VertexConsumer buffer, Matrix4f pose, float x, float y, float width, float height, double time, float interiorVisibility) {
        this(buffer, pose, x, y, width, height, time, interiorVisibility, false);
    }

    public EffectCanvas(VertexConsumer buffer, Matrix4f pose, float x, float y, float width, float height, double time, float interiorVisibility, boolean clipped) {
        this.buffer = buffer;
        this.pose = pose;
        this.left = x;
        this.top = y;
        this.width = width;
        this.height = height;
        this.right = x + width;
        this.bottom = y + height;
        this.cx = x + width * 0.5f;
        this.cy = y + height * 0.5f;
        this.time = time;
        this.interiorVisibility = clamp(interiorVisibility);
        this.intensity = EffectRuntime.intensity();
        this.size = parameter(EffectParameter.SIZE);
        this.glow = parameter(EffectParameter.GLOW);
        this.thickness = parameter(EffectParameter.THICKNESS);
        this.clipped = clipped && pose.m00() > 0 && pose.m11() > 0 && pose.m01() == 0 && pose.m10() == 0 && pose.m02() == 0 && pose.m12() == 0;
    }

    private boolean outside(float x0, float y0, float x1, float y1) {
        return clipped && (x1 < left || y1 < top || x0 > right || y0 > bottom);
    }

    /**
     * Rims with veils can see their transparency modified
     */
    public static float veilOpacity(int color, float opacity, float rim) {
        final float alpha = clamp(opacity);
        final float neutral = Math.min((color >>> 16) & 255, Math.min((color >>> 8) & 255, color & 255)) / 255f;
        final float fill = alpha * 0.70f / (1 + 5 * alpha * neutral);
        return fill + (alpha - fill) * clamp(rim);
    }

    /**
     * Continuous turbulence, whose motion does not change with a different framerate
     */
    public static float noise(double x, double y) {
        final int cellX = (int) Math.floor(x), cellY = (int) Math.floor(y);
        final float fractionX = smooth((float) (x - cellX)), fractionY = smooth((float) (y - cellY));
        final float topLeft = seed(cellX, cellY), topRight = seed(cellX + 1, cellY);
        final float bottomLeft = seed(cellX, cellY + 1), bottomRight = seed(cellX + 1, cellY + 1);
        final float top = topLeft + (topRight - topLeft) * fractionX;
        final float bottom = bottomLeft + (bottomRight - bottomLeft) * fractionX;
        return top * (1 - fractionY) + bottom * fractionY;
    }

    public static float flow(double x, double time) {
        return (noise(x, time) - 0.5f) + (noise(x * 2.07 + 17, time * 1.37) - 0.5f) * 0.45f;
    }

    /**
     * Mostly to specify a particle cap among big tooltips
     */
    public int particleCount(int reference) {
        return Math.max(0, Math.round(reference * Math.max(0.55f, Math.min(1.4f, (width + height) / 184f)) * EffectRuntime.density()));
    }

    /**
     * Stable noise
     */
    public static float seed(int index, int channel) {
        int value = index * 0x1f123bb5 + channel * 0x5f356495 + (int) EffectRuntime.value(EffectParameter.VARIATION) * 0x6c8e9cf5;
        value = (value ^ (value >>> 16)) * 0x45d9f3b;
        value = (value ^ (value >>> 16)) * 0x45d9f3b;
        return ((value ^ (value >>> 16)) & 0x00ffffff) / 16777216f;
    }

    /**
     * Speed travel (constant speed) around a rounded rectangle, starting on the top edge
     */
    public Point edge(double turn, float padding) {
        final float x = left - padding;
        final float y = top - padding;

        final float boxWidth = Math.max(2, width + padding * 2);
        final float boxHeight = Math.max(2, height + padding * 2);

        final float round = Math.min(5 + Math.max(0, padding) * 0.35f, Math.min(boxWidth, boxHeight) * 0.25f);

        final float horizontal = boxWidth - 2 * round, vertical = boxHeight - 2 * round, arc = TAU * round * 0.25f;
        float distance = cycle(turn) * (2 * horizontal + 2 * vertical + 4 * arc);
        if (distance < horizontal) {
            return new Point(x + round + distance, y);
        }

        distance -= horizontal;

        if (distance < arc) {
            return corner(x + boxWidth - round, y + round, round, -TAU * 0.25f + distance / round);
        }

        distance -= arc;

        if (distance < vertical) {
            return new Point(x + boxWidth, y + round + distance);
        }

        distance -= vertical;

        if (distance < arc) {
            return corner(x + boxWidth - round, y + boxHeight - round, round, distance / round);
        }

        distance -= arc;

        if (distance < horizontal) {
            return new Point(x + boxWidth - round - distance, y + boxHeight);
        }

        distance -= horizontal;

        if (distance < arc) {
            return corner(x + round, y + boxHeight - round, round, TAU * 0.25f + distance / round);
        }

        distance -= arc;

        if (distance < vertical) {
            return new Point(x, y + boxHeight - round - distance);
        }

        distance -= vertical;

        return corner(x + round, y + round, round, TAU * 0.5f + distance / round);
    }

    private static Point corner(float x, float y, float radius, float angle) {
        return new Point(x + cos(angle) * radius, y + sin(angle) * radius);
    }

    private float visibility(float x, float y) {
        // Effects are drawn above text, so this limits the visibility within the tooltip margins
        final float inset = Math.min(Math.min(x - left, right - x), Math.min(y - top, bottom - y));
        return (1 - (1 - interiorVisibility) * smooth(inset / 10f)) * smooth((inset + 28) / 12f);
    }

    public void vertex(float x, float y, int color, float opacity) {
        final int alpha = Math.round(((color >>> 24) & 255) * clamp(opacity) * visibility(x, y) * intensity);
        pose.transform(vertexPosition.set(x, y, 0, 1));
        buffer.vertex(vertexPosition.x(), vertexPosition.y(), vertexPosition.z()).color((color >>> 16) & 255, (color >>> 8) & 255, color & 255, alpha).endVertex();
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int color, float alpha1, float alpha2, float alpha3) {
        vertex(x1, y1, color, alpha1);
        vertex(x2, y2, color, alpha2);
        vertex(x3, y3, color, alpha3);
    }

    public void glow(float x, float y, float radius, int color, float alpha) {
        haze(x, y, radius, radius, color, alpha);
    }

    /**
     * A round particle with a solid center
     */
    public void dot(float x, float y, float radius, int color, float alpha) {
        radius *= size;
        if (alpha < 0.002f || radius <= 0) {
            return;
        }

        if (outside(x - radius, y - radius, x + radius, y + radius)) {
            return;
        }

        for (int i = 0; i < 12; i++) {
            final float ax = DOT_X[i] * radius, ay = DOT_Y[i] * radius;
            final float bx = DOT_X[i + 1] * radius, by = DOT_Y[i + 1] * radius;
            triangle(x, y, x + ax * 0.55f, y + ay * 0.55f, x + bx * 0.55f, y + by * 0.55f, color, alpha, alpha, alpha);
            triangle(x + ax * 0.55f, y + ay * 0.55f, x + ax, y + ay, x + bx, y + by, color, alpha, 0, 0);
            triangle(x + ax * 0.55f, y + ay * 0.55f, x + bx, y + by, x + bx * 0.55f, y + by * 0.55f, color, alpha, 0, alpha);
        }

    }

    /**
     * Blurred disc
     */
    public void haze(float x, float y, float rx, float ry, int color, float alpha) {
        rx *= size;
        ry *= size;
        alpha *= glow;
        final float radius = Math.max(rx, ry);
        if (alpha < 0.002f || radius <= 0) {
            return;
        }

        if (outside(x - rx, y - ry, x + rx, y + ry)) {
            return;
        }

        final GlowMesh mesh = radius > 5 ? LARGE_GLOW : SMALL_GLOW;
        for (int i = 0; i < mesh.x.length; i++) {
            vertex(x + mesh.x[i] * rx, y + mesh.y[i] * ry, color, alpha * mesh.alpha[i]);
        }

    }

    /**
     * Generic ribbon trail given a certain length and position
     */
    public void ribbon(int segments, RibbonPath path) {
        if (ribbonKnots == null || ribbonKnots.length < segments + 1) {
            ribbonKnots = new Knot[segments + 1];
            ribbonNX = new float[segments + 1];
            ribbonNY = new float[segments + 1];
        }

        final Knot[] knots = ribbonKnots;
        final float[] nx = ribbonNX, ny = ribbonNY;
        for (int i = 0; i <= segments; i++) {
            knots[i] = path.at(i / (float) segments);
        }

        final boolean closed = Math.hypot(knots[0].x - knots[segments].x, knots[0].y - knots[segments].y) < 0.001;
        if (closed) {
            knots[segments] = knots[0];
        }

        for (int i = 0; i <= segments; i++) {
            final Knot previous = knots[i == 0 && closed ? segments - 1 : Math.max(0, i - 1)];
            final Knot next = knots[i == segments && closed ? 1 : Math.min(segments, i + 1)];
            final float dx = next.x - previous.x;
            final float dy = next.y - previous.y;
            final float length = Math.max(0.0001f, (float) Math.hypot(dx, dy));
            final Knot current = knots[i];
            float width = current.width * thickness;
            final float backX = current.x - previous.x;
            final float backY = current.y - previous.y;
            final float forwardX = next.x - current.x;
            final float forwardY = next.y - current.y;
            final float cross = Math.abs(backX * forwardY - backY * forwardX);
            if (cross > 0.00001f) {
                final float radius = (float) (Math.hypot(backX, backY) * Math.hypot(forwardX, forwardY)) * length / (2 * cross);
                // So the ribbon doesn't overlap itself on the tip
                width = Math.min(width, radius * 0.8f);
            }

            nx[i] = -dy / length * width;
            ny[i] = dx / length * width;
        }

        for (int i = 0; i < segments; i++) {
            final Knot start = knots[i], end = knots[i + 1];
            if (Math.max(start.alpha, end.alpha) < 0.002f) {
                continue;
            }

            final float minX = Math.min(start.x - Math.abs(nx[i]), end.x - Math.abs(nx[i + 1]));
            final float minY = Math.min(start.y - Math.abs(ny[i]), end.y - Math.abs(ny[i + 1]));
            final float maxX = Math.max(start.x + Math.abs(nx[i]), end.x + Math.abs(nx[i + 1]));
            final float maxY = Math.max(start.y + Math.abs(ny[i]), end.y + Math.abs(ny[i + 1]));
            if (outside(minX, minY, maxX, maxY)) {
                continue;
            }

            for (int band = 0; band < 4; band++) {
                final float from = BAND_OFFSETS[band];
                final float to = BAND_OFFSETS[band + 1];
                final float fromAlpha = BAND_ALPHA[band];
                final float toAlpha = BAND_ALPHA[band + 1];
                vertex(start.x + nx[i] * from, start.y + ny[i] * from, start.color, start.alpha * fromAlpha);
                vertex(end.x + nx[i + 1] * from, end.y + ny[i + 1] * from, end.color, end.alpha * fromAlpha);
                vertex(end.x + nx[i + 1] * to, end.y + ny[i + 1] * to, end.color, end.alpha * toAlpha);
                vertex(start.x + nx[i] * from, start.y + ny[i] * from, start.color, start.alpha * fromAlpha);
                vertex(end.x + nx[i + 1] * to, end.y + ny[i + 1] * to, end.color, end.alpha * toAlpha);
                vertex(start.x + nx[i] * to, start.y + ny[i] * to, start.color, start.alpha * toAlpha);
            }

        }

    }

    /**
     * Subpixel mote with a halo around it
     */
    public void mote(float x, float y, float radius, int color, float alpha) {
        glow(x, y, radius * 3.5f, color, alpha * 0.15f);
        radius *= size;
        triangle(x, y - radius, x + radius * 0.65f, y, x, y + radius, color, alpha * 0.25f, alpha, alpha * 0.25f);
        triangle(x, y - radius, x, y + radius, x - radius * 0.65f, y, color, alpha * 0.25f, alpha * 0.25f, alpha);
    }

    /**
     * Generic line with transparent sides
     */
    public void line(float x0, float y0, float x1, float y1, float width0, float width1, int color, float alpha0, float alpha1) {
        width0 *= thickness;
        width1 *= thickness;
        final float dx = x1 - x0;
        final float dy = y1 - y0;
        final float length = (float) Math.hypot(dx, dy);
        if (length < 0.0001f) {
            return;
        }

        final float nx = -dy / length;
        final float ny = dx / length;
        for (int side = -1; side <= 1; side += 2) {
            final float ax = x0 + nx * width0 * side;
            final float ay = y0 + ny * width0 * side;
            final float bx = x1 + nx * width1 * side;
            final float by = y1 + ny * width1 * side;
            triangle(x0, y0, ax, ay, bx, by, color, alpha0, 0, 0);
            triangle(x0, y0, bx, by, x1, y1, color, alpha0, 0, alpha1);
        }

    }

    /**
     * 4 point star/sparkling mote
     */
    public void sparkle(float x, float y, float radius, float rotation, int color, float alpha) {
        glow(x, y, radius * 1.8f, color, alpha * 0.22f);

        radius *= size;

        for (int i = 0; i < 4; i++) {
            final float angle = rotation + TAU * i / 4;
            final float dx = cos(angle);
            final float dy = sin(angle);
            final float length = radius * (i % 2 == 0 ? 1 : 0.72f);
            triangle(x - dy * radius * 0.16f, y + dx * radius * 0.16f, x + dx * length, y + dy * length,
                    x + dy * radius * 0.16f, y - dx * radius * 0.16f, color, alpha, 0, alpha);
        }

        glow(x, y, 0.9f, 0xFFFFFFFF, alpha);
    }

    /**
     * Star with a certain amount of outer edges (points)
     */
    public void star(float x, float y, float radius, int points, float inner, float rotation, int color, float alpha) {
        glow(x, y, radius * 1.8f, color, alpha * 0.15f);
        radius *= size;
        for (int i = 0; i < points * 2; i++) {
            final float from = rotation + TAU * i / (points * 2);
            final float to = rotation + TAU * (i + 1) / (points * 2);
            final float fromRadius = radius * (i % 2 == 0 ? 1 : inner);
            final float toRadius = radius * (i % 2 == 0 ? inner : 1);
            triangle(x, y, x + cos(from) * fromRadius, y + sin(from) * fromRadius, x + cos(to) * toRadius, y + sin(to) * toRadius,
                    color, alpha, alpha * (i % 2 == 0 ? 0.08f : 0.55f), alpha * (i % 2 == 0 ? 0.55f : 0.08f));
        }

        glow(x, y, 0.65f, 0xFFFFFFFF, alpha);
    }

    /**
     * Curved arc for borders, rings and similar shapes
     */
    public void arc(float x, float y, float rx, float ry, float start, float sweep, float thickness, int color, float alpha) {
        final int segments = Math.max(8, Math.min(128, (int) (Math.max(rx, ry) * Math.abs(sweep) / 2)));
        for (int i = 0; i < segments; i++) {
            final float from = start + sweep * i / segments;
            final float to = start + sweep * (i + 1) / segments;
            line(x + cos(from) * rx, y + sin(from) * ry, x + cos(to) * rx, y + sin(to) * ry, thickness, thickness, color, alpha, alpha);
        }

    }

    public static float sin(double angle) {
        return (float) Math.sin(angle);
    }

    public static float cos(double angle) {
        return (float) Math.cos(angle);
    }

    public static float cycle(double value) {
        return (float) (value - Math.floor(value));
    }

    public static float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    public static float smooth(float value) {
        final float clamped = clamp(value);
        return clamped * clamped * (3 - 2 * clamped);
    }

    public static float life(float phase) {
        return smooth(phase / 0.16f) * smooth((1 - phase) / 0.32f);
    }

    public static float bell(float phase) {
        final float curve = sin(clamp(phase) * Math.PI);
        return curve * curve;
    }

    public static int mix(int from, int to, float amount) {
        final float blend = clamp(amount);
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            result |= Math.round(((from >>> shift) & 255) * (1 - blend) + ((to >>> shift) & 255) * blend) << shift;
        }

        return result;
    }

    /**
     * Radial mesh meant for glow simulation
     */
    public static final class GlowMesh {

        public final float[] x;
        public final float[] y;
        public final float[] alpha;

        private int cursor;

        public GlowMesh(int segments, int rings) {
            final int vertices = segments * (rings * 6 - 3);
            x = new float[vertices];
            y = new float[vertices];
            alpha = new float[vertices];

            for (int ring = 0; ring < rings; ring++) {
                final float inner = ring / (float) rings;
                final float outer = (ring + 1f) / rings;
                for (int i = 0; i < segments; i++) {
                    final float from = TAU * i / segments;
                    final float to = TAU * (i + 1) / segments;

                    add(from, inner);
                    add(from, outer);
                    add(to, outer);

                    if (ring > 0) {
                        add(from, inner);
                        add(to, outer);
                        add(to, inner);
                    }

                }

            }

        }

        private void add(float angle, float radius) {
            x[cursor] = cos(angle) * radius;
            y[cursor] = sin(angle) * radius;
            alpha[cursor++] = Math.max(0, ((float) Math.exp(-5 * radius * radius) - 0.006738f) / 0.993262f);
        }

    }

    public record Knot(
            float x,
            float y,
            float width,
            int color,
            float alpha
    ) {
        ;;
    }

    public record Point(
            float x,
            float y
    ) {
        ;;
    }

    @FunctionalInterface
    public interface RibbonPath {
        Knot at(float along);
    }

}