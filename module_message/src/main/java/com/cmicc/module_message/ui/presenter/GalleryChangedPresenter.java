package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.cmicc.module_message.ui.activity.MessagevideoActivity;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.GalleryChangedActivity;
import com.cmicc.module_message.ui.constract.GalleryChangedContract;
import com.constvalue.MessageModuleConst;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by GuoXietao on 2017/4/10.
 */

public class GalleryChangedPresenter implements GalleryChangedContract.Presenter {
    private final Context mContext;
    private final Activity mActivity;

    private ArrayList<MediaItem> mAllMediaItems;
    private ArrayList<MediaItem> mMediaItems;
    private Boolean isPreviewSelect;//仅仅预览选中的几个图片
    private int mCurrentPosition;
    private GalleryChangedContract.View mView;
    //private PowerManager.WakeLock wakeLock = null;

    public GalleryChangedPresenter(Activity activity) {
        mContext = activity;
        this.mActivity = activity;
    }

    @Override
    public void start() {
    }

    @Override
    public void setPreviewSelect(Boolean previewSelect) {
        isPreviewSelect = previewSelect;
        if (isPreviewSelect) {
            mMediaItems = getSelectItems();
        } else {
            mMediaItems = mAllMediaItems;
        }

    }

    @Override
    public Boolean getPreviewSelect() {
        return isPreviewSelect;

    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        if (currentPosition < 0) {
            currentPosition = 0;
        }
        this.mCurrentPosition = currentPosition;
    }

    @Override
    public void setView(GalleryChangedContract.View view) {
        mView = view;
    }

    @Override
    public void setAllMediaItems(ArrayList<MediaItem> allMediaItems) {
        mAllMediaItems = allMediaItems;
    }

    @Override
    public ArrayList<MediaItem> getMediaItems() {
        return mMediaItems;
    }

    @Override
    public ArrayList<MediaItem> getSelectItems() {
        ArrayList<MediaItem> mSelectedItems = new ArrayList<>();
        mSelectedItems.addAll(GalleryPresenter.mOrderSelectedItems);
        return mSelectedItems;
    }


    @Override
    public void handleSelect(boolean shouldAdd) {
        Log.e("XXX", "handleSelect:" + mCurrentPosition);
        if( GalleryPresenter.mOrderSelectedItems != null&& mCurrentPosition<0 || mCurrentPosition >= mMediaItems.size() ){ // 防止下标越界
            return;
        }
        MediaItem item = mMediaItems.get(mCurrentPosition); // 从用户当前选中的文件夹中选择，如果从全局集合中选择，会发送不对应的情况。
        if(GalleryPresenter.mOrderSelectedItems.contains(item)){  // 选中的集合中已经包含了这项，则需要移除调这项
            if(shouldAdd){
                return;
            }else{
                GalleryPresenter.mOrderSelectedItems.remove(item);
            }
        }else{
//            if(GalleryPresenter.mOrderSelectedItems.size()>8){
//                Toast.makeText(mContext, mContext.getString(R.string.limit_9_media), Toast.LENGTH_SHORT).show();
//            }else if(item.getFileLength()> GalleryPresenter.MAX_FILE_SIZE){
//                Toast.makeText(mContext, mContext.getString(R.string.max_file_length), Toast.LENGTH_SHORT).show();
//            }else{
//                GalleryPresenter.mOrderSelectedItems.add(item);
//            }

            Iterator<MediaItem> iterator = GalleryPresenter.mOrderSelectedItems.iterator() ;  // 不管从那个文件夹中选择，已选择中的对象放在的是用一个集合中。
            if (item.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) {
                boolean canSelect = true; // 可以选择的标志
                while (iterator.hasNext()){
                    MediaItem mediaItem = iterator.next();
                    if(mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO){
                        Toast.makeText(mContext, mContext.getString(R.string.pic_or_video), Toast.LENGTH_SHORT).show();
                        canSelect = false;
                        break;
                    }else if(mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE){
                        if(getSelectItems().size()>8){
                            Toast.makeText(mContext, mContext.getString(R.string.limit_upper_9), Toast.LENGTH_SHORT).show();
                            canSelect = false;
                            break;
                        }

                    }
                }
                if (canSelect) {
                    GalleryPresenter.mOrderSelectedItems.add(item);
                }
            } else if (item.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                boolean canSelect = true; // 可以选择的标志
                while (iterator.hasNext()){
                    MediaItem mediaItem = iterator.next();
                    if(mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO){
                        if(getSelectItems().size()>0){
                            Toast.makeText(mContext, mContext.getString(R.string.limit_upper_1_video), Toast.LENGTH_SHORT).show();
                            canSelect = false;
                            break;
                        }
                    }else if(mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE){
                        Toast.makeText(mContext, mContext.getString(R.string.pic_or_video), Toast.LENGTH_SHORT).show();
                        canSelect = false;
                        break;
                    }
                }
                if (canSelect) { // 当前向可以选择
                    GalleryPresenter.mOrderSelectedItems.add(item);
                }
            }
        }
        mView.setSelect(item);
        mView.updateSelectNumber(getSelectItems().size());
    }


    @Override
    public void handlePageSelected(int position) {

        MediaItem item = mMediaItems.get(position);
        mView.updateSelectNumber(getSelectItems().size());
        mView.setSelect(item);
        mCurrentPosition = position;

        int mediaType = item.getMediaType();
        if (mediaType == MediaItem.MEDIA_TYPE_VIDEO) {
            mView.showIvPlay(true);
        } else {
            mView.showIvPlay(false);
        }
    }

    @Override
    public void handlePhotoClick() {

    }


    @Override
    public void onDestroy() {
        //releaseWakeLock();
    }

    @Override
    public int getMediaType() {
        return mMediaItems.get(mCurrentPosition).getMediaType();
    }

    @Override
    public boolean isFilebroken(int position) {
        Bitmap bitmap = GalleryPresenter.getBitmap(position);
        if (bitmap == null) {
            return true;
        }
        if(mAllMediaItems == null || mAllMediaItems.size() <= position){
            return true;
        }
        String localPath = mAllMediaItems.get(position).getLocalPath();
        if (localPath != null&&!localPath.isEmpty()) {
            File file = new File(localPath);
            if (file != null && file.exists()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleEditImage() {
        MediaItem item = mMediaItems.get(mCurrentPosition); // 从用户当前选中的文件夹中选择，如果从全局集合中选择，会发送不对应的情况。
        String imageLocalPath = item.getLocalPath();

        String fileName = String.valueOf(System.currentTimeMillis()) + ImgEditorProxy.g.getServiceInterface().getFinalFileNameExtensionMessageImage();

        File mCameraPicture = new File(FileUtil.getSaveDir(),fileName);
        FileUtil.createParentDir(mCameraPicture);
        String mOutputFilePath = mCameraPicture.getAbsolutePath();


        Uri uri = getUri(((GalleryChangedActivity)mContext), imageLocalPath);
        ImgEditorProxy.g.getUiInterface().goPictureEditActivity(mActivity, uri, mOutputFilePath, MessageModuleConst.PreviewImagePresenterConst.FROM_GALLERY_CHANGED_ACTIVITY);
    }

    public static Uri getUri(Activity activity, String path){
        Uri uri = null;
        if (path != null) {
            path = Uri.decode(path);
            ContentResolver cr = activity.getContentResolver();
            StringBuffer buff = new StringBuffer();
            buff.append("(")
                    .append(MediaStore.Images.ImageColumns.DATA)
                    .append("=")
                    .append("'" + path + "'")
                    .append(")");
            Cursor cur = cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Images.ImageColumns._ID },
                    buff.toString(), null, null);
            int index = 0;
            for (cur.moveToFirst(); !cur.isAfterLast(); cur
                    .moveToNext()) {
                index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                index = cur.getInt(index);
            }
            if (index == 0) {
            } else {
                Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                if (uri_temp != null) {
                    uri = uri_temp;
                }
            }
        }
        return uri;
    }

//    // 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
//    private void acquireWakeLock() {
//        if (null == wakeLock) {
//            PowerManager pm = (PowerManager) App.getAppContext().getSystemService(Context.POWER_SERVICE);
//            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "PostLocationService");
//            if (null != wakeLock) {
//                wakeLock.acquire();
//            }
//        }
//    }
//
//    // 释放设备电源锁
//    private void releaseWakeLock() {
//        if (null != wakeLock) {
//            wakeLock.release();
//            wakeLock = null;
//        }
//    }



    @Override
    public void startPlay() {
        String path = mMediaItems.get(mCurrentPosition).getLocalPath();
        String thumb = mMediaItems.get(mCurrentPosition).getThumbPath();
        Intent intent = new Intent(mContext, MessagevideoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putString("image", thumb);
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }
}
