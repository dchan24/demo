package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_common.utils.LogF;

import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.constract.EditGroupPageContract;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

import java.util.ArrayList;

/**
 * Created by tigger on 2017/5/18.
 */

public class EditGroupPagePresenterImpl implements EditGroupPageContract.Presenter {

    private static final String TAG = "EditGroupPagePresenterImpl";

    private EditGroupPageContract.View mView;

    private Context mContext;

    private static int mCreateId = 0;

    public EditGroupPagePresenterImpl(EditGroupPageContract.View view, Context context) {
        mContext = context;

        mView = view;
        mView.setPresenter(this);

        ArrayList<Integer> actions = new ArrayList<>();
        actions.add(LogicActions.GROUP_CHAT_ESTABLISH_MESSAGE_SESSION_OK);
        actions.add(LogicActions.GROUP_CHAT_ESTABLISH_MESSAGE_SESSION_FAIL);
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    @Override
    public void start() {

    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            LogF.d(TAG, "onReceiveAction + action : " + action);
            int cookie = intent.getIntExtra(LogicActions.USER_ID, 0);
            if (cookie == mCreateId) {

                if (action == LogicActions.GROUP_CHAT_ESTABLISH_MESSAGE_SESSION_OK) {
                    String groupName = intent.getStringExtra(LogicActions.GROUP_CHAT_SUBJECT);
                    String groupId = intent.getStringExtra(LogicActions.GROUP_CHAT_ID);
                    LogF.i(TAG, "onReceiveAction  groupName: " + groupName + "  groupChatId : " + groupId + "  action : " + action);

                    gotoGroupChat(groupId, groupName);
                    mView.dismissProgressDialog();

                } else if (action == LogicActions.GROUP_CHAT_ESTABLISH_MESSAGE_SESSION_FAIL) {  // BusinessGroupChatLogic类rcsImSessEstabU方法
                    LogF.d(TAG, "创建群失败");
                    final int reason = intent.getIntExtra(LogicActions.REASON , 0); // 失败的原因
                    if(mContext instanceof Activity){

                    }
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.dismissProgressDialog();
                            if(reason == MtcImConstants.MTC_IM_ERR_RESLST_SYNTAX){ //创建群聊时，平台解析成员列表失败
                                mView.showReasonsFailureDialog("解析成员列表失败");
                            }else if(reason == MtcImConstants.MTC_IM_ERR_NO_PARTPLST){//创建群聊时缺少成员列表或者列表为空
                                mView.showReasonsFailureDialog("缺少成员列表或者列表为空");
                            }else if(reason == MtcImConstants.MTC_IM_ERR_CREATED_GRP_FULL){//用户创建的群数量已经超过系统上限
                                mView.showReasonsFailureDialog(mContext.getString(R.string.group_reason_group_upper_limit));
                            }else if(reason == MtcImConstants.MTC_IM_ERR_JOINED_GRP_FULL){//用户参与的群数量已经超过系统上限
                                mView.showReasonsFailureDialog("群数量已经超过系统上限");
                            }else if(reason == MtcImConstants.MTC_IM_ERR_MANAGED_GRP_FULL){//用户管理的群数量已经超过系统上限
                                mView.showReasonsFailureDialog("管理的群数量已经超过系统上限");
                            }else if(reason == MtcImConstants.MTC_IM_ERR_EXCEED_MAX_LENGTH){ //创建群聊时群名称超过30字节限制
                                mView.showReasonsFailureDialog("群名称超限");
                            }else if(reason == MtcImConstants.MTC_IM_ERR_TOO_FEW_PARTP){ //创建群聊时群成员总数少于规定的最小人数
                                mView.showReasonsFailureDialog("群成员总数少于规定的最小人数");
                            }
                        }
                    });
                }
            }
        }
    };

    /**
     * 跳转到会话
     */
    private void gotoGroupChat(String groupId, String name) {
        Bundle bundle = new Bundle();

        String clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.GROUP_CHAT_MESSAGE_FRAGMENT_CLASS);
        bundle.putString("address", groupId);
        bundle.putString("person", name);
        bundle.putLong("loadtime", 0);
        bundle.putString("clzName", clzName);

//        GroupInfo gi = GroupChatUtils.getGroupInfo(mContext , mCreateGroupId);
//        String mLoginUserAddress = LoginDaoImpl.getInstance().queryLoginUser(mContext);
//        LogF.d(TAG, "gotoGroupChat mLoginUserAddress: " + mLoginUserAddress);
//        if(gi!=null){
//            LogF.d(TAG, "gotoGroupChat groupChatId: " + mCreateGroupId
//                    + " groupType: " + gi.getType() + "  Owner:"+ gi.getOwner() + " thread "+ Thread.currentThread().getName() + "  " );
//        }
//        if( gi !=null && gi.getType() == 1 && !TextUtils.isEmpty(gi.getOwner()) && gi.getOwner().contains(mLoginUserAddress) ){ // 普通群并且自己是群主才有这样的操作
//            Log.d(TAG , " rcsImCbSessPartpAddOk  Process aname = " + getCurProcessName(mContext) + " Threa name = "+ Thread.currentThread().getName());
//            LogF.d(TAG, "gotoGroupChat groupChatId: " + mCreateGroupId
//                    + " groupType: " + gi.getType() + "  Owner:"+ gi.getOwner() );
//            SendServiceMsg msg = new SendServiceMsg();
//            msg.action = LogicActions.GROUP_CREATION_SUCCESS_ACTION;
//            msg.bundle.putString(LogicActions.USER_ID, mCreateGroupId); // 取消同意交换名片 的任务
//            IPCUtils.getInstance().send(msg);
//        }

//        MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);;
        MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);

        ContactSelectorActivity.clearAllSelectedContacts();//测试发现建群后直接进入群成员列表，点击添加人员按钮时，之前选中的成员没有被移除
        Intent data = new Intent();
        data.putExtra("needFinish", true);
        ((Activity) mContext).setResult(Activity.RESULT_OK, data);
        ((Activity) mContext).finish();
    }

    @Override
    public void sendGroupInvite(int id, String pcSubject, String pcPartpList) {//新建群聊
        mCreateId++;
        GroupChatControl.rcsImSessEstabU(mCreateId, pcSubject, pcPartpList);
    }

}
