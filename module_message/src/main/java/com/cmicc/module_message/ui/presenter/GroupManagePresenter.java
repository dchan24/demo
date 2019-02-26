package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.app.module.proxys.modulecontact.ContactProxy;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.GroupManageActivity;
import com.cmicc.module_message.ui.constract.GroupManageContract;

import java.util.ArrayList;
import java.util.List;



/**
 * @anthor situ
 * @time 2017/6/21 11:28
 * @description 群管理页面
 */

public class GroupManagePresenter implements GroupManageContract.IPresenter {

    private static final String TAG = "GroupManagePresenter";

    private GroupManageContract.IView mView;
    private Context mContext;
    private String mGroupId;
    private String mGroupName; // 会话名
    private ArrayList<String> mGroupMembers;

    private UIObserverManager mUIObserverManager;
    private static List<Integer> sActions;

    public GroupManagePresenter(Context context, GroupManageContract.IView view, Bundle bundle) {
        mContext = context;
        mView = view;
        mGroupId = bundle.getString(GroupManageActivity.BUNDLE_KEY_GROUP_ID);
        GroupInfo groupInfo = GroupChatUtils.getGroupInfo(mContext, mGroupId);
        mGroupName = groupInfo.getPerson();
//        mGroupMembers = bundle.getStringArrayList(GroupManageActivity.BUNDLE_KEY_MEMBERS);
        mGroupMembers = GroupManageActivity.mGroupMembers;

        ArrayList<Integer> actions = new ArrayList<Integer>();
        actions.add(LogicActions.GROUP_CHAT_DISSOLVE_OK_CB);
        actions.add(LogicActions.GROUP_CHAT_DISSOLVE_FAIL_CB);
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    @Override
    public void start() {

    }

    @Override
    public void transferGroup() {
        UmengUtil.buryPoint(mContext,"groupmessage_setup_manage","消息-群聊-群聊设置-群管理",0);
        if (mGroupMembers != null && mGroupMembers.size() > 0) {
            Intent intent = ContactProxy.g.getUiInterface().getContactSelectActivityUI().createIntentForGroupTransfer(mContext, mGroupId,mGroupMembers);
            mContext.startActivity(intent);
        } else {
            BaseToast.makeText(mContext, mContext.getString(R.string.group_no_one), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void disbandGroup() {
        UmengUtil.buryPoint(mContext,"groupmessage_setup_manage","消息-群聊-群聊设置-群管理",0);
        if (!AndroidUtil.isNetworkConnected(mContext)) {
            BaseToast.makeText(mContext, mContext.getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
            mView.toggleProgressDialog(false);
            return;
        }
        mView.toggleProgressDialog(true);
        GroupChatControl.rcsImSessDissolve(mGroupId);
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            mView.toggleProgressDialog(false);
            final String groupId = intent.getStringExtra(LogicActions.GROUP_CHAT_ID);
            LogF.d(TAG, "onReceiveAction action = " + action + ", groupId = " + groupId );
        }
    };

    public String getGroupId() {
        return mGroupId;
    }

    public String getGroupName() {
        return mGroupName;
    }

}
