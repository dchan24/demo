package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.module.proxys.moduleoffice.OfficeProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.YunFile;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.cmcc.cmrcs.android.data.yunfile.DownLoadListener;
import com.cmcc.cmrcs.android.data.yunfile.DownLoadTask;
import com.cmcc.cmrcs.android.data.yunfile.DownloadManager;
import com.cmcc.cmrcs.android.data.yunfile.YunFileDownLoadListener;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.dialogs.FileMenuDialog;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.utils.YunFileXmlParser;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by tigger on 2017/7/6.
 * 文件详情界面
 */

public class FileDetailActivity extends BaseActivity {

    public static final String KEY_DIALOG_ARGS = "key_dialog_args";//用户
    public static final String KEY_MESSAGE_ID = "key_message_id";
    public static final String KEY_ADDRESS = "key_address";
    public static final String KEY_TYPE = "key_type";
    public static final String KEY_STATUS = "key_status";
    public static final String KEY_DOWN_PERCENT = "key_down_percent";
    public static final int TYPE_NORMAL = 50;
    public static final int TYPE_GROUP = 51;
    public static final int TYPE_CLOUD_FILE = 52;

    private ImageView ivFileIcon;
    private TextView tvFileName;
    private RelativeLayout mLayoutLoading;
    private TextView mTvProgress;
    private ProgressBar pbLoading;
    private Button btnOpenFile;
    private Button btnDownloadFile;
    private TextView mFileSize;
    private TextView mTitle;
    private View mBack;
    private ImageView mMenuView;

    private boolean fromBlackList = false;

    private int mDownPercent;
    private int type = -1;
    private int status = -1;
    private Context mContext;


    private Message mMessage;
    private DownLoadTask mTask = null;//和彩云文件， 对应的是一个DownLoadTask

    private Bundle mOpenFileArgs;// 用来包括打开文件(收藏)所用的参数。
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_detail);
    }

    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(FileDetailActivity.KEY_DIALOG_ARGS);
        mMessage = (Message) bundle.getSerializable(FileMenuDialog.OPEN_FILE_ARG_MESSAGE);

        String thisFile;
        if (mMessage.getType() == Type.TYPE_MSG_FILE_YUN_SEND
                ||mMessage.getType() == Type.TYPE_MSG_FILE_YUN_RECV) {//和彩云文件


            final YunFile yunFile = YunFileXmlParser.parserYunFileXml(mMessage.getBody());

            String localPath = yunFile.getLocalPath();
            long fileLength = yunFile.getFileSize();

            mTask = DownloadManager.getInstance().getTask(yunFile.getFileUrl(), localPath, fileLength, mMessage);
            setYunFileDownloadListener(mTask);
            checkCloudDownload(mTask);

            thisFile = localPath;
        } else {//菊风文件
            String address = getIntent().getStringExtra(KEY_ADDRESS);
            int id = getIntent().getIntExtra(KEY_MESSAGE_ID, 0);
            mDownPercent = getIntent().getIntExtra(KEY_DOWN_PERCENT, 0);
            fromBlackList = getIntent().getBooleanExtra("FROMBLACKLIST", false);
            status = mMessage.getStatus();


            if(address.contains("-") && address.length()> 20){//群聊
                mMessage = getGroupMediaList(id, address);
            }else{
                mMessage = getMediaList(id, address);
            }

            ArrayList<Integer> actions = new ArrayList<Integer>();
            actions.add(LogicActions.FILE_RECV_DONE);
            actions.add(LogicActions.FILE_RECVING);
            UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
            checkDownload();
            thisFile = mMessage.getExtFilePath();
        }

        mTitle.setGravity(Gravity.START);
        mTitle.setText(getString(R.string.file_detail));
        mContext = this;
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //收藏所需要的参数，需要从外部传进来。
        mOpenFileArgs = getIntent().getBundleExtra(KEY_DIALOG_ARGS);
        if(mOpenFileArgs == null){
            mOpenFileArgs = new Bundle();
        }

        if(OfficeProxy.g.getUiInterface().isSupport(thisFile)){
            mOpenFileArgs.putInt(FileMenuDialog.OPEN_FILE_ARG_FROM, FileMenuDialog.OPEN_FILE_FROM_FILE_DETAIL_ACTIVITY_X);//用 Offce来预览
        }else{
            mOpenFileArgs.putInt(FileMenuDialog.OPEN_FILE_ARG_FROM, FileMenuDialog.OPEN_FILE_FROM_FILE_DETAIL_ACTIVITY); // 右上角按钮
        }
    }

    @Override
    protected void findViews() {
        mTitle = findViewById(R.id.title);
        mBack = findViewById(R.id.back);
        mMenuView = findViewById(R.id.menu);

        ivFileIcon = findViewById(R.id.iv_file_icon);
        tvFileName = findViewById(R.id.tv_file_name);
        mFileSize = findViewById(R.id.file_size);
        mLayoutLoading = findViewById(R.id.layout_progress);
        mTvProgress = findViewById(R.id.tv_progress);
        pbLoading = findViewById(R.id.progress_loading);
        pbLoading.setProgress(mDownPercent);

        btnOpenFile = findViewById(R.id.btn_open);
        btnDownloadFile = findViewById(R.id.btn_download);

        btnOpenFile.setOnClickListener(new NoDoubleClickListener());
        mTvProgress.setOnClickListener(new NoDoubleClickListener());
        btnDownloadFile.setOnClickListener(new NoDoubleClickListener());

        enableMenu(false);
        initData();

    }

    private void enableMenu(boolean e){
        if (e) {
            mMenuView.setImageResource(R.drawable.cc_chat_file_more_normal);
            mMenuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileMenuDialog dialog = new FileMenuDialog();
                    dialog.setArguments(mOpenFileArgs);
                    dialog.show(getSupportFragmentManager(), "");
                }
            });
        } else {
            mMenuView.setImageResource(R.drawable.cc_chat_file_more_disabled);
            mMenuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseToast.show(FileDetailActivity.this, FileDetailActivity.this.getString(R.string.toast_download_file));
                }
            });
        }
    }
    @Override
    protected void init() {

    }

    private void setFileIcon(String fileName) {
        // 文件图标

        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_word);
        } else if (fileName.endsWith(".txt")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_file);
        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".gif")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_photo);
        } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".3ga") || fileName.endsWith(".amr")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_music);
        } else if (fileName.endsWith(".mp4") || fileName.endsWith(".3gp")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_vide);
        } else if (fileName.endsWith(".ppt") || fileName.endsWith(".ppts") || fileName.endsWith(".pptx")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_ppt);
        } else if (fileName.endsWith(".zip")) {
            ivFileIcon.setImageResource(R.drawable.cc_chat_collect_zip);
        } else if (fileName.endsWith(".pdf")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_pdf);
        } else if (fileName.endsWith(".rar")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_rar);
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_excer);
        } else if (fileName.endsWith(".psd") || fileName.endsWith(".PSD")) {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_psd);
        } else {
            ivFileIcon.setImageResource(R.drawable.message_files_icon_content_unknown);
        }
    }

    /**
     * 是否已下载
     * 菊风文件。
     */
    private void checkDownload() {
        if (mMessage == null) {
            return;
        }

        int itemType = mMessage.getType();

        if (mMessage.getType() == Type.TYPE_MSG_FILE_YUN_RECV
                || mMessage.getType() == Type.TYPE_MSG_FILE_YUN_SEND) {
            tvFileName.setText(mMessage.getExtFileName());
            btnOpenFile.setVisibility(View.VISIBLE);
            btnDownloadFile.setVisibility(View.GONE);
            mLayoutLoading.setVisibility(View.GONE);
            setFileIcon(mMessage.getExtFileName());
            return;
        }

        tvFileName.setText(mMessage.getExtFileName());
        mFileSize.setText(StringUtil.formetFileSize(mMessage.getExtFileSize()));
        if (itemType == Type.TYPE_MSG_FILE_SEND) {
            btnOpenFile.setVisibility(View.VISIBLE);
            btnDownloadFile.setVisibility(View.GONE);
            mLayoutLoading.setVisibility(View.GONE);
        } else if (itemType == Type.TYPE_MSG_FILE_RECV) {
            String filePath = mMessage.getExtFilePath();
            File file = new File(filePath);

            if ((status == Status.STATUS_OK && !file.exists()) || status == Status.STATUS_FAIL) {
                btnOpenFile.setVisibility(View.GONE);
                btnDownloadFile.setVisibility(View.VISIBLE);
                mLayoutLoading.setVisibility(View.GONE);
            } else if (status == Status.STATUS_PAUSE) {
                btnOpenFile.setVisibility(View.GONE);
                btnDownloadFile.setVisibility(View.GONE);
                mLayoutLoading.setVisibility(View.VISIBLE);
                mTvProgress.setText(getString(R.string.download_continue)+"(" + mDownPercent + "%)");
            } else if (status == Status.STATUS_OK && file.exists()) {
                btnOpenFile.setVisibility(View.VISIBLE);
                btnDownloadFile.setVisibility(View.GONE);
                mLayoutLoading.setVisibility(View.GONE);
            } else if (status == Status.STATUS_LOADING) {
                btnOpenFile.setVisibility(View.GONE);
                btnDownloadFile.setVisibility(View.GONE);
                mLayoutLoading.setVisibility(View.VISIBLE);
                mTvProgress.setText(getString(R.string.download_pause)+"(" + mDownPercent + "%)");
            }
        }
        setFileIcon(mMessage.getExtFileName());
    }

    /**
     * 检查 和彩云文件的下载状态
     */
    private void checkCloudDownload(DownLoadTask task) {

        if(mMessage.getExtFileSize()>=0
                &&mMessage.getExtDownSize() == mMessage.getExtFileSize()
                && !TextUtils.isEmpty(mMessage.getExtFilePath())) {

            btnDownloadFile.setVisibility(View.GONE);
            btnOpenFile.setVisibility(View.VISIBLE);
            mLayoutLoading.setVisibility(View.GONE);
            enableMenu(true);

            String filename = mMessage.getExtFileName();
            if(TextUtils.isEmpty(filename)){
                filename = new File(mMessage.getExtFilePath()).getName();

            }

            setFileIcon(filename);
            tvFileName.setText(filename);
            mFileSize.setText(StringUtil.formetFileSize(mMessage.getExtFileSize()));

        }else{
            String filename = new File(task.mLocationPath).getName();
            setFileIcon(filename);
            tvFileName.setText(filename);
            mFileSize.setText(StringUtil.formetFileSize(task.mTotalSize));
            refreshYunFileDownLoadStatus(task);
        }
    }

    private void refreshYunFileDownLoadStatus(DownLoadTask task){

        long totalSize = task.mTotalSize;
        if(totalSize <=0){
            return;
        }
        long downloadSize = task.getDownSize();
        int percent =(int) (downloadSize *100/ totalSize);


        int status = task.getStatus();
        switch (status) {
            case DownLoadTask.STATE_DOWNLOAD_NEW:  //尚未开始下载
                btnDownloadFile.setVisibility(View.VISIBLE);
                btnOpenFile.setVisibility(View.GONE);
                mLayoutLoading.setVisibility(View.GONE);
                break;
            case DownLoadTask.STATE_DOWNLOAD_DEAD:
            case DownLoadTask.STATE_DOWNLOAD_COMPLETE:// 下载完成。
                btnDownloadFile.setVisibility(View.GONE);
                btnOpenFile.setVisibility(View.VISIBLE);
                mLayoutLoading.setVisibility(View.GONE);
                enableMenu(true);
                break;
            case DownLoadTask.STATE_DOWNLOAD_RUNNING://正在下载状态
                btnDownloadFile.setVisibility(View.GONE);
                btnOpenFile.setVisibility(View.GONE);
                mLayoutLoading.setVisibility(View.VISIBLE);
                pbLoading.setProgress(percent);
                mTvProgress.setText(getString(R.string.download_pause)+"(" + percent + "%)");
                break;
            case DownLoadTask.STATE_DOWNLOAD_PAUSED:
            case DownLoadTask.STATE_DOWNLOAD_PAUSING: // 已经下载了一部分，处于暂停状态
                btnDownloadFile.setVisibility(View.GONE);
                btnOpenFile.setVisibility(View.GONE);
                mLayoutLoading.setVisibility(View.VISIBLE);
                pbLoading.setProgress(percent);
                mTvProgress.setText(getString(R.string.download_continue)+"(" + percent + "%)");
                break;
            default:
        }
    }

    /**
     * 从数据读取数据
     * @param id  数据库主键id
     * @param address
     * @return
     */
    private Message getMediaList(int id, String address) {
        Message item = null;
        Cursor cursor = getContentResolver().query(
                fromBlackList ? Conversations.BlackListMessage.CONTENT_URI : Conversations.Message.CONTENT_URI,
                new String[]{Message.COLUMN_NAME_ID, Message.COLUMN_NAME_EXT_FILE_NAME, Message.COLUMN_NAME_EXT_FILE_PATH, Message.COLUMN_NAME_EXT_THUMB_PATH, Message.COLUMN_NAME_THREAD_ID,
                        Message.COLUMN_NAME_TYPE, Message.COLUMN_NAME_EXT_DOWN_SIZE, Message.COLUMN_NAME_EXT_FILE_SIZE, Message.COLUMN_NAME_EXT_SHORT_URL}, Message.COLUMN_NAME_ID + "=?",
                new String[]{id + ""}, Message.COLUMN_NAME_DATE);

        if (cursor.moveToNext()) {
            item = new Message();
            item.setExtThumbPath(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_THUMB_PATH)));
            item.setThreadId(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_THREAD_ID)));
            item.setExtFilePath(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_PATH)));
            item.setExtDownSize(cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_DOWN_SIZE)));
            item.setExtFileSize(cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_SIZE)));
            item.setExtFileName(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_NAME)));
            item.setType(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_TYPE)));
            item.setExtShortUrl(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_SHORT_URL)));
            item.setId(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_ID)));
            item.setStatus(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_STATUS)));
            item.setAddress(address);
        }
        cursor.close();
        return item;
    }

    /**
     * 从数据读取数据, 群文件
     * @param id  数据库主键id
     * @param address
     * @return
     */
    private Message getGroupMediaList(int id, String address) {
        Message item = null;
        Cursor cursor = getContentResolver().query(
                Conversations.Group.CONTENT_URI,
                new String[]{Message.COLUMN_NAME_ID, Message.COLUMN_NAME_EXT_FILE_NAME, Message.COLUMN_NAME_EXT_FILE_PATH, Message.COLUMN_NAME_EXT_THUMB_PATH, Message.COLUMN_NAME_THREAD_ID,
                        Message.COLUMN_NAME_TYPE, Message.COLUMN_NAME_EXT_DOWN_SIZE, Message.COLUMN_NAME_EXT_FILE_SIZE, Message.COLUMN_NAME_EXT_SHORT_URL}, Message.COLUMN_NAME_ID + "=?",
                new String[]{id + ""}, Message.COLUMN_NAME_DATE);

        if (cursor.moveToNext()) {
            item = new Message();
            item.setExtThumbPath(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_THUMB_PATH)));
            item.setExtFilePath(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_PATH)));
            item.setThreadId(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_THREAD_ID)));
            item.setExtDownSize(cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_DOWN_SIZE)));
            item.setExtFileSize(cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_SIZE)));
            item.setExtFileName(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_NAME)));
            item.setType(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_TYPE)));
            item.setExtShortUrl(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_SHORT_URL)));
            item.setId(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_ID)));
            item.setStatus(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_STATUS)));
            item.setAddress(address);
        }
        cursor.close();
        return item;
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            if(mMessage == null){
                return;
            }
            Bundle bundle = intent.getExtras();
            String shortUrl = bundle.getString(LogicActions.FILE_TRANSFER_ID);
            if (action == LogicActions.FILE_RECV_DONE) {
                if (!TextUtils.isEmpty(shortUrl) && shortUrl.equals(mMessage.getExtShortUrl())) {
                    FileDetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLayoutLoading.setVisibility(View.GONE);
                            btnOpenFile.setVisibility(View.VISIBLE);
                            btnDownloadFile.setVisibility(View.GONE);
                            enableMenu(true);
                            BaseToast.show(mContext, getString(R.string.download_to)+"\"" + FileUtil.getDownloadDir() + "\""+getString(R.string.folder));
                        }
                    });
                }

            } else if (action == LogicActions.FILE_RECVING) {
                if (bundle != null) {
                    int recvSize = bundle.getInt(LogicActions.FILE_RECV_SIZE);
                    int fileSize = bundle.getInt(LogicActions.FILE_TOTAL_SIZE);
                    if (!TextUtils.isEmpty(shortUrl) && shortUrl.equals(mMessage.getExtShortUrl())) {
                        if (fileSize != 0) {
                            mMessage.setExtDownSize(recvSize);// 下载之后，更新mData的下载量。
                            int persent = (int) (recvSize * 1f/ fileSize *100);
                            pbLoading.setProgress(persent);
                            mTvProgress.setText(getString(R.string.download_pause)+"(" + persent + "%)");
                        }
                    }
                }
            }
        }
    };

    public class NoDoubleClickListener implements View.OnClickListener {

        public static final int MIN_CLICK_DELAY_TIME = 1000;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;
                onNoDoubleClick(v);
            }
        }

        public void onNoDoubleClick(View v) {
            int id = v.getId();
            if (mMessage.getType() == Type.TYPE_MSG_FILE_YUN_SEND
                    || mMessage.getType() == Type.TYPE_MSG_FILE_YUN_RECV) {//和彩云文件
                if (id == R.id.btn_open) {
                    // 打开文件
                    String yunFilePath = mMessage.getExtFilePath();//本地路径
                    if(TextUtils.isEmpty(yunFilePath)){
                        yunFilePath = mTask.mLocationPath;
                    }
                    FileUtil.openFile(FileDetailActivity.this, yunFilePath, mOpenFileArgs);
                } else if (id == R.id.btn_download) {
                    if(!AndroidUtil.isNetworkAvailable(v.getContext())){
                        BaseToast.show(R.string.charge_network_error);
                        return;
                    }
                    mTask.start();
                }else if(id == R.id.tv_progress){
                    if (mTask.getStatus() == DownLoadTask.STATE_DOWNLOAD_PAUSED || mTask.getStatus() == DownLoadTask.STATE_DOWNLOAD_PAUSING) {//暂停，正在暂停。
                        if(!AndroidUtil.isNetworkAvailable(v.getContext())){
                            BaseToast.show(R.string.charge_network_error);
                            return;
                        }
                        mTask.start();
                    } else if (mTask.getStatus() == DownLoadTask.STATE_DOWNLOAD_RUNNING) {// 正在下载
                        mTask.pause();
                    }else {
                        if(!AndroidUtil.isNetworkAvailable(v.getContext())){
                            BaseToast.show(R.string.charge_network_error);
                            return;
                        }
                        mTask.start();
                    }
                }
            } else {//菊风文件
                if (id == R.id.tv_progress) {
                    if (status == Status.STATUS_PAUSE) {

                        if(!AndroidUtil.isNetworkAvailable(v.getContext())){
                            BaseToast.show(R.string.charge_network_error);
                            return;
                        }

                        status = Status.STATUS_LOADING;

                        int itemId =(int) mMessage.getId();
                        String filePath = mMessage.getExtFilePath();
                        long fileSize = mMessage.getExtFileSize();
                        long downSize = mMessage.getExtDownSize();
                        File file = new File(filePath);

                        if (type == TYPE_GROUP) {
                            if (file.exists()) {
                                int iStartOffset = (int) (downSize>=0? downSize:0);
                                int iStopOffset = (int) fileSize;
                                ComposeMessageActivityControl.rcsImFileResumeByRecverX(itemId, mMessage.getAddress(), mMessage.getExtShortUrl(), file.getAbsolutePath(), iStartOffset, iStopOffset);
                            } else {
                                ComposeMessageActivityControl.rcsImFileFetchViaMsrpX(id, mMessage.getAddress(), mMessage.getExtShortUrl(), file.getAbsolutePath());
                            }
                        } else {
                            if (file.exists()) {
                                int iStartOffset = (int) (downSize>=0? downSize:0);
                                int iStopOffset = (int) fileSize;
                                ComposeMessageActivityControl.rcsImFileResumeByRecver(itemId, mMessage.getAddress(), mMessage.getExtShortUrl(), file.getAbsolutePath(), iStartOffset, iStopOffset);
                            } else {
                                ComposeMessageActivityControl.rcsImFileFetchViaMsrp(id, mMessage.getAddress(), mMessage.getExtShortUrl(), file.getAbsolutePath());
                            }
                        }
                    } else if (status == Status.STATUS_LOADING) {
                        status = Status.STATUS_PAUSE;
                        CharSequence tmp = mTvProgress.getText();
                        String progress = tmp.toString().substring(4);
                        mTvProgress.setText(getString(R.string.download_continue) + progress);

                        // 调用菊风sdk取消发送接口
                        ComposeMessageActivityControl.rcsImFileRelease(mMessage.getExtShortUrl());
                    }
                } else if (id == R.id.btn_open) {
                    // 打开文件
                    FileUtil.openFile(FileDetailActivity.this, mMessage.getExtFilePath(), mOpenFileArgs);
                } else if (id == R.id.btn_download) {

                    if(!AndroidUtil.isNetworkAvailable(v.getContext())){
                        BaseToast.show(R.string.charge_network_error);
                        return;
                    }

                    status = Status.STATUS_LOADING;
                    int msgId =(int) mMessage.getId();
                    String filePath = mMessage.getExtFilePath();
                    long fileSize = mMessage.getExtFileSize();
                    long downSize = mMessage.getExtDownSize();
                    File file = new File(filePath);
                    if (type == TYPE_GROUP) {
                        if (file.exists()) {
                            int iStartOffset = (int) (downSize>=0? downSize:0);
                            int iStopOffset = (int) fileSize;
                            ComposeMessageActivityControl.rcsImFileResumeByRecverX(msgId, mMessage.getAddress(), mMessage.getExtShortUrl(), file.getAbsolutePath(), iStartOffset, iStopOffset);
                        } else {
                            ComposeMessageActivityControl.rcsImFileFetchViaMsrpX(msgId, mMessage.getAddress(), mMessage.getExtShortUrl(), file.getAbsolutePath());
                        }
                    } else {
                        if (file.exists()) {
                            int iStartOffset = (int) (downSize>=0? downSize:0);
                            int iStopOffset = (int) fileSize;
                            ComposeMessageActivityControl.rcsImFileResumeByRecver(msgId, mMessage.getAddress(), mMessage.getExtShortUrl(), file.getAbsolutePath(), iStartOffset, iStopOffset);
                        } else {
                            ComposeMessageActivityControl.rcsImFileFetchViaMsrp(msgId, mMessage.getAddress(), mMessage.getExtShortUrl(), file.getAbsolutePath());
                        }
                    }
                    mLayoutLoading.setVisibility(View.VISIBLE);
                    mTvProgress.setText(getString(R.string.download_pause)+"(" + mDownPercent + "%)");
                    btnOpenFile.setVisibility(View.GONE);
                    btnDownloadFile.setVisibility(View.GONE);
                }
            }
        }
    }


    private static final int HANDLER_NOTIFY_ID = 90;

    private void setYunFileDownloadListener(DownLoadTask task) {
        DownLoadListener listener = task.getListener();
        if (listener == null) {
            task.setListener(new YunFileDownLoadListener(mHandler, 0, HANDLER_NOTIFY_ID));
        } else if (listener instanceof YunFileDownLoadListener) {
            Handler h = ((YunFileDownLoadListener) listener).getHandler();
            if (!mHandler.equals(h)) {
                task.setListener(new YunFileDownLoadListener(mHandler, 0, HANDLER_NOTIFY_ID));
            }
        }
    }


    private Handler mHandler= new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            if(msg.what == HANDLER_NOTIFY_ID && msg.arg1 == HANDLER_NOTIFY_ID){
                refreshYunFileDownLoadStatus(mTask);
                //Log.i("Vampire1", mTask.getStatus() + "!");
            }
        }
    };
}
