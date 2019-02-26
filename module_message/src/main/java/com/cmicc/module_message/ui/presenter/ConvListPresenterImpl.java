package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.text.TextUtils;

import com.app.module.proxys.modulemain.MainProxy;
import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.BuryingPointUtils;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.PlatformUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.BroadcastActions;
import com.chinamobile.app.utils.CommonConstant;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_core.cmccauth.AuthWrapper;
import com.chinamobile.app.yuliao_core.cmccauth.AuthWrapper.RequestTokenListener;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.yuliao_core.juphoonwrapper.impl.JuphoonDelegate;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.ui.activity.GroupMassMsgListActivity;
import com.cmicc.module_message.ui.activity.MailMsgListActivity;
import com.cmicc.module_message.ui.activity.MailOAMsgListActivity;
import com.cmicc.module_message.ui.activity.MailOASummaryActivity;
import com.cmicc.module_message.ui.activity.SysMsgActivity;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
//import com.cmcc.cmrcs.android.ui.fragments.LabelGroupChatFragment;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.ConvCache.ConvCacheFinishCallback;
import com.cmcc.cmrcs.android.ui.utils.GlobalConfig;
import com.cmicc.module_message.utils.GroupChatCache;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmicc.module_message.utils.MessageCache;
import com.cmcc.cmrcs.android.ui.utils.MessageCursorLoader;
import com.cmcc.cmrcs.android.ui.utils.PublicAccountDefaultUtil;
import com.cmcc.cmrcs.android.ui.utils.RcsNetworkHelper;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.sso.sdk.auth.AuthnConstants;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.ConvListContracts;
import com.constvalue.MessageModuleConst;
import com.juphoon.cmcc.app.lemon.MtcCpConstants;
import com.juphoon.cmcc.app.lemon.MtcImConstants;
import com.rcs.rcspublicaccount.util.PublicAccountUtil;

import java.util.ArrayList;

import cn.com.fetion.zxing.qrcode.pclogin.ActionListener;
import cn.com.fetion.zxing.qrcode.pclogin.ActionResult;
import cn.com.fetion.zxing.qrcode.pclogin.GetPCOnlineStatusActionProxy;
import cn.com.fetion.zxing.qrcode.pclogin.GetPCOnlineStatusActionResult;
import rx.functions.Func1;


/**
 * Created by tigger on 2017/3/14.
 */

public class ConvListPresenterImpl implements ConvListContracts.Presenter, ConvCacheFinishCallback, PublicAccountUtil.InitRcsPublicAccountFactoryInter {
    private final static String TAG = "ConvListPresenterImpl";
    private ConvListContracts.View mConvListView;
    private LoaderManager mLoaderManager;
    private Context mContext;
    private boolean mIsLoading;
    private boolean mLoadOfflineDone = true;
    private boolean mLoadSmsDone = true;
    private boolean mLoadMmsDone = true;
    public static Object lock = new Object();
    private String pcToken = "";
    private String pcEpid = "";
    private static final int TOKEN_TYPE_FORCE_OFFLIE = 1;
    private static final int RETRY_GET_TOKEN = 2;
    private Handler mHandler = new Handler();



    public ConvListPresenterImpl(Context context, ConvListContracts.View convListView, LoaderManager loaderManager) {

        mContext = context;
        mConvListView = convListView;
        mLoaderManager = loaderManager;

        if (LoginUtils.getInstance().isLogined()) {
            PublicAccountUtil.getInstance().initRcsPublicAccountFactory(mContext.getApplicationContext(), this);
        }

        RcsNetworkHelper.addNetworkListener(new RcsNetworkHelper.NetworkListener() {
            @Override
            public void onNetworkChanged(int net, int preNet) {
                Log.e(TAG, "之前网络preNet：" + preNet);
                Log.e(TAG, "当前网络net：" + net);
                Log.e(TAG, "netAvailable：" + RcsNetworkHelper.netAvailable());

            }
        });

        ArrayList<Integer> actions = new ArrayList<>();
        actions.add(LogicActions.OFFLINE_MESSAGE_RECIVE_START_CB);
        actions.add(LogicActions.OFFLINE_MESSAGE_RECIVE_END_CB);
        actions.add(LogicActions.GROUP_CHAT_UPDATE);
//        actions.add(LogicActions.LOGIN_STATE_ACTION);//登录状态变化
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    @Override
    public void start() {
        loadConversations();
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            switch (action) {
                case LogicActions.GROUP_CHAT_UPDATE:
                    String groupChatId = intent.getStringExtra(LogicActions.GROUP_CHAT_ID);
                    Log.d("dchan", "onReceiveAction group chat update:" + groupChatId);
                    if (!TextUtils.isEmpty(groupChatId)) {
                        GlidePhotoLoader.getInstance(mContext).removeGroupPhotoFromCache(groupChatId);
                    }
                    break;
                case LogicActions.OFFLINE_MESSAGE_RECIVE_START_CB:
                    //                mConvListView.showLoading();
                    mLoadOfflineDone = false;
                    showLoading();
                    break;
                case LogicActions.OFFLINE_MESSAGE_RECIVE_END_CB:
                    //                mConvListView.hideLoading();
                    mLoadOfflineDone = true;
                    hideLoading();
                    break;
//                case LogicActions.LOGIN_STATE_ACTION:
//                    int state = intent.getIntExtra(LogicActions.KEY_LOGIN_STATE, -1);
//                    LogF.i("xyz-" + TAG, "登录状态：" + state);
//                    if (state == BusinessLoginLogic.MTC_REG_STATE_REGED) {//已登录
//                        PublicAccountUtil.getInstance().initRcsPublicAccountFactory(mContext.getApplicationContext(), ConvListPresenterImpl.this);
//                        mConvListView.hideLoginNotice();
//                        hideLoading();
////                        mConvListView.hideLoginNotice();
////                        showLoading();
//                    } else if (state == BusinessLoginLogic.MTC_REG_STATE_REGING) {//登录中
////                        mConvListView.showLoginNotice("正在登录...");
//                        mConvListView.hideLoginNotice();
//                        showLoading();
//                    } else if (state == BusinessLoginLogic.MTC_REG_STATE_UNREGING) {//登出中
////                        mConvListView.showLoginNotice("登出中");
//                        mConvListView.hideLoginNotice();
//                        showLoading();
//                    } else {//未登录
//                        Log.e("xyz-" + TAG, "onResume Login State:" + LoginUtils.getInstance().getLoginState());
//                        String action2 = (String) SharePreferenceUtils.getDBParam(JuphoonDelegate.SP_FILENAME_JUPHOONDELEGATE, mContext, JuphoonDelegate.KEY_ACTION, "");
//                        int failType = (int) SharePreferenceUtils.getDBParam(JuphoonDelegate.SP_FILENAME_JUPHOONDELEGATE, mContext, JuphoonDelegate.LoginFailType.FAIL_TYPE, -1);
//                        int wrongCode = (int) SharePreferenceUtils.getDBParam(JuphoonDelegate.SP_FILENAME_JUPHOONDELEGATE, mContext, JuphoonDelegate.LoginFailType.RESULT_CODE, -1);
//                        Log.e("xyz-" + TAG, "onResume : failType:" + failType + ",wrongCode:" + wrongCode + ",action:" + action2);
//                        Intent mUpViewIntent = new Intent();
//                        mUpViewIntent.setAction(action2);
//                        mUpViewIntent.putExtra(JuphoonDelegate.LoginFailType.FAIL_TYPE, failType);
//                        mUpViewIntent.putExtra(JuphoonDelegate.LoginFailType.RESULT_CODE, wrongCode);
//
////                        mConvListView.showLoginNotice("未登录" + updateLoginStateView(mContext, mUpViewIntent, true));
//                        hideLoading();
//                        mConvListView.showLoginNotice(mContext.getResources().getString(R.string.network_error));
//                    }
//                    break;
            }
        }
    };

    private int currentNotifyType = -1;
    private int wrongCode;

    private String updateLoginStateView(Context context, Intent intent, boolean onResumeUpdate) {
        String errorNoticesMsg = "";
        if (BroadcastActions.MESSAGE_LOGIN_FAILED_STATE.equals(intent.getAction())) {// 登录失败
            Log.d(TAG, "message--login failure");

            int failType = intent.getIntExtra(JuphoonDelegate.LoginFailType.FAIL_TYPE, -1);
            wrongCode = intent.getIntExtra(JuphoonDelegate.LoginFailType.RESULT_CODE, -1);
            Log.d(TAG, "failType:" + failType + "wrongCode:" + wrongCode);
            currentNotifyType = failType;
            if (failType == JuphoonDelegate.LoginFailType.AUTH) {
                errorNoticesMsg = handleAuthError(wrongCode);
            } else if (failType == JuphoonDelegate.LoginFailType.DM) {
                errorNoticesMsg = handleDMError(wrongCode);
            } else if (currentNotifyType == JuphoonDelegate.LoginFailType.FALSE_WIFI) {
                // if (wrongCode==57345 || wrongCode==57346 || wrongCode==57605 || wrongCode==57610 || wrongCode==57611) {
                errorNoticesMsg = context.getResources().getString(R.string.login_problem_reminder_no_wifi);
            } else if (currentNotifyType == JuphoonDelegate.LoginFailType.WEAK_NETWORK) {
                errorNoticesMsg = context.getResources().getString(R.string.login_problem_reminder_retry);
            }
        } else if (BroadcastActions.MESSAGE_NETWORK_FAILED_STATE.equals(intent.getAction())) {// 网络连接状态有问题
            LogF.d(TAG, "message---netWork error");
            context.removeStickyBroadcast(intent);

            int failType = intent.getIntExtra(JuphoonDelegate.LoginFailType.FAIL_TYPE, -1);
            wrongCode = intent.getIntExtra(JuphoonDelegate.LoginFailType.RESULT_CODE, -1);
            LogF.d(TAG, "failType:" + failType + "    wrongCode:" + wrongCode);
            currentNotifyType = failType;
            errorNoticesMsg = mContext.getResources().getString(R.string.network_error);

        }
//            else if (BroadcastActions.CONNECTIVITY_CHANGE_ACTION.equals(intent.getAction())) {
//                mPresenter.checkNet(URL_DM);
//            }
        if (!TextUtils.isEmpty(errorNoticesMsg)) {
            return "," + errorNoticesMsg;
        }
        return errorNoticesMsg;
    }

    // 处理双卡统一认证错误码
    private String handleAuthError(int errorCode) {
        LogF.e(TAG, "   handleAuthError()..errorCode:" + errorCode);
        String errorNoticesMsg = "login failure";
        if (errorCode == AuthnConstants.CLIENT_CODE_NETWORK_DISABLE) {
            // 102101 无网络，提示用户
            errorNoticesMsg = mContext.getResources().getString(R.string.auth_code_network_102101);
        } else if (errorCode == AuthnConstants.CLIENT_CODE_NETWORK_ERROR) {
            // 网络连接异常，提示用户，并定时重试
            errorNoticesMsg = mContext.getResources().getString(R.string.auth_code_network_102102);
        } else if (errorCode == AuthnConstants.CLIENT_CODE_DATA_SMS_FAILED) {
            // 提示用户开启短息权限
            errorNoticesMsg = "连接失败，请开启发送短息权限";
        } else {
            // 其他错误 调到短验登陆界面
            errorNoticesMsg = "连接失败：" + errorCode + "，请联系10086";
        }
        return errorNoticesMsg;
    }

    private String handleDMError(int errorCode) {
        String errorNoticesMsg = "login failure";
        switch (errorCode) {
            // 不重试，提示
            case MtcCpConstants.MTC_CP_STAT_ERR_FORBIDDEN:
            case MtcCpConstants.MTC_CP_STAT_ERR_DISABLED_TEMP:
            case MtcCpConstants.MTC_CP_STAT_ERR_DISABLED_PERM:
            case MtcCpConstants.MTC_CP_STAT_ERR_BOSS_ERROR:
            case MtcCpConstants.MTC_CP_STAT_ERR_NO_WHITE_USER:
            case MtcCpConstants.MTC_CP_STAT_ERR_BOSS_TIMEOUT:
                errorNoticesMsg = "连接失败：" + errorCode + "，请联系10086";
                break;
            default:
                break;
        }
        return errorNoticesMsg;
    }

    private synchronized void showLoading() {
        Log.d("tigger", "打开loading-----------------" + mLoadOfflineDone + " " + mLoadSmsDone);
        if (!mIsLoading) {
            mIsLoading = true;
            mConvListView.showLoading(true);
        }
    }

    private synchronized void hideLoading() {
        Log.d("tigger", "关闭loading-----------------" + mLoadOfflineDone + " " + mLoadSmsDone);
        if (mLoadOfflineDone && mLoadSmsDone) {
            mIsLoading = false;
            mConvListView.hideLoading();
        }
    }


    private boolean isNetworkConnected = true;

    public boolean checkNet(final String url) {

//        RxAsyncHelper help = new RxAsyncHelper<>("");
//        help.runInThread(new Func1<String, Integer>() {
//            @Override
//            public Integer call(String name) {
//                Runtime runtime = Runtime.getRuntime();
//                int statusCode = Status.NETWORK_STATUS_OK;
//                try{
//                    Process pingProcess = runtime.exec("/system/bin/ping -c 1 -w 10  " + url);
//                    int status = pingProcess.waitFor();
//                    statusCode = status;
//                } catch (Exception e) {
//                    //防止ping阻塞异常导致网络监测不准备，在快速移动的汽车环境下会出现
//                    try {
//                        Process pingProcess = runtime.exec("/system/bin/ping -c 1 -w 10  " + url);
//                        int status = pingProcess.waitFor();
//                        statusCode = status;
//                    } catch (Exception exception) {
//                        statusCode = Status.NETWORK_STATUS_OK;
//                        exception.printStackTrace();
//                    }
//                    e.printStackTrace();
//                }
//                return statusCode;
//            }
//        }).runOnMainThread(new Func1<Integer,Integer>() {
//            @Override
//            public Integer call(Integer code) {
//                if(code == Status.NETWORK_STATUS_OK) {
//                    Log.i(TAG, "bingle-http-network is normal");
//                    isNetworkConnected = true;
//                    int state = LoginUtils.getLoginState();
//                    if (state == BusinessLoginLogic.MTC_REG_STATE_REGED) {//已登录
//                        mConvListView.hideErrorNotice();
//                    } else if(state == BusinessLoginLogic.MTC_REG_STATE_REGING){//登录中
//                        mConvListView.showErrorNotice(mContext.getResources().getString(R.string.login_loging));
//                    } else {//未登录
//                        mConvListView.showErrorNotice(mContext.getResources().getString(R.string.login_no_logins));
//                    }
//                } else {
//                    Log.i(TAG, "bingle-http-network not work");
//                    isNetworkConnected = false;
//                    mConvListView.showErrorNotice(mContext.getResources().getString(R.string.narmal_no_network_tip));
//                }
//                return 0;
//            }
//        }).subscribe();
        return isNetworkConnected;
    }

    @Override
    public void synSms() {
        LogF.d("tigger", "开始同步短信-----------------");
        RxAsyncHelper rxAsyncHelper = new RxAsyncHelper("");
        rxAsyncHelper.runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                synchronized (lock) {
                    String user = (String) SharePreferenceUtils.getDBParam(mContext, GlobalConfig.SMS_SYS_USER, "");
                    long openDate = (Long) SharePreferenceUtils.getDBParam(mContext, GlobalConfig.SMS_SYS_TIME, Long.valueOf(0));
                    if (!LoginDaoImpl.getInstance().queryLoginUser(mContext).equals(user)) {
                        openDate = System.currentTimeMillis();
                        SharePreferenceUtils.setDBParam(mContext, GlobalConfig.SMS_SYS_USER, LoginDaoImpl.getInstance().queryLoginUser(mContext));
                        SharePreferenceUtils.setDBParam(mContext, GlobalConfig.SMS_SYS_TIME, openDate);
                    }
                    LogF.d("tigger", "短信开关开启时间-----------------" + openDate);
                    long date = MessageUtils.getLastSmsDateFromMessage(mContext);
                    LogF.d("tigger", "最新短信时间-----------------" + date);
                    if (date < openDate) {
                        date = openDate;
                    }
                    LogF.d("tigger", "最终提取时间-----------------" + date);
                    Cursor cursor = MessageUtils.getSysSmsCount(mContext, date);
                    if (cursor != null) {
                        LogF.d("tigger", "需同步的短信数量-----------------" + cursor.getCount());
                        if (cursor.getCount() > 0) {
                            mLoadSmsDone = false;
                            showLoading();

                            int count = MessageUtils.synSmsToMessage(mContext, cursor);
                            LogF.d("tigger", "同步短信数量-----------------" + count);
                        }
                        cursor.close();
                    }
                    mLoadSmsDone = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LogF.d("tigger", "关闭加载进度条-----------------");
                    hideLoading();
                    return null;
                }
            }
        }).runOnMainThread(new Func1() {
            @Override
            public Object call(Object o) {
                return null;
            }
        }).subscribe();
    }

    @Override
    public void synMms() {
//        LogF.d("tigger", "开始同步短信-----------------");
//        RxAsyncHelper rxAsyncHelper = new RxAsyncHelper("");
//        rxAsyncHelper.runInThread(new Func1() {
//            @Override
//            public Object call(Object o) {
//                synchronized (lock) {
//                    String user = (String) SharePreferenceUtils.getDBParam(mContext, GlobalConfig.SMS_SYS_USER, "");
//                    long openDate = (Long) SharePreferenceUtils.getDBParam(mContext, GlobalConfig.SMS_SYS_TIME, Long.valueOf(0));
//                    if (!LoginDaoImpl.getInstance().queryLoginUser(mContext).equals(user)) {
//                        openDate = System.currentTimeMillis();
//                        SharePreferenceUtils.setDBParam(mContext, GlobalConfig.SMS_SYS_USER, LoginDaoImpl.getInstance().queryLoginUser(mContext));
//                        SharePreferenceUtils.setDBParam(mContext, GlobalConfig.SMS_SYS_TIME, openDate);
//                    }
//                    LogF.d("tigger", "短信开关开启时间-----------------" + openDate);
//                    long date = MessageUtils.getLastMmsDateFromMessage(mContext);
//                    LogF.d("tigger", "最新短信时间-----------------" + date);
//                    if (date < openDate) {
//                        date = openDate;
//                    }
//                    LogF.d("tigger", "最终提取时间-----------------" + date);
//                    Cursor cursor = MessageUtils.getSysMmsCount(mContext, date);
//                    if (cursor != null) {
//                        LogF.d("tigger", "需同步的短信数量-----------------" + cursor.getCount());
//                        if (cursor.getCount() > 0) {
//                            mLoadMmsDone = false;
//                            showLoading();
//
//                            int count = MessageUtils.synMmsToMessage(mContext, cursor);
//                            LogF.d("tigger", "同步短信数量-----------------" + count);
//                        }
//                        cursor.close();
//                    }
//                    mLoadMmsDone = true;
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    LogF.d("tigger", "关闭加载进度条-----------------");
//                    hideLoading();
//                    return null;
//                }
//            }
//        }).runOnMainThread(new Func1() {
//            @Override
//            public Object call(Object o) {
//                return null;
//            }
//        }).subscribe();
    }

    @Override
    public void ondestory() {
        mUIObserver = null;
    }

    @Override
    public void checkPcLoginState() { //PC登陆状态
        getAccessToken();
    }

    @Override
    public void toMyDeviceActivity() {
       /* Intent intent = new Intent(mContext, MyDeviceActivity.class);
        intent.putExtra("token", pcToken);
        intent.putExtra("epid", pcEpid);
        mContext.startActivity(intent);*/
        MainProxy.g.getUiInterface().goDeviceActivity(mContext,pcToken,pcEpid);

    }

    public void updatePcLoginState() {
        new GetPCOnlineStatusActionProxy(mContext, LoginUtils.getInstance().getLoginUserName(),
                pcToken).sendAction(new ActionListener<GetPCOnlineStatusActionProxy, GetPCOnlineStatusActionResult>() {
            @Override
            public void onActionResult(GetPCOnlineStatusActionProxy actionProxy, GetPCOnlineStatusActionResult actionResult) {
                if (ActionResult.STATUS_ERROR_404 == actionResult.onlineStateCode || ActionResult.STATUS_ERROR_406 == actionResult.onlineStateCode) {
                    LogF.d(TAG, "getPcOnlineState() actionResult code=" + actionResult.onlineStateCode);
                    mConvListView.showPCOnline(false);
                } else if (ActionResult.STATUS_OK == actionResult.onlineStateCode) {
                    LogF.d(TAG, "getPcOnlineState() actionResult code=" + actionResult.onlineStateCode);
                    if (!TextUtils.isEmpty(actionResult.epid)) {
                        pcEpid = actionResult.epid;
                    }
                    mConvListView.showPCOnline(true);
                } else if (ActionResult.STATUS_ERROR_GET_TOKEN.contains(Integer.valueOf(actionResult.onlineStateCode))) {
                    LogF.d(TAG, "getPcOnlineState() actionResult code=" + actionResult.onlineStateCode);
                    pcToken = "";
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkPcLoginState();
                        }
                    }, 1000);

                } else {
                    LogF.d(TAG, "getPcOnlineState() actionResult code=" + actionResult.onlineStateCode);
                }
            }
        });
    }

    public void reLoadConversations() {
        ConvCache.getInstance().restartLoader(mLoaderManager);
    }

    public void loadConversations() {
        ConvCache.getInstance().initLoader(mContext, mLoaderManager, this);
    }

    @Override
    public void openItem(final Context context, Conversation conversation) {
        if(conversation == null){
            return;
        }
        Log.i("time debug", "time open ---" + java.lang.System.currentTimeMillis());
        LogF.i(TAG, "item click for :" + conversation.toString());
        int boxType = conversation.getBoxType();
        String clzName = null;
        final Bundle bundle = new Bundle();
        if (boxType == 2048) {
            boxType = Type.TYPE_BOX_SMS;//sms-mms
        }
        if ((boxType & Type.TYPE_BOX_MESSAGE) > 0) {
            BuryingPointUtils.messageEntrybPuryingPoint(mContext ,"消息进入点对点消息");
            UmengUtil.buryPoint(mContext, "message_p2pmessage", "消息-进入点对点会话", 0);
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS);
            bundle.putString("address", conversation.getAddress());
            bundle.putString("person", conversation.getPerson());
            bundle.putString(MessageModuleConst.INTENT_KEY_FOR_STRANGER_ENTERPRISE,conversation.getStrangerEnterprise());
            boolean isSlient = conversation.getSlientDate() > 0;
            bundle.putBoolean("slient", isSlient);
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
            if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                bundle.putString("draft", conversation.getBody());
            }
            bundle.putInt("unread", conversation.getUnReadCount());
            int pid = MessageCache.getInstance().prepareMessageCache(mContext, conversation.getAddress(), bundle);
            bundle.putBoolean("preCache", true);
            bundle.putInt("pid", pid);
            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
            LogF.i(TAG ,"open MessageDetailActivity one to one");
        } else if ((boxType & Type.TYPE_BOX_PC) > 0) {
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PC_MESSAGE_FRAGMENT_CLASS);
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
            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;

        } else if ((boxType & Type.TYPE_BOX_GROUP) > 0) {
            BuryingPointUtils.messageEntrybPuryingPoint(mContext ,"消息进入群聊");
            UmengUtil.buryPoint(mContext, "message_groupmessage", "消息-进入群聊", 0);
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
                UmengUtil.buryPoint(mContext, "message_corporategroup", "消息-进入群聊(企业群)", 0);
            } else if (conversation.getGroupType() == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY) {
                bundle.putBoolean("isPartyGroup", true);
            } else {
                UmengUtil.buryPoint(mContext, "message_commongroup", "消息-进入群聊(普通群)", 0);
            }
            //群类型,之后都用这种吧,不要传上面那个了
            bundle.putInt("grouptype", conversation.getGroupType());

            Log.i(MessageCursorLoader.MY_MESSAGE_TAG, "-------open group item-------");
            int pid = GroupChatCache.getInstance().prepareGroupChatCache(context, conversation.getAddress(), bundle);
            bundle.putBoolean("preCache", true);
            bundle.putInt("pid", pid);
            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
            LogF.i(TAG ,"open MessageDetailActivity Group");
        } else if ((boxType & Type.TYPE_BOX_SYSMSG) > 0) {
            UmengUtil.buryPoint(mContext, "message_system", "系统消息", 0);
            SysMsgActivity.show(context);

        } else if ((boxType & Type.TYPE_BOX_SMS) > 0 || (boxType & Type.TYPE_BOX_MMS) > 0) {

            Log.d("xyz", "短信消息boxType");
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
            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
        } else if ((boxType & Type.TYPE_BOX_PLATFORM) > 0 && "platform".equals(conversation.getAddress())) {
            UmengUtil.buryPoint(mContext, "message_subscriptions", "订阅号消息", 0);
//            MessageProxy.g.getUiInterface().showNotifySmsActivity(context, MessageModuleConst.NotifySmsActivityConst.SOURCE_PLATFORMCONV);
            Intent intent = MessageProxy.g.getUiInterface().getNotifySmsActivityIntent(context, MessageModuleConst.NotifySmsActivityConst.SOURCE_PLATFORMCONV);
            context.startActivity(intent);
        } else if ((boxType & Type.TYPE_BOX_PLATFORM) > 0 || (boxType & Type.TYPE_BOX_PLATFORM_DEFAULT) > 0) {
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PUBLIC_ACCOUNT_CHAT_FRAGMENT_CLASS);
            bundle.putString("name", conversation.getPerson());
            bundle.putString("address", conversation.getAddress());
            bundle.putString("clzName", clzName);
            bundle.putString("iconpath", PlatformUtils.getPlatformIcon(mContext, conversation.getAddress()));
            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
        } else if ((boxType & Type.TYPE_BOX_NOTIFY) > 0) {
            UmengUtil.buryPoint(mContext, "message_notice", "通知类短信", 0);
//            MessageProxy.g.getUiInterface().showNotifySmsActivity(context, MessageModuleConst.NotifySmsActivityConst.SOURCE_NOTIFYSMS);
            Intent intent = MessageProxy.g.getUiInterface().getNotifySmsActivityIntent(context, MessageModuleConst.NotifySmsActivityConst.SOURCE_NOTIFYSMS);
            context.startActivity(intent);

        } else if ((boxType & Type.TYPE_BOX_MAILASSISTANT) > 0) { // Mail Assistant
            UmengUtil.buryPoint(mContext, "message_139mail", "139邮箱", 0);
            Log.e(TAG, "conversation.getBoxType() = " + conversation.getBoxType());
            Log.e(TAG, "conversation.getType() = " + conversation.getType());
            Log.e(TAG, "conversation.getAddress() = " + conversation.getAddress());

            MailMsgListActivity.startMailMsgListActivity(mContext);
//            Intent intent = new Intent(baseActivity, MailMsgListActivity.class);
//            baseActivity.startActivityForResult(intent, REQUEST_CODE_FOR_OPEN_MAILASSISTANT, ConvListFragment.this);
        } else if ((boxType & Type.TYPE_BOX_MAIL_OA) > 0 || (boxType & Type.TYPE_BOX_OA) > 0) {// Mail OA
            MailOAMsgListActivity.startActivity(mContext, conversation.getAddress(), conversation.getBoxType());
        } else if ((boxType & Type.TYPE_BOX_MASS) > 0) {
            BuryingPointUtils.messageEntrybPuryingPoint(context , "消息列表-分组群发");
            clzName =  MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.LABEL_GROUP_CHAT_FRAGMENT_CLASS);
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
            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
        } else if ((boxType & Type.TYPE_BOX_ONLY_ONE_LEVEL_OA) > 0) {
            MailOASummaryActivity.startMailOASummaryActivity(context, conversation);
        } else if ((boxType & Type.TYPE_BOX_GROUP_MASS) > 0) {
            GroupMassMsgListActivity.start(context);
        }
//        updateUnreadCount(mContext, conversation);
    }

    @Override
    public void updateUnreadCount(final Context mContext, final Conversation conversation, final int boxType) {
        if (conversation.getUnReadCount() > 0) {
            final String addr = conversation.getAddress();
            ConvCache.getInstance().clearUnreadNumFake(addr);
            if (boxType == Type.TYPE_BOX_NOTIFY) {
                final ArrayList<String> unReadAddress = ConvCache.getInstance().getUnreadNotifyConvs();
                new RxAsyncHelper("").runInThread(new Func1() {
                    @Override
                    public Object call(Object o) {
                        ConversationUtils.updateSeenSpecify(mContext, unReadAddress);
                        for (String s : unReadAddress) {
                            ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, conversation.getDate(), s, CommonConstant.SINGLECHATTYPE);
                        }
                        return null;
                    }
                }).subscribe();
            } else {
                new RxAsyncHelper("").runInThread(new Func1() {
                    @Override
                    public Object call(Object o) {
                        if ((boxType & Type.TYPE_BOX_GROUP) > 0) {
                            Message msg = GroupChatUtils.getLastMessage(mContext, addr);
                            if (msg != null) {
                                String identify = msg.getIdentify();
                                if (TextUtils.isEmpty(msg.getIdentify())) {
                                    identify = GroupChatUtils.getIdentify(mContext, msg.getAddress());
                                }
                                ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getDate(), identify, CommonConstant.GROUPCHATTYPE);
                            }
                        } else if ((boxType & Type.TYPE_BOX_MESSAGE) > 0) {
                            ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, conversation.getDate(), addr, CommonConstant.SINGLECHATTYPE);
                        } else if ((boxType & Type.TYPE_BOX_PLATFORM) > 0) {
                            ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, conversation.getDate(), addr, CommonConstant.PLATFORMCHATTYPE);
                        } else if ((boxType & Type.TYPE_BOX_MAILASSISTANT) > 0) {
                            ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, conversation.getDate(), addr, CommonConstant.MAILCHATTYPE);
                        }
                        ConversationUtils.updateSeen(mContext, boxType, addr, "");
                        return null;
                    }
                }).subscribe();
            }
        }
    }

    @Override
    public void onLoadFinished(Cursor cursor) {

    }

    @Override
    public void notifyDatasetChanged() {
        mConvListView.notifyDataSetChanged();
    }

    @Override
    public void onFinishInit() {
        Log.d("public account", "onFinishInit");
        new RxAsyncHelper("").runOnMainThread(new Func1() {
            @Override
            public Object call(Object o) {
                //拉取关注列表
                PublicAccountUtil.getInstance().queryUserSub(0, 500, 1, null);
                // 手动关注默认帐号
                PublicAccountDefaultUtil publicAccountDefault = new PublicAccountDefaultUtil();
                publicAccountDefault.subscribe();
                return null;
            }
        }).subscribe();

    }

    @Override
    public void onFailInit() {
        Log.d("public account", "onFailInit");
    }

    public void getAccessToken() {
        if (!TextUtils.isEmpty(pcToken)) {
            updatePcLoginState();
            return;
        }
        AuthWrapper.getInstance(mContext).getRcsAuth(new RequestTokenListener() {
            @Override
            public void onSuccess(final String account, final String password) {

            }

            @Override
            public void onSuccess(String token) {
                LogF.d(TAG, "getAccessToken() ,onSuccess() token=" + token);
                pcToken = token;
                updatePcLoginState();
            }

            @Override
            public void onFail(final int errorCode) {
                LogF.d(TAG, "getAccessToken() ,onFail() errorCode=" + errorCode);
            }
        });
    }
}
