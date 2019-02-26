package com.cmicc.module_message.ui.broadcast;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.chinamobile.precall.utils.PhoneShowUtil;

import com.chinamobile.app.yuliao_common.sysetem.MetYouActivityManager;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.utils.RcsAudioPlayer;

public class CallShowReceiver extends BroadcastReceiver {
    public static final String TAG = "CallShowReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        LogF.d(TAG, "CallShowReceiver");
        LogF.d(TAG, intent == null ? "null" : "intent.getAction() = " + intent.getAction());
        if(intent == null){
            return;
        }
        // 如果是拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            //拨打电话会优先,收到此广播. 再收到 android.intent.action.PHONE_STATE 的 TelephonyManager.CALL_STATE_OFFHOOK 状态广播;

            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            LogF.d(TAG, phoneNumber);//获取拨打的手机号码
        } else {
            RcsAudioPlayer.getInstence(context).stop();
            // 如果是来电
            TelephonyManager tManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            //电话的状态
            switch (tManager.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //等待接听状态
                    LogF.d(TAG, "等待接听状态");
                    hideKeyboard(MetYouActivityManager.getInstance().getCurrentActivity());
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //接听状态
                    LogF.d(TAG, "接听状态");
                    hideKeyboard(MetYouActivityManager.getInstance().getCurrentActivity());
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //挂断状态
                    LogF.d(TAG, "挂断状态");
                    break;
            }
        }
        // CallShowManager 陌电相关
        PhoneShowUtil.onReceive(context, intent);
    }


    public static void hideKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && activity != null) {
            View view = activity.getCurrentFocus();
            if (view != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}