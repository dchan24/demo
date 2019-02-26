package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.listener.AudioTypeSelectListener;
import com.constvalue.MessageModuleConst;

public class AudioTypeSelectFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    private TextView mSendAudioAndTextView;
    private ImageView mSendAudioAndTextSelectIcon;
    private TextView mSendTextView;
    private ImageView mSendTextSelectIcon;
    private TextView mSendAudioView;
    private ImageView mSendAudioSelectIcon;
    private TextView mCancelSendSelect;
    private TextView mConfirmSendSelect;
    private AudioTypeSelectListener mListener;
    private boolean mViewCreated = false;
    public static String FRAGMENT_TAG = "audio_type_select_fragment";

    private int mSelectSendStype = MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT;


    public void init(AudioTypeSelectListener listener){
        mListener = listener;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_audio_type_select, null);
        initSendAudioSelectView(rootView);

        return rootView;
    }

    private void initSendAudioSelectView(View rootView){

        View sendTypeSelectRootView = rootView.findViewById(R.id.select_send_audio_type_root_view);

        mSendAudioAndTextView = (TextView)rootView.findViewById(R.id.select_send_audio_and_text);
        mSendAudioAndTextSelectIcon = (ImageView)rootView.findViewById(R.id.select_send_audio_and_text_icon);
      //  mSendAudioAndTextSelectIcon.setOnClickListener(this);
        mSendTextView = (TextView)rootView.findViewById(R.id.select_send_text);
        mSendTextSelectIcon = (ImageView)rootView.findViewById(R.id.select_send_text_icon);
      //  mSendTextSelectIcon.setOnClickListener(this);
        mSendAudioView = (TextView)rootView.findViewById(R.id.select_send_voice);
        mSendAudioSelectIcon = (ImageView)rootView.findViewById(R.id.select_send_voice_icon);
        //mSendAudioSelectIcon.setOnClickListener(this);

        mCancelSendSelect = (TextView)rootView.findViewById(R.id.select_send_audio_type_cancel);
        mCancelSendSelect.setOnClickListener(this);
        mConfirmSendSelect = (TextView)rootView.findViewById(R.id.select_send_audio_type_confirm);
        mConfirmSendSelect.setOnClickListener(this);

        rootView.findViewById(R.id.select_send_audio_and_text_root).setOnClickListener(this);
        rootView.findViewById(R.id.select_send_text_root).setOnClickListener(this);
        rootView.findViewById(R.id.select_send_audio_root).setOnClickListener(this);

        mViewCreated = true;
        initSelectSendType();
    }

    private void initSelectSendType(){
        mSelectSendStype = (Integer) SharePreferenceUtils.getParam(MyApplication.getAppContext(), MessageAudioTextFragment.SEND_TYPE_SP_KEY,MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT);;
        if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT){
            mSendAudioAndTextSelectIcon.setSelected(true);
            mSendTextSelectIcon.setSelected(false);
            mSendAudioSelectIcon.setSelected(false);

            mSendAudioAndTextView.setTextColor(0xff0d6cf9);
            mSendTextView.setTextColor(0xff2a2a2a);
            mSendAudioView.setTextColor(0xff2a2a2a);
        }else if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT){
            mSendAudioAndTextSelectIcon.setSelected(false);
            mSendTextSelectIcon.setSelected(true);
            mSendAudioSelectIcon.setSelected(false);

            mSendAudioAndTextView.setTextColor(0xff2a2a2a);
            mSendTextView.setTextColor(0xff0d6cf9);
            mSendAudioView.setTextColor(0xff2a2a2a);
        }else if(mSelectSendStype == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO){
            mSendAudioAndTextSelectIcon.setSelected(false);
            mSendTextSelectIcon.setSelected(false);
            mSendAudioSelectIcon.setSelected(true);

            mSendAudioAndTextView.setTextColor(0xff2a2a2a);
            mSendTextView.setTextColor(0xff2a2a2a);
            mSendAudioView.setTextColor(0xff0d6cf9);
        }
    }

    private boolean mIsGroupChat = false;

    public void show(FragmentManager manager, String tag,boolean isGroupChat){
        super.show(manager,tag);
        if(mViewCreated) {
            initSelectSendType();
        }
        mIsGroupChat = isGroupChat;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
         if(id == R.id.select_send_audio_and_text_root){
            mSelectSendStype = MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT;
            mSendAudioAndTextSelectIcon.setSelected(true);
            mSendTextSelectIcon.setSelected(false);
            mSendAudioSelectIcon.setSelected(false);
             mSendAudioAndTextView.setTextColor(0xff0d6cf9);

             mSendTextView.setTextColor(0xff2a2a2a);
             mSendAudioView.setTextColor(0xff2a2a2a);

             Activity activity = getActivity();
             if(mIsGroupChat && activity!=null) {
                 UmengUtil.buryPoint(activity, "groupmessage_talk_setup_voiceandtext", "消息-群聊-语音-设置-同时发送语音+文字", 0);
             }else if(activity!=null){
                 UmengUtil.buryPoint(activity, "p2pmessage_talk_setup_voiceandtext", "消息-点对点会话-语音-设置-同时发送语音+文字", 0);
             }
        }else if(id == R.id.select_send_text_root){
            mSelectSendStype =  MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT;
            mSendAudioAndTextSelectIcon.setSelected(false);
            mSendTextSelectIcon.setSelected(true);
            mSendAudioSelectIcon.setSelected(false);
             mSendAudioAndTextView.setTextColor(0xff2a2a2a);
             mSendTextView.setTextColor(0xff0d6cf9);
             mSendAudioView.setTextColor(0xff2a2a2a);

             Activity activity = getActivity();
             if(mIsGroupChat && activity!=null) {
                 UmengUtil.buryPoint(activity, "groupmessage_talk_setup_text", "消息-群聊-语音-设置-仅发送文字", 0);
             }else if(activity!=null){
                 UmengUtil.buryPoint(activity, "p2pmessage_talk_setup_text", "消息-点对点会话-语音-设置-仅发送文字", 0);
             }

        }else if(id == R.id.select_send_audio_root){
            mSelectSendStype =  MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO;
            mSendAudioAndTextSelectIcon.setSelected(false);
            mSendTextSelectIcon.setSelected(false);
            mSendAudioSelectIcon.setSelected(true);
             mSendAudioAndTextView.setTextColor(0xff2a2a2a);
             mSendTextView.setTextColor(0xff2a2a2a);
             mSendAudioView.setTextColor(0xff0d6cf9);

             Activity activity = getActivity();
             if(mIsGroupChat && activity!=null) {
                 UmengUtil.buryPoint(activity, "groupmessage_talk_setup_voice", "消息-群聊-语音-设置-仅发送语音", 0);
             }else if(activity!=null){
                 UmengUtil.buryPoint(activity, "p2pmessage_talk_setup_voice", "消息-点对点会话-语音-设置-仅发送语音", 0);
             }
        }else if(id == R.id.select_send_audio_type_cancel){
            mSelectSendStype = (Integer) SharePreferenceUtils.getParam(MyApplication.getAppContext(),  MessageAudioTextFragment.SEND_TYPE_SP_KEY,MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT);
            dismiss();

             Activity activity = getActivity();
             if(mIsGroupChat && activity!=null) {
                 UmengUtil.buryPoint(activity, "groupmessage_talk_setup_cancel", "消息-群聊-语音-设置-取消", 0);
             }else if(activity!=null){
                 UmengUtil.buryPoint(activity, "p2pmessage_talk_setup_cancel", "消息-点对点会话-语音-设置-取消", 0);
             }
           // changeToRecordPage();
        }else if(id == R.id.select_send_audio_type_confirm){
            SharePreferenceUtils.setParam(MyApplication.getAppContext(), MessageAudioTextFragment.SEND_TYPE_SP_KEY, mSelectSendStype);
            //changeToRecordPage();
             dismiss();

             Activity activity = getActivity();
             if(mIsGroupChat && activity!=null) {
                 UmengUtil.buryPoint(activity, "groupmessage_talk_setup_done", "消息-群聊-语音-设置-确定", 0);
             }else if(activity!=null){
                 UmengUtil.buryPoint(activity, "p2pmessage_talk_setup_done", "消息-点对点会话-语音-设置-确定", 0);
             }
        }
    }

    @Override
    public void dismiss(){
        super.dismiss();
        if(mListener!=null){
            mListener.sendSelectType(mSelectSendStype);
        }
    }

}
