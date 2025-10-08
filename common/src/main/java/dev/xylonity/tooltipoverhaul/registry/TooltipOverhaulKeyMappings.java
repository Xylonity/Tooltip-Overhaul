package dev.xylonity.tooltipoverhaul.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class TooltipOverhaulKeyMappings {

    public static final KeyMapping COMPARE_TOOLTIP = new KeyMapping(
            "wads",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            "dsad"
    );

}
