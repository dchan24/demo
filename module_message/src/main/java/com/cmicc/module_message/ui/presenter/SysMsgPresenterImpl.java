package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.text.TextUtils;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.SysMsgUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.NumberUtils;

import com.cmicc.module_message.ui.constract.SysMsgContract;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.model.SysMsgModel;
import com.cmcc.cmrcs.android.ui.model.SysMsgModel.SysMsgModelLoadFinishCallback;
import com.cmcc.cmrcs.android.ui.model.impls.SysMsgModelImpl;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.fragment.SysMsgFragment;
import com.constvalue.MessageModuleConst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tigger on 2017/5/23.
 */

public class SysMsgPresenterImpl implements SysMsgContract.Presenter, SysMsgModelLoadFinishCallback {

    private static String TAG = "SysMsgPresenterImpl";

    private UIObserverManager mUIObserverManager;

    private SysMsgContract.View mView;
    private SysMsgModel mSysMsgModel;
    private LoaderManager mLoaderManager;

    private Context mContext;
    private String consentEntryGroupID; // 同意入群的群ID

    int type;
    String fileName;
    String pcFileType;
    String pcFileName;
    long fileLengthe;
    String sizeDescript;
    String myCradbody;
    String applicantCardbody;

    public SysMsgPresenterImpl(Context context, SysMsgContract.View view, LoaderManager loaderManager) {

        mContext = context;
        mView = view;
        mLoaderManager = loaderManager;
        mSysMsgModel = new SysMsgModelImpl();

        ArrayList<Integer> actions = new ArrayList<Integer>();
        actions.add(LogicActions.GROUP_CHAT_ACCPTED_CB); // 加群成功
        actions.add(LogicActions.GROUP_CHAT_RELEASEED_CB);
        actions.add(LogicActions.GROUP_CHAT_REJECTED_CB); // rcsImCbSessReleased和rcsImCbSessRejected接口下MtcImConstants.MTC_IM_ERR_LEAVED，成员离开群 ，服务器拒绝
        actions.add(LogicActions.GROUP_CHAT_CANCELED_CB); // rcsImCbSessCanceled
        actions.add(LogicActions.GROUP_CHAT_REJECT_INVITE_OK); // 拒绝群邀请成功
        actions.add(LogicActions.GROUP_CHAT_REJECT_INVITE_FAIL); // 调用拒绝群邀请接口失败
        actions.add(LogicActions.GROUP_CHAT_ACCEPT_INVITE_FAIL); // 调用同意群邀请接口失败
        actions.add(LogicActions.GROUP_CHAT_REJECT_CHAIRMAN);    // 群主拒绝
        actions.add(LogicActions.GROUP_CHAT_ERROR_EXPELLED);     // 被踢
        actions.add(LogicActions.GROUP_CHAT_ERROR_GONE);         // 解散
        actions.add(LogicActions.GROUP_CHAT_ERROR_NO);           // 群超时
        actions.add(LogicActions.GROUP_REQUEST_ENTRY_GROUP);     //请求入群超时

        actions.add(LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_VIST_INTERFACE_FAIL); // 同意名片交换，发送文件访问接口发送失败
        actions.add(LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_VIST_INTERFACE_OK);
        actions.add(LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_FAIL);
        actions.add(LogicActions.CARD_FILE_AGREE_EXCHG_SENG_OK);
        actions.add(LogicActions.AGREE_GROUP_INVITATION_SENG_FAIL); // 同意群邀请，访问接口失败
        actions.add(LogicActions.GROUP_ACCEDEGROUP_BUT_GROUPDISSOLUTION); // 同意群邀请，但群已经解散

        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            String groupid = intent.getStringExtra(LogicActions.GROUP_CHAT_ID);
            String groupName = intent.getStringExtra(LogicActions.GROUP_CHAT_SUBJECT);
            LogF.d(TAG, "onReceiveAction  action : " + action + "  groupName : " + groupName + "  groupid : " + groupid + "  consentEntryGroupID : " + consentEntryGroupID);
            if (action == LogicActions.AGREE_GROUP_INVITATION_SENG_FAIL) {// 同意群邀请，访问接口失败
                ((SysMsgFragment) mView).hindDialog();
                return;
            }
            if (!TextUtils.isEmpty(consentEntryGroupID) && !TextUtils.isEmpty(groupid) && consentEntryGroupID.equals(groupid)) {// groupid和currentGroup需要一样才能表示是当前的群
                if (action == LogicActions.GROUP_CHAT_ACCPTED_CB) {// 群同意接受
                    mView.dismissProgressDialog();
                    if (mView instanceof SysMsgFragment) {
                        ((SysMsgFragment) mView).goToChat(groupid);
                    }
                } else {
                    mView.dismissProgressDialog();
                    if (action == LogicActions.GROUP_CHAT_REJECTED_CB) {// BusinessMsgCbListener rcsImCbSessRejected 59945 rcsImCbSessReleased 59945 服务器拒绝

                    } else if (action == LogicActions.GROUP_CHAT_ACCEPT_INVITE_FAIL) { // 这里不会走了，rcsImSessAccept 方法中注释掉了改用广播
                        BaseToast.show(mContext, mContext.getString(R.string.add_fail));
                    } else if (action == LogicActions.GROUP_CHAT_REJECT_INVITE_FAIL) { // 这里也不会走，没发现有put GROUP_CHAT_REJECT_INVITE_FAIL 的地方
                        BaseToast.show(mContext, mContext.getString(R.string.refuse_fail));
                    } else if (action == LogicActions.GROUP_CHAT_REJECT_INVITE_OK) {   // 拒绝邀请，这里也不会走。只发现有add 没发现有put GROUP_CHAT_REJECT_INVITE_OK的地方
                        long id = intent.getExtras().getLong(LogicActions.GROUP_CHAT_DATA_ID);
                        SysMsgUtils.update(mContext, id, Status.STATUS_FAIL);
                        BaseToast.show(mContext, mContext.getString(R.string.already_refuse));
                    } else if (action == LogicActions.GROUP_CHAT_RELEASEED_CB) {// 群release （也不会走）没发现有put GROUP_CHAT_RELEASEED_CB 的地方
                        Log.d(TAG, "onReceiverNotify " + " action: GROUP_CHAT_RELEASE_ACTION");
                        BaseToast.show(mContext, mContext.getString(R.string.server_realseed));
                    } else if (action == LogicActions.GROUP_CHAT_CANCELED_CB) { // BusinessMsgCbListener类rcsImCbSessCanceled这个在收到群邀请28S后，若没响应就会Canceled掉。
                        Log.d(TAG, "onReceiverNotify " + " action: GROUP_CHAT_RELEASE_ACTION");
                    } else if (action == LogicActions.GROUP_CHAT_ACCEPT_CHAIRMAN) {// 对方同意 不会走，没发现有put GROUP_CHAT_ACCEPT_CHAIRMAN 的地方 。
                        Log.d(TAG, "GROUP_CHAT_ACCEPT_CHAIRMAN");
                    } else if (action == LogicActions.GROUP_CHAT_REJECT_CHAIRMAN) {// 对方拒绝接收 不会走，没发现有put GROUP_CHAT_REJECT_CHAIRMAN 的地方 。
                        Log.v(TAG, "GROUP_CHAT_ACCEPT_CHAIRMAN");
                    } else if (action == LogicActions.GROUP_CHAT_ERROR_EXPELLED || action == LogicActions.GROUP_CHAT_ERROR_GONE) {
                        //BusinessMsgCbListener ， GROUP_CHAT_ERROR_EXPELLED 是 rcsImCbSessReleased 59917 ，
                        //GROUP_CHAT_ERROR_GONE 是 rcsImCbSessReleased 59918 59944 ， rcsImCbSessRejected 59944
                    } else if (action == LogicActions.GROUP_CHAT_ERROR_NO || action == LogicActions.GROUP_REQUEST_ENTRY_GROUP) { // BusinessMsgCbListener类rcsImCbSessReleased 59904 ,59914
                        BaseToast.show(mContext, mContext.getString(R.string.group_timeout));
                    } else if (action == LogicActions.GROUP_ACCEDEGROUP_BUT_GROUPDISSOLUTION) {
                        BaseToast.show(mContext, mContext.getString(R.string.group_dismiss));
                    }
                }
            }

            if (mView instanceof SysMsgFragment && ((SysMsgFragment) mView).isUiShow()) { // 在栈顶，说明在名片详情页同意的名片交换
                if (action == LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_VIST_INTERFACE_FAIL) { // 同意交换名片发送文件访问接口失败
                    if (mView instanceof SysMsgFragment) {
                        ((SysMsgFragment) mView).hindDialog();
                    }
                } else if (action == LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_VIST_INTERFACE_OK) { // 同意交换名片发送文件访问接口成功
                    // 把发送的名片的信息先发送到这里，等名片信息发送成功或失败的接口下，再把信息发送到这里来对比。判断是发送失败还是成功
                    LogF.d(TAG, "名片交换访问接口成功");
                    // 同意交换名片访问发送接口成功那里传来
                    type = intent.getIntExtra(LogicActions.MS_ITYPE, -1);
                    fileName = intent.getStringExtra(LogicActions.FILE_NAME);
                    pcFileType = intent.getStringExtra(LogicActions.FILE_TYPE);
                    pcFileName = intent.getStringExtra("pcFileName");
                    fileLengthe = intent.getLongExtra("fileLengthe", 0);
                    sizeDescript = intent.getStringExtra("sizeDescript");
                    myCradbody = intent.getStringExtra("myCradbody");
                    applicantCardbody = intent.getStringExtra("applicantCardbody");
                } else if (action == LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_FAIL) { //同意名片交换发送失败
                    String cradFn = intent.getStringExtra(LogicActions.FILE_NAME);
                    if (!TextUtils.isEmpty(fileName) && fileName.equals(cradFn)) { // 同意交换名片发送失败
                        LogF.d(TAG, "名片交换失败");
                        if (mView instanceof SysMsgFragment) {
                            ((SysMsgFragment) mView).hindDialog();
                        }
                    }
                } else if (action == LogicActions.CARD_FILE_AGREE_EXCHG_SENG_OK) { // 同意名片交换成功
                    String cradFn = intent.getStringExtra(LogicActions.FILE_NAME); // 文件的名字
                    String addess = intent.getStringExtra(LogicActions.MESSAGE_ADRESS); // 接受人的地址
                    String msgID = intent.getStringExtra(LogicActions.IMDN_MESSAG_ID); // 消息id
                    if (!TextUtils.isEmpty(fileName) && fileName.equals(cradFn)) { // 同意交换名片发送成功
                        LogF.d(TAG, "名片交换成功 msgID " + msgID);
                        insertMessage(type, fileName, pcFileType, pcFileName, fileLengthe, sizeDescript, myCradbody, applicantCardbody, addess, msgID);
                        if (mView instanceof SysMsgFragment) {
                            ((SysMsgFragment) mView).updateHint(addess);
                        }
                    }
                }
            }
        }
    };

    /**
     * 开始加载系统消息
     */
    @Override
    public void start() {
        if(mSysMsgModel!=null){
            mSysMsgModel.loadSysMsg(mContext, mLoaderManager, this);
        }
    }
    /**
     * 数据加载完成回调
     * @param cursor
     */
    @Override
    public void onLoadFinished(Cursor cursor) {
        mView.updateListView(cursor);
    }

    /**
     * 跳转到群会话界面
     * @param groupChatId
     * @param person
     */
    @Override
    public void gotoChat(String groupChatId, String person) {

        if(ConvCache.getInstance().getConvByAddress(groupChatId) == null ){
            Conversation conv = new Conversation(groupChatId);
            long time = System.currentTimeMillis();
            conv.setDate(time);
            conv.setTimestamp(time);
            conv.setBoxType(Type.TYPE_BOX_GROUP);
            conv.setType(Type.TYPE_MSG_TEXT_SEND);
            conv.setPerson(person);
            conv.setUnReadCount(0);
            ConversationUtils.insert(mContext, conv); // 插入一条会话消息   //创建群路口
        }
        Bundle bundle = new Bundle();
        String clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.GROUP_CHAT_MESSAGE_FRAGMENT_CLASS);
        bundle.putString("address", groupChatId);
        bundle.putString("person", person);
        bundle.putLong("loadtime", 0);
        bundle.putString("clzName", clzName);
//        MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);;
        MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);
    }


    /**
     * 插入消息
     */
    private void insertMessage(int type, String fileName, String pcFileType, String pcFileName, long fileLengthe,
                               String sizeDescript, String myCradbody, String applicantCardbody, String addess, String msgID) {

        String userPhone = LoginUtils.getInstance().getLoginUserName();
        userPhone = NumberUtils.getDialablePhoneWithCountryCode(userPhone);
        long date = System.currentTimeMillis();

        //先插入申请方的名片信息
        int idOne = -1;
        Message message = new Message();  // 插入一条提示信息，不经过菊粉SDK
        message.setId(idOne);
        message.setType(Type.TYPE_MSG_CARD_RECV);
        message.setBody(applicantCardbody);
        message.setAddress(NumberUtils.getPhone(addess));
        message.setSendAddress(addess);
        message.setDate(date - 400);
        message.setSeen(true);
        message.setRead(true);
        message.setStatus(Status.STATUS_OK);
        Uri uri = MessageUtils.insertMessage(mContext, message);
        idOne = MessageUtils.getIdFromUri(uri);
        message.setId(idOne);
        MessageUtils.updateMessage(mContext, message);


        //插入自己的名片信息
        int idTwo = -1;
        Message messageTo = new Message();
        messageTo.setId(idTwo);
        messageTo.setType(type);
        if (!TextUtils.isEmpty(myCradbody) && myCradbody.contains("AGREE:YES")) {
            myCradbody = myCradbody.replace("AGREE:YES", "");
            // 还原自己的发送的名片个，防止转发同意后自己的名片，接受方判断有AGREE:YES就会走来自同意的逻辑。
            String filePath = FileUtil.createMessageFilePath(fileName, FileUtil.LocalPath.TYPE_FILE);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(filePath));
                fos.write(myCradbody.getBytes());
                fos.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        messageTo.setBody(myCradbody); // 自己的名片信息
        messageTo.setAddress(NumberUtils.getPhone(addess));
        messageTo.setSendAddress(userPhone);
        messageTo.setExtFileName(fileName);
        messageTo.setExtSizeDescript(sizeDescript);
        messageTo.setExtFilePath(pcFileName);
        messageTo.setExtFileSize(fileLengthe);
        messageTo.setSeen(true);
        messageTo.setRead(true);
        messageTo.setMsgId(msgID);
        messageTo.setStatus(Status.STATUS_OK);
        messageTo.setDate(date);
        Uri uriM = MessageUtils.insertMessage(mContext, messageTo);
        idTwo = MessageUtils.getIdFromUri(uriM);
        messageTo.setId(idTwo);
        MessageUtils.updateMessage(mContext, messageTo);

        // 插入一条提示信息【已与对方交换名片，打个招呼吧】
        int idThr = -1;
        Message messageHint = new Message();
        messageHint.setBody(mContext.getResources().getString(R.string.exchange_calling_card_say_hello));
        messageHint.setType(Type.TYPE_MSG_SYSTEM_TEXT);
        messageHint.setAddress(NumberUtils.getPhone(addess));
        messageHint.setSendAddress(userPhone);
        messageHint.setDate(date + 400); // 时间
        messageHint.setId(idThr);
        messageHint.setSeen(true);
        messageHint.setRead(true);
        messageHint.setStatus(Status.STATUS_OK);
        Uri uris = MessageUtils.insertMessage(mContext, messageHint);
        idThr = MessageUtils.getIdFromUri(uris);
        messageHint.setId(idThr);
        MessageUtils.updateMessage(mContext, messageHint);

    }

    public String getConsentEntryGroupID() {
        return consentEntryGroupID;
    }

    public void setConsentEntryGroupID(String consentEntryGroupID) {
        this.consentEntryGroupID = consentEntryGroupID;
    }
}
