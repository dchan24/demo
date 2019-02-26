package com.cmicc.module_message.ui.broadcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.logic.BusinessGlobalLogic;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.OAList;
import com.chinamobile.app.yuliao_business.model.YunFile;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.provider.Conversations.Group;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.OAUtils;
import com.chinamobile.app.yuliao_business.util.PlatformUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.sysetem.MetYouActivityManager;
import com.chinamobile.app.yuliao_common.utils.CommonConstant;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.ObjectUtils;
import com.chinamobile.app.yuliao_common.utils.Setting;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.osutils.AppNotificationHelper;
import com.cmcc.cmrcs.android.ui.activities.HomeActivity;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.utils.BadgeUtil;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.MailAssistantUtils;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.ui.utils.VibratorUtil;
import com.cmcc.cmrcs.android.ui.utils.YunFileXmlParser;
import com.cmcc.cmrcs.android.widget.emoji.EmojiParser;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.MailMsgListActivity;
import com.cmicc.module_message.ui.activity.MailOAMsgListActivity;
import com.cmicc.module_message.ui.activity.SysMsgActivity;
import com.constvalue.MessageModuleConst;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

import cn.com.mms.jar.pdu.EncodedStringValue;
import cn.com.mms.jar.pdu.PduPersister;
import cn.com.mms.utils.MmsUtils;
import rx.functions.Func1;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.chinamobile.app.yuliao_common.application.MyApplication.CHANNEL_ID_MSG_NOTIFICATION;
import static com.chinamobile.app.yuliao_common.application.MyApplication.CHANNEL_NAME_MSG_NOTIFICATION;
import static com.constvalue.MessageModuleConst.MsgNotificationReceiverConst.FROM_MSG_NOTIFICATION;

/**
 * Created by situ on 2017/4/10.
 * 接收通知进行消息提醒的广播
 */
public class MsgNotificationReceiver extends BroadcastReceiver {

    public final String TAG = getClass().getSimpleName();



    // 全局开关
    public static boolean sNotifyStatus = true;
    // 当前页面是否是聊天列表
    public static boolean sIsCurrentConvList = false;
    // 当前会话对象address
    private static String sCurrentAddress = "";

    public static long sLastNotifyTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null || App.getApplication() == null) {
            return;
        }
        if (action.equals(BusinessGlobalLogic.NOTIFICATION_FOR_MESSAGE_RECV)) {
            notifyMessageRecv(context, intent);
        }

    }

    public static void setCurrentAddress(String address) {
        sCurrentAddress = address;
    }

    public static void clearCurrentAddress() {
        sCurrentAddress = "";
    }

    public static void setIsCurrentConvList(boolean isCurrentConvList) {
        sIsCurrentConvList = isCurrentConvList;
    }

    /**
     * 判断当前是否是address的会话界面
     */
    private static boolean isInConversation(String address) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }
        return address.equals(sCurrentAddress);
    }

    private void notifyMessageRecv(Context context, Intent intent) {
        sysUnreadMsgs(context, intent);

        // 是否要消息提醒
        if (!sNotifyStatus) {
            return;
        }
        boolean isSilent = intent.getBooleanExtra(BusinessGlobalLogic.RECIVE_MSG_SILENCE, false);
        if (isSilent) {
            return;
        }

        // 发送通知
        showMsgNotification(context, intent);
    }


    //震动milliseconds毫秒
    public static void vibrate(final Context context, long milliseconds) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    //以pattern[]方式震动
    public static void vibrate(final Context context, long[] pattern, int repeat) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, repeat);
    }

    public static void ring(Context context) {
        Uri uri = null;
        if(VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            uri =RingtoneManager.getActualDefaultRingtoneUri(context ,RingtoneManager.TYPE_NOTIFICATION);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_MSG_NOTIFICATION,
                    CHANNEL_NAME_MSG_NOTIFICATION, NotificationManager.IMPORTANCE_DEFAULT);
            uri = channel.getSound();
            if(uri == null){
                uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.newmessage);
            }
        }else{
            uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.newmessage);
        }
        Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
        r.play();
    }

    private void showMsgNotification(final Context context, Intent intent) {
        String address0rigin = intent.getStringExtra(BusinessGlobalLogic.SMS_ADDRESS_FILE);

        if (ObjectUtils.isNull(address0rigin)) {
            return;
        }
        final String address = generateAddress(context, address0rigin);
        //是否是离线消息
        final boolean isOfflineMsg = intent.getBooleanExtra(BusinessGlobalLogic.IS_OFFLINE, false);
        //是否是第一条离线消息
        final boolean isFirstOfflineMsg = intent.getBooleanExtra(BusinessGlobalLogic.IS_FIRST_OFFLINE_MSG, false);

        String sendAddress = intent.getStringExtra(BusinessGlobalLogic.SMS_SEND_ADDRESS);
        final boolean isGroup = intent.getBooleanExtra(BusinessGlobalLogic.IS_GROUP, false);
        // 提取发送内容
        final int msgContentType = intent.getIntExtra(BusinessGlobalLogic.NOTIFICATION_TYEP, Type.TYPE_BOX_MESSAGE);
        String body = intent.getStringExtra(BusinessGlobalLogic.SMS_CONTENT);

        String myNum = LoginUtils.getInstance().getLoginUserNameWithCountryCode();
        if (TextUtils.isEmpty(address) || "@pc".equalsIgnoreCase(address)
                || (!TextUtils.isEmpty(sendAddress) && sendAddress.equals(myNum))) {
            return;
        }

        if(isOfflineMsg){//离线消息不再通知提醒
            LogF.d(TAG ,"notification msg is offline");
            return;
        }

        boolean enable = !AppNotificationHelper.areNotificationEnable(context);// 系统消息通知关闭
        if (enable) {
            BadgeUtil.resetBadgeCount(null, context);
            return;
        }

        // 对应聊天设置是否设置消息免打扰
        boolean isSilent = ConversationUtils.isSlient(context, address);
        if (isSilent) {
            return;
        }

        LogF.i(TAG, "isOfflineMsg = " + isOfflineMsg + " IsCurrentConvList = " + sIsCurrentConvList);
        if ((sIsCurrentConvList || isInConversation(address))) {
            if(VERSION.SDK_INT >= Build.VERSION_CODES.O){//8.0以上在应用内不再提示声音
                return;
            }
            if (System.currentTimeMillis() - sLastNotifyTime < 3000) {
                // 3秒内来的消息不震动，不响铃
                return;
            } else {
                sLastNotifyTime = System.currentTimeMillis();
            }

//            if (MetYouActivityManager.getInstance().getCurrentActivity() instanceof MessageDetailActivity) {
            if (MessageProxy.g.getUiInterface().isAppointedActivity(MetYouActivityManager.getInstance().getCurrentActivity()
                    , MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY)) {
                if (Setting.getInstance().getNewMessageShock() && !isSystemSilent(context)) {
                    // 铃声关闭，震动打开
                    LogF.e(TAG, "铃声关闭，震动打开");
                    VibratorUtil.getInstance(MyApplication.getApplication()).vibrator(new long[]{0, 200, 100, 200});
                } else {
                    // 铃声关闭，震动关闭
                    LogF.e(TAG, "铃声关闭，震动关闭");
                }
            } else {
                if (Setting.getInstance().getNewMessageRing() && isSystemRingOpen(context)) {
                    if (Setting.getInstance().getNewMessageShock()) {
                        //铃声打开，震动打开
                        ring(context);
                        vibrate(context, new long[]{0, 200, 100, 200}, -1);

                    } else {
                        //铃声打开，震动关闭
                        ring(context);
                    }
                } else {

                    if (Setting.getInstance().getNewMessageShock() && !isSystemSilent(context)) {
                        // 铃声关闭，震动打开
                        LogF.e(TAG, "铃声关闭，震动打开");
                        VibratorUtil.getInstance(MyApplication.getApplication()).vibrator(new long[]{0, 200, 100, 200});
                    } else {
                        // 铃声关闭，震动关闭
                        LogF.e(TAG, "铃声关闭，震动关闭");
                    }
                }

            }
            return;
        }


        LogF.e(TAG, "showMsgNotification  address = " + address + ", sendAddress = " + sendAddress
                + ", body = " + body + ", msgContentType = " + msgContentType);
        if (isGroup) {
            LogF.e(TAG, "showMsgNotification  Group NickName = " + NickNameUtils.getNickName(context, sendAddress, address));
            LogF.e(TAG, "showMsgNotification  群昵称 = " + GroupChatUtils.getPerson(context, address));
        } else {
            LogF.e(TAG, "showMsgNotification  NickName = " + NickNameUtils.getNickName(address));
        }

        final Conversation conv = ConvCache.getInstance().getConversationByAddress(address);

        if (conv != null) {
            LogF.e(TAG, "----------------------------------------------------");
            LogF.e(TAG, "showMsgNotification  conv.getBoxType()      = " + conv.getBoxType());
            LogF.e(TAG, "showMsgNotification  conv.getType()         = " + conv.getType());
            LogF.e(TAG, "showMsgNotification  conv.getSendAddress()  = " + conv.getSendAddress());
            LogF.e(TAG, "showMsgNotification  conv.getAddress()      = " + conv.getAddress());
            LogF.e(TAG, "showMsgNotification  conv.getPerson()       = " + conv.getPerson());

            if (TextUtils.isEmpty(conv.getPerson())) { //修复通过push进入 会话顶部昵称消失
                conv.setPerson(isGroup ? GroupChatUtils.getPerson(context, address) : NickNameUtils.getNickName(address));
            }
        }

        new RxAsyncHelper<>("").runInSingleFixThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {

                int unReadSmsMmsAndMsgCount = 0;

                if (conv == null) {
                    return null;
                }

                unReadSmsMmsAndMsgCount = conv.getUnReadCount();
                if (unReadSmsMmsAndMsgCount > 0) {
                    String title = String.format(context.getString(R.string.not_read_news), unReadSmsMmsAndMsgCount);
                    String text = context.getString(R.string.click_see_detail);
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.putExtra(FROM_MSG_NOTIFICATION, true);

                    Intent intent2 = getIntent(context, conv);
                    if (intent2 != null) {
                        intent = intent2;
                    }

                    //是否再通知列表显示消息详情
                    if (Setting.getInstance().getMessageDetailVisible()) {
                        // oppo手机通过ticker显示横幅内容，产品说针对oppo做适配
                        if (Build.MANUFACTURER.toLowerCase().contains("oppo") && !isGroup) {
                            MsgNotificationReceiver.this.notify(context, getMessageTitle(context, conv),
                                    getUnreadCount(context, conv) + getMessageTitle(context, conv) +
                                            ": " + getMessageContent(context, conv),
                                    intent, address.hashCode(), isOfflineMsg);
                        } else {
                            MsgNotificationReceiver.this.notify(context, getMessageTitle(context, conv),
                                    getUnreadCount(context, conv) + getMessageContent(context, conv),
                                    intent, address.hashCode(), isOfflineMsg);
                        }
                    } else {
                        MsgNotificationReceiver.this.notify(context, title,
                                text, intent, address.hashCode(), isOfflineMsg);
                    }

                }
                return null;
            }
        }).subscribe();

    }

    private String generateAddress(Context context, String addressOrigin) {
        // 单聊时， 香港号码不能截取+86
        if (!NumberUtils.isHKLoginNum(context) && addressOrigin.startsWith("+86")) {
            addressOrigin = addressOrigin.substring(3, addressOrigin.length());
        }
        return addressOrigin;
    }


    public Intent getIntent(Context context, Conversation conversation) {
        LogF.e("time debug", "time open ---" + java.lang.System.currentTimeMillis());

        int boxType = conversation.getBoxType();
        String clzName = null;

        Intent intent = null;
        Bundle bundle = new Bundle();
        bundle.putBoolean(FROM_MSG_NOTIFICATION, true);
        if (boxType == 2048) {
            boxType = Type.TYPE_BOX_SMS;//sms-mms
        }
        if ((boxType & Type.TYPE_BOX_MESSAGE) > 0) {
            if (conversation.getAddress().equals(ConversationUtils.addressPc)) {
                clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PC_MESSAGE_FRAGMENT_CLASS);
                bundle.putString("address", conversation.getAddress());
                bundle.putString("person", context.getString(R.string.my_computer));
                bundle.putLong("loadtime", 0);
                bundle.putString("clzName", clzName);
                if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                    bundle.putString("draft", conversation.getBody());
                }
                bundle.putInt("unread", conversation.getUnReadCount());

//                intent = new Intent(context, MessageDetailActivity.class);
//                intent.putExtras(bundle);
                intent = MessageProxy.g.getServiceInterface().getIntentToActivity(context,MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY);
            } else if (conversation.getAddress().contains(";")) {
                clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.LABEL_GROUP_CHAT_FRAGMENT_CLASS);
                bundle.putString("address", conversation.getAddress());
                bundle.putString("person", conversation.getPerson());
                bundle.putLong("loadtime", 0);
                bundle.putString("clzName", clzName);
                if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                    bundle.putString("draft", conversation.getBody());
                }
                bundle.putInt("unread", conversation.getUnReadCount());

//                intent = new Intent(context, MessageDetailActivity.class);
//                intent.putExtras(bundle);
                intent = MessageProxy.g.getServiceInterface().getIntentToActivity(context,MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY);
            } else {

                clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS);
                bundle.putString("address", conversation.getAddress());
                bundle.putString("person", NickNameUtils.getPerson(context, boxType, conversation.getAddress()));
                boolean isSlient = conversation.getSlientDate() > 0;
                bundle.putBoolean("slient", isSlient);
                bundle.putLong("loadtime", 0);
                bundle.putString("clzName", clzName);
                if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                    bundle.putString("draft", conversation.getBody());
                }
                bundle.putInt("unread", conversation.getUnReadCount());

//                intent = new Intent(context, MessageDetailActivity.class);
//                intent.putExtras(bundle);
                intent = MessageProxy.g.getServiceInterface().getIntentToActivity(context,MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY);
            }
        } else if ((boxType & Type.TYPE_BOX_GROUP) > 0) {

            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.GROUP_CHAT_MESSAGE_FRAGMENT_CLASS);
            bundle.putString("address", conversation.getAddress());
            bundle.putString("person", conversation.getPerson());
            boolean isSlient = conversation.getSlientDate() > 0;
            bundle.putBoolean("slient", isSlient);
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
            if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                bundle.putString("draft", conversation.getBody());
            }
            bundle.putInt("unread", conversation.getUnReadCount());
            if (conversation.getNotifyDate() > 0) {
                bundle.putBoolean("has_at_msg", true);
            }
            if (conversation.getGroupType() == MtcImConstants.EN_MTC_GROUP_TYPE_ENTERPRISE) {
                bundle.putBoolean("isEPgroup", true);
            } else if (conversation.getGroupType() == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY) {
                bundle.putBoolean("isPartyGroup", true);
            }
            //群类型,之后都用这种吧,不要传上面那个了
            bundle.putInt("grouptype", conversation.getGroupType());

//            intent = new Intent(context, MessageDetailActivity.class);
//            intent.putExtras(bundle);
            intent = MessageProxy.g.getServiceInterface().getIntentToActivity(context,MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY);
        } else if ((boxType & Type.TYPE_BOX_SYSMSG) > 0) {
            intent = new Intent(context, SysMsgActivity.class);

        } else if ((boxType & Type.TYPE_BOX_SMS) > 0 || (boxType & Type.TYPE_BOX_MMS) > 0) {

            LogF.d("xyz", "短信消息boxType");
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MSM_SMS_FRAGMENT_CLASS);
            bundle.putString("address", conversation.getAddress());
            bundle.putString("thread_id", conversation.getThreadId());
            bundle.putString("person", conversation.getPerson());
            boolean isSlient = conversation.getSlientDate() > 0;
            bundle.putBoolean("slient", isSlient);
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
            if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                bundle.putString("draft", conversation.getBody());
            }
            bundle.putInt("unread", conversation.getUnReadCount());

//            intent = new Intent(context, MessageDetailActivity.class);
//            intent.putExtras(bundle);
            intent = MessageProxy.g.getServiceInterface().getIntentToActivity(context,MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY);

        } else if ((boxType & Type.TYPE_BOX_PLATFORM) > 0 || (boxType & Type.TYPE_BOX_PLATFORM_DEFAULT) > 0) {
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PUBLIC_ACCOUNT_CHAT_FRAGMENT_CLASS);
            bundle.putString("name", conversation.getPerson());
            bundle.putString("address", conversation.getAddress());
            bundle.putString("clzName", clzName);
            bundle.putString("iconpath", PlatformUtils.getPlatformIcon(context, conversation.getAddress()));
            intent = MessageProxy.g.getServiceInterface().getIntentToActivity(context,MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY);
//            intent = new Intent(context, MessageDetailActivity.class);
//            intent.putExtras(bundle);
        } else if ((boxType & Type.TYPE_BOX_NOTIFY) > 0) {
            MessageProxy.g.getUiInterface().getNotifySmsActivityIntent(context, MessageModuleConst.NotifySmsActivityConst.SOURCE_NOTIFYSMS);
        } else if ((boxType & Type.TYPE_BOX_MAILASSISTANT) > 0) { // Mail Assistant
            intent = new Intent(context, MailMsgListActivity.class);
        } else if ((boxType & Type.TYPE_BOX_MAIL_OA) > 0 || (boxType & Type.TYPE_BOX_OA) > 0) {// Mail OA
            intent = new Intent(context, MailOAMsgListActivity.class);
            intent.putExtra("address", conversation.getAddress());
            intent.putExtra("boxtype", conversation.getBoxType());
        }
        return intent;
    }


    private String getMessageContent(final Context context, final Conversation conv) {
        if (TextUtils.isEmpty(conv.getBody()) && conv.getType() == Type.TYPE_MSG_TEXT_RECV) {
            return context.getString(R.string.click_see_detail);
        }
        long time = System.currentTimeMillis();
        int type = conv.getType();
        int status = conv.getStatus();
        LogF.d(TAG, " type:" + type + " status:" + status);
        final int boxType = conv.getBoxType();
        String content = conv.getBody();
        switch (type) {
            case Type.TYPE_MSG_SYSTEM_TEXT:
            case Type.TYPE_MSG_TEXT_QUEUE:
            case Type.TYPE_MSG_TEXT_OUTBOX:
            case Type.TYPE_MSG_TEXT_FAIL:
            case Type.TYPE_MSG_TEXT_SUPER_SMS_SEND:
            case Type.TYPE_MSG_TEXT_RECV:
            case Type.TYPE_MSG_TEXT_SEND:
            case Type.TYPE_MSG_TEXT_SEND_CCIND:
            case Type.TYPE_MSG_TEXT_DRAFT:
            case Type.TYPE_MSG_SINGLE_PIC_TEXT_RECV:
            case Type.TYPE_MSG_MULIT_PIC_TEXT_RECV:
            case Type.TYPE_SMS_NOTICE:
            case Type.TYPE_SYSMSG_VCARD:
                break;
            case Type.TYPE_MSG_SMS_RECV:
                content = context.getString(R.string.news) + conv.getBody();
                break;
            case Type.TYPE_MSG_MMS_RECV:
                // 彩信的body查询的字段为其主题： sub
                if (!TextUtils.isEmpty(content)) {
                    EncodedStringValue v = null;
                    v = new EncodedStringValue(106, PduPersister.getBytes(content));
                    if (MmsUtils.isGarbled(v.getString())) {
                        content = MmsUtils.getStringOfGarbled(content, 6);
                    } else {
                        content = v.getString();
                    }
                    content = context.getString(R.string.super_news) + context.getString(R.string.title) + content;
                } else {
                    content = context.getString(R.string.super_news) + context.getString(R.string.no_subject);
                }
                break;
            case Type.TYPE_MSG_IMG_RECV:
            case Type.TYPE_MSG_IMG_SEND_CCIND:
            case Type.TYPE_MSG_IMG_SEND:
                if(TextUtils.isEmpty(content)){
                    content = context.getString(R.string.picture_);
                }
                break;
            case Type.TYPE_MSG_AUDIO_RECV:
            case Type.TYPE_MSG_AUDIO_SEND:
            case Type.TYPE_MSG_AUDIO_SEND_CCIND:
                content = context.getString(R.string.voice_);
                break;
            case Type.TYPE_MSG_FILE_YUN_SEND:
            case Type.TYPE_MSG_FILE_SEND:
            case Type.TYPE_MSG_FILE_SEND_CCIND:
            case Type.TYPE_MSG_FILE_YUN_RECV:
            case Type.TYPE_MSG_FILE_RECV:
                content = loadFileMsgName(context, conv);
                if (TextUtils.isEmpty(content)) {
                    content = context.getString(R.string.file_);
                } else {
                    content = context.getString(R.string.file_) + content;
                }
                break;
            case Type.TYPE_MSG_CARD_SEND:
            case Type.TYPE_MSG_CARD_SEND_CCIND:
            case Type.TYPE_MSG_CARD_RECV:
                content = context.getString(R.string.business_card_);
                break;
            case Type.TYPE_MSG_VIDEO_SEND:
            case Type.TYPE_MSG_VIDEO_RECV:
            case Type.TYPE_MSG_VIDEO_SEND_CCIND:
                content = context.getString(R.string.video_);
                break;
            case Type.TYPE_MSG_OA_ONE_CARD_SEND:
            case Type.TYPE_MSG_OA_ONE_CARD_RECV:
                String body = conv.getBody();
                if (TextUtils.isEmpty(body)) {
                    content = context.getString(R.string.oa_message);
                } else {
                    content = body;
                    conv.setLatestContent(null);
                }
                break;
            case Type.TYPE_MSG_CARD_VOUCHER_SEND:
            case Type.TYPE_MSG_CARD_VOUCHER_RECV:
                if(TextUtils.isEmpty(conv.getBody())){
                    content = context.getString(R.string.card_voucher_message);
                }else {
                    content = conv.getBody();
                    conv.setLatestContent(null);
                }
                break;
            case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
            case Type.TYPE_MSG_DATE_ACTIVITY_RECV:
                content = context.getString(R.string.date_activity_message);
                break;
            case Type.TYPE_MSG_T_CARD_SEND:
            case Type.TYPE_MSG_T_CARD_SEND_CCIND:
            case Type.TYPE_MSG_T_CARD_RECV:
            case Type.TYPE_MSG_ENTERPRISE_SHARE_SEND:
            case Type.TYPE_MSG_ENTERPRISE_SHARE_RECV:
                content = context.getString(R.string.enterprise_share_message);
                break;
            case Type.TYPE_MSG_WITHDRAW_REVOKE:
                content = conv.getBody();
                break;
            case Type.TYPE_MSG_BAG_SEND:
            case Type.TYPE_MSG_CASH_BAG_SEND:
            case Type.TYPE_MSG_BAG_RECV:
            case Type.TYPE_MSG_CASH_BAG_RECV:
                content = context.getString(R.string.meetyou_red_message) + conv.getBody();
                break;
            case Type.TYPE_MSG_BAG_RECV_COMPLETE:
            case Type.TYPE_MSG_BAG_SEND_COMPLETE:
                content = conv.getBody();
                break;
            case Type.TYPE_MSG_OA_CARD_SEND:
            case Type.TYPE_MSG_OA_CARD_RECV:
                content = conv.getBody();
                break;
            case Type.TYPE_MSG_LOC_RECV:
            case Type.TYPE_MSG_LOC_SEND:
            case Type.TYPE_MSG_LOC_SEND_CCIND:
                content = context.getString(R.string.location_type);
                break;
            default:
                content = context.getString(R.string.unkwown_type_);
        }

        final int mEmojiconSize = 47;
        // 设置草稿内容
        if (conv.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
            CharSequence draftBuilder = EmojiParser.getInstance().replaceAllEmojis(context, conv.getBody(), mEmojiconSize);
            return draftBuilder.toString();
        }
        CharSequence builderEmoji = null;
        SpannableStringBuilder builder = conv.getLatestContent();
        if (builder != null) {

        } else if ((boxType & Type.TYPE_BOX_GROUP) > 0) {
            if ((type & Type.TYPE_RECV) > 0) {
                long extNotify = conv.getNotifyDate();
                long time1 = System.currentTimeMillis();
                String nickName = NickNameUtils.getNickName(context, conv.getSendAddress(), conv.getAddress());
                LogF.d("abc", "nick name time : " + (System.currentTimeMillis() - time1));
                if (extNotify > 0) {
                    boolean isSlient = conv.getSlientDate() > 0;
                    int unreadCount = conv.getUnReadCount();
                    String slientString = "";
                    if (isSlient && unreadCount > 0) {
                        slientString = "[" + unreadCount + context.getString(R.string.news_unit) + "] ";
                        if (unreadCount > 99) {
                            slientString = context.getString(R.string.lots_of_news);
                        }
                    }
                    String atString = context.getString(R.string.notify_me).intern();
                    String extString = slientString + atString;
                    builder = new SpannableStringBuilder(extString);
                    ForegroundColorSpan redSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.message_draft));
                    builder.setSpan(redSpan, slientString.length(), slientString.length() + atString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builderEmoji = EmojiParser.getInstance().replaceAllEmojis(context, nickName + ": " + content, mEmojiconSize);
                    builder.append(builderEmoji);
                    conv.setLatestContent(builder);
                } else {
                    builder = EmojiParser.getInstance().replaceAllEmojis(context, nickName + ": " + content, mEmojiconSize);
                    conv.setLatestContent(builder);
                }
            } else if (boxType == Type.TYPE_BOX_MAIL_OA || boxType == Type.TYPE_BOX_OA || boxType == Type.TYPE_BOX_ONLY_ONE_LEVEL_OA) {
                //todo oa消息未处理
                if (TextUtils.isEmpty(content)) {
                    return context.getString(R.string.oa_news);
                } else {
                    return content;
                }
            } else {
                builder = EmojiParser.getInstance().replaceAllEmojis(context, content, mEmojiconSize);
                conv.setLatestContent(builder);
            }
        } else {
            builder = EmojiParser.getInstance().replaceAllEmojis(context, content, mEmojiconSize);
            conv.setLatestContent(builder);
        }
        LogF.d("abc", "bind content time: " + (System.currentTimeMillis() - time));
        return builder.toString();

    }

    private String getUnreadCount(Context context, final Conversation conv) {
        int unreadCount = conv.getUnReadCount();

        String unRead;

        if (unreadCount <= 1) {
            unRead = "";
        } else if (unreadCount >= 100) {
            unRead = context.getString(R.string.lots_of_news);
        } else {
            unRead = "[" + unreadCount + context.getString(R.string.news_unit) + "] ";
        }

        return unRead;
    }

    private String getMessageTitle(final Context context, final Conversation conv) {
        int boxType = conv.getBoxType();
        String person = conv.getPerson();

        if (boxType == Type.TYPE_BOX_PLATFORM || boxType == Type.TYPE_BOX_PLATFORM_DEFAULT) {
            if (!TextUtils.isEmpty(person)) {
                return person;
            }
        } else if (boxType == Type.TYPE_BOX_MAILASSISTANT) {
            if (TextUtils.isEmpty(conv.getPerson())) {
                conv.setPerson(context.getString(R.string.email_control_helper));
            }
            return context.getString(R.string.email_control_helper);
        } else if (boxType == Type.TYPE_BOX_MAIL_OA || boxType == Type.TYPE_BOX_OA || boxType == Type.TYPE_BOX_ONLY_ONE_LEVEL_OA) {
            String oaPerson = conv.getPerson();
            if (!TextUtils.isEmpty(oaPerson)) {
                return oaPerson;
            }
        }
        String name = getPerson(conv, context);
        conv.setPerson(name);
        return name;
    }


    private String getPerson(Conversation conv, Context context) {
        String address = conv.getAddress();
        if (StringUtil.isEmpty(address)) {
            return "null".intern();
        }
        int boxType = conv.getBoxType();
        LogF.d(TAG, "boxType:" + boxType);
        if (boxType == Type.TYPE_BOX_GROUP) {
            String person = conv.getPerson();
            if (TextUtils.isEmpty(person)) {
                person = NickNameUtils.getPerson(context, boxType, address);
            }
            return person;
        } else if (boxType == Type.TYPE_BOX_MAIL_OA || boxType == Type.TYPE_BOX_OA || boxType == Type.TYPE_BOX_ONLY_ONE_LEVEL_OA) {
            if (!TextUtils.isEmpty(address) && address.contains("@")) {
                address = address.substring(0, address.indexOf("@"));
            }
            OAList oa = OAUtils.getOA(context, address);
            ContentValues values = new ContentValues();
            values.put(BaseModel.COLUMN_NAME_PERSON, oa.getName());
            values.put(BaseModel.COLUMN_NAME_ICON_PATH, oa.getLogo());
            ConversationUtils.update(context, address, values);
            return oa.getName();
        }
        return NickNameUtils.getPerson(context, boxType, address);
    }

    public static void clearMsgNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();
    }



    private void notify(Context context, String title, String text, Intent intent, final int id, boolean isOfflineMsg) {
        LogF.e(TAG, "notify  title = " + title + ", text = " + text + ", id = " + id);
        NotificationCompat.Builder builder = null;
        if(VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder = new NotificationCompat.Builder(context, CHANNEL_ID_MSG_NOTIFICATION);
        }else{
            builder = new NotificationCompat.Builder(context);
        }

        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(true);

        if (!isOfflineMsg) {
            // 显示横幅通知的2个必要条件
            // 1.通知必须设置优先级最高，并且不要调用setFullScreenIntent
            // 2.声音和振动两个必须设置一个，不然framework会把通知降级
            builder.setPriority(Notification.PRIORITY_MAX);
            if (System.currentTimeMillis() - sLastNotifyTime < 3000) {
                // 3秒内来的消息不震动，不响铃, 或者消息开关没打开也不震动，不响铃
                LogF.e(TAG, "3秒内来的消息不震动，不响铃");
            } else {
                sLastNotifyTime = System.currentTimeMillis();
                if (Setting.getInstance().getNewMessageRing() && isSystemRingOpen(context)) {
                    if (Setting.getInstance().getNewMessageShock()) {
                        // 铃声打开，震动打开
                        LogF.e(TAG, "铃声打开，震动打开");
                        Uri notification = getRingUri(context);
                        builder.setSound(notification);
//                        SoundPlayUtil.play(1);
                        builder.setVibrate(new long[]{0, 200, 100, 200});
//                        VibratorUtil.getInstance(MyApplication.getApplication()).vibrator(new long[]{0, 200, 100, 200});
                    } else {
                        // 铃声打开，震动关闭
                        LogF.e(TAG, "铃声打开，震动关闭");
                        Uri notification = getRingUri(context);
                        builder.setSound(notification);
//                        SoundPlayUtil.play(1);
                    }
                } else {
                    if (Setting.getInstance().getNewMessageShock() && !isSystemSilent(context)) {
                        // 铃声关闭，震动打开
                        LogF.e(TAG, "铃声关闭，震动打开");
                        builder.setVibrate(new long[]{0, 200, 100, 200});
//                        VibratorUtil.getInstance(MyApplication.getApplication()).vibrator(new long[]{0, 200, 100, 200});
                    } else {
                        // 铃声关闭，震动关闭
                        LogF.e(TAG, "铃声关闭，震动关闭");
                    }
                }
            }
        }

        builder.setSmallIcon(R.drawable.logo_notify);
        if (android.os.Build.VERSION.SDK_INT < 21) {
            builder.setSmallIcon(R.drawable.logo_notify);// 较大分辨率情况下导致显示不全
        } else {
            // 适配android5.0以上版本，smallicon只支持白色和透明，不支持其他颜色
            builder.setSmallIcon(R.mipmap.ic_launcher_home_notification_small_white);
        }
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_notify));
        builder.setColor(Color.parseColor("#157CF8"));
        if (Build.MANUFACTURER.toLowerCase().contains("vivo") ||
                Build.MANUFACTURER.toLowerCase().contains("oppo")) {
            builder.setTicker(text);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        BadgeUtil.updateBadgeCount(notification, context);
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.notify(id, notification);
    }

    private static Uri getRingUri(Context context) {
        //应用在后台使用手机系统默认铃声
        boolean inBackground = MetYouActivityManager.getInstance().isAppInBackground();
        Uri uri;
        if(inBackground){
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }else {
            uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.newmessage);
        }
        return uri;
    }

    private boolean isSystemRingOpen(Context context) {
        if (context == null)
            return false;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        return ringerMode == AudioManager.RINGER_MODE_NORMAL;
    }

    private boolean isSystemSilent(Context context) {
        if (context == null)
            return false;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        return ringerMode == AudioManager.RINGER_MODE_SILENT;
    }

    private void sysUnreadMsgs(final Context context, Intent intent) {
        //同步已读
        String addressOrignal = intent.getStringExtra(BusinessGlobalLogic.SMS_ADDRESS_FILE);

        if (ObjectUtils.isNull(addressOrignal)) {
            return;
        }
        if (addressOrignal.startsWith("+86")) {
            addressOrignal = PhoneUtils.getMinMatchNumber(addressOrignal);
        }

        final String address = addressOrignal;

        if (!isInConversation(address)) {
            return;
        }
        final int boxType = intent.getIntExtra(BusinessGlobalLogic.NOTIFICATION_TYEP, Type.TYPE_BOX_MESSAGE);
        new RxAsyncHelper("").runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                if (boxType == Type.TYPE_BOX_PLATFORM) {
                    Message msg = PlatformUtils.getLastMessage(context, address);
                    if (msg != null) {
                        //同步未读数到其他端
                        ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getDate(), msg.getAddress(), CommonConstant.PLATFORMCHATTYPE);
                    }
                    ConversationUtils.updateSeen(context, Type.TYPE_BOX_PLATFORM, address, "");
                } else if (boxType == Type.TYPE_BOX_GROUP) {
                    Message msg = GroupChatUtils.getLastMessage(context, address);
                    if (msg != null) {
                        String identify = msg.getIdentify();
                        if (TextUtils.isEmpty(msg.getIdentify())) {
                            identify = GroupChatUtils.getIdentify(context, msg.getAddress());
                        }
                        //同步未读数到其他端
                        ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getDate(), identify, CommonConstant.GROUPCHATTYPE);
                    }

                    ConversationUtils.updateSeen(context, Type.TYPE_BOX_GROUP, address, "");
                } else if (boxType == Type.TYPE_BOX_MESSAGE) {
                    Message message = MessageUtils.getLastMessage(context, address);
                    if (message != null) {
                        //同步未读数到其他端
                        ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, message.getDate(), address, CommonConstant.SINGLECHATTYPE);
                    }
                    ConversationUtils.updateSeen(context, Type.TYPE_BOX_MESSAGE, address, "");
                } else if (boxType == Type.TYPE_BOX_MAILASSISTANT) {
                    Message msg = MailAssistantUtils.getLastMessage(context, address);
                    if (msg != null) {
                        if (address.contains("@139.com")) {
                            ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getTimestamp(), address, CommonConstant.MAILCHATTYPE);
                        } else {
                            ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getTimestamp(), address + "@139.com", CommonConstant.MAILCHATTYPE);
                        }
                    }
                    MailAssistantUtils.updateSeen(context, address);
                }
                return null;
            }
        }).subscribe();

        //已读同步end
    }

    private String loadFileMsgName(Context context, Conversation conv) {
        String fileName = "";
        if (conv == null || context == null) {
            return fileName;
        }
        if (conv.getType() == Type.TYPE_MSG_FILE_YUN_SEND || conv.getType() == Type.TYPE_MSG_FILE_YUN_RECV) {
            YunFile yunFile = YunFileXmlParser.parserYunFileXml(conv.getBody());
            if (yunFile != null) {
                fileName = yunFile.getFileName();
            }
            return fileName;
        }
        int boxType = conv.getBoxType();
        String where = null;
        Cursor cursor = null;
        if ((boxType & Type.TYPE_BOX_GROUP) > 0) {
            where = BaseModel.COLUMN_NAME_ADDRESS + "=='" + conv.getAddress() + "' AND " + BaseModel.COLUMN_NAME_SEND_ADDRESS + "=='" + NumberUtils.getDialablePhoneWithCountryCode(conv.getSendAddress()) + "' AND type == " + conv.getType();
            cursor = context.getContentResolver().query(Group.CONTENT_URI, new String[]{BaseModel.COLUMN_NAME_EXT_FILE_NAME}
                    , where, null, "date desc");

        } else if ((boxType & Type.TYPE_BOX_MESSAGE) > 0) {
            where = BaseModel.COLUMN_NAME_ADDRESS + "==" + "'" + conv.getAddress() + "'" + " AND type == " + conv.getType();
            cursor = context.getContentResolver().query(Conversations.Message.CONTENT_URI, new String[]{BaseModel.COLUMN_NAME_EXT_FILE_NAME}
                    , where, null, "date desc limit 1");
        }
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    fileName = cursor.getString(0);
                }
            } catch (Exception e) {
                LogF.e(TAG, e.getMessage());
            } finally {
                cursor.close();
            }

        }
        return fileName;
    }
}
