package com.wriety.cvte_m.accel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.wriety.cvte_m.R;

public class AccelDemoActivity extends Activity {
    private AcceleratedRenderer renderer;
    private AcceleratedCanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accel_demo);
        canvasView = findViewById(R.id.accel_canvas);

        renderer = new AcceleratedRenderer(this);
        if (renderer.isPlatformSupported() && renderer.init()) {
            canvasView.attachRenderer(renderer);
        } else {
            // fallback to software: do nothing, the view will draw itself
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (renderer != null && renderer.isPlatformSupported()) {
            renderer.beginRender();
        }
    }

    @Override
    protected void onPause() {
        if (renderer != null && renderer.isPlatformSupported()) {
            renderer.endRender();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (renderer != null && renderer.isPlatformSupported()) {
            renderer.destroy();
        }
        super.onDestroy();
    }
}

