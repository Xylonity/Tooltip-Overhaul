package dev.xylonity.tooltipoverhaul.compat.modernfix;

/**
 * The config entry under the name 'mixin.perf.faster_item_rendering', if set to true, certain unseen faces are culled,
 * so when the rendered stack starts rotating, some faces are seen invisible
 *
 * This should also fix Flerovium (Forge only) if 'itemBackFaceCulling' is set to true
 */
public final class ModernFixCompat {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public static void push() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void pop() {
        int depth = DEPTH.get();

        if (depth <= 1) {
            DEPTH.remove();
        }
        else {
            DEPTH.set(depth - 1);
        }

    }

    public static boolean isEnabled() {
        return DEPTH.get() > 0;
    }

}