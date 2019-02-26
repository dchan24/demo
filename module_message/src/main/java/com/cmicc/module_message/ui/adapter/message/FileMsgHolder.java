package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.YunFile;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.data.yunfile.DownLoadListener;
import com.cmcc.cmrcs.android.data.yunfile.DownLoadTask;
import com.cmcc.cmrcs.android.data.yunfile.DownloadManager;
import com.cmcc.cmrcs.android.data.yunfile.YunFileDownLoadListener;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.dialogs.FileMenuDialog;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.YunFileXmlParser;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmcc.cmrcs.android.widget.RoundProgressBar;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.io.File;
import java.math.BigDecimal;
import java.util.Calendar;

import static com.cmicc.module_message.ui.adapter.MessageChatListAdapter.HANDLER_NOTIFY_ID;


public class FileMsgHolder extends BaseViewHolder {

    public View sllMsg;

    public ImageView sendFailedView;
    public RoundProgressBar sSendProgress;
    public ImageView sIvDownFile;//完成符号
    public ImageView sIvFileIcon;
    public TextView sTvFileName;
    public TextView sTvFileSize;
    public TextView sTvHasRead;
    public ImageView sendStatus;
    public CheckBox multiCheckBox;

    public FileMsgHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
        super(itemView ,activity ,adapter ,presenter);
        sllMsg = itemView.findViewById(R.id.ll_msg);
        sIvDownFile = itemView.findViewById(R.id.img_message_down_file);//下载按钮
        sendFailedView = itemView.findViewById(R.id.imageview_msg_send_failed);
        sSendProgress = itemView.findViewById(R.id.progress_send_small);//发送进度条, 发送进度条和下载按钮是重合的。
        sTvFileName = itemView.findViewById(R.id.textview_file_name);
        sIvFileIcon = itemView.findViewById(R.id.iv_file_icon);
        sTvFileSize = itemView.findViewById(R.id.textview_file_size);
        sTvHasRead = itemView.findViewById(R.id.tv_has_read);
        sendStatus = itemView.findViewById(R.id.iv_send_status);
        multiCheckBox = itemView.findViewById(R.id.multi_check);

        sendFailedView.setOnClickListener(mOnMsgFailClickListener);
        sllMsg.setOnLongClickListener(new OnMsgContentLongClickListener());
        sllMsg.setOnClickListener(mClickListener);
        sSendProgress.setOnClickListener(mTransfSwitcherClickListener);//对于发送文件类的，点击 修改下载状态
        sIvDownFile.setOnClickListener(mTransfSwitcherClickListener);//对于发送文件类的，点击 修改下载状态
        sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());

    }

    @Override
    public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
        ((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
        if(isMultiSelectMode){
            //头像不显示，以消息气泡上下居中
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
            int type = mMessage.getType();
            switch (type){
                case Type.TYPE_MSG_FILE_YUN_SEND:
                case Type.TYPE_MSG_FILE_SEND:
                case Type.TYPE_MSG_FILE_SEND_CCIND:
                    params.topToTop = R.id.ll_msg;
                    params.bottomToBottom = R.id.ll_msg;

                    break;
                case Type.TYPE_MSG_FILE_YUN_RECV:
                case Type.TYPE_MSG_FILE_RECV:
                    if(sIvHead.getVisibility() == View.INVISIBLE){
                        params.topToTop = R.id.ll_msg;
                        params.bottomToBottom = R.id.ll_msg;
                    }else{
                        params.topToTop = R.id.svd_head;
                        params.bottomToBottom = R.id.svd_head;
                    }
                    break;
                default:
                    break;

            }

            multiCheckBox.setLayoutParams(params);
            multiCheckBox.setVisibility(View.VISIBLE);
            multiCheckBox.setChecked(isSelected);
        }else{
            multiCheckBox.setVisibility(View.GONE);
        }
    }

    /**
     * 同步msg中的文件信息到UI
     */
    public void bindFile() {
        Message msg = mMessage;
        int type = msg.getType();
        String fileNameRecv = msg.getExtFileName();
        int status = msg.getStatus();
        long fileSize = msg.getExtFileSize();
        long downSize = msg.getExtDownSize();
        int progress = 0;

        if (downSize <= 0 || fileSize <= 0) {
            progress = 0;
        } else if (downSize >= fileSize) {
            progress = 100;
        } else {
            double f1 = new BigDecimal((float) downSize / fileSize).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            progress = (int) (f1 * 100);
        }

        sTvFileName.setText(fileNameRecv);
        sTvFileSize.setText(StringUtil.formetFileSize(fileSize));

        switch (status) {
            case Status.STATUS_WAITING:
            case Status.STATUS_LOADING:
                // 传输中
                sSendProgress.setVisibility(View.VISIBLE);
                sIvDownFile.setVisibility(View.GONE);
                sSendProgress.isPause = false;
                sSendProgress.setProgress(progress);
                sendFailedView.setVisibility(View.GONE);
                break;
            case Status.STATUS_OK:
            case Status.STATUS_DESTROY:
                // 成功
                sSendProgress.setVisibility(View.GONE);
                sendFailedView.setVisibility(View.GONE);
                sIvDownFile.setVisibility(View.VISIBLE);
                if (type == Type.TYPE_MSG_FILE_RECV || type == Type.TYPE_MSG_FILE_YUN_RECV || type == Type.TYPE_MSG_FILE_YUN_SEND) {//需要接收的类型
                    if (isDone(msg)) {
                        sIvDownFile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_file_ic_finished));
                    } else {
                        sIvDownFile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_file_ic_download));
                    }
                }
                break;
            case Status.STATUS_PAUSE:
                // 暂停
                if(msg.getType() == Type.TYPE_MSG_FILE_SEND || msg.getType() == Type.TYPE_MSG_FILE_SEND_CCIND){
                    sendFailedView.setVisibility(View.VISIBLE);//暂停状态，出现 红色按钮
                    sSendProgress.setVisibility(View.GONE);//隐藏下载进度条
                }else{
                    sendFailedView.setVisibility(View.GONE);//
                    sSendProgress.setVisibility(View.VISIBLE);//显示下载进度条
                }
                sIvDownFile.setVisibility(View.GONE);  //下载完成符号隐藏
                sSendProgress.pauseProgress();//下载进度条 进度暂停
                sSendProgress.setProgress(progress);//显示进度数值
                break;
            case Status.STATUS_FAIL:
                if (type == Type.TYPE_MSG_FILE_RECV|| type == Type.TYPE_MSG_FILE_YUN_RECV || type == Type.TYPE_MSG_FILE_YUN_SEND) {//需要接收的类型
                    sSendProgress.setVisibility(View.GONE);
                    sendFailedView.setVisibility(View.VISIBLE);
                    sIvDownFile.setVisibility(View.VISIBLE);
                    if (isDone(msg)) {
                        sIvDownFile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_file_ic_finished));
                    } else {
                        sIvDownFile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_file_ic_download));
                    }
                } else {
                    sendFailedView.setVisibility(View.VISIBLE);
                    sSendProgress.setVisibility(View.GONE);
                    sIvDownFile.setVisibility(View.GONE);
                }

                break;
            default:
                // 失败
                if (type == Type.TYPE_MSG_FILE_RECV|| type == Type.TYPE_MSG_FILE_YUN_RECV || type == Type.TYPE_MSG_FILE_YUN_SEND) {//需要接收的类型
                    sSendProgress.setVisibility(View.GONE);
                    sIvDownFile.setVisibility(View.VISIBLE);
                    if (isDone(msg)) {
                        sIvDownFile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_file_ic_finished));
                    } else {
                        sIvDownFile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_file_ic_download));
                    }
                } else {
                    sendFailedView.setVisibility(View.VISIBLE);
                    sSendProgress.setVisibility(View.GONE);
                    sIvDownFile.setVisibility(View.GONE);
                }

                break;
        }

        // 文件图标
        if (!TextUtils.isEmpty(fileNameRecv)) {
            if (fileNameRecv.endsWith(".txt")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_txt);
            } else if (fileNameRecv.endsWith(".png") || fileNameRecv.endsWith(".jpg") || fileNameRecv.endsWith(".PNG")  || fileNameRecv.endsWith(".JPG") || fileNameRecv.endsWith(".jpeg") || fileNameRecv.endsWith(".JPEG") || fileNameRecv.endsWith(".GIF") || fileNameRecv.endsWith(".gif") || fileNameRecv.endsWith(".bmp") || fileNameRecv.endsWith(".BMP")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_pic);
            } else if (fileNameRecv.endsWith(".rar")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_rar);
            }else if (fileNameRecv.endsWith(".zip")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_zip);
            }  else if (fileNameRecv.endsWith(".mp3") || fileNameRecv.endsWith(".wav") || fileNameRecv.endsWith(".3ga") || fileNameRecv.endsWith(".amr")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_music);
            } else if (fileNameRecv.endsWith(".rmvb") || fileNameRecv.endsWith(".RMVB") || fileNameRecv.endsWith(".mov") || fileNameRecv.endsWith(".mp4") || fileNameRecv.endsWith(".MP4") || fileNameRecv.endsWith(".MOV") || fileNameRecv.endsWith(".3gp") || fileNameRecv.endsWith(".3GP") || fileNameRecv.endsWith(".WMV") || fileNameRecv.endsWith(".wmv") || fileNameRecv.endsWith(".AVI") || fileNameRecv.endsWith(".avi") || fileNameRecv.endsWith(".FLV") || fileNameRecv.endsWith(".flv")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_mp4);
            } else if (fileNameRecv.endsWith(".doc") || fileNameRecv.endsWith(".docx")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_doc);
            } else if (fileNameRecv.endsWith(".ppt") || fileNameRecv.endsWith(".pptx") || fileNameRecv.endsWith(".PPTX") || fileNameRecv.endsWith(".PPT") || fileNameRecv.endsWith(".ppts") ) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_ppt);
            } else if (fileNameRecv.endsWith(".pdf")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_pdf);
            } else if (fileNameRecv.endsWith(".xls") || fileNameRecv.endsWith(".xlsx")) {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_xlsx);
            } else {
                sIvFileIcon.setBackgroundResource(R.drawable.chat_file_unknown);
            }
        } else {
            sIvFileIcon.setBackgroundResource(R.drawable.chat_file_unknown);
        }
    }


    public void onBindViewYunFile(final Message msgBefore , int position) {

        Handler handler = adapter.mHandler;
        //yunfFile信息
        final YunFile yunFile = YunFileXmlParser.parserYunFileXml(mMessage.getBody());
        final String url = yunFile.getFileUrl();
        final String fileName = yunFile.getFileName();
        final long totalSize = yunFile.getFileSize();


        if(mMessage.getExtFileSize()>0
                &&mMessage.getExtDownSize() == mMessage.getExtFileSize()
                && !TextUtils.isEmpty(mMessage.getExtFilePath())){

            mMessage.setStatus(Status.STATUS_OK);

        }else{
            String bogusPath = yunFile.getLocalPath();//先传一个预定的文件名，但是最后下载完可能名字不一样
//            if(TextUtils.isEmpty(bogusPath)){
//                bogusPath = null;
//            }

            final File file = new File(bogusPath);
            DownLoadTask task = DownloadManager.getInstance().getTask(url, file.getAbsolutePath(), totalSize, mMessage);
            DownLoadListener listener = task.getListener();

            if (listener == null) {
                task.setListener(new YunFileDownLoadListener(handler, mMessage.getId(), HANDLER_NOTIFY_ID));
            } else if (listener instanceof YunFileDownLoadListener) {
                Handler h = ((YunFileDownLoadListener) listener).getHandler();
                if (handler.equals(h)) {
                    task.setListener(new YunFileDownLoadListener(handler, mMessage.getId(), HANDLER_NOTIFY_ID));
                }
            }

            final long downSize = task.getDownSize();

            int statusT = task.getStatus();
            switch (task.getStatus()) {
                case DownLoadTask.STATE_DOWNLOAD_NEW:
                    statusT = Status.STATUS_OK;
                    break;
                case DownLoadTask.STATE_DOWNLOAD_COMPLETE:
                    statusT = Status.STATUS_OK;
                    break;
                case DownLoadTask.STATE_DOWNLOAD_RUNNING:
                    statusT = Status.STATUS_LOADING;
                    break;
                case DownLoadTask.STATE_DOWNLOAD_PAUSED:
                case DownLoadTask.STATE_DOWNLOAD_PAUSING:
                    statusT = Status.STATUS_PAUSE;
                    break;
                default:
                    statusT = Status.STATUS_OK;
            }

            final int status = statusT;

            Log.i("Chenjianwen" ,"onFileRefresh: " + mMessage.getExtFilePath() + "---" + downSize + "/" + fileName);
            mMessage.setExtFileName(fileName);
//            mMessage.setExtFilePath(file.getAbsolutePath());
            mMessage.setExtDownSize(downSize);
            mMessage.setExtFileSize(totalSize);
            mMessage.setStatus(status);
        }




        bindHead(msgBefore);
        bindName(msgBefore);
        bindSendStatus();
        bindFile();
        bindBubble();
        bindTime(msgBefore, position);
    }

    public static boolean isDone(Message msg) {
        boolean isDone = false;
        long fileSize = msg.getExtFileSize();
        String filePath = msg.getExtFilePath();
        if(TextUtils.isEmpty(filePath)){//彩云文件存在这种情况
            return false;
        }

        File file = new File(filePath);
        if(!file.exists()){
            return false;
        }

        if (fileSize != 0
                && fileSize <= file.length()
                &&msg.getExtDownSize()>= fileSize) {

            isDone = true;
        }
        return isDone;
    }

    @Override
    public void bindSendStatus() {
        int type = mMessage.getType();
        int receipt = mMessage.getMessage_receipt();
        int status = mMessage.getStatus();

        if ((isEPGroup || isPartyGroup) && (type == Type.TYPE_MSG_FILE_SEND || type == Type.TYPE_MSG_FILE_SEND_CCIND || type == Type.TYPE_MSG_FILE_YUN_SEND)) {
            //企业群，党群，特殊消息另起头像，已读状态等下间距去掉
            if(mMessage.getSmallPadding()){
                sTvHasRead.setPadding(0 ,(int) AndroidUtil.dip2px(mContext, 7) ,0 ,0 );
            }else{
                sTvHasRead.setPadding(0 ,(int) AndroidUtil.dip2px(mContext, 7) ,0 ,(int) AndroidUtil.dip2px(mContext, 7) );
            }
            if (status == Status.STATUS_OK) {
                sTvHasRead.setVisibility(View.VISIBLE);
            } else {
                sTvHasRead.setVisibility(View.INVISIBLE);
            }
        } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT
                && (type == Type.TYPE_MSG_FILE_SEND || type == Type.TYPE_MSG_FILE_SEND_CCIND || type == Type.TYPE_MSG_FILE_YUN_SEND)
                && receipt != -1) {
            if (status == Status.STATUS_OK) {
                sTvHasRead.setVisibility(View.VISIBLE);
                sendStatus.setVisibility(View.VISIBLE);
                if (type == Type.TYPE_MSG_FILE_SEND_CCIND || type == Type.TYPE_MSG_FILE_YUN_SEND) {
                    sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                    sTvHasRead.setText(mContext.getString(R.string.from_pc));
                    sendStatus.setImageResource(R.drawable.my_chat_pc);
                    ViewGroup.LayoutParams params = sendStatus.getLayoutParams();
                    params.width = (int) AndroidUtil.dip2px(mContext, 10f);
                    params.height = (int) AndroidUtil.dip2px(mContext, 8.7f);
                    sendStatus.setLayoutParams(params);
                } else {
                    if (receipt == 0) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText("");
                        sendStatus.setImageResource(R.drawable.my_chat_waiting);
                    } else if (receipt == 1) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_delivered));
                        sendStatus.setImageResource(R.drawable.my_chat_delivered);
                    } else if (receipt == 2) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_delivered_by_sms));
                        sendStatus.setImageResource(R.drawable.my_chat_delivered);

                    } else if (receipt == 3) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_notified_by_sms));
                        sendStatus.setImageResource(R.drawable.my_chat_shortmessage);

                    } else {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.others_offline_already_notified));
                        sendStatus.setImageResource(R.drawable.my_chat_shortmessage);
                    }
                }
            } else {
                sTvHasRead.setVisibility(View.INVISIBLE);
            }
        } else {
            sTvHasRead.setVisibility(View.GONE);
        }
    }

    /**
     * 和彩云文件的点击事件<br/>
     *  和彩云文件的下载方式颇为不同，需要特别注意。
     */
    public void yunFileClickListener() {

        //查看是否已经下载结束。
        final YunFile yunFile = YunFileXmlParser.parserYunFileXml(mMessage.getBody());
        String url = yunFile.getFileUrl();
        final long totalSize = yunFile.getFileSize();
        File file = new File(yunFile.getLocalPath());
        String path = mMessage.getExtFilePath();
        if(!TextUtils.isEmpty(path)){//字段不为空，推测已经下载结束
            file = new File(path);
            if (file.exists() && (file.length() >= totalSize)) { //下载结束
                Bundle b = new Bundle();
                b.putSerializable(FileMenuDialog.OPEN_FILE_ARG_MESSAGE, mMessage);
                b.putInt(FileMenuDialog.OPEN_FILE_ARG_CHAT_TYPE, mChatType);
                b.putInt(FileMenuDialog.OPEN_FILE_ARG_FROM,  FileMenuDialog.OPEN_FILE_FROM_MESSAGE_DETAIL_ACTIVITY);
                FileUtil.openFile(mContext, file.getAbsolutePath(), b);
                return;
            }
        }



        final long msgId = mMessage.getId();
        DownLoadTask task = DownloadManager.getInstance().getTask(url, file.getAbsolutePath(), totalSize, mMessage);
        task.setListener(new YunFileDownLoadListener(adapter.mHandler, msgId, HANDLER_NOTIFY_ID));

        if (task.getStatus() == DownLoadTask.STATE_DOWNLOAD_PAUSED || task.getStatus() == DownLoadTask.STATE_DOWNLOAD_PAUSING) {//暂停，正在暂停。
            task.start();
        } else if (task.getStatus() == DownLoadTask.STATE_DOWNLOAD_RUNNING) {// 正在下载
            task.pause();
        } else {
            task.start();
        }
    }

    /**
     *  暂停，发送， 进度条， 这个按钮的点击事件
     */
    private View.OnClickListener mTransfSwitcherClickListener = new View.OnClickListener(){//这两个button的点击事件，产品2018年12月4日定义为， 发送方，点击 会改变 下载状态。接收方，和标准的点击事件一致。
        public static final int MIN_CLICK_DELAY_TIME = 1000;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME ) {
                lastClickTime = currentTime;
                onNoDoubleClick(v);
            }
        }

        public void onNoDoubleClick(View v) {
            if(mMessage.getType() == Type.TYPE_MSG_FILE_SEND){

                if (mMessage.getStatus() == Status.STATUS_DESTROY) {
                    BaseToast.show(mContext, mContext.getString(R.string.message_content_overdue));
                    return;
                }

                if(mMessage.getStatus() == Status.STATUS_OK && isDone(mMessage)){//下载结束

                    Bundle b = new Bundle();//文件打开页面， 收藏功能，所需要的参数。
                    b.putSerializable(FileMenuDialog.OPEN_FILE_ARG_MESSAGE, mMessage);
                    b.putInt(FileMenuDialog.OPEN_FILE_ARG_CHAT_TYPE, mChatType);
                    b.putInt(FileMenuDialog.OPEN_FILE_ARG_FROM,  FileMenuDialog.OPEN_FILE_FROM_MESSAGE_DETAIL_ACTIVITY);
                    FileUtil.openFile(mContext, mMessage.getExtFilePath(), b);//本地发送的文件，点击 就是打开文件
                    return;
                }

                if(mMessage.getStatus() == Status.STATUS_FAIL){
                    presenter.reSend(mMessage);
                }else if(mMessage.getStatus() == Status.STATUS_LOADING || mMessage.getStatus() == Status.STATUS_WAITING){//正在发送中
                    ComposeMessageActivityControl.releaseFileTransmission(mMessage);
                }else {
                    ComposeMessageActivityControl.resumeFileTransmission(mMessage, isGroupChat);
                }
            }else{
                mClickListener.onClick(v);
            }
        }
    };

    /**
     *  失败按钮， label 等的 点击事件
     */
    private View.OnClickListener mClickListener = new View.OnClickListener(){
        public static final int MIN_CLICK_DELAY_TIME = 1000;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME ) {
                lastClickTime = currentTime;
                onNoDoubleClick(v);
            }
        }

        public void onNoDoubleClick(View v) {
            if (mMessage.getStatus() == Status.STATUS_DESTROY) {
                BaseToast.show(mContext, mContext.getString(R.string.message_content_overdue));
                return;
            }

            Bundle b = new Bundle();//文件打开页面， 收藏功能，所需要的参数。
            b.putSerializable(FileMenuDialog.OPEN_FILE_ARG_MESSAGE, mMessage);
            b.putInt(FileMenuDialog.OPEN_FILE_ARG_CHAT_TYPE, mChatType);
            b.putInt(FileMenuDialog.OPEN_FILE_ARG_FROM,  FileMenuDialog.OPEN_FILE_FROM_MESSAGE_DETAIL_ACTIVITY);

            int type = mMessage.getType();
            switch (type){//五种文件的发送处理。
                case Type.TYPE_MSG_FILE_YUN_SEND://接收和彩云平台的文件
                case Type.TYPE_MSG_FILE_YUN_RECV://
                    yunFileClickListener();
                    break;
                case Type.TYPE_MSG_FILE_RECV://接收文件, 中兴平台
                case Type.TYPE_MSG_FILE_SEND_CCIND://废弃, 理论上，不应该有这种类型。

                    if (type == Type.TYPE_MSG_FILE_RECV || type == Type.TYPE_MSG_FILE_SEND_CCIND) { //迁移消息
                        boolean isDone = FileMsgHolder.isDone(mMessage);
                        if (!isDone && mMessage.getAddressId() == 99) {
                            BaseToast.show(mContext, mContext.getString(R.string.transfer_news));
                            LogF.e(TAG, "Click: 迁移消息!!!" + mMessage);
                            return;
                        }
                    }//迁移消息， 这种消息目前没见过。

                    if(mMessage.getStatus() == Status.STATUS_PAUSE|| mMessage.getStatus() == Status.STATUS_FAIL || mMessage.getStatus() == Status.STATUS_WAITING) { //已经暂停,或者失败, 或者等待下载。
                        ComposeMessageActivityControl.resumeFileTransmission(mMessage, isGroupChat);
                    } else if(mMessage.getStatus() == Status.STATUS_LOADING) {//正在下载
                        ComposeMessageActivityControl.releaseFileTransmission(mMessage);
                    }else if(mMessage.getStatus() == Status.STATUS_OK){
                        if (isDone(mMessage)) {//下载结束。
                            FileUtil.openFile(mContext, mMessage.getExtFilePath(), b);
                        }else{//尚未开始下载
                            ComposeMessageActivityControl.resumeFileTransmission(mMessage, isGroupChat);
                        }
                    }else{
                        ComposeMessageActivityControl.resumeFileTransmission(mMessage, isGroupChat);
                    }
                    break;
                case Type.TYPE_MSG_FILE_SEND://本地文件 发送
                    FileUtil.openFile(mContext, mMessage.getExtFilePath(), b);//本地发送的文件，点击 就是打开文件
                    break;
                default:
            }
        }
    };


    private View.OnClickListener mOnMsgFailClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            adapter.showDialog(mContext.getString(R.string.resent_message_hint), mContext.getString(R.string.btn_cancel), mContext.getString(R.string.btn_sure), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.getDialog().dismiss();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.getDialog().dismiss();
                    final Message msg = mMessage;
                    if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                        UmengUtil.buryPoint(mContext, "message_groupmessage_resend", "消息-群聊-重发", 0);
                    } else if (mChatType== MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                        UmengUtil.buryPoint(mContext, "message_p2pmessage_resend", "消息-点对点-重发", 0);
                    }

                    if(msg.getType() == Type.TYPE_MSG_FILE_YUN_RECV || msg.getType() == Type.TYPE_MSG_FILE_YUN_SEND){
                        yunFileClickListener();
                    }else if(msg.getStatus() == Status.STATUS_PAUSE){
                        ComposeMessageActivityControl.resumeFileTransmission(msg, isGroupChat);
                    }else{//重新发送
                        presenter.reSend(msg);
                    }
                }
            });
        }
    };
}
