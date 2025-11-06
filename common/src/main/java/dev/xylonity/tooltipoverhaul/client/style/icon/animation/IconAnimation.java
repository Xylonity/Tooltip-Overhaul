package dev.xylonity.tooltipoverhaul.client.style.icon.animation;

public enum IconAnimation {
    NONE,
    ZOOM,
    ROTATE,
    ROTATE_FAST,
    ROTATE_ZOOM,
    ZOOM_SNAP,
    SKEW,
    VIBRATION,
    TILT_WAVE,
    FLIP,
    PENDULUM,
    BOUNCE,
    GO_DOWN,
    PULSE,
    FAN_IN,
    HOVER_POP,
    BARREL_ROLL;

    public static IconAnimation fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        }
        catch (Exception ignore) {
            return NONE;
        }
    }

}