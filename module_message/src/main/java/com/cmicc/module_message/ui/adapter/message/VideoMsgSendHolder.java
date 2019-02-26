package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.TimeUtil;
import com.cmcc.cmrcs.android.glide.GlideApp;
import com.cmicc.module_message.ui.activity.MessagevideoActivity;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmcc.cmrcs.android.widget.MediaTransfProgressBar;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.io.File;
import java.util.Calendar;

public class VideoMsgSendHolder extends BaseViewHolder {
	private static final String TAG = VideoMsgSendHolder.class.getName();
    public CheckBox multiCheckBox; //多选框
    public TextView sTvHasRead; //已读动态
    public ImageView sendStatus; //发送状态

	private ImageView mImageviewMsgSendFailed; //失败重发标志
    private ImageView mThumbView;   //缩略图
    private ImageView mVideoPlayBtn;  //播放按钮
    private TextView mVideoSize; //视频大小
    private TextView mVideoDuration; //视频时长
    private MediaTransfProgressBar mProgressBar;// 一直转的进度条
    private TextView mProgressText;  //进度条的 数字进度

	public VideoMsgSendHolder(View itemView, Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
        multiCheckBox = itemView.findViewById(R.id.multi_check);
        sTvHasRead = itemView.findViewById(R.id.tv_has_read);
        sendStatus = itemView.findViewById(R.id.iv_send_status);

		mImageviewMsgSendFailed = itemView.findViewById(R.id.imageview_msg_send_failed);
		mThumbView = itemView.findViewById(R.id.video_thumb);
		mVideoPlayBtn = itemView.findViewById(R.id.video_play);
		mVideoSize = itemView.findViewById(R.id.textview_video_size);
		mVideoDuration = itemView.findViewById(R.id.textview_video_time);
		mProgressBar = itemView.findViewById(R.id.progress_bar);
		mProgressText = itemView.findViewById(R.id.progress_text);

		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());
		mVideoPlayBtn.setOnClickListener(mClickListener);
		mProgressBar.setOnClickListener(mClickListener);
        mThumbView.setOnClickListener(mClickListener);
		mProgressBar.setOnLongClickListener(new OnMsgContentLongClickListener());
		mVideoPlayBtn.setOnLongClickListener(new OnMsgContentLongClickListener());
		mThumbView.setOnLongClickListener(new OnMsgContentLongClickListener());
        mImageviewMsgSendFailed.setOnClickListener(mFailedClickListener);

	}

	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			params.topToTop = R.id.layout;
			params.bottomToBottom = R.id.layout;
			multiCheckBox.setLayoutParams(params);
			multiCheckBox.setVisibility(View.VISIBLE);
			multiCheckBox.setChecked(isSelected);
		}else{
			multiCheckBox.setVisibility(View.GONE);
		}
	}


	public void bindThumb(Message msg, int maxSize, int minSize){


		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;


		String extThumbPath = msg.getExtThumbPath();

		boolean isFileExist = false;
		if (extThumbPath != null && !extThumbPath.isEmpty()) { //判断thumb是否存在
			File file = new File(extThumbPath);
			if (file != null && file.exists()) {
				isFileExist = true;
			}
		}

		boolean isFileBroken = false;
		if (isFileExist) {
			BitmapFactory.decodeFile(extThumbPath, opts);
			if (opts.outHeight <= 0 || opts.outWidth <= 0) { //判断thumb是否损坏
				isFileBroken = true;
			}
		}

		ViewGroup.LayoutParams  params3 = mThumbView.getLayoutParams();
        RequestOptions options = new RequestOptions().bitmapTransform(new RoundedCorners((int) mContext.getResources().getDimension(R.dimen.dp4)));//此处，还有一个渐变边界，没有处理，等有时间来出来。
		if (!isFileExist || isFileBroken) { //thumb文件不存在， 或者已经损坏, 显示错误图
			params3.width = (int) AndroidUtil.dip2px(App.getAppContext(), 160);
			params3.height = (int) AndroidUtil.dip2px(App.getAppContext(), 100);
			mThumbView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			GlideApp.with(App.getAppContext()).load(R.drawable.loadfail_video).apply(options).into(mThumbView);

			mVideoPlayBtn.setVisibility(View.GONE);//播放按钮
			mImageviewMsgSendFailed.setVisibility(View.GONE); //发送失败
            mProgressText.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.pause();


			LogF.e(TAG, "Image Thumb is null, thumb:" + msg.getExtThumbPath() + ",id: " + msg.getId()+",file: " + msg.getExtFilePath());
		} else {// thumb文件正常， 显示thumb
			mThumbView.setScaleType(ImageView.ScaleType.FIT_CENTER);

			if (opts.outHeight > opts.outWidth) {
				params3.height = maxSize;
				params3.width = params3.height * opts.outWidth / opts.outHeight;
				if (params3.width < minSize) {
					params3.width = minSize;
				}
			} else {
				params3.width = maxSize;
				params3.height = params3.width * opts.outHeight / opts.outWidth;
				if (params3.height < minSize) {
					params3.height = minSize;
				}
			}
			GlideApp.with(App.getAppContext()).load(extThumbPath).apply(options).into(mThumbView);
		}
	}

	/**
	 * 绑定时间
	 * @param msg
	 */
	public void bindDuration(Message msg){
		int seconds = Integer.parseInt(msg.getExtSizeDescript());
		if(seconds == 0){
			seconds = 1;
		}

        //mVideoSize.setVisibility(View.VISIBLE);
        mVideoDuration.setVisibility(View.VISIBLE);
		mVideoDuration.setText(TimeUtil.getHHMMSSTimeString(seconds));// 设置视频时间
		//mVideoSize.setText(StringUtil.formetFileSize(msg.getExtFileSize()));
	}

	/**
	 *  绑定下载状态
	 */
	public void bindDownloadStatus(){
        long fileSize = mMessage.getExtFileSize();
        long downSize = mMessage.getExtDownSize();
        int progress;
        if (downSize <= 0 || fileSize <= 0) {
            progress = 0;
        } else if (downSize >= fileSize) {
            progress = 100;
        } else {
            progress = (int) (100 * downSize / fileSize);
        }

		int status = mMessage.getStatus();

        mProgressText.setText(progress + "%");//进度条

        switch (status) {
			case Status.STATUS_WAITING:
			case Status.STATUS_LOADING:
				// 发送中
				mImageviewMsgSendFailed.setVisibility(View.GONE);
				mVideoPlayBtn.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressText.setVisibility(View.VISIBLE);
                mProgressBar.start();
				break;
			case Status.STATUS_OK:
				// 成功
                mImageviewMsgSendFailed.setVisibility(View.GONE);
                mVideoPlayBtn.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mProgressText.setVisibility(View.GONE);
                mProgressBar.pause();
				break;
			case Status.STATUS_PAUSE:
				// 暂停
                mImageviewMsgSendFailed.setVisibility(View.VISIBLE);
                mVideoPlayBtn.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressText.setVisibility(View.VISIBLE);
                mProgressBar.pause();
				break;
			default:
				// 失败
				mImageviewMsgSendFailed.setVisibility(View.VISIBLE);
				if(downSize >0){//如果已经有 下载/上传进度，则 显示进度。
                    mVideoPlayBtn.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressText.setVisibility(View.VISIBLE);
                }else{
                    mVideoPlayBtn.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    mProgressText.setVisibility(View.GONE);
                }
                mProgressBar.pause();

				break;
		}
	}

	@Override
	public void bindSendStatus() {
		int status = mMessage.getStatus();
		int msgType = mMessage.getType();
		int receipt = mMessage.getMessage_receipt();
        if (isEPGroup || isPartyGroup) {//党群，企业群设置 已读标志
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
        } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT && receipt != -1) {
            if (status == Status.STATUS_OK) {
                sTvHasRead.setVisibility(View.VISIBLE);
                sendStatus.setVisibility(View.VISIBLE);
                if (msgType == Type.TYPE_MSG_VIDEO_SEND_CCIND) {
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
                sendStatus.setVisibility(View.INVISIBLE);
            }
        } else {
            sTvHasRead.setVisibility(View.GONE);
            sendStatus.setVisibility(View.GONE);
        }
	}

	// 失败按钮
    private View.OnClickListener mFailedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            adapter.showDialog(activity.getString(R.string.resent_message_hint), activity.getString(R.string.btn_cancel), activity.getString(R.string.btn_sure), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.getDialog().dismiss();
                }
            }, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    adapter.getDialog().dismiss();
                    if(mMessage.getStatus() == Status.STATUS_PAUSE){
                        ComposeMessageActivityControl.resumeFileTransmission(mMessage, isGroupChat);
                    }else{
                        presenter.reSend(mMessage);
                    }
                }
            });
        }
    };


    // thumb点击事件
    private View.OnClickListener mClickListener = new View.OnClickListener() {
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
            Message msg = mMessage;
            if (BitmapFactory.decodeFile(msg.getExtThumbPath()) == null) {// 缩略图加载失败
                BaseToast.show(mContext, mContext.getString(R.string.load_failed));
                return;
            }

            if (msg.getStatus() == Status.STATUS_DESTROY) {
                BaseToast.show(mContext, mContext.getString(R.string.message_content_overdue));
                return;
            }

            if(msg.getType() == Type.TYPE_MSG_VIDEO_SEND_CCIND){//抄送类的，需要下载
                long fileSize = msg.getExtFileSize();
                File file = new File(msg.getExtFilePath());
                if (file != null && file.exists() && fileSize != 0 && fileSize <= file.length()) {//下载结束
                    Intent intent = new Intent(mContext, MessagevideoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("path", msg.getExtFilePath());
                    bundle.putString("image", msg.getExtThumbPath());
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }else{//还没下载结束
                    boolean isDownLoading = mMessage.getStatus() == Status.STATUS_WAITING || mMessage.getStatus() == Status.STATUS_LOADING;

                    if (isDownLoading) {
                        ComposeMessageActivityControl.releaseFileTransmission(mMessage);
                    } else {
                        ComposeMessageActivityControl.resumeFileTransmission(mMessage, isGroupChat);
                    }
                    adapter.notifyItemChanged(getAdapterPosition());
                }
            }else if( msg.getType() == Type.TYPE_MSG_VIDEO_SEND){// 本地发送文件
                Intent intent = new Intent(mContext, MessagevideoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("path", msg.getExtFilePath());
                bundle.putString("image", msg.getExtThumbPath());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }else{//不存在这种情况

            }
        }
    };
}
