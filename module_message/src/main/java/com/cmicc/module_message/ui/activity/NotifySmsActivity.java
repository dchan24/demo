package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmcc.cmrcs.android.ui.view.dragbubble.DragBubbleView;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.NotifySmsContract;
import com.cmicc.module_message.ui.fragment.NotifySmsFragment;
import com.cmicc.module_message.ui.presenter.NotifySmsPresenterImpl;
import com.cmicc.module_message.ui.presenter.PlatformConvPresenterImpl;
import com.constvalue.MessageModuleConst;

/**
 * Created by tigger on 2017/7/27.
 */

public class NotifySmsActivity extends BaseActivity {
    public static final String TAG = "NotifySmsActivity";
    public static final String SOURCE = "source";

    private NotifySmsFragment mNotifySmsFragment;
    DragBubbleView dragBubble;
    private int source;
    private int mode = MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE;
    private TextView title ,selectAll, count;
    private ImageView mBackBtn ,mCancelBtn;

    public static void show(Context context,int source){
        Intent intent = new Intent(context, NotifySmsActivity.class);
        intent.putExtra(SOURCE,source);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        source = getIntent().getIntExtra(SOURCE,MessageModuleConst.NotifySmsActivityConst.SOURCE_NOTIFYSMS);
        setContentView(R.layout.activity_notify_sms_list);
        dragBubble = (DragBubbleView) findViewById(R.id.dragBubble);
    }

    @Override
    protected void findViews() {
        initToolBar();
    }

    @Override
    protected void init() {
        if(mNotifySmsFragment == null){
            mNotifySmsFragment = NotifySmsFragment.newInstantce();
        }

        NotifySmsContract.Presenter mPresenter = getPresenter();

        mNotifySmsFragment.setPresenter(mPresenter);
        mNotifySmsFragment.setSource(source);
        mPresenter.start();
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mNotifySmsFragment, R.id.contentFrame);
    }

    @NonNull
    private NotifySmsContract.Presenter getPresenter() {
        switch (source){
            case MessageModuleConst.NotifySmsActivityConst.SOURCE_NOTIFYSMS:
                return new NotifySmsPresenterImpl(this,
                        mNotifySmsFragment, getSupportLoaderManager());
            case MessageModuleConst.NotifySmsActivityConst.SOURCE_PLATFORMCONV:
                return new PlatformConvPresenterImpl(this,
                        mNotifySmsFragment, getSupportLoaderManager());
        }
        return new NotifySmsPresenterImpl(this,
                mNotifySmsFragment, getSupportLoaderManager());
    }

    private void initToolBar() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        mBackBtn = (ImageView) findViewById(R.id.select_picture_custom_toolbar_back_btn);
        mCancelBtn = (ImageView) findViewById(R.id.select_picture_custom_toolbar_cancel_btn);
        title  = (TextView)findViewById (R.id.select_picture_custom_toolbar_title_text);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        count = (TextView) findViewById(R.id.tv_count);
        selectAll = (TextView) findViewById(R.id.btn_editimage);
        if (source==MessageModuleConst.NotifySmsActivityConst.SOURCE_NOTIFYSMS){
            title.setText(R.string.notify_sms);
        }else {
            title.setText(R.string.platform_conv);
        }
        mBackBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mCancelBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                changeMode(MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE);
                mNotifySmsFragment.changeMode(MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE);
            }
        });
        findViewById(R.id.left_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode == MessageModuleConst.NotifySmsActivityConst.INTO_MULTIDELETE_MODE){
                    changeMode(MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE);
                    mNotifySmsFragment.changeMode(MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE);
                }else{
                    finish();
                }
            }
        });

        selectAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode == MessageModuleConst.NotifySmsActivityConst.INTO_MULTIDELETE_MODE){
                    if(selectAll.getText().equals(getText(R.string.total_selection))){
                        mNotifySmsFragment.selectAll();
                        selectAll.setText(R.string.cancel_total_selection);
                    }else if(selectAll.getText().equals(getText(R.string.cancel_total_selection))){
                        mNotifySmsFragment.cancelSelectAll();
                        selectAll.setText(R.string.total_selection);
                    }
                }
            }
        });
    }

    public DragBubbleView getDragBubble() {
        return dragBubble;
    }

    public void changeMode(int mode){
        this.mode = mode;
        LogF.d(TAG,"changeMode mode = " + mode);
        switch (mode){
            case MessageModuleConst.NotifySmsActivityConst.INTO_MULTIDELETE_MODE:
                LogF.i(TAG,"changeMode 进入批量删除模式 ");
                title.setText(getText(R.string.has_selected));
                mBackBtn.setVisibility(View.GONE);
                mCancelBtn.setVisibility(View.VISIBLE);
                count.setVisibility(View.VISIBLE);
                selectAll.setVisibility(View.VISIBLE);
                selectAll.setTextColor(Color.parseColor("#FF157CF8"));
                selectAll.setText(getText(R.string.total_selection));
                break;
            case MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE:
                LogF.i(TAG,"changeMode 退出批量删除模式 ");
                if (source==MessageModuleConst.NotifySmsActivityConst.SOURCE_NOTIFYSMS){
                    title.setText(R.string.notify_sms);
                }else {
                    title.setText(R.string.platform_conv);
                }
                mBackBtn.setVisibility(View.VISIBLE);
                mCancelBtn.setVisibility(View.GONE);
                count.setVisibility(View.GONE);
                selectAll.setVisibility(View.GONE);
                selectAll.setTextColor(Color.parseColor("#d5d5d5"));
                break;
            default:
                    break;
        }
    }

    public void onCheckChange(int selectedCount,int dataSize){

        LogF.d(TAG,"onCheckChange selectedCount = " + selectedCount);
        if(selectedCount > 0){
            title.setText(getText(R.string.has_selected));
            count.setText(String.valueOf(selectedCount));
            count.setVisibility(View.VISIBLE);
            if(selectedCount > 9){
                count.setBackgroundResource(R.drawable.cc_alldelete_dot2);
            }else{
                count.setBackgroundResource(R.drawable.cc_alldelete_dot1);
            }
            if(selectedCount == dataSize){
                selectAll.setText(R.string.cancel_total_selection);
            }else{
                selectAll.setText(R.string.total_selection);
            }
        }else{
            title.setText(getText(R.string.hasnot_select));
            count.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //  返回键退出批量删除模式
        if(keyCode == KeyEvent.KEYCODE_BACK && mode == MessageModuleConst.NotifySmsActivityConst.INTO_MULTIDELETE_MODE){
            changeMode(MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE);
            mNotifySmsFragment.changeMode(MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE);
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }
}
