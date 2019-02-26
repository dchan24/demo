package com.cmicc.module_message.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chinamobile.app.yuliao_common.utils.BroadcastActions;
import com.chinamobile.app.yuliao_contact.model.BaseContact;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.PreviewImageFragment;
import com.cmicc.module_message.ui.presenter.PreviewImagePresenter;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;


/**
 * Created by GuoXietao on 2017/4/27.
 * 图片预览界面
 */

public class PreviewImageActivity extends BaseActivity {

    PreviewImagePresenter mPresenter;
    PreviewImageFragment mView;
    MyBroadcast mMyBroadcast;

    public int mScreenHeight;
    public int mScreenWidth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //Log.i("VampireLog", "start Activity" + System.currentTimeMillis());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        mScreenWidth = dm.widthPixels;
//        mScreenHeight = dm.heightPixels;
    }

    @Override
    protected void findViews() {

    }

    @Override
    protected void init() {
        mMyBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastActions.ACTION_SEARCH_CONTACT_RESULT);
        registerReceiver(mMyBroadcast ,intentFilter);
        mPresenter = new PreviewImagePresenter(this);

        if (mView == null) {
            mView = new PreviewImageFragment();
            mView.setArguments(getIntent().getExtras());
        }

        mPresenter.setView(mView);
        mView.setPresenter(mPresenter);

        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mView, R.id.contentFrame);

    }

    @Override
    public void onBackPressed() {
        mView.onBackPressed();
//        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fixInputMethodManagerLeak(this);
        if(mMyBroadcast != null){
            unregisterReceiver(mMyBroadcast);
        }
        mPresenter.onDestory();
        //LogF.i("PreviewImageActivity", "onDestory");
        mPresenter = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mView.onActivityResult(requestCode, resultCode, data);
    }

    private class MyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BroadcastActions.ACTION_SEARCH_CONTACT_RESULT)){
                Bundle bundle = intent.getExtras();
                BaseContact baseContact = (BaseContact) bundle.getSerializable("contact");
                SimpleContact simpleContact = null;
                if(baseContact != null){
                    simpleContact = new SimpleContact();
                    simpleContact.setRawId(baseContact.getRawId());
                    simpleContact.setNumber(baseContact.getNumber());
                    simpleContact.setName(baseContact.getName());
                }
//                SimpleContact simpleContact = (SimpleContact) intent.getSerializableExtra("contact");
                String phone = intent.getStringExtra("number");
                mPresenter.searchContactResult(simpleContact ,phone);
            }
        }
    }
}
