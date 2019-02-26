package com.cmicc.module_message.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yang on 2018/9/26.
 */

public class ChatRichMediaViewPagerAdapter  extends PagerAdapter {

    private List<GridView> gridList;

    public ChatRichMediaViewPagerAdapter() {
        gridList = new ArrayList<>();
    }

   public void add(List<GridView> datas) {
        if (gridList.size() > 0) {
            gridList.clear();
        }
        gridList.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return gridList!=null?gridList.size():0;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(gridList.get(position));
        return gridList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
