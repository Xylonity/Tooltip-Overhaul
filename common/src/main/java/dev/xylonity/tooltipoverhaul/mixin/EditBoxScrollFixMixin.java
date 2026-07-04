package dev.xylonity.tooltipoverhaul.mixin;

import dev.xylonity.tooltipoverhaul.client.screen.config.TooltipOverhaulConfigScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This should fix a white box randomly appearing at the right of some edit boxes
 */
@Mixin(EditBox.class)
abstract class EditBoxScrollFixMixin {

    @Shadow private String value;
    @Shadow private int cursorPos;
    @Shadow private int highlightPos;
    @Shadow private int displayPos;
    @Shadow @Final private Font font;

    @Shadow public abstract int getInnerWidth();

    @Inject(method = "setHighlightPos", at = @At("TAIL"))
    private void tooltipoverhaul$keepCursorVisible(int position, CallbackInfo ci) {
        if (!(((Object) this) instanceof TooltipOverhaulConfigScreen.StyledEditBox) || cursorPos != highlightPos) {
            return;
        }

        int guard = 0;
        while (guard++ < 64 && cursorPos - displayPos > font.plainSubstrByWidth(value.substring(displayPos), getInnerWidth()).length()) {
            displayPos++;
        }

    }

    @Inject(method = "renderHighlight", at = @At("HEAD"), cancellable = true)
    private void tooltipoverhaul$skipHighlight(GuiGraphics graphics, int minX, int minY, int maxX, int maxY, CallbackInfo ci) {
        if (((Object) this) instanceof TooltipOverhaulConfigScreen.StyledEditBox && cursorPos == highlightPos) {
            ci.cancel();
        }

    }

}
