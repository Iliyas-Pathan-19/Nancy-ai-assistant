package com.example.speech2text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class OrbView extends View {
    private Paint orbPaint;
    private Paint auraPaint;
    private float amplitude = 0f;
    private float baseRadius;

    public OrbView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        orbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        orbPaint.setColor(ContextCompat.getColor(context, R.color.jarvis_cyan_glow));

        auraPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        auraPaint.setColor(ContextCompat.getColor(context, R.color.jarvis_cyan_glow));
        auraPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
    }

    public void updateAmplitude(float amplitude) {
        this.amplitude = amplitude;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        baseRadius = Math.min(w, h) / 3f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // The amplitude from onRmsChanged is a value in dB, typically from 0 to 10 for speech.
        // We will map this to a pixel value for the radius.
        float radiusOffset = amplitude * 4.0f;

        // Draw the outer glow/aura
        auraPaint.setAlpha(40); // Soft, semi-transparent glow
        canvas.drawCircle(centerX, centerY, baseRadius + radiusOffset + 20, auraPaint);
        auraPaint.setAlpha(80);
        canvas.drawCircle(centerX, centerY, baseRadius + radiusOffset + 10, auraPaint);

        // Draw the main orb
        canvas.drawCircle(centerX, centerY, baseRadius + radiusOffset, orbPaint);
    }
}
