package com.wriety.cvte_m.accel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import com.seewo.easinote.accelerator.base.CvtConfigManager;
import com.seewo.easinote.accelerator.base.IRenderAcceleratorManager;
import com.seewo.easinote.accelerator.base.RenderAcceleratorManagerConfig;
import com.seewo.easinote.accelerator.base.RenderAcceleratorManagerFactory;
import com.seewo.easinote.accelerator.base.RenderAcceleratorManagerFactory.AcceleratorType;

public final class AcceleratedRenderer {
    private final Context appContext;
    private IRenderAcceleratorManager manager;
    private boolean initialized;

    private static final int LAYER_INDEX_FOREGROUND = 1;
    private static final int LAYER_INDEX_ERASER = 2;

    public AcceleratedRenderer(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public boolean isPlatformSupported() {
        return CvtConfigManager.isPlatformSupport();
    }

    public boolean init() {
        manager = RenderAcceleratorManagerFactory
                .getRenderAcceleratorManagerByType(AcceleratorType.CVTRender);

        RenderAcceleratorManagerConfig config = RenderAcceleratorManagerConfig
                .obtain(appContext)
                .setScreenFreeze(false)
                .setUseEraserMode(false)
                .setForegroundLayerIndex(LAYER_INDEX_FOREGROUND)
                .setEraserLayerIndex(LAYER_INDEX_ERASER);

        initialized = manager.init(config);
        return initialized;
    }

    public void setRenderBounds(Rect rectOnScreen) {
        if (!initialized || rectOnScreen == null) return;
        manager.setRenderBounds(rectOnScreen);
    }

    public void beginRender() {
        if (!initialized) return;
        manager.setRenderable(true);
        manager.prepareToRender();
    }

    public void updateCanvas(Bitmap fullBitmap, Rect dirtyRect) {
        if (!initialized || fullBitmap == null || dirtyRect == null) return;
        manager.setLayer(LAYER_INDEX_FOREGROUND, 0, 0, fullBitmap);
        manager.render(dirtyRect);
    }

    public void endRender() {
        if (!initialized) return;
        manager.setRenderable(false);
        manager.finishRender();
    }

    public void clearAll() {
        if (!initialized) return;
        manager.cleanAll();
    }

    public void destroy() {
        if (!initialized) return;
        manager.destroy();
        initialized = false;
    }
}

