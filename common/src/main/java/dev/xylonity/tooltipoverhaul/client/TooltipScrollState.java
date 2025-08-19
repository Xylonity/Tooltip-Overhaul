package dev.xylonity.tooltipoverhaul.client;

/**
 * Handles the scrolling state for tooltips that are taller than the screen height
 */
public final class TooltipScrollState {

    private static final float SPEED_CAP = 800f;

    private static int maxScroll;
    private static int scroll;
    private static boolean isActive;

    private static float accumulated;
    private static float velocity;
    private static long lastNs;

    /**
     * Inits the scrolling state for a given tooltip
     * @param contentHeight height of the tooltip content
     * @param viewportHeight visible height of the tooltip area
     */
    public static void begin(int contentHeight, int viewportHeight) {
        int overflow = Math.max(0, contentHeight - Math.max(0, viewportHeight));

        // Avoids overshooting by leaving a 1 pixel margin
        if (overflow > 0) {
            overflow = Math.max(0, overflow - 1);
        }

        maxScroll = overflow;

        // Clamps curr scroll to the new maximum
        if (scroll > maxScroll) {
            scroll = maxScroll;
        }

        // Scrolls if the content is taller than the screen dims (this is capped inside other clases anyways)
        isActive = overflow > 0;

        // Resets scrolling if the scrolling isn't needed
        if (!isActive) {
            accumulated = 0f;
            velocity = 0f;
            lastNs = 0L;
        }

    }

    /**
     * Hardreset of the entire scroll state
     */
    public static void reset() {
        maxScroll = 0;
        scroll = 0;
        isActive = false;
        accumulated = 0f;
        velocity = 0f;
        lastNs = 0L;
    }

    /**
     * Resets the scroll state if it's not active
     */
    public static void resetIfInactive() {
        if (!isActive) {
            maxScroll = 0;
            scroll = 0;
            accumulated = 0f;
            velocity = 0f;
            lastNs = 0L;
        }

    }

    /**
     * Hotfix for non-scrollable hotbar after viewing a scrollable tooltip
     */
    public static boolean shouldCaptureScroll() {
        if (!isActive) return false;
        return lastNs != 0L && (System.nanoTime() - lastNs) <= 150_000_000L;
    }

    public static boolean isIsActive() {
        return isActive;
    }

    public static int getScroll() {
        return scroll;
    }

    /**
     * Adjusts scroll positions by a delta (px) clamping the value between 0 and maxscroll
     */
    public static void addScroll(int delta) {
        if (!isActive) return;

        int scrolls = scroll + delta;
        if (scrolls < 0) {
            scrolls = 0;
        }

        if (scrolls > maxScroll) {
            scrolls = maxScroll;
        }

        scroll = scrolls;
    }

    /**
     * Called when max scroll scroll input is received
     */
    public static void onRawScroll(double dy) {
        if (!isActive) return;

        // Permutee scroll delta to px
        float delta = (float)(-dy) * 2f;
        accumulated += delta;
        int step = (int) accumulated;

        // Applies int scroll if enough accum has been built
        if (step != 0) {
            addScroll(step);
            accumulated -= step;
        }

        velocity += (float)(-dy) * 220f;

        // Clamped speed
        if (velocity > SPEED_CAP) velocity = SPEED_CAP;
        if (velocity < -SPEED_CAP) velocity = -SPEED_CAP;

        if (scroll == 0 && velocity < 0f) velocity = 0f;
        if (scroll == maxScroll && velocity > 0f) velocity = 0f;
    }

    /**
     * Computes the core calculations
     */
    public static void tick() {
        long now = System.nanoTime();
        if (lastNs == 0L) {
            lastNs = now;
            return;
        }

        float dt = (now - lastNs) / 1_000_000_000f;
        lastNs = now;

        if (!isActive) {
            velocity = 0f;
            return;
        }

        if (Math.abs(velocity) > 1) {
            // Move scroll by vl * time
            int delta = Math.round(velocity * dt);
            if (delta != 0) {
                addScroll(delta);
            }

            // Scrolling friction based on the previous scrolling speed
            float dec = 9f * dt;
            velocity -= velocity * dec;

            // Stop vel if at max
            if (scroll == 0 && velocity < 0f) velocity = 0f;
            if (scroll == maxScroll && velocity > 0f) velocity = 0f;
        } else {
            velocity = 0f;
        }

    }

}
