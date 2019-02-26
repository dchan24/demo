package com.cmicc.module_message.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.bumptech.glide.manager.SupportRequestManagerFragment;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.HomeActivity;
import com.cmicc.module_message.ui.broadcast.MsgNotificationReceiver;
import com.cmicc.module_message.ui.fragment.BaseChatFragment;
//import com.cmcc.cmrcs.android.ui.fragments.LabelGroupChatFragment;
import com.cmcc.cmrcs.android.ui.interfaces.IFragmentBack;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmcc.cmrcs.android.ui.utils.MessageCursorLoader;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmicc.module_message.utils.RcsAudioPlayer;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by GuoXietao on 2017/3/21.
 * 消息详情界面
 */

public class MessageDetailActivity extends BaseActivity implements OnClickListener {
    public static final String TAG = "MessageDetailActivity";
    BaseChatFragment mFragment;
    private IFragmentBack iFragmentBack;
    Toolbar mToolbar;
    ImageView mIvSlient;
    ImageView groupType;
    TextView mTvTitle;
    ImageView mIvBack;
    boolean mHasOpenConfig;

    TextView mSelectTitle;
    TextView mSelectCount;
    private LinearLayout mChatModeLayout,mSelectModeLayout;

    private boolean mIsOpenFromNotfication;

    // 2018.5.5 YSF 靠近耳边熄屏使用到的类
    public SensorManager mSensorManager;
    public SensorEventListener mSensorEventListener;
    public Sensor mProximitySensor;
    public float defaultScreenBrightness;//默认的屏幕亮度
    public boolean isProximity;//当前是否贴近手机  默认不是  如果是 则不分发事件

    public PowerManager mPowerManager;
    public PowerManager.WakeLock mWakeLock;
    public KeyguardManager mKeyguardManager = null;
    public KeyguardManager.KeyguardLock mKeyguardLock = null;
    public static final int INTO_MULTI_SELECT_MODE = 1;
    public static final int OUT_MULTI_SELECT_MODE = 2;
    private boolean isMultiMode = false;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back) {
            if(isMultiMode){
                changeMode(OUT_MULTI_SELECT_MODE);
            }else{
                onBackPressed();
            }
        }
    }

    @Override
    protected void onResume() {
//        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----message activity onResume----");

        LogF.d(TAG, "MessageDetailActivity onResume");
        super.onResume();
//        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----message activity onResume2----");
        if (mFragment != null && mFragment.getAdapter() != null) {
            mFragment.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogF.d(TAG, "MessageDetailActivity onCreate");
//        openKeyguardLock();
//        preventTouch();
        super.onCreate(savedInstanceState);
        // 当activity被回收后，有Intent来时，onNewIntent不会跑，而是重新onCreated
        Intent intent = getIntent();
        mIsOpenFromNotfication = intent.getBooleanExtra(MessageModuleConst.MsgNotificationReceiverConst.FROM_MSG_NOTIFICATION, false);
        if (intent != null && intent.getBooleanExtra("finish", false)) {
            LogF.d(TAG, "onCreate finish");
            finish();
            return;
        }

        Log.d(TAG, "----------" + getIntent());

        handleIntent();

//        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----message activity setContentView1----");

        setContentView(R.layout.activity_message_detail);
//        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----message activity setContentView3----");

//        AndroidBug5497Workaround.assistActivity(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIsOpenFromNotfication = intent.getBooleanExtra(MessageModuleConst.MsgNotificationReceiverConst.FROM_MSG_NOTIFICATION, false);
        if (intent != null && intent.getBooleanExtra("finish", false)) {
            LogF.d(TAG ,"onNewIntent finish");
            finish();
            return;
        }

        setIntent(intent);
        handleIntent();

        LogF.d(TAG, "onNewIntent: " + getIntent().getStringExtra("address"));

        //initToolbar();
        initData();

    }

    private void handleIntent() {
        Intent intent = getIntent();

        String action = intent.getAction();
        String scheme = intent.getScheme();
        String data = intent.getDataString();
        String draft = intent.getStringExtra("sms_body");
        if (!TextUtils.isEmpty(action) && action.equals("android.intent.action.SENDTO") && !TextUtils.isEmpty(scheme) && scheme.equals("smsto")) {

            if (!TextUtils.isEmpty(data)) {
                try {
                    data = URLDecoder.decode(data, "utf-8");
                    data = data.substring(scheme.length() + 1);
                    data = NumberUtils.getPhone(data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (StringUtil.isPhoneNumber(data)) {
                    data = NumberUtils.getDialablePhoneWithCountryCode(data);
                    String clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS);
                    Bundle bundle = new Bundle();
                    bundle.putString("address", data);
                    if (App.getApplication() == null) {
                        bundle.putString("person", data);
                    } else {
                        bundle.putString("person", NickNameUtils.getNickName(data));
                    }

                    bundle.putBoolean("slient", false);
                    bundle.putLong("loadtime", 0);
                    bundle.putString("clzName", clzName);
                    bundle.putString("draft", draft);
                    intent.putExtras(bundle);
                }
            }
        }

        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----message activity onStart----");


        LogF.d(TAG+"ksbk", "onStart: "+"");
        RxAsyncHelper rxAsyncHelper = new RxAsyncHelper("");
        rxAsyncHelper.runInThread(new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return mFragment.isSlient();
            }
        }).runOnMainThread(new Func1<Boolean, Object>() {
            @Override
            public Object call(Boolean isSlient) {
                if (mIvSlient != null) {
                    mIvSlient.setVisibility(isSlient ? View.VISIBLE : View.INVISIBLE);
                }
                return null;
            }
        }).subscribe();

    }

    @Override
    protected void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar);
        mIvSlient = (ImageView) findViewById(R.id.iv_slient);
        groupType = (ImageView) findViewById(R.id.group_type);
        mTvTitle = (TextView) findViewById(R.id.title);
        mTvTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mSelectTitle = (TextView) findViewById(R.id.tv_title);
        mSelectCount = (TextView) findViewById(R.id.tv_count);

        mChatModeLayout = (LinearLayout) findViewById(R.id.chat_mode_content);
        mSelectModeLayout = (LinearLayout) findViewById(R.id.select_mode_content);


        //更加屏幕尺寸确定Title的最大尺寸
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int pixels = 0 ;
        boolean isPartyGroup = getIntent().getBooleanExtra("isPartyGroup" ,false);
        if(isPartyGroup){
            pixels = (int)AndroidUtil.dip2px(this,180);
        }else{
            pixels = (int)AndroidUtil.dip2px(this,160);
        }
        mTvTitle.setMaxWidth(width - pixels);


        mIvBack = (ImageView) findViewById(R.id.back_arrow);
        findViewById(R.id.back).setOnClickListener(this);
    }

    @Override
    protected void init() {
        initToolbar();
        initData();

    }

    @SuppressLint("RestrictedApi")
    private void initData() {
//        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----message activity initData----");

        Log.d(TAG, "initData: ");
        Bundle bundle = getIntent().getExtras();
        boolean isSlient = bundle.getBoolean("slient", false);

        if(mIvSlient!=null)
            mIvSlient.setVisibility(isSlient ? View.VISIBLE : View.INVISIBLE);

        String clzName = bundle.getString("clzName", "");
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getFragments() != null) {
            for (Fragment fragment : manager.getFragments()) {
                FragmentTransaction transaction = manager.beginTransaction();
                if (!(fragment instanceof SupportRequestManagerFragment))
                    transaction.remove(fragment);
                transaction.commit();
            }
        }
        mFragment = null;
        if (clzName.endsWith("MessageEditorFragment")) {
//            mFragment = MessageEditorFragment.newInstance(bundle);
//            iFragmentBack = (MessageEditorFragment) mFragment;

            Fragment fragment = MessageProxy.g.getUiInterface().getFragment(MessageModuleConst.FragmentType.MESSAGE_EDITOR_FRAGMENG, bundle);
            if(fragment instanceof BaseChatFragment) {
                mFragment = (BaseChatFragment) fragment;
            }
            if(mFragment instanceof IFragmentBack){
                iFragmentBack = (IFragmentBack) mFragment;
            }
        } else if (clzName.endsWith("LabelGroupChatFragment")) {
//            mFragment = LabelGroupChatFragment.newInstance(bundle);
//            iFragmentBack = (LabelGroupChatFragment) mFragment;

            Fragment fragment = MessageProxy.g.getUiInterface().getFragment(MessageModuleConst.FragmentType.LABEL_GROUP_CHAT_FRAGMENT, bundle);
            if(fragment instanceof BaseChatFragment) {
                mFragment = (BaseChatFragment) fragment;
            }
            if(mFragment instanceof IFragmentBack){
                iFragmentBack = (IFragmentBack) mFragment;
            }
        } else if (clzName.endsWith("GroupChatFragment")) {
            Fragment fragment = MessageProxy.g.getUiInterface().getFragment(MessageModuleConst.FragmentType.GROUP_CHAT_FRAGMENT, bundle);
            if(fragment instanceof BaseChatFragment) {
                mFragment = (BaseChatFragment) fragment;
            }
            if(mFragment instanceof IFragmentBack){
                iFragmentBack = (IFragmentBack) mFragment;
            }

        } else if (clzName.endsWith("PublicAccountChatFrament")) {
          //  mFragment = PublicAccountChatFrament.newInstance(bundle);
            Fragment fragment = MessageProxy.g.getUiInterface().getFragment(MessageModuleConst.FragmentType.PUBLIC_ACCOUNT_FRAGMENT, bundle);
            if(fragment instanceof BaseChatFragment) {
                mFragment = (BaseChatFragment) fragment;
            }
            if(mFragment instanceof IFragmentBack){
                iFragmentBack = (IFragmentBack) mFragment;
            }
        } else if (clzName.endsWith("MmsSmsFragment")) {
            Fragment fragment = MessageProxy.g.getUiInterface().getFragment(MessageModuleConst.FragmentType.MESSAGE_EDITOR_FRAGMENG, bundle);
            if(fragment instanceof BaseChatFragment) {
                mFragment = (BaseChatFragment) fragment;
            }
            if(mFragment instanceof IFragmentBack){
                iFragmentBack = (IFragmentBack) mFragment;
            }
//            mFragment = MmsSmsFragment.newInstance(bundle);
//            iFragmentBack = (MmsSmsFragment) mFragment;
        }else if (clzName.endsWith("PcMessageFragment")) {
            Fragment fragment = MessageProxy.g.getUiInterface().getFragment(MessageModuleConst.FragmentType.PC_MESSAGE_FRAGMENT, bundle);
            if(fragment instanceof BaseChatFragment) {
                mFragment = (BaseChatFragment) fragment;
            }
            if(mFragment instanceof IFragmentBack){
                iFragmentBack = (IFragmentBack) mFragment;
            }
//            mFragment = MmsSmsFragment.newInstance(bundle);
//            iFragmentBack = (MmsSmsFragment) mFragment;
        } else {
            Fragment fragment = MessageProxy.g.getUiInterface().getFragment(MessageModuleConst.FragmentType.MESSAGE_EDITOR_FRAGMENG, bundle);
            if(fragment instanceof BaseChatFragment) {
                mFragment = (BaseChatFragment) fragment;
            }
            if(mFragment instanceof IFragmentBack){
                iFragmentBack = (IFragmentBack) mFragment;
            }
        }
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mFragment, R.id.contentFrame);

    }

    public static void show(Context context, Bundle bundle) {
		try {
			Intent intent = new Intent(context, MessageDetailActivity.class);
			intent.putExtras(bundle);
			context.startActivity(intent);
		}catch (Exception e){
			e.printStackTrace();
			LogF.e(TAG ,"jump to MessageDetailActivity fail:"+e.getMessage());
			Intent intent = new Intent(context, MessageDetailActivity.class);
			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if(ab == null)
            return;
        //不显示左上角图标和title，采用自定义的
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public boolean isOpenFromNotfication() {
        return mIsOpenFromNotfication;
    }

    @Override
    public void onBackPressed() {
        if(mIsOpenFromNotfication){
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(MessageModuleConst.MsgNotificationReceiverConst.FROM_MSG_NOTIFICATION, true);
            startActivity(intent);
        }else {
            if (iFragmentBack != null) {
                iFragmentBack.onFragmentBack();
            }
        }
    }

    public void changeMode(int mode){

        switch (mode){
            case INTO_MULTI_SELECT_MODE:
                mSelectModeLayout.setVisibility(View.VISIBLE);
                mChatModeLayout.setVisibility(View.GONE);
                mIvBack.setImageResource(R.drawable.cc_chat_checkbox_close);
                isMultiMode = true;
                mFragment.changeMode(INTO_MULTI_SELECT_MODE);
                break;
            case OUT_MULTI_SELECT_MODE:
                mSelectModeLayout.setVisibility(View.GONE);
                mChatModeLayout.setVisibility(View.VISIBLE);
                mIvBack.setImageResource(R.drawable.common_back_selector);
                isMultiMode = false;
                mFragment.changeMode(OUT_MULTI_SELECT_MODE);
                break;

            default:
                break;
        }
    }

    public void onCheckChange(int selectedCount){

        if(selectedCount > 0){
            mSelectTitle.setText(getText(R.string.has_selected));
            mSelectCount.setVisibility(View.VISIBLE);
            mSelectCount.setText(String.valueOf(selectedCount));
            if(selectedCount > 9){
                mSelectCount.setBackgroundResource(R.drawable.cc_chat_checkbox_doubledigit);
            }else{
                mSelectCount.setBackgroundResource(R.drawable.cc_chat_checkbox_singledigit);
            }
        }else{
            mSelectTitle.setText(getText(R.string.hasnot_select));
            mSelectCount.setVisibility(View.GONE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            mIvSlient.setVisibility(data.getBooleanExtra("slient", false) ? View.VISIBLE : View.INVISIBLE);
        }
//        if (requestCode == ExpressionFragment.GET_GIF && resultCode == ExpressionFragment.GIF_RESULT_OK) {
//            if (data != null && mFragment != null) {
//                mFragment.onActivityResult(requestCode, resultCode, data);
//            }
//        }
//        if(requestCode	==	ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()){
//            if (data != null && mFragment != null) {
//                mFragment.onActivityResult(requestCode, resultCode, data);
//            }
//        }

        if(mFragment!=null){
            mFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public Toolbar getmToolbar() {
        return mToolbar;
    }

    public ImageView getmIvSlient() {
        return mIvSlient;
    }

    public ImageView getmIvBack() {
        return mIvBack;
    }

    public TextView getmTvTitle() {
        return mTvTitle;
    }

    public ImageView getGroupType(){
        return groupType;
    }

    @Override
    protected void onDestroy() {
        releaseWakeLock();
        if(mFragment!=null){
            mFragment.addressCleared();
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogF.d(TAG+"ksbk", "onStop: "+"");
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (mFragment == null)
            return;
        mFragment.stopMessageAudio();
    }

    //开启键盘锁以及亮屏  保证手机在锁屏熄屏情况下 可以唤醒
    @SuppressLint("MissingPermission")
    private void openKeyguardLock() {
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //获取电源管理器 开启键盘锁 以及点亮屏幕 之后可以释放锁
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mPowerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)){
            PowerManager.WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "wakeLock");
            if (!wakeLock.isHeld()){
                wakeLock.acquire();
            }
            wakeLock.release();
        }
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        // 初始化键盘锁，可以锁定或解开键盘锁
        mKeyguardLock = mKeyguardManager.newKeyguardLock("");
        // 禁用显示键盘锁定
        mKeyguardLock.disableKeyguard();
    }
    //防触摸处理
    private void preventTouch() {
        //版本在api21以上  并且当前设备支持 接近屏幕熄灭的电源管理器  该管理器可以在靠近手机时关闭屏幕
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mPowerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "CALL_ACTIVITY" + "#" + getClass().getName());
        }
        // 通过接近传感器来实现将手机屏幕亮度变低 远离是恢复
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
        if (sensorList != null && sensorList.size() > 0){
            mProximitySensor = sensorList.get(0);
        }
    }

    /**
     * 重新上锁 释放电源管理器
     */
    @SuppressLint("MissingPermission")
    private void releaseWakeLock() {
        try {
            if (mKeyguardLock != null) {
                mKeyguardLock.reenableKeyguard();
                mKeyguardLock = null;
            }
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        } catch (Exception e) {
            Log.e("AndroidRuntime", e.toString());
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onPause() {
        LogF.d(TAG+"ksbk", "onPause: "+"");
        super.onPause();
        if (mSensorEventListener != null && mSensorManager != null){
            mSensorManager.unregisterListener(mSensorEventListener); // 需取注册，注册地方在Adapter里面
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mSensorManager != null && mSensorEventListener != null && mProximitySensor != null) { //
            mSensorManager.registerListener( mSensorEventListener , mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                RcsAudioPlayer.getInstence(this).setUp( this );
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                RcsAudioPlayer.getInstence(this).setDoew( this );
                return true;
            case KeyEvent.KEYCODE_BACK:
                if(isMultiMode){
                    changeMode(OUT_MULTI_SELECT_MODE);
                }else{
                    onBackPressed();
                }
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if(!mHasOpenConfig){
                mHasOpenConfig = true;
                openKeyguardLock();
                preventTouch();
            }
            Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----MessageDetail onWindowFocusChanged----");
        }
    }
}
