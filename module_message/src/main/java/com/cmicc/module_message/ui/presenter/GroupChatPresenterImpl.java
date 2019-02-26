package com.cmicc.module_message.ui.presenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.chinamobile.app.utils.CalTextLength;
import com.chinamobile.app.utils.CommonConstant;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.aidl.SendServiceMsg;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.OAUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.StringUtil;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmicc.module_message.ui.fragment.BaseChatFragment;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.model.GroupChatModel;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.model.impls.GroupChatInfoModel;
import com.cmicc.module_message.ui.model.impls.GroupChatInfoModel.GroupChatInfoListener;
import com.cmcc.cmrcs.android.ui.model.impls.GroupChatModelImpl;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.FavoriteUtil;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.ThumbnailUtils;
import com.cmcc.cmrcs.android.ui.utils.message.LocationUtil;
import com.cmicc.module_message.utils.RcsAudioPlayer;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.GroupChatContracts;
import com.cmicc.module_message.ui.fragment.GroupChatFragment;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.functions.Func1;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.IS_MULTI_FORWARD;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_MESSAGE_FORWARD;

/**
 * Created by tigger on 2017/5/4.
 */

public class GroupChatPresenterImpl implements GroupChatContracts.Presenter, GroupChatModel.GroupChatLoadFinishCallback {

    private static final String TAG = "GroupChatPresenterImpl";

    private BaseChatContract.View mGroupChatView;

    private Context mContext;
    private LoaderManager mLoaderManager;
    private GroupChatModel mGroupChatModel;
    private GroupChatInfoModel mGroupChatInfoModel;
    private String mAddress; // 会话标识
    private String mPerson; // 会话名
    private int mGroupMember = 0; // 群人数
    private long mLoadTime; //加载的最早时间

    private int mFirstLoadNum = 0;
    private boolean isShow = false ;

    @Override
    public void sendMessage(String msg, String textSize) {
        GroupChatControl.rcsImSessMsgSend(mAddress, msg, textSize);
    }

    @Override
    public void sendMessageAt(String msg, String textSize, String pcUri) {
        GroupChatControl.rcsImSessMsgSendAt(mAddress, msg, textSize, pcUri);
    }

    @Override
    public void sendMessageAtAll(String msg, String textSize) {
        GroupChatControl.rcsImSessMsgSendAtAll(mAddress, msg, textSize, "");
    }

    public GroupChatPresenterImpl(Context context, BaseChatContract.View view, LoaderManager loaderManager, Bundle bundle) {
        isShow = true ;
        mContext = context;
        mGroupChatView = view;
        mLoaderManager = loaderManager;
        int id = new Random(System.currentTimeMillis()).nextInt();
        mGroupChatModel = new GroupChatModelImpl(id);

        mAddress = bundle.getString("address");

        mPerson = bundle.getString("person");

        mLoadTime = bundle.getLong("loadtime", 0);
        mFirstLoadNum = bundle.getInt("unread", 0);

        mGroupChatInfoModel = new GroupChatInfoModel(id + 1, mContext, mLoaderManager, mAddress, new GroupInfoListener());

        ArrayList<Integer> actions = new ArrayList<>();
        actions.add(LogicActions.GROUP_CHAT_BECOME_CHAIRMAN_SUCCESS_CB);
        actions.add(LogicActions.GROUP_CHAT_ERROR_EXPELLED);
        actions.add(LogicActions.GROUP_CHAT_ERROR_GONE);
        actions.add(LogicActions.GROUP_CHAT_REJECTED_CB);
        actions.add(LogicActions.GROUP_CHAT_MODIFY_CHAIRMAN_SUCCESS_CB);
        actions.add(LogicActions.GROUP_CHAT_SUBJECT_CHANGED_CB);// 群名称修改
        actions.add(LogicActions.GROUP_BE_FOYRCED_DISSOLUTION); // 群被迫解散
        actions.add(LogicActions.GROUP_KIKED_OUT_GOROUP);// 被踢出群
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    @Override
    public void start() {

        mGroupChatModel.loadMessages(mContext, mFirstLoadNum, mAddress, mLoadTime, mLoaderManager, this);
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            String groupID = intent.getStringExtra(LogicActions.GROUP_CHAT_ID );
            LogF.d(TAG , "onReceiveAction mAddress : "+ mAddress + " groupID : "+ groupID+ " isShow："+isShow);
            if(isShow && !TextUtils.isEmpty(mAddress) && mAddress.equals(groupID)){
                switch (action) {
                    case LogicActions.GROUP_CHAT_BECOME_CHAIRMAN_SUCCESS_CB:
                        mGroupChatView.hideSuperMsg();
                        break;
                    case LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_FAIL:
                        break;
                    case LogicActions.GROUP_CHAT_ERROR_EXPELLED:
                    case LogicActions.GROUP_CHAT_ERROR_GONE:
                        mGroupChatView.showToast(mContext.getString(R.string.you_get_out_group));
                        mGroupChatView.finish();
                        break;
                    case LogicActions.GROUP_CHAT_REJECTED_CB:
                        int errorCode = intent.getIntExtra(LogicActions.GROUP_CHAT_ERROR_CODE, -1);
                        if (errorCode == MtcImConstants.MTC_IM_ERR_LEAVED) {
                            mGroupChatView.showToast(mContext.getString(R.string.group_dismiss));
                            mGroupChatView.finish();
                        } else if (errorCode == MtcImConstants.MTC_IM_ERR_CREATED_GRP_FULL) {
                            mGroupChatView.showToast(mContext.getString(R.string.group_num_limit_upper));
                            mGroupChatView.finish();
                        }
                        break;
                    case LogicActions.GROUP_CHAT_MODIFY_CHAIRMAN_SUCCESS_CB:
                        mGroupChatView.hideSuperMsg();
                    case LogicActions.GROUP_CHAT_SUBJECT_CHANGED_CB: // 群名称变化
                        break;
                    case LogicActions.GROUP_BE_FOYRCED_DISSOLUTION: // 群被迫解散
                        mGroupChatView.showToast(mContext.getString(R.string.group_dismiss));
                        mGroupChatView.finish();
                        break;
                    case LogicActions.GROUP_KIKED_OUT_GOROUP: // 群被迫解散
                        LogF.d(TAG , "onReceiveAction "+ mContext.getString(R.string.you_leave_group));
                        mGroupChatView.showToast(mContext.getString(R.string.you_leave_group));
                        mGroupChatView.finish();
                        break;
                }
            }
        }
    };

    @Override
    public void updateUnreadCount() {
        LogF.i(TAG, "updateUnreadCount mAddress:" + mAddress);
        if (!StringUtil.isEmpty(mAddress)) {
            ConvCache.getInstance().clearUnreadNumFake(mAddress);
            new RxAsyncHelper("").runInThread(new Func1() {
                @Override
                public Object call(Object o) {
                    Message msg = GroupChatUtils.getLastMessage(mContext, mAddress);
                    if (msg != null) {
                        String identify = msg.getIdentify();
                        if (TextUtils.isEmpty(msg.getIdentify())) {
                            identify = GroupChatUtils.getIdentify(mContext, msg.getAddress());
                        }
                        //同步未读数到其他端
                        ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getDate(), identify, CommonConstant.GROUPCHATTYPE);
                    }

                    ConversationUtils.updateSeen(mContext, Type.TYPE_BOX_GROUP, mAddress, "");
                    return null;
                }
            }).subscribe();
        }
    }

    @Override
    public void loadMoreMessages() {
        mGroupChatModel.loadMoreMessages(mLoaderManager);
    }

    @Override
    public void resend(Message msg) {
        int type = msg.getType();
        switch (type) {
            case Type.TYPE_MSG_VIDEO_SEND:
                rcsImFileTrsfCThumb((int) msg.getId(), LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_BY_PHONTO, mAddress, "", msg.getExtFilePath(), msg.getExtThumbPath(), Integer.parseInt(msg.getExtSizeDescript()) * 1000);
                break;
            case Type.TYPE_MSG_TEXT_SEND:
                if (TextUtils.isEmpty(msg.getAtList())) {
                    GroupChatControl.rcsImSessMsgReSend((int) msg.getId(), mAddress, msg.getBody(), msg.getTextSize());
                } else {
                    GroupChatControl.rcsImSessMsgReSendAt((int) msg.getId(), mAddress, msg.getBody(), msg.getTextSize(), msg.getAtList(), 1);
                }

                break;
            case Type.TYPE_MSG_IMG_SEND:
                rcsImFileTrsfCThumb((int) msg.getId(), LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_BY_PHONTO, mAddress, "", msg.getExtFilePath(), msg.getExtThumbPath(), 0);
                break;
            case Type.TYPE_MSG_CARD_SEND:
            case Type.TYPE_MSG_CARD_SEND_CCIND:
                rcsImFileTrsfCThumb((int) msg.getId(), LogicActions.GROUP_CHAT_FILE_TRANSFTER, mAddress, msg.getBody(), msg.getExtFilePath(), "", FileUtil.getDuring(msg.getExtFilePath()));
                break;
            case Type.TYPE_MSG_FILE_SEND:
            case Type.TYPE_MSG_FILE_SEND_CCIND:
                final int id = (int) msg.getId();
                String filePathFile = msg.getExtFilePath();
                File tmp = new File(filePathFile);
                if (tmp == null || !tmp.exists()) {
                    BaseToast.show(mContext, mContext.getString(R.string.file_not_exit_));
                }
                String address = msg.getAddress();
                ComposeMessageActivityControl.rcsImFileReTrsfXByFileSystem(id, address, filePathFile, 0);
                break;
            case Type.TYPE_MSG_AUDIO_SEND:
            case Type.TYPE_MSG_AUDIO_SEND_CCIND:
                ComposeMessageActivityControl.rcsImFileReTrsfCDetailX((int) msg.getId(), mAddress, msg.getExtFilePath(), FileUtil.getDuring(msg.getExtFilePath()), msg.getBody());
                break;
            case Type.TYPE_MSG_OA_CARD_SEND:
                GroupChatControl.rcsImSessMsgSendOA((int) msg.getId(), mAddress, msg.getBody());
                break;
            case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
                GroupChatControl.rcsImSessMsgSendDateActivity((int) msg.getId(), mAddress, msg.getXml_content());
                break;
            case Type.TYPE_MSG_ENTERPRISE_SHARE_SEND:
                GroupChatControl.rcsImSessMsgSendEnterpriseShare((int) msg.getId(), mAddress, msg.getXml_content());
                break;

            case Type.TYPE_MSG_LOC_SEND:
                double longitude = Double.valueOf(LocationUtil.parseLongitude(msg.getBody()));
                double latitude = Double.valueOf(LocationUtil.parseLatitude(msg.getBody()));
                String loc_body = LocationUtil.parseFreeText(msg.getBody());
                String loc_title = LocationUtil.parseTitle(msg.getBody());
                sendLocation((int) msg.getId(), latitude, longitude, 1000, loc_title, loc_body);
                break;
            case Type.TYPE_MSG_BAG_SEND:
                GroupChatControl.rcsImSessMsgSendR((int) msg.getId(), msg.getAddress(), msg.getBody());
            case Type.TYPE_MSG_CASH_BAG_SEND:
                GroupChatControl.rcsImSessMsgSendCashR((int) msg.getId(), msg.getAddress(), msg.getBody());
            case Type.TYPE_MSG_T_CARD_SEND: {
                String xmlContent = OAUtils.replaceDisplayMode(msg.getXml_content());
                GroupChatControl.rcsImSessMsgReSendD((int) msg.getId(), msg.getAddress(), xmlContent);
                break;

            }
        }
    }

    @Override
    public void sendWithdrawnMessage(Message msg) {
        Log.i(TAG, "mAddress" + mAddress);
        GroupChatControl.rcsImSessWithdrawMsgSend(mAddress, msg);
    }

    @Override
    public void deleteMessage(Message msg) {
        Message m = new Message();
        m.setId(msg.getId());
        m.setStatus(Status.STATUS_DELETE);
        GroupChatUtils.update(mContext, m);
    }

    @Override
    public void deleteMultiMessage(final SparseBooleanArray selectList) {
        final MessageChatListAdapter adapter = ((GroupChatFragment)mGroupChatView).getAdapter();
        final CommomDialog dialog = new CommomDialog(mContext,"",mContext.getString(R.string.delete_multi_select_message));
        dialog.setPositiveName(mContext.getString(R.string.delete));
        dialog.setNegativeName(mContext.getString(R.string.cancel));
        dialog.setMessageColor(ContextCompat.getColor(mContext,R.color.color_8A000000));
        dialog.setNegativeBtnColor(ContextCompat.getColor(mContext,R.color.color_FF666666));
        dialog.setPositiveBtnColor(ContextCompat.getColor(mContext,R.color.color_FFFC2449));
        dialog.setOnNegativeClickListener(new CommomDialog.OnClickListener() {
            @Override
            public void onClick() {
                dialog.dismiss();
            }
        });
        dialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
            @Override
            public void onClick() {
                //进度条
                final ProgressDialog mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage(mContext.getString(R.string.dialog_multi_delete));
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.show();

                RxAsyncHelper rxAsyncHelper = new RxAsyncHelper("");
                rxAsyncHelper.runInThread(new Func1() {
                    @Override
                    public Object call(Object o) {
                        SparseBooleanArray selectedList = selectList;
                        List<Message> selectDatalist = new ArrayList();
                        boolean isNotSupport = false;

                        for(int i = 0; i < selectedList.size() ; i ++){
                            int position = selectedList.keyAt(i);
                            LogF.d("selectItem","selectItem = " + position);
                            LogF.d("selectItem","mDataListSize = " + adapter.getDataList().size());

                            Message message = (Message)adapter.getDataList().get(adapter.canLoadMore() ? position - 1: position);
                            //文件传输
                            if(dealWithSpc(message)){
                                isNotSupport = true;
                            }else{
                                selectDatalist.add(message);
                            }
                            //关闭语音播放
                            if(message.getType() == Type.TYPE_MSG_AUDIO_SEND || message.getType() == Type.TYPE_MSG_AUDIO_SEND_CCIND
                                    || message.getType() == Type.TYPE_MSG_AUDIO_RECV){
                                if(RcsAudioPlayer.getInstence(mContext).isPlaying() && adapter != null && !TextUtils.isEmpty(adapter.audioMessageID) && adapter.audioMessageID.equals(message.getMsgId())){
                                    RcsAudioPlayer.getInstence(mContext).stop();
                                }
                            }
                        }
                        for(int i = 0 ; i < selectDatalist.size() ; i ++){
                            Message m = new Message();
                            m.setId(selectDatalist.get(i).getId());
                            m.setStatus(Status.STATUS_DELETE);
                            GroupChatUtils.update(mContext, m);
                        }
                        return isNotSupport;
                    }
                }).runOnMainThread(new Func1() {
                    @Override
                    public Object call(Object o) {

                        mProgressDialog.dismiss();
                        boolean isNotSupport = (boolean)o;
                        if(isNotSupport){
                            Toast.makeText(mContext,mContext.getString(R.string.multi_delete_londing),Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext,mContext.getString(R.string.mutli_delete_success),Toast.LENGTH_SHORT).show();
                        }
                        ((MessageDetailActivity)mContext).changeMode(MessageDetailActivity.OUT_MULTI_SELECT_MODE);

                        return null;
                    }
                }).subscribe();

            }
        });
        dialog.show();
    }

    /**
     * 文件传输类型消息，需要先停止传输
     * */
    private boolean dealWithSpc(Message message){
        int type = message.getType();
        int status = message.getStatus();
        switch (type){
            case Type.TYPE_MSG_AUDIO_RECV:
            case Type.TYPE_MSG_AUDIO_SEND:
            case Type.TYPE_MSG_AUDIO_SEND_CCIND:
            case Type.TYPE_MSG_FILE_RECV:
            case Type.TYPE_MSG_FILE_SEND:
            case Type.TYPE_MSG_FILE_SEND_CCIND:
            case Type.TYPE_MSG_IMG_RECV:
            case Type.TYPE_MSG_IMG_SEND:
            case Type.TYPE_MSG_IMG_SEND_CCIND:
            case Type.TYPE_MSG_VIDEO_RECV:
            case Type.TYPE_MSG_VIDEO_SEND:
            case Type.TYPE_MSG_VIDEO_SEND_CCIND:
            case Type.TYPE_MSG_CARD_RECV:
            case Type.TYPE_MSG_CARD_SEND:
            case Type.TYPE_MSG_CARD_SEND_CCIND:
                if(status == Status.STATUS_WAITING || status == Status.STATUS_LOADING){
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void forwardMultiMessage(SparseBooleanArray selectList) {
        MessageChatListAdapter adapter = ((GroupChatFragment)mGroupChatView).getAdapter();
       Object[] objects = ((GroupChatFragment)mGroupChatView).getMessageForwardUtil().getForwardMseeages(adapter.getDataList(),selectList,adapter.canLoadMore());
       final List<Message> list = (List<Message>)objects[0];
       boolean isNotSupport = (boolean)objects[1];
       LogF.d(TAG,"forwardMultiMessage isNotSupport = " + isNotSupport + " listsize = " + list.size());
       //含有不支持转发的消息
       if(isNotSupport){
           String message = mContext.getString(R.string.multi_forward_notsupport) + "<br />" + mContext.getString(R.string.forward_notsupport_one) + "<br />"
                   + mContext.getString(R.string.forward_notsupport_two);
           final CommomDialog dialog = new CommomDialog(mContext , mContext.getString(R.string.multi_forward_tip) , Html.fromHtml(message));
           dialog.setPositiveName(mContext.getString(R.string.sure));
           dialog.setNegativeName(mContext.getString(R.string.cancel));
           dialog.setMessageColor(ContextCompat.getColor(mContext,R.color.color_8A000000));
           dialog.setNegativeBtnColor(ContextCompat.getColor(mContext,R.color.color_FF666666));
           dialog.setPositiveBtnColor(ContextCompat.getColor(mContext,R.color.color_FF4184F3));
           dialog.setOnNegativeClickListener(new CommomDialog.OnClickListener() {
               @Override
               public void onClick() {
                   dialog.dismiss();
               }
           });
           dialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
               @Override
               public void onClick() {
                   if(list.size() == 0){
                       dialog.dismiss();
                   }else{
//                       Intent intent = ContactsSelectActivity.createIntentForMessageForward(mContext);
                       Intent intent = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD,1);
                       Bundle bundle = new Bundle();
                       bundle.putBoolean(ContactSelectorActivity.IS_MULTI_FORWARD ,true);
                       intent.putExtras(bundle);
                       ((MessageDetailActivity)mContext).startActivityForResult( intent , BaseChatFragment.FORWARD_REQUEST_CODE);
                   }
               }
           });
           dialog.show();
       }else{
//           Intent intent = ContactsSelectActivity.createIntentForMessageForward(mContext);
           Intent intent = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD,1);
           Bundle bundle = new Bundle();
           bundle.putBoolean(IS_MULTI_FORWARD ,true);
           intent.putExtras(bundle);
           ((MessageDetailActivity)mContext).startActivityForResult( intent , BaseChatFragment.FORWARD_REQUEST_CODE);
       }

    }

    @Override
    public void sendImgAndVideo(final ArrayList<MediaItem> list, final boolean isOriginalPhoto) {
        RxAsyncHelper helper = new RxAsyncHelper("");
        helper.runInThread(new Func1<Object, String>() {
            @Override
            public String call(Object o) {
                for (MediaItem item : list) {
                    item.setMicroThumbPath(ThumbnailUtils.createThumb(item.getLocalPath(),item.getMediaType() != MediaItem.MEDIA_TYPE_IMAGE));
                }
                return null;
            }
        }).runOnMainThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {
                if (list.size() > 0) {
                    MediaItem item = list.get(0);
                    LogF.i(TAG, "senImgAndVideo, " + item.getLocalPath());//此处，通过判断list中第一个item的类型来设置发送参数.
                    if (item.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) {
                        rcsImFileTrsfCThumbOrder(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB, mAddress, list, isOriginalPhoto);
                    } else {
                        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_BY_PHONTO, mAddress, "", item.getLocalPath(), item.getMicroThumbPath(), item.getDuration());
                    }
                } else {
                    LogF.e(TAG, "---------- mediaItem list is empty ----------");
                }
                return null;
            }
        }).subscribe();
    }

    @Override
    public void sendFileMsg(Intent data) {
        if (data == null) {
            return;
        }
        Uri dataUri = data.getData();
        if (dataUri == null)
            return;
        String path = Uri.decode(FileUtil.getMediaRealPath(mContext, dataUri));
        if (StringUtil.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        long lenth = file.length();

        if (file == null || !file.exists()) {
            BaseToast.show(mContext, mContext.getString(R.string.file_not_exit_));
            return;
        }

        if (lenth >= BaseChatFragment.MAX_FILE_LENGTH) {
            BaseToast.makeText(mContext, R.string.max_file_length, Toast.LENGTH_SHORT).show();
            return;
        }
        if (BaseChatFragment.isImage(path)) {
            MediaItem mediaItem = new MediaItem(path, MediaItem.MEDIA_TYPE_IMAGE);
            mediaItem.setLocalPath(path);
            mediaItem.setFileLength(file.length());
            ArrayList list = new ArrayList<MediaItem>();
            list.add(mediaItem);
            sendImgAndVideo(list, true);
        } else if (BaseChatFragment.isVideo(path)) {
            MediaItem mediaItem = new MediaItem(path, MediaItem.MEDIA_TYPE_VIDEO);
            mediaItem.setDuration(FileUtil.getDuring(file.getAbsolutePath()));
            mediaItem.setFileLength(file.length());
            ArrayList list = new ArrayList<MediaItem>();
            list.add(mediaItem);
            sendImgAndVideo(list, true);
        } else {
            ComposeMessageActivityControl.rcsImFileTrsfXByFileSystem(mAddress, path, FileUtil.getDuring(path));
        }
    }

    @Override
    public void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) {

        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFTER, mAddress, pcSubject, pcFileName, "", duration);
    }

    /**
     *  群聊 文件传输 带缩略图
     * @param databaseId    数据库id
     * @param action     消息Action
     * @param groupid    群id
     * @param subject   主题
     * @param filePath   源文件路径
     * @param thumbPath   缩略图路径
     * @param duration    文件时长, 单位为秒 Viewo/Audio有效
     */
    public void rcsImFileTrsfCThumb(int databaseId, int action, String groupid, String subject, String filePath, String thumbPath, long duration) {
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = action;
        msg.bundle.putInt(LogicActions.USER_ID, databaseId);
        msg.bundle.putString(LogicActions.GROUP_CHAT_ID, groupid);
        msg.bundle.putString(LogicActions.FILE_TRANSFER_SUBJECT, subject);
        msg.bundle.putString(LogicActions.FILE_NAME, filePath);
        msg.bundle.putString(LogicActions.FILE_THUMB_PATH, thumbPath);
        msg.bundle.putLong(LogicActions.FILE_RECORD_DURATION, duration);
        IPCUtils.getInstance().send(msg);
    }

    /**
     * 顺序发送文件带缩略图
     * @param userId
     * @param action
     * @param pcUri
     * @param list
     * @param isOriginalPhoto
     */
    public void rcsImFileTrsfCThumbOrder(int userId, int action, String pcUri, List<MediaItem> list, boolean isOriginalPhoto) {
        ArrayList<SendServiceMsg> msgs = new ArrayList<>();
        for (MediaItem item : list) {
            SendServiceMsg msg = new SendServiceMsg();
            msg.action = action;
            msg.bundle.putInt(LogicActions.USER_ID, userId);
            msg.bundle.putString(LogicActions.GROUP_CHAT_ID, pcUri);
            msg.bundle.putString(LogicActions.GROUP_CHAT_SUBJECT, "");
            msg.bundle.putString(LogicActions.FILE_NAME, item.getLocalPath());
            msg.bundle.putString(LogicActions.FILE_THUMB_PATH, item.getMicroThumbPath());
            msg.bundle.putLong(LogicActions.FILE_RECORD_DURATION, item.getDuration());
            msg.bundle.putBoolean(LogicActions.FILE_IS_ORIGINAL_PHOTO, isOriginalPhoto);

            msgs.add(msg);
        }
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = action;
        msg.bundle.putInt(LogicActions.USER_ID, userId);
        msg.bundle.putString(LogicActions.GROUP_CHAT_ID, pcUri);
        msg.bundle.putParcelableArrayList(LogicActions.FILE_TRANSFER_ORDER_ITEM_LIST, msgs);
        IPCUtils.getInstance().send(msg);
    }


    @Override
    public void saveDraftMessage(boolean save, Message msg) {
        if (TextUtils.isEmpty(mAddress)) {
            return;
        }
        Message oldDraftMessage = GroupChatUtils.getDraft(mContext, mAddress);
        if (oldDraftMessage != null) {
            GroupChatUtils.delete(mContext, oldDraftMessage.getId());
        }

        if (save && msg != null) {
            msg.setAddress(mAddress);
            msg.setBoxType(Type.TYPE_BOX_GROUP);
            GroupChatUtils.insert(mContext, msg);
        }
    }

    @Override
    public Message getDraftMessage() {
        return GroupChatUtils.getDraft(mContext, mAddress);
    }

    @Override
    public void onLoadFinished(int loadType, int searchPos, Bundle bundle) {
        mGroupChatView.updateChatListView(loadType, searchPos, bundle);
    }

    public void sendAudio(String path, long length) {
        if (!TextUtils.isEmpty(mAddress)) {
            int action = LogicActions.GROUP_CHAT_FILE_TRANSFTER;
            long duration = length;
            if (duration <= 0) {  //此处使用 形参里的length， 以规避 半秒 这种情况导致的显示有误。
                duration = FileUtil.getDuring(path);
            }
            rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, action, mAddress, "", path, "", duration);
        }
    }

    @Override
    public void sendAudio(String path, long length, String detail) {
        if (!TextUtils.isEmpty(mAddress)) {
            long duration = length;
            if (duration <= 0) {  //此处使用 形参里的length， 以规避 半秒 这种情况导致的显示有误。
                duration = FileUtil.getDuring(path);
            }
            ComposeMessageActivityControl.rcsImFileTrsfCDetailX(mAddress, path, duration, detail);
        } else {
            LogF.e(TAG, " address is empty : " + mAddress);
        }
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public void clearAllMsg() {
        mGroupChatModel.clearAllMsg();
    }

    //发送地理位置
    @Override
    public void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel, String detailAddress) {
        sendLocation(BaseModel.DEFAULT_VALUE_INTEGER, dLatitude, dLongitude, fRadius, pcLabel, detailAddress);
    }

    public void sendLocation(int userId, double dLatitude, double dLongitude, float fRadius, String pcLabel, String detailAddress) {
        GroupChatUtils.sendLocation(mContext, mAddress, userId, dLatitude, dLongitude, fRadius, pcLabel, detailAddress);
    }

    @Override
    public void sendEditImage(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }
        String thumbPath = ThumbnailUtils.createThumb(imagePath, false);
        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_BY_PHONTO, mAddress, "", imagePath, thumbPath, 0);
    }

    @Override
    public void startInitLoader(ArrayList<Message> list, long loadStartTime, long updateTime) {
        mGroupChatModel.loadMessages(mContext, mAddress, loadStartTime, updateTime, mLoaderManager, list, this);
    }


    private class GroupInfoListener implements GroupChatInfoListener {

        @Override
        public void onLoadFinished(Cursor cursor) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                mGroupMember = cursor.getInt(0);
                mPerson = cursor.getString(1);
            }
            Log.d(TAG, "cursor count:" + cursor.getCount());

            String name = CalTextLength.handleText(mPerson, 18);
            if (mGroupMember > 0) {
                if (mGroupMember == 1) {
                    new RxAsyncHelper<>(mGroupMember).runInThread(new Func1<Integer, Object>() {
                        @Override
                        public Object call(Integer count) {
                            if (mContext == null) {
                                return null;
                            }
                            int realCount = GroupChatUtils.getMembersCount(mContext, mAddress);
                            if (realCount != count) {
                                LogF.d(TAG, "groupmenber is not right,count is:" + count + ",realCount is :" + realCount);
                                //群订阅,解决群没激活时群人数显示为1的问题
                                String identity = GroupChatUtils.getIdentify(mContext, mAddress);
                                GroupChatControl.rcsImConfSubsInfo(mAddress, identity);
                            }
                            return null;
                        }
                    }).subscribe();
                }
                String coutText = "(" + mGroupMember + ")";
                SpannableStringBuilder builder = new SpannableStringBuilder(mPerson + coutText);
                mGroupChatView.showTitleName(builder);
            } else {
                mGroupChatView.showTitleName(name);
            }
        }
    }

    public void addToFavorite(Message msg, int chatType, String address) {
        boolean collectSucc = FavoriteUtil.getInstance().addToFavorite(mContext, msg, chatType, address);
        LogF.e(TAG, "addToFavorite msg : " + msg.toString() + "\ncollectSucc:" + collectSucc);
        if (collectSucc) {
            BaseToast.show(mContext, mContext.getString(R.string.collect_succ));
        }
    }

    public void isShowTaost(){
        isShow = false ;
    }

}
