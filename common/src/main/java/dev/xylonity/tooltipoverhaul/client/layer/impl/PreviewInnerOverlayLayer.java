package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;

public interface PreviewInnerOverlayLayer extends PreviewBackgroundLayer {

    @Override
    default LayerDepth getLayerDepth() {
        return LayerDepth.INNER_FRAME;
    }

}