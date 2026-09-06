package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.joml.Matrix4f;

/**
 * Main utility class for geometry creation, where all primitives append to the same triangle batch
 */
public final class EffectCanvas {

    public static final float TAU = (float) (Math.PI * 2);

    private final BufferBuilder buffer;
    private final Matrix4f pose;

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

    private static final float[] BAND_OFFSETS = {-1, -0.5f, 0, 0.5f, 1};
    private static final float[] BAND_ALPHA = {0, 0.42f, 1, 0.42f, 0};
    private static final GlowMesh SMALL_GLOW = new GlowMesh(12, 3);
    private static final GlowMesh LARGE_GLOW = new GlowMesh(24, 5);

    public EffectCanvas(BufferBuilder buffer, Matrix4f pose, float x, float y, float width, float height, double time, float interiorVisibility) {
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
        final int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        final float u = smooth((float) (x - ix)), v = smooth((float) (y - iy));
        final float a = seed(ix, iy), b = seed(ix + 1, iy);
        final float d = seed(ix, iy + 1), e = seed(ix + 1, iy + 1);
        return (a + (b - a) * u) * (1 - v) + (d + (e - d) * u) * v;
    }

    public static float flow(double x, double time) {
        return (noise(x, time) - 0.5f) + (noise(x * 2.07 + 17, time * 1.37) - 0.5f) * 0.45f;
    }

    /**
     * Mostly to specify a particle cap among big tooltips
     */
    public int particleCount(int reference) {
        return Math.max(1, Math.round(reference * Math.max(0.55f, Math.min(1.4f, (width + height) / 184f))));
    }

    /// Stable noise
    public static float seed(int index, int channel) {
        int value = index * 0x1f123bb5 + channel * 0x5f356495;
        value = (value ^ (value >>> 16)) * 0x45d9f3b;
        value = (value ^ (value >>> 16)) * 0x45d9f3b;
        return ((value ^ (value >>> 16)) & 0x00ffffff) / 16777216f;
    }

    /**
     * Speed travel (constant speed) around a rounded rectangle, starting on the top edge
     */
    public Point edge(double turn, float padding) {
        final float x = left - padding, y = top - padding;
        final float w = Math.max(2, width + padding * 2), h = Math.max(2, height + padding * 2);
        final float round = Math.min(5 + Math.max(0, padding) * 0.35f, Math.min(w, h) * 0.25f);
        final float horizontal = w - 2 * round, vertical = h - 2 * round, arc = TAU * round * 0.25f;
        float distance = cycle(turn) * (2 * horizontal + 2 * vertical + 4 * arc);
        if (distance < horizontal) {
            return new Point(x + round + distance, y);
        }

        distance -= horizontal;

        if (distance < arc) {
            return corner(x + w - round, y + round, round, -TAU * 0.25f + distance / round);
        }
        distance -= arc;

        if (distance < vertical) {
            return new Point(x + w, y + round + distance);
        }

        distance -= vertical;

        if (distance < arc) {
            return corner(x + w - round, y + h - round, round, distance / round);
        }

        distance -= arc;

        if (distance < horizontal) {
            return new Point(x + w - round - distance, y + h);
        }

        distance -= horizontal;

        if (distance < arc) {
            return corner(x + round, y + h - round, round, TAU * 0.25f + distance / round);
        }

        distance -= arc;

        if (distance < vertical) {
            return new Point(x, y + h - round - distance);
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
        final int alpha = Math.round(((color >>> 24) & 255) * clamp(opacity) * visibility(x, y));
        buffer.vertex(pose, x, y, 0).color((color >>> 16) & 255, (color >>> 8) & 255, color & 255, alpha).endVertex();
    }

    public void triangle(float ax, float ay, float bx, float by, float cx, float cy, int color, float a, float b, float c) {
        vertex(ax, ay, color, a);
        vertex(bx, by, color, b);
        vertex(cx, cy, color, c);
    }

    public void glow(float x, float y, float radius, int color, float alpha) {
        haze(x, y, radius, radius, color, alpha);
    }

    /**
     * A round particle with a solid center
     */
    public void dot(float x, float y, float radius, int color, float alpha) {
        if (alpha < 0.002f || radius <= 0) {
            return;
        }

        for (int i = 0; i < 12; i++) {
            final float a = TAU * i / 12, b = TAU * (i + 1) / 12;
            final float ax = cos(a) * radius, ay = sin(a) * radius;
            final float bx = cos(b) * radius, by = sin(b) * radius;
            triangle(x, y, x + ax * 0.55f, y + ay * 0.55f, x + bx * 0.55f, y + by * 0.55f, color, alpha, alpha, alpha);
            triangle(x + ax * 0.55f, y + ay * 0.55f, x + ax, y + ay, x + bx, y + by, color, alpha, 0, 0);
            triangle(x + ax * 0.55f, y + ay * 0.55f, x + bx, y + by, x + bx * 0.55f, y + by * 0.55f, color, alpha, 0, alpha);
        }

    }

    /**
     * Blurred disc
     */
    public void haze(float x, float y, float rx, float ry, int color, float alpha) {
        final float radius = Math.max(rx, ry);
        if (alpha < 0.002f || radius <= 0) {
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
        final Knot[] knots = new Knot[segments + 1];
        final float[] nx = new float[segments + 1];
        final float[] ny = new float[segments + 1];
        for (int i = 0; i <= segments; i++) {
            knots[i] = path.at(i / (float) segments);
        }

        final boolean closed = Math.hypot(knots[0].x - knots[segments].x, knots[0].y - knots[segments].y) < 0.001;
        if (closed) {
            knots[segments] = knots[0];
        }

        for (int i = 0; i <= segments; i++) {
            final Knot a = knots[i == 0 && closed ? segments - 1 : Math.max(0, i - 1)];
            final Knot b = knots[i == segments && closed ? 1 : Math.min(segments, i + 1)];
            final float dx = b.x - a.x;
            final float dy = b.y - a.y;
            final float length = Math.max(0.0001f, (float) Math.hypot(dx, dy));
            final Knot current = knots[i];
            float width = current.width;
            final float ax = current.x - a.x;
            final float ay = current.y - a.y;
            final float bx = b.x - current.x;
            final float by = b.y - current.y;
            final float cross = Math.abs(ax * by - ay * bx);
            if (cross > 0.00001f) {
                final float radius = (float) (Math.hypot(ax, ay) * Math.hypot(bx, by)) * length / (2 * cross);
                // So the ribbon doesn't overlap itself on the tip
                width = Math.min(width, radius * 0.8f);
            }

            nx[i] = -dy / length * width;
            ny[i] = dx / length * width;
        }

        for (int i = 0; i < segments; i++) {
            final Knot a = knots[i], b = knots[i + 1];
            if (Math.max(a.alpha, b.alpha) < 0.002f) {
                continue;
            }

            for (int band = 0; band < 4; band++) {
                final float s = BAND_OFFSETS[band];
                final float t = BAND_OFFSETS[band + 1];
                final float sa = BAND_ALPHA[band];
                final float ta = BAND_ALPHA[band + 1];
                vertex(a.x + nx[i] * s, a.y + ny[i] * s, a.color, a.alpha * sa);
                vertex(b.x + nx[i + 1] * s, b.y + ny[i + 1] * s, b.color, b.alpha * sa);
                vertex(b.x + nx[i + 1] * t, b.y + ny[i + 1] * t, b.color, b.alpha * ta);
                vertex(a.x + nx[i] * s, a.y + ny[i] * s, a.color, a.alpha * sa);
                vertex(b.x + nx[i + 1] * t, b.y + ny[i + 1] * t, b.color, b.alpha * ta);
                vertex(a.x + nx[i] * t, a.y + ny[i] * t, a.color, a.alpha * ta);
            }

        }

    }

    /**
     * Subpixel mote with a halo around it
     */
    public void mote(float x, float y, float size, int color, float alpha) {
        glow(x, y, size * 3.5f, color, alpha * 0.15f);
        triangle(x, y - size, x + size * 0.65f, y, x, y + size, color, alpha * 0.25f, alpha, alpha * 0.25f);
        triangle(x, y - size, x, y + size, x - size * 0.65f, y, color, alpha * 0.25f, alpha * 0.25f, alpha);
    }

    /**
     * Generic line with transparent sides
     */
    public void line(float x0, float y0, float x1, float y1, float width0, float width1, int color, float alpha0, float alpha1) {
        final float dx = x1 - x0;
        final float dy = y1 - y0;
        float length = (float) Math.hypot(dx, dy);
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
        for (int i = 0; i < points * 2; i++) {
            final float a = rotation + TAU * i / (points * 2);
            final float b = rotation + TAU * (i + 1) / (points * 2);
            final float ra = radius * (i % 2 == 0 ? 1 : inner);
            final float rb = radius * (i % 2 == 0 ? inner : 1);
            triangle(x, y, x + cos(a) * ra, y + sin(a) * ra, x + cos(b) * rb, y + sin(b) * rb,
                    color, alpha, alpha * (i % 2 == 0 ? 0.08f : 0.55f), alpha * (i % 2 == 0 ? 0.55f : 0.08f));
        }

        glow(x, y, 0.65f, 0xFFFFFFFF, alpha);
    }

    /**
     * Curved arc for borders, rings, etc.
     */
    public void arc(float x, float y, float rx, float ry, float start, float sweep, float thickness, int color, float alpha) {
        final int segments = Math.max(8, Math.min(128, (int) (Math.max(rx, ry) * Math.abs(sweep) / 2)));
        for (int i = 0; i < segments; i++) {
            final float a = start + sweep * i / segments;
            final float b = start + sweep * (i + 1) / segments;
            line(x + cos(a) * rx, y + sin(a) * ry, x + cos(b) * rx, y + sin(b) * ry, thickness, thickness, color, alpha, alpha);
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
        final float t = clamp(value);
        return t * t * (3 - 2 * t);
    }

    public static float life(float phase) {
        return smooth(phase / 0.16f) * smooth((1 - phase) / 0.32f);
    }

    public static float bell(float phase) {
        final float v = sin(clamp(phase) * Math.PI);
        return v * v;
    }

    public static int mix(int a, int b, float amount) {
        float t = clamp(amount);
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            result |= Math.round(((a >>> shift) & 255) * (1 - t) + ((b >>> shift) & 255) * t) << shift;
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

        private int loc;

        public GlowMesh(int segments, int rings) {
            final int vertices = segments * (rings * 6 - 3);
            x = new float[vertices];
            y = new float[vertices];
            alpha = new float[vertices];

            for (int ring = 0; ring < rings; ring++) {
                final float inner = ring / (float) rings;
                final float outer = (ring + 1f) / rings;
                for (int i = 0; i < segments; i++) {
                    final float a = TAU * i / segments;
                    final float b = TAU * (i + 1) / segments;
                    add(a, inner);
                    add(a, outer);
                    add(b, outer);
                    if (ring > 0) {
                        add(a, inner);
                        add(b, outer);
                        add(b, inner);
                    }

                }

            }

        }

        private void add(float angle, float radius) {
            x[loc] = cos(angle) * radius;
            y[loc] = sin(angle) * radius;
            alpha[loc++] = Math.max(0, ((float) Math.exp(-5 * radius * radius) - 0.006738f) / 0.993262f);
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
        Knot at(float u);
    }

}
