package com.cmicc.module_message.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.sysetem.MetYouActivityManager;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_common.utils.Threads.HandlerThreadFactory;
import com.chinamobile.app.yuliao_common.utils.permission.PermissionUtil;
import com.chinamobile.manager.XFAudioToTextManager;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.callback.SendAudioTextCallBack;
import com.cmcc.cmrcs.android.ui.dialogs.PermissionDeniedDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.message.RcsAudioRecorder;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.listener.AudioTypeSelectListener;
import com.cmicc.module_message.ui.view.RecordAnimView;
import com.cmicc.module_message.ui.view.SendAudioNotNetDialog;
import com.constvalue.MessageModuleConst;
import com.iflytek.cloud.SpeechError;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class MessageAudioTextFragment  extends BaseFragment implements View.OnClickListener ,XFAudioToTextManager.AudioTextListener,AudioTypeSelectListener {

    private static final String TAG = "MessageAudioTextFragment";
    private WeakReference<SendAudioTextCallBack> mSendAudioTextCallback;
    private int mPageTye;

    private View mRecordRootView;
    private ImageView mRecoderBtn;
   // private RecordWaveView mRecordWaveView;
    //    private VoiceAnimator mRecordWaveView;
    private RecordAnimView mRecordWaveView;
    private View mRecordOperationRotView;
    private ProgressBar mProgressBar;
    private TextView mEixtRecord;
    private TextView mFinishRecord;
    private TextView mRecoderTip;
    private TextView mRecoderTimeUpTip;


//    private View mSendTypeSelectRootView;
//    private TextView mSendAudioAndTextView;
//    private ImageView mSendAudioAndTextSelectIcon;
//    private TextView mSendTextView;
//    private ImageView mSendTextSelectIcon;
//    private TextView mSendAudioView;
//    private ImageView mSendAudioSelectIcon;
//    private TextView mCancelSendSelect;
//    private TextView mConfirmSendSelect;


    private final int AUDIO_STOP_FROM_OTHER = 0;
    private final int AUDIO_STOP_FROM_EXIT = 1;
    private final int AUDIO_STOP_FROM_FINISH = 2;
    private final int AUDIO_STOP_FROM_ETMESSAGE_INTERRUPT = 3;
    private final int AUDIO_STOP_FROM_ERROR = 4;
    private final int AUDIO_STOP_FROM_END_SPEECH_INTERRUPT = 5;
    public int mAudioStopType = AUDIO_STOP_FROM_OTHER;

    private final int AUDIO_AND_TEXT_MODE_TIME_OUT = 3*1000; //单位ms
    private final int AUDIO_MODE_TIME_OUT = 60*1000;//单位ms
    private int mTimeOut = AUDIO_AND_TEXT_MODE_TIME_OUT;

    public final int ONE_SECOND = 1000;
    public final int MAX_RECORD_TIME = 59;
    public final int RECODER_WARNING_TIME = 55;
    private int mRecorderTime = 0;
    private AudioTypeSelectFragment mSelectTypeDialog;
    private SendAudioNotNetDialog mNetDialog;
    private String mErrorInterruptResult; //这个是为了在发送文字模式下，保存识别过程中由于错误中断识别的识别结果
    private String mLastResult;//上一次语音识别的结果
    private boolean mRecordStop = false;


    private int mSelectSendStype = MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT;


    public static final String SEND_TYPE_SP_KEY = "send_audio_or_text_select_type";

    private String START_SAY_TIP;
    private String SETTING_TEXT = "设置";
    private String SEND_RECORD = "发送";
    private boolean mViewInited = false;
    private PowerManager.WakeLock mWakelock;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void initViews(View rootView) {
        super.initViews(rootView);
        Activity attachActivity = getActivity();
        if(attachActivity == null)
            return;
        START_SAY_TIP = getResources().getString(R.string.touch_start_say);
        SETTING_TEXT = getResources().getString(R.string.record_setting);
        SEND_RECORD = getResources().getString(R.string.send_record);

        initRecordView(rootView);
        initSendVideoSelectView(rootView);
        mSelectSendStype = (Integer) SharePreferenceUtils.getParam(MyApplication.getAppContext(), SEND_TYPE_SP_KEY, MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT);
        mViewInited = true;
        if (mPageTye == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_VOICE_PAGE) {
            changeToSettingPage();
        } else {
            changeToRecordPage();
        }

    }

    private void initRecordView(View rootView) {
        mRecordRootView = rootView.findViewById(R.id.message_audio_record_root_view);
        mRecoderBtn = (ImageView) rootView.findViewById(R.id.record_audio_btn);
        mRecordWaveView = (RecordAnimView) rootView.findViewById(R.id.record_audio_wave_anim);
        mRecordOperationRotView = rootView.findViewById(R.id.recodr_audio_exit);
        mEixtRecord = (TextView) rootView.findViewById(R.id.recodr_audio_exit);
        mFinishRecord = (TextView) rootView.findViewById(R.id.recodr_audio_finish);
        mRecoderTip = (TextView) rootView.findViewById(R.id.recoder_tip);
        mRecoderTimeUpTip = (TextView) rootView.findViewById(R.id.recoder_time_up_tip);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.recoder_progress_bar);
        mEixtRecord.setOnClickListener(this);
        mFinishRecord.setOnClickListener(this);
        mRecoderBtn.setOnClickListener(this);

//        mRecordWaveView.setAnimationMode(VoiceAnimator.AnimationMode.ANIMATION);

    }

    private void initSendVideoSelectView(View rootView) {

//        mSendTypeSelectRootView = rootView.findViewById(R.id.select_send_audio_type_root_view);
//
//        mSendAudioAndTextView = (TextView)rootView.findViewById(R.id.select_send_audio_and_text);
//        mSendAudioAndTextSelectIcon = (ImageView)rootView.findViewById(R.id.select_send_audio_and_text_icon);
//        mSendAudioAndTextSelectIcon.setOnClickListener(this);
//        mSendTextView = (TextView)rootView.findViewById(R.id.select_send_text);
//        mSendTextSelectIcon = (ImageView)rootView.findViewById(R.id.select_send_text_icon);
//        mSendTextSelectIcon.setOnClickListener(this);
//        mSendAudioView = (TextView)rootView.findViewById(R.id.select_send_voice);
//        mSendAudioSelectIcon = (ImageView)rootView.findViewById(R.id.select_send_voice_icon);
//        mSendAudioSelectIcon.setOnClickListener(this);
//
//        mCancelSendSelect = (TextView)rootView.findViewById(R.id.select_send_audio_type_cancel);
//        mCancelSendSelect.setOnClickListener(this);
//        mConfirmSendSelect = (TextView)rootView.findViewById(R.id.select_send_audio_type_confirm);
//        mConfirmSendSelect.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }


   private boolean hasRecoderPermission(){
        MediaRecorder mediaRecorder = null;
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.prepare();
        } catch (Exception exception) {
            String ex = exception.getMessage();
            if(ex.contains("Permission deny")) {
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                return false;
            }
        }
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        return true;
    }

    private void changeToSettingPage() {
//        if (mSelectTypeDialog == null) {
        mSelectTypeDialog = new AudioTypeSelectFragment();
        mSelectTypeDialog.init(this);
//        }
        mSelectTypeDialog.show(getActivity().getSupportFragmentManager(), AudioTypeSelectFragment.FRAGMENT_TAG,mIsGroupChat);
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        if( fragmentManager.findFragmentByTag("audio_type_select_fragment") != null && mSelectTypeDialog!=null){
//            transaction.show(mSelectTypeDialog);
//        }else {
//            transaction.add(com.cmic.module_base.R.id.fl_more, mSelectTypeDialog, "audio_type_select_fragment");
//        }

//        mSendTypeSelectRootView.setVisibility(View.VISIBLE);
//        mRecordRootView.setVisibility(View.GONE);
//        initSelectSendType();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        if(hidden == false){
//            if(mPageTye == ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_VOICE_PAGE){
//                mSendTypeSelectRootView.setVisibility(View.VISIBLE);
//                mRecordRootView.setVisibility(View.GONE);
//            }else{
//                changeToRecordPage();
//            }
//        }
    }

    private void initSelectSendType() {
//        mSelectSendStype = (Integer) SharePreferenceUtils.getParam(MyApplication.getAppContext(), SEND_TYPE_SP_KEY,ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT);;
//        if(mSelectSendStype == ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT){
//            mSendAudioAndTextSelectIcon.setSelected(true);
//            mSendTextSelectIcon.setSelected(false);
//            mSendAudioSelectIcon.setSelected(false);
//        }else if(mSelectSendStype == ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_TEXT){
//            mSendAudioAndTextSelectIcon.setSelected(false);
//            mSendTextSelectIcon.setSelected(true);
//            mSendAudioSelectIcon.setSelected(false);
//        }else if(mSelectSendStype == ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
//            mSendAudioAndTextSelectIcon.setSelected(false);
//            mSendTextSelectIcon.setSelected(false);
//            mSendAudioSelectIcon.setSelected(true);
//        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.recodr_audio_exit) {
            mAudioStopType = AUDIO_STOP_FROM_EXIT;
            if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
                stopRcsAudioRecord();
                Activity activity = getActivity();
                if(mIsGroupChat && activity!=null) {
                    UmengUtil.buryPoint(activity, "groupmessage_talk_quit", "消息-群聊-语音-退出", 0);
                }else if(activity!=null){
                    UmengUtil.buryPoint(activity, "p2pmessage_talk_quit", "消息-点对点会话-语音-退出", 0);
                }
                return;
            }
            XFAudioToTextManager.getInstance().stop();

        } else if (id == R.id.recodr_audio_finish) {
            if (!SETTING_TEXT.endsWith(mFinishRecord.getText().toString())) {
                mAudioStopType = AUDIO_STOP_FROM_FINISH;
                mRecoderTip.setText(START_SAY_TIP);
                mRecordWaveView.setVisibility(View.GONE);
                mRecoderBtn.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mFinishRecord.setText(SETTING_TEXT);
                mRecoderTimeUpTip.setVisibility(View.GONE);
                if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
                    stopRcsAudioRecord();
                    return;
                }
                XFAudioToTextManager.getInstance().stop();
                Activity activity = getActivity();
                if(mIsGroupChat&& activity!=null) {
                    UmengUtil.buryPoint(activity, "groupmessage_talk_send", "消息-群聊-语音-发送", 0);
                }else if(activity!=null){
                    UmengUtil.buryPoint(activity, "p2pmessage_talk_send", "消息-点对点会话-语音-发送", 0);
                }
            } else {
                changeToSettingPage();
                Activity activity = getActivity();
                if(mIsGroupChat && activity!=null) {
                    UmengUtil.buryPoint(activity, "groupmessage_talk_setup", "消息-群聊-语音-设置", 0);
                }else if(activity!=null){
                    UmengUtil.buryPoint(activity, "p2pmessage_talk_setup", "消息-点对点会话-语音-发送-设置", 0);
                }
            }

        }/*else if(id == R.id.select_send_audio_and_text_icon){
            mSelectSendStype = ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT;
            mSendAudioAndTextSelectIcon.setSelected(true);
            mSendTextSelectIcon.setSelected(false);
            mSendAudioSelectIcon.setSelected(false);
        }else if(id == R.id.select_send_text_icon){
            mSelectSendStype =  ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_TEXT;
            mSendAudioAndTextSelectIcon.setSelected(false);
            mSendTextSelectIcon.setSelected(true);
            mSendAudioSelectIcon.setSelected(false);
        }else if(id == R.id.select_send_voice_icon){
            mSelectSendStype =  ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_AUDIO;
            mSendAudioAndTextSelectIcon.setSelected(false);
            mSendTextSelectIcon.setSelected(false);
            mSendAudioSelectIcon.setSelected(true);
        }else if(id == R.id.select_send_audio_type_cancel){
            mSelectSendStype = (Integer) SharePreferenceUtils.getParam(MyApplication.getAppContext(), SEND_TYPE_SP_KEY,ConstValue.MessageModule.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT);
            changeToRecordPage();
        }else if(id == R.id.select_send_audio_type_confirm){
            SharePreferenceUtils.setParam(MyApplication.getAppContext(), SEND_TYPE_SP_KEY, mSelectSendStype);
            changeToRecordPage();
        }*/ else if (id == R.id.record_audio_btn) {
            changeToRecordPage();
            Activity activity = getActivity();
            if(mIsGroupChat && activity!=null) {
                UmengUtil.buryPoint(activity, "groupmessage_talk_begin", "消息-群聊-语音-轻触开始说话", 0);
            }else if(activity!=null){
                UmengUtil.buryPoint(activity, "p2pmessage_talk_begin", "消息-点对点会话-语音-轻触开始说话", 0);
            }
        }

    }

    private void changeToRecordPage() {
        //     mSendTypeSelectRootView.setVisibility(View.GONE);
//        Context context  = getActivity();
//        if(context!=null && AndroidUtil.isNetworkAvailable(context)){
//            if(mNetDialog == null){
//                mNetDialog = new SendAudioNotNetDialog(context);
//            }
//            mNetDialog.show();
//        }
        getRecordPermission();

    }

    private void goToRecord(){
        HandlerThreadFactory.getMainThreadHandler().removeCallbacks(mTimeRunnable);
        Activity activity = getActivity();
        if(!isNetWorkOk() ||activity == null){
            return;
        }
        if(!hasRecoderPermission()){
            if(activity!=null) {
                String message = activity.getString(com.cmic.module_base.R.string.need_voice_permission);
                PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
                permissionDeniedDialog.show();

                SendAudioTextCallBack callBack = null;
                if (mSendAudioTextCallback != null) {
                    callBack = mSendAudioTextCallback.get();
                    if (callBack != null) {
                        callBack.onRecordPermissionFaild();
                    }
                }
                return;
            }
        }
        mRecordRootView.setVisibility(View.VISIBLE);
        mRecordStop = false;
        if (mSendAudioTextCallback != null) {
            SendAudioTextCallBack callBack = mSendAudioTextCallback.get();
            if (callBack != null) {
                callBack.onAudioSelectSendMode(mSelectSendStype);
            }
        }



        if (mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
            mRecoderTip.setText(getResources().getString(R.string.recording));
            mTimeOut = AUDIO_MODE_TIME_OUT;
            mRecoderTimeUpTip.setText("0:00");
            mRecoderTimeUpTip.setTextColor(0xff157cf8);
            mRecoderTimeUpTip.setVisibility(View.VISIBLE);
            mFinishRecord.setVisibility(View.VISIBLE);
            mFinishRecord.setText(SEND_RECORD);
        } else if (mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT
                || mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT) {
            mRecoderTip.setText(getResources().getString(R.string.smart_recording));
            mTimeOut = AUDIO_AND_TEXT_MODE_TIME_OUT;
            mFinishRecord.setVisibility(View.VISIBLE);
            mRecoderTimeUpTip.setTextColor(0xfffc2449);
            mFinishRecord.setText(SEND_RECORD);
            mRecoderTimeUpTip.setVisibility(View.GONE);
        }

        mRecoderBtn.setVisibility(View.GONE);
        mRecordWaveView.setVisibility(View.VISIBLE);

        startReocrd();

        mRecorderTime = 0;
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(mRecorderTime);
        HandlerThreadFactory.getMainThreadHandler().postDelayed(mTimeRunnable, ONE_SECOND);
    }

    private boolean isNetWorkOk(){
        boolean isNetWorkOk = false;
        Context context  = getActivity();
        if(context!=null && AndroidUtil.isNetworkAvailable(context)){
            isNetWorkOk = true;
        }
        if(isNetWorkOk == false && context!=null) {
            mRecoderTip.setText(getResources().getString(R.string.record_network_is_error));
            mRecordWaveView.setVisibility(View.GONE);
            mRecoderBtn.setVisibility(View.VISIBLE);
            mFinishRecord.setText(SETTING_TEXT);
            mProgressBar.setVisibility(View.GONE);
            if(mSelectSendStype!=MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
                if (mNetDialog == null && context != null) {
                    mNetDialog = new SendAudioNotNetDialog(context);
                }
                mNetDialog.show();
            }
        }
        return isNetWorkOk;
    }

    private void getRecordPermission() {
        Activity attachActivity = getActivity();
        if(attachActivity==null)
            return;
        if (!PermissionUtil.with(MessageAudioTextFragment.this).has(Manifest.permission.RECORD_AUDIO)){
            ((BaseActivity) attachActivity).requestPermissions(new BaseActivity.OnPermissionResultListener() {
                boolean isAlwaysDenied = false;

                @Override
                public void onAllGranted() {
                    super.onAllGranted();
                    goToRecord();
                }

                public void onAnyDenied(String[] permissions) { // 权限被禁止，但是还能询问。
                    super.onAnyDenied(permissions);
                    if (isAlwaysDenied) {
                        return;
                    }

                    SendAudioTextCallBack callBack = null;
                    if(mSendAudioTextCallback!=null){
                        callBack = mSendAudioTextCallback.get();
                        if(callBack!=null){
                            callBack.onRecordPermissionFaild();
                        }
                    }
                }

                @Override
                public void onAlwaysDenied(String[] permissions) {//权限被禁止， 并且 不在询问。
                    super.onAlwaysDenied(permissions);
                    isAlwaysDenied = true;
                    Activity activity = getActivity();
                    if(activity!=null) {
                        String message = activity.getString(com.cmic.module_base.R.string.need_voice_permission);
                        PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
                        permissionDeniedDialog.show();

                        SendAudioTextCallBack callBack = null;
                        if (mSendAudioTextCallback != null) {
                            callBack = mSendAudioTextCallback.get();
                            if (callBack != null) {
                                callBack.onRecordPermissionFaild();
                            }
                        }
                    }
                }

            }, Manifest.permission.RECORD_AUDIO);
        } else {
            goToRecord();
        }
    }

    private String mRecordAudioPath;
    private void startReocrd(){
        if(mSendAudioTextCallback!=null){
            SendAudioTextCallBack callBack = mSendAudioTextCallback.get();
            if(callBack!=null)
                callBack.onAudioRecordStart();
        }

        if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
            startRcsAudioRecord();
            return;
        }

        try {
            mRecordAudioPath =FileUtil.createAudioFile("record"+System.currentTimeMillis()+".wav").getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        XFAudioToTextManager.getInstance().start(this,mRecordAudioPath,mTimeOut,16000);

        Activity attachActivity = getActivity();
        if(attachActivity==null)
            return;
        attachActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
            Activity attachActivity = getActivity();
            if(attachActivity == null)
                return;
            if(mRecordStop)
                return;
            mRecorderTime++;
            mProgressBar.setProgress(mRecorderTime);
            if(mRecorderTime>=MAX_RECORD_TIME){
                mAudioStopType = AUDIO_STOP_FROM_FINISH;
                if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT
                        || mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT) {
                    mRecoderTip.setText(START_SAY_TIP);
                    mRecordWaveView.setVisibility(View.GONE);
                    mRecoderBtn.setVisibility(View.VISIBLE);
                    mRecoderTimeUpTip.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    mFinishRecord.setText(SETTING_TEXT);
                }else if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
                    mRecoderTip.setText(START_SAY_TIP);
                    mRecordWaveView.setVisibility(View.GONE);
                    mRecoderBtn.setVisibility(View.VISIBLE);
                    mRecoderTimeUpTip.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    mFinishRecord.setVisibility(View.VISIBLE);
                    mFinishRecord.setText(SETTING_TEXT);
                }
                if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
                    stopRcsAudioRecord();
                }else {
                    XFAudioToTextManager.getInstance().stop();
                }
                return;
            }
            if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
                mRecoderTimeUpTip.setVisibility(View.VISIBLE);
                if(mRecorderTime>=RECODER_WARNING_TIME){
                    mRecoderTimeUpTip.setVisibility(View.VISIBLE);
                    mRecoderTimeUpTip.setTextColor(0xfffc2449);
                    mRecoderTimeUpTip.setText(getResources().getString(R.string.record_count_down)+(MAX_RECORD_TIME-mRecorderTime)+getResources().getString(R.string.record_second));
                }else {
                    mRecoderTimeUpTip.setTextColor(0xff157cf8);
                    String text = mRecorderTime / 60 + ":" + mRecorderTime % 60 / 10 + mRecorderTime % 60 % 10;
                    mRecoderTimeUpTip.setText(text);
                }
            }else if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT
                    || mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT){
                if(mRecorderTime>=RECODER_WARNING_TIME){
                    mRecoderTimeUpTip.setVisibility(View.VISIBLE);
                    mRecoderTimeUpTip.setTextColor(0xfffc2449);
                    mRecoderTimeUpTip.setText(getResources().getString(R.string.record_count_down)+(MAX_RECORD_TIME-mRecorderTime)+getResources().getString(R.string.record_second));
                }
            }

            HandlerThreadFactory.getMainThreadHandler().postDelayed(this,ONE_SECOND);
        }
    };

    public void setFragmentPageType(int pageType){
        mPageTye = pageType;
        if(mViewInited == false)
            return;
        if(mPageTye == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_VOICE_PAGE){
            changeToSettingPage();
        }else{
            changeToRecordPage();
        }
    }

    public void onEvent(int eventType){
        if(eventType == MessageModuleConst.BASE_CHAT_FRAGMENT_SEND_BTN_CLICK){
            mAudioStopType = AUDIO_STOP_FROM_FINISH;
            if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT
                    || mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT) {
                mRecoderTip.setText(START_SAY_TIP);
                mRecoderTimeUpTip.setVisibility(View.GONE);
                mRecordWaveView.setVisibility(View.GONE);
                mRecoderBtn.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mFinishRecord.setText(SETTING_TEXT);
            }else if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
                mRecoderTip.setText(START_SAY_TIP);
                mRecordWaveView.setVisibility(View.GONE);
                mRecoderBtn.setVisibility(View.VISIBLE);
                mRecoderTimeUpTip.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mFinishRecord.setVisibility(View.VISIBLE);
                mFinishRecord.setText(SETTING_TEXT);
            }
            if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
                stopRcsAudioRecord();
                return;
            }
            XFAudioToTextManager.getInstance().stop();
        }else if(eventType == MessageModuleConst.BASE_CHAT_FRAGMENT_ETMESSAGE_FOCUSE){
            mAudioStopType = AUDIO_STOP_FROM_ETMESSAGE_INTERRUPT;
            if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
                stopRcsAudioRecord();
                return;
            }
            XFAudioToTextManager.getInstance().stop();

        }else if(eventType ==MessageModuleConst.BASE_CHAT_FRAGMENT_ETMESSAGE_TEXT_EMPTY ){
            mLastResult = null;
            mErrorInterruptResult = null;
        }
    }


    @Override
    public void initData() {

    }

    private boolean mIsGroupChat;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_message_audio_text;
    }

    public void sendAudioTextCallback(SendAudioTextCallBack callback){
        mSendAudioTextCallback = new WeakReference<SendAudioTextCallBack>(callback);
        mIsGroupChat = callback.isGroupChat();
    }

    @Override
    public void onTextResult(String result) {
        if(mErrorInterruptResult!=null) {
            mLastResult = result + mErrorInterruptResult;
        }else{
            mLastResult = result;
        }
        if(mSendAudioTextCallback!=null){
            SendAudioTextCallBack callBack = mSendAudioTextCallback.get();
            if(callBack!=null){
                if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT && mErrorInterruptResult!=null){
                    result = mErrorInterruptResult+","+result;
                }
                callBack.onAudioTextResult(result,false);
            }
        }
    }

    @Override
    public void onTextLastResult(String result){
        if(mSendAudioTextCallback!=null){
            SendAudioTextCallBack callBack = mSendAudioTextCallback.get();
            if(callBack!=null){
                callBack.onAudioTextResult(result,true);
            }
        }
    }

    @Override
    public void onStopRecord(){
        mRecordStop = true;
        HandlerThreadFactory.getMainThreadHandler().removeCallbacks(mTimeRunnable);

        Activity attachActivity = getActivity();
        if(attachActivity!=null)
            attachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

         SendAudioTextCallBack callBack = null;
        if(mSendAudioTextCallback!=null){
            callBack = mSendAudioTextCallback.get();
        }
        if(callBack == null){
            return;
        }
        switch (mAudioStopType){
            case AUDIO_STOP_FROM_EXIT:
                callBack.onAudioRecordExit();
                break;
            case AUDIO_STOP_FROM_FINISH:
                callBack.onAudioRecordFinish(mRecordAudioPath);
                mErrorInterruptResult = null;
                mLastResult = null;
                break;

            case AUDIO_STOP_FROM_ETMESSAGE_INTERRUPT:
                callBack.onAudioRecordInterrupt();
                break;
            case AUDIO_STOP_FROM_END_SPEECH_INTERRUPT:
                callBack.onRecordSpeechEnd();
                break;
            case AUDIO_STOP_FROM_ERROR:
                break;
        }
    }

    private final static int NET_WORK_ERROR = 10114; //网络错误返回码
    @Override
    public void onError(SpeechError speechError) {
        LogF.i(TAG,"onError = "+speechError);
        if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO)
            return;

        mAudioStopType = AUDIO_STOP_FROM_ERROR;
        XFAudioToTextManager.getInstance().stop();
        mErrorInterruptResult = mLastResult;
        Context AttachActivity = getActivity();
        if(AttachActivity == null){
            return;
        }
        boolean isNetWorkError = false;
        if(!AndroidUtil.isNetworkAvailable(AttachActivity)){
            isNetWorkError = true;
        }
        if(isNetWorkError){
            HandlerThreadFactory.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    mRecoderTip.setText("网络不可用，请检查网络设置");
                    mRecordWaveView.setVisibility(View.GONE);
                    mRecoderBtn.setVisibility(View.VISIBLE);
                    mFinishRecord.setText(SETTING_TEXT);
                    mProgressBar.setVisibility(View.GONE);
                    mRecoderTimeUpTip.setVisibility(View.GONE);
                    if(mSelectSendStype!=MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
                        Context context = getActivity();
                        if (mNetDialog == null && context != null) {
                            mNetDialog = new SendAudioNotNetDialog(context);
                        }
                        if (mNetDialog != null) {
                            mNetDialog.show();
                        }
                    }
                }
            });
        }else {
            HandlerThreadFactory.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    mRecoderTip.setText("无法识别，请重试");
                    mRecordWaveView.setVisibility(View.GONE);
                    mRecoderTimeUpTip.setVisibility(View.GONE);
                    mRecoderBtn.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    mFinishRecord.setText(SETTING_TEXT);
                }
            });
        }
    }

    @Override
    public void onVolumeChanged(int i, final byte[] bytes) {
        LogF.i(TAG,"onVolumeChanged = "+i);
//        mRecordWaveView.setValue(i/30.0f);
        mRecordWaveView.setVolume(i);

    }

    @Override
    public void onEndOfSpeech() {
        if(!TextUtils.isEmpty(mLastResult)){
            mAudioStopType = AUDIO_STOP_FROM_END_SPEECH_INTERRUPT;
            XFAudioToTextManager.getInstance().stop();

        }else {
            onError(new SpeechError(1, "onEndOfSpeech"));
        }
    }

    @Override
    public void sendSelectType(int type) {
        mSelectSendStype = type;
        changeToRecordPage();
    }

    /**
     * 录音器
     */
    private RcsAudioRecorder mRcsAudioRecorder;

    private void startRcsAudioRecord() {
        mStopRcsAnim = false;
        requestRcsAudioFocus();
        try {
            if (mRcsAudioRecorder == null) {
                mRcsAudioRecorder = new RcsAudioRecorder();
            }
            mRcsAudioRecorder.startRecording(getActivity());
            } catch (Exception e) {
            e.printStackTrace();
        }
        HandlerThreadFactory.getMainThreadHandler().postDelayed(mRcsAudioRecordAnim,RCSAUDIO_ANIM_TIME);
    }

    /**
     * 取消录音
     */
    private void cancelRcsAudioRecord() {
        if (mRcsAudioRecorder != null) {
            mRcsAudioRecorder.cancel();
            mRcsAudioRecorder = null;
        }
        mStopRcsAnim = true;
    }

    /**
     * 停止录音
     */
    private void stopRcsAudioRecord() {
        mStopRcsAnim = true;
        abandonAudioFocus();

        if (mRcsAudioRecorder != null) {
            mRcsAudioRecorder.stopRecording();
        }

        if (mRcsAudioRecorder != null) {
            if (null != mSendAudioTextCallback) {
    //				mSendAudioMessageCallBack.senAudioMsg(audioRecorder.getPath(), recordtime);

                //audioTime 判断是否是有效的录音文件
                mRecordAudioPath = mRcsAudioRecorder.getPath();
                long audioTime = FileUtil.getDuring(mRecordAudioPath);
                if (audioTime <= 0) {
                    cancelRcsAudioRecord();
                    return;
                }
            }
            mRcsAudioRecorder = null;
        }
        onStopRecord();
    }

    private void requestRcsAudioFocus() {
        if (mAudioManager == null) {
            return;
        }

        LogF.v(TAG, "requestAudioFocus mAudioFocus = " + mAudioFocus);

        if (!mAudioFocus) {

            int result = mAudioManager.requestAudioFocus(afChangeListener,

                    AudioManager.STREAM_MUSIC, // Use the music stream.

                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                mAudioFocus = true;

            } else {

                LogF.e(TAG, "AudioManager request Audio Focus result = " + result);

            }

        }

    }
    AudioManager mAudioManager = (AudioManager) MetYouActivityManager.getInstance().getCurrentActivity().getSystemService(Context.AUDIO_SERVICE);
    private boolean mAudioFocus;
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {

                case AudioManager.AUDIOFOCUS_GAIN:

                    LogF.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN");

                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:

                    LogF.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT");

                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:

                    LogF.i(TAG, "AudioFocusChange AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");

                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:

                    Log.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS");

                    mAudioFocus = false;

                    abandonAudioFocus();

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                    LogF.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT");

                    mAudioFocus = false;

                    abandonAudioFocus();

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                    LogF.i(TAG, "AudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                    mAudioFocus = false;

                    abandonAudioFocus();

                    break;

                default:

                    LogF.i(TAG, "AudioFocusChange focus = " + focusChange);

                    break;

            }
        }
    };


    private void requestAudioFocus() {
        if (mAudioManager == null) {
            return;
        }

        Log.v(TAG, "requestAudioFocus mAudioFocus = " + mAudioFocus);

        if (!mAudioFocus) {

            int result = mAudioManager.requestAudioFocus(afChangeListener,

                    AudioManager.STREAM_MUSIC, // Use the music stream.

                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                mAudioFocus = true;

            } else {

                Log.e(TAG, "AudioManager request Audio Focus result = " + result);

            }

        }

    }

    private void abandonAudioFocus() {
        if (mAudioManager == null) {
            return;
        }

        Log.v(TAG, "abandonAudioFocus mAudioFocus = " + mAudioFocus);

        if (mAudioFocus) {

            mAudioManager.abandonAudioFocus(afChangeListener);

            mAudioFocus = false;

        }

    }

    private int[] mRcsAudioAnimVolume = new int[]{1,5,9,14,18,20,25};
    private int mGetVolumeIndex = 0;
    private final int RCSAUDIO_ANIM_TIME = 50;
    private boolean mStopRcsAnim = false;
    private boolean mAnimIndexAdd = true;
    private Runnable mRcsAudioRecordAnim = new Runnable() {
        @Override
        public void run() {
            if(mRecordWaveView!=null){
//                if(mGetVolumeIndex>=mRcsAudioAnimVolume.length-1){
//                    mAnimIndexAdd = false;
//                }else if(mGetVolumeIndex<1){
//                    mAnimIndexAdd = true;
//                }
//                if(mAnimIndexAdd){
//                    mGetVolumeIndex++;
//                }else{
//                    mGetVolumeIndex--;
//                }
//                mRecordWaveView.setVolume(mRcsAudioAnimVolume[mGetVolumeIndex]);
                if(mRcsAudioRecorder!=null) {
                    mRecordWaveView.setVolume((int) (mRcsAudioRecorder.getVolumn() * 10));
                }
            }
            if(mStopRcsAnim == false){
                HandlerThreadFactory.getMainThreadHandler().postDelayed(this,RCSAUDIO_ANIM_TIME);
            }
        }

    };
}

