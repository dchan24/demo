package com.cmicc.module_message.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmicc.module_message.ui.constract.MessageVideoContract;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmicc.module_message.ui.presenter.MessaegVideoPresenter;
import com.cmicc.module_message.R;

/**
 * Created by Tiu on 2017/7/7.
 */

public class MessageVideoFragment extends BaseFragment implements MessageVideoContract.View {

    MessageVideoContract.Presenter mPresenter;
    SurfaceView mSurface;
    ImageView mControlImageView;
    TextView mCurrentPositionTextView;
    SeekBar mControlSeekBar;
    TextView mEndPositionTextView;
    ImageView mIvFrame;
    ImageView mIvClose;
    FrameLayout mFlClose;
    FrameLayout mFlControl;
    private LinearLayout mLlControl;

    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        mSurface = (SurfaceView) rootView.findViewById(R.id.surface);
        mControlImageView = (ImageView) rootView.findViewById(R.id.control_imageView);
        mCurrentPositionTextView = (TextView) rootView.findViewById(R.id.currentPosition_textView);
        mControlSeekBar = (SeekBar) rootView.findViewById(R.id.control_seekBar);
        mEndPositionTextView = (TextView) rootView.findViewById(R.id.endPosition_textView);
        mIvFrame = (ImageView) rootView.findViewById(R.id.iv_frame);
        mIvClose = (ImageView) rootView.findViewById(R.id.iv_close);
        mFlClose = (FrameLayout) rootView.findViewById(R.id.fl_close);
        mFlControl = (FrameLayout) rootView.findViewById(R.id.fl_control);
        mLlControl = (LinearLayout)rootView.findViewById(R.id.layout_control);

    }

    @Override
    public void initData() {
        mPresenter.start();
        mControlImageView.setImageResource(R.drawable.playbar_video_selector);
        mFlControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.handleControl();
            }
        });
        mFlClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_message_video;
    }

    public void setPresenter(MessaegVideoPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
//        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        unbinder.unbind();
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return mSurface.getHolder();
    }

    @Override
    public Bundle getDataSource() {
        return getArguments();
    }

    @Override
    public void setControlViewPlay(boolean isplay) {
        if (!isplay) {
            mControlImageView.setImageResource(R.drawable.playbar_video_selector);
        } else {
            mControlImageView.setImageResource(R.drawable.pausebar_video_selector);
        }
    }

    @Override
    public SeekBar getSeekBar() {
        return mControlSeekBar;
    }

    @Override
    public void updateCurrentTextView(String format) {
        mCurrentPositionTextView.setText(format);
    }

    @Override
    public void updateEndPositionTextView(String format) {
        mEndPositionTextView.setText(format);
    }

    @Override
    public void showFrame(boolean b) {
        mIvFrame.setVisibility(b == true ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateSurfaceView(FrameLayout.LayoutParams layoutParams) {
        mSurface.setLayoutParams(layoutParams);
    }

    @Override
    public void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }
} 
