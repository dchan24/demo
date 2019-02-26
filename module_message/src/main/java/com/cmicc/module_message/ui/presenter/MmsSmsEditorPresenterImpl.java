package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.TextUtils;
import android.widget.Toast;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.CalTextLength;
import com.chinamobile.app.yuliao_business.aidl.SendServiceMsg;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.BeanUtils;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_common.utils.SystemUtil;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.broadcast.SmsSentReceiver;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.ui.constract.MmsSmsEditorContracts;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmicc.module_message.ui.fragment.BaseChatFragment;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.MmsSmsEditorModel;
import com.cmcc.cmrcs.android.ui.model.impls.MmsSmsEditorModelImpl;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xieyizhi on 2017/7/21.
 */

public class MmsSmsEditorPresenterImpl implements MmsSmsEditorContracts.Presenter, MmsSmsEditorModel.MmsSmsEditorLoadFinishCallback {

    private static final String TAG = "xyz-MmsSmsEditorPresenterImpl";

    private BaseChatContract.View mMmsSmsView;
    private Context mContext;
    private LoaderManager mLoaderManager;
    private MmsSmsEditorModel mMmsSmsEditorModel;
    private String mAddress; // 会话标识
    private String mPerson; // 会话名
    private String mThreadId;
    private long mLoadTime; //加载的最早时间

    private static final long SIZE_200K = 200 * 1024L;
    public int POST_PIC_SIZE = 500000;

    private int mFirstLoadNum = 0;

    @Override
    public void start() {
        mMmsSmsEditorModel.loadMessages(mContext, mFirstLoadNum,
                mAddress, mLoadTime, mLoaderManager, this);
        showTitleName();
    }

    @Override
    public void onLoadFinished(int loadType, int searchPos, Bundle bundle) {
        mMmsSmsView.updateChatListView(loadType, searchPos, bundle);
    }

    public MmsSmsEditorPresenterImpl(Context context, BaseChatContract.View view, LoaderManager loaderManager, Bundle bundle) {
        mContext = context;
        mMmsSmsView = view;
        mLoaderManager = loaderManager;
        mMmsSmsEditorModel = new MmsSmsEditorModelImpl();

        mAddress = bundle.getString("address");
//        mAddress = NumberUtils.getPhone(mAddress);
        mThreadId = bundle.getString("thread_id");
        mPerson = bundle.getString("person");
        if (mPerson != null && mPerson.equals(mContext.getString(R.string.unknow_phone_num))) {
            mPerson = mAddress;
        }
        mLoadTime = bundle.getLong("loadtime", 0);
        mFirstLoadNum = bundle.getInt("unread", 0);
    }


    @Override
    public void sendSuperMessage(final String message, final String size) {

        boolean isFirstSend = (boolean) SharePreferenceUtils.getDBParam(mContext, "first_send_super_sms", true);
        if (isFirstSend) {
            CommomDialog mDialog = new CommomDialog(mContext, mContext.getString(R.string.msg_reminding),
                    Html.fromHtml(mContext.getString(R.string.send_msg_to)+"<font color='#f7b155'>"+mContext.getString(R.string.china_mobie_free)+"</font>，"+mContext.getString(R.string.send_others_msg_no_free)));
            mDialog.setPositiveName(mContext.getString(R.string.gallery_send));
            mDialog.show();
            mDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {

                @Override
                public void onClick() {

                    SharePreferenceUtils.setDBParam(mContext, "first_send_super_sms", false);
                    rcsImMsgSendSuperSms(message, size);
                }
            });
            mDialog.setOnNegativeClickListener(new CommomDialog.OnClickListener() {

                @Override
                public void onClick() {

                }
            });
        } else {
            rcsImMsgSendSuperSms(message, size);
        }
    }

    /**
     * @param size
     * @param message 超级短信
     */
    private void rcsImMsgSendSuperSms(String message, String size) {
        LogF.d(TAG, "rcsImMsgSendSuperSms mMessage:" + message);
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = LogicActions.MESSAGE_SEND_SUPER_SMS_ACTION;
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, mAddress);
        msg.bundle.putString(LogicActions.MESSAGE_CONTENT, message);
        msg.bundle.putInt(LogicActions.MESSAGE_TYPE, Type.TYPE_MSG_TEXT_SUPER_SMS_SEND);
        msg.bundle.putInt(LogicActions.USER_ID, BaseModel.DEFAULT_VALUE_INTEGER);
        msg.bundle.putString(LogicActions.MESSAGE_SIZE, size);
        IPCUtils.getInstance().send(msg);
    }


    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendMessage(String message, String size) {
        LogF.d(TAG, "发送消息：" + message);
        PackageManager pm = mContext.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.WRITE_SMS", mContext.getPackageName()));
        showSystemParameter();
        if (permission) {
            if (!SystemUtil.isSupportModelSmsSend()) {
                doSendSMSTo(mAddress, message);
            } else {
                insertSMS(mContext, mAddress, message);
            }
            LogF.i(TAG, "有WRITE_SMS这个权限");
        } else {
            doSendSMSTo(mAddress, message);
            LogF.i(TAG, "木有WRITE_SMS这个权限");
        }

//      sendSMS(mContext,mAddress, message);//test
    }

    private void showSystemParameter() {
        Log.e(TAG, "手机厂商：" + SystemUtil.getDeviceBrand());
        Log.e(TAG, "手机型号：" + SystemUtil.getSystemModel());
        Log.e(TAG, "Android系统版本号：" + SystemUtil.getSystemVersion());
    }

    public static void sendSms(Context context, String address, String body) {
        SmsManager manager = SmsManager.getDefault();
        List<String> smss = manager.divideMessage(body);

        Intent intent = new Intent(SmsSentReceiver.SENT_SMS_ACTION);
        //短信发出去后，系统会发送一条广播，告知我们短信发送是成功还是失败
        PendingIntent semtIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);
        for (String text : smss) {
            //这个api只负责发送短信，不会把短息写入数据库
            manager.sendTextMessage(address, null, text, semtIntent, null);
            //把短息插入数据库
            Uri uri = insertSms(context, address, body, 2, true);
            LogF.d(TAG, "发送消息uri：" + uri.toString());
        }
    }

    /**
     * 发送完短息后，需要把短息插入数据库，这样才能在发信息的listView上刷新显示
     *
     * @param context
     * @param address
     * @param body
     */
    public static Uri insertSms(Context context, String address, String body, int type, boolean isRead) {

        ContentValues values = new ContentValues();
//        values.put("address",address);
//        values.put("body",body);
//        values.put("type", 2);
        values.put("address", NumberUtils.getPhone(address)); // 发送地址
        values.put("body", body); // 消息内容
        values.put("date", System.currentTimeMillis()); // 创建时间
        values.put("type", type); // 1:接收;2:发送
        values.put("read", isRead ? 1 : 0); // 0:未读;1:已读
        values.put("seen", isRead ? 1 : 0); // 0:未查看;1:已查看
        values.put("status", 2);// 发送中
        return context.getContentResolver().insert(Uri.parse("content://sms"), values);
    }


    /**
     * 直接调用短信接口发短信
     *
     * @param phoneNumber
     * @param message
     */
    public void sendSMS(Context context, String phoneNumber, String message) {
        //获取短信管理器
        SmsManager smsManager = SmsManager.getDefault();

        String SENT = "sms_sent";
        String DELIVERED = "sms_delivered";

        PendingIntent sentPI = PendingIntent.getActivity(mContext, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getActivity(mContext, 0, new Intent(DELIVERED), 0);

        context.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("====>", "Activity.RESULT_OK");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.i("====>", "RESULT_ERROR_GENERIC_FAILURE");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.i("====>", "RESULT_ERROR_NO_SERVICE");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.i("====>", "RESULT_ERROR_NULL_PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.i("====>", "RESULT_ERROR_RADIO_OFF");
                        break;
                }
            }
        }, new IntentFilter(SENT));

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("====>", "RESULT_OK");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("=====>", "RESULT_CANCELED");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
        //拆分短信内容（手机短信长度限制）
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, sentPI, deliveredPI);
            //把短息插入数据库
            Uri uri = insertSms(context, phoneNumber, message, 2, true);
            LogF.d(TAG, "发送消息uri：" + uri.toString());
        }
    }

    /**
     * 调起系统发短信功能，会调起系统短信app发短信界面
     *
     * @param phoneNumber
     * @param message
     */
    public void doSendSMSTo(String phoneNumber, String message) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", message);
            mContext.startActivity(intent);
        }
    }

    /**
     * 发送短信
     *
     * @param context
     * @param address
     * @param body
     */
    public void insertSMS(Context context, String address, String body) {
        if (TextUtils.isEmpty(body))
            return;
        Uri uri = MessageUtils.insertSMS(context, address, body, 2, true);
        int id = MessageUtils.getIdFromUri(uri);
        Intent itSend = new Intent(SmsSentReceiver.SENT_SMS_ACTION);
        itSend.putExtra("_ID", id);
        LogF.d(TAG, "发送消息uri：" + uri.toString());
        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> mSMSMessage = sms.divideMessage(body);
        int messageCount = mSMSMessage.size();
        PendingIntent mSendPI = PendingIntent.getBroadcast(context, id, itSend,
                PendingIntent.FLAG_ONE_SHOT);
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messageCount);
        for (int i = 0; i < messageCount; i++) {
            sentIntents.add(mSendPI);
        }
        // 适配群发短信
        String[] addrs;
        if (NumberUtils.isGroup(address)) {
            addrs = address.split(";");
        } else {
            addrs = new String[]{address};
        }
        for (String addr : addrs) {
            if (TextUtils.isDigitsOnly(addr)) {
                sms.sendMultipartTextMessage(addr, null, mSMSMessage, sentIntents, null);
            }
        }
        // 群发短信end
        // 更新会话列表缓存
//        if (uri!=null && StringUtil.isEmpty(ConvCache.get().getThreadId(address))) {
//            Cursor cursor = context.getContentResolver().query(uri, new String[] {
//                    BaseModel.COLUMN_NAME_THREAD_ID }, null, null, null);
//            if (cursor != null) {
//                try {
//                    if (cursor.moveToFirst()) {
//                        String threadId = cursor.getString(0);
//                        if (!StringUtil.isEmpty(threadId)) {
//                            ConvCache.get().addThreadId(address, threadId);
//                        }
//                    }
//                } finally {
//                    cursor.close();
//                }
//            }
//        }
    }

    @Override
    public void updateUnreadCount() {
        if (!StringUtil.isEmpty(mAddress)) {
            ConversationUtils.updateSeen(mContext, Type.TYPE_BOX_SMS, mAddress, "");
        }
    }

    @Override
    public void loadMoreMessages() {
        mMmsSmsEditorModel.loadMoreMessages(mLoaderManager);
    }

    private void showTitleName() {
        String name = CalTextLength.handleText(mPerson, 20);
        //mMmsSmsView.showTitleName(name);
        mMmsSmsView.showTitleName(mPerson);
    }


    @Override
    public void sendImgAndVideo(ArrayList<MediaItem> list) {
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
        if (lenth >= BaseChatFragment.MAX_FILE_LENGTH) {
            BaseToast.makeText(mContext, R.string.max_file_length, Toast.LENGTH_SHORT).show();
            return;
        }
        ComposeMessageActivityControl.rcsImFileTrsfByFileSystem(mAddress, path, FileUtil.getDuring(path));
    }

    @Override
    public void reSend(Message msg) {
        int type = msg.getType();
        switch (type) {
            case Type.TYPE_MSG_TEXT_SEND:
//                MessageUtils.delete(mContext, msg.getId());
                //sendMessage(msg.getBody());
//                sendMessage(msg.getBody(), msg.getTextSize());
                break;
        }

    }

    @Override
    public void sendWithdrawnMessage(Message msg) {
        Log.i(TAG, "bingle--sendWithdrawnMessage--MessageEditorPresenterImpl");
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
//        Toast.makeText(mContext,"暂不支持删除短信", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "bingle--delete Message--single");
//        MessageUtils.delete(mContext, msg.getId());
        Message m = new Message();
        m.setId(msg.getId());
        m.setStatus(Status.STATUS_DELETE);
        MessageUtils.updateMessage(mContext, m);
    }

    /*
     * 保存草稿信息
     */
    @Override
    public void saveDraftMessage(boolean save, Message Msg) {
        if (TextUtils.isEmpty(mAddress)) {
            return;
        }
        Message oldDraftMessage = MessageUtils.getDraft(mContext, mAddress);
        if (oldDraftMessage != null) {
            MessageUtils.deleteDraft(mContext, oldDraftMessage);
        }
        if (Msg == null) {
            return;
        }
        Msg.setAddress(NumberUtils.getPhone(mAddress));
        if (save) {
            MessageUtils.insertMessage(mContext, Msg);
        }

        Conversation conv = new Conversation();
        conv.setAddress(Msg.getAddress());
        conv.setDate(Msg.getDate());
        conv.setBoxType(Msg.getBoxType());
        conv.setBody(Msg.getBody());
        conv.setType(Msg.getType());
        conv.setStatus(Msg.getStatus());
        if (mAddress.startsWith("10658139")) {
            conv.setAddress("10658139");
            conv.setBoxType(Type.TYPE_BOX_MAIL);
        } else if (mAddress.startsWith("106")) {
            conv.setAddress("106");
            conv.setBoxType(Type.TYPE_BOX_NOTIFY);
        }
        ConversationUtils.update(mContext, conv.getAddress(), BeanUtils.fillContentValues(conv));
    }

    @Override
    public Message getDraftMessage() {
        return MessageUtils.getDraft(mContext, mAddress);
    }

    @Override
    public void sendAudio(String path) {
        if (!LoginUtils.getInstance().isLogined() || AndroidUtil.isNetworkConnected(mContext)) {
            BaseToast.show(mContext, mContext.getResources().getString(R.string.no_login_or_no_net));
            return;
        }
        if (!TextUtils.isEmpty(mAddress)) {
            int action = LogicActions.FILE_TRANSFER_ACTION;
            rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, action, mAddress, "", path, "", FileUtil.getDuring(path));
        }
    }

    @Override
    public void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) {
        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, mAddress, pcSubject, pcFileName, "", duration);

    }

    // 文件传输带缩略图
    public void rcsImFileTrsfCThumb(int userId, int action, String pcUri, String pcSubject, String pcFileName, String thumbPath, long duration) {
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = action;
        msg.bundle.putInt(LogicActions.USER_ID, userId);
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, pcUri);
        msg.bundle.putString(LogicActions.MESSAGE_DETAIL, pcSubject);
        msg.bundle.putString(LogicActions.FILE_NAME, pcFileName);
        msg.bundle.putString(LogicActions.FILE_THUMB_PATH, thumbPath);
        msg.bundle.putLong(LogicActions.FILE_RECORD_DURATION, duration);
        IPCUtils.getInstance().send(msg);
    }
}
