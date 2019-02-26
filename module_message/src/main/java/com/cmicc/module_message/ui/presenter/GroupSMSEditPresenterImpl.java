package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.utils.AndroidUtil;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

/**
 * Created by yangshaowei on 2017/7/13.
 */

public class GroupSMSEditPresenterImpl {

    public int sendGroupSMS(Context context, String address, String content) {
        if (context == null || TextUtils.isEmpty(content) ||  TextUtils.isEmpty(address)) {
            return R.string.sms_content_empty;
        }
        // 注释原因：1.只有移动卡才能使用群短信功能，外面进入到此界面处做了当前登录的是不是异网卡的判断，所以这里没必要再次判断。
        // 2.下面这个判断有不足，双卡时，拿的是卡1，所以存在有误的情况。
//        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
//        String operator = telManager.getSimOperator();
//        if (!TextUtils.isEmpty(operator)) {
//            if (!AndroidUtil.isCMCCNumber(operator)) {
//                return R.string.no_send_permission;
//            }
//        }
        String uri = GroupChatUtils.getIdentify(context, address);
        GroupChatControl.rcsImMsgSendG(address, uri, content);
        return -1;// 执行成功
    }
}
