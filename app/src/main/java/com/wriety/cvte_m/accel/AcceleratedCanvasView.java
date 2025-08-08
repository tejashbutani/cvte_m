package com.wriety.cvte_m.accel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AcceleratedCanvasView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private Bitmap backBitmap;
    private Canvas backCanvas;

    private AcceleratedRenderer renderer;
    private boolean useAcceleration = true;

    private float lastX, lastY;
    private final Rect dirtyRect = new Rect();

    public AcceleratedCanvasView(Context context) { this(context, null); }
    public AcceleratedCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setColor(0xFF1E88E5);
        setWillNotDraw(false);
    }

    public void attachRenderer(AcceleratedRenderer renderer) {
        this.renderer = renderer;
        this.useAcceleration = renderer != null && renderer.isPlatformSupported();
        post(this::updateRenderBoundsFromLocation);
    }

    private void ensureBackBuffer(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (backBitmap != null && backBitmap.getWidth() == w && backBitmap.getHeight() == h) return;
        backBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        backCanvas = new Canvas(backBitmap);
        invalidate();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ensureBackBuffer(w, h);
        post(this::updateRenderBoundsFromLocation);
    }

    private void updateRenderBoundsFromLocation() {
        if (renderer == null) return;
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        Rect bounds = new Rect(loc[0], loc[1], loc[0] + getWidth(), loc[1] + getHeight());
        renderer.setRenderBounds(bounds);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!useAcceleration && backBitmap != null) {
            canvas.drawBitmap(backBitmap, 0, 0, null);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.moveTo(x, y);
                lastX = x; lastY = y;
                expandDirty((int) x, (int) y);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f);
                lastX = x; lastY = y;
                expandDirty((int) x, (int) y);
                drawPathAndFlush();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                path.lineTo(x, y);
                expandDirty((int) x, (int) y);
                drawPathAndFlush();
                path.reset();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void drawPathAndFlush() {
        if (backCanvas == null) return;
        backCanvas.drawPath(path, paint);

        if (useAcceleration && renderer != null && backBitmap != null) {
            Rect clipped = new Rect(
                    Math.max(0, dirtyRect.left - 16),
                    Math.max(0, dirtyRect.top - 16),
                    Math.min(getWidth(), dirtyRect.right + 16),
                    Math.min(getHeight(), dirtyRect.bottom + 16));
            renderer.updateCanvas(backBitmap, clipped);
            dirtyRect.setEmpty();
        } else {
            invalidate(dirtyRect);
            dirtyRect.setEmpty();
        }
    }

    private void expandDirty(int x, int y) {
        int pad = 24;
        if (dirtyRect.isEmpty()) {
            dirtyRect.set(x - pad, y - pad, x + pad, y + pad);
        } else {
            dirtyRect.union(x - pad, y - pad, x + pad, y + pad);
        }
    }
}

