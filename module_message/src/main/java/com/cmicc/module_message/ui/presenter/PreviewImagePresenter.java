package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulecontact.ContactProxy;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.blankj.utilcode.util.ToastUtils;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.BroadcastActions;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.yuliao_core.cmccauth.AuthWrapper;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.chinamobile.icloud.im.sync.model.PhoneKind;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.chinamobile.icloud.im.vcard.ReadVCardAndAddContacts;
import com.cmcc.cmrcs.android.contact.observer.ContactsObserver;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmcc.cmrcs.android.ui.activities.QRCodeCommonActivity;
import com.cmcc.cmrcs.android.ui.contracts.PreviewImageContract;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmicc.module_message.ui.fragment.PreviewImageFragment;
import com.cmcc.cmrcs.android.ui.fragments.QrCodeStrangerFragment;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.MediaSet;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.ThumbnailUtils;
import com.cmcc.cmrcs.android.ui.utils.VcardContactUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.PreviewImageActivity;
import com.constvalue.MessageModuleConst;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.fetion.zxing.qrcode.activity.QRCodeManager;
import cn.com.fetion.zxing.qrcode.activity.QRCodeScanResultActivity;
import com.google.rcszxing.QRCodeUtil;
import cn.com.fetion.zxing.qrcode.activity.QueryQRCodeUtil;
import rx.functions.Func1;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_THUMB_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_TYPE;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_MESSAGE_FORWARD;
import static com.constvalue.MessageModuleConst.PreviewImagePresenterConst.FROM;
import static com.constvalue.MessageModuleConst.PreviewImagePresenterConst.FROM_CHAT_FILE_ACTIVITY;
import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;

/**
 * Created by GuoXietao on 2017/4/27.
 */

public class PreviewImagePresenter implements PreviewImageContract.IPresenter {
    private static final String TAG = PreviewImagePresenter.class.getSimpleName();
    public static final String KEY_ADDRESS = "key_address";
    public static final String KEY_MESSAGE_ID = "key_message_id";
    public static final String KEY_CONV_TYPE = "key_conv_type";
    public static final String KEY_EXT_THUMB_PATH= "key_thumb_path";//传参，先传输缩略图路径，让Activity先显示缩略图，给异步读取数据库留下时间。

    private final Context mContext;
    private final Activity mActivity;
    private PreviewImageContract.IView mView;
    private String mAddress;
    private MediaSet mMediaSet;
    private HashMap<String, String> mNeedShowTowDimension;
    private long mMessageId;//Message.id
    private int mConvType;

    private int mCurPosition;

    private int mFirstPositon;

    private String mTransfId;

    private static String tdCodeToken = "";//获取二维码的token
    private ProgressDialog waittingDialog;
    private boolean isSaving;
    private boolean isFetching;
    private boolean scan_fail;


    private ArrayList<Integer> mActions = new ArrayList<Integer>();

    public PreviewImagePresenter(Activity a) {
        mContext = a;
        mActivity = a;

        mActions.add(LogicActions.FILE_RECV_DONE);
        mActions.add(LogicActions.FILE_RECVING);
        mActions.add(LogicActions.FILE_RELEASE_CB);
        mActions.add(LogicActions.FILE_FETCH_VIA_MSRP_FAIL);
        mActions.add(LogicActions.FILE_REJECT);//向服务器发送接收文件请求， 服务器拒绝。
        mActions.add(LogicActions.FILE_RECVING_ERROR);//向服务器发送 断点续接文件， 服务器拒绝。
        mActions.add(LogicActions.GROUP_CHAT_FILE_TRANSFER_RESUME_RECV_FAIL);
        mActions.add(LogicActions.FILE_FETCH_VIA_MSRP_X_FAIL);

        UIObserverManager.getInstance().registerObserver(mUIObserver, mActions);
    }

    public void onDestory(){
        //UIObserverManager.getInstance().unregisterObserver(mUIObserver);
        UIObserverManager.getInstance().unRegisterObserver(mUIObserver, mActions);
        mUIObserver = null;
        mActions.clear();
        mActions = null;
    }


    @Override
    public void setView(PreviewImageContract.IView v) {
        mView = v;
    }

    @Override
    public int getCurPosition() {
        return mCurPosition;
    }

    @Override
    public int getFirstPositon() {
        return mFirstPositon;
    }

    @Override
    public MediaSet getMediaSet() {
        return mMediaSet;
    }


    @Override
    public void saveImage(int position) {
        if (AndroidUtil.isSdcardAvailable()) {
            if (AndroidUtil.isSdcardReady()) {
                final MediaItem item = mMediaSet.getMediaList().get(mCurPosition);
                AsyncTask<Void, Void, Boolean> mAsyncTask = new AsyncTask<Void, Void, Boolean>() {

                    @Override
                    protected Boolean doInBackground(Void... params) {
                        if (!TextUtils.isEmpty(item.getLocalPath())) {
                            File file = new File(item.getLocalPath());
                            if (file.exists()) {
                                File savefile = new File(FileUtil.getSaveDir(), file.getName());
                                if (savefile.exists()) {
                                    return true;
                                } else {
                                    // 插入系统图库，并更新媒体库
                                    boolean success = true;//FileUtil.writeBytesToFile(savefile.getAbsolutePath(), FileUtil.readFileToBytes(file));
                                    try {
                                        Files.copy(file, savefile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        success = false;
                                    }
                                    Intent intent_scan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    Uri uri = Uri.fromFile(savefile);
                                    intent_scan.setData(uri);
                                    mContext.sendBroadcast(intent_scan);
                                    return success;
                                }
                            }
                        }
                        return false;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        if (result) {
                            File file = new File(item.getLocalPath());
                            File savefile = null;
                            if (file.exists()) {
                                savefile = new File(FileUtil.getSaveDir(), file.getName());
                            }
                            if (savefile != null) {
                                Intent intent_scan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri uri = Uri.fromFile(savefile);
                                intent_scan.setData(uri);
                                mContext.sendBroadcast(intent_scan);
                            }
                            BaseToast.show(mContext, mContext.getString(R.string.save_success));
                        } else {
                            BaseToast.show(mContext, mContext.getString(R.string.toast_save_failed));
                        }
                    }

                }.execute();
            } else {

                BaseToast.show(mContext, mContext.getString(R.string.space_no_enough));
            }
        }
    }


    private static final List<String> mCompressTasks = Collections.synchronizedList(new ArrayList<String>());
    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(final int action, Intent intent) {
            Bundle bundle = intent.getExtras();
            String transId = bundle.getString(LogicActions.FILE_TRANSFER_ID);
            MediaItem item = mMediaSet.getItemByTransId(transId);
            LogF.d(TAG, "UIObserver, action: " + action + ",transId:"+ transId);
            if (action == LogicActions.FILE_RECV_DONE) {
                int fileSize = bundle.getInt(LogicActions.FILE_TOTAL_SIZE);
                item.setDownSize(fileSize);
                item.setStatus(Status.STATUS_OK);

                if(fileSize>FileUtil.MAX_IMG_SIZE){
                    compressImage(item.getLocalPath(), transId, new ImageCompressCallback() {
                        @Override
                        public void onFinish(String srcPath, Object cookie) {
                            String transId = (String) cookie;
                            if (transId != null && transId.equals(getTransfId())) {
                                mView.onFileRecvDone(true);
                            } else {
                                mView.onFileRecvDone(false);
                            }
                        }
                    });
                }else{
                    if (transId != null && transId.equals(getTransfId())) {
                        mView.onFileRecvDone(true);
                    } else {
                        mView.onFileRecvDone(false);
                    }
                }

            } else if (action == LogicActions.FILE_RECVING) {
                int recvSize = bundle.getInt(LogicActions.FILE_RECV_SIZE);
                item.setDownSize(recvSize);
                if (transId != null && transId.equals(getTransfId())) {
                    int fileSize = bundle.getInt(LogicActions.FILE_TOTAL_SIZE);
                    LogF.d(TAG, "---------- FILE_RECVING ----------" + recvSize + "/" + fileSize);
                    if (fileSize != 0) {
                        int percent =(int) (recvSize * 100L/ fileSize);
                        mView.setProgress(percent);
                    } else {

                    }
                }
            } else if (action == LogicActions.FILE_RELEASE_CB    //文件下载任务被释放。
                    || action == LogicActions.GROUP_CHAT_FILE_TRANSFER_RESUME_RECV_FAIL  //群聊文件， 尝试续传文件，建立 下载任务失败。
                    || action == LogicActions.FILE_FETCH_VIA_MSRP_FAIL   //创建文件接收任务失败。 常见于断网状态，推测会偶现于 掉线状态(?)。
                    || action == LogicActions.FILE_REJECT  // 请求接收文件， 服务器拒绝
                    || action == LogicActions.FILE_RECVING_ERROR//请求断点续接文件， 服务器拒绝， 这两种情况，多发于 服务器主动拒绝（文件超过服务器的7天缓冲时间)
                    || action == LogicActions.FILE_FETCH_VIA_MSRP_X_FAIL) {//常见于激活群失败。
                item.setStatus(Status.STATUS_DESTROY);//对于被服务器拒绝的图片，直接认为 该图片DESTORY
                if (transId != null && transId.equals(getTransfId())) {
                    if(action == LogicActions.GROUP_CHAT_FILE_TRANSFER_RESUME_RECV_FAIL || action == LogicActions.FILE_FETCH_VIA_MSRP_FAIL){
                        BaseToast.show(R.string.net_connect_error);
                    }
                    mView.onFileRecvFail(true);
                }
            }
        }
    };

    public synchronized static void compressImage(final String srcPath, final Object cookie, final ImageCompressCallback callback){
        if(mCompressTasks.contains(srcPath)){
            return;
        }
        mCompressTasks.add(srcPath);
        new Thread(){
            @Override
            public void run() {
                final String destPath = getPreviewImagePath(srcPath);
                File midFile = new File(destPath);
                if(midFile == null || !midFile.exists()){
                    boolean success = ThumbnailUtils.createPreviewImage(srcPath, destPath);
                }
                synchronized (mCompressTasks){
                    mCompressTasks.remove(srcPath);
                    if(callback!=null) {
                        callback.onFinish(srcPath, cookie);
                    }
                }
            }
        }.start();
    }

    public static String getPreviewImagePath(String srcPath){//20M以上图片， 中间图的路径
        int p = srcPath.lastIndexOf(".");
        String destPath ;
        if(p >=0){
            destPath = srcPath.substring(0, p) + "_mid_" + srcPath.substring(p);
        }else{
            destPath = srcPath  +"_mid_";
        }
        return destPath;
    }

    @Override
    public void setData(Bundle bundle) {
        mConvType = bundle.getInt(KEY_CONV_TYPE);
        mAddress = bundle.getString(KEY_ADDRESS);
        mMessageId = bundle.getLong(KEY_MESSAGE_ID);

        String from = bundle.getString(FROM, "");
        boolean sort = true;//ASC,正序
        if(FROM_CHAT_FILE_ACTIVITY.equals(from)){
            sort = false; //DESC ,逆序
        }
        mNeedShowTowDimension = new HashMap<>();

        mMediaSet = new MediaSet();
        mFirstPositon = getMediaList(mContext, mConvType, mAddress, mMessageId, mMediaSet, sort);
        mCurPosition = mFirstPositon;
    }


    /**
     * 从数据库抓取数据; 并返回 当前图片列表内编号.
     * <br/>  方法体内去掉成员变量的使用，增加内聚性。
     * @param c     Context 上下文
     * @param convType      会话天类型
     * @param address  当前会话id
     * @param key    当前图片的数据库id
     * @param dataSet   数据列表
     * @param sort ASC true-> 主界面;  DESC  false-> 聊天文件界面。
     * @return  当前 图片在MediaSet中的index
     */
    private int getMediaList(Context c, int convType, String address, long key, MediaSet dataSet, boolean sort) {
        String ad = address;
        Uri uri = Conversations.Message.CONTENT_URI;

        if (convType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
            ad = NumberUtils.getPhone(address);
            uri = Conversations.Message.CONTENT_URI;
        } else if (convType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
            uri = Conversations.Group.CONTENT_URI;
        } else if (convType == MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) {
            uri = Conversations.Message.CONTENT_URI;
        } else if (convType == MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT) {
            uri = Conversations.Platform.CONTENT_URI;
        }
        String whereAddress = Conversations.buildWhereAddress(ad);
        Cursor cursor = c.getContentResolver().query(uri,
                new String[]{Message.COLUMN_NAME_ID, Message.COLUMN_NAME_EXT_FILE_NAME, Message.COLUMN_NAME_EXT_FILE_PATH, Message.COLUMN_NAME_EXT_THUMB_PATH, Message.COLUMN_NAME_THREAD_ID,
                        Message.COLUMN_NAME_TYPE, Message.COLUMN_NAME_EXT_DOWN_SIZE, Message.COLUMN_NAME_EXT_FILE_SIZE, Message.COLUMN_NAME_EXT_SHORT_URL, Message.COLUMN_NAME_STATUS,
                        Message.COLUMN_NAME_LOCKED, Type.TYPE_BOX_MESSAGE + " as " + Message.COLUMN_NAME_BOX_TYPE, Message.COLUMN_NAME_SEND_ADDRESS, Message.COLUMN_NAME_MSG_ID},
                whereAddress + " AND (" + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? )",
                new String[]{Type.TYPE_MSG_IMG_RECV + "", Type.TYPE_MSG_IMG_SEND + "", Type.TYPE_MSG_IMG_SEND_CCIND + ""},
                Message.COLUMN_NAME_DATE+ (sort?" ASC":" DESC"));
        Log.d("tigger", "-------------getMediaList time：" + java.lang.System.currentTimeMillis());

        int index = 0;
        int result = 0;
        while (cursor.moveToNext()) {
            MediaItem item = new MediaItem(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_PATH)), MediaItem.MEDIA_TYPE_IMAGE);
            item.setThumbPath(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_THUMB_PATH)));
            item.setTag(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_THREAD_ID)));
            item.setDownSize(cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_DOWN_SIZE)));
            item.setFileLength(cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_SIZE)));
            item.setFileName(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_NAME)));
            item.setMessageType(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_TYPE)));
            item.setThreadId(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_SHORT_URL)));
            item.setSendAddress(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_SEND_ADDRESS)));
            item.setID(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_ID)));
            item.setStatus(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_STATUS)));
            item.setLocked(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_LOCKED)));
            item.setBoxType(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_BOX_TYPE)));
            item.setMsgId(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_MSG_ID)));
            item.setSelected(true);
            item.setAddress(address);
            if (item.getStatus() == Status.STATUS_DELETE) {
                continue;
            }
            dataSet.addMediaItem(item);
            if (item.getID() == key) {// 将 Message.msgId作为检索key
                result = index;
            }
            index++;
        }
        if (cursor != null) {
            cursor.close();
        }

        return result;
    }

    @Override
    public String getTransfId() {
        return mTransfId;
    }

    @Override
    public void checkImageForTowDimension() {
        checkImageForTowDimension(mCurPosition);
    }

    public void checkImageForTowDimension(int position) {
        new RxAsyncHelper(mMediaSet.getMediaList().get(position)).runInThread(new Func1<MediaItem, TwoDimensionPackage>() {
            @Override
            public TwoDimensionPackage call(MediaItem mediaItem) {
                String imagePath = mediaItem.getLocalPath();
                String qrUrl = null;
                try {
                    qrUrl = QRCodeUtil.decodeQRCode(imagePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return new TwoDimensionPackage(imagePath, qrUrl);
            }
        }).runOnMainThread(new Func1<TwoDimensionPackage, Object>() {
            @Override
            public Object call(TwoDimensionPackage twoDimensionPackage) {

                if (!TextUtils.isEmpty(twoDimensionPackage.qrUrl) && !TextUtils.isEmpty(twoDimensionPackage.imagePath)) {
                    mNeedShowTowDimension.put(twoDimensionPackage.imagePath, twoDimensionPackage.qrUrl);
                }
                return null;
            }
        }).subscribe();
    }

    @Override
    public boolean isNeedShowTowDimensionHint() {
        String loacalPath = mMediaSet.getMediaList().get(mCurPosition).getLocalPath();
        if (!TextUtils.isEmpty(loacalPath)) {
            return !TextUtils.isEmpty(mNeedShowTowDimension.get(loacalPath));
        }
        return false;
    }

    @Override
    public void handleTwoDimensionScan() {
        String loacalPath = mMediaSet.getMediaList().get(mCurPosition).getLocalPath();
        String uri = mNeedShowTowDimension.get(loacalPath);
        parseScansResult(uri);
    }

    @Override
    public void searchContactResult(SimpleContact simpleContact, String phone) {
        if (simpleContact != null) {
            ContactProxy.g.getUiInterface().getContactDetailActivityUI()
                    .showForSimpleContact(mContext ,simpleContact, 0);
        } else {
            if (!TextUtils.isEmpty(phone) && bundleForSearchContact != null) {
                if (phone.equals(bundleForSearchContact.getString("num"))) {
                    Intent intent = new Intent(mContext, QRCodeCommonActivity.class);
                    intent.putExtras(bundleForSearchContact);
                    mContext.startActivity(intent);
                }
            }
        }
    }

    @Override
    public void updateItemOK() {
        MediaItem item = mMediaSet.getMediaList().get(mCurPosition);
        item.setLocalPath(item.getLocalPath());
        item.setStatus(Status.STATUS_OK);
    }

    @Override
    public void sendImage(int position) {
        if (AndroidUtil.isSdcardAvailable()) {
            if (AndroidUtil.isSdcardReady()) {
                final MediaItem item = mMediaSet.getMediaList().get(mCurPosition);
                if (item.getStatus() == Status.STATUS_LOADING) {
                    BaseToast.show(R.string.downloading_img);
                    return;
                }

                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
                Bundle bundle = new Bundle();
                bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
                bundle.putString(MESSAGE_FILE_THUMB_PATH, item.getThumbPath());

                if(item.getFileLength()> FileUtil.MAX_IMG_SIZE){
                    File midFile = new File(PreviewImagePresenter.getPreviewImagePath(item.getLocalPath()));
                    if(midFile == null || !midFile.exists()){
                        BaseToast.show(mContext, "正在压缩");
                        return;
                    }
                    bundle.putString(MESSAGE_FILE_PATH, midFile.getPath());
                }else{
                    bundle.putString(MESSAGE_FILE_PATH, item.getLocalPath());
                }
                i.putExtras(bundle);
                mContext.startActivity(i);
            }
        }
    }

    @Override
    public void start() {

    }

    /**
     * 描述		：通过扫描二维码识别出的信息进行对应页面的跳转
     *
     * @param result 二维码成功扫描识别出的信息
     */
    private void parseScansResult(final String result) {
        //和飞信扫码登陆，进入授权登陆页面
        final Activity activity = ((PreviewImageFragment) mView).getActivity();
        if (activity == null) {
            return;
        }
        if (result.contains("sessionid")) {
            activity.finish();
            //进入圈子
        } else if (result.startsWith("circle")) {
            return;
        } else if (result.startsWith("/public-group/global/")) {
            QRCodeManager.getInstance().setShowGroupQR(true);
            QRCodeManager.getInstance().checkGroupQrResult(mContext, result, tdCodeToken);
            //二维码名片
        } else {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(200L);

            RxAsyncHelper helper = new RxAsyncHelper("");
            helper.runOnMainThread(new Func1() {
                @Override
                public Object call(Object o) {
                    if (waittingDialog == null) {
                        waittingDialog = new ProgressDialog(activity);
                        waittingDialog.setMessage(mContext.getString(R.string.wait_please));
                        waittingDialog.setCancelable(false);
                    }
                    // 在UI线程保证ContactsObserver已经初始化，避免没初始化时在doInBackground处理异常
                    ContactsObserver.init(activity.getApplicationContext());
                    try {
                        if (!waittingDialog.isShowing()) {
                            waittingDialog.show();
                            isSaving = true;
                        }

                    } catch (Exception e) {
                    }
                    return null;
                }
            }).runInThread(new Func1() {
                @Override
                public Object call(Object o) {
                    resultControl(result);
                    return null;
                }
            }).runOnMainThread(new Func1() {
                @Override
                public Object call(Object o) {
                    if (!isFetching) {
                        if (waittingDialog != null) {
                            waittingDialog.setCancelable(true);
                            waittingDialog.dismiss();
                        }
                    }
                    if (scan_fail) {
                        scan_fail = false;
                        BaseToast.makeText(mContext, mContext.getString(R.string.can_not_distinguish_QR_code), 1000).show();

                    }
                    return null;
                }
            }).subscribe();
        }
    }

    /**
     * 描述		：处理二维码名片信息
     */
    private void resultControl(final String reString) {
        String vcardString = "";
        Pattern pattern = Pattern.compile("^(http|https|ftp)\\://(([a-zA-z0-9]|-){1,}\\.){1,}[a-zA-z0-9]{1,}-*");
        Log.e(TAG, "开始时间：=" + new Date().getTime());
        Bundle bundle = new Bundle();
        bundle.putString("result", reString);
        Matcher matcher = pattern.matcher(reString);
        Intent intent = null;
        Log.e(TAG, "resultControl-result:" + reString);
        if (reString.contains("BEGIN:VCARD") && reString.contains("END:VCARD")) {
            // 判断是否是profile 生成的二维码
            VcardContactUtils.isChinaSoftQRCode = matcher.find();
            // 扫描139邮箱崩溃问题，会出现substring_end＝－1的情况
            int substring_end = reString.indexOf("BEGIN:VCARD") - 1;
            if (substring_end <= 0) {
                scan_fail = true;
                isSaving = false;
                return;
            }
            final String url = reString.substring(0, substring_end);
            Log.e(TAG, "====url======>>>" + url);

            if (AndroidUtil.isNetworkConnected(mContext)) {
                if (!TextUtils.isEmpty(tdCodeToken)) {
                    if (!TextUtils.isEmpty(url)) {
                        vcardString = QueryQRCodeUtil.getRemoteData(mContext, url, tdCodeToken);
                        Log.e(TAG, "capture:===url--result:" + vcardString);
                        if (TextUtils.isEmpty(vcardString)) {
                            vcardString = reString.substring(reString.indexOf("BEGIN:VCARD"),
                                    reString.indexOf("END:VCARD") + 9);
                        }
                    } else {
                        vcardString = reString.substring(reString.indexOf("BEGIN:VCARD"),
                                reString.indexOf("END:VCARD") + 9);
                    }
                } else {
                    new RxAsyncHelper("").runOnMainThread(new Func1() {
                        @Override
                        public Object call(Object o) {
                            if (!TextUtils.isEmpty(url)) {
                                isFetching = true;
                                getToken(url, reString);
                            } else {
                                String vcardString = reString.substring(reString.indexOf("BEGIN:VCARD"),
                                        reString.indexOf("END:VCARD") + 9);

                                showVcardUI(mContext, vcardString);
                            }
                            return null;
                        }
                    }).subscribe();

                    return;
                }
            } else {
                vcardString = reString.substring(reString.indexOf("BEGIN:VCARD"), reString.indexOf("END:VCARD") + 9);
            }
            showVcardUI(mContext, vcardString);
            return;

        }else if(reString.startsWith("http://")||reString.startsWith("https://")){
            EnterPriseProxy.g.getUiInterface().jumpToBrowserWithShare(mContext, reString);
            return;
        } else if (matcher.find()) {
            bundle.putString("type", "website");
            intent = new Intent(mContext, QRCodeScanResultActivity.class);
        } else {
            bundle.putString("type", "text");
            intent = new Intent(mContext, QRCodeScanResultActivity.class);
        }
        Log.e(TAG, "结束时间：=" + new Date().getTime());
        intent.putExtras(bundle);
        ((PreviewImageActivity) mContext).startActivityForResult(intent, 2);//create contact
    }

    /**
     *  * 方法名：getToken(final String url)
     *  * 功    能：从统一认证平台获取token，成功后发起请求获取vcardString信息
     *  * 参    数： String url - 扫描二维码得到的url
     *  * 返回值：无
     *  
     */
    private void getToken(final String url, final String reString) {
        AuthWrapper.getInstance(mContext).getRcsAuth(new AuthWrapper.RequestTokenListener() {

            @Override
            public void onFail(final int arg0) {
                Log.d(TAG, "getRcsAuth onFail arg0:" + arg0);
                if (!TextUtils.isEmpty(url)) {
                    String vcardString = reString.substring(reString.indexOf("BEGIN:VCARD"),
                            reString.indexOf("END:VCARD") + 9);
                    isFetching = false;
                    if (waittingDialog != null) {
                        waittingDialog.setCancelable(true);
                        waittingDialog.dismiss();
                    }
                }
            }

            @Override
            public void onSuccess(final String arg0) {
                Log.d(TAG, "getRcsAuth onSuccess arg0:" + arg0);
                if (arg0 != null && arg0.length() > 0) {
                    tdCodeToken = arg0;
                    if (!TextUtils.isEmpty(url)) {
                        RxAsyncHelper helper = new RxAsyncHelper("");
                        helper.runOnMainThread(new Func1() {
                            @Override
                            public Object call(Object o) {
                                String vcardString = QueryQRCodeUtil.getRemoteData(mContext, url, arg0);
                                if (TextUtils.isEmpty(vcardString)) {
                                    vcardString = reString.substring(reString.indexOf("BEGIN:VCARD"),
                                            reString.indexOf("END:VCARD") + 9);
                                }

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        isFetching = false;
                                        handleTwoDimensionScan();
                                    }
                                });
                                return null;
                            }
                        }).subscribe();
                    }
                }
            }

            @Override
            public void onSuccess(String arg0, String arg1) {
                Log.d(TAG, "getRcsAuth onSuccess arg0:" + arg0 + ";arg1" + arg1);
            }
        });
    }

    private Bundle bundleForSearchContact;

    private void showVcardUI(Context context, String vcardString) {
        Intent intent = new Intent(context, QRCodeCommonActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type", "vcard_contact");
        bundle.putString("vcard", vcardString);
        bundle.putString("title", context.getResources().getString(R.string.ac_title_qr_code_vcard));
        bundle.putString("fragment", QrCodeStrangerFragment.class.getName());
        String phoneNum = null;
        RawContact rawContact = ReadVCardAndAddContacts.createdVcardStringToContact(context, vcardString);// 根据vcardString识别出本地是否存在该联系人
        if (rawContact != null) {
            List<PhoneKind> phones = rawContact.getPhones();//获取该联系人所有的号码
            int size = phones.size();
            String number = "";
            for (int i = 0; i < size; i++) {
                if (phones.get(i).getType() == 2) {// type:TYPE_MOBILE
                    number = phones.get(i).getNumber();
                    if (number.startsWith("+86"))
                        number = number.substring(3);
                    phones.get(i).setNumber(number);
                    bundle.putString("num", number);
                    phoneNum = number;
                } else if (phones.get(i).getType() == 5) {// type,FAX_HOME:5-->FAX_WORK:4
                    phones.get(i).setType(4);
                }
            }
        }
        if (!TextUtils.isEmpty(phoneNum)) {

            String myNumber =(String) SharePreferenceUtils.getDBParam(LoginUtils.SP_FILENAME_LOGIN_INFO, context, LoginUtils.LOGIN_ACCOUNT, "");
            if(phoneNum.equals(myNumber)){
                AboutMeProxy.g.getUiInterface().goToUserProfileActivity(context);
                return;
            }
            Intent intentForBroad = new Intent(BroadcastActions.ACTION_SEARCH_CONTACT);
            intentForBroad.putExtra("number", phoneNum);
            mContext.sendBroadcast(intentForBroad);
            bundleForSearchContact = bundle;
            return;
        }
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * 用于线程间传递
     */
    private static class TwoDimensionPackage {
        private String imagePath;
        private String qrUrl;

        public TwoDimensionPackage(String imagePath, String qrUrl) {
            this.imagePath = imagePath;
            this.qrUrl = qrUrl;
        }
    }

    @Override
    public void editImage(int which) {
        final MediaItem item = mMediaSet.getMediaList().get(mCurPosition);
        File file = new File(item.getLocalPath());
        if(item.getFileLength()> FileUtil.MAX_IMG_SIZE) {//大图片
            File midFile = new File(PreviewImagePresenter.getPreviewImagePath(item.getLocalPath()));
            if (midFile == null || !midFile.exists()) {
                BaseToast.show(mContext, "正在压缩");
                return;
            }
            file = midFile;
        }

        Uri uri = Uri.fromFile(file);
        String fileName = String.valueOf(System.currentTimeMillis()) + ImgEditorProxy.g.getServiceInterface().getFinalFileNameExtensionMessageImage();
        File mCameraPicture = new File(FileUtil.getSaveDir(),fileName);
        FileUtil.createParentDir(mCameraPicture);
        String mOutputFilePath = mCameraPicture.getAbsolutePath();
        Intent intent = mActivity.getIntent();
        String from = "";
        if (intent != null) {
            from = intent.getStringExtra(FROM);
        }
        ImgEditorProxy.g.getUiInterface().goPictureEditActivity(mActivity, uri, mOutputFilePath, from);
    }

    @Override
    public void setCurrentPage(int position) {
        mCurPosition = position;
        MediaItem item = mMediaSet.getMediaList().get(position);
        String fileUri = item.getAddress();
        mTransfId = item.getThreadId();
        int id = item.getID();

        // 公众号图片
        if (item.getMessageType() == Type.TYPE_MSG_IMG_RECV && mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT) {
            LogF.i(TAG, "public image");
            return;
        }

        File file = new File(item.getLocalPath());
        long fileSize = item.getFileLength();//文件总大小
        long downSize = item.getDownSize();//文件已经下载的大小

        int percent =(int) (downSize *100/ fileSize);//下载进度,
        boolean isNotNeedDownLoad= false;

        if(item.getMessageType() ==  Type.TYPE_MSG_IMG_SEND){//本地发送的图片
            isNotNeedDownLoad = true;
        }else{
            if(file != null){
                isNotNeedDownLoad = file.exists()
                        && file.length()>= fileSize
                        && downSize>= fileSize;
            }
        }

        if(item.getStatus() == Status.STATUS_ERROR){//大图，不支持发送
            ToastUtils.showShort(R.string.big_img_unsupport);
        }

        LogF.d(TAG, "setCurrentPage: status: " + item.getStatus()+ ",path:" + item.getLocalPath() +",size:" +  item.getDownSize()+"/" + item.getFileLength()+", id:" + item.getID() +"--"+ item.getThreadId());
        if (isNotNeedDownLoad) {
            LogF.d(TAG, "noNeedDownload");
            checkImageForTowDimension();
            MessageUtils.updateReadById(mContext, item.getID());
            mView.udpateView(true, View.INVISIBLE, -1);

            if(file.length()> FileUtil.MAX_IMG_SIZE){
                File midFile = new File(getPreviewImagePath(file.getPath()));
                if(midFile !=null && midFile.exists()){//mid 文件存在

                }else{
                    mView.onFileCompressing(true);
                    compressImage(item.getLocalPath(), item.getThreadId(), new ImageCompressCallback() {
                        @Override
                        public void onFinish(String srcPath, Object cookie) {
                            String transId = (String) cookie;
                            if (transId != null && transId.equals(getTransfId())) {
                                mView.onFileRecvDone(true);
                            } else {
                                mView.onFileRecvDone(false);
                            }
                        }
                    });
                }
            }
        } else {
            if (item.getStatus() == Status.STATUS_OK) {
                if (mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT || mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) {
                    mView.udpateView(false, View.VISIBLE, 0);
                    item.setStatus(Status.STATUS_LOADING);
                    ComposeMessageActivityControl.rcsImFileFetchViaMsrp(id, fileUri, mTransfId, file.getAbsolutePath());
                } else if (mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    mView.udpateView(false, View.VISIBLE, 0);
                    item.setStatus(Status.STATUS_LOADING);
                    ComposeMessageActivityControl.rcsImFileFetchViaMsrpX(id, fileUri, mTransfId, file.getAbsolutePath());
                }
            } else if (item.getStatus() == Status.STATUS_LOADING) {
                mView.udpateView(false, View.VISIBLE, percent);
            } else if (item.getStatus() == Status.STATUS_PAUSE || item.getStatus() == Status.STATUS_WAITING) {
                int iStartOffset = (int) downSize + 1;
                int iStopOffset = (int) fileSize;
                //如果是发送图片，压缩图被删除，不要去服务器拉图片
                if (item.getMessageType() == Type.TYPE_MSG_IMG_SEND) {
                    return;
                }
                if (mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT || mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) {
                    mView.udpateView(false, View.VISIBLE, percent);
                    item.setStatus(Status.STATUS_LOADING);
                    ComposeMessageActivityControl.rcsImFileResumeByRecver(id, fileUri, mTransfId, file.getAbsolutePath(), iStartOffset, iStopOffset);
                } else if (mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    mView.udpateView(false, View.VISIBLE, percent);
                    item.setStatus(Status.STATUS_LOADING);
                    ComposeMessageActivityControl.rcsImFileResumeByRecverX(id, fileUri, mTransfId, file.getAbsolutePath(), iStartOffset, iStopOffset);
                }
            } else if (item.getStatus() == Status.STATUS_FAIL) {
                if (mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT || mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) {
                    mView.udpateView(false, View.VISIBLE, 0);
                    item.setStatus(Status.STATUS_LOADING);
                    ComposeMessageActivityControl.rcsImFileFetchViaMsrp(id, fileUri, mTransfId, file.getAbsolutePath());
                } else if (mConvType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    mView.udpateView(false, View.VISIBLE, 0);
                    item.setStatus(Status.STATUS_LOADING);
                    ComposeMessageActivityControl.rcsImFileFetchViaMsrpX(id, fileUri, mTransfId, file.getAbsolutePath());
                }
            } else if (item.getStatus() == Status.STATUS_DESTROY) {
                mView.udpateView(true, View.INVISIBLE, -1);
                mView.onFileRecvFail(true);
            }
        }
    }

    @Override
    public boolean isEditable() {
        final MediaItem item = mMediaSet.getMediaList().get(mCurPosition);
        File file = new File(item.getLocalPath());
        boolean hasDownloadFinsh = file.exists() && file.length() >= item.getFileLength();
        if(hasDownloadFinsh){
            if(file.length()> FileUtil.MAX_IMG_SIZE){//大图
                File midFile = new File( PreviewImagePresenter.getPreviewImagePath(file.getPath()));
                    if(midFile!= null && midFile.exists()){//中间图存在
                        return true;
                    }else{
                        return false;
                    }
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public void compressImage(final MediaItem item){
        new Thread(){
            @Override
            public void run() {
                String path = item.getLocalPath();
                String destPath = getPreviewImagePath(path);
                boolean success = ThumbnailUtils.createPreviewImage(path, destPath);

                String transId = item.getThreadId();//压缩结束，显示结束的动画
                if (transId != null && transId.equals(getTransfId())) {
                    mView.onFileRecvDone(true);
                } else {
                    mView.onFileRecvDone(false);
                }
            }
        }.start();
    }


    public interface ImageCompressCallback{
        public void onFinish(String srcPath, Object cookie);
    }
}
