package com.cmicc.module_message.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.YunFile;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.chinamobile.app.yuliao_core.util.TimeUtil;
import com.cmcc.cmrcs.android.glide.GlideApp;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.FileDetailActivity;
import com.cmicc.module_message.ui.activity.MessagevideoActivity;
import com.cmicc.module_message.ui.activity.PreviewImageActivity;
import com.cmcc.cmrcs.android.ui.adapter.headerrecyclerview.BaseHeaderAdapter;
import com.cmcc.cmrcs.android.ui.adapter.headerrecyclerview.PinnedHeaderEntity;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.dialogs.FileMenuDialog;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.presenter.PreviewImagePresenter;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.YunFileXmlParser;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.ui.view.RoundTransForm;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.ChatFileActivity;
import com.constvalue.MessageModuleConst;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_THUMB_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_TYPE;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT;

import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_MESSAGE_FORWARD;

/**
 * @anthor situ
 * @time 2017/6/8 16:20
 * @description 聊天文件列表adapter
 */

public class ChatFileAdapter extends BaseHeaderAdapter<MediaItem, PinnedHeaderEntity<MediaItem>> {

    private Context mContext;
    private int mBoxType;
    private String mAddress;
    private boolean isImgVideo;
    private String mNickNameOther;

    @Override
    public void setData(List<PinnedHeaderEntity<MediaItem>> data) {
//        int size = data.size();
//
//        if(size <1){
//            mData = data;
//            notifyDataSetChanged();
//        }else if(mData.size() == data.size()){
//            MediaItem head0 = mData.get(0).getData();
//            MediaItem head1 = data.get(0).getData();
//            MediaItem foot0 = mData.get(0).getData();
//            MediaItem foot1 = data.get(0).getData();
//            if(mData.get(0).equals(data.get(0))
//                    && mData.get(size-1).equals(data.get(size-1))){
//                for(int i = 0; i < size; i ++){
//                    MediaItem item0 = mData.get(i).getData();
//                    MediaItem item1 = data.get(i).getData();
//                    if(item0 == null || item1== null){
//                        mData.set(i, data.get(i));
//                        notifyItemChanged(i);
//                        return;
//                    }
//                    if(item0.hashCodeOrigin() != item1.hashCodeOrigin()){//如果如果有变动，则
//                        mData.set(i, data.get(i));
//                        notifyItemChanged(i);
//                    }
//                }
//            }else{
//                notifyDataSetChanged();
//            }
//        }else{
//            mData = data;
//            notifyDataSetChanged();
//        }
//
//        List<PinnedHeaderEntity<MediaItem>> oldData = new ArrayList<>();
//        oldData.addAll(mData);
////
//        MediaItem item = data.get(1).getData();
////
//        DiffUtil.DiffResult result  = DiffUtil.calculateDiff(new DiffCB(oldData, data));
//        this.mData = data;
//        result.dispatchUpdatesTo(this);
        this.mData = data;
        notifyDataSetChanged();
    }

    class DiffCB extends DiffUtil.Callback {
        private List<PinnedHeaderEntity<MediaItem>> oldList = new ArrayList<>();
        private List<PinnedHeaderEntity<MediaItem>> newList ;
        public DiffCB(List<PinnedHeaderEntity<MediaItem>>oldList, List<PinnedHeaderEntity<MediaItem>> newList){
//            this.oldList.clear();
//            this.oldList.addAll(oldList);
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            LogF.i("VamprieKK", "areItemsTheSame: start ");
            MediaItem item1 = oldList.get(oldItemPosition).getData();
            MediaItem item2 = newList.get(newItemPosition).getData();
            if(item1 == null && item2 == null){
                if(oldList.get(oldItemPosition).getPinnedHeaderName().equals(newList.get(newItemPosition).getPinnedHeaderName())){
                    return true;
                }
            }
            if(item1 == null || item2 == null){
                LogF.i("VamprieKK", "areItemsTheSame: null" + oldItemPosition + "-" + newItemPosition);
                return false;
            }


            if(item1.getID() == item2.getID()){

                return item1.getDownSize() == item2.getDownSize();
            }else{
                return false;
            }
//            boolean result =
//            LogF.i("VamprieKK", "areItemsTheSame: " + result  + oldItemPosition+"-" + newItemPosition);
//            return result;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
//            LogF.i("VamprieKK", "areContentsTheSame: ");
            MediaItem item1 = oldList.get(oldItemPosition).getData();
            MediaItem item2 = newList.get(newItemPosition).getData();
            if(item1 == item2){
                return true;
            }
            if(item1 == null || item2 == null){
                return false;
            }

            boolean result =  (item1.hashCodeOrigin() == item2.hashCodeOrigin());
//            LogF.i("VamprieKK", "areContentsTheSame: " + result);
            return result;
        }
    }


//    private DiffUtil.Callback mDiffCallback = new DiffUtil.Callback() {
//        @Override
//        public int getOldListSize() {
//            return mData.size();
//        }
//
//        @Override
//        public int getNewListSize() {
//            return mNewData.size();
//        }
//
//        @Override
//        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
//            LogF.i("VamprieKK", "areItemsTheSame: start ");
//            MediaItem item1 = mData.get(oldItemPosition).getData();
//            MediaItem item2 = mNewData.get(newItemPosition).getData();
//            if(item1 == item2){
//                LogF.i("VamprieKK", "areItemsTheSame: " + oldItemPosition + "-" + newItemPosition + mData + mNewData);
//                return true;
//            }
//            if(item1 == null || item2 == null){
//                LogF.i("VamprieKK", "areItemsTheSame: null" + oldItemPosition + "-" + newItemPosition);
//                return false;
//            }
//
//
//            boolean result =  (item1.hashCodeOrigin() == item2.hashCodeOrigin());
//            LogF.i("VamprieKK", "areItemsTheSame: " + result  + oldItemPosition+"-" + newItemPosition);
//            return result;
//        }
//
//        @Override
//        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
////            LogF.i("VamprieKK", "areContentsTheSame: ");
//            MediaItem item1 = mData.get(oldItemPosition).getData();
//            MediaItem item2 = mNewData.get(newItemPosition).getData();
//            if(item1 == item2){
//                return true;
//            }
//            if(item1 == null || item2 == null){
//                return false;
//            }
//
//            boolean result =  (item1.hashCodeOrigin() == item2.hashCodeOrigin());
////            LogF.i("VamprieKK", "areContentsTheSame: " + result);
//            return result;
//        }
//    };

    private OnClickListener onItemClickListener = new OnClickListener() {
        @Override
        public void onClickListener(View v, int position) {
            Bundle bundle = new Bundle();
            MediaItem mediaItem = getItem(position);
            int mediaType = mediaItem.getMediaType();
            if (mediaType == MediaItem.MEDIA_TYPE_IMAGE) {

                if(BitmapFactory.decodeFile(mediaItem.getThumbPath()) == null){// 缩略图加载失败
                    BaseToast.show(mContext, mContext.getString(R.string.load_failed));
                    return;
                }

                int chatType = mBoxType == Type.TYPE_BOX_GROUP ? TYPE_GROUP_CHAT : MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT;
                Intent intent = new Intent(mContext, PreviewImageActivity.class);
                bundle.putInt(PreviewImagePresenter.KEY_CONV_TYPE, chatType);
                bundle.putLong(PreviewImagePresenter.KEY_MESSAGE_ID, mediaItem.getID());
                bundle.putString(PreviewImagePresenter.KEY_ADDRESS, mediaItem.getAddress());
                bundle.putString(PreviewImagePresenter.KEY_EXT_THUMB_PATH, mediaItem.getThumbPath());
                bundle.putString(MessageModuleConst.PreviewImagePresenterConst.FROM, MessageModuleConst.PreviewImagePresenterConst.FROM_CHAT_FILE_ACTIVITY);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
                ((Activity) mContext).overridePendingTransition(0, 0);
            } else if (mediaType == MediaItem.MEDIA_TYPE_FILE) {
                openChatFile(mediaItem);
            } else if (mediaType == MediaItem.MEDIA_TYPE_VIDEO) {

                if(BitmapFactory.decodeFile(mediaItem.getThumbPath()) == null){// 缩略图加载失败
                    BaseToast.show(mContext, mContext.getString(R.string.load_failed));
                    return;
                }

                openChatVideo(mediaItem);
            }
        }
    };

    private OnLongClickListener onItemLongClickListener = new OnLongClickListener() {
        @Override
        public void onLongClickListener(View v, int position) {
            final MediaItem mediaItem = getItem(position);
            if (mediaItem == null) {
                return;
            }
            final int mediaType = mediaItem.getMediaType();
            MessageOprationDialog messageOprationDialog;
            String[] itemList;
            if (mediaType == MediaItem.MEDIA_TYPE_IMAGE) {

                if(BitmapFactory.decodeFile(mediaItem.getThumbPath()) == null){// 缩略图加载失败
                    BaseToast.show(mContext, mContext.getString(R.string.load_failed));
                    return;
                }
                itemList = mContext.getResources().getStringArray(R.array.msg_search_img_long_click);
                messageOprationDialog = new MessageOprationDialog(mContext, null, itemList, null);
                messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
                    @Override
                    public void onClick(String item, int which, String address) {
                        if (item.equals(mContext.getString(R.string.forwarld))) {
                            String extFilePath = mediaItem.getLocalPath();
                            File file = new File(extFilePath);
                            if (!file.exists()) {
                                BaseToast.show(mContext, mContext.getString(R.string.toast_download_img));
                                return;
                            }
                            Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
                            Bundle bundle = new Bundle();
                            bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
                            boolean isBigImage = mediaItem.getFileLength()> FileUtil.MAX_IMG_SIZE;//20M以上大图，需要特殊处理
                            if(isBigImage) {
                                File midFile = new File(PreviewImagePresenter.getPreviewImagePath(extFilePath));
                                if (midFile == null || !midFile.exists()) {//中间图不存在
                                    BaseToast.show(mContext, mContext.getString(R.string.toast_download_img));
                                    PreviewImagePresenter.compressImage(extFilePath,null,null);
                                } else {
                                    bundle.putString(MESSAGE_FILE_PATH, midFile.getPath());
                                }
                            }else{
                                bundle.putString(MESSAGE_FILE_PATH, extFilePath);
                            }
                            bundle.putString(MESSAGE_FILE_THUMB_PATH, mediaItem.getThumbPath());
                            i.putExtras(bundle);
                            mContext.startActivity(i);
                        } else if (item.equals(mContext.getString(R.string.delete))) {
                            delete(mediaItem.getID());
                        } else if (item.equals(mContext.getString(R.string.collect))) {
                            String extFilePath = mediaItem.getLocalPath();
                            File file = new File(extFilePath);
                            if (!file.exists()) {
                                BaseToast.show(mContext, mContext.getString(R.string.toast_download_img));
                                return;
                            }
                            if (((ChatFileActivity) mContext).mPresenter.collect(mediaItemToMessage(mediaItem))) {
                                BaseToast.show(mContext, mContext.getString(R.string.collect_succ));
                            }
                        }
                    }
                });
                messageOprationDialog.show();
            } else if (mediaType == MediaItem.MEDIA_TYPE_FILE) {
                itemList = mContext.getResources().getStringArray(R.array.msg_search_img_long_click);
                messageOprationDialog = new MessageOprationDialog(mContext, null, itemList, null);
                messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
                    @Override
                    public void onClick(String item, int which, String address) {
                        if (item.equals(mContext.getString(R.string.forwarld))) {
                            File file;
                            String extFilePath;
                            boolean isFileDownloadSuccess;
                            if (mediaItem.getMessageType() == Type.TYPE_MSG_FILE_YUN_RECV || mediaItem.getMessageType() == Type.TYPE_MSG_FILE_YUN_SEND) {
                                YunFile yunfile = YunFileXmlParser.parserYunFileXml(mediaItem.getMessageBody());
                                if (yunfile == null) {
                                    return;
                                }
                                file = new File(yunfile.getLocalPath());
                                extFilePath = file.getAbsolutePath();
                                isFileDownloadSuccess = file.exists() && (file.length() == yunfile.getFileSize());
                            } else {
                                extFilePath = mediaItem.getLocalPath();
                                file = new File(extFilePath);
                                isFileDownloadSuccess = file.exists() && (file.length() == mediaItem.getFileLength());
                            }

                            if (!isFileDownloadSuccess) {
                                BaseToast.show(mContext, mContext.getString(R.string.toast_download_file));
                                return;
                            }
//                            Intent i = ContactsSelectActivity.createIntentForMessageForward(mContext);
                            Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD,1);
                            Bundle bundle = new Bundle();
                            bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_FILE_SEND);
                            bundle.putString(MESSAGE_FILE_PATH, extFilePath);
                            bundle.putString(MESSAGE_FILE_THUMB_PATH, mediaItem.getThumbPath());
                            i.putExtras(bundle);
                            mContext.startActivity(i);
                        } else if (item.equals(mContext.getString(R.string.delete))) {
                            delete(mediaItem.getID());
                        } else if (item.equals(mContext.getString(R.string.collect))) {
                            File file;
                            String extFilePath;
                            if (mediaItem.getMessageType() == Type.TYPE_MSG_FILE_YUN_RECV || mediaItem.getMessageType() == Type.TYPE_MSG_FILE_YUN_SEND) {
                                YunFile yunfile = YunFileXmlParser.parserYunFileXml(mediaItem.getMessageBody());
                                if (yunfile == null) {
                                    return;
                                }
                                file = new File(yunfile.getLocalPath());
                            } else {
                                extFilePath = mediaItem.getLocalPath();
                                file = new File(extFilePath);
                            }
                            if (!file.exists()) {
                                BaseToast.show(mContext, mContext.getString(R.string.toast_download_file));
                                return;
                            }
                            if (((ChatFileActivity) mContext).mPresenter.collect(mediaItemToMessage(mediaItem))) {
                                BaseToast.show(mContext, mContext.getString(R.string.collect_succ));
                            }
                        }
                    }
                });
                messageOprationDialog.show();
            } else if (mediaType == MediaItem.MEDIA_TYPE_VIDEO) {

                if(BitmapFactory.decodeFile(mediaItem.getThumbPath()) == null){// 缩略图加载失败
                    BaseToast.show(mContext, mContext.getString(R.string.load_failed));
                    return;
                }

                itemList = mContext.getResources().getStringArray(R.array.msg_search_img_long_click);
                messageOprationDialog = new MessageOprationDialog(mContext, null, itemList, null);
                messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
                    @Override
                    public void onClick(String item, int which, String address) {
                        if (item.equals("转发")) {
                            String extFilePath = mediaItem.getLocalPath();
                            File file = new File(extFilePath);
                            if (!file.exists()) {
                                if (mediaItem.getMessageType() == Type.TYPE_MSG_VIDEO_RECV) {
                                    BaseToast.show(mContext, mContext.getString(R.string.toast_download_video));
                                } else {
                                    BaseToast.show(mContext, mContext.getString(R.string.toast_download_video_send));
                                }
                                return;
                            }
                            if (file.length() != mediaItem.getFileLength()) {
                                if (mediaItem.getMessageType() == Type.TYPE_MSG_VIDEO_RECV) {
                                    BaseToast.show(mContext, mContext.getString(R.string.downloading));
                                    return;
                                }
                            }
//                            Intent i = ContactsSelectActivity.createIntentForMessageForward(mContext);
                            Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD,1);
                            Bundle bundle = new Bundle();
                            bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
                            bundle.putString(MESSAGE_FILE_PATH, extFilePath);
                            bundle.putString(MESSAGE_FILE_THUMB_PATH, mediaItem.getThumbPath());
                            i.putExtras(bundle);
                            mContext.startActivity(i);
                        } else if (item.equals(mContext.getString(R.string.delete))) {
                            delete(mediaItem.getID());
                        } else if (item.equals(mContext.getString(R.string.collect))) {
                            String extFilePath = mediaItem.getLocalPath();
                            File file = new File(extFilePath);
                            if (!file.exists()) {
                                BaseToast.show(mContext, mContext.getString(R.string.toast_download_video));
                                return;
                            }
                            if (((ChatFileActivity) mContext).mPresenter.collect(mediaItemToMessage(mediaItem))) {
                                BaseToast.show(mContext, mContext.getString(R.string.collect_succ));
                            }
                        }
                    }
                });
                messageOprationDialog.show();
            }
        }
    };

    public ChatFileAdapter(Context context, int boxType ,String address,boolean isImgVideo ,String nickNameOther) {

        mContext = context;
        mBoxType = boxType;
        mAddress = address;
        this.isImgVideo = isImgVideo;
        mNickNameOther = nickNameOther;

        setOnItemClickListener(onItemClickListener);
        setOnItemLongClickListener(onItemLongClickListener);
    }

    @Override
    protected ItemViewHolder itemViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case MediaItem.MEDIA_TYPE_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_file_img, parent, false);
                return new ChatImgViewHolder(view);
            case MediaItem.MEDIA_TYPE_VIDEO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_file_video, parent, false);
                return new ChatVideoViewHolder(view);
            case MediaItem.MEDIA_TYPE_FILE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_file_file, parent, false);
                return new ChatFileViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_file_file, parent, false);
                return new ChatFileViewHolder(view);
        }
    }

    @Override
    protected HeadViewHolder headerViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_file_header, parent, false);
        if(!isImgVideo){
            view.setBackgroundResource(R.color.transparent);
        }
        return new ChatFileHeadViewHolder(view);
    }

    @Override
    protected void bindHeaderViewHolder(HeadViewHolder viewHolder, PinnedHeaderEntity<MediaItem> item) {
        ((ChatFileHeadViewHolder) viewHolder).tv.setText(item.getPinnedHeaderName());
    }

    @Override
    protected void bindItemViewHolder(ItemViewHolder viewHolder, PinnedHeaderEntity<MediaItem> item, int viewType) {
        switch (viewType) {
            case MediaItem.MEDIA_TYPE_IMAGE:
                bindChatImage((ChatImgViewHolder) viewHolder, item.getData());
                break;
            case MediaItem.MEDIA_TYPE_VIDEO:
                bindChatVideo((ChatVideoViewHolder) viewHolder, item.getData());
                break;
            case MediaItem.MEDIA_TYPE_FILE:
                bindChatFile((ChatFileViewHolder) viewHolder, item.getData());
                break;
            default:

        }

    }

    private void bindChatImage(ChatImgViewHolder holder, MediaItem mediaItem) {
        ImageView imageView = holder.img;
        //缩略图
        String path = mediaItem.getThumbPath();
        boolean isThumbExist = false;
        if (path != null && !path.isEmpty() && (new File(path)).exists()) {
            isThumbExist = true;
        }
        if (isThumbExist) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            RequestOptions requestOptions = new RequestOptions().dontAnimate().transform(new RoundTransForm(mContext));
            GlideApp.with(mContext).load(path) .placeholder(R.drawable.cc_chat_albumimage_default)
                    .error(R.drawable.cc_chat_albumimage_default).apply(requestOptions).into(imageView);
            return;
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RequestOptions requestOptions = new RequestOptions().dontAnimate().transform(new RoundTransForm(mContext ,4));
        Glide.with(mContext).load(R.drawable.cc_chat_albumimage_default).apply(requestOptions).into(imageView);
    }

    private void bindChatVideo(ChatVideoViewHolder holder, MediaItem mediaItem) {
        ImageView imageView = holder.img;
        //缩略图
        String path = mediaItem.getThumbPath();
        boolean isThumbExist = false;
        if (path != null && !path.isEmpty() && (new File(path)).exists()) {
            isThumbExist = true;
        }
        if (isThumbExist) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            RequestOptions requestOptions = new RequestOptions().dontAnimate().transform(new RoundTransForm(mContext ,4));
            Glide.with(mContext).load(path).apply(requestOptions).into(imageView);
        } else {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            RequestOptions requestOptions = new RequestOptions().dontAnimate().transform(new RoundTransForm(mContext ,4));
            Glide.with(mContext).load(R.drawable.cc_chat_albumimage_default).apply(requestOptions).into(imageView);
        }

        //时长
        long seconds = mediaItem.getDuration();
        StringBuffer sb = new StringBuffer();
        int temp;
        temp = (int) seconds % 3600 / 60;
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");
        temp = (int) seconds % 3600 % 60;
        sb.append((temp < 10) ? "0" + temp : "" + temp);
        holder.duration.setText(sb);

        long fileSize = mediaItem.getFileLength();
        long downSize = mediaItem.getDownSize();
        int progress;
        if (downSize <= 0 || fileSize <= 0) {
            progress = 0;
        } else if (downSize >= fileSize) {
            progress = 100;
        } else {
            progress = (int) (100 * downSize / fileSize);
        }

        switch (mediaItem.getStatus()) {
            case Status.STATUS_WAITING:
            case Status.STATUS_LOADING:
                // 发送中
                holder.mImgFail.setVisibility(View.GONE);
                holder.mProgressBar.setVisibility(View.VISIBLE);
                holder.mProgressText.setText(""+progress);
                holder.mProgressText.setVisibility(View.VISIBLE);
                holder.mProgressBar.start();
                break;
            case Status.STATUS_OK:
                // 成功
                holder.mImgFail.setVisibility(View.GONE);
                holder.mProgressBar.setVisibility(View.GONE);
                holder.mProgressBar.pause();
                holder.mProgressText.setVisibility(View.GONE);
                break;
            case Status.STATUS_PAUSE:
                // 暂停
                holder.mImgFail.setVisibility(View.GONE);
                holder.mProgressBar.setVisibility(View.VISIBLE);
                holder.mProgressText.setText(""+progress);
                holder.mProgressText.setVisibility(View.VISIBLE);
                holder.mProgressBar.pause();;
                break;
            default:
                // 失败
                holder.mImgFail.setVisibility(View.VISIBLE);
                holder.mProgressBar.setVisibility(View.GONE);
                holder.mProgressBar.pause();
                holder.mProgressText.setVisibility(View.GONE);
                break;
        }
    }

    private void bindChatFile(ChatFileViewHolder holder, MediaItem item) {
        long fileSize = item.getFileLength();
        holder.fileSize.setText(StringUtil.formetFileSize(fileSize));
        String fileNameRecv = item.getFileName();
        holder.fileName.setText(fileNameRecv);

        String sendAddress = item.getSendAddress();
        String person;
        if(!TextUtils.isEmpty(sendAddress)){
            String loginUser = LoginUtils.getInstance().getLoginUserName();
            if(NumberUtils.getDialablePhoneWithCountryCode(sendAddress).equals(NumberUtils.getDialablePhoneWithCountryCode(loginUser))){
                person = "我";
            }else{
                if(mBoxType == Type.TYPE_BOX_GROUP){
                    person = NickNameUtils.getNickName(mContext,sendAddress ,mAddress);
                }else{
                    if(!TextUtils.isEmpty(mNickNameOther)){
                        person = mNickNameOther;
                    }else{
                        person = NickNameUtils.getPerson(mContext ,mBoxType ,sendAddress);
                    }
                }

            }
        }else{
            person = sendAddress;
        }
        holder.person.setText(person);

        holder.date.setText(TimeUtil.formatFavoriteTime(item.getDate()));

        // 文件图标
        if (!TextUtils.isEmpty(fileNameRecv)) {
            if (fileNameRecv.endsWith(".txt")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_txt);
            } else if (fileNameRecv.endsWith(".png") || fileNameRecv.endsWith(".jpg") || fileNameRecv.endsWith(".PNG") || fileNameRecv.endsWith(".JPG") || fileNameRecv.endsWith(".jpeg") || fileNameRecv.endsWith(".JPEG") || fileNameRecv.endsWith(".GIF") || fileNameRecv.endsWith(".gif") || fileNameRecv.endsWith(".bmp") || fileNameRecv.endsWith(".BMP")) {
                //缩略图
                String path = item.getThumbPath();
                if (!TextUtils.isEmpty(path) && (new File(path)).exists()) {
                    RequestOptions requestOptions = new RequestOptions().dontAnimate()
                            .transform(new RoundTransForm(mContext)).placeholder(R.drawable.cc_chat_collect_pic).error(R.drawable.cc_chat_collect_pic);
                    Glide.with(mContext).load(path).apply(requestOptions).into(holder.img);
                } else {
                    holder.img.setImageResource(R.drawable.cc_chat_collect_pic);
                }
            } else if (fileNameRecv.endsWith(".rar")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_rar);
            } else if (fileNameRecv.endsWith(".zip")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_zip);
            } else if (fileNameRecv.endsWith(".mp3") || fileNameRecv.endsWith(".wav") || fileNameRecv.endsWith(".3ga") || fileNameRecv.endsWith(".amr")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_music);
            } else if (fileNameRecv.endsWith(".rmvb") || fileNameRecv.endsWith(".RMVB") || fileNameRecv.endsWith(".mov") || fileNameRecv.endsWith(".mp4") || fileNameRecv.endsWith(".MP4") || fileNameRecv.endsWith(".MOV") || fileNameRecv.endsWith(".3gp") || fileNameRecv.endsWith(".3GP") || fileNameRecv.endsWith(".WMV") || fileNameRecv.endsWith(".wmv") || fileNameRecv.endsWith(".AVI") || fileNameRecv.endsWith(".avi") || fileNameRecv.endsWith(".FLV") || fileNameRecv.endsWith(".flv")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_mp4);
            } else if (fileNameRecv.endsWith(".doc") || fileNameRecv.endsWith(".docx")
                    || fileNameRecv.endsWith(".DOC") || fileNameRecv.endsWith(".DOCX")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_doc);
            } else if (fileNameRecv.endsWith(".ppt") || fileNameRecv.endsWith(".ppts")
                    || fileNameRecv.endsWith(".pptx") || fileNameRecv.endsWith(".PPT") || fileNameRecv.endsWith(".PPTS")
                    || fileNameRecv.endsWith(".PPTX")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_ppt);
            } else if (fileNameRecv.endsWith(".pdf") || fileNameRecv.endsWith(".PDF")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_pdf);
            } else if (fileNameRecv.endsWith(".xls") || fileNameRecv.endsWith(".xlsx") || fileNameRecv.endsWith(".XLS") || fileNameRecv.endsWith(".XLSX")) {
                holder.img.setImageResource(R.drawable.cc_chat_collect_xlsx);
            } else {
                holder.img.setImageResource(R.drawable.chat_localfile_unknown);
            }
        } else {
            holder.img.setImageResource(R.drawable.chat_localfile_unknown);
        }
    }

    class ChatFileHeadViewHolder extends HeadViewHolder {
        TextView tv;
        View divider;
        public ChatFileHeadViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_date);
            divider = itemView.findViewById(R.id.divider);
            if(!isImgVideo){
                divider.setVisibility(View.GONE);
            }
        }
    }

    class ChatImgViewHolder extends ItemViewHolder {
        ImageView img;

        public ChatImgViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_thumb);
        }
    }

    class ChatVideoViewHolder extends ItemViewHolder {
        ImageView img;
        TextView duration;
        ImageView mImgFail;
        com.cmcc.cmrcs.android.widget.MediaTransfProgressBar mProgressBar;
        TextView mProgressText;

        public ChatVideoViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_thumb);
            duration = itemView.findViewById(R.id.video_duration);
            mImgFail = itemView.findViewById(R.id.recv_failed);
            mProgressBar = itemView.findViewById(R.id.progress_bar);
            mProgressText = itemView.findViewById(R.id.progress_text);
        }
    }

    class ChatFileViewHolder extends ItemViewHolder {
        TextView fileName;
        ImageView img;
        TextView fileSize;
        TextView person;
        TextView date;

        public ChatFileViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            img = itemView.findViewById(R.id.favorite_image_shortcut);
            fileSize = itemView.findViewById(R.id.file_size);
            person = itemView.findViewById(R.id.tv_name);
            date = itemView.findViewById(R.id.tv_time);
        }
    }

    private void openChatFile(MediaItem mediaItem) {

        int chatType = mBoxType == Type.TYPE_BOX_GROUP ? MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT : MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT;

        Message msg = mediaItemToMessage(mediaItem);
        Bundle bundle = new Bundle();//用于文件menu对话框的 “收藏”功能，传参。
        bundle.putInt(FileMenuDialog.OPEN_FILE_ARG_CHAT_TYPE, chatType);
        bundle.putSerializable(FileMenuDialog.OPEN_FILE_ARG_MESSAGE, msg);
        bundle.putInt(FileMenuDialog.OPEN_FILE_ARG_FROM, FileMenuDialog.OPEN_FILE_FROM_CHAT_FILE_ACTIVITY);

        int messageType = mediaItem.getMessageType();
        if (messageType == Type.TYPE_MSG_FILE_YUN_SEND || messageType == Type.TYPE_MSG_FILE_YUN_RECV) {// 和彩云文件
            if (mediaItem.getStatus() == Status.STATUS_DESTROY) {
                BaseToast.show(mContext, mContext.getString(R.string.message_content_overdue));
                return;
            }
            final YunFile yunFile = YunFileXmlParser.parserYunFileXml(mediaItem.getMessageBody());
            Intent yunIntent = new Intent(mContext, FileDetailActivity.class);

            if(!TextUtils.isEmpty(mediaItem.getLocalPath())){//直接打开文件
                File file = new File(mediaItem.getLocalPath());
                if(file !=null && file.exists()){
                    bundle.putString(FileMenuDialog.OPEN_FILE_ARG_PATH, yunFile.getLocalPath());
                    FileUtil.openFile(mContext, file.getAbsolutePath(), bundle);
                    return;
                }
            }
            yunIntent.putExtra(FileDetailActivity.KEY_DIALOG_ARGS, bundle);
            mContext.startActivity(yunIntent);

        } else if (messageType == Type.TYPE_MSG_FILE_RECV || messageType == Type.TYPE_MSG_FILE_SEND || messageType == Type.TYPE_MSG_FILE_SEND_CCIND) {// 中兴文件。
            if (mediaItem.getStatus() == Status.STATUS_DESTROY) {
                BaseToast.show(mContext, mContext.getString(R.string.message_content_overdue));
                return;
            }

            if (!isSend(messageType)) {
                boolean isDone = isDone(mediaItem);
                if (!isDone && mediaItem.getAddressId() == 99) {
                    BaseToast.show(mContext, mContext.getString(R.string.transfer_news));
                    return;
                }
            }

            bundle.putString(FileMenuDialog.OPEN_FILE_ARG_PATH, mediaItem.getLocalPath());
            if(isDone(mediaItem)){// 文件已经下载完成， 直接打开文件。
                FileUtil.openFile(mContext, mediaItem.getLocalPath(), bundle);
                return;
            }

            Intent intent = new Intent(mContext, FileDetailActivity.class);
            intent.putExtra(FileDetailActivity.KEY_DIALOG_ARGS, bundle);
            mContext.startActivity(intent);
        }

    }

    private void openChatVideo(MediaItem mediaItem) {
        if (mediaItem.getStatus() == Status.STATUS_DESTROY) {
            BaseToast.show(mContext, mContext.getString(R.string.message_content_overdue));
            return;
        }

        if (!isSend(mediaItem.getMessageType())) {
            boolean isDone = false;
            long videoFileSize = mediaItem.getFileLength();
            String fileName = mediaItem.getLocalPath();
            File videoFile = new File(fileName);
            if (videoFile.exists() && videoFileSize != 0 && videoFileSize <= videoFile.length()) {
                isDone = true;
            }
            if (!isDone && mediaItem.getAddressId() == 99) {
                BaseToast.show(mContext, mContext.getString(R.string.transfer_news));
                return;
            }
        }
        if (checkVideoDownload(mediaItem)) {
            Intent intent = new Intent(mContext, MessagevideoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("path", mediaItem.getLocalPath());
            bundle.putString("image", mediaItem.getThumbPath());
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }
    }

    private boolean isDone(MediaItem mediaItem) {
        boolean isDone = false;
        long fileSize = mediaItem.getFileLength();
        String fileName = mediaItem.getLocalPath();
        File file = new File(fileName);
        if (file.exists() && fileSize != 0 && fileSize <= file.length()) {
            isDone = true;
        }
        return isDone;
    }

    private boolean isSend(int type) {
        if ((type & Type.TYPE_SEND) > Type.TYPE_SEND) {
            return true;
        }
        return false;
    }

    private boolean checkVideoDownload(MediaItem mediaItem) {
        if (isSend(mediaItem.getMessageType())) {
            return true;
        }
        boolean isDone = false;
        long fileSize = mediaItem.getFileLength();
        String fileName = mediaItem.getLocalPath();
        long downSize = mediaItem.getDownSize();
        File file = new File(fileName);
        int id = mediaItem.getID();
        if (file.exists() && fileSize != 0 && fileSize <= file.length()) {
            isDone = true;
        } else {
            if(!AndroidUtil.isNetworkConnected(mContext)){
                BaseToast.show(mContext,mContext.getString(R.string.net_connect_error));
                return false;
            }
            if (mediaItem.getStatus() != Status.STATUS_LOADING) {
                boolean isGroupChat = mBoxType == Type.TYPE_BOX_GROUP;
                if (isGroupChat) {
                    if (downSize > 0) {
                        int iStartOffset = (int) downSize + 1;
                        int iStopOffset = (int) fileSize;
                        ComposeMessageActivityControl.rcsImFileResumeByRecverX(id, mediaItem.getAddress(), mediaItem.getThreadId(), file.getAbsolutePath(), iStartOffset, iStopOffset);
                    } else {
                        ComposeMessageActivityControl.rcsImFileFetchViaMsrpX(id, mediaItem.getAddress(), mediaItem.getThreadId(), file.getAbsolutePath());
                    }
                } else {
                    if (downSize > 0) {
                        int iStartOffset = (int) downSize + 1;
                        int iStopOffset = (int) fileSize;
                        ComposeMessageActivityControl.rcsImFileResumeByRecver(id, mediaItem.getAddress(), mediaItem.getThreadId(), file.getAbsolutePath(), iStartOffset, iStopOffset);
                    } else {
                        ComposeMessageActivityControl.rcsImFileFetchViaMsrp(id, mediaItem.getAddress(), mediaItem.getThreadId(), file.getAbsolutePath());
                    }
                }
            }
        }
        return isDone;
    }

    private void delete(int msgId) {
        if (mBoxType == Type.TYPE_BOX_GROUP) {
            Message m = new Message();
            m.setId(msgId);
            m.setStatus(Status.STATUS_DELETE);
            GroupChatUtils.update(mContext, m);
        } else if (mBoxType == Type.TYPE_BOX_MESSAGE) {
            Message m = new Message();
            m.setId(msgId);
            m.setStatus(Status.STATUS_DELETE);
            MessageUtils.updateMessage(mContext, m);
        }
    }

    private Message mediaItemToMessage(MediaItem mediaItem) {
        Message message = new Message();
        message.setAddress(mediaItem.getAddress());
        message.setType(mediaItem.getMessageType());
        message.setStatus(mediaItem.getStatus());
        message.setExtFileSize(mediaItem.getDownSize());
        message.setExtDownSize(mediaItem.getDownSize());
        message.setExtFilePath(mediaItem.getLocalPath());
        message.setExtThumbPath(mediaItem.getThumbPath());
        message.setMsgId(mediaItem.getMsgId());
        message.setBoxType(mediaItem.getBoxType());
        message.setId(mediaItem.getID());
        message.setDate(mediaItem.getDate());
        message.setBody(mediaItem.getMessageBody());
        message.setPerson(mediaItem.getmPerson());
        message.setSendAddress(mediaItem.getSendAddress());
        message.setExtSizeDescript(mediaItem.getmExtSizeDescript());

        return message;
    }
}
