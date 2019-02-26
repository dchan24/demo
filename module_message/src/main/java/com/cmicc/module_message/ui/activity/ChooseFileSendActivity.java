package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.ChooseFileTypeFragment;
import com.cmicc.module_message.R;

/**
 * Created by tigger on 2017/7/3.
 * 选择文件发送界面
 */

public class ChooseFileSendActivity extends BaseActivity {

    private FragmentManager mFgManager;
    private String mAddress;
    private String mSendFileAction;
    private TextView mTitle;
    private RelativeLayout mBack;

    public static int REQUEST_CODE = 100;

    public static final String ONLY_FINISH_ACTIVITY_KEY = "only_finish_activity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_file_send_layout);
    }

    @Override
    protected void findViews() {

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
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(R.string.select_file);
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mBack = (RelativeLayout) findViewById(R.id.back);
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle("");
//        toolbar.setTitleTextColor(getResources().getColor(R.color.color_3d5b96));
        toolbar.setSubtitle(null);
        toolbar.setLogo(null);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView(){
        mFgManager.beginTransaction()
                .add(R.id.fl_container, new ChooseFileTypeFragment(), "type").commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data) {
        if(requestCode == REQUEST_CODE && result == Activity.RESULT_OK){
            boolean justFinish = data.getBooleanExtra(ONLY_FINISH_ACTIVITY_KEY, false);
            if(justFinish){
                this.finish();
            }else{
                setResult(Activity.RESULT_OK,data);
                this.finish();
            }
        }
        super.onActivityResult(requestCode, result, data);
    }
}
