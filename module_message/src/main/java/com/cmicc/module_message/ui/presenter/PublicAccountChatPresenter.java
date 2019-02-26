package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.text.TextUtils;

import com.chinamobile.app.utils.CommonConstant;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.aidl.SendServiceMsg;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.Platform;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.PlatformUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.impls.PublicAccountChatModel;
import com.cmcc.cmrcs.android.ui.model.impls.PublicAccountChatModelImpl;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.FavoriteUtil;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.ThumbnailUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.ui.constract.PublicAccountChatContract;
import com.cmicc.module_message.ui.model.impls.PublicAccountPreModelImpl;
import com.rcs.rcspublicaccount.util.PublicAccountUtil;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

/**
 * @anthor situ
 * @time 2017/7/7 15:00
 * @description 公众号会话界面
 */

public class PublicAccountChatPresenter implements PublicAccountChatContract.Presenter, PublicAccountUtil.InitRcsPublicAccountFactoryInter,
        PublicAccountUtil.SubStatusCallback, PublicAccountChatModel.PublicAccountChatLoadFinishCallback {

    private static final String TAG = PublicAccountChatPresenter.class.getSimpleName();
    private Context mContext;
    private BaseChatContract.View mView;
    private LoaderManager mLoaderManager;
    private PublicAccountChatModel mPublicAccountChatModel;
    public String mAddress;
    private long mLoadTime; //加载的最早时间
    private int mFirstLoadNum = 0;

    private boolean isInit = true;
    private boolean isPreConversation = false;
    private String mPa_name = null;// 昵称
    private String mPa_Icon = null;// 头像

    private Handler mHandler;

    public PublicAccountChatPresenter(Context context, BaseChatContract.View view, LoaderManager loaderManager, Bundle bundle, Handler handler) {
        mContext = context;
        mView = view;
        mLoaderManager = loaderManager;
        mHandler = handler;

        isPreConversation = bundle.getBoolean("preConversation", false);
        //mPublicAccountChatModel = new PublicAccountChatModelImpl();
        if (isPreConversation){
            mPublicAccountChatModel = new PublicAccountPreModelImpl();
        }else {
            mPublicAccountChatModel = new PublicAccountChatModelImpl();
        }


        mLoadTime = bundle.getLong("loadtime", 0);
        mFirstLoadNum = bundle.getInt("unread", 0);

        setupData(bundle);
    }

    private void setupData(Bundle bundle) {
        mAddress = bundle.getString("address");

        mPa_Icon = bundle.getString("iconpath");
        mPa_name = bundle.getString("name");
        if (TextUtils.isEmpty(mPa_name)) {
            mPa_name = PlatformUtils.getPlatformName(mContext, mAddress);
        }
        LogF.d(TAG, "address=========" + mAddress);
        if (isPreConversation) {
            isInit = true;
        }
        if (StringUtil.isEmpty(mAddress)) {
            mAddress = "null";
        }
        LogF.v(TAG, "getAddress() == " + mAddress);
        if (isPreConversation) {
//            loadingDialog = new DeleteProgressDialog(baseActivity, null);
//            loadingDialog.show();
        }

    }

    @Override
    public void start() {
        mPublicAccountChatModel.loadMessages(mContext, mFirstLoadNum, mAddress, mLoadTime, mLoaderManager, this);
    }

    @Override
    public void onFinishInit() {
        LogF.d(TAG, "onFinishInit success");
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void onFailInit() {

    }

    @Override
    public void callback() {

    }

    @Override
    public void init() {
        PublicAccountUtil.getInstance().setSubStatusCallback(this);
        PublicAccountUtil.getInstance().initRcsPublicAccountFactory(mContext.getApplicationContext(), this);
    }

    @Override
    public void onLoadFinished(int loadType, int searchPos, Bundle bundle) {
        mView.updateChatListView(loadType, searchPos, bundle);
    }

    @Override
    public void updateUnreadCount() {
        if (!StringUtil.isEmpty(mAddress)) {
            ConvCache.getInstance().clearUnreadNumFake(mAddress);
            new RxAsyncHelper("").runInThread(new Func1() {
                @Override
                public Object call(Object o) {
                    Message msg = PlatformUtils.getLastMessage(mContext, mAddress);
                    if (msg != null) {
                        //同步未读数到其他端
                        ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getDate(), msg.getAddress(), CommonConstant.PLATFORMCHATTYPE);
                    }
                    ConversationUtils.updateSeen(mContext, Type.TYPE_BOX_PLATFORM, mAddress, "");
                    return null;
                }
            }).subscribe();
        }
    }

    @Override
    public void loadMoreMessages() {
        mPublicAccountChatModel.loadMoreMessages(mLoaderManager);
    }

    @Override
    public void sendMessage(String message) {
        ComposeMessageActivityControl.rcsImMsgSendP(mAddress, message, message);
    }

    @Override
    public void sendAudioMessage(String path, long length) {
        long duration = length;
        if(duration<=0){  //此处使用 形参里的length， 以规避 半秒 这种情况导致的显示有误。
            duration = FileUtil.getDuring(path);
        }
        ComposeMessageActivityControl.rcsImFileTrsfP("sip:" + mAddress, path, duration);
    }

    @Override
    public void sendAudioMessage(String path, long length, String detail) {
        long duration = length;
        if(duration<=0){  //此处使用 形参里的length， 以规避 半秒 这种情况导致的显示有误。
            duration = FileUtil.getDuring(path);
        }
        ComposeMessageActivityControl.rcsImFileTrsfP("sip:" + mAddress, path, duration);
    }

    @Override
    public void sendImgAndVideo(final ArrayList<MediaItem> list, final boolean isOriginPhoto) {
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
                        rcsImFileTrsfCThumbOrder(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.RCS_PUBLIC_ACCOUNT_FILE_TRANSFER_THUMB_BY_PHOTO_ACTION, "sip:" + mAddress, list, isOriginPhoto);
                    } else {
                        ComposeMessageActivityControl.sendPlatformFileWithThumb( "sip:" + mAddress, "", item.getLocalPath(), item.getMicroThumbPath(), item.getDuration());
                    }
                } else {
                    LogF.e(TAG, "---------- mediaItem list is empty ----------");
                }
                return null;
            }
        }).subscribe();
    }

    @Override
    public void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) {
        ComposeMessageActivityControl.sendPlatformFileWithThumb("sip:" + mAddress,pcSubject, pcFileName, "", duration);
    }

    @Override
    public void clearAllMsg() {
        mPublicAccountChatModel.clearAllMsg();
    }

    @Override
    public void reSend(Message msg) {
        int type = msg.getType();
//        MessageUtils.deletePaltForm(mContext, msg.getId(), mAddress);
        switch (type) {
            case Type.TYPE_MSG_TEXT_SEND:
                ComposeMessageActivityControl.rcsImMsgReSendP((int) msg.getId(), mAddress, msg.getBody(), msg.getBody());
                break;
            default:
        }

    }

    @Override
    public void deleteMessage(Message msg) {
        Log.i(TAG, "bingle--delete Message--single");
//        PlatformUtils.delete(mContext, msg.getId());
        Platform m = new Platform();
        m.setId(msg.getId());
        m.setStatus(Status.STATUS_DELETE);
        PlatformUtils.update(mContext, m);
    }

    @Override
    public void addToFavorite(Message msg, int chatType, String address) {
        boolean collectSucc = FavoriteUtil.getInstance().addToFavorite(mContext, msg, chatType, address);
        LogF.e(TAG, "addToFavorite msg : " + msg.toString() + "\ncollectSucc:" + collectSucc);
        if (collectSucc) {
            BaseToast.show(mContext, mContext.getString(R.string.collect_succ));
        }
    }

    public void rcsImFileTrsfCThumbOrder(int userId, int action, String pcUri, List<MediaItem> list, boolean isOriginalPhoto) {
//        ArrayList<SendServiceMsg> msgs = new ArrayList<>();
        for (MediaItem item : list) {
//            SendServiceMsg msg = new SendServiceMsg();
//            msg.action = action;
//            msg.bundle.putInt(LogicActions.USER_ID, userId);
//            msg.bundle.putString(LogicActions.PARTICIPANT_URI, pcUri);
//            msg.bundle.putString(LogicActions.FILE_TRANSFER_SUBJECT, "");
//            msg.bundle.putString(LogicActions.FILE_NAME, item.getLocalPath());
//            msg.bundle.putString(LogicActions.FILE_THUMB_PATH, item.getMicroThumbPath());
//            msg.bundle.putLong(LogicActions.FILE_RECORD_DURATION, item.getDuration());
//            msg.bundle.putBoolean(LogicActions.FILE_IS_ORIGINAL_PHOTO, isOriginalPhoto);
//
//            msgs.add(msg);
            ComposeMessageActivityControl.sendPlatformFileWithThumb( "sip:" + mAddress, "", item.getLocalPath(), item.getMicroThumbPath(), item.getDuration());
        }
//        SendServiceMsg msg = new SendServiceMsg();
//        msg.action = action;
//        msg.bundle.putInt(LogicActions.USER_ID, userId);
//        msg.bundle.putString(LogicActions.PARTICIPANT_URI, pcUri);
//        msg.bundle.putParcelableArrayList(LogicActions.FILE_TRANSFER_ORDER_ITEM_LIST, msgs);
//        IPCUtils.getInstance().send(msg);
    }

    // 文件传输带缩略图
    public void rcsImFileTrsfCThumb(int userId, int action, String pcUri, String pcSubject, String pcFileName, String thumbPath, long duration) {
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = action;
        msg.bundle.putInt(LogicActions.USER_ID, userId);
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, pcUri);
        msg.bundle.putString(LogicActions.FILE_TRANSFER_SUBJECT, pcSubject);
        msg.bundle.putString(LogicActions.FILE_NAME, pcFileName);
        msg.bundle.putString(LogicActions.FILE_THUMB_PATH, thumbPath);
        msg.bundle.putLong(LogicActions.FILE_RECORD_DURATION, duration);
        IPCUtils.getInstance().send(msg);
    }

    @Override
    public void sendEditImage(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }

        MediaItem item = new MediaItem(imagePath,MediaItem.MEDIA_TYPE_IMAGE );
        ArrayList<MediaItem> list = new ArrayList<>();
        list.add(item);
        sendImgAndVideo(list, false);
    }
}
