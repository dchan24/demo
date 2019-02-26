package com.cmicc.module_message.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.MapView;
import com.chinamobile.app.yuliao_common.utils.LogF;

public class GDMapView extends MapView {
    private Context mContext;
    private String TAG = "GDMapView";
    public GDMapView(Context context) {
        super(context);
        init(context);
    }

    public GDMapView(Context context, AttributeSet var2) {
        super(context, var2);
        init(context);
    }

    public GDMapView(Context context, AttributeSet var2, int var3) {
        super(context, var2, var3);
        init(context);
    }

    public GDMapView(Context context, AMapOptions var2) {
        super(context,var2);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    ViewGroup child = (ViewGroup) getChildAt(0);//地图框架
                    if (child == null) {
                        LogF.e(TAG, "Map root view is null");
                    }
                    // child.getChildAt(0).setVisibility(View.VISIBLE);//地图
                    if (child != null) {
                        View logoView = child.getChildAt(2);
                        if (logoView != null)
                            logoView.setVisibility(View.GONE);//logo
                        // child.getChildAt(5).setVisibility(View.VISIBLE);//缩放按钮
                        // child.getChildAt(6).setVisibility(View.VISIBLE);//定位按钮
                        // child.getChildAt(7).setVisibility(View.VISIBLE);//指南针
                    }
                }catch(Exception e){
                    LogF.e(TAG,"exception ="+e);
                }
            }
        });
    }
}
