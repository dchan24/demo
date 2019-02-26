package com.cmicc.module_message.ui.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.interfaces.IFragmentBack;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.fragment.ChooseFileFragment;

/**
 * Created by tigger on 2017/7/3.
 * 选择本地文件界面
 */

public class ChooseLocalFileActivity extends BaseActivity {

    private FragmentManager mFgManager;
    private String mAddress;
    private String mSendFileAction;
    private IFragmentBack iFragmentBack;

    public TextView mTvSend;
    TextView  mToolbarTitle;

    View mBottombar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_local_file_layout);
    }

    @Override
    protected void findViews() {
        mTvSend = (TextView)findViewById(R.id.button_send);
        mBottombar = findViewById(R.id.rl_panel);
    }

    @Override
    protected void init() {
        mFgManager = getSupportFragmentManager();
        initParams();
        initToolBar();
        initView();
    }

    private void initParams() {
        Intent intent = getIntent();
        if (null != intent && null != intent.getExtras()) {
            mAddress = intent.getExtras().getString(LogicActions.PHONE_NUMBER);
            mSendFileAction = intent.getExtras().getString(LogicActions.NOTIFICATION_GOTO_SEND_FILE_ACTION);
        }
    }

    public String getAddress() {
        return mAddress;
    }

    public String getSendFileAction() {
        return mSendFileAction;
    }

    private void initToolBar() {
//        mToolbar.setTitle(R.string.local_files);
////        toolbar.setTitleTextColor(getResources().getColor(R.color.color_3d5b96));
//        mToolbar.setSubtitle(null);
//        mToolbar.setLogo(null);
//        setSupportActionBar(mToolbar);
//        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        ImageView toolbarBackBtn = (ImageView)findViewById(R.id.select_picture_custom_toolbar_back_btn);
        mToolbarTitle = (TextView)findViewById(R.id.select_picture_custom_toolbar_title_text);
        mToolbarTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mToolbarTitle.setText(R.string.select_file);
//        toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });
        findViewById(R.id.left_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initView(){
        mFgManager.beginTransaction()
                .add(R.id.fl_container, new ChooseFileFragment(), "choosefile").commit();
    }

    public void setFragmentBack(IFragmentBack callback){
        iFragmentBack = callback;
    }

    @Override
    public void onBackPressed() {
        if(iFragmentBack!=null){
            if(iFragmentBack.onFragmentBack()) {
                return;
            }
        }
        mBottombar.setVisibility(View.GONE);
        mToolbarTitle.setText(R.string.select_file);
        super.onBackPressed();
    }

    public void setToolBarTitle(String title){
        mToolbarTitle.setText(title);
    }

    public void setSendButtonStutus(boolean enable){
        mTvSend.setEnabled(enable);
    }
}
