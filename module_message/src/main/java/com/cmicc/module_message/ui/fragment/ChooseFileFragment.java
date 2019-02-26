package com.cmicc.module_message.ui.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinamobile.app.utils.RxAsyncHelper;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.model.ImgVedioAudioModel;
import com.cmicc.module_message.R;

import rx.functions.Func1;

/**
 * Created by tigger on 2017/7/3.
 */

public class ChooseFileFragment extends BaseFragment implements OnClickListener {

    private Context mContext;

    TextView mTvPic;
    TextView mTvMusic;
    TextView mTvVedio;
    TextView mTvDoc;
    TextView mTvMobileMemory;
    TextView mTvSD;

    LinearLayout mLLMobileMemory;
    LinearLayout mLLMusic;
    LinearLayout mLLVedio;
    LinearLayout mLLPic;

    View mViewLastLine;

    @Override
    public void initViews(View rootView) {
        super.initViews(rootView);
        mTvPic = (TextView) rootView.findViewById(R.id.tv_pic);
        mTvMusic = (TextView) rootView.findViewById(R.id.tv_music);
        mTvVedio = (TextView) rootView.findViewById(R.id.tv_vedio);
        mTvDoc = (TextView) rootView.findViewById(R.id.tv_doc);
        mTvMobileMemory = (TextView) rootView.findViewById(R.id.tv_mobile_memory);
        mTvSD = (TextView) rootView.findViewById(R.id.tv_sd);
        mViewLastLine = (View) rootView.findViewById(R.id.view_last_line);

        mLLMobileMemory = (LinearLayout) rootView.findViewById(R.id.ll_mobile_memory);
        mLLMusic = (LinearLayout) rootView.findViewById(R.id.ll_music);
        mLLVedio = (LinearLayout) rootView.findViewById(R.id.ll_vedio);
        mLLPic = (LinearLayout) rootView.findViewById(R.id.ll_pic);


    }

    private FragmentManager mFgManager;
    private FragmentTransaction mFgTransaction;
    private ChooseFileSdFragment mChooseFileSdFragment;
    private ChooseFileImgVedioMusicFragment mImgVedioMusicFragment;
    private int mImgCount;
    private int mVedioCount;
    private int mMusicCount;
    private final static int SCAN_IMG_OK = 1;
    private final static int SCAN_MUSIC_OK = 2;
    private final static int SCAN_VEDIO_OK = 3;
    private int mType = -1;

    @Override
    public void initData() {
        mContext = getActivity();
        mFgManager = getFragmentManager();

        mChooseFileSdFragment = new ChooseFileSdFragment();
        mImgVedioMusicFragment = new ChooseFileImgVedioMusicFragment();

        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_choose_file_layout;
    }

    public void initView() {
        mTvPic.setOnClickListener(this);
        mTvMusic.setOnClickListener(this);
        mTvVedio.setOnClickListener(this);
        mTvDoc.setOnClickListener(this);
        mTvMobileMemory.setOnClickListener(this);

        mLLMobileMemory.setOnClickListener(this);
        mLLMusic.setOnClickListener(this);
        mLLVedio.setOnClickListener(this);
        mLLPic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mFgTransaction = mFgManager.beginTransaction();
        Bundle bundle = new Bundle();
        int vid = v.getId();
        if (vid == R.id.tv_pic) {
            bundle.putInt("type", ImgVedioAudioModel.MEDIA_TYPE_IMAGE);
            mImgVedioMusicFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mImgVedioMusicFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();
        } else if (vid == R.id.tv_music) {
            bundle.putInt("type", ImgVedioAudioModel.MEDIA_TYPE_MUSIC);
            mImgVedioMusicFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mImgVedioMusicFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();
        } else if (vid == R.id.tv_vedio) {
            bundle.putInt("type", ImgVedioAudioModel.MEDIA_TYPE_VIDEO);
            mImgVedioMusicFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mImgVedioMusicFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();
        } else if (vid == R.id.tv_doc) {
            bundle.putInt("type", ImgVedioAudioModel.MEDIA_TYPE_DOC);
            mImgVedioMusicFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mImgVedioMusicFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();
        } else if (vid == R.id.tv_sd) {
            bundle.putString("save", "sd");
            mChooseFileSdFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mChooseFileSdFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();
        } else if (vid == R.id.tv_mobile_memory) {
            bundle.putString("save", "mobile");
            mChooseFileSdFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mChooseFileSdFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();
        } else if (vid == R.id.ll_mobile_memory) {
            bundle.putString("save", "mobile");
            mChooseFileSdFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mChooseFileSdFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();

        } else if (vid == R.id.ll_music) {
            bundle.putInt("type", ImgVedioAudioModel.MEDIA_TYPE_MUSIC);
            mImgVedioMusicFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mImgVedioMusicFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();

        } else if (vid == R.id.ll_pic) {
            bundle.putInt("type", ImgVedioAudioModel.MEDIA_TYPE_IMAGE);
            mImgVedioMusicFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mImgVedioMusicFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();

        } else if (vid == R.id.ll_vedio) {
            bundle.putInt("type", ImgVedioAudioModel.MEDIA_TYPE_VIDEO);
            mImgVedioMusicFragment.setArguments(bundle);
            mFgTransaction.replace(R.id.fl_container, mImgVedioMusicFragment, "currentFragment");
            mFgTransaction.addToBackStack(null);
            mFgTransaction.commit();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((BaseActivity) getActivity()).requestPermissions(new BaseActivity.OnPermissionResultListener() {
//
//            @Override
//            public void onAllGranted() {
//                super.onAllGranted();
//                getImgCount();
//                getMusicCount();
//                getVedioCount();
//            }
//
//            @Override
//            public void onAnyDenied(String[] permissions) {
//                BaseToast.makeText(getActivity(), "应用必须获得sd卡读写权限才能正常使用", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onAlwaysDenied(String[] permissions) {
//                String message = "应用必须获得sd卡读写权限才能正常使用";
//                PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
//                permissionDeniedDialog.show();
//            }
//        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        getImgCount();
//        getMusicCount();
//        getVedioCount();
    }

    /**
     * 获取图片的数量
     */
    private void getImgCount() {
        new RxAsyncHelper("").runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                ContentResolver mContentResolver = mContext.getContentResolver();
                Cursor mCursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                mImgCount = mCursor.getCount();
                mHandler.sendEmptyMessage(SCAN_IMG_OK);
                mCursor.close();
                return null;
            }
        }).subscribe();
    }

    /**
     * 获取文稿的数量
     */
    private void getDocCount() {
        new RxAsyncHelper<>("").runInThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {
                ContentResolver mContentResolver = mContext.getContentResolver();
                Cursor mCursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                mImgCount = mCursor.getCount();
                mCursor.close();
                mHandler.sendEmptyMessage(SCAN_IMG_OK);
                return null;
            }
        }).subscribe();
    }

    /**
     * 获取视频的数量
     */
    private void getVedioCount() {
        new RxAsyncHelper<>("").runInThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {
                ContentResolver mContentResolver = mContext.getContentResolver();
                Cursor mCursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                mVedioCount = mCursor.getCount();
                mCursor.close();
                mHandler.sendEmptyMessage(SCAN_VEDIO_OK);
                return null;
            }
        }).subscribe();
    }

    /**
     * 获取音乐的数量
     */
    private void getMusicCount() {
        new RxAsyncHelper<>("").runInThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {
                ContentResolver mContentResolver = mContext.getContentResolver();
                Cursor mCursor = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                mMusicCount = mCursor.getCount();
                mCursor.close();
                mHandler.sendEmptyMessage(SCAN_MUSIC_OK);
                return null;
            }
        }).subscribe();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SCAN_IMG_OK) {
                mTvPic.setText(getString(R.string.conversation_file_pic, mImgCount));
            } else if (msg.what == SCAN_MUSIC_OK) {
                mTvMusic.setText(getString(R.string.conversation_file_music, mMusicCount));
            } else if (msg.what == SCAN_VEDIO_OK) {
                mTvVedio.setText(getString(R.string.conversation_file_vedio, mVedioCount));
            }
        }

    };
}
