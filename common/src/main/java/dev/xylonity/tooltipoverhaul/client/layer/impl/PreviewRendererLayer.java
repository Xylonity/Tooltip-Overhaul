package dev.xylonity.tooltipoverhaul.client.layer.impl;

import dev.xylonity.tooltipoverhaul.client.layer.LayerDepth;

public interface PreviewRendererLayer extends PreviewBackgroundLayer {

    @Override
    default LayerDepth getLayerDepth() {
        return LayerDepth.RENDERS;
    }

}