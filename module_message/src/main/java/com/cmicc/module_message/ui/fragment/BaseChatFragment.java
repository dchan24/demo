package com.cmicc.module_message.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulemessage.MessageProxy;
import com.app.module.proxys.moduleredpager.RedpagerProxy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.CommonUtils;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.PopWindowFor10GUtil;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_common.utils.StringConstant;
import com.chinamobile.app.yuliao_common.utils.Threads.HandlerThreadFactory;
import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.chinamobile.app.yuliao_contact.model.BaseContact;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.bqss.BQSSConstants;
import com.cmcc.cmrcs.android.bqss.api.BQSSApiCallback;
import com.cmcc.cmrcs.android.bqss.api.BQSSApiResponseObject;
import com.cmcc.cmrcs.android.bqss.api.BQSSSearchApi;
import com.cmcc.cmrcs.android.bqss.api.BQSSWebSticker;
import com.cmcc.cmrcs.android.ui.ScrollForToolbar;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.interfaces.SendAudioMessageCallBack;
import com.cmcc.cmrcs.android.ui.interfaces.ShadowPanelListener;
import com.cmicc.module_message.ui.activity.ChooseLocalFileActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.GalleryActivity;
import com.cmicc.module_message.ui.activity.MessageBgSetActivity;
import com.cmicc.module_message.ui.activity.SuperMsgActivity;
import com.cmicc.module_message.ui.activity.VideoRecordActivity;
import com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter.OnSetCursorDoneListener;
import com.cmicc.module_message.ui.adapter.ChatRichMediaGirdAdapter;
import com.cmicc.module_message.ui.adapter.ChatRichMediaViewPagerAdapter;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.broadcast.MsgNotificationReceiver;
import com.cmcc.cmrcs.android.ui.callback.HbAuthCallback;
import com.cmcc.cmrcs.android.ui.callback.SendAudioTextCallBack;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.control.LimitedUserControl;
import com.cmcc.cmrcs.android.ui.control.LimitedUserControl.OnLimitedUserLinster;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.dialogs.HbAuthDialog;
import com.cmcc.cmrcs.android.ui.dialogs.PermissionDeniedDialog;
import com.cmcc.cmrcs.android.ui.dialogs.RedPaperProgressDialog;
import com.cmcc.cmrcs.android.ui.dialogs.ToastDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.interfaces.IFragmentBack;
import com.cmcc.cmrcs.android.ui.model.ImageData;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.model.impls.MessageEditorModelImpl;
import com.cmicc.module_message.ui.presenter.BaseChatPresenterImpl;
import com.cmicc.module_message.ui.presenter.ExpressionPresenter;
import com.cmicc.module_message.ui.presenter.GalleryPresenter;
import com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.GifMessageUtil;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.MessageThemeUtils;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.NoDoubleClickUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.ui.utils.ServiceCallMainUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmcc.cmrcs.android.widget.emoji.EmojiEditText;
import com.cmcc.cmrcs.android.widget.emoji.EmojiParser;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.constvalue.MessageModuleConst;
import com.feinno.redpaper.sdk.IQueryHbAuthCallback;
import com.feinno.redpaper.utils.SdkInitManager4Red;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.view.View.VISIBLE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.AT_MEMBER_ALL;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.AT_MEMBER_LIST;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.CONTACT_VCARD_STRING;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_CHAT_ID;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_OWNER;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_TYPE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.IS_EP_GROUP;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.VCARD_EXPORT;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.VCARD_SELECT_CONTACT;
import static com.cmicc.module_message.ui.activity.MessageBgSetActivity.MESSAGE_STYLE_CHANGE;
import static com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter.TYPE_NEW_MSG_RECV_DIVIDE_LINE;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_NODOWN;

import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_AT_GROUP_MEMBER;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_VCARD_SEND;

/**
 * Created by tigger on 2017/5/3.
 */

public abstract class BaseChatFragment extends BaseFragment implements IFragmentBack, BaseChatContract.View, SendAudioMessageCallBack, SendAudioTextCallBack,
        ShadowPanelListener, OnTouchListener, View.OnClickListener, View.OnLongClickListener, MessageChatListAdapter.OnCheckChangeListener {

    private static final String TAG = "BaseChatFragment";
    private static final int MAX_HEIGHT = 1200;
    private static final int MIN_HEIGHT = 500;
    private static final int MAX_HEIGHT_DP = 400; // 以1080 X 1920 的分辨率为主（3）来算的
    private static final int MIN_HEIGHT_DP = 257; // 772/3.0 = 257
    private static final int DEFAULT_TEXT_SIZE = 16;
    public static final long MAX_FILE_LENGTH = 500 << 20;
    private static final String PREF_KEY_INPUT_METHOD_HEIGHT = "pref_key_input_method_height";
    private final String SP_KEY_FIRST_DRAG_UP_SEND = "sp_key_first_drag_up_send";    // 是否第一次"向上拖动"发送
    LinearLayout mLlTextInput;
    //Unbinder unbinder;
    private ScrollForToolbar scrollForToolbar;
    protected String phone;
    private boolean justSend = false;
    //表情搜搜
    private MyHandler mHandler;
    public static final int SUCCESS = 1;
    public static final int ERROR = 2;
    private boolean isGifViewShow;
    public ImageButton mIbSend;
    public LinearLayout mRichPanel;
    public RecyclerView mRecyclerView;
    public LinearLayout mKeyboardPanel;
    ImageView mIbPic;
    ImageView mIbTackPhoto;
    ImageView mIbProfile;
    ImageView mIvBkg;
    RelativeLayout mLayout;
    public ImageButton mIbExpression;
    public ImageButton mIbAudio;
    //    ImageButton mIbAudioToText;
    public FrameLayout mFlMore;
    public EmojiEditText mEtMessage;//输入框
    ImageButton mIbExpressionKeyboard;
    LinearLayout fl_ani_panel;
    LinearLayout lltInputAndMenu;
    EmojiEditText ani_message;
    public FrameLayout mInputLayout;
    protected RelativeLayout mBtMsgATTip;
    TextView mBtMsgCountTip;
    RelativeLayout mRLMsgCountTip;
    TextView mBtMsgCountTip_up;//跟@处于同个位置
    RelativeLayout mRLMsgCountTip_up;//跟@处于同个位置
    TextView mTvAt;
    BqssHorizontalScrollView mBqssHScrollview;
    LinearLayout mStickersContainer;


    private float mPosX, mPosY, mCurPosX, mCurPosY;
    private boolean isScroll = false;
    private boolean isScrollUp = false; //字体放大
    private boolean isScrollDown = false; // 字体缩小
    //消息字体大小
    protected String messageSize = null;
    private String messageContent = null;
    //@群成员列表
    private static HashMap<String, ArrayList<AtMemberLength>> mapForMemberlist = new HashMap<>();

    protected boolean mIsAtAll;
    protected int mAtAllSelectionStart;
    protected int mAtAllSelectionEnd;

    private PopupWindow mChatSendPopupWindow;
    private boolean mAtInput; //长按at导致的键盘输入g605
    private boolean mIsAnimating = false;//是否在开启动画

    public boolean isEPGroup = false;//是否为企业群
    public boolean isPartyGroup = false;//是否为党群
    public boolean isOwner = false; // 群主
    private LinearLayout mLayoutForMessage;
    private LinearLayout mLayoutForSMS;
    private TextView mTvIsFree;
    private TextView mTvExitSMS;
    private TextView mTxt;//逗号
    private LinearLayout layoutSmsPanel;
    private EmojiEditText mEtSms;
    private ImageButton mIbSmsSend;

    private ImageView mIbMore;

    private ImageView mIvCancelGif;
    private LinearLayout mLLGifLayout;
    private ImageView mSmsDirection;

    protected int mRawId;

    protected View mRecorderRedDot;

    private boolean mIsInputAudioText; //是否使用了语音转文字输入

    private Fragment mMessageAudioTextFragment;

    HbAuthDialog mHbAuthDialog;
    protected View moreRedDot ;  // 红点
    protected LinearLayout chatRichMediaIndexes;
    protected View indexes_one , indexes_two;
    protected ViewPager chatRichMediaVp;
    private List<GridView> gridViews;
    protected ChatRichMediaViewPagerAdapter mPagerAdapter ;
    protected int [][] richMediaData = null;
    protected int [][] richMediaDataTwo = null;
    protected String mAccountUserPhone ; //当前登陆用户的手机号码
    private OnGlobalLayoutListener mOnGlobalLayoutListener;
    protected boolean mPreCache;
    protected int mPid;

    protected LinearLayout mMultiOpreaLayout;
    protected TextView mMultiDeleteBtn , mMultiForwardBtn;
    protected LinearLayout mBaseInputLayout;

    public final int SEND_TXT_TYPE = 1;
    public final int SEND_PIC_TYPE = 2;
    public final int SEND_AUDIO_TYPE = 3;
    public final int SEND_CARD_TYPE = 4;
    public final int SEND_LOCATION_TYPE = 5;
    public final int SEND_FILE_TYPE = 6;
    public final int SEND_GIF_TYPE = 7;
    public final int SEND_RED_PACKAGE_TYPE = 8;
    public final int SEND_APPROV_TYPE = 9;
    public final int SEND_LOG_TYPE = 10;
    public final int SEND_SMS_TYPE = 11;
    private boolean isSMSMode = false ; // 在短信模式界面

    public void initViews(View rootView) {
        super.initViews(rootView);
        mLlTextInput = (LinearLayout) rootView.findViewById(R.id.ll_text_input);
        mIbSend = (ImageButton) rootView.findViewById(R.id.ib_send);
        mIbSend.setOnTouchListener(this);
        mIbSend.setOnClickListener(this);
        mIbSend.setOnLongClickListener(this);

        mRichPanel = (LinearLayout) rootView.findViewById(R.id.ll_rich_panel);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_message_chat);
        mKeyboardPanel = (LinearLayout) rootView.findViewById(R.id.fl_edit_panel);

        mLayoutForMessage = (LinearLayout) rootView.findViewById(R.id.layout_for_message);
        mLayoutForSMS = (LinearLayout) rootView.findViewById(R.id.layout_for_sms);

        mTvIsFree = (TextView) rootView.findViewById(R.id.tv_isFree);
        //mTvFreeBadge = (TextView) rootView.findViewById(R.id.tv_free_badge);
        mSmsDirection = (ImageView) rootView.findViewById(R.id.sms_direction);
        mSmsDirection.setOnClickListener(this);

        mTvExitSMS = (TextView) rootView.findViewById(R.id.tv_exitsms);
        mTvExitSMS.setOnClickListener(this);

        mTxt = (TextView) rootView.findViewById(R.id.txt);


        layoutSmsPanel = (LinearLayout) rootView.findViewById(R.id.layout_sms_pannel);
        mEtSms = (EmojiEditText) rootView.findViewById(R.id.et_sms);
        mIbSmsSend = (ImageButton) rootView.findViewById(R.id.ib_sms_send);
        mIbSmsSend.setOnClickListener(this);


        mIvCancelGif = (ImageView) rootView.findViewById(R.id.iv_cancel_gif);
        mIvCancelGif.setOnClickListener(this);

        mLLGifLayout = (LinearLayout) rootView.findViewById(R.id.ll_gif_layout);


        mIbMore = (ImageView) rootView.findViewById(R.id.ib_more);
        mIbMore.setOnClickListener(this);


        mIbPic = (ImageView) rootView.findViewById(R.id.ib_pic);
        mIbPic.setOnClickListener(this);

        mIbTackPhoto = (ImageView) rootView.findViewById(R.id.ib_take_photo);
        mIbTackPhoto.setOnClickListener(this);

        mIbProfile = (ImageView) rootView.findViewById(R.id.ib_profile);
        mIbProfile.setOnClickListener(this);

        mIvBkg = (ImageView) rootView.findViewById(R.id.iv_bkg);
        mLayout = (RelativeLayout) rootView.findViewById(R.id.message_editor_layout);
        mIbExpression = (ImageButton) rootView.findViewById(R.id.ib_expression);
        mIbExpression.setOnClickListener(this);

        mIbAudio = (ImageButton) rootView.findViewById(R.id.ib_audio);
        mIbAudio.setOnClickListener(this);

        mRecorderRedDot = rootView.findViewById(R.id.ib_record_red_dot);
        mNotTipRecord = (boolean) SharePreferenceUtils.getParam(MyApplication.getAppContext(), NOT_TIP_ANY_MORE_KEY, false);
        if (mNotTipRecord == false) {
            mRecorderRedDot.setVisibility(View.VISIBLE);
        } else {
            mRecorderRedDot.setVisibility(View.GONE);
        }


        rootView.findViewById(R.id.ib_gif).setOnClickListener(this);
        rootView.findViewById(R.id.ib_file).setOnClickListener(this);

        mFlMore = (FrameLayout) rootView.findViewById(R.id.fl_more);
        mEtMessage = (EmojiEditText) rootView.findViewById(R.id.et_message);
        mEtMessage.setOnClickListener(this);
        mIbExpressionKeyboard = (ImageButton) rootView.findViewById(R.id.ib_expression_keyboard);
        mIbExpressionKeyboard.setOnClickListener(this);
        fl_ani_panel = (LinearLayout) rootView.findViewById(R.id.fl_ani_panel);
        lltInputAndMenu = (LinearLayout) rootView.findViewById(R.id.input_and_menu);
//        ani_send = (ImageButton) RootView.findViewById(R.id.ani_send);
        ani_message = (EmojiEditText) rootView.findViewById(R.id.ani_message);
        mInputLayout = (FrameLayout) rootView.findViewById(R.id.input_layout);
        mBtMsgATTip = (RelativeLayout) rootView.findViewById(R.id.message_at_tip);
        mBtMsgATTip.setOnClickListener(this);

        mBtMsgCountTip = (TextView) rootView.findViewById(R.id.message_count_tip);
        mRLMsgCountTip = (RelativeLayout) rootView.findViewById(R.id.rl_message_count_tip);
        mBtMsgCountTip_up = (TextView) rootView.findViewById(R.id.message_count_tip1);
        mRLMsgCountTip_up = (RelativeLayout) rootView.findViewById(R.id.rl_message_count_tip1);
        mRLMsgCountTip.setOnClickListener(this);
        mRLMsgCountTip_up.setOnClickListener(this);
        mTvAt = (TextView) rootView.findViewById(R.id.tv_at);
        mBqssHScrollview = (BqssHorizontalScrollView) rootView.findViewById(R.id.bqss_hscrollview);
        mStickersContainer = (LinearLayout) rootView.findViewById(R.id.stickers_container);

        moreRedDot = rootView.findViewById(R.id.id_more_red_dot);
        chatRichMediaIndexes = rootView.findViewById(R.id.indexes_ll);
        indexes_one = rootView.findViewById(R.id.indexes_one);
        indexes_two = rootView.findViewById(R.id.indexes_two);
        chatRichMediaVp = (ViewPager) rootView.findViewById(R.id.chat_rich_media_vp);

        mBaseInputLayout = (LinearLayout) rootView.findViewById(R.id.base_input_layout);

        mMultiOpreaLayout = (LinearLayout) rootView.findViewById(R.id.multi_opera_layout);
        mMultiDeleteBtn = (TextView) rootView.findViewById(R.id.multi_btn_delete);
        mMultiForwardBtn = (TextView) rootView.findViewById(R.id.multi_btn_forward);
        mMultiDeleteBtn.setOnClickListener(this);
        mMultiForwardBtn.setOnClickListener(this);
    }

    protected boolean mNeedSendAudio = false;
    private int mSelectSendType;
    private final int RECORD_IS_NOMRAL = 0;
    private final int RECORD_STOP_EXIT_TYPE = 1;
    private final int RECORD_STOP_FINISH_TYPE = 2;
    private final int RECORD_STOP_INTERRUPT_TYPE = 3;
    private final int RECORD_HAS_SEND_TYPE = 4;
    private final int NOT_IN_RECORD_MODE = 5;
    private int mRecordExitType = NOT_IN_RECORD_MODE;
    private long mRecordStartTime = 0;

    private void setAudioBtnVisibility(int visibility) {
        mIbAudio.setVisibility(visibility);
        if (visibility == VISIBLE) {
            if (mNotTipRecord == false) {
                mRecorderRedDot.setVisibility(View.VISIBLE);
            } else {
                mRecorderRedDot.setVisibility(View.GONE);
            }
        } else {
            mRecorderRedDot.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAudioRecordStart() {
        mRecordExitType = RECORD_IS_NOMRAL;
        mRecordStartTime = System.currentTimeMillis();
        if (mSelectSendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
            mIbSend.setEnabled(true);
        }

    }

    @Override
    public boolean isGroupChat(){
        return getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT;
    }

    @Override
    public void onAudioTextResult(String result, boolean isLast) {
        if (mRecordExitType == RECORD_HAS_SEND_TYPE
                || mRecordExitType == RECORD_STOP_EXIT_TYPE
                || mRecordExitType == RECORD_STOP_FINISH_TYPE) {
            return;
        }
        if (!isLast && mSelectSendType != MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
            mEtMessage.setText(result);

            LogF.d("XFTest", "set text：" + result + ",record type = " + mRecordExitType);
            mEtMessage.setSelection(mEtMessage.getText().length());
        }
        switch (mRecordExitType) {
            case RECORD_IS_NOMRAL:
                mIbSend.setEnabled(true);
                break;

            case RECORD_STOP_INTERRUPT_TYPE:
                mRichPanel.setVisibility(View.VISIBLE);
                mIbExpression.setVisibility(View.VISIBLE);
                mIbSend.setEnabled(true);

                break;
        }

    }

    @Override
    public void onAudioRecordFinish(String audioSavaPath) {
        mRecordExitType = RECORD_STOP_FINISH_TYPE;
//
        String result = mEtMessage.getText().toString();
        final long duration = (System.currentTimeMillis() - mRecordStartTime);
        if (!TextUtils.isEmpty(result)) {
            if (mSelectSendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT) {
                startSendAudio(audioSavaPath, duration, mEtMessage.getText().toString());

            } else if (mSelectSendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT) {
                sendMessage();
            }
            mRecordExitType = RECORD_HAS_SEND_TYPE;
            LogF.d("XFTest", "onAudioRecordFinish：set result = " + result + ",record type = " + mRecordExitType + ",duration = " + duration);
        } else if (mSelectSendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
            Context context = getActivity();
            if (duration < 1000) {
                if (context != null)
                    Toast.makeText(getContext(), "录音时间太短，请重试", Toast.LENGTH_SHORT).show();
                return;
            }
            LogF.d("XFTest", "onAudioRecordFinish：set audio result = " + result + ",record type = " + mRecordExitType + ",duration = " + duration);
            startSendAudio(audioSavaPath, duration, "");

        }

        mIbSend.setEnabled(false);
        mIbSend.setVisibility(View.VISIBLE);
        setAudioBtnVisibility(View.GONE);
        mEtMessage.setText("");

    }

    private void startSendAudio(final String path, final long lon, final String detail) {
        File file = new File(path);
        if (file.length() > 0) {
            if (mSelectSendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
                senAudioMessage(path, lon, "");
                return;
            }
            senAudioMessage(path, lon, detail);
        } else {
            LogF.d("XFtest", "sendAudioAfter 100ms");
            SendAudioRunnable runnable = new SendAudioRunnable(path, lon, detail);
            HandlerThreadFactory.getHandlerThread(HandlerThreadFactory.BackgroundThread).postDelay(runnable, 100);
        }
    }

    public boolean isReocrding() {
        return mRecordExitType == RECORD_IS_NOMRAL;
    }


    public class SendAudioRunnable implements Runnable {
        private int mRetryTimes;
        private String path;
        private long duration;
        private String detail;
        private final static int MAX_RETRY_TIMES = 10;

        public SendAudioRunnable(String path, long duration, String detail) {
            this.path = path;
            this.duration = duration;
            this.detail = detail;
        }

        @Override
        public void run() {
            mRetryTimes++;
            File file = new File(path);
            if (file.length() > 0 || mRetryTimes > MAX_RETRY_TIMES) {
                LogF.d("XFtest", "sendAudio in delay runnable , retry time =" + mRetryTimes);
                if (mSelectSendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
                    senAudioMessage(path, duration, "");
                } else {
                    senAudioMessage(path, duration, detail);
                }
                mRetryTimes = 0;
            } else {
                LogF.d("XFtest", "sendAudioAfter 100ms");
                HandlerThreadFactory.getHandlerThread(HandlerThreadFactory.BackgroundThread).postDelay(this, 100);
            }
        }
    }

    @Override
    public void onAudioSelectSendMode(int sendType) {
        mSelectSendType = sendType;
        Activity activity = getActivity();
        if (activity == null)
            return;
        if (sendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO_AND_TEXT
                || sendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_TEXT) {
            if (isAdded()) {
                mEtMessage.setText("");
                mEtMessage.setHint(getResources().getString(R.string.say_something));
            }
        } else if (sendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) {
            mEtMessage.setHint(getResources().getString(R.string.send_voice_tips));
            mEtMessage.setText("");
        }
    }

    @Override
    public void onAudioRecordExit() {
        FragmentActivity context = getActivity();
        if (context == null)
            return;
        mRecordExitType = RECORD_STOP_EXIT_TYPE;
        mRichPanel.setVisibility(View.VISIBLE);
//        mIbExpressionKeyboard.setVisibility(View.VISIBLE);
        mIbExpression.setVisibility(View.VISIBLE);
        mEtMessage.setHint(getResources().getString(R.string.say_something));
//        mEtMessage.setText("");
        mIbSend.setEnabled(true);
        if (isShowAudio) {
            isShowAudio = false;
            showKeyboardBySoft();
            if (mMessageAudioTextFragment != null) {
                FragmentManager fragmentManager = context.getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (fragmentManager.findFragmentByTag("audio_fragment") != null || hasAddAudioFragment) {
                    //   transaction.show(mMessageAudioFragment);
                    transaction.hide(mMessageAudioTextFragment);
//                    hasAddAudioFragment = false;
                }
            }
            return;
        }

    }

    @Override
    public void onAudioRecordInterrupt() {
        String messageText = mEtMessage.getText().toString();
        if ("".equals(messageText)) {
            mRecordExitType = RECORD_STOP_EXIT_TYPE;
        } else {
            mRecordExitType = RECORD_STOP_INTERRUPT_TYPE;
        }

        if (isAdded()) {
            mEtMessage.setHint(getResources().getString(R.string.say_something));
        }
        if (mSelectSendType == MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_AUDIO) { //如果是录音模式的话，中断相当于就直接退出了
            mRecordExitType = RECORD_STOP_EXIT_TYPE;
            mRichPanel.setVisibility(View.VISIBLE);
            mIbExpression.setVisibility(View.VISIBLE);
            mEtMessage.setText("");
            mIbSend.setEnabled(true);
            if (isShowAudio) {
                isShowAudio = false;
                if (mMessageAudioTextFragment != null) {
                    FragmentActivity context = getActivity();
                    if (context == null)
                        return;
                    FragmentManager fragmentManager = context.getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (fragmentManager.findFragmentByTag("audio_fragment") != null || hasAddAudioFragment) {
                        //   transaction.show(mMessageAudioFragment);
                        transaction.hide(mMessageAudioTextFragment);
//                    hasAddAudioFragment = false;
                    }
                }
                return;
            }
        }
        if (isAdded()) {
            mEtMessage.setHint(getResources().getString(R.string.say_something));
        }
    }

    @Override
    public void onRecordSpeechEnd() {
        FragmentActivity context = getActivity();
        if (context == null)
            return;
        mRecordExitType = RECORD_STOP_INTERRUPT_TYPE;
        mRichPanel.setVisibility(View.VISIBLE);
        mIbExpression.setVisibility(View.VISIBLE);
        if (isAdded()) {
            mEtMessage.setHint(getResources().getString(R.string.say_something));
        }
        mIbSend.setEnabled(true);
        if (isShowAudio) {
            isShowAudio = false;
            showKeyboardBySoft();
            if (mMessageAudioTextFragment != null) {
                FragmentManager fragmentManager = context.getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (fragmentManager.findFragmentByTag("audio_fragment") != null || hasAddAudioFragment) {
                    transaction.hide(mMessageAudioTextFragment);
                }
            }
        }
    }

    @Override
    public void onRecordPermissionFaild() {
        FragmentActivity context = getActivity();
        if (context == null)
            return;
        mRecordExitType = RECORD_STOP_EXIT_TYPE;
        mRichPanel.setVisibility(View.VISIBLE);
        mIbExpressionKeyboard.setVisibility(View.VISIBLE);
        mIbExpression.setVisibility(View.VISIBLE);
        if (isAdded()) {
            mEtMessage.setHint(getResources().getString(R.string.say_something));
        }
        mEtMessage.setText("");
        mIbSend.setEnabled(true);
        if (isShowAudio) {
            isShowAudio = false;
            showKeyboardBySoft();
            if (mMessageAudioTextFragment != null) {

                FragmentManager fragmentManager = context.getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (fragmentManager.findFragmentByTag("audio_fragment") != null || hasAddAudioFragment) {
                    transaction.hide(mMessageAudioTextFragment);

                }
            }
        }
    }
    private static final String NOT_TIP_ANY_MORE_KEY = "not_tip_record_red_dot";
    private boolean mNotTipRecord = true;

    private void audioRecordBtnClick() {
        Activity attachActivity = getActivity();
        if (attachActivity == null)
            return;

        if (ServiceCallMainUtils.isCallingState()) {
            Toast.makeText(attachActivity, attachActivity.getResources().getString(R.string.calling_not_record), Toast.LENGTH_SHORT).show();
            return;

        }

        if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
            UmengUtil.buryPoint(attachActivity, "message_groupmessage_talk", "语音", 0);
        } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
            UmengUtil.buryPoint(attachActivity, "message_p2pmessage_talk", "语音", 0);
        }

        mIbSend.setVisibility(View.VISIBLE);
        setAudioBtnVisibility(View.GONE);
        mIbSend.setEnabled(false);
        mEtMessage.clearFocus();

        isShowAudio = true;
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        hideKeyboardAndTryHideGifView();
        setPanelHeight(mInputHeight);


        FragmentActivity context = getActivity();
        if (context == null)
            return;
        FragmentManager fragmentManager = context.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mExpressionFragment != null && fragmentManager.findFragmentByTag("expression_fragment") != null) {
            transaction.hide(mExpressionFragment);
        }
        if (mFlMore.getBackground() != null) {
            mFlMore.setBackgroundDrawable(null);
        }
        if (mMessageAudioTextFragment != null && fragmentManager.findFragmentByTag("audio_fragment") != null || hasAddAudioFragment) {
            transaction.show(mMessageAudioTextFragment);
        } else {
            mMessageAudioTextFragment = MessageProxy.g.getUiInterface().getAudioTextFragment(this);
            hasAddAudioFragment = true;
            transaction.add(R.id.fl_more, mMessageAudioTextFragment, "audio_fragment");
        }


        if (!mNotTipRecord) {
            SharePreferenceUtils.setParam(MyApplication.getAppContext(), NOT_TIP_ANY_MORE_KEY, true);
            MessageProxy.g.getUiInterface().setAudioFragmentPageType(mMessageAudioTextFragment, MessageModuleConst.AUDIO_FRAGMENT_SELECT_SEND_VOICE_PAGE);
            mRecorderRedDot.setVisibility(View.GONE);
            mNotTipRecord = true;
        } else {
            MessageProxy.g.getUiInterface().setAudioFragmentPageType(mMessageAudioTextFragment, MessageModuleConst.AUDIO_FRAGMENT_SEND_VOICE_AND_TEXT_PAGE);
        }
        transaction.commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }, 200);
        mFlMore.setVisibility(View.VISIBLE);

        mRichPanel.setVisibility(View.GONE);
        mIbExpression.setVisibility(View.GONE);
        mIbExpressionKeyboard.setVisibility(View.GONE);
        if (chatRichMediaVp != null && chatRichMediaVp.getVisibility() == View.VISIBLE) {//关闭
            mOpenFlag = false;
            mIsAnimating = true;
            animateClose(chatRichMediaVp);
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (isShowAudio || mRecordExitType == RECORD_STOP_INTERRUPT_TYPE) {
            return false;
        }

        if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SMSMMS_SINGLE_CHAT) {//公众号会话屏蔽麦克风
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                mPosX = event.getX();
                mPosY = event.getY();

                mCurPosX = mPosX;
                mCurPosY = mPosY;

                messageContent = mEtMessage.getText().toString();
                ani_message.setText(messageContent);
                ani_message.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE);
                CharSequence c = EmojiParser.getInstance().replaceAllEmojis(getActivity(), ani_message.getText().toString(), mEmojiconSize);
                ani_message.setLinkText(c);
                ani_message.setSelection(messageContent.length());  //文字太多显示不全时，选择显示末尾文字
                break;
            case MotionEvent.ACTION_MOVE:
                mCurPosX = event.getX();
                mCurPosY = event.getY();
                if (Math.abs(mCurPosY - mPosY) > 10 || isScroll) {//滑动触发
                    isScroll = true;
                    LogF.d(TAG, "OnTouch ACTION_MOVE: " + mPosY + "," + mCurPosY + ",message:" + mEtMessage.getText());
                    if (mCurPosY - mPosY <= 0) {

                        //防止一直埋点
                        if (!isScrollUp) {
                            isScrollUp = true;
                        }
                        //触屏向上滑动
                        if (Math.abs(mCurPosY - mPosY) / 6 < 32) {
                            messageSize = String.valueOf(DEFAULT_TEXT_SIZE + (int) Math.abs(mCurPosY - mPosY) / 6);
                        } else {
                            messageSize = "46";
                        }
                    } else {
                        if (!isScrollDown) {
                            isScrollDown = true;
                        }
                        //触屏向下滑动
                        if (Math.abs(mCurPosY - mPosY) / 25 < 9) {
                            messageSize = String.valueOf(DEFAULT_TEXT_SIZE - (int) (Math.abs(mCurPosY - mPosY) / 25));
                        } else {
                            messageSize = "7";
                        }
                    }
                    //一定范围内保持原来默认大小
                    int size = Integer.parseInt(messageSize);
                    if (size <= DEFAULT_TEXT_SIZE + 3 && size >= DEFAULT_TEXT_SIZE - 3) {
                        size = DEFAULT_TEXT_SIZE;
                    } else if (size > DEFAULT_TEXT_SIZE + 3) {
                        size = size - 3;
                    } else if (size < DEFAULT_TEXT_SIZE - 3) {
                        size = size + 3;
                    }
                    messageSize = String.valueOf(size);
                    ani_message.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(messageSize));
                    //隐藏趣图框
                    setGifView(false);
                    fl_ani_panel.setVisibility(View.VISIBLE);
                    int mEmojiconSize = (int) ani_message.getTextSize() + (int) AndroidUtil.dip2px(getContext(), 7);
                    CharSequence builder = EmojiParser.getInstance().replaceAllEmojis(getActivity(), ani_message.getText().toString(), mEmojiconSize);
                    ani_message.setLinkText(builder);
                    ani_message.setSelection(messageContent.length());  //滑动变大，文字显示不全时，选择显示末尾文字

                    if (mPosX - mCurPosX > 100) {//左滑取消
                        fl_ani_panel.setVisibility(View.GONE);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                LogF.d(TAG, "OnTouch: " + mPosX + "," + mCurPosX + "," + mPosY + "," + mCurPosY + ",message:" + mEtMessage.getText());
                if (messageSize != null) {
                    if (messageSize.length() == 1) {
                        messageSize = "0" + messageSize;
                    }
                }
                if (mPosX - mCurPosX > 100) {//左滑取消

                } else if (mEtMessage.getText().length() < 1) {

                } else {
                    //如果是默认大小则置null，兼顾ios默认大小
                    if (TextUtils.equals(messageSize, String.valueOf(DEFAULT_TEXT_SIZE))) {
                        messageSize = null;
                    }
                    sendMessage();
                    mEtMessage.setText("");
                    mIbSend.setEnabled(false);
                    disableDragupSend();
                }
                isScroll = false;
                isScrollDown = false;
                isScrollUp = false;
                messageSize = null;
                messageContent = null;
                ani_message.setText("");
                fl_ani_panel.setVisibility(View.GONE);
                mEtMessage.requestFocus();
                break;
        }
        return false;
    }

    protected MessageChatListAdapter mMessageChatListAdapter;
    protected WrapContentLinearLayoutManager mLinearLayoutManager;
    private BaseChatContract.Presenter mBasePresenter;

    protected boolean mIsFromSearch = false;
    protected String mUserNum;
    protected String mAddress;
    protected String mPerson;
    protected String mStrangerEnterPriseStr;//陌生联系人和我的共同公司名字,只有在单聊的时候会赋值
    protected boolean mIsFromSms;

    protected int mOrignHeight = 0;
    protected int mScreenHeight = 0;
    protected int mInputHeight = 0; // 输入键盘的高度
    protected boolean mIsInputMethodHeightDirty = false;
    protected boolean mIsFirstMoveToEnd = true;//为了解决布局完成后,跳转列表尾部和设置last item的padding时间错乱导致的布局问题
    protected boolean mHasMore = false;
    protected boolean mIsLoadMore = false;
    private boolean mOpenFlag = false;

    public static final int OPEN_VIDEO_REQUEST = 100;
    public static final int OPEN_CAMERA_REQUEST = 101;
    public static final int OPEN_GALLERY_REQUEST = 102;
    public static final int SELECT_AT_MEMBER_REQUEST = 103;
    public static final int VCARD_SEND_REQUEST = 104;
    public static final int SELECT_FILE_REQUEST = 105;
    public static final int OPEN_SMS_REQUEST = 106;
    public static final int PREVIEW_IMAGE_REQUEST = 107;
    public static final int FORWARD_REQUEST_CODE = 108;
    public static final int RESULT_OK = -1;
    public File mCameraPicture;

    private OnScrollListener mOnScrollListener;

    protected int mEmojiconSize;
    private ExpressionFragment mExpressionFragment;
    //    private MessageAudioFragment mMessageAudioFragment;
    // 草稿信息
    private boolean mHasDraftMessage;
    protected boolean mIsFirstTextInit = false;

    protected ChatRemindHelper mChatRemindHelper;

    private boolean isShowAudio = false;

    private boolean hasAddAudioFragment = false;//是否已经添加到activity

    TextView mNightView = null;//蒙版

    private int oldHeight = 0; // 记录上一次的高度

    @Override
    public boolean onFragmentBack() {
        ((Activity) getContext()).finish();
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogF.d(TAG, "BaseChatFragment onCreate");
    }

    @Override
    public void initData() {
        LogF.d(TAG, "BaseChatFragment init");
        mEmojiconSize = (int) mEtMessage.getTextSize() + (int) AndroidUtil.dip2px(getContext(), 3);
        mEtMessage.setMaxHeightByLine(4.5f);
        Bundle bundle = getArguments();
        mAddress = bundle.getString("address");
        mRawId = bundle.getInt("rawId", -1);
        mPerson = bundle.getString("person");
        mIsFromSms = bundle.getBoolean("isFromSms", false);
        mPreCache = bundle.getBoolean("preCache", false);
        mPid = bundle.getInt("pid", 0);

        long loadTime = bundle.getLong("loadtime", 0);

        mIsFromSearch = loadTime > 0; //判断是否来自搜索

        // 新消息提示与跳转处理
        mChatRemindHelper = new ChatRemindHelper();
        if (!mIsFromSearch) {
            mChatRemindHelper.initUnReadCount(bundle);
        }
        //mTvFreeBadge.setVisibility(View.GONE);// 免费短信提醒
        mAccountUserPhone = LoginUtils.getInstance().getLoginUserName();
        if (!AndroidUtil.isCMCCMobileByNumber(NumberUtils.getNumForStore(mAddress)) || !AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone)) {
            //异网
            mTvIsFree.setText(getString(R.string.msg_count));
        } else {
            //本网
            mTvIsFree.setText(getString(R.string.you_are_use_free_msg));
        }

        if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) { // 单聊
            mIbProfile.setImageDrawable(getResources().getDrawable(R.drawable.chat_sms_selector));// 名片换成短信
        }


        initPresenter(bundle);

        mBasePresenter = new BaseChatPresenterImpl(this.getActivity(), this);

        mLinearLayoutManager = new WrapContentLinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mMessageChatListAdapter = new MessageChatListAdapter(getActivity(), mBasePresenter, this);
        mMessageChatListAdapter.setmRecyclerView(mRecyclerView);
        mMessageChatListAdapter.setPreMsg(getArguments().getBoolean("preConversation", false));
        mMessageChatListAdapter.setHasStableIds(true);
        mMessageChatListAdapter.setRawId(mRawId);
        mMessageChatListAdapter.setChatType(getChatType());
        mRecyclerView.setAdapter(mMessageChatListAdapter);
        mRecyclerView.setItemAnimator(null);
        mMessageChatListAdapter.setUnReadCount(mChatRemindHelper.getmFirstUnReadCount());
        mMessageChatListAdapter.setSelectAtMemberCallback(new MessageChatListAdapter.SelectAtCallback() {
            @Override
            public void selectAtMember(GroupMember gm) {
                if (gm != null) {
                    String person = gm.getPerson();
                    int selectionStart = mEtMessage.getSelectionStart();
                    mEtMessage.getText().insert(selectionStart, "@" + person + " ");
                    int end = mEtMessage.getSelectionStart();
                    AtMemberLength atMemberLength = new AtMemberLength(selectionStart, end - 1, gm);
                    ArrayList<AtMemberLength> memberList = getAtMemberLengthList();
                    memberList.add(atMemberLength);
                    mAtInput = true;
                    showKeyboardBySoft();
                }
            }
        });
        mMessageChatListAdapter.setPreviewImagelistener(new MessageChatListAdapter.PreviewImageListener() {
            @Override
            public Bundle previewImage(int position) {
                hideKeyboardAndTryHideGifView();
                int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                int lastPosition = mLinearLayoutManager.findLastVisibleItemPosition();
                int firstIndex = -1;
                ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();
                View childrenView;
                for (int i = firstPosition; i <= lastPosition; i++) {
                    if (i == position) {
                        firstIndex = imageDatas.size();
                    }
                    childrenView = mLinearLayoutManager.findViewByPosition(i);
                    if (childrenView != null) {
                        ImageView imageView = (ImageView) childrenView.findViewById(R.id.imageview_msg_image);
                        if (imageView != null) {
                            int[] location = new int[2];
                            imageView.getLocationOnScreen(location);
                            ImageData imageData = new ImageData(location[0], location[1], imageView.getWidth(), imageView.getHeight());
                            imageDatas.add(imageData);
                        }
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("imageDatas", imageDatas);
                bundle.putInt("firstIndex", firstIndex);
                return bundle;

            }
        });

        mMessageChatListAdapter.setOnCheckChangeListener(this);
        if(isNeedShowStrangerTip()){
            mMessageChatListAdapter.notifyDataSetChanged();
        }

        //        //处理recyclerView滑动时toolbar阴影效果
        scrollForToolbar = new ScrollForToolbar((Toolbar) getActivity().findViewById(R.id.id_toolbar));
        scrollForToolbar.setLineView(getActivity().findViewById(R.id.view_line));
        mRecyclerView.addOnScrollListener(scrollForToolbar);

        initBg();

        initView();
        if (!mIsFromSearch) {
            initDraftData();
        }

        mHandler = new MyHandler(this);
        mBqssHScrollview.setHandler(mHandler);

        //短信模式
        if (ConvCache.getInstance().SMS_STATUS_CACHE.get(mAddress) == Boolean.TRUE || isSpeticalNum(bundle) || mIsFromSms) {
            toSmSLayout(true);
        }

        phone = bundle.getString("address");// phone 的值，有点坑。慎重修改。

        if (isSpeticalNum(phone)) {
            mTvExitSMS.setVisibility(View.GONE);
            mTxt.setVisibility(View.GONE);
        }

        //清除未读数
        clearUnreadCount();
    }

    protected boolean isNeedShowStrangerTip(){
        return false;
    }

    // 匹配特殊的端口号，暂时归类106开头 9开头 10086 10010 10000为端口号长度限制为3位数起最长不超过20位
    public boolean isSpeticalNum(Bundle bundle) {
        String phone = bundle.getString("address");
        if (!TextUtils.isEmpty(phone)) {
            Pattern pattern = Pattern.compile(StringConstant.NUMBER_REGULAR);
            Matcher matcher = pattern.matcher(phone);
            return matcher.matches();
        }
        return false;
    }

    private boolean isSpeticalNum(String phone) {
        if (!TextUtils.isEmpty(phone)) {
            Pattern pattern = Pattern.compile(StringConstant.NUMBER_REGULAR);
            Matcher matcher = pattern.matcher(phone);
            return matcher.matches();
        }
        return false;
    }


    /**
     * 初始化背景和气泡颜色
     */
    private void initBg() {
        mUserNum = LoginDaoImpl.getInstance().queryLoginUser(getContext());
        Bundle bundle = getArguments();
        mAddress = bundle.getString("address");
        colorTypeSet();
    }

    private void initDraftData() {
        // 获取草稿内容
        Bundle bundle = getArguments();
//        Message message = getDraftMessage();
        String draft = bundle.getString("draft");
        if (draft != null) {
            mIsFirstTextInit = true;
            mEtMessage.setText(draft);
            mEtSms.setText(draft);
            //            mHasDraftMessage = true;
            mEtMessage.setSelection(mEtMessage.getText().length());
            mIbSend.setEnabled(true);
        }
    }

    /*
     * 更新草稿信息
     */
    private void updateDraftMessage() {
        if (TextUtils.isEmpty(mAddress)) {
            return;
        }
        Editable text;
        if (mLayoutForMessage.getVisibility() == VISIBLE) {
            text = mEtMessage.getText();
        } else {
            text = mEtSms.getText();
        }
        String content = null;
        if (text != null && !TextUtils.isEmpty(text.toString())) {
            content = text.toString().trim();
        }
        boolean cleanConv = false;
        if (mHasDraftMessage) {
            cleanConv = true;
        }

        Message lastM = null;

        if (mMessageChatListAdapter.getItemRealCount() > 0) {
            lastM = mMessageChatListAdapter.getItem(mMessageChatListAdapter.getItemRealCount() - 1);
        }

        if (!TextUtils.isEmpty(content)) {
            // 草稿插入数据库
            Message draftMessage = new Message();
            draftMessage.setType(Type.TYPE_MSG_TEXT_DRAFT);
            draftMessage.setStatus(Status.STATUS_OK);
            draftMessage.setBody(content);
            if (lastM != null) {
                draftMessage.setDate(lastM.getDate() + 1);
            } else {
                draftMessage.setDate(System.currentTimeMillis());
            }
            draftMessage.setRead(true);
            draftMessage.setSeen(true);
            saveDraftMessage(true, draftMessage);
        } else if (cleanConv) {
            // 草稿为空，还原最新一条会话
            saveDraftMessage(false, null);
        }
    }

    private void checkThemeChange() {
        if ((int) SharePreferenceUtils.getParam(getContext(), MESSAGE_STYLE_CHANGE + mUserNum + mAddress, 0) == 0) {
            colorTypeSet();
            isNeedShowStrangerTip();
            mMessageChatListAdapter.notifyDataSetChanged();
            LogF.d(TAG, "notifyDataSetChanged : ------------ checkThemeChange");
            SharePreferenceUtils.setParam(getContext(), MESSAGE_STYLE_CHANGE + mUserNum + mAddress, -1);
        }
    }

    private int colorTypeSet() {
//        int type = (int) SharePreferenceUtils.getParam(getContext(), MessageModuleConst.MESSAGE_THEME_TYPE + mUserNum + mAddress, MessageBgSetActivity.THEME_0NE);
        int type = 0;
        int drawableId = R.drawable.theme_background_angles;
        switch (type) {
            case MessageBgSetActivity.THEME_0NE:
                drawableId = R.drawable.theme_background_angles;
                type = 0;
                changeMenu(MessageBgSetActivity.THEME_0NE);
                break;
            case MessageBgSetActivity.THEME_TWO:
                drawableId = R.drawable.theme_background_angles;
                type = 1;
                changeMenu(MessageBgSetActivity.THEME_TWO);
                break;
            case MessageBgSetActivity.THEME_THREE:
                drawableId = R.drawable.theme_background_doodles;
                type = 2;
                changeMenu(MessageBgSetActivity.THEME_THREE);
                break;
            case MessageBgSetActivity.THEME_FOUR:
                drawableId = R.drawable.theme_background_forest;
                type = 3;
                changeMenu(MessageBgSetActivity.THEME_FOUR);
                break;
            case MessageBgSetActivity.THEME_FIVE:
                drawableId = R.drawable.theme_background_leaf;
                type = 4;
                changeMenu(MessageBgSetActivity.THEME_FIVE);
                break;
            case MessageBgSetActivity.THEME_SIX:
                drawableId = R.drawable.theme_background_veins;//R.drawable.bitmap_message_theme_five;
                type = 5;
                changeMenu(MessageBgSetActivity.THEME_SIX);
                break;
            case MessageBgSetActivity.THEME_SEVEN:
                drawableId = R.drawable.chat_theme_dazzle_bg;//R.drawable.bitmap_message_theme_seven;
                type = 6;
                changeMenu(MessageBgSetActivity.THEME_SEVEN);
                break;
            case MessageBgSetActivity.THEME_EIGHT:
                drawableId = R.drawable.chat_theme_pink_bg;//R.drawable.bitmap_message_theme_eight;
                type = 7;
                changeMenu(MessageBgSetActivity.THEME_EIGHT);
                break;

        }
        List<Drawable> drawables = MessageThemeUtils.getDrawableFromTheme(getContext(), MessageThemeUtils.THEME_DRAWABLE_NUMBER, type * MessageThemeUtils.THEME_DRAWABLE_NUMBER);
        int colors[] = MessageThemeUtils.getValueFromTheme(getContext(), 1, type * MessageThemeUtils.THEME_COLOR_NUMBER);
        scrollForToolbar.setBackColorId(type);
        ((MessageDetailActivity) getActivity()).getmToolbar().setBackgroundColor(colors[4]);
        ((MessageDetailActivity) getActivity()).getmTvTitle().setTextColor(colors[5]);
        ((MessageDetailActivity) getActivity()).getmIvBack().setImageDrawable(drawables.get(0));
        ((MessageDetailActivity) getActivity()).getmIvSlient().setImageDrawable(drawables.get(5));

        mTvAt.setTextColor(colors[1]);
        mBtMsgATTip.setBackgroundDrawable(drawables.get(11));
        mIbAudio.setImageDrawable(drawables.get(9));
//        ViewGroup.LayoutParams lp = mIvBkg.getLayoutParams();
//        float bkgHeight = AndroidUtil.getScreenHeight(getContext()) - getContext().getResources().getDimension(R.dimen.toolbar_height);
//        lp.width = (int) bkgHeight;
//        mIvBkg.setLayoutParams(lp);
//        mIvBkg.setImageBitmap(BitmapUtils.readBitMap(getContext(), drawableId));
        mIbSend.setImageDrawable(drawables.get(3));
        mIbExpression.setImageDrawable(drawables.get(2));
        mIbExpressionKeyboard.setImageDrawable(drawables.get(4));
        mMessageChatListAdapter.setLeftColorId(colors[0]);
        mMessageChatListAdapter.setRightColorId(colors[1]);
        mMessageChatListAdapter.setLeftTextColor(colors[2]);
        mMessageChatListAdapter.setRightTextColor(colors[3]);
        mMessageChatListAdapter.setNameTextColor(colors[6]);
        mMessageChatListAdapter.setSysTextBackColor(colors[8]);
        mBtMsgCountTip.setTextColor(colors[9]);
        //放大缩小主题设置
        GradientDrawable myAni = (GradientDrawable) fl_ani_panel.getBackground();
        myAni.setColor(colors[1]);
        ani_message.setTextColor(colors[3]);
        mChatRemindHelper.hideMessageCountTip();
        return type;
    }

    @Override
    public void onResume() {
        LogF.d(TAG, "BaseChatFragment onResume");
        checkThemeChange();
        initBoardsState();
        //从GifSendActivity返回，需要重置键盘模式。
        Editable text = mEtMessage.getText();
        if (text != null && !TextUtils.isEmpty(text.toString())) {
            mHasDraftMessage = true;
        }

        super.onResume();
    }

    private void initBoardsState() {
        if (!TextUtils.isEmpty(mEtMessage.getText()) && isShowAudio == false) {
            if (mIbExpressionKeyboard != null && mIbExpressionKeyboard.getVisibility() == View.VISIBLE) {
                mEtMessage.requestFocus();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideKeyboardAndTryHideGifView();
                    }
                }, 120);
            } else {
                mEtMessage.requestFocus();
                showKeyboard();
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        } else {
            mEtMessage.clearFocus();
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        MsgNotificationReceiver.setCurrentAddress(bundle.getString("address"));
    }

    @Override
    public void onPause() {
        super.onPause();
        new RxAsyncHelper<>("").runInSingleFixThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {
                SharePreferenceUtils.setParam(getActivity(), PREF_KEY_INPUT_METHOD_HEIGHT, mInputHeight);
                return null;
            }
        }).subscribe();
        hideKeyboardAndTryHideGifView();
    }

    @Override
    public void onStop() {
        updateDraftMessage();
        //清除未读数
        clearUnreadCount();
        MsgNotificationReceiver.clearCurrentAddress();
        super.onStop();
    }

    int lastEditTextLine = 1;//输入框的行数
    int lastEditTextLineSms = 1;//输入框的行数 sms

    public void initView() {
        StatusBarCompat.setStatusBarColor(getActivity(), getResources().getColor(R.color.color_2c2c2c));

        mEtMessage.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mEtMessage.getText().length() < 1) {
                        if (!(getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT)) {
                            mIbSend.setVisibility(View.GONE);
                            setAudioBtnVisibility(View.VISIBLE);
                        }
                    }
                    chatRichMediaVp.setVisibility(View.GONE);
                    mIbMore.setImageResource(R.drawable.cc_chat_ic_input_more);
                } else {
                    // 此处为失去焦点时的处理内容
                }
            }
        });


        mEtSms.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    mIbSmsSend.setEnabled(false);
                    mIbSmsSend.setImageDrawable(getResources().getDrawable(R.drawable.chat_send_grey));
                } else {
                    mIbSmsSend.setEnabled(true);
                    mIbSmsSend.setImageDrawable(getResources().getDrawable(R.drawable.send_selector));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //输入框在不同行数显示不同高度
                int lineCount = mEtSms.getLineCount();
                if (lastEditTextLineSms != lineCount) {
                    ViewGroup.LayoutParams params = layoutSmsPanel.getLayoutParams();
                    if (lineCount == 1) {
                        params.height = (int) AndroidUtil.dip2px(getActivity(), 48);
                    } else if (lineCount <= 4) {
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    } else {
                        if (lastEditTextLineSms <= 4) {
                            int singleLineHigh = (int) AndroidUtil.dip2px(getActivity(), 24);
                            params.height = singleLineHigh * 4 + singleLineHigh / 2;
                        }
                    }
                    layoutSmsPanel.setLayoutParams(params);
                    lastEditTextLineSms = lineCount;
                }
            }
        });


        mEtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                LogF.d(TAG, "beforeTextChanged s:"+s);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LogF.d(TAG, "onTextChanged000");
                //                if (isGifViewShow && AndroidUtil.isNetworkConnected(getContext()) && !TextUtils.isEmpty(s)) {
                //                    mBqssHScrollview.setKeyword(s.toString().trim());
                //                    mBqssHScrollview.loadMore();
                //                }
                if (!TextUtils.isEmpty(s)) {
                    mIbSend.setEnabled(true);
                    mIbSend.setVisibility(View.VISIBLE);
                    setAudioBtnVisibility(View.GONE);
                } else {
                    mIbSend.setEnabled(false);
                    if (getChatType() !=  MessageModuleConst.MessageChatListAdapter.TYPE_SMSMMS_SINGLE_CHAT && !(getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT) //公众号会话屏蔽麦克风
                            && isShowAudio == false) {
                        mIbSend.setVisibility(View.GONE);
                        setAudioBtnVisibility(View.VISIBLE);
                        if (mRecordExitType == RECORD_STOP_INTERRUPT_TYPE) {
                            mRecordExitType = RECORD_STOP_EXIT_TYPE;
                        }
                        MessageProxy.g.getUiInterface().onSendToFragmentPageEvent(mMessageAudioTextFragment, MessageModuleConst.BASE_CHAT_FRAGMENT_ETMESSAGE_TEXT_EMPTY);
                    }
                }
                if (s != null && s.length() >= 1 && count > 0) {
                    EmojiParser.getInstance().replaceEmoji(getActivity(), mEtMessage.getText(), mEmojiconSize, start, before, count);
                }

                if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    if (s != null && s.length() >= 1 && count > 0) {
                        char c;
                        if (count > 1) {
                            c = s.charAt(s.length() - 1);
                        } else {
                            c = s.charAt(start);
                        }
                        String temp = String.valueOf(c);
                        LogF.d(TAG, "onTextChanged temp:" + temp);
                        if (!mIsFirstTextInit && !TextUtils.isEmpty(temp) && isInsertAtPerson(start, before, count)) {//在@人之间输入字符
                            return;
                        }
                        if (!mIsFirstTextInit && !TextUtils.isEmpty(temp) && temp.equals("@") && mIsAtAll == false) {
                            //企业群显示电话，非企业群不显示
//                            Intent intent = ContactsSelectActivity.createIntentForGroupAt(getActivity(), mAddress,
//                                    isEPGroup || isPartyGroup, getArguments().getInt(ContactsSelectActivity.GROUP_TYPE)
//                                    , isEPGroup || isPartyGroup, isOwner);

                            Intent intent = ContactSelectorActivity.creatIntent(getActivity(), SOURCE_AT_GROUP_MEMBER, 1);
                            intent.putExtra("my_num", LoginDaoImpl.getInstance().queryLoginUser(getActivity()));
                            intent.putExtra(GROUP_CHAT_ID, mAddress);
                            intent.putExtra(GROUP_TYPE,getArguments().getInt(GROUP_TYPE));
                            intent.putExtra("isShowNumber", isEPGroup || isPartyGroup);
                            intent.putExtra(GROUP_OWNER, isOwner);
                            intent.putExtra(IS_EP_GROUP, isEPGroup || isPartyGroup);
                            startActivityForResult(intent, SELECT_AT_MEMBER_REQUEST);
                        }
                        mIsFirstTextInit = false;
                    } else if (count == 0) {
                        isInsertAtPerson(start, before, count);//处理@人
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                LogF.d(TAG, "afterTextChanged000");
                if (justSend) {
                    justSend = false;
                    return;
                }

                if (s.toString().equals("")) {
                    mIbSend.setEnabled(false);

                    if (mHasDraftMessage) {//处理@人跳转到联系人选择器时，@被保存到草稿，然后清除文本返回到会话列表，会显示草稿的提示
                        updateDraftMessage();
                        mHasDraftMessage = false;
                    }
                } else {
                    mIbSend.setEnabled(true);
                }

                //输入框在不同行数显示不同高度
                int lineCount = mEtMessage.getLineCount();
                if (lastEditTextLine != lineCount) {
                    ViewGroup.LayoutParams params = mKeyboardPanel.getLayoutParams();
                    if (lineCount == 1) {
                        params.height = (int) AndroidUtil.dip2px(getActivity(), 43);
                    } else if (lineCount <= 4) {
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    } else {
                        if (lastEditTextLine <= 4) {
                            int singleLineHigh = (int) AndroidUtil.dip2px(getActivity(), 24);
                            params.height = singleLineHigh * 4 + singleLineHigh / 2;
                        }
                    }
                    mKeyboardPanel.setLayoutParams(params);
                    lastEditTextLine = lineCount;
                }
            }
        });

        mEtMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (isShowAudio) {
                            isShowAudio = false;
                            showKeyboardBySoft();
                            if (mMessageAudioTextFragment != null) {
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                if (fragmentManager.findFragmentByTag("audio_fragment") != null || hasAddAudioFragment) {
                                    //   transaction.show(mMessageAudioFragment);
                                    transaction.hide(mMessageAudioTextFragment);
//                                    hasAddAudioFragment = false;
                                }
                            }

                            mRichPanel.setVisibility(View.VISIBLE);
//                            mIbExpressionKeyboard.setVisibility(View.VISIBLE);
                            mIbExpression.setVisibility(View.VISIBLE);
                            if (mRecordExitType == RECORD_IS_NOMRAL) {
                                MessageProxy.g.getUiInterface().onSendToFragmentPageEvent(mMessageAudioTextFragment, MessageModuleConst.BASE_CHAT_FRAGMENT_ETMESSAGE_FOCUSE);
                            }
                            break;
                        }
                        if (mFlMore.getVisibility() != View.VISIBLE) {
                            mEtMessage.requestFocus();
                            showKeyboardBySoft();
                        } else hideMorePanel();

                        break;

                }
                return false;
            }
        });

        RxTextView.textChanges(mEtMessage).debounce(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharSequence>() {

                    @Override
                    public void call(CharSequence charSequence) {
                        if (isGifViewShow && AndroidUtil.isNetworkConnected(getContext())) {
                            mBqssHScrollview.retSetCurrentCount();
                            mBqssHScrollview.retSetCurrentPage();
                            mNeedLoadMore = true;
                            if (!TextUtils.isEmpty(charSequence)) {
                                mBqssHScrollview.setKeyword(charSequence.toString().trim());
                            } else {
                                mBqssHScrollview.setKeyword(BQSSConstants.TRENDING_STICKER_TAG);
                            }
                            mBqssHScrollview.loadMore();
                        }
                    }
                });

        mInputLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mRecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
//                        if (mAtInput || mIsLoadMore || (mMessageAudioFragment != null && mMessageAudioFragment.isStartAudio())) {
//                            return true;
//                        }

                        if (mAtInput || mIsLoadMore || (mRecordExitType == RECORD_IS_NOMRAL)) {
                            return true;
                        }
                        //滚动关闭富媒体
                        if (chatRichMediaVp.getVisibility() == View.VISIBLE) {
                            mOpenFlag = false;
                            mIsAnimating = true;
                            animateClose(chatRichMediaVp);
                        }
                        mEtMessage.clearFocus();
                        hideKeyboard();
                        mFlMore.setVisibility(View.GONE);
                        mIbExpressionKeyboard.setVisibility(View.GONE);
                        if (!(getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SMSMMS_SINGLE_CHAT)) {
                            mIbExpression.setVisibility(View.VISIBLE);
                        }
                        if (isShowAudio) {
                            isShowAudio = false;
                            mIbSend.setVisibility(View.GONE);
                            setAudioBtnVisibility(View.VISIBLE);
                            mRichPanel.setVisibility(View.VISIBLE);
//                            mIbExpressionKeyboard.setVisibility(View.VISIBLE);
                            mIbExpression.setVisibility(View.VISIBLE);
                        }
                        //点击聊天空白处，不改变发送录音按钮状态
//                        mIbSend.setVisibility(View.GONE);
//                        setAudioBtnVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        mAtInput = false;
                }
                return false;
            }
        });
        //布局发生变化回调
        mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mLayout == null) {
                    return;
                }
                int currentScreenHeight = mLayout.getHeight();
                if (mScreenHeight == 0) {
                    mScreenHeight = mLayout.getHeight(); // 全屏的高度
                }

                if (AndroidUtil.checkDeviceHasNavigationBar(getContext())) { //带虚拟按键的手机
                    if (currentScreenHeight != mScreenHeight && currentScreenHeight != (mScreenHeight - mInputHeight)) {
                        if ((mScreenHeight - currentScreenHeight <= AndroidUtil.dip2px(getActivity(), 73)) || (Math.abs(oldHeight - (mScreenHeight - currentScreenHeight)) <= AndroidUtil.dip2px(getActivity(), 73))) {
                            // 一开始虚拟按键显示，然后影藏虚拟按键这样就 小于0 ,
                            // 一开始虚拟按键不显示，然后显示这样就是 小于220
                            // 表情栏显示下，虚拟按键显示和不显示切换 小于220
                            // 条件二
                            // 输入软键盘显示下，虚拟按键显示和不显示切换 小于220
                            LogF.d("TAG", "小于等于220");
                        } else {
                            mInputHeight = mScreenHeight - currentScreenHeight;
                            oldHeight = mInputHeight;
                            mIsInputMethodHeightDirty = true;
                            setPanelHeight(mInputHeight);
                        }
                    }
                } else {
                    //不等于屏幕高度或者记录的键盘高度，不等于屏幕高度或者记录的键盘高度说明键盘高度发生了变化，需要更新记录的高度  // 原来版本的代码 只有这个if语句
                    if (currentScreenHeight != mScreenHeight && currentScreenHeight != (mScreenHeight - mInputHeight) && (mScreenHeight - currentScreenHeight > AndroidUtil.dip2px(getActivity(), 73))) {
                        mInputHeight = mScreenHeight - currentScreenHeight;
                        mIsInputMethodHeightDirty = true;
                        setPanelHeight(mInputHeight);
                    }
                }

                int currentHeight = mRecyclerView.computeVerticalScrollExtent();
                if (mOrignHeight == 0) {
                    mOrignHeight = currentHeight;
                }

                //多选模式下，不要滚动到消息底部
                if (currentHeight != mOrignHeight && !mMessageChatListAdapter.getIsMultiSelectMode()) {
                    moveToEnd();
                    mOrignHeight = currentHeight;
                }

                //最大字体

//                mMessageChatListAdapter.setLastItemPadding(mLlTextInput.getMeasuredHeight() + (mBqssHScrollview.getVisibility() == View.VISIBLE ? mBqssHScrollview.getMeasuredHeight() : 0), mBqssHScrollview.getVisibility() == View.VISIBLE || isScroll);
//                if (mIsFirstMoveToEnd) {
//                    Log.d("tigger", "------move to end-----");
//                    mIsFirstMoveToEnd = false;
//                    moveToEnd();
//                }



                // recycleview显示完成后，更新新消息提示语
                mChatRemindHelper.updateChatRemindWhenLayout();

                if (mLinearLayoutManager.findFirstVisibleItemPosition() == 0 && mHasMore) {
                    if (!mIsLoadMore) {
                        onLoadMore();
                        LogF.d(TAG, "-------LoadMore when onGlobalLayout");
                    }
                }
            }
        };
        mLayout.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            private int firstVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 当不滚动时
                if (newState == SCROLL_STATE_IDLE) {
                    if (firstVisibleItem == 0 && mHasMore) {
                        //加载更多功能的代码
                        if (mIsLoadMore == false) {
                            onLoadMore();
                            LogF.d(TAG, "-------LoadMore");
                            return;
                        }
                    }
                }
                LogF.d(TAG, "-------onScrollStateChanged");

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                mChatRemindHelper.updateChatRemindWhenScroll(dy);
            }
        }
        ;

        mRecyclerView.addOnScrollListener(mOnScrollListener);

        setHasOptionsMenu(true);

        mIbSend.setEnabled(false);

        mInputHeight = (int) SharePreferenceUtils.getParam(this.getActivity(), PREF_KEY_INPUT_METHOD_HEIGHT, 0);

        setPanelHeight(mInputHeight);

        // ysf
        if (mInputHeight != 0) {
            oldHeight = mInputHeight;
        }
    }

    /*
     * 语音发送时显示的蒙板
     * */
    public void night() {
        WindowManager mWindowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        int screenHeight = mScreenHeight == 0 ? mLayout.getHeight() : mScreenHeight;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LayoutParams.FILL_PARENT, screenHeight - (int) AndroidUtil.dip2px(getActivity(), mInputHeight) + getActivity().findViewById(R.id.id_toolbar).getHeight(), WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        lp.gravity = Gravity.TOP;// 可以自定义显示的位置
        if (mNightView == null) {
            mNightView = new TextView(getActivity());
            mNightView.setId(R.id.text1);
            mNightView.setBackgroundColor(getResources().getColor(R.color.color_66000000));
            mNightView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mNightView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
        try {
            mWindowManager.addView(mNightView, lp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 隐藏语音的蒙板
     */
    private void hideNight() {
        if (mNightView == null) {
            return;
        }
        WindowManager mWindowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        try {
            mWindowManager.removeView(mNightView);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void onFirstLoadDone(int searchPos, ArrayList<? extends BaseModel> list, boolean hasMore) {

        mMessageChatListAdapter.setSourceDataForMessageOnly(null, list);
        if (hasMore) {
            //            mMessageChatListAdapter.setCanNotLoadMore();
            LogF.d(TAG, "setCanLoadMore mIsFromSearch " + mIsFromSearch);
            mMessageChatListAdapter.setCanLoadMore();
            mHasMore = true;
        } else {
            searchPos = searchPos - 1;
        }
        isNeedShowStrangerTip();
        mMessageChatListAdapter.notifyDataSetChanged();
        LogF.d(TAG, "notifyDataSetChanged : ------------ onFirstLoadDone");
        if (!mIsFromSearch) {
            moveToEnd();
        } else {
            LogF.d(TAG, "onFirstLoadDone mIsFromSearch " + mIsFromSearch + "  searchPos " + searchPos);
            moveToSearchPos(searchPos);
        }
//        Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "----onFirstLoadDone over----");
    }

    public void onNormalLoadDone(ArrayList<? extends BaseModel> list, boolean isAddMessage) {
        ArrayList<? extends BaseModel> oldList = (ArrayList<? extends BaseModel>) mMessageChatListAdapter.getDataList();
        boolean shouldToEnd = false;
        int addCount = 0;
        Message message = null;
        boolean isCard = false;
        //最新一条信息为接受名片,调整listview位置
        if (list != null && list.size() > 0) {
            if (oldList != null && oldList.size() > 0) {
                Message oldMessage = (Message) oldList.get(oldList.size() - 1);
                message = (Message) list.get(list.size() - 1);
                if (message.getId() == oldMessage.getId() && oldMessage.getStatus() != message.getStatus()) {
                    isCard = true;
                }
            }
        }

        if (isAddMessage || isCard) {
            addCount = list.size() + mChatRemindHelper.getChatRemindMsgCount() - mMessageChatListAdapter.getItemRealCount();
            if (mLinearLayoutManager.findLastVisibleItemPosition() == mLinearLayoutManager.getItemCount() - 1) {
                shouldToEnd = true;
            }
            if (addCount > 0) {
                int num = addCount;
                if (list != null && list.size() > 0) {
                    for (int i = 1; i <= num; i++) {
                        message = (Message) list.get(list.size() - i);
                        boolean isLeft = (message.getType() & Type.TYPE_RECV) > 0;
                        if (!isLeft) {
                            addCount--;
                            shouldToEnd = true; //如果是发送的消息，直接跳转到底部
                        }
                    }
                }
            }
        }

        //判断最后一条消息的类型切换短信状态
        if (isAddMessage && list != null && list.size() > 0) {
            BaseModel model = list.get(list.size() - 1);
            if (model instanceof Message) {
                Message m = (Message) model;
                if ((m.getType() & Type.TYPE_MSG_SMS) != Type.TYPE_MSG_SMS && (m.getType() & Type.TYPE_MSG_MMS) != Type.TYPE_MSG_MMS && (m.getType() & Type.TYPE_MSG_SUPER_SMS) != Type.TYPE_MSG_SUPER_SMS) {
                    if(!isSMSMode){
                        toSmSLayout(false);
                    }
                }
            }
        }

        mChatRemindHelper.updateChatRemindWhenNewMsgCome(false, addCount);
        mMessageChatListAdapter.setSourceDataForMessageOnly(null, list);
        mChatRemindHelper.updateChatRemindWhenNewMsgCome(true, addCount);
        isNeedShowStrangerTip();
        mMessageChatListAdapter.notifyDataSetChanged();
        LogF.d(TAG, "notifyDataSetChanged : ------------ onNormalLoadDone");
        if (shouldToEnd) moveToEnd();
    }

    public void onLoadMoreDone(ArrayList<? extends BaseModel> list, final int addNum) {
        if (list == null) {
            LogF.d(TAG, "onLoadMoreDone list is null");
            mMessageChatListAdapter.setCanNotLoadMore();
            mHasMore = false;
            mIsLoadMore = false;
            if (mEtMessage != null) {
                mEtMessage.setEnabled(true);
            }
            return;
        }

        //多选模式的时候加载更多消息，需要对已选列表进行更新
        if(mMessageChatListAdapter.getIsMultiSelectMode()){
            SparseBooleanArray selectList = mMessageChatListAdapter.getSelectedList();
            SparseBooleanArray oldSelectList = selectList.clone();
            selectList.clear();
            if(oldSelectList.size() != 0){
                for(int i = 0 ; i < oldSelectList.size(); i++){
                    int position = oldSelectList.keyAt(i);
                    //没有更多的时候，position - 1
                    if(addNum < MessageEditorModelImpl.NUM_LOAD_ONE_TIME){
                        selectList.put(position + addNum - 1 , true);
                    }else{
                        selectList.put(position + addNum , true);
                    }
                }
            }
        }

        mMessageChatListAdapter.setSourceDataForMessageOnly(new OnSetCursorDoneListener() {
            @Override
            public void onSetCursorDone() {
                mMessageChatListAdapter.setCanNotLoadMore();
                mChatRemindHelper.updateChatRemindWhenOldMsgLoaded(addNum);
                mMessageChatListAdapter.notifyItemRangeInserted(0, addNum);

                if (addNum < MessageEditorModelImpl.NUM_LOAD_ONE_TIME) {
                    mHasMore = false;
                } else {
                    mMessageChatListAdapter.setCanLoadMore();
                }
                mIsLoadMore = false;
                if (mEtMessage != null) {
                    mEtMessage.setEnabled(true);
                }
            }

        }, list);
    }

    public void onLoadMore() {
        mIsLoadMore = true;
        if (mEtMessage != null) {
            mEtMessage.setEnabled(false);
        }
        mLinearLayoutManager.scrollToPosition(0);
        // 原始数据的数目需要去除掉插入的“新消息提示”项
        loadMoreMessages();
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.ib_send) {
            if (getChatType() !=  MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT && getChatType() !=  MessageModuleConst.MessageChatListAdapter.TYPE_MASS
                    && (mRecordExitType != RECORD_IS_NOMRAL && mRecordExitType!=RECORD_STOP_INTERRUPT_TYPE)) {
                fl_ani_panel.setVisibility(View.VISIBLE);
            }
        }
        return false;
    }

    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.ib_send) {
            if (isShowAudio || mRecordExitType == RECORD_STOP_INTERRUPT_TYPE) {
                // isShowAudio = false;
                //  showKeyboardBySoft();
                MessageProxy.g.getUiInterface().onSendToFragmentPageEvent(mMessageAudioTextFragment, MessageModuleConst.BASE_CHAT_FRAGMENT_SEND_BTN_CLICK);
                if (isGroupChat()) {
                    UmengUtil.buryPoint(mContext, "groupmessage_talk_send", "消息-群聊-语音-发送", 0);
                } else {
                    UmengUtil.buryPoint(mContext, "p2pmessage_talk_send", "消息-点对点会话-语音-发送", 0);
                }
                onSendClickReport(SEND_AUDIO_TYPE);
//                mRecordExitType =RECORD_IS_NOMRAL;
                return;
            }
            if (TextUtils.isEmpty(mEtMessage.getText().toString())) {
                return;
            }
            onSendClickReport(SEND_TXT_TYPE);
            sendMessage();
            mEtMessage.setText("");
            mIbSend.setEnabled(false);

        } else if (viewId == R.id.ib_more) {
            rmRedTipDisappear();
            if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                UmengUtil.buryPoint(getActivity(), "message_groupmessage_more", "更多", 0);
            } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                UmengUtil.buryPoint(getActivity(), "message_p2pmessage_more", "加号", 0);
            }
            //隐藏表情栏
            if (mFlMore.getVisibility() == View.VISIBLE) {
                mFlMore.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFlMore.setVisibility(View.GONE); // 表情栏隐藏
                    }
                }, 200);

                mIbExpressionKeyboard.setVisibility(View.GONE); //  输入键盘图标按钮
                mIbExpression.setVisibility(View.VISIBLE); // 表情图标按钮
            }

            if (mIsAnimating) return;
            if(gridViews == null){
                initRichMediaData();
            }
            if (chatRichMediaVp.getVisibility() == View.VISIBLE) {
                mOpenFlag = false;
                mIsAnimating = true;
                animateClose(chatRichMediaVp);
            } else {//打开
                mEtMessage.clearFocus();
                hideKeyboard();
                mOpenFlag = true;
                mIsAnimating = true;
                if (richMediaData != null && richMediaData.length > 4) {
                    if(richMediaDataTwo!=null){
                        animateOpen(chatRichMediaVp, (int) (AndroidUtil.dip2px(getContext(), 208) + 0.5));// 84
                    }else{
                        animateOpen(chatRichMediaVp, (int) (AndroidUtil.dip2px(getContext(), 198) + 0.5));// 84
                    }
                } else {
                    animateOpen(chatRichMediaVp, (int) (AndroidUtil.dip2px(getContext(), 106) + 0.5));// 84
                }
            }

        } else if (viewId == R.id.tv_exitsms) {
            UmengUtil.buryPoint(mContext, "p2pmessage_sms_quit", "消息-点对点会话-加号-免费短信-退出短信", 0);
            boolean hasFocus = false;
            if (mEtSms.hasFocus()) {
                hasFocus = true;
            }
            mEtMessage.setText(mEtSms.getText().toString());
            toSmSLayout(false);
            if (hasFocus) {
                mEtMessage.requestFocus();
                mEtMessage.setSelection(mEtMessage.getText().length());
            }

        } else if (viewId == R.id.ib_sms_send) { // 发送短信
            UmengUtil.buryPoint(mContext, "p2pmessage_sms_send", "消息-点对点会话-加号-免费短信-发送按钮", 0);
            if (AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone) && !PhoneUtils.isNotChinaNum(mAccountUserPhone)) {//只有移动号码并且是大陆号码才调用菊风的接口发短信
                String s = mEtSms.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    sendSmsMessage(s);
//                    mEtSms.setText("");
                }
            } else { // 香港号码和非移动用户号码调用系统短信的功能
                String s = mEtSms.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    sendSysSmsMessage(s);
                    mEtSms.setText("");
                }
            }
        } else if (viewId == R.id.iv_cancel_gif) {
            setGifView(false);
        } else if (viewId == R.id.ib_gif) { // 点击 gif 的itme
            if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {   //单聊
                UmengUtil.buryPoint(v.getContext(), "message_p2pmessage_GIF", "消息-点对点会话-GIF", 0);
            } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {    //群聊
                UmengUtil.buryPoint(v.getContext(), "message_groupmessage_GIF", "消息-群聊-GIF", 0);
            }

            if (!AndroidUtil.isNetworkConnected(getContext())) {
                BaseToast.show(R.string.network_disconnect);
                return;
            }
            setGifView(true);
            mNeedLoadMore = true;
            mBqssHScrollview.retSetCurrentCount();
            mBqssHScrollview.retSetCurrentPage();
            hideMorePanel();
            if (mBqssHScrollview != null) {
                String s = mEtMessage.getText().toString();
                if (!s.isEmpty()) {
                    mBqssHScrollview.setKeyword(s.trim());
                } else {
                    mBqssHScrollview.setKeyword(BQSSConstants.TRENDING_STICKER_TAG);
                }
            }
            mBqssHScrollview.loadMore();
        } else {
            if (viewId == R.id.ib_pic) {//输入框上方一排按钮，点击进入相册
                if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    UmengUtil.buryPoint(getActivity(), "message_groupmessage_photos", "相片", 0);
                } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                    UmengUtil.buryPoint(getActivity(), "message_p2pmessage_photos", "相片", 0);
                }
                if (!NoDoubleClickUtils.isDoubleClick()) {

                    Intent intent = new Intent(getActivity(), GalleryActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("address", getArguments().getString("address"));
                    intent.putExtras(bundle);
                    startActivityForResult(intent, OPEN_GALLERY_REQUEST);
                }
            } else if (viewId == R.id.ib_take_photo) {
                if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    UmengUtil.buryPoint(getActivity(), "message_groupmessage_camera", "拍照", 0);
                } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                    UmengUtil.buryPoint(getActivity(), "message_p2pmessage_camera", "拍照", 0);
                }

                //如果在视频通话，则不能使用拍照
                if (IPCUtils.getInstance().isMergeCall() && IPCUtils.getInstance().isMergeCall()) {
                    CommonUtils.showToast(getString(R.string.video_call_ing), getActivity());
                    return;
                }
                if (IPCUtils.getInstance().isMutilVideoCall()) {
                    CommonUtils.showToast(getString(R.string.multi_video_ing), getActivity());
                    return;
                }
                ((BaseActivity) getActivity()).requestPermissions(new BaseActivity.OnPermissionResultListener() {

                    @Override
                    public void onAllGranted() {
                        super.onAllGranted();
                        startVideo();
                    }

                    @Override
                    public void onAnyDenied(String[] permissions) {
                        BaseToast.makeText(getActivity(), getActivity().getString(R.string.need_voice_pic_permission), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAlwaysDenied(String[] permissions) {
                        String message = getActivity().getString(R.string.need_voice_pic_permission);
                        PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
                        permissionDeniedDialog.show();
                    }
                }, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
            } else if (viewId == R.id.ib_expression) { // 点击表情栏

                if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    UmengUtil.buryPoint(getActivity(), "message_groupmessage_emoticon", "表情", 0);
                } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                    UmengUtil.buryPoint(getActivity(), "message_p2pmessage_emoticon", "表情", 0);
                }
                if (!(getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT)) {
                    setGifView(false); //关闭gif趣图
                }

                isShowAudio = false;
//                if(mMessageAudioTextFragment !=null) {
//                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                    FragmentTransaction transaction = fragmentManager.beginTransaction();
//                    if (fragmentManager.findFragmentByTag("audio_fragment") != null || hasAddAudioFragment) {
//                        //   transaction.show(mMessageAudioFragment);
//                        transaction.remove(mMessageAudioTextFragment);
//                        hasAddAudioFragment = false;
//                    }
//                }

                //关闭富媒体栏
                if (chatRichMediaVp.getVisibility() == View.VISIBLE) {
                    mOpenFlag = false;
                    mIsAnimating = true;
                    animateClose(chatRichMediaVp);
                }

                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                hideKeyboardAndTryHideGifView(); // 隐藏输入键盘和趣图
                setPanelHeight(mInputHeight);  // 设置表情栏的高度
                mIbExpressionKeyboard.setVisibility(View.VISIBLE); // 输入键盘图标
                mIbExpression.setVisibility(View.GONE); // 表情栏图标
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                if (mExpressionFragment == null) {
                    mExpressionFragment = new ExpressionFragment();
                    ExpressionPresenter presenter = new ExpressionPresenter(getActivity());
                    mExpressionFragment.setPresenter(presenter);
                    presenter.setView(mExpressionFragment);
                    mExpressionFragment.setListener(new ExpressionFragment.setResultToMessageEditorFragment() {
                        @Override
                        public void getResultFrofragment(String msg) {
                            if (msg != null) {
                                mEtMessage.requestFocus();
                                int start = mEtMessage.getSelectionStart();
                                if (start < 0) {
                                    mEtMessage.append(msg);
                                } else {
                                    mEtMessage.getText().insert(start, msg);
                                }
                            }
                        }

                        @Override
                        public void deleteTextButton() {
                            mEtMessage.onKeyDown(KeyEvent.KEYCODE_DEL, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                        }
                    });
                    mExpressionFragment.setGifSendListener(new ExpressionFragment.startGetGif() {
                        @Override
                        public void startGetGif() {
                            if (!AndroidUtil.isNetworkConnected(getContext())) {
                                BaseToast.show(R.string.network_disconnect);
                                return;
                            }
                            setGifView(true);
                            mNeedLoadMore = true;
                            mBqssHScrollview.retSetCurrentCount();
                            mBqssHScrollview.retSetCurrentPage();
                            hideMorePanel();
                            if (mBqssHScrollview != null) {
                                String s = mEtMessage.getText().toString();
                                if (s != null) {
                                    if (!s.isEmpty()) {
                                        mBqssHScrollview.setKeyword(s.trim());
                                    } else {
                                        mBqssHScrollview.setKeyword(BQSSConstants.TRENDING_STICKER_TAG);
                                    }
                                }
                            }
                            mBqssHScrollview.loadMore();
                        }
                    });
                    fragmentTransaction.add(R.id.fl_more, mExpressionFragment, "expression_fragment");
                }
//                if (mMessageAudioFragment != null && manager.findFragmentByTag("audio_fragment") != null) {
//                    fragmentTransaction.hide(mMessageAudioFragment);
//                }
                if (mMessageAudioTextFragment != null && manager.findFragmentByTag("audio_fragment") != null) {
                    fragmentTransaction.hide(mMessageAudioTextFragment);
                }
                if (mFlMore.getBackground() == null) {
                    mFlMore.setBackgroundColor(getResources().getColor(R.color.color_f5f5f5));
                }
//                if (manager.findFragmentByTag("expression_fragment") != null) {
//                    fragmentTransaction.show(mExpressionFragment);
//                } else {
//                    fragmentTransaction.add(R.id.fl_more, mExpressionFragment, "expression_fragment");
//                }
                fragmentTransaction.show(mExpressionFragment);

                fragmentTransaction.commitAllowingStateLoss();
                mFlMore.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    }
                }, 200);
            } else if (viewId == R.id.rl_message_count_tip || viewId == R.id.rl_message_count_tip1) {
                mChatRemindHelper.jumpToPosition();
            } else if (viewId == R.id.message_at_tip) {
                mChatRemindHelper.jumpToATPosition();
            } else if (viewId == R.id.ib_profile) { // 名片（位置）菜单
                if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) { // 单聊时点击该位置 -> 短信
                    mEtSms.setText(mEtMessage.getText().toString());
                    mFlMore.setVisibility(View.GONE); // 超级短信显示前，要把表情栏隐藏
                    if (!AndroidUtil.isCMCCMobileByNumber(mAddress) || !AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone)) {
                        boolean isFirstIn = (boolean) SharePreferenceUtils.getDBParam(getContext(), SuperMsgActivity.SP_KEY_FIRST_SUPER_MSG, true);
                        LogF.d(TAG, "isfirstin = " + isFirstIn);
                        if (isFirstIn) {
                            startActivityForResult(SuperMsgActivity.createIntentForSuperMsgSetting(getActivity(), 1), OPEN_SMS_REQUEST);
                        } else {
                            final ToastDialog dialog = new ToastDialog(getActivity());
                            dialog.setText(getResources().getString(R.string.into_msg));
                            dialog.show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    toSmSLayout(true);
                                    dialog.dismiss();
                                }
                            }, 400);

                        }
                    } else {
                        LimitedUserControl.limitedUserDialog(mContext, LoginUtils.getInstance().isLimitUser(), LimitedUserControl.FREE_MSG, new LimitedUserControl.OnLimitedUserLinster() {
                            @Override
                            public void onLimitedOperation() {
                            }

                            @Override
                            public void onNormalOperation() {
                                boolean isFirstIn = (boolean) SharePreferenceUtils.getDBParam(getContext(), SuperMsgActivity.SP_KEY_FIRST_SUPER_MSG, true);
                                LogF.d(TAG, "isfirstin = " + isFirstIn);
                                if (isFirstIn) {
                                    startActivityForResult(SuperMsgActivity.createIntentForSuperMsgSetting(getActivity(), 1), OPEN_SMS_REQUEST);
                                } else {
                                    final ToastDialog dialog = new ToastDialog(getActivity());
                                    dialog.setText(getResources().getString(R.string.into_free_msg));
                                    dialog.show();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            toSmSLayout(true);
                                            dialog.dismiss();
                                        }
                                    }, 400);

                                }
                            }
                        });
                    }
                } else { // 不是单聊就保留原来的功能
                    if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                        UmengUtil.buryPoint(getActivity(), "message_groupmessage_contactcard", "名片", 0);
                    } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                        UmengUtil.buryPoint(getActivity(), "message_p2pmessage_contactcard", "名片", 0);
                    }
                    Intent intent = ContactSelectorActivity.creatIntent(getActivity(), SOURCE_VCARD_SEND, 1);
                    intent.putExtra(VCARD_SELECT_CONTACT, VCARD_EXPORT);
                    startActivityForResult(intent, VCARD_SEND_REQUEST);
                }
            } else if (viewId == R.id.ib_expression_keyboard) { // 点击输入键盘
                hideMorePanel();
            } else if (viewId == R.id.ib_file) {


                chooseFile();

            } else if (viewId == R.id.ib_audio) {

                audioRecordBtnClick();

            } else if (viewId == R.id.sms_direction) {
                String stip = mContext.getResources().getString(R.string.fee_reminding_CCMC_stip);
                final CommomDialog mDialog;
                if (!AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone) || PhoneUtils.isNotChinaNum(mAccountUserPhone)) {
                    stip = mContext.getResources().getString(R.string.fee_reminding_not_CCMC_stip);
                    SpannableString spannableString = new SpannableString(stip);
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF157CF8"));
                    spannableString.setSpan(colorSpan, 19, 25, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    mDialog = new CommomDialog(mContext, mContext.getString(R.string.price_remind), spannableString);
                } else {
                    SpannableString sb = new SpannableString(stip);
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.color_0D6CF9));
                    sb.setSpan(colorSpan, 26, 31, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mDialog = new CommomDialog(mContext, mContext.getString(R.string.price_remind), sb);
                }
                mDialog.setPositiveName(mContext.getResources().getString(R.string.i_know));
                mDialog.setPositiveBtnOnly();
                mDialog.setPositiveTextSize(16);
                mDialog.show();
                mDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
                    @Override
                    public void onClick() {
                        mDialog.dismiss();
                    }
                });
            } else if (viewId == R.id.multi_btn_delete){
                //多选模式删除
                mBasePresenter.deleteMultiMessage(mMessageChatListAdapter.getSelectedList());

            } else if (viewId == R.id.multi_btn_forward){
                //多选模式转发
                mBasePresenter.forwardMultiMessage(mMessageChatListAdapter.getSelectedList());
            }
        }
    }

    protected abstract void onSendClickReport(int type);

    private void toSmSLayout(boolean isTo) {
        toSmSAndAudioTextLayout(isTo, false);
    }

    private void toSmSAndAudioTextLayout(boolean isTo, boolean isaudioText) {
        if (isSpeticalNum(phone)) {
            this.finish();
        }
        if (isTo) {
            mIbMore.setImageResource(R.drawable.cc_chat_ic_input_more);
            chatRichMediaVp.setVisibility(View.GONE);

            mLayoutForSMS.setVisibility(View.VISIBLE);
            mLayoutForMessage.setVisibility(View.GONE);
            ConvCache.getInstance().SMS_STATUS_CACHE.put(mAddress, true);
            isSMSMode = true ;
        } else {
            mLayoutForMessage.setVisibility(View.VISIBLE);
            mLayoutForSMS.setVisibility(View.GONE);
            ConvCache.getInstance().SMS_STATUS_CACHE.remove(mAddress);
            isSMSMode = false ;
        }
    }

    private void setPanelHeight(int height) {
        int tepHeight = height;
        if (height > MAX_HEIGHT) {
            tepHeight = (int) AndroidUtil.dip2px(mContext, MAX_HEIGHT_DP);
        } else if (height < MIN_HEIGHT) {
            tepHeight = (int) AndroidUtil.dip2px(mContext, MIN_HEIGHT_DP);
        }

        if (tepHeight != mFlMore.getMeasuredHeight() && !isShowAudio) {
            ViewGroup.LayoutParams params = mFlMore.getLayoutParams();
            params.height = tepHeight;
            mFlMore.setLayoutParams(params);
            SharePreferenceUtils.setParam(getActivity(), TAG + "_keyboard_height", tepHeight);
        } else if (isShowAudio) {
            int audio_height = (int) AndroidUtil.dip2px(getActivity(), 223f);
            if (audio_height != mFlMore.getLayoutParams().height) {
                ViewGroup.LayoutParams params = mFlMore.getLayoutParams();
                params.height = (int) AndroidUtil.dip2px(getActivity(), 223f);
                mFlMore.setLayoutParams(params);
            }
        }
    }

    private void hideMorePanel() {
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        mFlMore.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFlMore.setVisibility(View.GONE); // 表情栏隐藏
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); //该Activity总是调整屏幕的大小以便留出软键盘的空间
            }
        }, 200);

        mIbExpressionKeyboard.setVisibility(View.GONE); //  输入键盘图标按钮
        mIbExpression.setVisibility(View.VISIBLE); // 表情图标按钮
        mEtMessage.requestFocus();  // 输入框获得焦点
        showKeyboard();  // 键盘显示
    }

    private void setSoftInputMode(int mode) {
        this.getActivity().getWindow().setSoftInputMode(mode);
    }

    //强制隐藏键盘
    protected void hideKeyboardAndTryHideGifView() {
        hideKeyboard();
        //如果gif趣图是显示状态，就关闭gif趣图
        if (isGifViewShow) {
            setGifView(false);
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEtMessage.getWindowToken(), 0);
        }
    }

    //强制显示键盘
    protected void showKeyboard() {
        if (getActivity() != null) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mEtMessage, InputMethodManager.SHOW_FORCED);
        }
    }

    //强制停止RecyclerView滑动方法
    public void forceStopRecyclerViewScroll() {
        mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    public static final int REQUEST_EDIT_PICTURE = 150;
    public static final String EXTRA_IMAGE_SAVE_PATH = "image_save_path";

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_VIDEO_REQUEST) {
            if (resultCode == RESULT_OK) {
                MediaItem mediaItem = (MediaItem) data.getSerializableExtra(MessageModuleConst.GalleryActivityConst.KEY_MEDIA_SET);
                String path = mediaItem.getLocalPath();
                File file = new File(path);
                if (file != null && file.exists()) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(new File(file.getAbsolutePath())));
                    getContext().sendBroadcast(intent);

                    MediaItem item;
                    if (mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                        item = new MediaItem(file.getAbsolutePath(), MediaItem.MEDIA_TYPE_VIDEO);
                        item.setSelected(true);
                        item.setLocalPath(file.getAbsolutePath());
                        item.setDuration(FileUtil.getDuring(file.getAbsolutePath()));
                    } else {
                        item = new MediaItem(file.getAbsolutePath(), MediaItem.MEDIA_TYPE_IMAGE);
                        item.setSelected(true);
                        item.setLocalPath(file.getAbsolutePath());
                    }
                    ArrayList<MediaItem> items = new ArrayList<MediaItem>();
                    items.add(item);

                    moveToEnd();
                    sendImgAndVideo(items);
                    PopWindowFor10GUtil.showToast(getActivity());
                }
            }
        }
//        REQUEST_EDIT_PICTURE
        if (requestCode == REQUEST_EDIT_PICTURE) {
            if (data == null) return;
            String path = data.getStringExtra(EXTRA_IMAGE_SAVE_PATH);
            if (TextUtils.isEmpty(path)) {
                return;
            }
            MediaItem item = new MediaItem(path, MediaItem.MEDIA_TYPE_IMAGE);
            item.setSelected(true);
            item.setLocalPath(path);
            ArrayList<MediaItem> items = new ArrayList<MediaItem>();
            items.add(item);
            moveToEnd();
            sendImgAndVideo(items);
        }

        if (requestCode == OPEN_CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                LogF.d(TAG, "------OPEN_CAMERA_REQUEST ----- RESULT_OK");
                if (mCameraPicture != null && mCameraPicture.exists()) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(new File(mCameraPicture.getAbsolutePath())));
                    getContext().sendBroadcast(intent);

                    MediaItem item = new MediaItem(mCameraPicture.getAbsolutePath(), MediaItem.MEDIA_TYPE_IMAGE);
                    item.setSelected(true);
                    item.setLocalPath(mCameraPicture.getAbsolutePath());
                    ArrayList<MediaItem> items = new ArrayList<MediaItem>();
                    items.add(item);

                    moveToEnd();
                    sendImgAndVideo(items);
                    PopWindowFor10GUtil.showToast(getActivity());
                }
            }

        }

        if (requestCode == OPEN_GALLERY_REQUEST) {//打开系统图库查找图片
            if (resultCode == RESULT_OK) {
                moveToEnd();
                mKeyboardPanel.setVisibility(View.VISIBLE);
                ArrayList<MediaItem> serializableExtra = (ArrayList<MediaItem>) data.getSerializableExtra(GalleryPresenter.SELECTED_ITEMS);
                boolean isOriginalPhoto = data.getBooleanExtra(GalleryPresenter.ORIGIN_PHOTO, false);
                if (serializableExtra != null) {
                    Iterator<MediaItem> iterator = serializableExtra.iterator();
                    while (iterator.hasNext()) {
                        MediaItem next = iterator.next();
                        String localPath = next.getLocalPath();
                        if (localPath != null & !localPath.isEmpty()) {
                            File file = new File(localPath);
                            if (file != null && file.exists()) {
                            } else {
                                iterator.remove();
                                if (next.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) {
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.img_not_exit), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.video_not_exit), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
                sendImgAndVideo(serializableExtra, isOriginalPhoto);
                onSendClickReport(SEND_PIC_TYPE);
            }
        }

        if (requestCode == SELECT_AT_MEMBER_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
//                    String person = data.getStringExtra("NAME");
//                    String addrs = data.getStringExtra("ADDR");
                    boolean isAtAll = data.getBooleanExtra(AT_MEMBER_ALL, false);
                    int selectionStart = 0;
                    int end = 0;
                    if (isAtAll) {
                        Activity attachActivity = getActivity();
                        if (attachActivity == null)
                            return;
                        mAtAllSelectionStart = mEtMessage.getSelectionStart();
                        mEtMessage.getText().insert(mAtAllSelectionStart, getResources().getString(R.string.contact_name_at_all) + " ");
                        mAtAllSelectionEnd = mEtMessage.getSelectionStart();
                        mIsAtAll = true;
                    } else {
                        ArrayList<BaseContact> atList = data.getParcelableArrayListExtra(AT_MEMBER_LIST);
                        if (atList == null)
                            return;
                        boolean isFirstPerson = true;
                        for (BaseContact contact : atList) {
                            String person = contact.getName();
                            String addrs = contact.getNumber();
                            if (!TextUtils.isEmpty(person)) {
                                selectionStart = mEtMessage.getSelectionStart();
                                if (isFirstPerson) {
                                    mEtMessage.getText().insert(selectionStart, person + " ");
                                    isFirstPerson = false;
                                } else {
                                    mEtMessage.getText().insert(selectionStart, "@" + person + " ");
                                }
                                end = mEtMessage.getSelectionStart();
                            }
                            if (!TextUtils.isEmpty(addrs)) {
                                GroupMember member = new GroupMember();
                                member.setAddress(addrs);
                                member.setPerson(person);
                                AtMemberLength atMemberLength = new AtMemberLength(selectionStart - 1, end - 1, member);
                                ArrayList<AtMemberLength> memberList = getAtMemberLengthList();
                                memberList.add(atMemberLength);
                            }
                        }
                    }
                }
            }
        }

        if (requestCode == VCARD_SEND_REQUEST) {
            if (resultCode == RESULT_OK) {
                LogF.d(TAG, "onActivityResult:VCARD_SEND ");
                handleVcard(data);
                moveToEnd();
                onSendClickReport(SEND_CARD_TYPE);
            }
        }

        if (requestCode == SELECT_FILE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri dataUri = data.getData();
                    long lenth = 0;
                    if (dataUri != null) {
                        String path = Uri.decode(FileUtil.getMediaRealPath(mContext, dataUri));
                        if (!StringUtil.isEmpty(path)) {
                            File file = new File(path);
                            if (file.exists()) {
                                lenth = file.length();
                                if (lenth < BaseChatFragment.MAX_FILE_LENGTH) {
                                    PopWindowFor10GUtil.showToast(getActivity());
                                }
                            }
                        }
                    }
                    onSendClickReport(SEND_FILE_TYPE);
                    sendFileMsg(data);
                    moveToEnd();
                }
            }
        }
        if (requestCode == ExpressionFragment.GET_GIF) {
            RxAsyncHelper<MediaItem> helper = new RxAsyncHelper("");
            helper.runInThread(new Func1() {
                @Override
                public MediaItem call(Object o) {
                    return GifMessageUtil.downLoadGif(data.getStringExtra(ExpressionFragment.GIF_URL), getContext());
                }
            }).subscribe(new Action1<MediaItem>() {
                @Override
                public void call(MediaItem mediaItem) {
                    if (mediaItem != null) {
                        ArrayList<MediaItem> items = new ArrayList<MediaItem>();
                        items.add(mediaItem);
                        sendImgAndVideo(items);
                    }
                }
            });
        }

        if (requestCode == OPEN_SMS_REQUEST) {
            if (resultCode == RESULT_OK) {
                toSmSLayout(true);
            }
        } else if (requestCode == MessageModuleConst.START_LOCATION_ACTIVITY_REQUEST_CODE) {
            if (data != null) {
                double dlatitude = data.getDoubleExtra(MessageModuleConst.INTENT_KEY_FOR_LATITUDE, 0.0f);
                double dlongitude = data.getDoubleExtra(MessageModuleConst.INTENT_KEY_FOR_LONGITUDE, 0.0f);
                String locationAddress = data.getStringExtra(MessageModuleConst.INTENT_LEY_FOR_LOCATION_ADDRESS);
                String specialAddress = data.getStringExtra(MessageModuleConst.INTENT_LEY_FOR_LOCATION_SPECIAL_ADDRESS);
                sendLocation(dlatitude, dlongitude, 1000, specialAddress, locationAddress);
                onSendClickReport(SEND_LOCATION_TYPE);
            }
        }
    }

    private void handleVcard(Intent data) {
        String vcardString = data.getStringExtra(CONTACT_VCARD_STRING);
        if (!TextUtils.isEmpty(vcardString)) {
            UUID uuid = UUID.randomUUID();
            String fileName = uuid + ".vcf";
            File card_clip = FileUtil.getCardClip(mContext);
            LogF.d(TAG, " card_clip path = " + card_clip.getAbsolutePath());
            File fvcf = new File(card_clip, fileName); // Environment.getExternalStorageDirectory()
            try {
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fvcf));
                osw.write(vcardString);
                osw.flush();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String filepath = fvcf.getAbsolutePath();
            LogF.d(TAG, "filepath: " + filepath + " vcardString:" + vcardString);
            sendVcard(mAddress, vcardString, filepath, FileUtil.getDuring(filepath));
        }
    }

    private void startVideo() {
        if (AndroidUtil.isSdcardReady()) {
            if (AndroidUtil.isSdcardAvailable()) {
                try {
                    System.gc();
                    Intent it = new Intent();
                    it.setClass(getContext(), VideoRecordActivity.class);
                    startActivityForResult(it, OPEN_VIDEO_REQUEST);
                    Activity a = getActivity();
                    if (a != null) {
                        getActivity().overridePendingTransition(R.anim.anim_in_from_bottom_gif, R.anim.anim_fake);
                    }
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), getString(R.string.open_camera_defail), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.no_sd_card), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_sd_card_), Toast.LENGTH_LONG).show();
        }
    }

    private void chooseFile() {
        Intent i = new Intent(getActivity(), ChooseLocalFileActivity.class);
        startActivityForResult(i, SELECT_FILE_REQUEST);
    }

    public void moveToTopOfBQSS() {
        mLinearLayoutManager.scrollToPositionWithOffset(mLinearLayoutManager.getItemCount() - 1, -1000);
        forceStopRecyclerViewScroll();
    }

    public void moveToEnd() {
        LogF.d(TAG, "---------to end -------");
        //        mLinearLayoutManager.scrollToPosition(mLinearLayoutManager.getItemCount() - 1);
        mLinearLayoutManager.scrollToPositionWithOffset(mLinearLayoutManager.getItemCount() - 1, -40000);
        forceStopRecyclerViewScroll();
    }

    public void moveToOffsetEnd() {
        LogF.d(TAG, "---------to offset end -------");
        moveToEnd();
    }

    private void moveToSearchPos(int searchPos) {
        mLinearLayoutManager.scrollToPosition(searchPos);
    }

    /**
     * 新消息提示类。分两种情况处理。
     * 1.进入会话，用户查看完x条新信息前：提示x条新信息，此时有新消息不提示。
     * 2.用户查看完x条新信息后：此时有新消息则提示新消息来到。
     */
    protected class ChatRemindHelper {
        private static final String LOG_TAG = "ChatRemindHelper";
        // 最大未读数目限制
        public static final int MAX_UNREAD_COUNT = 300;

        public int getmFirstUnReadCount() {
            return mFirstUnReadCount;
        }

        // 未读消息数量
        private int mFirstUnReadCount = 0;
        // 第一条未读消息位置
        private int mFirstUnReadPosition = -1;
        // 是否已经插入“---以下为新消息---”项
        private boolean mUnreadDataHasInsert = false;
        // 向下滑动
        private boolean mScrollToDown = false;
        // 当前是否已显示"跳转到最新一条"提示
        private boolean mLastMsgShow = false;
        // 当前是否已显示"x条新消息"提示
        private boolean mNewMsgShow = false;
        // 新接收到的消息数目
        private int mNewMsgCount = 0;

        private boolean mHasAT;

        /**
         * 返回recycleview adapter数据源中插入的"新消息提示"条目
         */
        public int getChatRemindMsgCount() {
            return mUnreadDataHasInsert ? 1 : 0;
        }

        /**
         * 跳转到最新消息处或第一条未读消息处
         */
        public void jumpToPosition() {
            if (mHasAT) {
                mBtMsgATTip.setVisibility(View.GONE);
                mHasAT = false;
                mMessageChatListAdapter.setUnReadCount(0);

            }
            if (mScrollToDown) {
                hideMessageCountTip();
                mNewMsgCount = 0;
                moveToEnd();

            } else {
                hideMessageCountTip();
                int position = mFirstUnReadPosition;
                if (mFirstUnReadCount <= 0 || position < 0 || position >= mMessageChatListAdapter.getItemRealCount()) {
                    LogF.d(LOG_TAG, "error : mFirstUnReadCount " + mFirstUnReadCount + ", position " + position);
                    return;
                }
                forceStopRecyclerViewScroll();
                Message msg = new Message();
                msg.setType(TYPE_NEW_MSG_RECV_DIVIDE_LINE);
                mMessageChatListAdapter.getDataList().add(position, msg);
                reCalculateBubble(position);
                mUnreadDataHasInsert = true;
                isNeedShowStrangerTip();
                mMessageChatListAdapter.notifyDataSetChanged();
                if (mMessageChatListAdapter.canLoadMore()) {
                    position++;
                    if (position <= mMessageChatListAdapter.getItemCount()) {
                        mRecyclerView.smoothScrollToPosition(position);
                        hideMessageCountTip();
                        mFirstUnReadCount = 0;
                        mNewMsgCount = 0;
                        if (mHasAT) {
                            mHasAT = false;
                            mBtMsgATTip.setVisibility(View.GONE);
                            mMessageChatListAdapter.setUnReadCount(0);
                        }
                        return;
                    }
                }
                if (position <= mMessageChatListAdapter.getItemRealCount()) {
                    mRecyclerView.smoothScrollToPosition(position);
                    hideMessageCountTip();
                    mFirstUnReadCount = 0;
                    mNewMsgCount = 0;
                    if (mHasAT) {
                        mHasAT = false;
                        mBtMsgATTip.setVisibility(View.GONE);
                        mMessageChatListAdapter.setUnReadCount(0);
                    }
                }
            }
        }

        /**
         * 跳转到@消息处
         */
        public void jumpToATPosition() {
            mBtMsgATTip.setVisibility(View.GONE);
            hideMessageCountTip();
            mFirstUnReadCount = 0;
            mMessageChatListAdapter.setUnReadCount(0);
            int position = mMessageChatListAdapter.getATPosition();
            mLinearLayoutManager.scrollToPosition(position);
            forceStopRecyclerViewScroll();
        }

        /**
         * 初始化未读消息数目
         */
        public int initUnReadCount(Bundle bundle) {
            if (bundle == null) {
                return 0;
            }
            mFirstUnReadCount = bundle.getInt("unread", 0);
            LogF.d(LOG_TAG, "initUnReadCount count=" + mFirstUnReadCount);
            // 限制第一次加载的最大数目
            if (mFirstUnReadCount > MAX_UNREAD_COUNT) {
                mFirstUnReadCount = MAX_UNREAD_COUNT;
            }
            bundle.putInt("unread", mFirstUnReadCount);
            mHasAT = bundle.getBoolean("has_at_msg", false);
            LogF.d(LOG_TAG, "mHasAT " + mHasAT);
            return mFirstUnReadCount;
        }

        /**
         * 更新接收到新消息后的提示
         */
        private void updateNewMsgTip() {
            int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
            if (lastVisibleItem >= 0) {
                if (lastVisibleItem < mLinearLayoutManager.getItemCount() - 1) {
                    if (!mNewMsgShow && mNewMsgCount > 0) {
                        String text = getResources().getString(R.string.tv_label_new_msg_come, mNewMsgCount);
                        showMessageCountTip(text, true, true);
                        mNewMsgShow = true;
                    } else if (!mNewMsgShow && !mLastMsgShow) {
                        if (lastVisibleItem < mLinearLayoutManager.getItemCount() - 16) {
                            String text = getResources().getString(R.string.tv_label_last_msg_jump);
                            showMessageCountTip(text, true, false);
                        }
                    } else if (!mNewMsgShow) {
                        if (lastVisibleItem >= mLinearLayoutManager.getItemCount() - 16) {
                            hideMessageCountTip();
                        }
                    }
                } else {
                    if (mLastMsgShow || mNewMsgShow) {
                        hideMessageCountTip();
                        mNewMsgCount = 0;
                    }
                }
            }
        }

        /**
         * 用户滑动时更新提示。当用户滑动到第一条未读消息时，关闭提示。
         */
        public void updateChatRemindWhenScroll(int dy) {
            if (mFirstUnReadCount <= 0) {
                if (dy < 0) {
                    if (mLastMsgShow && mNewMsgCount == 0) {
                        return;
                    }
                }
                updateNewMsgTip();
            } else {
                int firstVisibleItem = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
                if (firstVisibleItem == RecyclerView.NO_POSITION) {//处理超过一页的长文本导致@人提示不消失的问题
                    int firstItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                    int lastItem = mLinearLayoutManager.findLastVisibleItemPosition();
                    if (firstItem == lastItem) {
                        firstVisibleItem = firstItem;
                    }
                }
                if (firstVisibleItem == mFirstUnReadPosition && mFirstUnReadPosition != -1) { //滑到最早未读消息位置，跳转到最新消息提示消失
                    hideMessageCountTip();
                    mFirstUnReadCount = 0;
                    mNewMsgCount = 0;
                    if (mHasAT) {
                        mHasAT = false;
                        mBtMsgATTip.setVisibility(View.GONE);
                        mMessageChatListAdapter.setUnReadCount(0);
                    }
                }
            }
        }

        /**
         * 布局完成时，初始化第一条未读消息的位置mFirstUnReadPosition
         */
        public void updateChatRemindWhenLayout() {

            if (mFirstUnReadCount > 0) {
                int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                if (mFirstUnReadPosition < 0) {
                    if (firstVisibleItem == 0) {
                        LogF.i(LOG_TAG, "no need to show chat remind");
                        mFirstUnReadCount = 0;
                        return;
                    }
                    int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
                    if (firstVisibleItem > 0 && lastVisibleItem > 0) {
                        if (mMessageChatListAdapter.getItemCount() < mFirstUnReadCount) {
                            LogF.e(LOG_TAG, "error : message count is less than unRead count");
                            mFirstUnReadCount = 0;
                            return;
                        }
                        if (lastVisibleItem == mMessageChatListAdapter.getItemCount() - 1) {
                            if (lastVisibleItem - firstVisibleItem + 1 < mFirstUnReadCount) {
                                mFirstUnReadPosition = mMessageChatListAdapter.getItemRealCount() - mFirstUnReadCount;
                                if (mFirstUnReadPosition < 0) {
                                    mFirstUnReadCount = 0;
                                    return;
                                }

                                // @显示初始化
                                int atPosition = mMessageChatListAdapter.getATPosition();
                                LogF.d(LOG_TAG, " at position " + atPosition);
                                if (mHasAT && atPosition >= 0 && atPosition < firstVisibleItem) {
                                    mBtMsgATTip.setVisibility(View.VISIBLE);
                                } else {//有@显示时不显示最新消息
                                    String text = getResources().getString(R.string.tv_label_new_msg_come, mFirstUnReadCount);
                                    showMessageCountTip(text, false, true);
                                }
                            } else {
                                mFirstUnReadCount = 0;
                            }
                        }
                    }
                }
            } else {
                updateNewMsgTip();
            }
        }

        /**
         * 加载更多旧记录时，需要更新"---以下为新消息---"项的position
         */
        public void updateChatRemindWhenOldMsgLoaded(int offset) {
            if (mUnreadDataHasInsert && mFirstUnReadPosition > 0) {
                int position = mFirstUnReadPosition;
                position += offset;
                if (position > 0 && position < mMessageChatListAdapter.getItemRealCount()) {
                    Message msg = new Message();
                    msg.setType(TYPE_NEW_MSG_RECV_DIVIDE_LINE);
                    mMessageChatListAdapter.getDataList().add(position, msg);
                    reCalculateBubble(position);
                    mFirstUnReadPosition = position;
                }
            }
        }

        /**
         * 接收到新消息，在设置新的数据源curosr之前更新新消息数目，之后插入"---以下为新消息---"项
         */
        public void updateChatRemindWhenNewMsgCome(boolean afterChange, int offset) {
            if (offset < 0) {
                return;
            }
            if (!afterChange) {
                if (mLinearLayoutManager.findLastVisibleItemPosition() < mLinearLayoutManager.getItemCount() - 1) {
                    if (mFirstUnReadCount <= 0) {
                        mNewMsgCount = mNewMsgCount + offset;
                        if (offset > 0) {
                            mNewMsgShow = false;
                        }
                    }
                }
                // @位置处理
                if (mFirstUnReadCount > 0) {
                    mMessageChatListAdapter.setUnReadCount(mFirstUnReadCount + offset);
                }
            } else {
                // 新消息提示位置处理
                if (mUnreadDataHasInsert) {
                    if (mFirstUnReadPosition > 0) {
                        int position = mFirstUnReadPosition;
                        if (position > 0 && position < mMessageChatListAdapter.getItemRealCount()) {
                            Message msg = new Message();
                            msg.setType(TYPE_NEW_MSG_RECV_DIVIDE_LINE);
                            mMessageChatListAdapter.getDataList().add(position, msg);
                            reCalculateBubble(position);
                        }
                    }
                }
            }
        }

        /**
         * 隐藏提示
         */
        public void hideMessageCountTip() {
            mLastMsgShow = false;
            mNewMsgShow = false;
            mBtMsgCountTip.setText(null);
            mRLMsgCountTip.setVisibility(View.GONE);
            mRLMsgCountTip_up.setVisibility(View.GONE);
        }

        /**
         * 显示提示
         */
        public void showMessageCountTip(String text, boolean down, boolean isUp) {
            mLastMsgShow = true;
            mScrollToDown = down;


            if (isUp) {
                mRLMsgCountTip_up.setVisibility(View.VISIBLE);
                mBtMsgCountTip_up.setText(text);
            } else {
                int type = (int) SharePreferenceUtils.getParam(getContext(), MessageModuleConst.MESSAGE_THEME_TYPE + mUserNum + mAddress, MessageBgSetActivity.THEME_0NE);
                if (down) {
                    if (type > 2) {
                        mBtMsgCountTip.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.chat_remind_down_white, 0);
                    } else {
                        mBtMsgCountTip.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.chat_remind_down, 0);
                    }

                } else {
                    if (type > 2) {
                        mBtMsgCountTip.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.chat_remind_up_white, 0);
                    } else {
                        mBtMsgCountTip.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.chat_remind_up, 0);
                    }
                }
                mRLMsgCountTip.setVisibility(View.VISIBLE);
                mBtMsgCountTip.setText(text);
            }
        }
    }

    //插入“以下为新消息”项的时候，需要把position前后两个item的气泡重新处理
    public void reCalculateBubble(int position){
        List mesageList = mMessageChatListAdapter.getDataList();
        LogF.e(TAG,"reCalculateBubble position = " + position +" mesageListSize = " + mesageList.size());

        if(position > 0){
            Message preMessgage = (Message)mesageList.get(position - 1);
            int preMessageBubble = preMessgage.getBubbleType();

            switch (preMessageBubble){
                case BUBBLE_NOUP_NODOWN:
                    preMessgage.setBubbleType(BUBBLE_NOUP_NODOWN);
                    break;
                case BUBBLE_NOUP_DOWN:
                    preMessgage.setBubbleType(BUBBLE_NOUP_NODOWN);
                    break;
                case BUBBLE_UP_NODOWN:
                    preMessgage.setBubbleType(BUBBLE_UP_NODOWN);
                    break;
                case BUBBLE_UP_DOWN:
                    preMessgage.setBubbleType(BUBBLE_UP_NODOWN);
                    break;

                default:
                    break;
            }

        }
        //处理后面一个item
        if(position < mesageList.size() - 1){
            Message nextMessgage = (Message)mesageList.get(position + 1);
            int nextMessgageBubble = nextMessgage.getBubbleType();

            switch (nextMessgageBubble){
                case BUBBLE_NOUP_NODOWN:
                    nextMessgage.setBubbleType(BUBBLE_NOUP_NODOWN);
                    break;
                case BUBBLE_NOUP_DOWN:
                    nextMessgage.setBubbleType(BUBBLE_NOUP_DOWN);
                    break;
                case BUBBLE_UP_NODOWN:
                    nextMessgage.setBubbleType(BUBBLE_NOUP_NODOWN);
                    break;
                case BUBBLE_UP_DOWN:
                    nextMessgage.setBubbleType(BUBBLE_NOUP_DOWN);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void senAudioMessage(String path, long lon) {

    }

    @Override
    public void senAudioMessage(String path, long lon, String detail) {

    }

    @Override
    public void stopMessageAudio() {
        mMessageChatListAdapter.stopAudioForActivity();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_base_chat;
    }

    public boolean isSlient() {
        LogF.w(TAG, "isSlient : addr=" + mAddress);
        return ConversationUtils.isSlient(getContext(), mAddress);
    }

    @Override
    public void showPanel(boolean show) {
        if (show) {
            night();
        } else {
            hideNight();
        }
    }

    private void disableDragupSend() {
        if (mChatSendPopupWindow != null) {
            mChatSendPopupWindow.dismiss();
            mChatSendPopupWindow = null;
        }
        SharePreferenceUtils.setParam(getContext(), SP_KEY_FIRST_DRAG_UP_SEND, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mChatSendPopupWindow != null) {
            mChatSendPopupWindow.dismiss();
        }
        if (mLayout != null && mOnGlobalLayoutListener != null) {
            mLayout.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRedPaperProgressDialog && mRedPaperProgressDialog.isShowing()) {
            mRedPaperProgressDialog.dismiss();
        }
        if (null != mHbAuthDialog && mHbAuthDialog.isShowing()) {
            mHbAuthDialog.dismiss();
            mHbAuthDialog = null;
        }
        if (mMessageChatListAdapter != null && mMessageChatListAdapter.getItemRealCount() == 0) {
            ConvCache.getInstance().SMS_STATUS_CACHE.remove(mAddress);
        }
        ArrayList<AtMemberLength> list = getAtMemberLengthList();
        if (list != null && list.size() == 0) {
            removeAtMemberLengthList();
        }
    }


    @Override
    public void finish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    //加载更多
    protected abstract void loadMoreMessages();

    //发送图片视频
    protected abstract void sendImgAndVideo(ArrayList<MediaItem> items);

    //发送图片视频 添加原图判断
    protected abstract void sendImgAndVideo(ArrayList<MediaItem> items, boolean isOriginPhoto);

    //发送地理位置
    protected abstract void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel, String detailAddress);

    //发送文件
    protected abstract void sendFileMsg(Intent data);

    //发送名片
    protected abstract void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration);

    //发送消息
    protected abstract void sendMessage();

    //初始化presenter
    protected abstract void initPresenter(Bundle bundle);

    //获取草稿
    protected abstract Message getDraftMessage();

    //保存草稿
    protected abstract void saveDraftMessage(boolean save, Message Msg);

    //获取聊天类型
    public abstract int getChatType();

    //加载更多
    protected abstract void clearUnreadCount();

    //更改menu
    protected abstract void changeMenu(int themeOption);

    /**
     * 发送免费短信
     */
    protected void sendSmsMessage(String msg) {
    }

    protected void sendSysSmsMessage(String msg) {

    }

    public MessageChatListAdapter getAdapter() {
        return mMessageChatListAdapter;
    }

    private static class MyHandler extends Handler {
        WeakReference<BaseChatFragment> weakReference;

        MyHandler(BaseChatFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            final BaseChatFragment fragment = weakReference.get();
            if (fragment != null) {
                fragment.handleQTSS(msg);
            }
        }
    }

    public void handleQTSS(android.os.Message msg) {
        if (getActivity() == null) {
            return;
        }
        if (msg.arg2 == 1) {
            if (mStickersContainer != null) {
                mStickersContainer.removeAllViews();
            }
            if (mBqssHScrollview != null) {
                mBqssHScrollview.scrollTo(0, 0);
            }
            switch (msg.arg1) {
                case SUCCESS:
                    final List<BQSSWebSticker> webStickerList = (List<BQSSWebSticker>) msg.obj;
                    if (webStickerList == null || webStickerList.size() == 0) {
                        if (msg.arg2 == 1)
                            Toast.makeText(getActivity(), getString(R.string.no_result), Toast.LENGTH_SHORT).show();
                    } else {
                        int gifShowSize = webStickerList.size();
                        float scale = getActivity().getResources().getDisplayMetrics().density;
                        final float height = (75 * scale + 0.5F);
                        if (mBqssHScrollview != null) {
                            mBqssHScrollview.setVisibility(View.VISIBLE);
                        }
                        for (int i = 0; i < gifShowSize; i++) {
                            final String mainImg = webStickerList.get(i).getMain();
                            Uri uri = Uri.parse(mainImg);
                            ImageView simpleDraweeView = new ImageView(getActivity());
                            RequestOptions options = new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.bqss_sticker_loading).error(R.mipmap.icon_loading_failed);
                            Glide.with(this).load(uri).apply(options).into(simpleDraweeView);

                            BQSSWebSticker webSticker = webStickerList.get(i);
                            float ratio = webSticker.getWidth() * 1.0f / webSticker.getHeight();
                            int width = (int) (height * ratio);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
                            layoutParams.setMargins(3, 0, 3, 0);
                            if (mStickersContainer == null) {
                                LogF.e("","mStickersContainer is null");
                            }else {
                                mStickersContainer.addView(simpleDraweeView, layoutParams);
                            }
                            simpleDraweeView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    RxAsyncHelper<MediaItem> helper = new RxAsyncHelper("");
                                    helper.runInThread(new Func1() {
                                        @Override
                                        public MediaItem call(Object o) {
                                            File file = null;
                                            try {
                                                file = Glide.with(mContext).load(mainImg).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            }
                                            if (file != null) {
                                                String gifPath = copyGifImageToGitCache(file);

                                                MediaItem item = new MediaItem(gifPath, MediaItem.MEDIA_TYPE_IMAGE);
                                                return item;
                                            }

                                            return null;
                                        }
                                    }).subscribe(new Action1<MediaItem>() {
                                        @Override
                                        public void call(MediaItem mediaItem) {
                                            justSend = true;
//                                            if (mEtMessage != null) {
//                                                mEtMessage.setText("");
//                                            }
                                            if (mediaItem != null) {
                                                ArrayList<MediaItem> items = new ArrayList<MediaItem>();
                                                items.add(mediaItem);
                                                sendImgAndVideo(items);
                                                onSendClickReport(SEND_GIF_TYPE);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                    break;
                case ERROR:
                    setGifView(false);
                    Toast.makeText(getActivity(), R.string.get_gif_err, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * 将".cnt"后缀的gif图copy到Cache缓存目录
     */
    private String copyGifImageToGitCache(File sourceFile) {
        String path = sourceFile.getName();
        StringBuffer sb = new StringBuffer(path);
        int index = path.lastIndexOf(".");
        if (index > 0) {
            sb.replace(index, sb.length(), ".gif");
        }

        final String cacheDir = FileUtil.getGifsPathUnderData(getContext().getApplicationContext());
        sb.insert(0, cacheDir);

        File destFile = new File(sb.toString());
        if (!destFile.exists()) {
            FileUtil.copyFile(sourceFile, destFile);
        }
        return sb.toString();
    }

    /**
     * 设置趣图相关控件的显示和隐藏
     * @param shouldShowFunnyContainer
     */
    private void setGifView(boolean shouldShowFunnyContainer) {
        if (mBqssHScrollview != null) {
            isGifViewShow = shouldShowFunnyContainer;
            if (shouldShowFunnyContainer) {
                mBqssHScrollview.setVisibility(View.VISIBLE);
                mLLGifLayout.setVisibility(View.VISIBLE);
                mRichPanel.setVisibility(View.GONE);
                moveToTopOfBQSS();
                mEtMessage.setHint(getString(R.string.funny_pic));
            } else {
                moveToEnd();
                if (mStickersContainer != null) {
                    mStickersContainer.removeAllViews();
                }
                mBqssHScrollview.setKeyword(BQSSConstants.TRENDING_STICKER_TAG);
                mBqssHScrollview.setVisibility(View.GONE);
                mLLGifLayout.setVisibility(View.GONE);
                mRichPanel.setVisibility(View.VISIBLE);
                mEtMessage.setHint(getString(R.string.say_something));
            }
        }
    }

    private static boolean mNeedLoadMore = true;
    private static final int MAX = 18;

    public static class BqssHorizontalScrollView extends HorizontalScrollView {
        private String keyword = BQSSConstants.TRENDING_STICKER_TAG;
        private int currentPage = 1;
        private int currentCount = 0;

        public void retSetCurrentPage() {
            this.currentPage = 1;
        }

        public void retSetCurrentCount() {
            this.currentCount = 0;
        }

        public void setHandler(Handler mHandler) {
            this.mHandler = mHandler;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        private Handler mHandler;

        public BqssHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public BqssHorizontalScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public BqssHorizontalScrollView(Context context) {
            super(context);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            int maxX = getChildAt(0).getMeasuredWidth() - getMeasuredWidth();
            if (maxX == getScrollX()) {
                loadMore();  //freso 加载过多会出现oom。
            }
        }

        public void loadMore() {
            if (mNeedLoadMore) {
                mNeedLoadMore = false;
                if (keyword.equals(BQSSConstants.TRENDING_STICKER_TAG)) {
                    getTrendingStickers();
                } else {
                    getStickers();
                }
            }
        }

        private void getStickers() {
            BQSSSearchApi.getSearchStickers(keyword, currentPage, 6, new BQSSApiCallback<BQSSWebSticker>() {
                @Override
                public void onSuccess(BQSSApiResponseObject<BQSSWebSticker> result) {
                    android.os.Message message = android.os.Message.obtain();
                    message.arg1 = SUCCESS;
                    message.arg2 = currentPage;
                    message.obj = result.getDatas();
                    mHandler.sendMessage(message);
                    currentCount += result.getDatas().size();
                    if (result.getDatas().size() < 6 || currentCount >= MAX) {
                        mNeedLoadMore = false;
                    } else {
                        currentPage++;
                        mNeedLoadMore = true;
                    }
                }

                @Override
                public void onError(String errorInfo) {
                    android.os.Message message = android.os.Message.obtain();
                    message.arg1 = ERROR;
                    message.obj = errorInfo;
                    mHandler.sendMessage(message);
                }
            });

        }

        private void getTrendingStickers() {
            BQSSSearchApi.getTrendingStickers(currentPage, 6, new BQSSApiCallback<BQSSWebSticker>() {
                @Override
                public void onSuccess(BQSSApiResponseObject<BQSSWebSticker> result) {
                    android.os.Message message = android.os.Message.obtain();
                    message.arg1 = SUCCESS;
                    message.arg2 = currentPage;
                    message.obj = result.getDatas();
                    mHandler.sendMessage(message);
                    currentCount += result.getDatas().size();
                    if (result.getDatas().size() < 6 || currentCount >= MAX) {
                        mNeedLoadMore = false;
                    } else {
                        currentPage++;
                        mNeedLoadMore = true;
                    }
                }

                @Override
                public void onError(String errorInfo) {
                    android.os.Message message = android.os.Message.obtain();
                    message.arg1 = ERROR;
                    message.obj = errorInfo;
                    mHandler.sendMessage(message);
                }
            });
        }
    }

    //弹出软键盘 并且考虑各种情况
    private void showKeyboardBySoft() {
        if (mFlMore != null && mFlMore.getVisibility() == View.VISIBLE) {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            mFlMore.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFlMore.setVisibility(View.GONE);
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
            }, 200);

            mIbExpressionKeyboard.setVisibility(View.GONE);
            mIbExpression.setVisibility(View.VISIBLE);
            mEtMessage.requestFocus();
            showKeyboard();
        } else if (mRichPanel != null && mRichPanel.getVisibility() == View.VISIBLE) {
            mKeyboardPanel.setVisibility(View.VISIBLE);
            mEtMessage.requestFocus();
            showKeyboard();
        } else {
            mEtMessage.requestFocus();
            showKeyboard();
        }
    }

    //R.id.ib_more 的关闭和展开动画
    private ValueAnimator createAnimator(final View view, int start, final int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    //展开动画
    private void animateOpen(View view, int end) {
        mIbMore.setImageResource(R.drawable.cc_chat_ic_input_close);
        view.setVisibility(View.VISIBLE);

        ValueAnimator animator = createAnimator(view, 0, end);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                if(richMediaDataTwo != null && richMediaDataTwo.length>0){
                    chatRichMediaIndexes.setVisibility(View.VISIBLE);
                }else{
                    chatRichMediaIndexes.setVisibility(View.GONE);
                }
            }
        });
        animator.setDuration(400).start();
        onAnimat((int) (AndroidUtil.dip2px(getContext(), 50) + 0.5), 0, view);
    }

    //关闭动画
    private void animateClose(final View view) {
        chatRichMediaIndexes.setVisibility(View.GONE);
        int origHeight = view.getHeight();
        mIbMore.setImageResource(R.drawable.cc_chat_ic_input_more);
        ValueAnimator animator = createAnimator(view, origHeight, 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                mIsAnimating = false;
                mIbMore.setImageResource(R.drawable.cc_chat_ic_input_more);
            }
        });
        animator.setDuration(400).start();
        onAnimat(0, (int) (AndroidUtil.dip2px(getContext(), 50) + 0.5), view);
    }

    private void onAnimat(int i, int j, final View view) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(view, "translationY", i, j);
        oa.setDuration(400).start();
        oa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mOpenFlag) view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void hideSuperMsg() {

    }

    public static boolean isImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".PNG") || path.endsWith(".JPG") || path.endsWith(".jpeg") || path.endsWith(".JPEG") || path.endsWith(".GIF") || path.endsWith(".gif") || path.endsWith(".bmp") || path.endsWith(".BMP")) {
            return true;
        }
        return false;
    }

    public static boolean isVideo(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        if (path.endsWith(".rmvb") || path.endsWith(".RMVB") || path.endsWith(".mov") || path.endsWith(".mp4") || path.endsWith(".MP4") || path.endsWith(".MOV") || path.endsWith(".3gp") || path.endsWith(".3GP") || path.endsWith(".WMV") || path.endsWith(".wmv") || path.endsWith(".AVI") || path.endsWith(".avi") || path.endsWith(".FLV") || path.endsWith(".flv")) {
            return true;
        }
        return false;
    }

    /**
     * 输入的字符是否在@人之间,假如存在则把该数据清除并且返回true
     *
     * @param start 输入开始的位置
     * @return
     */
    private boolean isInsertAtPerson(int start, int before, int count) {
        if (mIsAtAll) {
            if (start > mAtAllSelectionStart && start < mAtAllSelectionEnd) {
                mIsAtAll = false;
                return true;
            }
            if (start <= mAtAllSelectionStart) {
                if (count > 0) {
                    mAtAllSelectionStart = mAtAllSelectionStart + count;
                    mAtAllSelectionEnd = mAtAllSelectionEnd + count;
                } else if (count == 0 && before > 0) {
                    mAtAllSelectionStart = mAtAllSelectionStart - before;
                    mAtAllSelectionEnd = mAtAllSelectionEnd - before;
                }
            }
        } else {
            ArrayList<AtMemberLength> memberList = getAtMemberLengthList();
            for (AtMemberLength atMemberLength : memberList) {
                if (start > atMemberLength.start && start < atMemberLength.end) {
                    memberList.remove(atMemberLength);
                    return true;
                }
                if (start <= atMemberLength.start) {
                    if (count > 0) {
                        atMemberLength.start = atMemberLength.start + count;
                        atMemberLength.end = atMemberLength.end + count;
                    } else if (count == 0 && before > 0) {
                        atMemberLength.start = atMemberLength.start - before;
                        atMemberLength.end = atMemberLength.end - before;
                    }
                }

            }
        }
        return false;
    }

    //获取对应当前会话的@人的list
    protected ArrayList<AtMemberLength> getAtMemberLengthList() {
        if (TextUtils.isEmpty(mAddress) || mapForMemberlist == null) {
            return new ArrayList<AtMemberLength>();
        }
        ArrayList<AtMemberLength> list = mapForMemberlist.get(mAddress);
        if (list == null) {
            list = new ArrayList<>();
            mapForMemberlist.put(mAddress, list);
        }
        return list;
    }

    protected boolean isAtAll() {
        return mIsAtAll;
    }

    protected void removeAtMemberLengthList() {
        if (TextUtils.isEmpty(mAddress) || mapForMemberlist == null) {
            return;
        }
        mapForMemberlist.remove(mAddress);
    }

    //用于处理@人
    protected class AtMemberLength {
        int start;
        int end;
        public GroupMember groupMember;

        public AtMemberLength(int start, int end, GroupMember groupMember) {
            this.start = start;
            this.end = end;
            this.groupMember = groupMember;
        }
    }

    @Override
    public void cleanSMSContent() {
        if (mEtSms != null) {
            mEtSms.setText("");
        }
    }

    /**
     * 富媒体数据初始化
     */
    protected void initRichMediaData() {
        if (mContext != null) {
            gridViews = new ArrayList<>();
            gridViews.clear();// 先清空

            if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) { // 单聊
                if (PhoneUtils.isNotChinaNum(mAccountUserPhone)) { // 香港号码没有红包
                    richMediaData = new int[][]{
                            {R.string.hefeixin_call_free, R.drawable.cc_chat_input_ic_hefeixin},
                            {R.string.network_call, R.drawable.cc_chat_input_ic_video},
                            {R.string.file, R.drawable.more_item_file},
                            {R.string.business_card, R.drawable.cc_chat_input_ic_card},
                            {R.string.location_standard, R.drawable.cc_chat_input_ic_position}
                    };
                } else {
                    richMediaData = new int[][]{
                            {R.string.hefeixin_call_free, R.drawable.cc_chat_input_ic_hefeixin},
                            {R.string.network_call, R.drawable.cc_chat_input_ic_video},
                            {R.string.file, R.drawable.more_item_file},
                            {R.string.business_card, R.drawable.cc_chat_input_ic_card},
                            {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                            {R.string.redpacket_, R.drawable.more_item_redpager},
//                            {R.string.web_hall_coupons, R.drawable.cc_chat_input_ic_coupon}
                    };
                }
            } else if (isGroupChat()) { // 群聊
                LogF.d(TAG, "initRichMediaData isEPGroup = " + isEPGroup + ", isPartyGroup = " + isPartyGroup + " isOwner = " + isOwner);
                if (isEPGroup) {//企业群
                    if (PhoneUtils.isNotChinaNum(mAccountUserPhone)) { // 香港号码没有红包没有群短信
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file}, //
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                                {R.string.approval, R.drawable.cc_chat_input_ic_shenpi},
                                {R.string.log, R.drawable.cc_chat_input_ic_log}
                        };
                    } else if (!AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone)) {// 异网用户没有群短信
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file},
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                                {R.string.redpacket_, R.drawable.more_item_redpager},
                                {R.string.approval, R.drawable.cc_chat_input_ic_shenpi},
                                {R.string.log, R.drawable.cc_chat_input_ic_log}
                        };
                    } else {
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file},
                                {R.string.group_sms, R.drawable.more_item_sms},
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                                {R.string.redpacket_, R.drawable.more_item_redpager},
                                {R.string.approval, R.drawable.cc_chat_input_ic_shenpi},
                                {R.string.log, R.drawable.cc_chat_input_ic_log}
                        };
//                        richMediaDataTwo = new int[][]{
//                                {R.string.web_hall_coupons, R.drawable.cc_chat_input_ic_coupon}
//                        };
                    }
                } else if (isPartyGroup) { // 党建群
                    if (PhoneUtils.isNotChinaNum(mAccountUserPhone)) { // 香港号码没有红包没有群短信
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file},
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position}
                        };
                    } else if (!AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone)) {// 异网用户没有群短信
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file},
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                                {R.string.redpacket_, R.drawable.more_item_redpager}
                        };
                    } else {
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file},
                                {R.string.group_sms, R.drawable.more_item_sms},
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                                {R.string.redpacket_, R.drawable.more_item_redpager}
                        };
                    }
                } else { // 普通群
                    if (PhoneUtils.isNotChinaNum(mAccountUserPhone)) { // 香港号码没有红包没有群短信
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file},
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position}
                        };
                    } else if (isOwner && AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone)) { // 普通群 群主并且是本网用户才支持群短信
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file},
                                {R.string.group_sms, R.drawable.more_item_sms},
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                                {R.string.redpacket_, R.drawable.more_item_redpager}
                        };
                    } else {
                        richMediaData = new int[][]{
                                {R.string.multiparty_call, R.drawable.cc_chat_input_ic_hefeixin},
                                {R.string.multi_video_call_toolbar_title, R.drawable.cc_chat_input_ic_video},
                                {R.string.file, R.drawable.more_item_file},
                                {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                                {R.string.redpacket_, R.drawable.more_item_redpager}
                        };
                    }
                }
            } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) { // 我的电脑
                richMediaData = new int[][]{
                        {R.string.file, R.drawable.more_item_file},
                        {R.string.location_standard, R.drawable.cc_chat_input_ic_position}};

            } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_MASS) { // 分组群发
                if (PhoneUtils.isNotChinaNum(mAccountUserPhone)) { // 香港号码没有红包
                    richMediaData = new int[][]{
                            {R.string.file, R.drawable.more_item_file},
                            {R.string.location_standard, R.drawable.cc_chat_input_ic_position}
                    };
                } else {
                    richMediaData = new int[][]{
                            {R.string.file, R.drawable.more_item_file},
                            {R.string.location_standard, R.drawable.cc_chat_input_ic_position},
                            {R.string.redpacket_, R.drawable.more_item_redpager}
                    };
                }
            }


            final ChatRichMediaGirdAdapter adapter = new ChatRichMediaGirdAdapter(mContext, richMediaData,AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone),PhoneUtils.isNotChinaNum(mAccountUserPhone));
            GridView gridViewTwo = null ;
            GridView gridView = new GridView(mContext);
            gridView.setNumColumns(4);
            gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
            gridView.setAdapter(adapter);
            gridView.setHorizontalScrollBarEnabled(false);
            gridView.setVerticalScrollBarEnabled(false);
            gridViews.add(gridView);
            if(richMediaDataTwo!=null && richMediaDataTwo.length>0){
                ChatRichMediaGirdAdapter adapterTwo = new ChatRichMediaGirdAdapter(mContext, richMediaDataTwo , AndroidUtil.isCMCCMobileByNumber(mAccountUserPhone),PhoneUtils.isNotChinaNum(mAccountUserPhone));
                gridViewTwo = new GridView(mContext);
                gridViewTwo.setNumColumns(4);
                gridViewTwo.setSelector(new ColorDrawable(Color.TRANSPARENT));
                gridViewTwo.setAdapter(adapterTwo);
                gridViewTwo.setHorizontalScrollBarEnabled(false);
                gridViewTwo.setVerticalScrollBarEnabled(false);
                gridViews.add(gridViewTwo);
            }

            if (mPagerAdapter == null) {
                mPagerAdapter = new ChatRichMediaViewPagerAdapter();
                chatRichMediaVp.setAdapter(mPagerAdapter);
            }
            mPagerAdapter.add(gridViews);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int itmeContent = adapter.getItem(position);
                    if (itmeContent == 0) { //
                        return;
                    }
                    if(itmeContent == R.string.hefeixin_call_free){ // 和飞行电话
                        diaHeFeiXinCall();
                    } else if(itmeContent == R.string.network_call){ // 网络通话
                        //关闭富媒体栏
                        if (chatRichMediaVp.getVisibility() == View.VISIBLE) {
                            mOpenFlag = false;
                            mIsAnimating = true;
                            animateClose(chatRichMediaVp);
                        }
                        dialNetworkCall();
                    } else if (itmeContent == R.string.multiparty_call) { // 群聊加号多方电话
                        dialMultipartyCall(ContactSelectorUtil.SOURCE_GROUP_ADD_MULTI_CALL);
                    } else if (itmeContent == R.string.multi_video_call_toolbar_title) { // 多方视频
                        dialMultipartyVideo();
                    } else if (itmeContent == R.string.file) { // 文件
                        richMediaFileItme();
                    } else if (itmeContent == R.string.business_card) { // 名片
                        richMediaCardItme();
                    } else if (itmeContent == R.string.location_standard) { // 位置
                        richMediaLocationItme();
                    } else if (itmeContent == R.string.redpacket_) { // 红包
                        richMediaRedpagerItme();
                    } else if (itmeContent == R.string.group_sms) { // 群短信
                        LimitedUserControl.limitedUserDialog(getActivity(), LoginUtils.getInstance().isLimitUser(), LimitedUserControl.GROUP_SMS, new OnLimitedUserLinster() {
                            @Override
                            public void onLimitedOperation() {

                            }

                            @Override
                            public void onNormalOperation() {
                                richMediaGroupSmsItme();
                            }
                        });

                    } else if (itmeContent == R.string.log) {
                        richMediaLogItme();
                    } else if (itmeContent == R.string.approval) {
                        richMediaApprovalItme();
                    } else if (itmeContent == R.string.web_hall_coupons) {
                        richMediaCardVoucherItem();
                    }
                }
            });
            if(gridViewTwo != null ){
                gridViewTwo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int itmeContent = adapter.getItem(position);
                        if (itmeContent == 0) { //
                            return;
                        }
                        if (itmeContent == R.string.web_hall_coupons) {
                            richMediaCardVoucherItem();
                        }
                    }
                });
                chatRichMediaVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        if(position == 0){
                            indexes_one.setBackgroundResource(R.drawable.chatrichmediaindxes);
                            indexes_two.setBackgroundResource(R.drawable.chatrichmediaindxes_nochoice);
                        }else{
                            indexes_one.setBackgroundResource(R.drawable.chatrichmediaindxes_nochoice);
                            indexes_two.setBackgroundResource(R.drawable.chatrichmediaindxes);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
        }
    }

    /**
     * 消息富媒体红点提示
     */
    protected void rmRedTipDisappear(){
        moreRedDot.setVisibility(View.GONE);
    }

    /**
     * 和飞信电话
     */
    protected void diaHeFeiXinCall(){

    }

    /**
     * 拨打网络通话
     */
    protected void dialNetworkCall(){

    }

    /**
     * 多方电话
     */
    protected void dialMultipartyCall(){

    }

    protected void dialMultipartyCall(int source){

    }

    /**
     * 多方视频
     */
    protected void dialMultipartyVideo(){

    }

    /**
     * 审批
     */
    protected void richMediaApprovalItme() {

    }

    /**
     * 日志
     */
    protected void richMediaLogItme() {

    }

    /**
     * 打开文件
     */
    private void richMediaFileItme() {
        if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {   //单聊
            UmengUtil.buryPoint(mContext, "message_p2pmessage_more_file", "消息-点对点会话-加号-文件", 0);
        } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {    //群聊
            UmengUtil.buryPoint(mContext, "message_groupmessage_more_file", "消息-群聊-加号-文件", 0);
        }
        mIbMore.setImageResource(R.drawable.cc_chat_ic_input_more);
        mIsAnimating = true;
        animateClose(chatRichMediaVp);
        chooseFile();
    }

    /**
     * 打开名片
     */
    private void richMediaCardItme() {
        if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
            UmengUtil.buryPoint(getActivity(), "message_groupmessage_contactcard", "名片", 0);
        } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
            UmengUtil.buryPoint(getActivity(), "message_p2pmessage_contactcard", "名片", 0);
        }
        Intent intent = ContactSelectorActivity.creatIntent(getActivity(), SOURCE_VCARD_SEND, 1);
        intent.putExtra(VCARD_SELECT_CONTACT, VCARD_EXPORT);
        startActivityForResult(intent, VCARD_SEND_REQUEST);
    }

    /**
     * 打开位置
     */
    private void richMediaLocationItme() {
        if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {   //单聊
            UmengUtil.buryPoint(mContext, "message_p2pmessage_more_location", "消息-点对点会话-加号-定位", 0);
        } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {    //群聊
            UmengUtil.buryPoint(mContext, "message_groupmessage_more_location", "消息-群聊-加号-定位", 0);
        }
        MessageProxy.g.getUiInterface().startLocationActivityForResult(getActivity(), MessageModuleConst.START_LOCATION_ACTIVITY_REQUEST_CODE, null);
    }

    RedPaperProgressDialog mRedPaperProgressDialog;

    /**
     * 打开红包
     */
    private void richMediaRedpagerItme() {
        if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_MASS) {
            BaseToast.show(getActivity(), getString(R.string.red_bag_unable));
            return;
        }
        mRedPaperProgressDialog = RedPaperProgressDialog.getInstance(mContext);
        mRedPaperProgressDialog.setRedPaperProgressListener(new RedPaperProgressDialog.RedPaperProgressListener() {
            @Override
            public void onSuccess() {
                goToRedpager();
            }

            @Override
            public void onFail() {
            }

            @Override
            public void onDisappear() {
            }
        });
        mRedPaperProgressDialog.show();
        mRedPaperProgressDialog.loading();
    }

    private void goToRedpager() {
        LogF.i(TAG, "goToRedpager--->进入钱包业务");
        final HbAuthCallback hbAuthCallback = new HbAuthCallback() {
            @Override
            public void onGrantedAuthSuccess() {
                if (null != mHbAuthDialog && mHbAuthDialog.isShowing()) {
                    LogF.i(TAG, "goToRedpager--->onGrantedAuthSuccess--->授权成功");
                    mHbAuthDialog.dismiss();
                    mHbAuthDialog = null;
                    BaseToast.show(R.string.addHbAuth_onSuccess);
                }

                if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    UmengUtil.buryPoint(getActivity(), "message_groupmessage_redpacket", "红包", 0);
                } else if (getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                    UmengUtil.buryPoint(getActivity(), "message_p2pmessage_redpacket", "红包", 0);
                }
                mIbMore.setImageResource(R.drawable.cc_chat_ic_input_more);
                mIsAnimating = true;
                animateClose(chatRichMediaVp);

                LoginUtils.getInstance().getLoginUserName();
                if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                    String sender = "";
                    String familyName = AboutMeProxy.g.getServiceInterface().getMyProfileFamilyName(BaseChatFragment.this.getActivity().getApplicationContext());
                    String givenName = AboutMeProxy.g.getServiceInterface().getMyProfileGiveName(BaseChatFragment.this.getActivity().getApplicationContext());
                    sender = familyName + givenName;
                    if (TextUtils.isEmpty(sender)) {
                        sender = NumberUtils.formatPerson(LoginUtils.getInstance().getLoginUserName());
                    }
                    RedpagerProxy.g.getServiceInterface().sendPerRedPacket(BaseChatFragment.this.getActivity(), sender, getAddress());
                    onSendClickReport(SEND_RED_PACKAGE_TYPE);
                } else if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    String nickName = NickNameUtils.getNickName(BaseChatFragment.this.getActivity(), LoginUtils.getInstance().getLoginUserName(), getAddress());
                    nickName = nickName.trim();
                    RedpagerProxy.g.getServiceInterface().sendGroupRedPacket(BaseChatFragment.this.getActivity(), nickName, getAddress());
                    onSendClickReport(SEND_RED_PACKAGE_TYPE);
                } else if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) {
                    BaseToast.show(getActivity(), getActivity().getString(R.string.unsupport_it));
                } else if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_MASS) {
                    BaseToast.show(getActivity(), getString(R.string.red_bag_unable));
                }
            }

            @Override
            public void onGrantedAuthFailed() {
                if (null != mHbAuthDialog && mHbAuthDialog.isShowing()) {
                    mHbAuthDialog.dismiss();
                    mHbAuthDialog = null;
                    BaseToast.show(R.string.addHbAuth_onFailed);
                }
            }

            @Override
            public void onDeniedAuth() {
                if (null != mHbAuthDialog && mHbAuthDialog.isShowing()) {
                    mHbAuthDialog.dismiss();
                    mHbAuthDialog = null;
                }
            }
        };
        SdkInitManager4Red.getInstance().queryHbAuth(mContext, new IQueryHbAuthCallback() {

            @Override
            public void onGranted() {
                LogF.i(TAG, "SdkInitManager4Red.queryHbAuth--->onGranted--->和包已授权");
                if (null != mRedPaperProgressDialog && mRedPaperProgressDialog.isShowing()) {
                    mRedPaperProgressDialog.dismiss();
                }
                // 和包已授权直接进入钱包
                hbAuthCallback.onGrantedAuthSuccess();
            }

            @Override
            public void onDenied() {
                LogF.i(TAG, "SdkInitManager4Red.queryHbAuth--->onDenied--->和包未授权");
                if (null != mRedPaperProgressDialog && mRedPaperProgressDialog.isShowing()) {
                    mRedPaperProgressDialog.dismiss();
                }
                mHbAuthDialog = HbAuthDialog.getInstance(mContext);
                mHbAuthDialog.setHbAuthCallback(hbAuthCallback);
                mHbAuthDialog.show();
            }

            @Override
            public void onFailed(String msg) {
                LogF.i(TAG, "SdkInitManager4Red.queryHbAuth--->onFailed--->msg:" + msg);
                if (null != mRedPaperProgressDialog && mRedPaperProgressDialog.isShowing()) {
                    mRedPaperProgressDialog.dismiss();
                }
                if (null != msg && (msg.toLowerCase().contains("unknownhostexception") || msg.toLowerCase().contains("connectexception"))) {
                    BaseToast.show(R.string.public_net_exception);
                } else {
                    BaseToast.show(R.string.onFailed_err);
                }
            }

            @Override
            public void onTokenError() {
                LogF.i(TAG, "SdkInitManager4Red.queryHbAuth--->onTokenError--->和包授权查询失败，token失效");
                RedpagerProxy.g.getUiInterface().initRedPaper(MyApplication.getAppContext());
                if (null != mRedPaperProgressDialog) {
                    mRedPaperProgressDialog.loading();
                }
            }
        });
    }

    /**
     * 打开群短信
     */
    private void richMediaGroupSmsItme() {
        if (isGroupChat()) {
//            ((GroupChatFragment) this).openRichMediaGroupSmsItme();
            MessageProxy.g.getUiInterface().onSendToFragmentPageEvent(this, MessageModuleConst.EventType.GROUP_CHAT_FRAGMENT_OPEN_RICH_MEDIA_GROUP_SMS_ITME);
        }
    }

    /**
     * 打开卡券
     */
    private void richMediaCardVoucherItem() {
        mIbMore.setImageResource(R.drawable.cc_chat_ic_input_more);
        chatRichMediaVp.setVisibility(View.GONE);

        LoginUtils.getInstance().getLoginUserName();
        if (BaseChatFragment.this.getChatType() ==  MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
            String sender;
            String familyName = AboutMeProxy.g.getServiceInterface().getMyProfileFamilyName(BaseChatFragment.this.getActivity().getApplicationContext());
            String givenName = AboutMeProxy.g.getServiceInterface().getMyProfileGiveName(BaseChatFragment.this.getActivity().getApplicationContext());
            sender = familyName + givenName;
            if (TextUtils.isEmpty(sender)) {
                sender = NumberUtils.formatPerson(LoginUtils.getInstance().getLoginUserName());
            }
            RedpagerProxy.g.getServiceInterface().sendPerCardBagRedPacket(BaseChatFragment.this.getActivity(),
                    sender, BaseChatFragment.this.getAddress());
        } else if (BaseChatFragment.this.getChatType() == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
            String nickName = NickNameUtils.getNickName(BaseChatFragment.this.getActivity(), LoginUtils.getInstance().getLoginUserName(), BaseChatFragment.this.getAddress());
            nickName = nickName.trim();
            RedpagerProxy.g.getServiceInterface().sendGroupCardBagRedPacket(BaseChatFragment.this.getActivity(), nickName, BaseChatFragment.this.getAddress());
        }
    }

    public String getAddress(){
        return mAddress;
    }

    @Override
    public void onCheckChange(int selectedCount) {

        if(selectedCount > 0){
            mMultiDeleteBtn.setEnabled(true);
            mMultiForwardBtn.setEnabled(true);
            mMultiDeleteBtn.setTextColor(Color.parseColor("#FF666666"));
            mMultiForwardBtn.setTextColor(Color.parseColor("#FF666666"));
        }else{
            mMultiDeleteBtn.setEnabled(false);
            mMultiForwardBtn.setEnabled(false);
            mMultiDeleteBtn.setTextColor(Color.parseColor("#FFCCCCCC"));
            mMultiForwardBtn.setTextColor(Color.parseColor("#FFCCCCCC"));
        }

        MessageDetailActivity activity = (MessageDetailActivity)getActivity();
        if(activity != null){
            activity.onCheckChange(selectedCount);
        }
    }

    public void changeMode(int mode){
        switch (mode){
            case MessageDetailActivity.INTO_MULTI_SELECT_MODE:
                //退出短信模式
                toSmSLayout(false);
                //关闭键盘，隐藏趣图
                //如果gif趣图是显示状态，就关闭gif趣图
                if (isGifViewShow) {
                    setGifView(false);
                }
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                //关闭富媒体栏
                if (chatRichMediaVp.getVisibility() == View.VISIBLE) {
                    mOpenFlag = false;
                    mIsAnimating = true;
                    animateClose(chatRichMediaVp);
                }
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if (mExpressionFragment != null && fragmentManager.findFragmentByTag("expression_fragment") != null) {
                    transaction.hide(mExpressionFragment);
                }
                //录音状态
                if (isShowAudio) {
                    isShowAudio = false;
                    if (mMessageAudioTextFragment != null && fragmentManager.findFragmentByTag("audio_fragment") != null || hasAddAudioFragment) {
                        transaction.hide(mMessageAudioTextFragment);
                    }
                    if (mRecordExitType == RECORD_IS_NOMRAL) {
                        MessageProxy.g.getUiInterface().onSendToFragmentPageEvent(mMessageAudioTextFragment, MessageModuleConst.BASE_CHAT_FRAGMENT_ETMESSAGE_FOCUSE);
                    }
                }
                transaction.commit();
                //隐藏表情栏
                if (mFlMore.getVisibility() == View.VISIBLE) {
                    mFlMore.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mFlMore.setVisibility(View.GONE); // 表情栏隐藏
                        }
                    }, 200);

                    mIbExpressionKeyboard.setVisibility(View.GONE); //  输入键盘图标按钮
                    mIbExpression.setVisibility(View.VISIBLE); // 表情图标按钮
                }

                mMultiOpreaLayout.setVisibility(View.VISIBLE);
                mRichPanel.setVisibility(View.GONE);
                mBaseInputLayout.setVisibility(View.GONE);
                mMessageChatListAdapter.setMultiSelectMode(true);
                hideToolBarMenu();
                break;
            case MessageDetailActivity.OUT_MULTI_SELECT_MODE:
                mMultiOpreaLayout.setVisibility(View.GONE);
                mRichPanel.setVisibility(View.VISIBLE);
                mBaseInputLayout.setVisibility(View.VISIBLE);
                mMessageChatListAdapter.setMultiSelectMode(false);
                mMessageChatListAdapter.clearSelection();
                showToolBarMenu();
                break;
            default:
                break;
        }

    }

    // 会话地址清空
    public void addressCleared(){}
}

