package com.cmicc.module_message.ui.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chinamobile.app.yuliao_common.utils.FontUtil;
import com.chinamobile.app.yuliao_common.utils.SystemUtil;

/**
 * Created by linguohong on 2017/11/13.
 */

public class ConvListSpaceDecoration extends RecyclerView.ItemDecoration{
    private int defaultSpace = SystemUtil.dip2px(12);//

    public ConvListSpaceDecoration() {
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        outRect.left = space;
//        outRect.right = space;
        int space = (int)(defaultSpace* FontUtil.getFontScale());
        outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space;
        } else {
            outRect.top = 0;
        }
    }
}
