package dev.xylonity.tooltipoverhaul.compat.apotheosis;

public final class ApotheosisHook {

    private static final ThreadLocal<Integer> AMOUNT = ThreadLocal.withInitial(() -> 0);

    public static void enter() {
        AMOUNT.set(AMOUNT.get() + 1);
    }

    public static void exit() {
        AMOUNT.set(Math.max(0, AMOUNT.get() - 1));
    }

    public static boolean isActive() {
        return AMOUNT.get() > 0;
    }

}
