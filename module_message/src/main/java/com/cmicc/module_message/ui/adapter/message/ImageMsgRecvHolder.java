package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.io.File;

import static com.constvalue.MessageModuleConst.MessageChatListAdapter.MAX_IMG_SIZE_IN_LIST;

public class ImageMsgRecvHolder extends BaseViewHolder {
    private static final String TAG = ImageMsgRecvHolder.class.getName();
	public ImageView imageMsg;
	public FrameLayout frameLayout;
    public CheckBox multiCheckBox;

	public ImageMsgRecvHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		frameLayout = (FrameLayout) itemView.findViewById(R.id.imgae_fl);
		imageMsg = (ImageView) itemView.findViewById(R.id.imageview_msg_image);
        multiCheckBox = itemView.findViewById(R.id.multi_check);
		imageMsg.setOnClickListener(new NoDoubleClickListener());
		imageMsg.setOnLongClickListener(new OnMsgContentLongClickListener());

	}

    @Override
    public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
        ((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
	    if(isMultiSelectMode){
	        //头像不显示，以消息气泡上下居中
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
	        if(sIvHead.getVisibility() == View.INVISIBLE){
                params.topToTop = R.id.imgae_fl;
                params.bottomToBottom = R.id.imgae_fl;

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

	public void bindImage(int chatType, final int maxSize, final int minSize,final int midSize){
        final Message msg = mMessage;
	    String recvImgthumbPath = msg.getExtThumbPath();
        long fileSize = msg.getExtFileSize();
        long downSize = msg.getExtDownSize();

        final ViewGroup.LayoutParams params1 = imageMsg.getLayoutParams();

        // 公众号图片
        if (recvImgthumbPath != null && chatType == MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT) {
            RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    calculateSize(params1, resource.getIntrinsicWidth() / resource.getIntrinsicHeight(), maxSize, minSize, midSize);
                    return false;
                }
            };
            RequestOptions options = new RequestOptions().placeholder(imageMsg.getDrawable())
                    .error(R.drawable.chat_image_loading_fail).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .dontTransform();
            Glide.with(App.getAppContext())
                    .load(recvImgthumbPath)
                    .apply(options)
                    .listener(requestListener)
                    .into(imageMsg);
            return;
        }

        Bitmap recvThumbBitmap = BitmapFactory.decodeFile(recvImgthumbPath);

        //压缩图
        String extFilePath = msg.getExtFilePath();
        boolean isImageReady = false;
        boolean isGift = false;
        if (extFilePath != null && !extFilePath.isEmpty()) {
            if (extFilePath.endsWith(".gif")) {
                isGift = true;
            }
            File file = new File(extFilePath);// 修改文件名字
            if (file != null) {
                isImageReady = file.exists() && fileSize != 0 && fileSize == file.length() && downSize == file.length();
                if (file.length() >= MAX_IMG_SIZE_IN_LIST && !isGift) {
                    isImageReady = false;
                }
            }
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(extFilePath, opts);
        if ((opts.outWidth <= 0 || opts.outHeight <= 0)) {//
            isImageReady = false;
        }

        if (recvThumbBitmap != null) {
            imageMsg.setBackgroundColor(Color.TRANSPARENT);
            calculateSize(params1, (recvThumbBitmap.getWidth() * 1.0) / (recvThumbBitmap.getHeight() * 1.0), maxSize, minSize, midSize);
            if(fileSize> FileUtil.MAX_IMG_SIZE){// 20M以上的图片，无论什么图，都只加载thumb
                imageMsg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageMsg.setImageBitmap(recvThumbBitmap);
            }else if (isImageReady) {
                if (isGift) {
                    imageMsg.setScaleType(ImageView.ScaleType.FIT_XY);
                } else {
                    imageMsg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                RequestOptions options = new RequestOptions().placeholder(new BitmapDrawable(recvThumbBitmap))
                        .error(R.drawable.chat_image_loading_fail).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .dontTransform();
                Glide.with(App.getAppContext())
                        .load(extFilePath)
                        .apply(options)
                        .into(imageMsg);

            } else {
                imageMsg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageMsg.setImageBitmap(recvThumbBitmap);
            }
        } else {
            params1.width = midSize;
            params1.height = midSize * 2 / 3;
            imageMsg.setScaleType(ImageView.ScaleType.CENTER);
            imageMsg.setBackgroundColor(mContext.getResources().getColor(R.color.color_e5e5e5));
            imageMsg.setImageResource(R.drawable.chat_image_loading_fail);
            LogF.e(TAG, "Image Thumb is null, thumb:" + msg.getExtThumbPath() + ",id: " + msg.getId()+",file: " + msg.getExtFilePath());
        }
	}


    /**
     *  接收图片消息，图片imageview  计算
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
}
