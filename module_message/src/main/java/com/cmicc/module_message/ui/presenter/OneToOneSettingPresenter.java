package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.utils.AndroidUtil;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.ManagePersonalCfg;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.OneToOneSettingContract;
import com.cmicc.module_message.ui.fragment.OneToOneSettingFragment;
import com.juphoon.cmcc.app.lemon.MtcCliConstants;

/**
 * Created by GuoXietao on 2017/3/31.
 */

public class OneToOneSettingPresenter implements OneToOneSettingContract.Presenter {

    private Context mContext;
    OneToOneSettingContract.View mView;

    public OneToOneSettingPresenter(Context context, OneToOneSettingContract.View view) {
        mContext = context;
        mView = view;
    }

    @Override
    public void start() {

    }

    @Override
    public boolean getUndisturbSetting(String address) {
        boolean isSlient = ConversationUtils.isSlient(mContext, address);
        mView.setUndisturbSwitch(isSlient);
        return isSlient;
    }

    @Override
    public void setUndisturbSettingLocal(String address, boolean disturb) {
        ConversationUtils.setSlient(mContext, address, disturb);
        mContext.getContentResolver().notifyChange(Conversations.Conversation.CONTENT_URI, null);
    }

    @Override
    public void setUndisturbSettingServer(String address, String status) {
        //先判断联网状态
        if (!AndroidUtil.isNetworkConnected(mContext)) {
            Toast.makeText(mContext, R.string.contact_list_no_net_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        //再判断是否已经登录
        int loginState = LoginUtils.getInstance().getLoginState();
        if (loginState != MtcCliConstants.MTC_REG_STATE_REGED) {
            Toast.makeText(mContext, R.string.login_no_logins, Toast.LENGTH_SHORT).show();
            return;
        }

        ManagePersonalCfg managePersonalCfg = ManagePersonalCfg.getInstance(mContext);
        //因为我们拦截了checkBox的点击操作，当前状态尚未更改（其实也不会更改了，需要我们收到服务器响应后编码更改）。
        //所以当前状态已打开的时候要发给服务器off，当前状态未打开时要发给服务器on。注意这里是反的！！！

        managePersonalCfg.update1V1MessageDnd(address, status, new ManagePersonalCfg.UiCallback(){
            @Override
            public void onResult(final boolean isOk, int statusCode) {
                if(mView != null){
                    Activity activity = ((OneToOneSettingFragment) mView).getActivity();
                    if(activity != null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mView.updateUndisturbFinish(isOk);

                            }
                        });
                    }
                }

            }
        });

    }
}
