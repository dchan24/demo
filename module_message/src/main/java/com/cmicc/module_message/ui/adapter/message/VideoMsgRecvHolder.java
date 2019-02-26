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

import java.io.File;
import java.util.Calendar;

public class VideoMsgRecvHolder extends BaseViewHolder {
    private static final String TAG = VideoMsgRecvHolder.class.getName();
	public CheckBox multiCheckBox;

    private ImageView mImageviewMsgSendFailed; //失败重发标志
    private ImageView mThumbView;   //缩略图
    private ImageView mVideoPlayBtn;  //播放按钮
    private TextView mVideoSize; //视频大小
    private TextView mVideoDuration; //视频时长
    private MediaTransfProgressBar mProgressBar;// 一直转的进度条
    private TextView mProgressText;  //进度条的 数字进度

	public VideoMsgRecvHolder(View itemView, Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		multiCheckBox = itemView.findViewById(R.id.multi_check);

        mImageviewMsgSendFailed = itemView.findViewById(R.id.imageview_msg_send_failed);
        mThumbView = itemView.findViewById(R.id.video_thumb);
        mVideoPlayBtn = itemView.findViewById(R.id.video_play);
        mVideoSize = itemView.findViewById(R.id.textview_video_size);
        mVideoDuration = itemView.findViewById(R.id.textview_video_time);
        mProgressBar = itemView.findViewById(R.id.progress_bar);
        mProgressText = itemView.findViewById(R.id.progress_text);

        mProgressBar.setOnClickListener(mProgressBarClickListener);
        mProgressText.setOnClickListener(mProgressBarClickListener);

        mThumbView.setOnClickListener(mClickListener);
        mVideoPlayBtn.setOnClickListener(mClickListener);

        mProgressBar.setOnLongClickListener(new OnMsgContentLongClickListener());
        mProgressText.setOnLongClickListener(new OnMsgContentLongClickListener());
        mVideoPlayBtn.setOnLongClickListener(new OnMsgContentLongClickListener());
        mThumbView.setOnLongClickListener(new OnMsgContentLongClickListener());

        mImageviewMsgSendFailed.setOnClickListener(mFailClickListener);
	}

	/**
	 *
	 * 绑定多选状态
	 * */
	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//头像不显示，以消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			if(sIvHead.getVisibility() == View.INVISIBLE){
				params.topToTop = R.id.ll;
				params.bottomToBottom = R.id.ll;
			}else{
				params.topToTop = R.id.svd_head;
				params.bottomToBottom = R.id.svd_head;
			}
			multiCheckBox.setLayoutParams(params);
			multiCheckBox.setVisibility(View.VISIBLE);
			multiCheckBox.setChecked(isSelected);
		}else{
			multiCheckBox.setVisibility(View.GONE);
		}
	}

	/**
	 *  绑定缩略图
	 * @param msg
	 * @param maxSize
	 * @param minSize
	 */
	public void bindThumb(Message msg, int maxSize, int minSize){
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;

		String extThumbPath = msg.getExtThumbPath();
		boolean isFileExist = false;
		if (extThumbPath != null && !extThumbPath.isEmpty()) {
			File file = new File(extThumbPath);
			if (file != null && file.exists()) {
				isFileExist = true;
			}
		}

		boolean isFileBroken = false;

		if (isFileExist) {
			BitmapFactory.decodeFile(extThumbPath, opts);
			if (opts.outHeight <= 0 || opts.outWidth <= 0) {
				isFileBroken = true;
			}
		}

		ViewGroup.LayoutParams params3 = mThumbView.getLayoutParams();

        RequestOptions options = new RequestOptions().bitmapTransform(new RoundedCorners((int) mContext.getResources().getDimension(R.dimen.dp4))); /*.dontTransform()*///glide参数
		if (!isFileExist || isFileBroken) {//缩略图损坏，或者不存在
		    mThumbView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			params3.width = (int) AndroidUtil.dip2px(App.getAppContext(), 160);
			params3.height = (int) AndroidUtil.dip2px(App.getAppContext(), 100);
			GlideApp.with(App.getAppContext()).load(R.drawable.loadfail_video).apply(options).into(mThumbView);

            mVideoPlayBtn.setVisibility(View.GONE);//播放按钮
            mImageviewMsgSendFailed.setVisibility(View.GONE); //发送失败
            mProgressText.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.pause();
			//缩略图损坏，直接显示错误。

			LogF.e(TAG, "Image Thumb is null, thumb:" + msg.getExtThumbPath() + ",id: " + msg.getId()+",file: " + msg.getExtFilePath());
		} else {
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
	 */
	public void bindDuration(){
		int seconds = Integer.parseInt(mMessage.getExtSizeDescript());
		if(seconds == 0){
			seconds = 1;
		}
		mVideoDuration.setText(TimeUtil.getHHMMSSTimeString(seconds));// 设置视频时间
	//	mVideoSize.setText(StringUtil.formetFileSize(mMessage.getExtFileSize()));
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
			//进度为0时，会出现进度条满的情况，所以这里强制给个初始值
		} else if (downSize >= fileSize) {
			progress = 100;
		} else {
			progress = (int) (100 * downSize / fileSize);
		}

		int status = mMessage.getStatus();

        mProgressText.setText(progress + "%");
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
                mImageviewMsgSendFailed.setVisibility(View.GONE);
                mVideoPlayBtn.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressText.setVisibility(View.VISIBLE);
                mProgressBar.pause();
				break;
			default:
				// 失败
				mImageviewMsgSendFailed.setVisibility(View.VISIBLE);
				mVideoPlayBtn.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressText.setVisibility(View.VISIBLE);
                mProgressBar.pause();
				break;
		}
	}

	//进度条点击事件
    private View.OnClickListener mProgressBarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean isDownLoading = mMessage.getStatus() == Status.STATUS_WAITING || mMessage.getStatus() == Status.STATUS_LOADING;

            if (isDownLoading) {
                ComposeMessageActivityControl.releaseFileTransmission(mMessage);
            } else {
                ComposeMessageActivityControl.resumeFileTransmission(mMessage, isGroupChat);
            }
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
            if(BitmapFactory.decodeFile(msg.getExtThumbPath()) == null){// 缩略图加载失败
                BaseToast.show(mContext, mContext.getString(R.string.load_failed));
                return;
            }

            if (msg.getStatus() == Status.STATUS_DESTROY) {
                BaseToast.show(mContext, mContext.getString(R.string.message_content_overdue));
                return;
            }

            long fileSize = msg.getExtFileSize();
            File file = new File(msg.getExtFilePath());
            if (file != null && file.exists() && fileSize != 0 && fileSize <= file.length()) {// 下载结束
                Intent intent = new Intent(mContext, MessagevideoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("path", msg.getExtFilePath());
                bundle.putString("image", msg.getExtThumbPath());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }else if(msg.getAddressId() == 99){//迁移消息，未见过，保留逻辑
                BaseToast.show(mContext, mContext.getString(R.string.transfer_news));
            } else { //视频尚未下载完成
                mProgressBarClickListener.onClick(v);//执行进度条点击事件
            }
        }
    };

	private View.OnClickListener mFailClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ComposeMessageActivityControl.resumeFileTransmission(mMessage, isGroupChat);
		}
	};
}
