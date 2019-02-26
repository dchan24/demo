package com.cmicc.module_message.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.GalleryChangedFragment;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.presenter.GalleryChangedPresenter;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.presenter.GalleryPresenter;

import java.util.ArrayList;


/**
 * Created by GuoXietao on 2017/4/10.
 * 图片编辑界面
 */

public class GalleryChangedActivity extends BaseActivity {

    GalleryChangedPresenter mGalleryChangedPresenter;
    GalleryChangedFragment mGalleryChangedFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_changed);
    }

    @Override
    protected void findViews() {
    }

    @Override
    protected void init() {
        Bundle bundle = getIntent().getExtras();

        if (mGalleryChangedFragment == null) {
            mGalleryChangedFragment = new GalleryChangedFragment();
        }
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mGalleryChangedFragment, R.id.contentFrame);

        mGalleryChangedPresenter = new GalleryChangedPresenter(this);
        mGalleryChangedPresenter.setAllMediaItems((ArrayList<MediaItem>) GalleryPresenter.mMediaSetList.clone()); // mAllMediaItems
        mGalleryChangedPresenter.setPreviewSelect(bundle.getBoolean(GalleryPresenter.PREVIEW_SELECT, false));
        mGalleryChangedPresenter.setCurrentPosition(bundle.getInt(GalleryPresenter.CURRENT_POSITION, 0));
        mGalleryChangedPresenter.setView(mGalleryChangedFragment);

        mGalleryChangedFragment.setPresenter(mGalleryChangedPresenter);
        mGalleryChangedFragment.setArguments(bundle);
    }

    @Override
    public void onBackPressed() {
        mGalleryChangedFragment.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode	==	ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()){
            mGalleryChangedFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        mGalleryChangedPresenter.onDestroy();
        mGalleryChangedPresenter = null;
        super.onDestroy();
    }
}
