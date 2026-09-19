package dev.xylonity.tooltipoverhaul.client.style.effect.internal;

import dev.xylonity.tooltipoverhaul.client.layer.impl.EffectLayer;
import dev.xylonity.tooltipoverhaul.client.style.effect.*;

import java.util.List;
import java.util.Locale;

public final class EffectCatalog {

    public static final List<String> IDS = List.of(
            "bubbles", "cinder", "crystals", "fireflies", "fireflies_2", "echo",
            "galaxy", "magic_orbs", "speed_lines", "nebula", "spiral", "white_dust", "metal_shining", "steel_shining",
            "rim_light", "ripples", "sonar", "stars", "floating_stars", "prism", "snowfall", "opal", "aurora",
            "fluorite", "astral", "comets", "storm", "sunbeams", "fireworks", "eruption", "searchlights", "blasts",
            "firebreath", "shield", "lasers", "missiles", "wisps"
    );

    public static int[] palette(String id) {
        return switch (canonical(id)) {
            case "bubbles" -> new int[]{0xFF7ACDDB, 0xFFD8A7E8, 0xFFF5DFAD};
            case "cinder" -> new int[]{0xFFFFEDBB, 0xFFFFA34D, 0xFF9C4840};
            case "comets" -> new int[]{0xFF83CEFF, 0xFFC7A1F3, 0xFFFFEACB};
            case "storm" -> new int[]{0xFF7FB6FF, 0xFFC9A8FF, 0xFFF3F8FF};
            case "fireworks" -> new int[]{0xFFFF7AA8, 0xFF7AD7FF, 0xFFFFE08A};
            case "firebreath" -> new int[]{0xFFFF6A1F, 0xFFFFC23A, 0xFFFFF5C2};
            case "shield" -> new int[]{0xFF5FE8D8, 0xFFB8FFF4, 0xFFFFFFFF};
            case "lasers" -> new int[]{0xFFFF3B3B, 0xFFFF9A3B, 0xFFFFFFFF};
            case "wisps" -> new int[]{0xFF2FD9C8, 0xFF5B8CFF, 0xFFEAFFFB};
            case "missiles" -> new int[]{0xFFFFC994, 0xFFDA8967, 0xFFB9BBC4};
            case "blasts" -> new int[]{0xFFF3B27C, 0xFFD67C58, 0xFFFFEAD0};
            case "searchlights" -> new int[]{0xFFDCE9FF, 0xFFA8C4FF, 0xFFFFFFFF};
            case "eruption" -> new int[]{0xFFFF7A2A, 0xFFFFD36B, 0xFFFFF4C8};
            case "sunbeams" -> new int[]{0xFFFFEFC2, 0xFFFFD08A, 0xFFFFFDF0};
            case "crystals" -> new int[]{0xFFA995DD, 0xFF9FD2E0, 0xFFECE7FF};
            case "fireflies" -> new int[]{0xFFFFF2B8, 0xFFD8E86A, 0xFFFFF2B8};
            case "fireflies_2" -> new int[]{0xFFCFDF83, 0xFFFFD08A, 0xFFFFE4A3};
            case "echo" -> new int[]{0xFFB6A0E4, 0xFF8ECFE4, 0xFFE2DCFF};
            case "galaxy" -> new int[]{0xFF4A2080, 0xFF6A40A0, 0xFFFFFFDD};
            case "magic_orbs" -> new int[]{0xFF8BDAEF, 0xFFC3A0EA, 0xFF9EE0BD};
            case "speed_lines" -> new int[]{0xFF719BC7, 0xFFE2F4FF, 0xFFFFFFFF};
            case "nebula" -> new int[]{0xFFFADFF6, 0xFF8AC791, 0xFF2A5193};
            case "spiral" -> new int[]{0xFF81B7C5, 0xFFDFD9C7, 0xFFE1EBE7};
            case "white_dust" -> new int[]{0xFFFFFFFF, 0xFFE0B35C, 0xFFFFFFFF};
            case "metal_shining" -> new int[]{0xFFC0DAF5, 0xFFFFECCB, 0xFFFFF3DC};
            case "steel_shining" -> new int[]{0xFF94B8D8, 0xFFD5E3EF, 0xFFF3F8FF};
            case "ripples" -> new int[]{0xFF8ACDDF, 0xFFAD91EE, 0xFFF0BBDD};
            case "sonar" -> new int[]{0xFF538EBD, 0xFFA5EBDD, 0xFFC8F7EA};
            case "snowfall" -> new int[]{0xFFBDCADA, 0xFFF2F3F5, 0xFFA9CEEF};
            case "prism" -> new int[]{0xFF70CFC6, 0xFF9390DF, 0xFFE6BC8B};
            case "opal" -> new int[]{0xFF77BBB5, 0xFFA69BD3, 0xFFD5B596};
            case "aurora" -> new int[]{0xFF6CDFB8, 0xFF947ADC, 0xFFB1EAD5};
            case "fluorite" -> new int[]{0xFF5ABBB5, 0xFF879CDB, 0xFFD9A6BF};
            case "astral" -> new int[]{0xFF659DCF, 0xFFB2A5E6, 0xFFE4C49A};
            case "floating_stars" -> new int[]{0xFFFFE6BA, 0xFFDDEAFF, 0xFFE1CFF3};
            case "stars" -> new int[]{0xFFFFFFFF, 0xFFE3E8FF, 0xFFFFE9D2};
            case "rim_light" -> new int[]{0xFFB3CDD7, 0xFFD0BFD9, 0xFFF1EDF8};
            default -> new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
        };

    }

    public static String canonical(String id) {
        return switch (id.trim().toLowerCase(Locale.ROOT)) {
            case "stars_2" -> "stars";
            case "metal_shining_2" -> "steel_shining";
            case "moondust" -> "snowfall";
            default -> id.trim().toLowerCase(Locale.ROOT);
        };

    }

    public static EffectLayer create(String id) {
        return switch (canonical(id)) {
            case "bubbles" -> new BubblesEffect();
            case "cinder" -> new CinderEffect();
            case "comets" -> new CometsEffect();
            case "storm" -> new StormEffect();
            case "fireworks" -> new FireworksEffect();
            case "firebreath" -> new FirebreathEffect();
            case "shield" -> new ShieldEffect();
            case "lasers" -> new LasersEffect();
            case "wisps" -> new WispsEffect();
            case "missiles" -> new MissilesEffect();
            case "blasts" -> new BlastsEffect();
            case "searchlights" -> new SearchlightsEffect();
            case "eruption" -> new EruptionEffect();
            case "sunbeams" -> new SunbeamsEffect();
            case "crystals" -> new CrystalsEffect();
            case "fireflies" -> new FirefliesEffect();
            case "fireflies_2" -> new Fireflies2Effect();
            case "echo" -> new EchoEffect();
            case "galaxy" -> new GalaxyEffect();
            case "magic_orbs" -> new MagicOrbsEffect();
            case "speed_lines" -> new SpeedLinesEffect();
            case "nebula" -> new NebulaEffect();
            case "spiral" -> new TimeSpiralEffect();
            case "white_dust" -> new WhiteDustEffect();
            case "metal_shining" -> new MetalShiningEffect();
            case "steel_shining", "metal_shining_2" -> new SteelShiningEffect();
            case "rim_light" -> new RimLightEffect(0xDD599FDB, 0xCC9C79DC);
            case "ripples" -> new RipplesEffect();
            case "sonar" -> new SonarEffect();
            case "floating_stars" -> new StarsEffect();
            case "stars" -> new Stars2Effect();
            case "prism" -> new PrismEffect();
            case "snowfall", "moondust" -> new SnowfallEffect();
            case "opal" -> new OpalEffect();
            case "aurora" -> new AuroraEffect();
            case "fluorite" -> new FluoriteEffect();
            case "astral" -> new AstralEffect();
            default -> null;
        };

    }

}
