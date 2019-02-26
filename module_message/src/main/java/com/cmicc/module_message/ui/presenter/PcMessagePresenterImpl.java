package com.cmicc.module_message.ui.presenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.chinamobile.app.utils.CalTextLength;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.aidl.SendServiceMsg;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.OAUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.PcMessageModel;
import com.cmcc.cmrcs.android.ui.model.impls.PcMessageModelImpl;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.FavoriteUtil;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.ThumbnailUtils;
import com.cmcc.cmrcs.android.ui.utils.message.LocationUtil;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.ui.constract.PcMessageContracts;
import com.cmicc.module_message.ui.fragment.BaseChatFragment;
import com.cmicc.module_message.ui.fragment.PcMessageFragment;
import com.cmicc.module_message.utils.RcsAudioPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.IS_MULTI_FORWARD;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_MESSAGE_FORWARD;

/**
 * Created by tigger on 2018/5/7.
 */

public class PcMessagePresenterImpl implements PcMessageContracts.Presenter, PcMessageModel.PcMessageLoadFinishCallback {

    private static final String TAG = "PcMessagePresenterImpl";

    private BaseChatContract.View mMessageListView;
    private Context mContext;
    private LoaderManager mLoaderManager;
    private PcMessageModel mPcMessageModel;
    private String mAddress; // 会话标识
    private String mPerson; // 会话名
    private long mLoadTime; //加载的最早时间

    private int mFirstLoadNum = 0;

    public PcMessagePresenterImpl(Context context, BaseChatContract.View view, LoaderManager loaderManager, Bundle bundle) {
        mContext = context;
        mMessageListView = view;
        mLoaderManager = loaderManager;
        mPcMessageModel = new PcMessageModelImpl();

        mAddress = bundle.getString("address");
        mAddress = NumberUtils.getPhone(mAddress);

        mPerson = bundle.getString("person");
        if (mPerson != null && mPerson.equals(context.getString(R.string.unknow_phone_num))) {
            mPerson = mAddress;
        }
        mLoadTime = bundle.getLong("loadtime", 0);
        mFirstLoadNum = bundle.getInt("unread", 0);
    }

    @Override
    public void start() {
        mPcMessageModel.loadMessages(mContext, mFirstLoadNum, mAddress, mLoadTime, mLoaderManager, this);
        showTitleName();
    }

    private void showTitleName() {
        String name = CalTextLength.handleText(mPerson, 20);
        //mMessageListView.showTitleName(name);
        mMessageListView.showTitleName(mPerson);
    }

    @Override
    public void onLoadFinished(int loadType, int searchPos, Bundle bundle) {
        mMessageListView.updateChatListView(loadType, searchPos, bundle);
    }

    @Override
    public void sendMessage(String message) {
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = LogicActions.MESSAGE_SEND_ACTION;
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, mAddress);
        msg.bundle.putString(LogicActions.MESSAGE_CONTENT, message);
        msg.bundle.putInt(LogicActions.MESSAGE_TYPE, Type.TYPE_MSG_TEXT_SEND);
        msg.bundle.putInt(LogicActions.USER_ID, BaseModel.DEFAULT_VALUE_INTEGER);
        //msg.bundle.putString(LogicActions.MESSAGE_SIZE, "80");
        IPCUtils.getInstance().send(msg);
    }

    @Override
    public void sendMessage(String message, String size) {
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = LogicActions.MESSAGE_SEND_ACTION;
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, mAddress);
        msg.bundle.putString(LogicActions.MESSAGE_CONTENT, message);
        msg.bundle.putInt(LogicActions.MESSAGE_TYPE, Type.TYPE_MSG_TEXT_SEND);
        msg.bundle.putInt(LogicActions.USER_ID, BaseModel.DEFAULT_VALUE_INTEGER);
        msg.bundle.putString(LogicActions.MESSAGE_SIZE, size);
        IPCUtils.getInstance().send(msg);
    }

    @Override
    public void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel, String detailAddress) {
        sendLocation(BaseModel.DEFAULT_VALUE_INTEGER,getAddress(),dLatitude,dLongitude,fRadius,pcLabel,detailAddress);
//
//        SendServiceMsg msg = new SendServiceMsg();
//        msg.action = LogicActions.PUSH_GEOLOCATION_INFO;
//        msg.bundle.putInt(LogicActions.USER_ID, BaseModel.DEFAULT_VALUE_INTEGER);
//        msg.bundle.putString(LogicActions.GELOCATION_PCFREETEXT, detailAddress);
//        msg.bundle.putDouble(LogicActions.GELOCATION_DLATITUDE, dLatitude);
//        msg.bundle.putDouble(LogicActions.GELOCATION_DLONGITUDE, dLongitude);
//        msg.bundle.putFloat(LogicActions.GELOCATION_FRADIUS, fRadius);
//        msg.bundle.putString(LogicActions.PARTICIPANT_URI, mAddress);
//        msg.bundle.putString(LogicActions.GELOCATION_PCLABEL, pcLabel);
//        IPCUtils.getInstance().send(msg);
    }

    public void sendLocation(int userId,String address,double dLatitude, double dLongitude, float fRadius, String pcLabel,String detailAddress){
        ComposeMessageActivityControl.sendLocation(mPerson,userId,address,dLatitude,dLongitude,fRadius,pcLabel,detailAddress);
    }

    @Override
    public void sendWithdrawnMessage(Message msg) {
        Log.i(TAG, "sendWithdrawnMessage--" + "  msg.getId():" + msg.getId() + "  msg.getMsgId()" + msg.getMsgId() + ";  mAddress" + mAddress);
        SendServiceMsg message = new SendServiceMsg();
        message.action = LogicActions.MESSAGE_SEND_WITHDRAW_ACTION;
        message.bundle.putString(LogicActions.MESSAGE_WITHDRAW_REVOKE_ID, msg.getMsgId());
        message.bundle.putString(LogicActions.PARTICIPANT_URI, mAddress);
        message.bundle.putString(LogicActions.MESSAGE_SENDER, msg.getSendAddress());
        message.bundle.putString(LogicActions.MESSAGE_RECEIVER, msg.getAddress());
        //        message.bundle.putString(LogicActions.MESSAGE_CONTENT, "你撤回了一条消息");
        message.bundle.putInt(LogicActions.MESSAGE_TYPE, Type.TYPE_MSG_TEXT_SEND);
        message.bundle.putInt(LogicActions.USER_ID, BaseModel.DEFAULT_VALUE_INTEGER);
        IPCUtils.getInstance().send(message);
    }

    @Override
    public void deleteMessage(Message msg) {
        Message m = new Message();
        m.setId(msg.getId());
        m.setStatus(Status.STATUS_DELETE);
        MessageUtils.updateMessage(mContext, m);
    }

    @Override
    public void deleteMultiMessage(final SparseBooleanArray selectList) {
        final MessageChatListAdapter adapter = ((PcMessageFragment)mMessageListView).getAdapter();
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
                            MessageUtils.updateMessage(mContext, m);
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
        MessageChatListAdapter adapter = ((PcMessageFragment)mMessageListView).getAdapter();
        Object[] objects = ((PcMessageFragment)mMessageListView).getMessageForwardUtil().getForwardMseeages(adapter.getDataList(),selectList,adapter.canLoadMore());
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
//                        Intent intent = ContactsSelectActivity.createIntentForMessageForward(mContext);
                        Intent intent = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD,1);
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(IS_MULTI_FORWARD ,true);
                        intent.putExtras(bundle);
                        ((MessageDetailActivity)mContext).startActivityForResult( intent , BaseChatFragment.FORWARD_REQUEST_CODE);
                    }
                }
            });
            dialog.show();
        }else{
//            Intent intent = ContactsSelectActivity.createIntentForMessageForward(mContext);
            Intent intent = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD,1);
            Bundle bundle = new Bundle();
            bundle.putBoolean(IS_MULTI_FORWARD ,true);
            intent.putExtras(bundle);
            ((MessageDetailActivity)mContext).startActivityForResult( intent , BaseChatFragment.FORWARD_REQUEST_CODE);
        }
    }

    @Override
    public void updateUnreadCount() {
        if (!StringUtil.isEmpty(mAddress)) {
            ConvCache.getInstance().clearUnreadNumFake(mAddress);
            new RxAsyncHelper("").runInThread(new Func1() {
                @Override
                public Object call(Object o) {
                    ConversationUtils.updateSeen(mContext, Type.TYPE_BOX_MESSAGE, mAddress, "");
                    return null;
                }
            }).subscribe();
        }
    }

    @Override
    public void loadMoreMessages() {
        mPcMessageModel.loadMoreMessages(mLoaderManager);
    }

    @Override
    public void sendImgAndVideo(final ArrayList<MediaItem> list, final boolean isOriginalPhoto) {
        RxAsyncHelper helper = new RxAsyncHelper("");
        helper.runInThread(new Func1<Object, String>() {
            @Override
            public String call(Object o) {
                for (MediaItem item : list) {
                    String thumbPath = ThumbnailUtils.createThumb(item.getLocalPath(), item.getMediaType() != MediaItem.MEDIA_TYPE_IMAGE);
                    item.setMicroThumbPath(thumbPath);
                }
                return null;
            }
        }).runOnMainThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {
                if (list.size() > 0) {
                    MediaItem item = list.get(0);
                    if (item.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) {
                        rcsImFileTrsfCThumbOrder(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.FILE_TRANSFER_THUMB_ACTION, mAddress, list, isOriginalPhoto);
                    } else {
                        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, mAddress, "", item.getLocalPath(), item.getMicroThumbPath(), item.getDuration());
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
        if(BaseChatFragment.isImage(path)){
            MediaItem mediaItem = new MediaItem(path ,MediaItem.MEDIA_TYPE_IMAGE);
            mediaItem.setLocalPath(path);
            mediaItem.setFileLength(file.length());
            ArrayList list = new ArrayList<MediaItem>();
            list.add(mediaItem);
            sendImgAndVideo(list ,true);
        }else if(BaseChatFragment.isVideo(path)){
            MediaItem mediaItem = new MediaItem(path ,MediaItem.MEDIA_TYPE_VIDEO);
            mediaItem.setDuration(FileUtil.getDuring(file.getAbsolutePath()));
            mediaItem.setFileLength(file.length());
            ArrayList list = new ArrayList<MediaItem>();
            list.add(mediaItem);
            sendImgAndVideo(list ,true);
        }else{
            ComposeMessageActivityControl.rcsImFileTrsfByFileSystem(mAddress, path, FileUtil.getDuring(path));
        }
    }

    @Override
    public void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) {
        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, mAddress, pcSubject, pcFileName, "", duration);
    }

    @Override
    public void reSend(Message msg) {
        int type = msg.getType();
        //        MessageUtils.delete(mContext, msg.getId());
        switch (type) {
            case Type.TYPE_MSG_VIDEO_SEND:
            case Type.TYPE_MSG_VIDEO_SEND_CCIND:
                rcsImFileTrsfCThumb((int) msg.getId(), LogicActions.FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, mAddress, "", msg.getExtFilePath(), msg.getExtThumbPath(), Integer.parseInt(msg.getExtSizeDescript()) * 1000);
                break;
            case Type.TYPE_MSG_TEXT_SEND:
                //sendMessage(msg.getBody());
                //                sendMessage(msg.getBody(), msg.getTextSize());
                reSendMessage((int) msg.getId(), msg.getBody(), msg.getTextSize());
                break;
            case Type.TYPE_MSG_IMG_SEND:
                rcsImFileTrsfCThumb((int) msg.getId(), LogicActions.FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, mAddress, "", msg.getExtFilePath(), msg.getExtThumbPath(), FileUtil.getDuring(msg.getExtFilePath()));
                break;
            case Type.TYPE_MSG_CARD_SEND:
            case Type.TYPE_MSG_CARD_SEND_CCIND:
                //                MessageUtils.delete(mContext, msg.getId());
                rcsImFileTrsfCThumb((int) msg.getId(), LogicActions.FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, mAddress, msg.getBody(), msg.getExtFilePath(), "", FileUtil.getDuring(msg.getExtFilePath()));
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
                ComposeMessageActivityControl.rcsImFileReTrsfByFileSystem(id, address, filePathFile, FileUtil.getDuring(filePathFile));
                break;
            case Type.TYPE_MSG_AUDIO_SEND:
            case Type.TYPE_MSG_AUDIO_SEND_CCIND:
//                rcsImFileTrsfCThumb((int) msg.getId(), LogicActions.FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, mAddress, "", msg.getExtFilePath(), "", FileUtil.getDuring(msg.getExtFilePath()));
                ComposeMessageActivityControl.rcsImFileReTrsfWithDetail((int) msg.getId(), mAddress, msg.getExtFilePath(), FileUtil.getDuring(msg.getExtFilePath()), msg.getBody(), "");
                break;
            case Type.TYPE_MSG_OA_CARD_SEND:
                ComposeMessageActivityControl.rcsImMsgReSendOA((int) msg.getId(), msg.getAddress(), msg.getXml_content());
                break;
            case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
                ComposeMessageActivityControl.rcsImMsgReSendDateActivity((int) msg.getId(), msg.getAddress(), msg.getXml_content());
                break;
            case Type.TYPE_MSG_ENTERPRISE_SHARE_SEND:
                ComposeMessageActivityControl.rcsImMsgReSendEnterpriesShare((int) msg.getId(), msg.getAddress(), msg.getXml_content());
                break;
            case Type.TYPE_MSG_TEXT_SUPER_SMS_SEND:
                ComposeMessageActivityControl.rcsImMsgReSendSuperSms((int) msg.getId(), msg.getAddress(), msg.getBody());
                break;
            case Type.TYPE_MSG_LOC_SEND:
                double longitude = Double.valueOf(LocationUtil.parseLongitude(msg.getBody()));
                double latitude = Double.valueOf(LocationUtil.parseLatitude(msg.getBody()));
                String loc_body = LocationUtil.parseFreeText(msg.getBody());
                String loc_title = LocationUtil.parseTitle(msg.getBody());
                sendLocation((int)msg.getId(),msg.getAddress(),latitude,longitude,1000,loc_title,loc_body);
                break;
            case Type.TYPE_MSG_T_CARD_SEND:
                String xmlContent = OAUtils.replaceDisplayMode(msg.getXml_content());
                ComposeMessageActivityControl.RcsImMsgReSendCard((int)msg.getId(),msg.getAddress(), xmlContent);
                break;
            case Type.TYPE_MSG_BAG_SEND:
                ComposeMessageActivityControl.rcsImMsgSendBag((int) msg.getId(), msg.getAddress(),msg.getBody());
            case Type.TYPE_MSG_CASH_BAG_SEND:
                ComposeMessageActivityControl.rcsImMsgSendCashBag((int) msg.getId(), msg.getAddress(),msg.getBody());
        }
    }

    @Override
    public void saveDraftMessage(boolean save, Message msg) {
        if (TextUtils.isEmpty(mAddress)) {
            return;
        }
        Message oldDraftMessage = MessageUtils.getDraft(mContext, mAddress);
        if (oldDraftMessage != null) {
            MessageUtils.deleteDraft(mContext, oldDraftMessage);
        }

        if (save && msg != null) {
            msg.setBoxType(Type.TYPE_BOX_MESSAGE);
            msg.setAddress(NumberUtils.getPhone(mAddress));
            MessageUtils.insertMessage(mContext, msg);
        }
    }

    @Override
    public Message getDraftMessage() {
        return MessageUtils.getDraft(mContext, mAddress);
    }

    @Override
    public void sendAudio(String path, long length) {
        if (!TextUtils.isEmpty(mAddress)) {
            int action = LogicActions.FILE_TRANSFER_ACTION;
            long duration = length;
            if(duration<=0){  //此处使用 形参里的length， 以规避 半秒 这种情况导致的显示有误。
                duration = FileUtil.getDuring(path);
            }
            rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, action, mAddress, "", path, "", duration);
        }
    }

    @Override
    public void sendAudio(String path, long length, String detail) {
        if (!TextUtils.isEmpty(mAddress)) {
            long duration = length;
            if(duration<=0){  //此处使用 形参里的length， 以规避 半秒 这种情况导致的显示有误。
                duration = FileUtil.getDuring(path);
            }
            ComposeMessageActivityControl.rcsImFileTrsfWithDetail(mAddress, path, duration, detail, "");
        }else {
            LogF.e(TAG, " address is empty : " + mAddress);
        }
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    // 文件传输带缩略图
    public void rcsImFileTrsfCThumb(int userId, int action, String pcUri, String cardString, String pcFileName, String thumbPath, long duration) {
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = action;
        msg.bundle.putInt(LogicActions.USER_ID, userId);
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, pcUri);
        msg.bundle.putString(LogicActions.FILE_TRANSFER_SUBJECT, cardString); //  FILE_TRANSFER_SUBJECT
        msg.bundle.putString(LogicActions.FILE_NAME, pcFileName);
        msg.bundle.putString(LogicActions.FILE_THUMB_PATH, thumbPath);
        msg.bundle.putLong(LogicActions.FILE_RECORD_DURATION, duration);
        IPCUtils.getInstance().send(msg);
    }

    public void rcsImFileTrsfCThumbOrder(int userId, int action, String pcUri, List<MediaItem> list, boolean isOriginalPhoto) {
        ArrayList<SendServiceMsg> msgs = new ArrayList<>();
        for (MediaItem item : list) {
            SendServiceMsg msg = new SendServiceMsg();
            msg.action = action;
            msg.bundle.putInt(LogicActions.USER_ID, userId);
            msg.bundle.putString(LogicActions.PARTICIPANT_URI, pcUri);
            msg.bundle.putString(LogicActions.FILE_TRANSFER_SUBJECT, "");
            msg.bundle.putString(LogicActions.FILE_NAME, item.getLocalPath());
            msg.bundle.putString(LogicActions.FILE_THUMB_PATH, item.getMicroThumbPath());
            msg.bundle.putLong(LogicActions.FILE_RECORD_DURATION, item.getDuration());
            msg.bundle.putBoolean(LogicActions.FILE_IS_ORIGINAL_PHOTO, isOriginalPhoto);

            msgs.add(msg);
        }
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = action;
        msg.bundle.putInt(LogicActions.USER_ID, userId);
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, pcUri);
        msg.bundle.putParcelableArrayList(LogicActions.FILE_TRANSFER_ORDER_ITEM_LIST, msgs);
        IPCUtils.getInstance().send(msg);
    }

    public void reSendMessage(int userId, String message, String size) {
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = LogicActions.MESSAGE_SEND_ACTION;
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, mAddress);
        msg.bundle.putString(LogicActions.MESSAGE_CONTENT, message);
        msg.bundle.putInt(LogicActions.MESSAGE_TYPE, Type.TYPE_MSG_TEXT_SEND);
        msg.bundle.putInt(LogicActions.USER_ID, userId);
        msg.bundle.putString(LogicActions.MESSAGE_SIZE, size);
        IPCUtils.getInstance().send(msg);
    }

    @Override
    public void sendEditImage(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }
        String thumbPath = ThumbnailUtils.createThumb(imagePath, false);
        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, mAddress, "", imagePath, thumbPath, 0);
    }

    public void addToFavorite(Message msg, int chatType, String address) {
        boolean collectSucc = FavoriteUtil.getInstance().addToFavorite(mContext, msg, chatType, address);
        LogF.e(TAG, "addToFavorite msg : " + msg.toString() + "\ncollectSucc:" + collectSucc);
        if (collectSucc) {
            BaseToast.show(mContext, mContext.getString(R.string.collect_succ));
        }
    }
}
