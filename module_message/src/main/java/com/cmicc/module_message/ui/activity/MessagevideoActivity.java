package com.cmicc.module_message.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.MessageVideoFragment;
import com.cmicc.module_message.ui.presenter.MessaegVideoPresenter;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;

/**
 * Created by Tiu on 2017/7/7.
 * 视频消息界面
 */

public class MessagevideoActivity extends BaseActivity {

    MessageVideoFragment mView;
    MessaegVideoPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_message_video);
//        ButterKnife.bind(this);
        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();


        findViewById(R.id.contentFrame).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mPresenter.showDialog(bundle);
//                mPresenter.saveVideo(bundle);
                return false;
            }
        });

    }

    @Override
    protected void findViews() {

    }

    @Override
    protected void init() {
        if (mView == null) {
            mView = new MessageVideoFragment();
            mView.setArguments(getIntent().getExtras());
        }
        mPresenter = new MessaegVideoPresenter(this);
        mView.setPresenter(mPresenter);
        mPresenter.setView(mView);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mView, R.id.contentFrame);
    }
}
