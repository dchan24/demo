package com.cmicc.module_message.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RecordWaveView extends View {
    private int viewWight;
    private int viewHeight;
    private int totalLength;
    private Paint mPaint;
    private int maxVolume = 15, volume;
    private int mMinAmplitude, mMaxAmplitude;

    private List<WaveBean> waveList = new ArrayList<>();

    private Map<Integer, List<WaveBean>> map = new HashMap<>();
    private Random random;
    private String[] strings;
    private int amplitude;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public RecordWaveView(Context context) {
        super(context);
        initPaint();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    createWave();
                    handler.sendEmptyMessageDelayed(1, 500);
                    break;
            }
        }
    };


    public void startWave() {

        random = new Random();
        handler.sendEmptyMessageDelayed(1, 1000);
    }

    public void setVolume(int volume) {

        amplitude = volume / maxVolume + 1;
    }

    public void createWave() {

        totalLength = getMeasuredWidth() / 2;
        for (int i = 0; i < random.nextInt(4 * amplitude + 2); i++) {
            WaveBean waveBean = new WaveBean();
            waveBean.maxLength = random.nextInt(totalLength / 10) + totalLength / 3;
            int total = totalLength - waveBean.maxLength;
            switch (random.nextInt(3)) {
                case 0:
                    waveBean.positionX = random.nextInt(total / 3);
                    waveBean.maxHeight = random.nextInt(viewHeight / 3) + viewHeight / 3;
                    break;
                case 1:
                    waveBean.positionX = random.nextInt(total / 3)
                            + total * 1 / 3;
                    waveBean.maxHeight = random.nextInt(viewHeight / 3) + viewHeight * 2 / 3;
                    break;
                case 2:
                    waveBean.positionX = random.nextInt(total / 3)
                            + total * 2 / 3;
                    waveBean.maxHeight = random.nextInt(viewHeight / 3) + viewHeight / 3;
                    break;
            }
            waveBean.positionX = waveBean.positionX - (totalLength - waveBean.maxLength) / 2 - waveBean.maxLength / 2;
            waveBean.maxHeight = random.nextInt(viewHeight / 8) * ((volume + 1) / 10)
                    + (totalLength / 2 - Math.abs(waveBean.positionX + waveBean.maxLength / 2)) / 4;
            waveBean.duration = random.nextInt(2000 - volume * 100) + 100;
            waveBean.color = Color.parseColor(getRandColorCode());
            List<WaveBean> waveBeans = map.get(waveBean.color);
            if (waveBeans != null) {
                waveBeans.add(waveBean);
            } else {
                List<WaveBean> list = new ArrayList<>();
                list.add(waveBean);
                map.put(waveBean.color, list);
            }

            initAnimator(waveBean);

        }

    }

    public String getRandColorCode() {
        Random random = new Random();
        int i = random.nextInt(3);
        return strings[i];
    }

    public RecordWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        strings = new String[]{"#179F76", "#2A74FF", "#9B2E2E"};
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewHeight = h;
        viewWight = w;
        totalLength = 3 * w / 5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        mPaint.setXfermode(null);
        drawLine(canvas);

        canvas.translate(viewWight / 2, viewHeight / 2);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        for (int i = 0; i < 3; i++) {
            canvas.save();
            List<WaveBean> waveBeans = map.get(Color.parseColor(strings[i]));

            if (waveBeans != null && waveBeans.size() != 0) {

                mPaint.setColor(Color.parseColor(strings[i]));
                drawColorVolume(canvas, mPaint, waveBeans, amplitude);
//                drawColorPowFunction(canvas, mPaint, waveBeans, amplitude);

            }
            canvas.restore();
        }
    }
    private void drawPowFunction(Canvas canvas,Paint paint){

    }

    private void initAnimator(final WaveBean waveBean) {
        ValueAnimator animator = ValueAnimator.ofInt(0, waveBean.maxHeight);
        animator.setDuration(waveBean.duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waveBean.waveHeight = (int) animation.getAnimatedValue();
                if (waveBean.waveHeight > waveBean.maxHeight / 2) {
                    waveBean.waveHeight = waveBean.maxHeight - waveBean.waveHeight;
                }
                postInvalidate();
//                Log.e("AAA-->", "initAnimator: " + waveBean.toString());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                map.get(waveBean.color).remove(waveBean);
            }
        });
        animator.start();
    }

    private void drawVolume(Canvas canvas, int waveHeight, int waveLength, Paint paint, int positionX, int positionY) {

        canvas.save();
        canvas.translate(positionX, positionY);
        Path path = new Path();

        for (int i = -waveLength / 2; i < waveLength / 2; i++) {
            double sin = waveHeight * Math.cos(Math.PI / waveLength * i);
            path.lineTo(i, (float) sin);
        }
        for (int i = waveLength / 2; i > -waveLength / 2; i--) {
            double sin = -waveHeight * Math.cos(Math.PI / waveLength * i);
            path.lineTo(i, (float) sin);
        }
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    private void drawLine(Canvas canvas) {
        int colors[] = new int[4];
        float positions[] = new float[4];

        // 第1个点
        colors[0] = 0xFF111111;
        positions[0] = 0;

        // 第2个点
        colors[1] = 0xFFFFFFFF;
        positions[1] = 0.2f;

        colors[2] = 0xFFFFFFFF;
        positions[2] = 0.8f;

        // 第3个点
        colors[3] = 0xFF111111;
        positions[3] = 1;

        LinearGradient shader = new LinearGradient(
                viewWight / 20, 0,
                viewWight * 19 / 20, 0,
                colors,
                positions,
                Shader.TileMode.MIRROR);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        mPaint.setShader(shader);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(viewWight / 20, viewHeight / 2, viewWight * 19 / 20, viewHeight / 2, mPaint);
        mPaint.setXfermode(null);
        mPaint.setShader(null);
        mPaint.clearShadowLayer();
        canvas.restore();
    }


    private void drawColorPowFunction(Canvas canvas, Paint paint, List<WaveBean> waveBeans, int amplitude) {
        Path path = new Path();
        for (int i = -totalLength / 2; i < totalLength / 2; i++) {
            float function;
            float max = 0;
            for (int i1 = 0; i1 < waveBeans.size(); i1++) {

                WaveBean waveBean = waveBeans.get(i1);


                if (i > waveBean.positionX - waveBean.maxLength / 2 && i < waveBean.positionX + waveBean.maxLength / 2) {
                    function = functionPow(i - waveBean.positionX, waveBean.maxHeight);
                } else {
                    function = 0;
                }
                max = Math.max(max, function);
            }
            if (max == 0) {
                path.moveTo(i, 0);
            } else {
                Log.e("AAA-->", "drawColorPowFunction: " + max + "--" + i);
                path.lineTo(i, max);
            }
        }
        for (int i = totalLength / 2; i > -totalLength / 2; i--) {
            double function;
            double max = 0;
            for (int i1 = 0; i1 < waveBeans.size(); i1++) {

                WaveBean waveBean = waveBeans.get(i1);

                if (i >= waveBean.positionX - waveBean.maxLength / 2 && i < waveBean.positionX + waveBean.maxLength / 2) {
                    function = functionPow(i - waveBean.positionX, waveBean.maxHeight);
                } else {
                    function = 0;
                }
                max = Math.max(max, function);
            }
            if (max == 0) {
                path.moveTo(i, 0);
            } else {
                path.lineTo(i, (float) -max);
            }
        }
        canvas.drawPath(path, paint);
    }
    private int functionPow(float x, float maxValue) {
        x = x / 100;
        float pow = (float) Math.pow(1 / (1 + Math.pow(x * 2, 2)), 2);
        Log.e("AAA-->", "functionPow: " + pow + "--" + maxValue);
        int v = (int) (maxValue * pow);

        return v;
    }

    private void drawColorVolume(Canvas canvas, Paint paint, List<WaveBean> waveBeans, int amplitude) {

        Path path = new Path();
        for (int i = -totalLength / 2; i < totalLength / 2; i++) {
            double sin;
            double max = 0;
            for (int i1 = 0; i1 < waveBeans.size(); i1++) {

                WaveBean waveBean = waveBeans.get(i1);

                if (i >= waveBean.positionX && i < waveBean.positionX + waveBean.maxLength) {
                    sin = waveBean.waveHeight * amplitude * Math.sin(Math.PI / waveBean.maxLength * (i - waveBean.positionX));
                } else {
                    sin = 0;
                }
                max = Math.max(max, sin);
            }
            if (max == 0) {
                path.moveTo(i, 0);
            } else {
                path.lineTo(i, (float) max);
            }
        }
        for (int i = totalLength / 2; i > -totalLength / 2; i--) {
            double sin;
            double max = 0;
            for (int i1 = 0; i1 < waveBeans.size(); i1++) {

                WaveBean waveBean = waveBeans.get(i1);

                if (i >= waveBean.positionX && i < waveBean.positionX + waveBean.maxLength) {
                    sin = waveBean.waveHeight * amplitude * Math.sin(Math.PI / waveBean.maxLength * (i - waveBean.positionX));
                } else {
                    sin = 0;
                }
                max = Math.max(max, sin);
            }
            if (max == 0) {
                path.moveTo(i, 0);
            } else {
                path.lineTo(i, (float) -max);
            }
        }
        canvas.drawPath(path, paint);
    }
    class WaveBean {
        int maxHeight;
        int maxLength;
        int waveHeight;
        int color;
        int duration;
        int positionX;
        double sin;
        @Override
        public String toString() {
            return "WaveBean{" +
                    "maxHeight=" + maxHeight +
                    ", maxLength=" + maxLength +
                    ", waveHeight=" + waveHeight +
                    ", color=" + color +
                    ", duration=" + duration +
                    ", positionX=" + positionX +
                    '}';
        }
    }
}