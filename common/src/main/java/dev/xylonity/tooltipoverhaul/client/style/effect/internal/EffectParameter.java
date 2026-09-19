package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import java.util.Set;

public enum EffectParameter {

    SIZE("size", 0.25f, 3f, 1f, false, Set.of(
            "bubbles", "cinder", "crystals", "fireflies", "fireflies_2", "galaxy", "magic_orbs",
            "spiral", "metal_shining", "ripples", "sonar", "snowfall", "floating_stars", "stars",
            "speed_lines", "aurora", "comets", "storm", "sunbeams", "fireworks", "eruption", "blasts", "firebreath", "shield", "lasers", "missiles", "wisps")),
    THICKNESS("thickness", 0.25f, 3f, 1f, false, Set.of(
            "bubbles", "cinder", "crystals", "fireflies_2", "echo", "galaxy", "magic_orbs",
            "spiral", "metal_shining", "steel_shining", "rim_light", "ripples", "sonar", "stars", "speed_lines", "comets", "storm", "fireworks", "eruption", "blasts", "firebreath", "shield", "lasers", "missiles", "wisps")),
    GLOW("glow", 0f, 3f, 1f, false, Set.of(
            "bubbles", "cinder", "crystals", "fireflies", "fireflies_2", "galaxy", "magic_orbs",
            "metal_shining", "ripples", "sonar", "snowfall", "floating_stars", "stars",
            "speed_lines", "aurora", "comets", "storm", "sunbeams", "fireworks", "eruption", "searchlights", "blasts", "firebreath", "shield", "lasers", "missiles", "wisps")),
    VARIATION("variation", 0f, 100f, 0f, true, Set.of(
            "bubbles", "cinder", "crystals", "fireflies_2", "echo", "magic_orbs", "spiral", "ripples",
            "sonar", "snowfall", "floating_stars", "stars", "speed_lines", "prism", "opal", "aurora",
            "fluorite", "astral", "comets", "storm", "sunbeams", "fireworks", "eruption", "searchlights", "blasts", "firebreath", "shield", "lasers", "missiles", "wisps")),
    SATURATION("saturation", 0f, 2f, 1f, false, Set.of()),
    TRAIL_LENGTH("trailLength", 0f, 3f, 1f, false, Set.of("cinder", "magic_orbs", "speed_lines", "comets", "fireworks", "eruption", "blasts", "missiles", "wisps")),
    TURBULENCE("turbulence", 0f, 3f, 1f, false, Set.of("bubbles", "cinder", "snowfall", "fireflies_2", "speed_lines", "comets", "storm", "eruption", "firebreath", "missiles")),
    TRAVEL("travel", 0f, 2f, 1f, false, Set.of("bubbles")),
    STRETCH("stretch", 0.25f, 3f, 1f, false, Set.of("bubbles", "crystals")),
    ROTATION_SPEED("rotationSpeed", 0f, 4f, 1f, false, Set.of("crystals", "snowfall")),
    TWINKLE_SPEED("twinkleSpeed", 0f, 4f, 1f, false, Set.of("fireflies", "fireflies_2", "floating_stars", "galaxy")),
    TWINKLE_DEPTH("twinkleDepth", 0f, 1f, 1f, false, Set.of("fireflies", "fireflies_2", "floating_stars", "galaxy")),
    ORBIT_RADIUS("orbitRadius", 0.25f, 1.5f, 1f, false, Set.of("magic_orbs", "fireflies", "nebula")),
    ARM_COUNT("armCount", 1f, 8f, 3f, true, Set.of("galaxy")),
    TWIST("twist", 0f, 3f, 1f, false, Set.of("galaxy", "spiral", "nebula")),
    EXPANSION("expansion", 0.25f, 2f, 1f, false, Set.of("echo", "ripples")),
    WAVE_COUNT("waveCount", 1f, 5f, 2f, true, Set.of("ripples")),
    WAVE_SPACING("waveSpacing", 0.5f, 3f, 1f, false, Set.of("sonar")),
    ORIGIN_X("originX", 0f, 1f, 0.5f, false, Set.of("sonar")),
    ORIGIN_Y("originY", 0f, 1f, 0.85f, false, Set.of("sonar")),
    STAR_POINTS("starPoints", 3f, 12f, 4f, true, Set.of("stars")),
    BURST_DURATION("burstDuration", 0.25f, 3f, 1f, false, Set.of("stars", "fireworks", "blasts")),
    FIELD_SCALE("fieldScale", 0.5f, 2.5f, 1f, false, Set.of("prism", "opal", "aurora", "fluorite", "astral")),
    DISTORTION("distortion", 0f, 3f, 1f, false, Set.of("prism", "opal", "aurora", "fluorite", "astral", "echo")),
    SHARPNESS("sharpness", 0.25f, 2f, 1f, false, Set.of("prism", "opal", "fluorite", "astral")),
    CURTAIN_HEIGHT("curtainHeight", 0.25f, 3f, 1f, false, Set.of("aurora")),
    BAND_WIDTH("bandWidth", 0.25f, 3f, 1f, false, Set.of("metal_shining", "steel_shining", "sunbeams", "searchlights")),
    SLANT("slant", 0f, 2f, 0.55f, false, Set.of("metal_shining", "sunbeams")),
    DUTY_CYCLE("dutyCycle", 0.1f, 1f, 0.4f, false, Set.of("metal_shining", "steel_shining")),
    ARC_LENGTH("arcLength", 0.2f, 1.7f, 1f, false, Set.of("rim_light")),
    GLYPH_SIZE("glyphSize", 0.25f, 3f, 1f, false, Set.of("white_dust")),
    DIRECTION("direction", 0f, 360f, 25f, false, Set.of("comets")),
    FORKS("forks", 0f, 5f, 2f, true, Set.of("storm"));

    private final String key;
    private final float minimum, maximum, defaultValue;
    private final boolean integer;
    private final Set<String> effects;

    EffectParameter(String key, float minimum, float maximum, float defaultValue, boolean integer, Set<String> effects) {
        this.key = key;
        this.minimum = minimum;
        this.maximum = maximum;
        this.defaultValue = defaultValue;
        this.integer = integer;
        this.effects = effects;
    }

    public String key() {
        return key;
    }

    public float minimum() {
        return minimum;
    }

    public float maximum() {
        return maximum;
    }

    public float defaultValue() {
        return defaultValue;
    }

    public boolean integer() {
        return integer;
    }

    public boolean supports(String effect) {
        return effects.isEmpty() || effects.contains(EffectCatalog.canonical(effect));
    }

}