package com.cmicc.module_message.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_common.utils.SystemUtil;

public class RecordAnimView extends View {
    private int mViewHeight;
    private int mViewWidth;
    private int mDotsNum;
    private int mLeft;
    private int mTop;
    private Paint mPaint;
    private int mVolume;
    private final int  MIN_VOLUME = 4;
    private int mDotsWidth = SystemUtil.dip2px(MIN_VOLUME);
    private final int DOTS_MIN_HEIGHT= mDotsWidth/2;
    private int mDotsInternal= SystemUtil.dip2px(5);
    private int mStartX = SystemUtil.dip2px(5);
    private RecordAnimColorGradient mColorGradient;
    private int mY;
    private boolean mDrawMinHeigh = false;

    private int mRandomIgnoreNum = 0;
    private double[] mValue = {0.2f,0.3f,0.6f,1.0f,
            0.2f,0.4f,0.2f,0.6f,0.2f,0.3f,0.6f,1.0f,
            0.2f,0.3f,0.2f,0.6f,0.2f,0.3f,0.4f,1.0f,
            0.2f,0.6f,0.2f,0.5f,0.3f,0.5f,1.0f,
            0.5f,0.3f,0.2f,0.7f,0.4f,0.2f,1.0f,
            0.7f,0.4f,0.2f
            };
    private double[]mCurrentValue = {0.2f,0.3f,0.6f,1.0f,
            0.2f,0.4f,0.2f,0.6f,0.2f,0.3f,0.6f,1.0f,
            0.2f,0.3f,0.2f,0.6f,0.2f,0.3f,0.4f,1.0f,
            0.2f,0.6f,0.2f,0.5f,0.3f,0.5f,1.0f,
            0.5f,0.3f,0.2f,0.7f,0.4f,0.2f,1.0f,
            0.7f,0.4f,0.2f
    };

    public RecordAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint=new Paint();
        mPaint.setAntiAlias(true);
        mColorGradient = new RecordAnimColorGradient(0xff54c3ff,0xff157cf8);
        mPaint.setColor(0xff54c3ff);
    }

    public void setDotsNum(int dotNums){
        mDotsNum = dotNums;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeft = left;
        mTop = top;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewHeight = h;
        mViewWidth = w;
        mY = mViewHeight/2;
//        mDotsNum = (mViewWidth-mStartX)/(mDotsWidth+mDotsInternal);
        mDotsNum = mValue.length;
        mDotsInternal = (mViewWidth-mStartX)/mDotsNum - mDotsWidth;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
      //  int step = 720/mDotsNum;
    //    int degrees = 10;
        int currentX = mStartX;
        for(int i = 0;i<mDotsNum;i++){
            int color = mColorGradient.getColor((float)i/mDotsNum);
            mPaint.setColor(color);
    //        double radians = Math.toRadians(degrees);
            double value = 0.0f;
            int height = 0;
            if(mDrawMinHeigh){
                value = 1.0f;
//                height = (int)((mVolume*value)*SystemUtil.dip2px(1));
                height = DOTS_MIN_HEIGHT;
            }else {
                mRandomIgnoreNum++;
                if(mRandomIgnoreNum >= 10){
                    mRandomIgnoreNum = 0;
                    int ran = (int)((mDotsNum-1)*Math.random());
                    value = mValue[ran];
                    mCurrentValue[i] = value;
                }else{
                    value = mCurrentValue[i];
                }
                height = (int)((mVolume*value)*SystemUtil.dip2px(1));
                if(height < DOTS_MIN_HEIGHT){
                    height = DOTS_MIN_HEIGHT;
                }
            }

            RectF rect = new RectF();
            rect.left = currentX;
            rect.top = mY - height;
            rect.right = currentX+mDotsWidth;
            rect.bottom = mY+height;
            canvas.drawRoundRect(rect,mDotsWidth/2,mDotsWidth/2,mPaint);
            currentX = currentX+mDotsWidth+mDotsInternal;
//            degrees+=step;
        }

    }

    public void setVolume(int volume){
        mVolume = volume;
        if(mVolume<MIN_VOLUME){
            mVolume = MIN_VOLUME;
            mDrawMinHeigh = true;
        }else {
            mDrawMinHeigh = false;
        }
        invalidate();
    }
}
