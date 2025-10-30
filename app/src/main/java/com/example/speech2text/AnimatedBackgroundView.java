package com.example.speech2text;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class AnimatedBackgroundView extends View {

    private Paint backgroundPaint;
    private int[] colors;
    private float[] positions;
    private LinearGradient shader;
    private Matrix shaderMatrix;
    private ValueAnimator animator;

    public AnimatedBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shaderMatrix = new Matrix();

        colors = new int[]{
                ContextCompat.getColor(context, R.color.gradient_start_color),
                ContextCompat.getColor(context, R.color.jarvis_dark_blue),
                ContextCompat.getColor(context, R.color.gradient_start_color)
        };
        positions = new float[]{0f, 0.5f, 1f};
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (shader == null) {
            shader = new LinearGradient(0, 0, 0, h, colors, positions, Shader.TileMode.CLAMP);
            backgroundPaint.setShader(shader);

            animator = ValueAnimator.ofFloat(0, h * 2);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(10000);
            animator.addUpdateListener(animation -> {
                float translate = (float) animation.getAnimatedValue();
                shaderMatrix.setTranslate(0, translate);
                shader.setLocalMatrix(shaderMatrix);
                invalidate();
            });
            animator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPaint(backgroundPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }
}
