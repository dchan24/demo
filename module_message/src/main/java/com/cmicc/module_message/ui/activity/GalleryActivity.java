package com.cmicc.module_message.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.GalleryFragment;
import com.cmicc.module_message.ui.presenter.GalleryPresenter;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;

/**
 * Created by GuoXietao on 2017/3/27.
 * 相册界面
 */

public class GalleryActivity extends BaseActivity {
    GalleryPresenter mPresenter;
    GalleryFragment mView;
    long beginTime = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        beginTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LogF.d("GalleryActivity","GalleryActivity onWindowFocusChanged time:"+(System.currentTimeMillis() - beginTime));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogF.d("GalleryActivity","GalleryActivity on resume time:"+(System.currentTimeMillis() - beginTime));
    }

    @Override
    protected void findViews() {
    }

    @Override
    protected void init() {
        mView = new GalleryFragment();
        mView.setArguments(getIntent().getExtras());
        mPresenter = new GalleryPresenter(this);
        mView.setPresenter(mPresenter);
        mPresenter.setView(mView);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mView, R.id.contentFrame);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        getSupportFragmentManager().beginTransaction().remove(mView);
    }

}
