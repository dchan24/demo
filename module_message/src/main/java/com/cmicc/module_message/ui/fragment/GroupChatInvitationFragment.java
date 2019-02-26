package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.SysMsgUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.BroadcastActions;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.AndroidUtil;

import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.GroupChatInvitationActivtiy;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;

/**
 * Created by LY on 2018/5/7.
 */

public class GroupChatInvitationFragment extends BaseFragment implements View.OnClickListener{

    private String TAG = "GroupChatInvitationFragment" ;
    private TextView groupNmae ; // 群名字
    private TextView groupNumbermemberText ; // 群成员数
    private TextView invitationStipText ; // 邀请提示语
    private Button agreeButton ; // 同意按钮

    private Bundle bundle ;
    private String groupAddress ;
    private String invitationStip ;
    private  GroupInfo groupInfo;
    private Activity mActivity ;
    private ProgressDialog mProgressDialog;

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            String groupid = intent.getStringExtra(LogicActions.GROUP_CHAT_ID);
            String groupName = intent.getStringExtra(LogicActions.GROUP_CHAT_SUBJECT);
            LogF.d(TAG, "onReceiveAction  action : " + action +"  groupName : "+ groupName + "  groupid : "+ groupid +  " groupAddress : "+ groupAddress);
            if(action == LogicActions.AGREE_GROUP_INVITATION_SENG_FAIL){ // 同意群邀请，访问接口失败
                dismiss();
                agreeButton.setClickable(true); // 可以再次点击
                return;
            }
            if( !TextUtils.isEmpty(groupid) && groupid.equals(groupAddress)) {// groupid和currentGroup需要一样才能表示是当前的群
                if (action == LogicActions.GROUP_CHAT_ACCPTED_CB) {// 群同意接受
                    dismiss();
                    goToChat( groupAddress , groupNmae.getText().toString() );
                } else {
                    dismiss();
                    if (action == LogicActions.GROUP_CHAT_REJECTED_CB) {// BusinessMsgCbListener rcsImCbSessRejected 59945 rcsImCbSessReleased 59945 服务器拒绝
                    } else if (action == LogicActions.GROUP_CHAT_ACCEPT_INVITE_FAIL) { // 这里，handleGroupAcceptInviteResult
                        BaseToast.show(mContext, mContext.getString(R.string.add_fail));
                    } else if (action == LogicActions.GROUP_CHAT_REJECT_INVITE_FAIL) {// 这里也不会走，没发现有put GROUP_CHAT_REJECT_INVITE_FAIL 的地方
                        BaseToast.show(mContext, mContext.getString(R.string.refuse_fail));
                    } else if (action == LogicActions.GROUP_CHAT_REJECT_INVITE_OK) {// 拒绝邀请，这里也不会走。只发现有add 没发现有put GROUP_CHAT_REJECT_INVITE_OK的地方
                        long id = intent.getExtras().getLong(LogicActions.GROUP_CHAT_DATA_ID);
                        SysMsgUtils.update(mContext, id, Status.STATUS_FAIL);
                        BaseToast.show(mContext, mContext.getString(R.string.already_refuse));
                    } else if (action == LogicActions.GROUP_CHAT_RELEASEED_CB) {// 群release （也不会走）没发现有put GROUP_CHAT_RELEASEED_CB 的地方
                        LogF.d(TAG, "onReceiverNotify " + " action: GROUP_CHAT_RELEASE_ACTION");
                        BaseToast.show(mContext, mContext.getString(R.string.server_realseed));
                    } else if (action == LogicActions.GROUP_CHAT_CANCELED_CB) { // BusinessMsgCbListener类rcsImCbSessCanceled这个在收到群邀请28S后，若没响应就会Canceled掉。
                        LogF.d(TAG, "onReceiverNotify " + " action: GROUP_CHAT_RELEASE_ACTION");
                    } else if (action == LogicActions.GROUP_CHAT_ACCEPT_CHAIRMAN) {// 对方同意 不会走，没发现有put GROUP_CHAT_ACCEPT_CHAIRMAN 的地方 。
                        LogF.d(TAG, "GROUP_CHAT_ACCEPT_CHAIRMAN");
                    } else if (action == LogicActions.GROUP_CHAT_REJECT_CHAIRMAN) {// 对方拒绝接收 不会走，没发现有put GROUP_CHAT_REJECT_CHAIRMAN 的地方 。
                        LogF.v(TAG, "GROUP_CHAT_ACCEPT_CHAIRMAN");
                    } else if (action == LogicActions.GROUP_CHAT_ERROR_EXPELLED || action == LogicActions.GROUP_CHAT_ERROR_GONE) {
                        //BusinessMsgCbListener ， GROUP_CHAT_ERROR_EXPELLED 是 rcsImCbSessReleased 59917 ，
                        //GROUP_CHAT_ERROR_GONE 是 rcsImCbSessReleased 59918 59944 ， rcsImCbSessRejected 59944
                    } else if (action == LogicActions.GROUP_CHAT_ERROR_NO || action == LogicActions.GROUP_REQUEST_ENTRY_GROUP) { // BusinessMsgCbListener类rcsImCbSessReleased 59904 ,59914
                        BaseToast.show(mContext, mContext.getString(R.string.group_timeout));
                    }else if(action == LogicActions.GROUP_ACCEDEGROUP_BUT_GROUPDISSOLUTION){
                        BaseToast.show(R.string.group_dismiss);
                        if(mActivity!=null){
                            mActivity.finish();
                        }
                    }
                }
            }

        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.fragment_group_chat_invitation_layout;
    }

    @Override
    public void initViews(View rootView){
        mActivity = getActivity() ;
        groupNmae = rootView.findViewById(R.id.groupname_text);
        groupNumbermemberText = rootView.findViewById(R.id.group_numbermember_text);
        invitationStipText = rootView.findViewById(R.id.invitation_stip_text);
        agreeButton = rootView.findViewById(R.id.agree);
        agreeButton.setOnClickListener(this);
    }

    @Override
    public void initData() {
        initViewData();
        initDilog();
        initCallbackBroadcast();
        initRegisterObserver();
    }

    /**
     * 显示数据
     */
    private void initViewData(){
        bundle = getArguments();
        if(bundle !=null ){
            groupAddress = bundle.getString(GroupChatInvitationActivtiy.groupaddress); // 群ID
            invitationStip = bundle.getString(GroupChatInvitationActivtiy.invitationStip);
            if(!TextUtils.isEmpty(invitationStip)){
                invitationStipText.setText(invitationStip);
            }
            groupInfo = GroupChatUtils.getGroupInfo(getActivity(),groupAddress);
        }
        if(groupInfo !=null ){
            groupNmae.setText(groupInfo.getPerson()); // 群名字
            groupNumbermemberText.setText(groupInfo.getMemberCount()+"人"); // 群成员数
        }
    }

    /**
     * 加载框
     */
    private void initDilog() {
        mProgressDialog = new ProgressDialog(this.getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.wait_please));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);
    }

    /**
     * 注册观察者
     */
    private void initRegisterObserver(){
        ArrayList<Integer> actions = new ArrayList<Integer>();
        actions.add(LogicActions.GROUP_CHAT_ACCPTED_CB);
        actions.add(LogicActions.GROUP_CHAT_RELEASEED_CB);
        actions.add(LogicActions.GROUP_CHAT_REJECTED_CB);
        actions.add(LogicActions.GROUP_CHAT_CANCELED_CB);
        actions.add(LogicActions.GROUP_CHAT_REJECT_INVITE_OK);
        actions.add(LogicActions.GROUP_CHAT_REJECT_INVITE_FAIL);
        actions.add(LogicActions.GROUP_CHAT_ACCEPT_INVITE_FAIL);
        actions.add(LogicActions.GROUP_CHAT_REJECT_CHAIRMAN);
        actions.add(LogicActions.GROUP_CHAT_ERROR_EXPELLED);
        actions.add(LogicActions.GROUP_CHAT_ERROR_GONE);
        actions.add(LogicActions.GROUP_CHAT_ERROR_NO);
        actions.add(LogicActions.AGREE_GROUP_INVITATION_SENG_FAIL); // 同意群邀请，访问接口失败
        actions.add(LogicActions.GROUP_ACCEDEGROUP_BUT_GROUPDISSOLUTION); // 同意群邀请，但群已经解散
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.agree){ // 同意操作
            if (!AndroidUtil.isNetworkConnected(mActivity)) {
                BaseToast.show(R.string.network_disconnect);
                return;
            }
            if(!TextUtils.isEmpty(groupAddress)){
                mProgressDialog.show();
                agreeButton.setClickable(false); // 防止多次点击
                GroupChatControl.acceptInvite(groupAddress);
            }else{
                Toast.makeText(mActivity , getString(R.string.can_not_enjoy_group_chat) ,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private CallbackBroadcast mCallbackBroadcast ;
    private IntentFilter intentFilter ;
    private static boolean isOpen = false ;

    /**
     * 注册广播
     */
    private void initCallbackBroadcast(){
        isOpen = true ;
        if(mCallbackBroadcast == null  && mActivity != null ){
            mCallbackBroadcast = new CallbackBroadcast();
            intentFilter = new IntentFilter();
            intentFilter.addAction(BroadcastActions.MESSAGE_GROUP_STATUS_UPDATE_SUCCESS_ACTION);
            mActivity.registerReceiver(mCallbackBroadcast,intentFilter);
        }
    }

    /**
     * 同意加入群聊从底进程发送来的广播
     */
    class CallbackBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BroadcastActions.MESSAGE_GROUP_STATUS_UPDATE_SUCCESS_ACTION) { // 在同意前，群已经解散了
                String groupID = intent.getStringExtra("GroupID");
                if (!TextUtils.isEmpty(groupID) && !TextUtils.isEmpty(groupAddress) && groupID.equals(groupAddress)) {
                    dismiss();
                    SysMsgUtils.updateGroupInvitation(mContext, groupID, Status.STATUS_DESTROY, 1);
                    if (mActivity != null) {
                        mActivity.finish();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOpen = false ;
        if(mActivity != null && mCallbackBroadcast != null  ){
            mActivity.unregisterReceiver(mCallbackBroadcast);
        }
    }

    /**
     * 跳转到群会话界面
     * @param groupId
     */
    public void goToChat(String groupId , String groupName){

        Conversation conv = new Conversation(groupId);
        long time = System.currentTimeMillis();
        conv.setDate(time);
        conv.setTimestamp(time);
        conv.setBoxType(Type.TYPE_BOX_GROUP);
        conv.setType(Type.TYPE_MSG_TEXT_SEND);
        conv.setPerson(groupName);
        conv.setUnReadCount(0);
        ConversationUtils.insert(mContext,conv); // 插入一条会话消息   //创建群路口

        if(mActivity != null ){
            Bundle bundle = new Bundle();
            String clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.GROUP_CHAT_MESSAGE_FRAGMENT_CLASS);
            bundle.putString("address", groupId); // 群ID
            bundle.putString("person", groupName); // 群名称
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
//            MessageDetailActivity.show(mActivity, bundle);
            MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);
            mActivity.setResult(1002 , new Intent()); // 关闭系统消息界面
            mActivity.finish(); // 关闭本界面
        }
    }

    private void dismiss(){
        if(mProgressDialog != null ){
            mProgressDialog.dismiss();
        }
    }

    public static boolean getIsOpen(){
        return isOpen ;
    }
}
