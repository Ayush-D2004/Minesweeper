package com.example.minesweeper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.os.Handler;

public class AnimatedGradientView extends View {
    private Paint paint;
    private float angle = 0;
    private Handler handler;
    private Runnable updateRunnable;

    public AnimatedGradientView(Context context) {
        super(context);
        init();
    }

    public AnimatedGradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedGradientView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                angle = (angle + 10) % 360;
                invalidate();
                handler.postDelayed(this, 1000); // Update every second
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float width = getWidth();
        float height = getHeight();
        
        // Calculate gradient points based on angle
        float radians = (float) Math.toRadians(angle);
        float x1 = (float) (width/2 + width/2 * Math.cos(radians));
        float y1 = (float) (height/2 + height/2 * Math.sin(radians));
        float x2 = (float) (width/2 - width/2 * Math.cos(radians));
        float y2 = (float) (height/2 - height/2 * Math.sin(radians));
        
        LinearGradient gradient = new LinearGradient(
            x1, y1, x2, y2,
            new int[]{0xFF85FFBD, 0xFFFFFB7D},
            null,
            Shader.TileMode.CLAMP
        );
        
        paint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, paint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(updateRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(updateRunnable);
    }
} 