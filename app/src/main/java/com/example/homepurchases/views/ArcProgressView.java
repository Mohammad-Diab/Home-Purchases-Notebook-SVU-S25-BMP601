package com.example.homepurchases.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ArcProgressView extends View {

    private final Paint trackPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcOval        = new RectF();

    private int   progress    = 0;
    private float strokeWidth = 0;

    // Start at left (180°), sweep counter-clockwise (-180°) through the top to right.
    private static final float START_ANGLE   = 180f;
    private static final float SWEEP_TOTAL   = -180f;

    public ArcProgressView(Context context) {
        super(context);
        init();
    }

    public ArcProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        float density  = getResources().getDisplayMetrics().density;
        strokeWidth    = 16 * density;

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(0xFFBDBDBD);

        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(strokeWidth);
        indicatorPaint.setStrokeCap(Paint.Cap.ROUND);
        indicatorPaint.setColor(0xFF6200EE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Oval is a full circle fitting the view width.
        // The view height is ~half the width, so only the top arch is visible.
        float inset = strokeWidth / 2 + 4 * getResources().getDisplayMetrics().density;
        arcOval.set(inset, inset, w - inset, w - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Full track arch
        canvas.drawArc(arcOval, START_ANGLE, SWEEP_TOTAL, false, trackPaint);
        // Filled indicator
        if (progress > 0) {
            float sweep = SWEEP_TOTAL * progress / 100f;
            canvas.drawArc(arcOval, START_ANGLE, sweep, false, indicatorPaint);
        }
    }

    public void setProgress(int p) {
        progress = Math.max(0, Math.min(100, p));
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    public void setIndicatorColor(int color) {
        indicatorPaint.setColor(color);
        invalidate();
    }

    public void setTrackColor(int color) {
        trackPaint.setColor(color);
        invalidate();
    }
}
