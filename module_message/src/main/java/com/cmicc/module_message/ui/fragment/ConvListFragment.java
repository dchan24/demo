package com.cmicc.module_message.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.provider.ContactsContract;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.util.StrangerEnterpriseUtil;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.CommonUtils;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.db.dao.MessageDao;
import com.chinamobile.app.yuliao_business.logic.BusinessLoginLogic;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.Group;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.BeanUtils;
import com.chinamobile.app.yuliao_business.util.BuryingPointUtils;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.PlatformUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.baseActivity.LoadingActivity;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_common.utils.permission.PermissionUtil;
import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.cmcc.cmrcs.android.osutils.AppNotificationHelper;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.activity.GroupSMSEditActivity;
import com.cmicc.module_message.ui.activity.GroupMassWelcomeActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmcc.cmrcs.android.ui.activities.HomeActivity;
import com.cmicc.module_message.ui.activity.SuperMsgActivity;
import com.cmcc.cmrcs.android.ui.activities.group.GroupListActivity;
import com.cmicc.module_message.ui.broadcast.MsgNotificationReceiver;
import com.cmicc.module_message.ui.constract.ConvListContracts;
import com.cmcc.cmrcs.android.ui.control.LimitedUserControl;
import com.cmcc.cmrcs.android.ui.dialogs.PermissionDeniedDialog;
import com.cmcc.cmrcs.android.ui.fragments.HomeFragment;
import com.cmicc.module_message.ui.presenter.ConvListPresenterImpl;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.ConvCache.CacheType;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils.LoginStateListener;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.NoDoubleClickUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.widget.ActionBarPop;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.adapter.ConvListAdapter;
import com.cmicc.module_message.ui.listener.UpdateCallingViewListener;
import com.cmicc.module_message.utils.CallViewListenerUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.google.rcszxing.activity.CaptureActivity;
import rx.functions.Func1;

import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_NEW_FREE_MSG;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_NEW_MSG;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_START_OR_NEW_GROUP;
import static com.cmicc.module_message.ui.activity.GroupMassWelcomeActivity.GROUP_MASS_FIRST_TIP_SHOW_KEY;
import static com.cmicc.module_message.ui.activity.GroupMassWelcomeActivity.GROUP_MASS_SHOW_WELCOME_PAGE;
import static com.cmcc.cmrcs.android.ui.utils.ConvCache.MY_TAG;
import static com.cmicc.module_message.ui.adapter.ConvListAdapter.mCache;


/**
 * Created by tigger on 2017/3/13.
 */

public class ConvListFragment extends HomeFragment implements ConvListContracts.View, ConvListAdapter.OnRecyclerViewItemClickListener, ConvListAdapter.OnPcOnlieItemClickListener, OnClickListener, UpdateCallingViewListener {
    private static final String TAG = "ConvListFragment";
    private static final int MAX_PERIOD = 16;

    private ConvListContracts.Presenter mPresenter;

    RecyclerView mRecyclerView;
    TextView mTvEmpty;
    ImageView mImEmpty;
    TextView mTvTitle;
    TextView mTvLoading;
    ProgressBar mPbLoading;
    private Timer timer;
    private int pointNum = 1;
    private ViewGroup mLlEmptyHint;
    private ConvListAdapter mConvListAdapter;
    private ImageView redPoint;
    /**
     * 1统一认证失败；
     * 2dm登录失败；
     * 3中兴服务器登录失败；
     * 4网络问题；
     * 5统一认证获取号码为空;
     * 7:首次登陆开启飞行模式
     */

    private View convToolbar;
    private ActionBarPop mBop;
    private boolean mNetworkTipShowed;
    private boolean mNotificationNoticeShowed;
    private boolean mHasOpen;
    private Timer mTimer;
    private long mPeriod;
    private long mTargetPeriod;
    private ConvContentObserver mObserver;

    public void initViews(View rootView) {
        super.initViews(rootView);
        mRecyclerView = rootView.findViewById(R.id.rv_conv_list);
        mTvEmpty = rootView.findViewById(R.id.tv_empty);
        mImEmpty = rootView.findViewById(R.id.im_empty);
        convToolbar = rootView.findViewById(R.id.toolbar);

        mTvTitle = rootView.findViewById(R.id.tv_title);
        mTvTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mTvLoading = rootView.findViewById(R.id.tv_loding);
        mPbLoading = rootView.findViewById(R.id.pb_loding_small);
        mLlEmptyHint = rootView.findViewById(R.id.empty_hint);
        redPoint = rootView.findViewById(R.id.red_point);

        mTvTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                AdConfigRequestUtil.hangUpShowAd(mContext);
            }
        });

        rootView.findViewById(R.id.action_add).setOnClickListener(this);

        CallViewListenerUtil.getInstance().setListener(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_conv_list;
    }

    @Override
    public void initDataLazy() {
        super.initDataLazy();
        LogF.i(TAG, "ConvListFragment init");
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity.getMultiLanChange()) {
            homeActivity.resetMultiLanChange();
            ConvCache.getInstance().clear();//在多语言设置修改的情况，先清空缓存，以免翻译无法实时显示
        }
        //设置状态栏
        StatusBarCompat.setStatusBarColor(getActivity(), getResources().getColor(R.color.color_2c2c2c));

        mPresenter = new ConvListPresenterImpl(this.getActivity(), this, getLoaderManager());

        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 12);//默认是5
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false));
        mConvListAdapter = new ConvListAdapter(this.getActivity(), CacheType.CT_ALL);
        mConvListAdapter.setHasStableIds(true);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(null);
        mConvListAdapter.setHasSearchView(true);

        mRecyclerView.setAdapter(mConvListAdapter);
        mConvListAdapter.setRecyclerViewItemClickListener(this);
        mConvListAdapter.setOnPcOnlieItemClickListener(this);


        setHasOptionsMenu(true);

        LoginUtils.getInstance().registerLoginListener(mLoginStateListener);

        //群信息变化时清除缓存
        //群数或联系人数据变化时清除缓存
        mObserver = new ConvContentObserver(null);
        mPresenter.start();
        //mPresenter.reLoadConversations();
//        mPresenter.checkPcLoginState();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.action_add) {
            UmengUtil.buryPoint(getActivity(), "message_more", "加号", 0);
            if (!NoDoubleClickUtils.isDoubleClick()) {
                showTopbarMoreItem();
            }
        }
    }

    private LoginStateListener mLoginStateListener = new LoginStateListener() {
        @Override
        public void onLoginStateChange(int state) {
            LogF.i(TAG, "--------onLoginStateChange------" + state);
            mNetworkTipShowed = false;
            switch (state) {
                case BusinessLoginLogic.MTC_REG_STATE_REGED:
                    hideLoginNotice();
                    hideLoading();
                    mNetworkTipShowed = false;
                    break;
                case BusinessLoginLogic.MTC_REG_STATE_REGING:
                    showLoading(false);
                    hideLoginNotice();
                    mNetworkTipShowed = false;
                    break;
                case BusinessLoginLogic.MTC_REG_STATE_IDLE:
                    if (!LoginUtils.getInstance().getReloginError()) {
                        if (AndroidUtil.isNetworkConnected(getActivity())) {
                            showLoading(false);
                            hideLoginNotice();
                            mNetworkTipShowed = false;
                        } else {
                            LogF.i(TAG, "--------提示网络异常------");
                            hideLoading();
                            showLoginNotice(getActivity().getResources().getString(R.string.network_error));
                            mNetworkTipShowed = true;
                        }
                    }
                    break;
            }

            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mNetworkTipShowed) {
                        showNotificationNoticeView();
                    }
                }
            });
        }
    };

    @Override
    public void onPcOnlineClick() {
        mPresenter.toMyDeviceActivity();
    }

    @Override
    public void callStatus(final int status, final long base) {
        LogF.d(TAG, "callStatus--- status:" + status + ", base:" + base);
        if (mConvListAdapter.isShowCallingView()) {
            updateCallingView(status, base);
        }
    }

    private void updateCallingView(final int status, final long base) {
        LogF.d(TAG, "updateCallingView--- status:" + status + ", base:" + base);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConvListAdapter.isShowCallingView()) {
                    if (status == MessageUtils.MSG_CALLING_VIEW_STATUS_FINISH) {
                        mConvListAdapter.removeCallingView();
                    } else {
                        int type = IPCUtils.getInstance().getCallType();
                        mConvListAdapter.updateCallingView(isVoice(type), getCallViewText(type), base);
//                        mConvListAdapter.notifyDataSetChanged();

                        RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(mConvListAdapter.getCallViewPosition());
                        if (viewHolder instanceof ConvListAdapter.CallingViewHolder) {
                            ConvListAdapter.CallingViewHolder callingViewHolder = (ConvListAdapter.CallingViewHolder) viewHolder;
                            callingViewHolder.timeCh.setVisibility(View.VISIBLE);
                            callingViewHolder.timeCh.setBase(base);
                            callingViewHolder.timeCh.start();
                        }
                    }
                }
            }
        });

    }

    private static class ConvContentObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ConvContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(Conversations.GroupMember.CONTENT_URI) || uri.equals(ContactsContract.Contacts.CONTENT_URI)) {
                NickNameUtils.clearCache();
            } else if (uri.equals(Conversations.GroupInfo.CONTENT_URI)) {
                GroupChatUtils.clearCache();
            }

        }

    }

    @Override
    public void onResumeLazy() {
        LogF.d(MY_TAG, "-----convlist fragment load done-----" + System.currentTimeMillis());
        super.onResumeLazy();
        AppNotificationHelper.getInstance().checkIfNeedShowNotificationPermissionDialog(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showNotificationNoticeView();
            }
        });

        showNotificationNoticeView();
        mHasOpen = false;
        final String account = LoginUtils.getInstance().getLoginUserName();
        boolean isShow = (Boolean) SharePreferenceUtils.getDBParam(getContext(), GROUP_MASS_FIRST_TIP_SHOW_KEY, true);
        if (PhoneUtils.isNotChinaNum(account) || !AndroidUtil.isCMCCMobileByNumber(account)) {
            redPoint.setVisibility(View.GONE);
        } else {
            redPoint.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onFragmentStartLazy() {
        super.onFragmentStartLazy();

        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTargetPeriod = 0;
        mPeriod = 0;
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mPeriod++;
                if (mPeriod >= mTargetPeriod) {
                    if (mTargetPeriod == 0) {
                        mTargetPeriod = 1;
                    } else {
                        mTargetPeriod = mTargetPeriod * 2;
                        mTargetPeriod = mTargetPeriod > MAX_PERIOD ? MAX_PERIOD : mTargetPeriod;
                    }
                    if (mPresenter != null) {
                        mPresenter.checkPcLoginState();
                    }
                    mPeriod = 0;
                }
            }
        }, 0, 1000l);

        getContext().getContentResolver().registerContentObserver(Conversations.GroupMember.CONTENT_URI, false, mObserver);
        getContext().getContentResolver().registerContentObserver(Conversations.GroupInfo.CONTENT_URI, false, mObserver);
        if(PermissionUtil.with(this).has(Manifest.permission.READ_CONTACTS)){
            getContext().getContentResolver()
                    .registerContentObserver(ContactsContract.Contacts.CONTENT_URI, false, mObserver);

        }
    }

    @Override
    //当Fragment被滑到不可见的位置，offScreen时，调用
    protected void onFragmentStopLazy() {
        super.onFragmentStopLazy();
        //发送消息通知的时候，判断是否处在聊天列表页
        MsgNotificationReceiver.setIsCurrentConvList(false);

        if (mTimer != null) {
            mTimer.cancel();
        }
        getContext().getContentResolver()
                .unregisterContentObserver(mObserver);
    }

    private final float TOOBAR_TEXT_SIZE = 20.0f;

    @Override
    protected void onAppFontSizeChanged(float scale) {
        super.onAppFontSizeChanged(scale);
        mTvTitle.setTextSize(scale * TOOBAR_TEXT_SIZE);
        mConvListAdapter.onAppFontSizeChanged(scale);
    }

    @Override
    public void onPauseLazy() {
        super.onPauseLazy();
    }

    @Override
    public void showLoading(final boolean isOfflineMsg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startTimer(isOfflineMsg);
                mTvTitle.setVisibility(View.GONE);
                mTvLoading.setVisibility(View.VISIBLE);
                mPbLoading.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideLoading() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pauseTimer();
                mTvTitle.setVisibility(View.VISIBLE);
                mTvLoading.setVisibility(View.GONE);
                mPbLoading.setVisibility(View.GONE);
            }
        });
    }

    public void showNotificationNoticeView() {
        if (!mNetworkTipShowed && AppNotificationHelper.getInstance().isNeedShowNotificationTip(getActivity())) {
            mConvListAdapter.setNotificationNoticeView(true, new ConvListAdapter.TipViewCreator() {
                @Override
                @LayoutRes
                public int layoutRes() {
                    return R.layout.item_notification_notice;
                }

                @Override
                public void onCreateView(View view) {
                    view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppNotificationHelper.goToPushNotificationSettingPage(v.getContext());
                        }
                    });
                    view.findViewById(R.id.fl_close).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppNotificationHelper.getInstance().setTipShowed();
                            hideNotificationNoticeView();
                        }
                    });
                }
            });
            mNotificationNoticeShowed = true;
        }
        if (!AppNotificationHelper.getInstance().isNeedShowNotificationTip(getActivity())
                && mNotificationNoticeShowed
                && !mNetworkTipShowed) {
            hideNotificationNoticeView();
        }
    }

    public void hideNotificationNoticeView() {
        mConvListAdapter.setNotificationNoticeView(false,null);
        mNotificationNoticeShowed = false;
    }

    @Override
    public void showLoginNotice(final String msg) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConvListAdapter.setNetErrorView(true);
            }
        });
    }

    @Override
    public void hideLoginNotice() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConvListAdapter.setNetErrorView(false);
            }
        });
    }

    @Override
    public void showPCOnline(final boolean b) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConvListAdapter.setHasPCOnLineView(b);
                    notifyDataSetChanged();
                }
            });
        }
    }

    private void updateLoadingText(final boolean isOfflineMsg) {
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        String txt = getString(R.string.login_loaing_);
                        if (isOfflineMsg) {
                            txt = getString(R.string.loading_offline_msgs_);
                        }

                        if (pointNum == 0) {
                            pointNum = 1;
                            mTvLoading.setText(txt);
                        } else if (pointNum == 1) {
                            pointNum = 2;
                            mTvLoading.setText(txt + ".");
                        } else if (pointNum == 2) {
                            pointNum = 3;
                            mTvLoading.setText(txt + "..");
                        } else if (pointNum == 3) {
                            pointNum = 0;
                            mTvLoading.setText(txt + "...");
                        }
                    }
                }
            });
        }
    }

    public void startTimer(final boolean isOfflineMsg) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();//创建Timer对象
        //执行定时任务
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateLoadingText(isOfflineMsg);
            }
        }, 0, 500);
    }

    public void pauseTimer() {
        pointNum = 0;
        if (timer != null) {
            timer.cancel();
        }
    }


    @Override
    public void notifyDataSetChanged() {

        checkAdapterItemCount();
        if (getActivity() != null) {
            int count = ConvCache.getInstance().getUnreadCountAllConvs();
            LogF.e(TAG, "AllConvs unread count = " + count);
            ((HomeActivity) getActivity()).setMessageBadge(count);
        }
        ArrayList<Conversation> list = (ArrayList<Conversation>) mCache.getCache(CacheType.CT_ALL);
        int size = list.size();
        if (size == 0) {
            mLlEmptyHint.setVisibility(View.VISIBLE);
        } else {
            mLlEmptyHint.setVisibility(View.GONE);
        }
        mConvListAdapter.notifyDataSetChanged();
    }

    private void checkAdapterItemCount() {
        if (mConvListAdapter.getItemCount() <= 0) {
            if (mImEmpty != null)
                mImEmpty.setVisibility(View.VISIBLE);
            if (mTvEmpty != null)
                mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            if (mImEmpty != null)
                mImEmpty.setVisibility(View.GONE);
            if (mTvEmpty != null)
                mTvEmpty.setVisibility(View.GONE);
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        //有时候位置为-1，应用会崩溃，后续需要优化，暂时直接解决崩溃问题
        if (position == -1 || mHasOpen) {
            return;
        }
        mHasOpen = true;
        final Conversation conversation = mConvListAdapter.getItem(position);
        mPresenter.openItem(this.getActivity(), conversation);
    }

    @Override
    public boolean onItemLongCLickListener(View v, int position) {
        final Conversation conversation = mConvListAdapter.getItem(position);
        if (conversation == null) {
            return false;
        }
        String address = conversation.getAddress();
        String title = conversation.getPerson();
        final int boxType = conversation.getBoxType();

        MessageOprationDialog messageOprationDialog;
        ArrayList<String> list = new ArrayList<>();
        if (conversation.getUnReadCount() > 0) {
            list.add(v.getContext().getString(R.string.set_read_label));
        }
        if (conversation.getTopDate() > 0) {
            list.add(v.getContext().getString(R.string.cancal_top));
        } else {
            list.add(v.getContext().getString(R.string.set_chat_top));
        }
        if (boxType != Type.TYPE_BOX_NOTIFY && boxType != Type.TYPE_BOX_MMS && boxType != Type.TYPE_BOX_SMS)
            list.add(v.getContext().getString(R.string.delete_chat));
        String[] itemList = list.toArray(new String[list.size()]);
        messageOprationDialog = new MessageOprationDialog(this.getActivity(), null, itemList, address);
        messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
            @Override
            public void onClick(String item, int which, String address) {
                if (item.equals(getString(R.string.set_read_label))) {
                    UmengUtil.buryPoint(getActivity(), "message_read", "消息已读", 0);
                    mPresenter.updateUnreadCount(getActivity(), conversation, boxType);
                } else if (item.equals(getString(R.string.set_chat_top))) {
                    UmengUtil.buryPoint(getActivity(), "message_top", "消息置顶", 0);
                    long time = System.currentTimeMillis();
                    if (ConversationUtils.setTop(getActivity(), address, time))
                        ConvCache.getInstance().updateToTop(address, CacheType.CT_ALL, time);
                } else if (item.equals(getString(R.string.delete_chat))) {
                    UmengUtil.buryPoint(getActivity(), "message_delete", "消息删除", 0);
                    if (address.equals("platform")) {
                        PlatformUtils.deleteInnerConv(getContext());
                        return;
                    }
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(BaseModel.COLUMN_NAME_STATUS, Status.STATUS_DELETE);
                    StrangerEnterpriseUtil.setNotTipState(address,false);
                    ConversationUtils.update(getActivity(), address, contentValues, boxType);
                } else if (item.equals(getString(R.string.cancal_top))) {
                    UmengUtil.buryPoint(getActivity(), "message_top_cancel", "消息-列表-取消置顶", 0);
                    if (ConversationUtils.setTop(getActivity(), address, -1))
                        ConvCache.getInstance().updateToTop(address, CacheType.CT_ALL, -1);
                }
            }
        });
        messageOprationDialog.show();
        return true;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_convlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pauseTimer();
        LoginUtils.getInstance().removeLoginListener(mLoginStateListener);
    }

    private void showTopbarMoreItem() {
        final String account = LoginUtils.getInstance().getLoginUserName();
        int id = R.array.conv_list_menu_items_title;
        int idIcon = R.array.conv_list_menu_items_icon;
        boolean isMainLandNumber = true;//是否为大陆号
        if (PhoneUtils.isNotChinaNum(account) || !AndroidUtil.isCMCCMobileByNumber(account)) {//非大陆号或非移动号
            id = R.array.conv_list_menu_items_title2;
            idIcon = R.array.conv_list_menu_items_icon2;
            isMainLandNumber = false;
        }
        String[] titles = getResources().getStringArray(id);
        TypedArray ar = getResources().obtainTypedArray(idIcon);
        final int len = ar.length();
        int[] icos = new int[len];
        for (int i = 0; i < len; i++) {
            icos[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();

        if (titles.length != icos.length) {
            Log.d(TAG, "title, icon num error, return");
            return;
        }
        setBackgroundAlpha(getActivity(), 0.5f);
        mBop = new ActionBarPop(this.getActivity(), icos, titles);
        mBop.setBackgroundDrawable(getResources().getDrawable(R.drawable.cc_chat_new_bg));

        final boolean finalIsMainLandNumber = isMainLandNumber;
        mBop.setOnActionBarPopItemClick(new ActionBarPop.OnActionBarPopItemClick() {
            @Override
            public void onActionBarPopItemClick(int position) {
                switch (position) {
                    case 0:
                        clickPopWindow(TYPE_SINGLE_CHAT);
                        break;
                    case 1:
                        if (finalIsMainLandNumber) {
                            clickPopWindow(TYPE_FREE_SMS);
                        } else {
                            clickPopWindow(TYPE_SMS);
                        }
                        break;
                    case 2:
                        clickPopWindow(TYPE_GROUP_CHAT);
                        break;
                    case 3:
                        if (AndroidUtil.isCMCCMobileByNumber(account)) {
                            clickPopWindow(TYPE_GROUP_MASS);
                        } else {
                            clickPopWindow(TYPE_GROUP_LIST);
                        }
                        break;
                    case 4:
                        clickPopWindow(TYPE_SCAN);
                        break;
                }
            }
        });

        View view = convToolbar;

        mBop.showAsDropDown(view, view.getWidth() - mBop.getWidth() - (int) AndroidUtil.dip2px(this.getActivity(), 5), (int) AndroidUtil.dip2px(this.getActivity(), -10));
        mBop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(getActivity(), 1f);
            }
        });
    }

    private int TYPE_SINGLE_CHAT = 10;//单聊
    private int TYPE_SMS = 11;//发送短信
    private int TYPE_GROUP_CHAT = 12; //群聊
    private int TYPE_GROUP_LIST = 13;//分组群发
    private int TYPE_SCAN = 14;//扫一扫
    private int TYPE_FREE_SMS = 15;//免费短信
    private int TYPE_GROUP_MASS = 16;//群发助手

    private void clickPopWindow(int type) {
        if (type == TYPE_SINGLE_CHAT) {
            BuryingPointUtils.messageEntrybPuryingPoint(mContext ,"新建消息");
            UmengUtil.buryPoint(getActivity(), "message_more_newmessage", "新建消息", 0);
//            Intent i = new Intent(ConvListFragment.this.getActivity(), ContactsSelectActivity.class);
            Intent i = ContactSelectorActivity.creatIntent(ConvListFragment.this.getActivity(), SOURCE_NEW_MSG,1);
            startActivity(i);
        } else if (type == TYPE_FREE_SMS) {
            UmengUtil.buryPoint(getActivity(), "message_more_SMS", "免费消息", 0);
            LimitedUserControl.limitedUserDialog(mContext, LoginUtils.getInstance().isLimitUser(), LimitedUserControl.FREE_MSG, new LimitedUserControl.OnLimitedUserLinster() {
                @Override
                public void onLimitedOperation() {
                }

                @Override
                public void onNormalOperation() {
                    boolean isFirstIn = (boolean) SharePreferenceUtils.getDBParam(getContext(), SuperMsgActivity.SP_KEY_FIRST_SUPER_MSG, true);
                    Log.d(TAG, "isfirstin = " + isFirstIn);
                    if (isFirstIn) {
                        startActivity(SuperMsgActivity.createIntentForSuperMsgSetting(getActivity(), 0));
                    } else {
                        Intent intent = ContactSelectorActivity.creatIntent(getActivity(), SOURCE_NEW_FREE_MSG,1);
                        startActivity(intent);
                    }
                }
            });
        } else if (type == TYPE_SMS) {
            UmengUtil.buryPoint(getActivity(), "message_more_SMS", "免费消息", 0);
            boolean isFirstIn = (boolean) SharePreferenceUtils.getDBParam(getContext(), SuperMsgActivity.SP_KEY_FIRST_SUPER_MSG, true);
            Log.d(TAG, "isfirstin = " + isFirstIn);
            if (isFirstIn) {
                startActivity(SuperMsgActivity.createIntentForSuperMsgSetting(getActivity(), 0));
            } else {
                Intent intent = ContactSelectorActivity.creatIntent(getActivity(), SOURCE_NEW_FREE_MSG,1);
                startActivity(intent);
            }

        } else if (type == TYPE_GROUP_CHAT) {
            UmengUtil.buryPoint(getActivity(), "message_more_groupmessage", "发起群聊", 0);
//            Intent intent = ContactsSelectActivity.createIntentForCreateGroup(getActivity());
            Intent intent = ContactSelectorActivity.creatIntent(mContext, SOURCE_START_OR_NEW_GROUP,500);
            startActivity(intent);
        } else if (type == TYPE_GROUP_LIST) {
            UmengUtil.buryPoint(getActivity(), "message_more_groupSMS", "分组群发", 0);
            Intent intent = new Intent(getActivity(), GroupListActivity.class);
            intent.putExtra(GroupListActivity.FROM_WHERE_KEY, GroupListActivity.FROM_GROUP_SEND_MSG);
            startActivity(intent);
        } else if (type == TYPE_SCAN) {
            // 扫一扫菜单
            UmengUtil.buryPoint(getActivity(), "message_more_scan", "扫一扫", 0);
            toScan();
        } else if (type == TYPE_GROUP_MASS) {
            boolean needShowGuide = (Boolean) SharePreferenceUtils.getDBParam(getContext(), GROUP_MASS_SHOW_WELCOME_PAGE, true);
            if (needShowGuide) {
                startActivity(GroupMassWelcomeActivity.createIntent(getContext()));
            } else {
                GroupSMSEditActivity.start(getActivity(), "", 1);
            }
            SharePreferenceUtils.setDBParam(getContext(), GROUP_MASS_FIRST_TIP_SHOW_KEY, false);
        }
    }

    /**
     * 扫一扫
     */
    private void toScan() {
        if (!AndroidUtil.isSIMReady(getContext().getApplicationContext())) {
            Toast.makeText(getActivity(), getString(R.string.insert_sim_card), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!LoginUtils.getInstance().isLogined()) {
            Intent intent = new Intent(getActivity(), LoadingActivity.class);
            intent.putExtra("TITLE", getString(R.string.menu_item_qr_scan));
            startActivity(intent);
            return;
        }
        //如果在视频通话，则不能使用扫一扫
        if (IPCUtils.getInstance().isMergeCall() && IPCUtils.getInstance().isMergeCall()) {
            CommonUtils.showToast(getString(R.string.video_call_ing), getContext());
            return;
        }
        if (IPCUtils.getInstance().isMutilVideoCall()) {
            CommonUtils.showToast(getString(R.string.multi_video_ing), getContext());
            return;
        }
        ((BaseActivity) getActivity()).requestPermissions(new BaseActivity.OnPermissionResultListener() {
            @Override
            public void onAllGranted() {
                super.onAllGranted();
                startActivity(new Intent(getActivity(), CaptureActivity.class));
            }

            @Override
            public void onAnyDenied(String[] permissions) {
                super.onAnyDenied(permissions);
            }

            @Override
            public void onAlwaysDenied(String[] permissions) {
                String message = getString(R.string.need_camera_permission);
                PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
                permissionDeniedDialog.show();
            }
        }, Manifest.permission.CAMERA);

    }

    public void clearClickConvAddress() {
        if (mConvListAdapter != null) {
            mConvListAdapter.clearClickConvAddress();
        }
    }

    @Override
    public void onDestroyViewLazy() {
        mPresenter.ondestory();
        super.onDestroyViewLazy();
    }

    @Override
    protected int getPageIndex() {
        return 0;
    }

    public void testInsertData() {
        final ProgressDialog p = new ProgressDialog(getActivity());
        p.show();
        new RxAsyncHelper("").runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                Cursor gCursor = getActivity().getContentResolver().query(Conversations.GroupInfo.CONTENT_URI,
                        new String[]{BaseModel.COLUMN_NAME_ADDRESS}, null, null, null);
                List<String> addList = new ArrayList<String>();
                if (!gCursor.isClosed() && gCursor.moveToFirst()) {
                    do {
                        addList.add(gCursor.getString(gCursor.getColumnIndex(BaseModel.COLUMN_NAME_ADDRESS)));
                    } while (gCursor.moveToNext());
                }
                Set<String> ss = new HashSet<String>();
                Set<String> sss = new HashSet<String>();
                SQLiteDatabase db = null;
                db = MessageDao.getDbHelper(getActivity()).getWritableDatabase();

                try {
                    long time = 27126437;
                    db.beginTransaction();
                    int i = 1;
                    while (i++ < 10000) {
                        Message m = new Message();
                        int num = (int) (Math.random() * 99);
                        if (num < 100) {
                            num = 100 + num;
                        }
                        m.setAddress("13802885" + num);
                        m.setBody("测测测");
                        m.setSendAddress("13802885" + num);
                        m.setRead(1);
                        m.setSeen(0);
                        m.setStatus(2);
                        m.setType(1);
                        time = time + 10;
                        m.setDate(Long.valueOf("14994" + time));

                        ss.add("13802885" + num);
                        db.insert(Message.TABLE_NAME, null,
                                BeanUtils.fillContentValuesForInsert(m));
                    }

                    while (i++ < 20000) {
                        Group g = new Group();
                        int num = (int) (Math.random() * 99);
                        if (num < 100) {
                            num = 100 + num;
                        }
                        String tmp = addList.get((int) (Math.random() * addList.size()));
                        g.setAddress(tmp);
                        g.setBody("测测测");
                        g.setSendAddress("13802885" + num);
                        g.setRead(1);
                        g.setSeen(0);
                        g.setStatus(2);
                        g.setType(1);
                        time = time + 10;
                        g.setDate(Long.valueOf("14994" + time));

                        sss.add(tmp);
                        db.insert(Group.TABLE_NAME, null,
                                BeanUtils.fillContentValuesForInsert(g));
                    }
                    db.setTransactionSuccessful();
                    SQLiteDatabase database = MessageDao.getDbHelper(getActivity())
                            .getWritableDatabase();
                    for (Object s : ss) {
                        Conversations.getInstance().onTrigger(
                                database,
                                Message.TABLE_NAME,
                                Type.TYPE_BOX_MESSAGE,
                                String.format("address='%s'",
                                        s));
                    }
                    for (Object s : sss) {
                        Conversations.getInstance().onTrigger(
                                database,
                                Group.TABLE_NAME,
                                Type.TYPE_BOX_GROUP,
                                String.format("address='%s'",
                                        s));
                    }

                    ContentResolver contentResolver = getActivity().getContentResolver();
                    contentResolver.notifyChange(Conversations.Conversation.CONTENT_URI, null);
                    contentResolver.notifyChange(Conversations.Message.CONTENT_URI, null);
                    contentResolver.notifyChange(Conversations.Group.CONTENT_URI, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO: handle exception
                } finally {
                    try {
                        if (null != db) {
                            db.endTransaction();
//                            db.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }).runOnMainThread(new Func1() {
            @Override
            public Object call(Object o) {
                p.dismiss();
                return null;
            }
        }).subscribe();
    }

    /**
     * 2018.3.28 ysf 华为手机不能变暗的bug
     * 设置页面的透明度
     *
     * @param bgAlpha 1表示不透明
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); //不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);   //此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }


    private boolean showCallingView = false;

    public void showCallingView(boolean isShow) {
        showCallingView = true;
        final int type = IPCUtils.getInstance().getCallType();
        final long time = IPCUtils.getInstance().getCallTime();
        LogF.d(TAG, "updateCallingView--- type:" + type + ", base:" + time);
        //修改多语言的时候出现crash
        Looper.myQueue().addIdleHandler(new IdleHandler() {
            @Override
            public boolean queueIdle() {
                mConvListAdapter.showCallingView(isVoice(type), getCallViewText(type), time);
                return false;
            }
        });

    }

    private boolean isVoice(int type) {
        return type == MessageUtils.MSG_CALLING_VIEW_CALLBACK_TYPE_MULTIPARTY
                || type == MessageUtils.MSG_CALLING_VIEW_CALLBACK_TYPE_SMARTVOICE;
    }

    private String getCallViewText(int type) {
        if (type == MessageUtils.MSG_CALLING_VIEW_CALLBACK_TYPE_MULTIPARTY) {
            return getString(R.string.you_calling_multi_voice);
        } else if (type == MessageUtils.MSG_CALLING_VIEW_CALLBACK_TYPE_MULTIVIDEO) {
            return getString(R.string.you_calling_multi_video);
        } else if (type == MessageUtils.MSG_CALLING_VIEW_CALLBACK_TYPE_SMARTVOICE) {
            return getString(R.string.you_calling_voice);
        } else if (type == MessageUtils.MSG_CALLING_VIEW_CALLBACK_TYPE_SMARTVIDEO) {
            return getString(R.string.you_calling_video);
        }
        return getString(R.string.you_calling_video);
    }

    public boolean isShowCallingView() {
        if (mConvListAdapter == null) {
            return false;
        } else {
            return mConvListAdapter.isShowCallingView();
        }
    }

}