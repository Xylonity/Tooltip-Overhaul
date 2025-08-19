package dev.xylonity.tooltipoverhaul.util;

public class Util {

    public static float calcRotY(double time) {
        return (float) ((System.currentTimeMillis() / time) * 360 % 360);
    }

}