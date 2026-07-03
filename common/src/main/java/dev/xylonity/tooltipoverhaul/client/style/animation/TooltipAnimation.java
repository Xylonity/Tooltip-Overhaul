package dev.xylonity.tooltipoverhaul.client.style.animation;

public enum TooltipAnimation {
    NONE,
    FADE,
    POP,
    RISE,
    UNFOLD,
    ZOOM,
    SLIDE,
    SWING,
    EMERGE,
    SQUASH,
    CARD,
    SHAKE;

    public static TooltipAnimation fromString(String name) {
        if (name == null) {
            return NONE;
        }

        try {
            return valueOf(name.trim().toUpperCase());
        }
        catch (Exception ignored) {
            return NONE;
        }

    }

    public boolean isAnimated() {
        return this != NONE;
    }

}
