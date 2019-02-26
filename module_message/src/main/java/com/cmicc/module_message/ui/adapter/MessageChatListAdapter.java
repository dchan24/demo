package com.cmicc.module_message.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.BeanUtils.ColumnIndex;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.FontUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.activity.PreviewImageActivity;
import com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter;
import com.cmcc.cmrcs.android.ui.interfaces.AudioListener;
import com.cmicc.module_message.ui.adapter.message.AudioMsgRecvHolder;
import com.cmicc.module_message.ui.adapter.message.AudioMsgSendHolder;
import com.cmicc.module_message.ui.adapter.message.BaseViewHolder;
import com.cmicc.module_message.ui.adapter.message.CardVoucherHolder;
import com.cmicc.module_message.ui.adapter.message.DateActivityMessageHolder;
import com.cmicc.module_message.ui.adapter.message.EnterpriseCardMessageHolder;
import com.cmicc.module_message.ui.adapter.message.EnterpriseShareMessageHolder;
import com.cmicc.module_message.ui.adapter.message.FileMsgHolder;
import com.cmicc.module_message.ui.adapter.message.ImageMsgRecvHolder;
import com.cmicc.module_message.ui.adapter.message.ImageMsgSendHolder;
import com.cmicc.module_message.ui.adapter.message.LocationMsgSendHolder;
import com.cmicc.module_message.ui.adapter.message.MmsMessageHolder;
import com.cmicc.module_message.ui.adapter.message.MsgCashBagReturnHolder;
import com.cmicc.module_message.ui.adapter.message.MultiPicTextHolder;
import com.cmicc.module_message.ui.adapter.message.OAMessageHolder;
import com.cmicc.module_message.ui.adapter.message.RedpaperCompleteHolder;
import com.cmicc.module_message.ui.adapter.message.RedpaperMsgHolder;
import com.cmicc.module_message.ui.adapter.message.SinglePicTextHolder;
import com.cmicc.module_message.ui.adapter.message.StrangerTipHolder;
import com.cmicc.module_message.ui.adapter.message.SysMsgViewHolder;
import com.cmicc.module_message.ui.adapter.message.TextMsgHolder;
import com.cmicc.module_message.ui.adapter.message.VcardRecMsgHolder;
import com.cmicc.module_message.ui.adapter.message.VcardSendMsgHolder;
import com.cmicc.module_message.ui.adapter.message.VideoMsgRecvHolder;
import com.cmicc.module_message.ui.adapter.message.VideoMsgSendHolder;
import com.cmicc.module_message.ui.adapter.message.ViewHolder;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmicc.module_message.ui.presenter.PreviewImagePresenter;
import com.cmcc.cmrcs.android.ui.utils.MessageCursorLoader;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.message.LocationUtil;
import com.cmicc.module_message.utils.RcsAudioPlayer;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.ui.fragment.BaseChatFragment;
import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import rx.functions.Func1;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.MAX_IMG_SIZE_IN_LIST;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT;


/**
 * Created by ksk on 2017/3/22.
 */

public class MessageChatListAdapter extends BaseCustomCursorAdapter<ViewHolder, Message> {
    public static final String TAG = "MessageChatListAdapter";
    private final BaseChatContract.Presenter mPresenter;
    private final Activity mActivity;
    private AlertDialog dialog;
    private Context mContext;
    private int mChatType;

    private RecyclerView mRecyclerView;

    private boolean isEPGroup = false;
    private boolean isPartyGroup = false;

    public boolean isMultiSelectMode = false;

    private static final long ONE_DAY_IN_MILLS = 24 * 60 * 60 * 1000;

    private int leftColorId, rightColorId = 0; //左边，右边背景色

    private int leftTextColor;//左边文字
    private int rightTextColor;//右边文字
    private int nameTextColor;//消息发送人名称 颜色
    private int sysTextBackColor;//群系统提示消息背景色

    private PreviewImageListener mPreviewImagelistener;
    public SelectAtCallback mSelectAtCallback;

    private boolean isGroupChat = false;//是否为群聊

    public void setRawId(int rawId) {
        mRawId = rawId;
    }

    private int mRawId = -1;

    private int maxSize = 0;
    private int minSize = 0;
    private int midSize = 0;
    private int mLastItemPadding = 0;

    public int isPlayingAudio = -1;//正在播放的音频
    public boolean isPlayingAudioMessage = false;
    public String audioMessageID = "";
    private static ArrayList<Integer> actions;

    public AnimationDrawable animationDrawableRecv = null; // 接收语音动画
    public AnimationDrawable animationDrawable = null; // 发送语音动画

    public ImageView mPlayingRecAudio = null;
    public ImageView mPlayBgRec = null;
    public ImageView mPlaySmallRec = null;
    public ImageView mPlayingSendAudio = null;
    public ImageView mPlayBgSend = null;
    public ImageView mPlaySmallSend = null;
    public ProgressBar mAudioPlayProgressBar;

    public boolean isLongClick = false;

    private boolean isPreMsg = false;//是否为历史消息
    private String publicAccountTitle = "";
    private Fragment mAttachFragment;
    private OnCheckChangeListener mOnCheckChangeListener;

    private SparseBooleanArray selectedList = new SparseBooleanArray();


    public MessageChatListAdapter(Activity a, BaseChatContract.Presenter presenter) {
        this(a, presenter, null);
    }

    public MessageChatListAdapter(Activity a, BaseChatContract.Presenter presenter, Fragment attachFragment) {
        super(Message.class);
        mPresenter = presenter;
        mActivity = a;
        mContext = a;
        mAttachFragment = attachFragment;
        int width = AndroidUtil.getScreenWidth(mContext);
        maxSize = width / 2;  //图片最大长宽
        minSize = maxSize * 2 / 5; //图片最小长宽
        midSize = minSize * 2;  //图片中等长宽

        // 21API一下使用这个
        if (mActivity != null && mActivity instanceof MessageDetailActivity) {
            final MessageDetailActivity activity = (MessageDetailActivity) mActivity;
            activity.mSensorEventListener = new SensorEventListener() {
                // 距离传感器类型时 距离发送变化会调用这个方法。
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event == null || event.values == null || event.values.length == 0) {
                        return;
                    }
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("RcsVoiceSetting", Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    LogF.i(TAG, "onSensorChanged isPlayingAudioMessage = " + isPlayingAudioMessage + "  TYPE = " + event.sensor.getType());
                    if ((RcsAudioPlayer.getInstence(mContext).isPlaying() || isPlayingAudioMessage) && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {  // 进入，有一个条件。就是正在播放语音。 Sensor.TYPE_PROXIMITY 距离传感器 isPlayingAudio != -1 &&
                        float[] values = event.values;
                        if (values[0] == 0.0) {// 贴近手机
                            editor.putBoolean("in_call", false); // 同听模式
                            editor.commit();
                            LogF.d(TAG, "贴近手机");
                            RcsAudioPlayer.getInstence(mContext).changeToReceiver(); // 听筒模式
                        } else {// 远离手机
                            editor.putBoolean("in_call", true); // 同听模式
                            editor.commit();
                            LogF.d(TAG, "远离手机");
                            RcsAudioPlayer.getInstence(mContext).changeToSpeaker(); // 外放模式
                        }
                    }
                }

                // 当传感器的精度发生变化时会调用OnAccuracyChanged()方法
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
            if (activity.mSensorManager != null && activity.mSensorEventListener != null && activity.mProximitySensor != null) {
                activity.mSensorManager.registerListener(activity.mSensorEventListener, activity.mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
        RcsAudioPlayer.getInstence(mContext).setmAdapter(this);

        actions = new ArrayList<>();
        actions.add(LogicActions.REVOKE_AUDIO_MESSAGE);// 撤回语音消息
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    public void setPreviewImagelistener(PreviewImageListener previewImagelistener) {
        mPreviewImagelistener = previewImagelistener;
    }

    public void setSelectAtMemberCallback(SelectAtCallback selectAtCallback) {
        mSelectAtCallback = selectAtCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----MessageDetail onCreateViewHolder----");

        Log.d(TAG, "-- onCreateViewHolder --" + viewType);
        if (viewType == BaseCustomCursorAdapter.TYPE_HEAD) {
            View headView = getHeadView(parent);
            if (headView != null) {
                return new ViewHolder(headView, mActivity, MessageChatListAdapter.this, mPresenter);
            }
        }

        if (viewType == BaseCustomCursorAdapter.TYPE_NEW_MSG_RECV_DIVIDE_LINE) {
            View divideLineView = getDivideLineView(parent);
            ((TextView) divideLineView.findViewById(R.id.tv_msg)).setTextColor(nameTextColor);
            (divideLineView.findViewById(R.id.left_line)).setBackgroundColor(nameTextColor);
            (divideLineView.findViewById(R.id.right_line)).setBackgroundColor(nameTextColor);
            return new ViewHolder(divideLineView, mActivity, MessageChatListAdapter.this, mPresenter);
        }

        View view;
        switch (viewType) {
            case Type.TYPE_MSG_SYSTEM:
            case Type.TYPE_MSG_WITHDRAW_REVOKE:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_system_message, parent, false);
                return new SysMsgViewHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_IMG_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_image_recv, parent, false);
                return new ImageMsgRecvHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_VIDEO_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_video_recv, parent, false);
                return new VideoMsgRecvHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_CARD_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_msg_recv_vcard, parent, false);
                return new VcardRecMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_IMG_SEND:
            case Type.TYPE_MSG_IMG_SEND_CCIND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_image_send, parent, false);
                return new ImageMsgSendHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_VIDEO_SEND:
            case Type.TYPE_MSG_VIDEO_SEND_CCIND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_video_send, parent, false);
                return new VideoMsgSendHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_TEXT_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_list_receive, parent, false);
                return new TextMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_TEXT_SEND:
            case Type.TYPE_MSG_TEXT_SEND_CCIND:
            case Type.TYPE_MSG_SMS_SEND:
            case Type.TYPE_MSG_TEXT_QUEUE:
            case Type.TYPE_MSG_TEXT_OUTBOX:
            case Type.TYPE_MSG_TEXT_DRAFT:
            case Type.TYPE_MSG_TEXT_FAIL:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_list_send, parent, false);
                return new TextMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_CARD_SEND:
            case Type.TYPE_MSG_CARD_SEND_CCIND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_msg_send_card, parent, false);
                return new VcardSendMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_AUDIO_SEND:
            case Type.TYPE_MSG_AUDIO_SEND_CCIND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_msg_send_audio, parent, false);
                return new AudioMsgSendHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_AUDIO_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_msg_recv_audio, parent, false);
                return new AudioMsgRecvHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_FILE_YUN_SEND:
            case Type.TYPE_MSG_FILE_SEND:
            case Type.TYPE_MSG_FILE_SEND_CCIND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_send_file, parent, false);
                return new FileMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_FILE_YUN_RECV:
            case Type.TYPE_MSG_FILE_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_receive_file, parent, false);
                return new FileMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_OA_ONE_CARD_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_oa_send, parent, false);
                return new OAMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_OA_ONE_CARD_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_oa_receive, parent, false);
                return new OAMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_date_activity_send, parent, false);
                return new DateActivityMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_DATE_ACTIVITY_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_date_activity_receive, parent, false);
                return new DateActivityMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_ENTERPRISE_SHARE_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_enterprise_share_send, parent, false);
                return new EnterpriseShareMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_ENTERPRISE_SHARE_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_enterprise_share_receive, parent, false);
                return new EnterpriseShareMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_T_CARD_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_enterprise_card_receive, parent, false);
                return new EnterpriseCardMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_T_CARD_SEND:
            case Type.TYPE_MSG_T_CARD_SEND_CCIND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_enterprise_card_send, parent, false);
                return new EnterpriseCardMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_MMS_SEND://彩信发送
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_mms_send, parent, false);
                return new MmsMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_MMS_RECV://彩信接收
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_mms_receive, parent, false);
                return new MmsMessageHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_SINGLE_PIC_TEXT_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.msg_item_recv_singlepicc_text, parent, false);
                return new SinglePicTextHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_MULIT_PIC_TEXT_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.msg_item_recv_mulpic_text, parent, false);
                return new MultiPicTextHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_BAG_SEND:
            case Type.TYPE_MSG_CASH_BAG_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_redpaper_send, parent, false);
                return new RedpaperMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_BAG_RECV:
            case Type.TYPE_MSG_CASH_BAG_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_redpaper_recv, parent, false);
                return new RedpaperMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_BAG_RECV_COMPLETE:
            case Type.TYPE_MSG_BAG_SEND_COMPLETE:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_redpaper_complete, parent, false);
                return new RedpaperCompleteHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_CASH_BAG_RETURN:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_cash_bag_return, parent, false);
                return new MsgCashBagReturnHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_SMS_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_list_receive, parent, false);
                return new TextMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_TEXT_SUPER_SMS_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_list_send, parent, false);
                return new TextMsgHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_LOC_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_lloc_recv, parent, false);
                return new LocationMsgSendHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_LOC_SEND:
            case Type.TYPE_MSG_LOC_SEND_CCIND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_lloc_send, parent, false);
                return new LocationMsgSendHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_CARD_VOUCHER_SEND:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_card_voucher_send, parent, false);
                return new CardVoucherHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_CARD_VOUCHER_RECV:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_card_voucher_recv, parent, false);
                return new CardVoucherHolder(view, mActivity, this, mPresenter);
            case Type.TYPE_MSG_STRANGERPTIP:
                 view = LayoutInflater.from(mContext).inflate(R.layout.stranger_save_contact_tip_layout, parent, false);
                 return new StrangerTipHolder(view, mActivity, this, mPresenter);
            default:
                Log.e(TAG, "--onCreateViewHolder 错误消息类型--" + viewType);
                view = LayoutInflater.from(mContext).inflate(R.layout.item_message_list_receive, parent, false);
                return new TextMsgHolder(view, mActivity, this, mPresenter);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----MessageDetail onBindViewHolder----");

        int currentPosition = canLoadMore() ? position - 1 : position;//当前Message 在dataList里面的index
        if(currentPosition <0){//规避数组越界
            return;
        }

        if (getItemViewType(position) == BaseCustomCursorAdapter.TYPE_HEAD){
            return;
        }

        if (getItemViewType(position) == BaseCustomCursorAdapter.TYPE_NEW_MSG_RECV_DIVIDE_LINE) {
            return;
        }
        int type = getItemViewType(position);

        Message msg = getItem(currentPosition);
        Message msgBefore = (currentPosition -1>=0)?getItem(currentPosition):null;

        boolean isSelected = selectedList.get(position);
        LogF.i(TAG, "msg: " + msg);
        long date = msg.getDate();
        int status = msg.getStatus();
//        LogF.i(TAG + "ksbk", "onBindViewHolder: position" + position + "  date: " + date);

        //将一些参数与 ViewHolder 绑定
        holder.setMessage(msg);
        holder.setColors(new int[]{
                leftColorId,
                rightColorId,
                leftTextColor,
                rightTextColor,
                nameTextColor,
                sysTextBackColor,
        });
        holder.setChatArgs(isGroupChat, isEPGroup, isPartyGroup, mChatType);

        bindMargin(holder, msg, position);

        float textSize = 16 * FontUtil.getFontScale();
        switch (type) {
            case Type.TYPE_MSG_SYSTEM:
            case Type.TYPE_MSG_WITHDRAW_REVOKE:
                SysMsgViewHolder sysMsgViewHolder = (SysMsgViewHolder) holder;
                sysMsgViewHolder.bindText();
                sysMsgViewHolder.bindTime(msgBefore, position);
                sysMsgViewHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                if(isMultiSelectMode && isSelected){//解决多选的时候，撤回消息问题
                    removeSelection(position , false);
                }
                break;
            case Type.TYPE_MSG_SMS_RECV:
            case Type.TYPE_MSG_TEXT_RECV:
                LogF.i(TAG, "msg:" + msg.toString());
                TextMsgHolder textMsgRecvHolder = (TextMsgHolder) holder;

                textMsgRecvHolder.bindTextRecv();
                textMsgRecvHolder.bindHead(msgBefore);
                textMsgRecvHolder.bindTime( msgBefore, position);
                textMsgRecvHolder.bindBubble();
                textMsgRecvHolder.bindName(msgBefore);
                textMsgRecvHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_CASH_BAG_RETURN:
                LogF.i(TAG, "msg:" + msg.toString());
                MsgCashBagReturnHolder msgCashBagReturnHolder = (MsgCashBagReturnHolder) holder;
                msgCashBagReturnHolder.bindTextRecv();
                msgCashBagReturnHolder.bindTime( msgBefore, position);
                break;
            case Type.TYPE_MSG_TEXT_QUEUE:
            case Type.TYPE_MSG_TEXT_OUTBOX:
            case Type.TYPE_MSG_TEXT_DRAFT:
            case Type.TYPE_MSG_TEXT_FAIL:
            case Type.TYPE_MSG_TEXT_SEND:
            case Type.TYPE_MSG_TEXT_SEND_CCIND:
            case Type.TYPE_MSG_SMS_SEND:
            case Type.TYPE_MSG_TEXT_SUPER_SMS_SEND:
                TextMsgHolder textMsgHolder = (TextMsgHolder) holder;

                textMsgHolder.bindTextSend();
                textMsgHolder.bindHead(msgBefore);
                textMsgHolder.bindTime( msgBefore, position);
                textMsgHolder.bindBubble();
                textMsgHolder.bindName(msgBefore);
                textMsgHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                textMsgHolder.bindSendStatus();
                break;
            case Type.TYPE_MSG_IMG_RECV:
                ImageMsgRecvHolder imageMsgRecvHolder = (ImageMsgRecvHolder) holder;
                imageMsgRecvHolder.bindHead(msgBefore);
                imageMsgRecvHolder.bindTime(msgBefore, position);
                imageMsgRecvHolder.bindName(msgBefore);
                imageMsgRecvHolder.bindImage(mChatType, maxSize, minSize, midSize);
                imageMsgRecvHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_IMG_SEND:
            case Type.TYPE_MSG_IMG_SEND_CCIND:
                ImageMsgSendHolder imageMsgHolder = (ImageMsgSendHolder) holder;
                imageMsgHolder.bindHead(msgBefore);
                imageMsgHolder.bindTime(msgBefore, position);
                imageMsgHolder.bindName(msgBefore);
                imageMsgHolder.bindImage(maxSize, minSize, midSize);
                imageMsgHolder.bindSendStatus();
                imageMsgHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_VIDEO_RECV:
                VideoMsgRecvHolder videoMsgRecvHolder = (VideoMsgRecvHolder) holder;
                videoMsgRecvHolder.bindHead(msgBefore);
                videoMsgRecvHolder.bindTime(msgBefore, position);
                videoMsgRecvHolder.bindName(msgBefore);
                videoMsgRecvHolder.bindDuration();
                videoMsgRecvHolder.bindDownloadStatus();
                videoMsgRecvHolder.bindThumb(msg, maxSize, minSize);
                videoMsgRecvHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_VIDEO_SEND:
            case Type.TYPE_MSG_VIDEO_SEND_CCIND:
                VideoMsgSendHolder videoMsgSendHolder = (VideoMsgSendHolder) holder;
                videoMsgSendHolder.bindHead(msgBefore);
                videoMsgSendHolder.bindTime(msgBefore, position);
                videoMsgSendHolder.bindName(msgBefore);
                videoMsgSendHolder.bindDownloadStatus();
                videoMsgSendHolder.bindDuration(msg);
                videoMsgSendHolder.bindThumb(msg, maxSize, minSize);
                //此处有坑， bindThumb必须放在后面，因为 bindThumb执行了部分bindDownloadStatus的功能，待优化
                videoMsgSendHolder.bindSendStatus();
                videoMsgSendHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_CARD_SEND:
            case Type.TYPE_MSG_CARD_SEND_CCIND:
                VcardSendMsgHolder vcardMsgSendHolder = (VcardSendMsgHolder) holder;
                vcardMsgSendHolder.bindTime(msgBefore, position);
                vcardMsgSendHolder.bindName(msgBefore);
                vcardMsgSendHolder.bindCard( msg.getBody());
                vcardMsgSendHolder.bindHead(msgBefore);
                vcardMsgSendHolder.bindBubble();
                vcardMsgSendHolder.bindSendStatus();
                vcardMsgSendHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_CARD_RECV:
                VcardRecMsgHolder vcardMsgRecvHolder = (VcardRecMsgHolder) holder;
                vcardMsgRecvHolder.bindTime(msgBefore, position);
                vcardMsgRecvHolder.bindName(msgBefore);
                vcardMsgRecvHolder.bindCard(msg.getBody());
                vcardMsgRecvHolder.bindHead(msgBefore);
                vcardMsgRecvHolder.bindBubble();
                vcardMsgRecvHolder.bindDownloadStatus();
                vcardMsgRecvHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_FILE_YUN_SEND:
            case Type.TYPE_MSG_FILE_YUN_RECV:
                ((FileMsgHolder) holder).onBindViewYunFile(msgBefore, position);
                ((FileMsgHolder) holder).bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_FILE_SEND:
            case Type.TYPE_MSG_FILE_SEND_CCIND:
            case Type.TYPE_MSG_FILE_RECV:
                FileMsgHolder fileMsgRecvHolder = (FileMsgHolder) holder;
                fileMsgRecvHolder.bindHead(msgBefore);
                fileMsgRecvHolder.bindName(msgBefore);
                fileMsgRecvHolder.bindSendStatus();
                fileMsgRecvHolder.bindFile();
                fileMsgRecvHolder.bindBubble();
                fileMsgRecvHolder.bindTime(msgBefore, position);
                fileMsgRecvHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_AUDIO_SEND:
            case Type.TYPE_MSG_AUDIO_SEND_CCIND:
                AudioMsgSendHolder audioMsgSendHolder = (AudioMsgSendHolder) holder;
                audioMsgSendHolder.bindSendStatus();
                audioMsgSendHolder.bindTime(msgBefore, position);
                audioMsgSendHolder.bindName(msgBefore);
                audioMsgSendHolder.bindHead(msgBefore);
                audioMsgSendHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);

                if(msg != null && !TextUtils.isEmpty(msg.getBody())){ // 有文字
                    audioMsgSendHolder.layout_Audio_content.setVisibility(View.GONE);
                    audioMsgSendHolder.audoi_and_text_messag.setVisibility(View.VISIBLE); // 文字内容
                    audioMsgSendHolder.audio_progressbar_rl.setVisibility(View.VISIBLE); // 文字内容
                    audioMsgSendHolder.audioTime.setVisibility(View.GONE);
                    audioMsgSendHolder.audioAndTv_message.setTextSize(textSize);
                    audioMsgSendHolder.bindAudioAndTextBubble(status, type, msg);
                } else {
                    audioMsgSendHolder.layout_Audio_content.setVisibility(View.VISIBLE);
                    audioMsgSendHolder.audoi_and_text_messag.setVisibility(View.GONE);
                    audioMsgSendHolder.audio_progressbar_rl.setVisibility(View.GONE); // 文字内容
                    audioMsgSendHolder.audioTime.setVisibility(View.VISIBLE);
                    audioMsgSendHolder.bindBubble();
                    audioMsgSendHolder.bindAudioTime();
                }

                int sendid = (int) msg.getId();
                // 正在播放语音
                if (isPlayingAudio != -1) {
                    if (isPlayingAudio != sendid) {
                        audioMsgSendHolder.image_audio.setImageResource(R.drawable.message_voice_playing_send_f6);
                        audioMsgSendHolder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
                        audioMsgSendHolder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
                    } else {
                        if (animationDrawable != null) {
                            animationDrawable = (AnimationDrawable) mContext.getResources().getDrawable(R.drawable.audio_send_animation);
                            audioMsgSendHolder.image_audio.setImageDrawable(animationDrawable);
                            animationDrawable.start();
                            audioMsgSendHolder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_play_big2);
                            audioMsgSendHolder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_play_small2);
                        }
                    }
                } else {
                    audioMsgSendHolder.image_audio.setImageResource(R.drawable.message_voice_playing_send_f6);
                    audioMsgSendHolder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
                    audioMsgSendHolder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
                }
                break;
            case Type.TYPE_MSG_AUDIO_RECV:
                final AudioMsgRecvHolder audioMsgRecvHolder = (AudioMsgRecvHolder) holder;
                audioMsgRecvHolder.bindTime(msgBefore, position);
                audioMsgRecvHolder.bindName(msgBefore);
                audioMsgRecvHolder.bindHead(msgBefore);
                audioMsgRecvHolder.bindDownloadStatus();
                audioMsgRecvHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);

                if(msg != null && !TextUtils.isEmpty(msg.getBody())){
                    audioMsgRecvHolder.audio_ll.setVisibility(View.GONE);
                    //audioMsgRecvHolder.layout_Audio_content.setVisibility(View.GONE);
                    audioMsgRecvHolder.audoi_and_text_messag.setVisibility(View.VISIBLE); // 文字内容
                    audioMsgRecvHolder.audio_progressbar_rl.setVisibility(View.VISIBLE); // 文字内容
                    //audioMsgRecvHolder.audioTime.setVisibility(View.GONE);
                    //audioMsgRecvHolder.image_audio_unread.setVisibility(View.GONE);
                    audioMsgRecvHolder.audioAndTv_message.setTextSize(textSize);
                    audioMsgRecvHolder.bindAudioAndTextBubble(status, type, msg);
                } else {
                    audioMsgRecvHolder.bindAudioTime();
                    audioMsgRecvHolder.bindBubble();
                    audioMsgRecvHolder.audio_ll.setVisibility(View.VISIBLE);
                    //audioMsgRecvHolder.layout_Audio_content.setVisibility(View.VISIBLE);
                    audioMsgRecvHolder.audoi_and_text_messag.setVisibility(View.GONE);
                    audioMsgRecvHolder.audio_progressbar_rl.setVisibility(View.GONE); // 文字内容
                    if (msg.isRead()) {
                        audioMsgRecvHolder.image_audio_unread.setVisibility(View.GONE);
                    } else {
                        audioMsgRecvHolder.image_audio_unread.setVisibility(View.VISIBLE);
                    }
                    //audioMsgRecvHolder.audioTime.setVisibility(View.VISIBLE);

                }
                int recid = (int) msg.getId();
                // 正在播放语音
                if (isPlayingAudio != -1) {
                    if (isPlayingAudio != recid) {
                        audioMsgRecvHolder.image_audio.setImageResource(R.drawable.message_voice_playing_f6);
                        audioMsgRecvHolder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_stop_big);
                        audioMsgRecvHolder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_stop_small);
                    } else {
                        if (animationDrawableRecv != null) {
                            animationDrawableRecv = (AnimationDrawable) mContext.getResources().getDrawable(R.drawable.audio_animation);
                            audioMsgRecvHolder.image_audio.setImageDrawable(animationDrawableRecv);
                            animationDrawableRecv.start();
                            audioMsgRecvHolder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_play_big);
                            audioMsgRecvHolder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_play_small);
                        }
                    }
                } else {
                    audioMsgRecvHolder.image_audio.setImageResource(R.drawable.message_voice_playing_f6);
                    audioMsgRecvHolder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_stop_big);
                    audioMsgRecvHolder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_stop_small);
                }
                break;
            case Type.TYPE_MSG_SINGLE_PIC_TEXT_RECV:
                SinglePicTextHolder singlePicTextHolder = (SinglePicTextHolder) holder;
                LogF.d(TAG + "ksbk", "onBindViewHolder: date:" + msg.getDate() + "  subtitle : " + msg.getSubTitle() + "   position : " + position);
                singlePicTextHolder.bindTime(msgBefore, position);
                singlePicTextHolder.bindSinglePicText(msg);
                break;
            case Type.TYPE_MSG_MULIT_PIC_TEXT_RECV:
                MultiPicTextHolder multiPicTextHolder = (MultiPicTextHolder) holder;
                LogF.d(TAG + "ksbk", "onBindViewHolder: date:" + msg.getDate() + "  subtitle : " + msg.getSubTitle() + "   position : " + position);
                multiPicTextHolder.bindTime(msgBefore, position);
                multiPicTextHolder.bindMultiPicText(msg);
                break;

            case Type.TYPE_MSG_OA_ONE_CARD_SEND:
            case Type.TYPE_MSG_OA_ONE_CARD_RECV:
                OAMessageHolder oaMsgRecvHolder = (OAMessageHolder) holder;

                oaMsgRecvHolder.bindHead(msgBefore);
                oaMsgRecvHolder.bindTime(msgBefore, position);
                oaMsgRecvHolder.bindName(msgBefore);
                oaMsgRecvHolder.bindBubble();
                oaMsgRecvHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                oaMsgRecvHolder.bindText();
                oaMsgRecvHolder.bindSendStatus();
                break;

            case Type.TYPE_MSG_CARD_VOUCHER_SEND:
            case Type.TYPE_MSG_CARD_VOUCHER_RECV:
                CardVoucherHolder cardVoucherHolder = (CardVoucherHolder)holder;
                cardVoucherHolder.bindHead(msgBefore);
                cardVoucherHolder.bindTime(msgBefore, position);
                cardVoucherHolder.bindName(msgBefore);
                cardVoucherHolder.bindContent();
                cardVoucherHolder.bindSendStatus();
                cardVoucherHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;

            case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
            case Type.TYPE_MSG_DATE_ACTIVITY_RECV:
                DateActivityMessageHolder dateActivityMessageHolder = (DateActivityMessageHolder) holder;

                dateActivityMessageHolder.bindHead(msgBefore);
                dateActivityMessageHolder.bindTime(msgBefore, position);
                dateActivityMessageHolder.bindName( msgBefore);
                dateActivityMessageHolder.bindBubble();
                dateActivityMessageHolder.bindText();
                dateActivityMessageHolder.bindSendStatus();
                dateActivityMessageHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);

                break;
            case Type.TYPE_MSG_T_CARD_SEND:
            case Type.TYPE_MSG_T_CARD_RECV:
            case Type.TYPE_MSG_T_CARD_SEND_CCIND:
                EnterpriseCardMessageHolder enterpriseCardMessageHolder = (EnterpriseCardMessageHolder) holder;
                enterpriseCardMessageHolder.bindHead(msgBefore);
                enterpriseCardMessageHolder.bindTime(msgBefore, position);
                enterpriseCardMessageHolder.bindName(msgBefore);
                enterpriseCardMessageHolder.bindBubble();
                enterpriseCardMessageHolder.bindText();
                enterpriseCardMessageHolder.bindSendStatus();
                enterpriseCardMessageHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;

            case Type.TYPE_MSG_ENTERPRISE_SHARE_SEND:
            case Type.TYPE_MSG_ENTERPRISE_SHARE_RECV: {
                EnterpriseShareMessageHolder enterpriseShareMessageHolder = (EnterpriseShareMessageHolder) holder;

                enterpriseShareMessageHolder.bindHead(msgBefore);
                enterpriseShareMessageHolder.bindTime(msgBefore, position);
                enterpriseShareMessageHolder.bindName(msgBefore);
                enterpriseShareMessageHolder.bindBubble();
                enterpriseShareMessageHolder.bindText();
                enterpriseShareMessageHolder.bindSendStatus();
                enterpriseShareMessageHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
            }
            break;
            case Type.TYPE_MSG_MMS_RECV:
            case Type.TYPE_MSG_MMS_SEND:
                MmsMessageHolder mmsMessageHolder = (MmsMessageHolder) holder;

                mmsMessageHolder.bindText();
                mmsMessageHolder.bindHead(msgBefore);
                mmsMessageHolder.bindTime(msgBefore, position);
                mmsMessageHolder.bindName(msgBefore);
                mmsMessageHolder.bindBubble();
                mmsMessageHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_BAG_SEND:
            case Type.TYPE_MSG_CASH_BAG_SEND:
            case Type.TYPE_MSG_BAG_RECV:
            case Type.TYPE_MSG_CASH_BAG_RECV:
                RedpaperMsgHolder redMsgHolder = (RedpaperMsgHolder) holder;
                redMsgHolder.bindHead(msgBefore);
                redMsgHolder.bindTime(msgBefore, position);
                redMsgHolder.bindName(msgBefore);
                redMsgHolder.bindContent();
                redMsgHolder.bindSendStatus();
                redMsgHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);
                break;
            case Type.TYPE_MSG_BAG_RECV_COMPLETE:
            case Type.TYPE_MSG_BAG_SEND_COMPLETE:
                RedpaperCompleteHolder redpaperCompleteHolder = (RedpaperCompleteHolder) holder;
                redpaperCompleteHolder.setText(msg.getBody());
                redpaperCompleteHolder.bindTime(msgBefore, position);
                break;
            case Type.TYPE_MSG_LOC_RECV:
            case Type.TYPE_MSG_LOC_SEND:
            case Type.TYPE_MSG_LOC_SEND_CCIND:
                LocationMsgSendHolder llocMsgSendHolder = (LocationMsgSendHolder) holder;
                llocMsgSendHolder.bindHead(msgBefore);
                llocMsgSendHolder.bindTime(msgBefore, position);
                llocMsgSendHolder.bindName(msgBefore);
                llocMsgSendHolder.bindMultiSelectStatus(isMultiSelectMode,isSelected);

                try {
                    String loc_body = LocationUtil.parseFreeText(msg.getBody());
                    String loc_title = LocationUtil.parseTitle(msg.getBody());
                    llocMsgSendHolder.setAddress(loc_body);
                    llocMsgSendHolder.setSpecialAddress(loc_title);
                    double longitude = Double.valueOf(LocationUtil.parseLongitude(msg.getBody()));
                    double latitude = Double.valueOf(LocationUtil.parseLatitude(msg.getBody()));
                    llocMsgSendHolder.setLatitude(latitude);
                    llocMsgSendHolder.setLongitude(longitude);
                    if (!TextUtils.isEmpty(loc_title)) {
                        llocMsgSendHolder.mFamousAddress.setText(loc_title);
                    } else {
                        llocMsgSendHolder.mFamousAddress.setText("");
                    }
                } catch (Exception e) {
                    LogF.e(TAG, "parse location msg body exception.e = " + e);
                }
                llocMsgSendHolder.bindSendStatus();
                break;
            case Type.TYPE_MSG_STRANGERPTIP:
                StrangerTipHolder strangerTipHolder = (StrangerTipHolder)holder;
                strangerTipHolder.bindData(msg);
                LogF.i(TAG, "TYPE_MSG_STRANGERPTIP");
                break;
            default:
                LogF.e(TAG, "--onBindViewHolder 错误消息类型--" + type);
                String msgType = mContext.getString(R.string.unkwown_type) + type;
                switch (type) {
                    case 33:
                    case 37:
                        msgType = mContext.getString(R.string.voice);
                        break;
                    case 49:
                    case 53:
                        msgType = mContext.getString(R.string.video);
                        break;
                    case 113:
                    case 117:
                        msgType = mContext.getString(R.string.business_card);
                        break;
                    case 65:
                    case 69:
                        msgType = mContext.getString(R.string.file);
                        break;
                    case 97:
                        msgType = mContext.getString(R.string.shop_emoji);
                        break;
                    case 161:
                        msgType = mContext.getString(R.string.redpacket_);
                        break;
                }

                ((TextMsgHolder) holder).sTvMessage.setText(mContext.getString(R.string.send) + "[" + msgType + "]，" + mContext.getString(R.string.can_suport_see_it_));
                ((TextMsgHolder) holder).sTvMessage.setTextSize(textSize);
                ((TextMsgHolder) holder).bindHead(msgBefore);
                holder.bindTime(msgBefore, position);
                ((TextMsgHolder) holder).bindBubble();
                ((TextMsgHolder) holder).bindName(msgBefore);
                ((TextMsgHolder) holder).bindMultiSelectStatus(isMultiSelectMode,isSelected);

        }

    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        if (position == NO_POSITION) {
            return;
        }
        if (getItemViewType(position) == BaseCustomCursorAdapter.TYPE_HEAD) return;
        if (getItemViewType(position) == BaseCustomCursorAdapter.TYPE_NEW_MSG_RECV_DIVIDE_LINE)
            return;

        final Message msg = getItem(canLoadMore() ? position - 1 : position);
        Log.i(TAG, "onViewAttachedToWindow: " + msg.getBody());

        if (msg.getType() == Type.TYPE_MSG_IMG_RECV || msg.getType() == Type.TYPE_MSG_IMG_SEND_CCIND) {
            if (holder.sCDT != null) {
                holder.sCDT.cancel();
                holder.sCDT = null;
            }

            holder.sCDT = new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    LogF.e(TAG, "CountDownTimer onTick : " + msg.getExtFilePath());
                }

                @Override
                public void onFinish() {
                    LogF.e(TAG, "CountDownTimer onFinish : " + msg.getExtFilePath());

                    long fileSize = msg.getExtFileSize();
                    String extFilePath = msg.getExtFilePath();
                    boolean isFileExist = false;
                    boolean isGift = false;
                    boolean isBeyondTime = false;
                    if (extFilePath != null && !extFilePath.isEmpty()) {
                        if (extFilePath.toLowerCase().endsWith(".gif")) {
                            isGift = true;
                        }
                        File file = new File(extFilePath);// 修改文件名字
                        if (file != null) {
                            isFileExist = file.exists();
                        }
                    }

                    isBeyondTime = System.currentTimeMillis() - msg.getDate() > ONE_DAY_IN_MILLS;

                    if (!isBeyondTime && !isFileExist && !isGift && fileSize < MAX_IMG_SIZE_IN_LIST && msg.getStatus() == Status.STATUS_OK) {
                        LogF.e(TAG, "CountDownTimer onFinish 自动下载确认: " + msg.getExtFilePath());
                        if (mChatType == TYPE_GROUP_CHAT) {
                            LogF.i("tigger", "-------------rcsImFileFetchViaMsrpX time：" + System.currentTimeMillis());
                            ComposeMessageActivityControl.rcsImFileFetchViaMsrpX((int) msg.getId(), msg.getAddress(), msg.getExtShortUrl(), extFilePath);
                        } else if (mChatType == TYPE_SINGLE_CHAT || mChatType == TYPE_PC_CHAT) {
                            ComposeMessageActivityControl.rcsImFileFetchViaMsrp((int) msg.getId(), extFilePath, msg.getExtShortUrl(), extFilePath);
                        }
                    }
                }
            };
            holder.sCDT.start();
        }

        if (isPartyGroup || isEPGroup) {
            boolean isLeft = (msg.getType() & Type.TYPE_RECV) > 0;
            if (isLeft && msg.getType() != Type.TYPE_MSG_SMS_RECV && msg.getType() != Type.TYPE_MSG_MMS_RECV) {
                if (msg.getExactRead() != 1 && msg.getExdSendStuaus() == -1) {
                    msg.setExactRead(1);
                    msg.setExdSendStuaus(Status.STATUS_LOADING);
                    new RxAsyncHelper("").runInThread(new Func1() {
                        @Override
                        public Object call(Object o) {
                            GroupChatControl.rcsImSendDispG((int) msg.getId(), msg.getMsgId(), msg.getIdentify(), "tel:" + msg.getSendAddress(), msg.getAddress());
                            return null;
                        }
                    }).subscribe();
                }
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.sCDT != null) {
            holder.sCDT.cancel();
        }
    }

    public void setPublicAccountTitle(String publicAccountTitle) {
        this.publicAccountTitle = publicAccountTitle;
    }

    public String getPublicAccountTitle() {
        return publicAccountTitle;
    }

    public void audioSendContentClick(Message msg, AudioMsgSendHolder holder) {  // 点击发送的语音消息，正在播放就停止
        if (mAttachFragment instanceof BaseChatFragment) {
            if (((BaseChatFragment) mAttachFragment).isReocrding())
                return;
        }
        String pathSend = msg.getExtFilePath();
        int sendid = (int) msg.getId();
        LogF.i(TAG, "audioSendContentClick sendid = " + sendid + "  isPlayingAudio = " + isPlayingAudio);
        if (isPlayingAudio == sendid) { // 点击的是正在播放的那条语音信息
            if (animationDrawable != null) {
                Log.i(TAG, "audioSendContentClick: animationDrawable.stop()");
                animationDrawable.stop();// 停止动画
            }
            holder.image_audio.setImageResource(R.drawable.message_voice_playing_send_f6);
            holder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
            holder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
            stopAudio(); // 停止语音播放
            holder.audioPlayProgressBar.setVisibility(View.GONE);// 隐藏进度
            holder.audioPlayProgressBar.setProgress(0);// 从头播放
            isPlayingAudio = -1;  // 复位
            audioMessageID = "";
            isPlayingAudioMessage = false;
            mPlayingSendAudio = null; // 波浪图标
            mPlayBgSend = null;      // 播放大图标
            mPlaySmallSend = null;   // 播放小图标
            mAudioPlayProgressBar = null; // 播放进度条
            // 2018.5.5 YSF 靠近耳边熄屏
            if (mActivity instanceof MessageDetailActivity) {
                MessageDetailActivity activity = (MessageDetailActivity) mActivity;
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (activity.mWakeLock != null && activity.mWakeLock.isHeld()) {
                    activity.mWakeLock.release();
                }
            }
            LogF.i(TAG, "audioSendContentClick 1 isPlayingAudioMessage = " + isPlayingAudioMessage);
        } else {
            if (animationDrawable != null) {
                animationDrawable.stop();// 停止动画
            }
            if (mPlayingRecAudio != null) { // 接收方图片复位
                mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
            }
            if (mPlayBgRec != null) {
                mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
            }
            if (mPlaySmallRec != null) {
                mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
            }
            if (mPlayingSendAudio != null) { // 发送图片复位
                mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
            }
            if (mPlayBgSend != null) {
                mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
            }
            if (mPlaySmallSend != null) {
                mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
            }
            if (mAudioPlayProgressBar != null) {
                mAudioPlayProgressBar.setVisibility(View.GONE);
                mAudioPlayProgressBar.setProgress(0);
            }
            animationDrawable = (AnimationDrawable) mContext.getResources().getDrawable(R.drawable.audio_send_animation);
            holder.image_audio.setImageDrawable(animationDrawable);
            animationDrawable.start();// 开启动画
            holder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_play_big2);
            holder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_play_small2);
            if (!TextUtils.isEmpty(holder.audioAndTv_message.getText().toString())) {
                int width = holder.audioAndTv_message.getWidth(); // 获取文字空间的宽度
                int paddingRight = holder.audioAndTv_message.getPaddingRight();
                int paddingLeft = holder.audioAndTv_message.getPaddingLeft();
                RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams) holder.audioPlayProgressBar.getLayoutParams();
                rp.width = width - paddingLeft - paddingRight - 15;
                holder.audioPlayProgressBar.setLayoutParams(rp);
                int time = getAudioMessageTime(msg);
                LogF.i(TAG, "width = " + width + "  time = " + time + "  paddingLeft = " + paddingLeft + "  paddingRight = " + paddingRight);
                holder.audioPlayProgressBar.setProgress(0);   // 当前进度
                holder.audioPlayProgressBar.setVisibility(View.VISIBLE);
            }
            mPlayingSendAudio = holder.image_audio;
            mPlayBgSend = holder.img_play_icon;
            mPlaySmallSend = holder.img_audio_play_small_icon;
            mAudioPlayProgressBar = holder.audioPlayProgressBar;
            isPlayingAudio = sendid;
            audioMessageID = msg.getMsgId();// 消息ID
            isPlayingAudioMessage = true;
            LogF.i(TAG, "audioSendContentClick 2 isPlayingAudioMessage = " + isPlayingAudioMessage);
            playAudio(pathSend, new AudioListener1(this)); // 开始播放语音
            LogF.i(TAG, "audioSendContentClick 3 isPlayingAudioMessage = " + isPlayingAudioMessage);
        }
    }

    private int getAudioMessageTime(Message msg) {
        String duration = msg.getExtSizeDescript();
        int tm = 0;// duration
        if (StringUtil.isEmpty(duration)) {
            tm = (int) FileUtil.getDuring(msg.getExtFilePath()) / 1000;
            if (tm == 61) {
                tm = 60;
            }
            duration = tm + "";
        }
        if (tm <= 0) try {
            tm = Integer.parseInt(duration);
            if (tm == 61) {
                tm = 60;
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return tm;
    }

    private static class AudioListener1 implements AudioListener {
        SoftReference<MessageChatListAdapter> softReference;

        public AudioListener1(MessageChatListAdapter adapter) {
            softReference = new SoftReference<>(adapter);
        }

        @Override
        public void AudioComplete() { // 播放自己发的语音
            MessageChatListAdapter adapter = softReference.get();
            RcsAudioPlayer.getInstence(MyApplication.getApplication()).changeToSpeaker();
            RcsAudioPlayer.getInstence(MyApplication.getApplication()).abandonAudioFocus();// 释放音频焦点
            if (adapter != null) {
                // 2018.5.5 YSF 靠近耳边熄屏
                if (adapter.mActivity instanceof MessageDetailActivity) {
                    MessageDetailActivity activity = (MessageDetailActivity) adapter.mActivity;
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if (activity.mWakeLock != null && activity.mWakeLock.isHeld()) {
                        activity.mWakeLock.release();
                    }
                }
                adapter.animationDrawable.stop();// 停止动画
                if (adapter.mAudioPlayProgressBar != null) {
                    adapter.mAudioPlayProgressBar.setProgress(0);// 从头开始
                    adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
                    adapter.mAudioPlayProgressBar = null;
                }
                if (adapter.mPlayingSendAudio != null) { // 发送图片复位
                    adapter.mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
                }
                if (adapter.mPlayBgSend != null) {
                    adapter.mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
                    adapter.mPlayBgSend = null;
                }
                if (adapter.mPlaySmallSend != null) {
                    adapter.mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
                    adapter.mPlaySmallSend = null;
                }
                adapter.isPlayingAudio = -1;
                adapter.audioMessageID = "";
                adapter.isPlayingAudioMessage = false; // 播放完自己发的某条语音 ，没有播放语音
                adapter.mPlayingSendAudio = null;
                adapter.notifyDataSetChanged();
                if ((adapter.mContext != null)) {
                    SharedPreferences sharedPreferences = adapter.mContext.getSharedPreferences("RcsVoiceSetting", Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("in_call", true); // 后面没有语音了，听完之后要切回外放模式
                    editor.commit();
                } else {
                    SharedPreferences sharedPreferences = App.getAppContext().getSharedPreferences("RcsVoiceSetting", Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("in_call", true); // 后面没有语音了，听完之后要切回外放模式
                    editor.commit();
                }
                LogF.i(TAG, "AudioListener1 AudioComplete isPlayingAudioMessage = " + adapter.isPlayingAudioMessage);
            }
        }
    }

    private static class AudioListeneR implements AudioListener {
        SoftReference<MessageChatListAdapter> softReference;
        int position;
        AudioMsgRecvHolder holder;

        public AudioListeneR(MessageChatListAdapter adapter, int position, AudioMsgRecvHolder holder) {
            softReference = new SoftReference<>(adapter);
            this.position = position;
            this.holder = holder;
        }

        @Override
        public void AudioComplete() { // 接收的语音播放完毕
            RcsAudioPlayer.getInstence(MyApplication.getApplication()).changeToSpeaker();
            MessageChatListAdapter adapter = softReference.get();
            if (adapter != null) {
                // 2018.5.5 YSF 靠近耳边熄屏
                if (adapter.mActivity instanceof MessageDetailActivity) {
                    MessageDetailActivity activity = (MessageDetailActivity) adapter.mActivity;
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if (activity.mWakeLock != null && activity.mWakeLock.isHeld()) {
                        activity.mWakeLock.release();
                    }
                }
                if (holder != null) {
                    if (holder.image_audio != null) {
                        holder.image_audio.setImageResource(R.drawable.message_voice_playing_f6);
                    }
                    if (holder.img_play_icon != null) {
                        holder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_stop_big);
                    }
                    if (holder.img_audio_play_small_icon != null) {
                        holder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_stop_small);
                    }
                    if (holder.audioPlayProgressBar != null) {
                        holder.audioPlayProgressBar.setProgress(0);
                        holder.audioPlayProgressBar.setVisibility(View.GONE);
                    }
                }
                if (adapter != null) {
                    if (adapter.mAudioPlayProgressBar != null) {
                        adapter.mAudioPlayProgressBar.setProgress(0);// 从头开始
                        adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
                        adapter.mAudioPlayProgressBar = null;
                    }
                    if (adapter.mPlayingRecAudio != null) { // 发送图片复位
                        adapter.mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
                        adapter.mPlayingRecAudio = null;
                    }
                    if (adapter.mPlayBgRec != null) {
                        adapter.mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
                        adapter.mPlayBgRec = null;
                    }
                    if (adapter.mPlaySmallRec != null) {
                        adapter.mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
                        adapter.mPlaySmallRec = null;
                    }
                }
                adapter.playAudioComplete(position);//检查是否还有下一条为播的语音信息。
            } else {
                RcsAudioPlayer.getInstence(MyApplication.getApplication()).abandonAudioFocus();// 释放音频焦点
            }
        }
    }

    private static class AudioListener3 implements AudioListener {
        SoftReference<MessageChatListAdapter> softReference;
        int position;
        AudioMsgRecvHolder holder;
        int target;

        public AudioListener3(MessageChatListAdapter adapter, int target) {
            softReference = new SoftReference<>(adapter);
            this.position = position;
            this.target = target;
        }

        public AudioListener3(MessageChatListAdapter adapter, int target, AudioMsgRecvHolder holder) {
            softReference = new SoftReference<>(adapter);
            this.position = position;
            this.target = target;
            this.holder = holder;
        }

        @Override
        public void AudioComplete() {
            RcsAudioPlayer.getInstence(MyApplication.getApplication()).changeToSpeaker();
            MessageChatListAdapter adapter = softReference.get();
            if (adapter != null) {
                // 2018.5.5 YSF 靠近耳边熄屏
                if (adapter.mActivity instanceof MessageDetailActivity) {
                    MessageDetailActivity activity = (MessageDetailActivity) adapter.mActivity;
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if (activity.mWakeLock != null && activity.mWakeLock.isHeld()) {
                        activity.mWakeLock.release();
                    }
                }
                if (holder != null) {
                    if (holder.image_audio != null) {
                        holder.image_audio.setImageResource(R.drawable.message_voice_playing_f6);
                    }
                    if (holder.img_play_icon != null) {
                        holder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_stop_big);
                    }
                    if (holder.img_audio_play_small_icon != null) {
                        holder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_stop_small);
                    }
                    if (holder.audioPlayProgressBar != null) {
                        holder.audioPlayProgressBar.setProgress(0);
                        holder.audioPlayProgressBar.setVisibility(View.GONE);
                    }
                }
                if (adapter != null) {
                    if (adapter.mAudioPlayProgressBar != null) {
                        adapter.mAudioPlayProgressBar.setProgress(0);// 从头开始
                        adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
                        adapter.mAudioPlayProgressBar = null;
                    }
                    if (adapter.mPlayingRecAudio != null) { // 发送图片复位
                        adapter.mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
                        adapter.mPlayingRecAudio = null;
                    }
                    if (adapter.mPlayBgRec != null) {
                        adapter.mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
                        adapter.mPlayBgRec = null;
                    }
                    if (adapter.mPlaySmallRec != null) {
                        adapter.mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
                        adapter.mPlaySmallRec = null;
                    }
                }
                adapter.playAudioComplete(target);
            } else {
                RcsAudioPlayer.getInstence(MyApplication.getApplication()).abandonAudioFocus();// 释放音频焦点
            }
        }
    }

    public void audioRecvContentClick(Message msg, final AudioMsgRecvHolder holder, final int position) {// 点击接受到的语音消息，正在播放就停止
        if (mAttachFragment instanceof BaseChatFragment) {
            if (((BaseChatFragment) mAttachFragment).isReocrding())
                return;
        }
        if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
            ComposeMessageActivityControl.resumeFileTransmission(msg, isGroupChat);
            return;
        } else if (msg.getStatus() == Status.STATUS_DESTROY) {
            BaseToast.show(mContext, mContext.getString(R.string.fade));
            return;
        } else if (msg.getStatus() == Status.STATUS_LOADING) {//正在下载语音则不能点击播放
            return;
        }
        String path = msg.getExtFilePath();
        int secvid = (int) msg.getId();
        holder.image_audio.setImageDrawable(animationDrawableRecv);
        LogF.i(TAG, "audioSendContentClick secvid = " + secvid + "  isPlayingAudio = " + isPlayingAudio);
        if (isPlayingAudio == secvid) { // 点击的是正在播放的语音信息
            if (animationDrawableRecv != null) {
                animationDrawableRecv.stop(); // 停止语音动画
            }
            holder.image_audio.setImageResource(R.drawable.message_voice_playing_f6);
            holder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_stop_big);
            holder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_stop_small);
            stopAudio(); // 停止语音播放
            holder.audioPlayProgressBar.setVisibility(View.GONE);// 隐藏进度
            holder.audioPlayProgressBar.setProgress(0);// 从头播放
            isPlayingAudio = -1;
            audioMessageID = "";
            isPlayingAudioMessage = false; // 点击对方发过来的怎在播放的某条语音，没有播放语音
            mPlayingRecAudio = null; // 波浪图标
            mPlayBgRec = null;      // 大图标
            mPlaySmallRec = null;   // 小图标
            mAudioPlayProgressBar = null; // 播放进度条
            // 2018.5.5 YSF 靠近耳边熄屏
            if (mActivity instanceof MessageDetailActivity) {
                MessageDetailActivity activity = (MessageDetailActivity) mActivity;
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (activity.mWakeLock != null && activity.mWakeLock.isHeld()) {
                    activity.mWakeLock.release();
                }
            }
            LogF.i(TAG, "audioRecvContentClick 1  isPlayingAudioMessage = " + isPlayingAudioMessage);
        } else {
            if (animationDrawableRecv != null) {
                animationDrawableRecv.stop();
            }
            if (mPlayingRecAudio != null) { // 接收语音图片复位
                mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
            }
            if (mPlayBgRec != null) {
                mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
            }
            if (mPlaySmallRec != null) {
                mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
            }
            if (mPlayingSendAudio != null) { // 发送语音图片复位
                mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
            }
            if (mPlayBgSend != null) {
                mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
            }
            if (mPlaySmallSend != null) {
                mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
            }
            if (mAudioPlayProgressBar != null) {
                mAudioPlayProgressBar.setProgress(0);
                mAudioPlayProgressBar.setVisibility(View.GONE);
            }
            animationDrawableRecv = (AnimationDrawable) mContext.getResources().getDrawable(R.drawable.audio_animation);
            holder.image_audio.setImageDrawable(animationDrawableRecv);
            animationDrawableRecv.start(); // 播放动画
            holder.img_play_icon.setImageResource(R.drawable.chat_voiceinformation_play_big);
            holder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_play_small);
            if (!TextUtils.isEmpty(holder.audioAndTv_message.getText().toString())) {
                int width = holder.audioAndTv_message.getWidth(); // 获取文字空间的宽度
                int paddingRight = holder.audioAndTv_message.getPaddingRight();
                int paddingLeft = holder.audioAndTv_message.getPaddingLeft();
                RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams) holder.audioPlayProgressBar.getLayoutParams();
                rp.width = width - paddingLeft - paddingRight - 20;
                holder.audioPlayProgressBar.setLayoutParams(rp);
                int time = getAudioMessageTime(msg);
                LogF.i(TAG, "width = " + width + "  time = " + time + "  paddingLeft = " + paddingLeft + "  paddingRight = " + paddingRight);
                holder.audioPlayProgressBar.setProgress(0);   // 当前进度
                holder.audioPlayProgressBar.setVisibility(View.VISIBLE);
            }
            isPlayingAudio = secvid;
            audioMessageID = msg.getMsgId();// 消息ID
            isPlayingAudioMessage = true; // 开始播放对方发过来的语音，正在播放语音
            mPlayingRecAudio = holder.image_audio;
            mPlayBgRec = holder.img_play_icon;
            mPlaySmallRec = holder.img_audio_play_small_icon;
            mAudioPlayProgressBar = holder.audioPlayProgressBar;
            LogF.i(TAG, "audioRecvContentClick 2  isPlayingAudioMessage = " + isPlayingAudioMessage);
            playAudio(path, new AudioListeneR(this, position, holder)); // 开启语音播放
            LogF.i(TAG, "audioRecvContentClick 3  isPlayingAudioMessage = " + isPlayingAudioMessage);
            if (!msg.isRead()) {
                if (isGroupChat) {
                    GroupChatUtils.updateReadById(mContext, msg.getId());
                } else {
                    MessageUtils.updateReadById(mContext, msg.getId());
                }
            }
        }
    }


    private int getNextUnreadAudio(int oldPos) {
        int newPos = oldPos;
        newPos++;
        if (newPos >= getItemRealCount()) {
            return -1;
        }
        Message msg = getItem(newPos);
        if (msg.getType() == Type.TYPE_MSG_AUDIO_RECV && !msg.isRead()) {
            Log.i("dchan", "oldPo:" + oldPos + ",newPo:" + newPos);
            return newPos;
        } else {
            Log.i("dchan", "oldPo:" + oldPos + ",newPo:" + newPos + ",not fit ,type:" + msg.getType() + ",read:" + msg.isRead());
            return getNextUnreadAudio(newPos);
        }
    }

    private void playAudioComplete(int pos) {
        final int target = getNextUnreadAudio(pos);
        if (target > 0) {
            Message msg = getItem(target);
            isPlayingAudio = (int) msg.getId();
            audioMessageID = msg.getMsgId();// 消息ID
            AudioMsgRecvHolder viewHolder = null;
            if (!TextUtils.isEmpty(msg.getBody()) && msg.getType() == Type.TYPE_MSG_AUDIO_RECV) {
                BaseViewHolder vh = (BaseViewHolder) mRecyclerView.findViewHolderForAdapterPosition(target);
                if (vh instanceof AudioMsgRecvHolder) {
                    viewHolder = (AudioMsgRecvHolder) vh;
                    mAudioPlayProgressBar = viewHolder.audioPlayProgressBar; // 进度条
                    mPlayingRecAudio = viewHolder.image_audio;    // 波浪图标
                    mPlayBgRec = viewHolder.img_play_icon; // 播放大图标
                    mPlaySmallRec = viewHolder.img_audio_play_small_icon; // 播放小图标
                    viewHolder.img_audio_play_small_icon.setImageResource(R.drawable.chat_voiceinformation_play_small);
                    LogF.i(TAG, "连续播放小图标显示");
                    if (!TextUtils.isEmpty(viewHolder.audioAndTv_message.getText().toString())) {
                        LogF.i(TAG, "连续播放进度条显示");
                        int width = viewHolder.audioAndTv_message.getWidth(); // 获取文字空间的宽度
                        int paddingRight = viewHolder.audioAndTv_message.getPaddingRight();
                        int paddingLeft = viewHolder.audioAndTv_message.getPaddingLeft();
                        RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams) viewHolder.audioPlayProgressBar.getLayoutParams();
                        rp.width = width - paddingLeft - paddingRight - 20;
                        viewHolder.audioPlayProgressBar.setLayoutParams(rp);
                        int time = getAudioMessageTime(msg);
                        LogF.i(TAG, "width = " + width + "  time = " + time + "  paddingLeft = " + paddingLeft + "  paddingRight = " + paddingRight);
                        viewHolder.audioPlayProgressBar.setProgress(0);   // 当前进度
                        viewHolder.audioPlayProgressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
            playAudio(msg.getExtFilePath(), new AudioListener3(this, pos, viewHolder));
            if (!msg.isRead()) {
                if (isGroupChat) {
                    GroupChatUtils.updateReadById(mContext, msg.getId());
                } else {
                    MessageUtils.updateReadById(mContext, msg.getId());
                }
            }
        } else {
            RcsAudioPlayer.getInstence(MyApplication.getApplication()).abandonAudioFocus();// 释放音频焦点
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("RcsVoiceSetting", Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("in_call", true); // 后面没有语音了，听完之后要切回外放模式
            editor.commit();
            isPlayingAudio = -1;// 复位 要设为 -1 感觉 0 会出些问题
            audioMessageID = "";
            isPlayingAudioMessage = false;// 后面没有语音可播放了，没有播放语音
        }
        LogF.i(TAG, "playAudioComplete  isPlayingAudioMessage = " + isPlayingAudioMessage);
        notifyDataSetChanged();
    }

    public void imageContentShow(int position, ViewHolder holder, Message msg) {
        if (holder.sCDT != null) holder.sCDT.cancel();

        Bundle bundle = new Bundle();
        if (mPreviewImagelistener != null) {
            bundle = mPreviewImagelistener.previewImage(position);
        }
        Log.i(TAG, "msg :" + msg.getId());
        if (BitmapFactory.decodeFile(msg.getExtThumbPath()) == null) {// 缩略图加载失败
            BaseToast.show(mContext, mContext.getString(R.string.load_failed));
        } else {
            Intent intent = new Intent(mContext, PreviewImageActivity.class);
            bundle.putInt(PreviewImagePresenter.KEY_CONV_TYPE, mChatType);
            bundle.putLong(PreviewImagePresenter.KEY_MESSAGE_ID, msg.getId());
            bundle.putString(PreviewImagePresenter.KEY_ADDRESS, msg.getAddress());
            bundle.putString(PreviewImagePresenter.KEY_EXT_THUMB_PATH,msg.getExtThumbPath());
            intent.putExtras(bundle);
            mActivity.startActivityForResult(intent, BaseChatFragment.PREVIEW_IMAGE_REQUEST);
            mActivity.overridePendingTransition(0, 0);
            UmengUtil.buryPoint(mContext, "message_groupmessage_press_recall", "撤回", 0);
        }
    }

    private void bindMargin(ViewHolder holder, Message msg, int position) {
        if (msg.getIsLast()) {
            LogF.d(TAG, "---------last----------" + msg.getBody() + "+++" + mLastItemPadding);
            holder.itemView.setPadding(0, 0, 0, mLastItemPadding + 18 * 3);
            return;
        }
        holder.itemView.setPadding(0, 0, 0, msg.getBigMargin() ? (int) AndroidUtil.dip2px(mContext, 24) : (int) AndroidUtil.dip2px(mContext, 8));
        LogF.d(TAG, "---------not last----------" + msg.getBody() + "+++" + msg.getBigMargin());


        if (holder instanceof SysMsgViewHolder) {
            Message msgAfter = getItem(canLoadMore() ? position : position + 1);
            boolean isSystemView = true;
            if (msgAfter != null && (msgAfter.getType() == Type.TYPE_MSG_SYSTEM || msgAfter.getType() == Type.TYPE_MSG_WITHDRAW_REVOKE)) {
                isSystemView = false;
            }

            if (!msg.getBigMargin() && isGroupChat && isSystemView) {
                holder.itemView.setPadding(0, 0, 0, (int) AndroidUtil.dip2px(mContext, 24));
            } else {
                holder.itemView.setPadding(0, 0, 0, (int) AndroidUtil.dip2px(mContext, 8));
            }
        }
    }

    @Override
    public void onDataSetChanged() {
    }

    public void setLeftColorId(int leftColorId) {
        this.leftColorId = leftColorId;
    }

    public void setRightColorId(int rightColorId) {
        this.rightColorId = rightColorId;
    }

    public void setRightTextColor(int rightTextColor) {
        this.rightTextColor = rightTextColor;
    }

    public void setLeftTextColor(int leftTextColor) {
        this.leftTextColor = leftTextColor;
    }

    public void setNameTextColor(int nameTextColor) {
        this.nameTextColor = nameTextColor;
    }

    public void setSysTextBackColor(int sysTextBackColor) {
        this.sysTextBackColor = sysTextBackColor;
    }

    public void setIsGroupChat(boolean isGroupChat) {
        this.isGroupChat = isGroupChat;
    }

    public boolean getIsGroupChat() {
        return isGroupChat;
    }

    public int getRawId() {
        return mRawId;
    }

    @Override
    public Message getValueFromCursor(Cursor cursor) {
        // return super.getValueFromCursor(cursor, t); // super 使用反射自动注入
        Message t = new Message();

        if (mColumnIndex == null) {
            mColumnIndex = new ColumnIndex(cursor);
        }
        String value;
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_ID);
            if (!StringUtil.isEmpty(value)) t.setId(Long.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_DATE);
            if (!StringUtil.isEmpty(value)) t.setDate(Long.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_BOX_TYPE);
            if (!StringUtil.isEmpty(value)) t.setBoxType(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_STATUS);
            if (!StringUtil.isEmpty(value)) {
                int v = Integer.valueOf(value);
                t.setStatus(v < 0 ? 0 : v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_READ);
            if (!StringUtil.isEmpty(value)) t.setRead(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SEEN);
            if (!StringUtil.isEmpty(value)) t.setSeen(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_LOCKED);
            if (!StringUtil.isEmpty(value)) t.setLocked(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_ERROR_CODE);
            if (!StringUtil.isEmpty(value)) t.setErrorCode(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_FILE_SIZE);
            if (!StringUtil.isEmpty(value)) t.setExtFileSize(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_DOWN_SIZE);
            if (!StringUtil.isEmpty(value)) t.setExtDownSize(Long.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_ADDRESS_ID);
            if (!StringUtil.isEmpty(value)) t.setAddressId(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_MESSAGE_RECEIPT);
            if (!StringUtil.isEmpty(value)) t.setMessage_receipt(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SHOW_SEND);
            if (!StringUtil.isEmpty(value)) t.setShow_send(Integer.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
        t.setAddress(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_ADDRESS));
        t.setSendAddress(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SEND_ADDRESS));
        t.setPerson(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_PERSON));
        t.setBody(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_BODY));
        t.setMsgId(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_MSG_ID));
        t.setThreadId(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_THREAD_ID));
        t.setExtUrl(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_URL));
        t.setExtShortUrl(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_SHORT_URL));
        t.setExtTitle(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_TITLE));
        t.setExtFileName(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_FILE_NAME));
        t.setExtFilePath(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_FILE_PATH));
        t.setExtThumbPath(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_THUMB_PATH));
        t.setExtSizeDescript(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_EXT_SIZE_DESCRIPT));
        try {
            t.setUrl(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_URL));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTitle(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TITLE));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setSubBody(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SUB_BODY));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setSubTitle(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SUB_TITLE));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setSubUrl(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SUB_URL));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setSubImgPath(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SUB_IMG_PATH));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setSubOriginUrl(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SUB_ORIGIN_LINK));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setSubSourceUrl(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SUB_SOURCE_LINK));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setMedia_uuid(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_SUB_MEDIA_UUID));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setPa_uuid(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_PA_UUID));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_PLATFORM_ACTIVE_STATUS);
            if (!StringUtil.isEmpty(value)) {
                t.setPlatform_active_status(Integer.valueOf(value));
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_PLATFORM_FORWARD);
            if (!StringUtil.isEmpty(value)) {
                t.setPlatform_forward(Integer.valueOf(value));
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setAuthor(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_AUTHOR));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setIdentify(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_IDENTIFY));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_NOTIFY_DATE);
            if (!StringUtil.isEmpty(value)) t.setNotifyDate(Long.valueOf(value));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_title(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_TITLE));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_top_color(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_TOP_COLOR));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_first_text(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_FIRST_TEXT));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_first_color(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_FIRST_COLOR));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_last_text(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_LAST_TEXT));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_last_color(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_LAST_COLOR));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_url(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_URL));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_name(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_NAME));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_value_text(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_VALUE_TEXT));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setTemplate_value_color(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TEMPLATE_VALUE_COLOR));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            t.setXml_content(mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_XML_CONTENT));
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_NAME_TYPE);
            if (!StringUtil.isEmpty(value)) {
                int type = Integer.valueOf(value);
                t.setType(type);
                if (type == Type.TYPE_MSG_TEXT_DRAFT) {
                    Log.e(TAG, "!! Should not be this:" + t);
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            value = mColumnIndex.getValue(cursor, BaseModel.COLUMN_TEXT_SIZE);
            if (!StringUtil.isEmpty(value)) {
                t.setTextSize(value);
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.getMessage());
        }
        return t;
    }

    public interface PreviewImageListener {
        Bundle previewImage(int position);
    }

    public interface SelectAtCallback {
        public void selectAtMember(GroupMember gm);
    }

    public void setChatType(int chatType) {
        mChatType = chatType;
    }

    public int getChatType() {
        return mChatType;
    }

    public void isEPGroup(boolean isEPGroup) {
        this.isEPGroup = isEPGroup;
    }

    public boolean getIsEPGroup() {
        return isEPGroup;
    }

    public void isPartyGroup(boolean isPartyGroup) {
        this.isPartyGroup = isPartyGroup;
    }

    public boolean getIsPartyGroup() {
        return isPartyGroup;
    }

    private void playAudio(String path, AudioListener listener) {
        try {
            // 2018.5.5 YSF 靠近耳边熄屏
            if (mActivity instanceof MessageDetailActivity) {
                MessageDetailActivity activity = (MessageDetailActivity) mActivity;
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 屏幕常亮
                if (activity.mWakeLock != null && !activity.mWakeLock.isHeld()) {
                    activity.mWakeLock.acquire();
                }
            }
            setAudioManager();
            RcsAudioPlayer.getInstence(mContext).play(path, listener);
        } catch (Exception e) {
            e.printStackTrace();
            RcsAudioPlayer.getInstence(MyApplication.getApplication()).abandonAudioFocus();// 释放音频焦点
        }
    }

    private void setAudioManager() {
        try {
            final SharedPreferences sharedPreferences = mContext.getSharedPreferences("RcsVoiceSetting", Context.MODE_MULTI_PROCESS);
            final boolean isSpeaker = sharedPreferences.getBoolean("in_call", true); // 同听模式
            Log.i(TAG, "isSpeaker = " + isSpeaker);
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            boolean isSpeekerphoneOn = audioManager.isSpeakerphoneOn();
            if (isSpeaker) { //外放
                if (!isSpeekerphoneOn) {
                    audioManager.setSpeakerphoneOn(true);
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                }
            } else {
                if (isSpeekerphoneOn) {
                    audioManager.setSpeakerphoneOn(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        LogF.d(TAG, "setAudioManager 听筒");
                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    } else {
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                    }
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.STREAM_VOICE_CALL);
                    Class<?> audioSystemClass = Class.forName("android.media.AudioSystem");
                    Method method = audioSystemClass.getDeclaredMethod("setForceUse", int.class, int.class);
                    Field field_c = audioSystemClass.getField("FOR_COMMUNICATION");
                    int int_c = field_c.getInt(audioSystemClass);
                    Field field_n = audioSystemClass.getField("FORCE_NONE");
                    int int_n = field_n.getInt(audioSystemClass);
                    method.invoke(audioSystemClass, int_c, int_n);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void stopAudioForActivity() {
        if (animationDrawable != null) {
            animationDrawable.stop();
        }
        if (mPlayingRecAudio != null) {
            mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
            mPlayingRecAudio = null;
        }
        if (mPlayBgRec != null) {
            mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
            mPlayBgRec = null;
        }
        if (mPlaySmallRec != null) {
            mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
            mPlaySmallRec = null;
        }
        if (mPlayingSendAudio != null) {
            mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
            mPlayingSendAudio = null;
        }
        if (mPlayBgSend != null) {
            mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
            mPlayBgSend = null;
        }
        if (mPlaySmallSend != null) {
            mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
            mPlaySmallSend = null;
        }
        if (mAudioPlayProgressBar != null) {
            mAudioPlayProgressBar.setProgress(0);
            mAudioPlayProgressBar.setVisibility(View.GONE);
        }
        RcsAudioPlayer.getInstence(mContext).stop();
        isPlayingAudio = -1;
        audioMessageID = "";
        isPlayingAudioMessage = false; // 复位
        LogF.i(TAG, "stopAudioForActivity  isPlayingAudioMessage = " + isPlayingAudioMessage);
        notifyDataSetChanged();
    }

    private void stopAudio() {
        RcsAudioPlayer.getInstence(mContext).stop();
    }

    public void setPreMsg(boolean preMsg) {
        isPreMsg = preMsg;
    }

    public boolean getIsPreMsg() {
        return isPreMsg;
    }

    public final static int HANDLER_NOTIFY_ID = 882567;
    public android.os.Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == HANDLER_NOTIFY_ID && msg.arg1 == HANDLER_NOTIFY_ID) {
                MessageChatListAdapter.this.notifyDataSetChanged();
            }
        }
    };

    public AlertDialog getDialog() {
        return dialog;
    }

    public void showDialog(String content, String leftText, String rightText, View.OnClickListener leftListener, View.OnClickListener rightListener) {
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = new AlertDialog.Builder(mContext).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_dialog_common, null);
        dialog.setContentView(view);
        TextView dialogMessage = view.findViewById(R.id.dialog_message);
        dialogMessage.setText(content);

        TextView cancel = view.findViewById(R.id.btn_cancel);
        cancel.setText(leftText);
        cancel.setOnClickListener(leftListener);
        TextView sure = view.findViewById(R.id.btn_ok);
        sure.setText(rightText);
        sure.setOnClickListener(rightListener);
    }


    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            if (action == LogicActions.REVOKE_AUDIO_MESSAGE) { // 撤回语音
                String msgID = intent.getStringExtra(LogicActions.IMDN_MESSAG_ID);
                if (RcsAudioPlayer.getInstence(mContext).isPlaying() &&
                        !TextUtils.isEmpty(msgID) &&
                        !TextUtils.isEmpty(audioMessageID) &&
                        msgID.equals(audioMessageID)) {
                    RcsAudioPlayer.getInstence(mContext).stop();//停止语音播放
                }
            }
        }
    };

    public void setmRecyclerView(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;

    }

    public void setMultiSelectMode(boolean isMultiSelectMode){
        this.isMultiSelectMode = isMultiSelectMode;
        notifyDataSetChanged();
    }

    public boolean getIsMultiSelectMode(){
        return this.isMultiSelectMode;
    }

    public SparseBooleanArray getSelectedList(){
        return selectedList;
    }

    public void addSelection(int position){
        LogF.i(TAG,"addSelection position = " + position);
        selectedList.put(position,true);
        notifyItemDataChanged(position ,true);
    }

    public void removeSelection(int position ,boolean needChange){
        LogF.i(TAG,"removeSelection position = " + position);
        selectedList.delete(position);
        notifyItemDataChanged(position , needChange);
    }

    public void clearSelection(){
        LogF.i(TAG,"clearSelection");
        selectedList.clear();
    }

    private void notifyItemDataChanged(int position ,boolean needChange){
        if(needChange){
            notifyItemChanged(position);
        }
        if(mOnCheckChangeListener != null){
            mOnCheckChangeListener.onCheckChange(selectedList.size());
        }
    }

    public interface OnCheckChangeListener{
        void onCheckChange(int selectedCount);
    }

    public void setOnCheckChangeListener(OnCheckChangeListener listener){
        mOnCheckChangeListener = listener;
    }
}