package com.cmicc.module_message.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class MyRecyclerView extends RecyclerView {
    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }
}
