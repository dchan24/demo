package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.glide.GlideApp;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.io.File;

import static com.constvalue.MessageModuleConst.MessageChatListAdapter.MAX_IMG_SIZE_IN_LIST;

public class ImageMsgSendHolder extends BaseViewHolder {
    private static final String TAG = ImageMsgSendHolder.class.getName();
	public ImageView sendFailedView;

	public ImageView imageMsg;
	public ProgressBar mSendProgress;

	public TextView mLoadingSize;

	public FrameLayout layoutLoading;
	public FrameLayout frameLayout;

	public TextView sTvHasRead;
	public ImageView sendStatus;

    public CheckBox multiCheckBox;

	public ImageMsgSendHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {

		super(itemView ,activity ,adapter ,presenter);

		sendFailedView = itemView.findViewById(R.id.imageview_msg_send_failed);
		imageMsg = itemView.findViewById(R.id.imageview_msg_image);
		mSendProgress = itemView.findViewById(R.id.progress_send_small);
		mLoadingSize = itemView.findViewById(R.id.tv_loading_text);
		layoutLoading = itemView.findViewById(R.id.layout_loading);
		frameLayout = itemView.findViewById(R.id.imgae_fl);
		sTvHasRead = itemView.findViewById(R.id.tv_has_read);
		sendStatus = itemView.findViewById(R.id.iv_send_status);
        multiCheckBox = itemView.findViewById(R.id.multi_check);

		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());
		imageMsg.setOnClickListener(new NoDoubleClickListener());
		sendFailedView.setOnClickListener(mFailedClickListener);
		imageMsg.setOnLongClickListener(new OnMsgContentLongClickListener());

	}

    public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
        ((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);

        if(isMultiSelectMode){
            //消息气泡上下居中
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
            params.topToTop = R.id.ll;
            params.bottomToBottom = R.id.ll;
            multiCheckBox.setLayoutParams(params);
            multiCheckBox.setVisibility(View.VISIBLE);
            multiCheckBox.setChecked(isSelected);
        }else{
            multiCheckBox.setVisibility(View.GONE);
        }
    }


    public void bindImage(int maxSize, int minSize, int midSize){
        final Message msg = mMessage;
	    int type = msg.getType();
	    int status = msg.getStatus();

        long fileSize = msg.getExtFileSize();
        long downSize = msg.getExtDownSize();
        int progress;
        if (downSize <= 0 || fileSize <= 0) {
            progress = 0;
        } else if (downSize >= fileSize) {
            progress = 100;
        } else {
            progress = (int) (100 * downSize / fileSize);
        }

        //缩略图
        String thumbPath = msg.getExtThumbPath();

        Bitmap thumbBitmap = BitmapFactory.decodeFile(thumbPath);
        //压缩图
        String extFilePath = msg.getExtFilePath();
        boolean isImageReady = false;//大图已经准备好加载了
        boolean isGif = false;
        if (extFilePath != null && !extFilePath.isEmpty()) {
            File file = new File(extFilePath);
            if (file != null && file.exists()) {
                if (file.length() < MAX_IMG_SIZE_IN_LIST){
                    isImageReady = true;
                }
            }
            String suffix = FileUtil.getFilePostfix(extFilePath);
            isGif = ".gif".equalsIgnoreCase(suffix);
            if (isGif) {
                isImageReady = true;
            }
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(extFilePath, opts);
        if ((opts.outWidth <= 0 || opts.outHeight <= 0)) {//
            isImageReady = false;
        }

        ViewGroup.LayoutParams params2 = imageMsg.getLayoutParams();
        if (thumbBitmap != null) {//缩略图正常
            imageMsg.setBackgroundColor(Color.TRANSPARENT);
            calculateSize(params2, (thumbBitmap.getWidth() * 1.0) / (thumbBitmap.getHeight() * 1.0), maxSize, minSize, midSize);
            if(fileSize> FileUtil.MAX_IMG_SIZE){// 20M以上的图片，无论什么图，都只加载thumb
                imageMsg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageMsg.setImageBitmap(thumbBitmap);
            }else if (isImageReady) {
                if (isGif) {
                    imageMsg.setScaleType(ImageView.ScaleType.FIT_XY);
                } else {
                    imageMsg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                RequestOptions options = new RequestOptions().placeholder(new BitmapDrawable(mContext.getResources(),thumbBitmap))
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).dontTransform();
                GlideApp.with(App.getAppContext())
                        .load(extFilePath)
                        .apply(options)
                        .into(imageMsg);
            } else {
                imageMsg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageMsg.setImageBitmap(thumbBitmap);
            }
        }  else {//缩略图 加载失败。
            params2.width = midSize;
            params2.height = midSize * 2 / 3;
            layoutLoading.getLayoutParams().height = params2.height;
            layoutLoading.getLayoutParams().width = params2.width;
            imageMsg.setScaleType(ImageView.ScaleType.CENTER);
            imageMsg.setBackgroundColor(mContext.getResources().getColor(R.color.color_e5e5e5));
            imageMsg.setImageResource(R.drawable.chat_image_loading_fail);
            mSendProgress.setVisibility(View.GONE);
            mLoadingSize.setVisibility(View.GONE);
            sendFailedView.setVisibility(View.GONE);
            LogF.e(TAG, "Image Thumb is null, thumb:" + msg.getExtThumbPath() + ",id: " + msg.getId()+",file: " + msg.getExtFilePath());
        }


        layoutLoading.getLayoutParams().height = params2.height;
        layoutLoading.getLayoutParams().width = params2.width;
        if (type == Type.TYPE_MSG_IMG_SEND_CCIND) {
            status = Status.STATUS_OK;
        }

        if(status == Status.STATUS_ERROR){
            sendFailedView.setImageResource(R.drawable.chat_messagelist_error);
        }else{
            sendFailedView.setImageResource(R.drawable.send_message_fail_drawable);
        }

        switch (status) {
            case Status.STATUS_WAITING:
            case Status.STATUS_LOADING:
                mSendProgress.setVisibility(View.VISIBLE);
                mLoadingSize.setVisibility(View.VISIBLE);
                mLoadingSize.setText(progress + "%");
                sendFailedView.setVisibility(View.GONE);
                break;
            case Status.STATUS_OK:
                mSendProgress.setVisibility(View.GONE);
                mLoadingSize.setVisibility(View.GONE);
                sendFailedView.setVisibility(View.GONE);
                break;
            case Status.STATUS_FAIL:
                mSendProgress.setVisibility(View.GONE);
                mLoadingSize.setVisibility(View.GONE);
                sendFailedView.setVisibility(View.VISIBLE);
                break;
            default:
                mSendProgress.setVisibility(View.GONE);
                mLoadingSize.setVisibility(View.GONE);
                sendFailedView.setVisibility(View.VISIBLE);
        }
	}

    /**
     *  发送文件， 文件imageview 尺寸计算
     * @param params
     * @param ratio
     * @param maxSize
     * @param minSize
     * @param midSize
     */
    private void calculateSize(ViewGroup.LayoutParams params, double ratio, int maxSize, int minSize, int midSize) {
        double width = 0;
        double height = 0;
        //根据宽高比来设置外框的size
        if (ratio < 0.4) {
            width = minSize;
            height = maxSize;
        } else if (ratio >= 0.4 && ratio <= 0.5) {
            width = minSize;
            height = minSize / ratio;
        } else if (ratio > 0.5 && ratio < 1) {
            width = midSize * ratio;
            height = midSize;
        } else if (ratio >= 1 && ratio < 1 / 0.5) { //和前面的宽高转置
            height = midSize * (1 / ratio);
            width = midSize;
        } else if (ratio >= 1 / 0.5 && ratio < 1 / 0.4) {
            height = minSize;
            width = minSize / (1 / ratio);
        } else if (ratio >= 1 / 0.4) {
            height = minSize;
            width = maxSize;
        }
        params.width = (int) width;
        params.height = (int) height;
    }

    @Override
    public void bindSendStatus() {
        int status = mMessage.getStatus();
        int msgType = mMessage.getType();
        int receipt = mMessage.getMessage_receipt();
        if (isEPGroup || isPartyGroup) {
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
                if (msgType == Type.TYPE_MSG_IMG_SEND_CCIND) {
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

            if(mMessage.getStatus() == Status.STATUS_ERROR){
                ToastUtils.showShort(R.string.big_img_unsupport);
                return;
            }

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
}
