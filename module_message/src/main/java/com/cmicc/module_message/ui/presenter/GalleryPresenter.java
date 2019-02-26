package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.chinamobile.app.utils.Luban;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.activity.GalleryChangedActivity;
import com.cmicc.module_message.ui.constract.GalleryContract;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.fragment.GalleryFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import rx.functions.Func1;

import static com.chinamobile.app.yuliao_business.logic.PersonalProfileSdkManager.TAG;

/**
 * Created by GuoXietao on 2017/3/27.
 */

public class GalleryPresenter implements GalleryContract.Presenter {
    public static final String[] DEFAULT_IMAGE_MIME = {"image/jpeg", "image/jpg", "image/png", "image/bmp", "image/gif"};
    public static final String[] DEFAULT_VIDEO_MIME = {"video/mp4", "video/3gpp"};

    public static ArrayList<MediaItem> mAllMediaItems = new ArrayList<>();
    public static ArrayList<MediaItem> mOrderSelectedItems = new ArrayList<>(); // 存放选中的图片
    public static ArrayList<MediaItem> mMediaSetList = new ArrayList<>(); // 当前选择的图片文件夹
    private boolean mIsOriginPhoto; // 原图标志

    private final Context mContext;
    public GalleryContract.View mView;
    public static final String SELECTED_ITEMS = "SelectedItems";
    public static final String CURRENT_POSITION = "currentPosition";
    public static final String PREVIEW_SELECT = "previewSelect";
    public static final String ORIGIN_PHOTO = "originPhoto";
    public static ContentResolver mMContentResolver;
    public static Cursor mMCursor;
    private final int POST_PIC_SIZE = 500000;//500k以下的图片不需要压缩
    public static final long MAX_FILE_SIZE = 500 * 1024 *1024; // 500M以上的文件不能发送

    public GalleryPresenter(Context context) {
        mContext = context;
        mAllMediaItems.clear();
        mOrderSelectedItems.clear();
    }

    @Override
    public void setView(GalleryContract.View v) {
        mView = v;
    }

    @Override
    public void handleSend(View v) {
        // 压缩图片逻辑
        if(mOrderSelectedItems.size()<1){//没有东西被选中
            return;
        }
        if (!mIsOriginPhoto) {
            mView.showCompressProgressBar();
            new Thread(){
                @Override
                public void run() {
                    for (MediaItem mediaItem : mOrderSelectedItems) {
                        if(mediaItem.getMediaType() != MediaItem.MEDIA_TYPE_IMAGE){//非图片类的，不压缩
                            continue;
                        }
                        String localPath = mediaItem.getLocalPath();
                        File file = new File(localPath);
                        String ext = FileUtil.getFilePostfix(localPath).toLowerCase();

                        if("gif".equals(ext)){//gif图不压缩
                            continue;
                        }

                        if (file.exists() && file.length() > POST_PIC_SIZE) {
                            LogF.d(TAG ,"压缩前图片的大小:"+file.length());
                            String path = file.getName();
                            StringBuffer sb = new StringBuffer(path);
                            int index = path.lastIndexOf(".");
                            if (index > 0) {
                                sb.insert(index, "big");
                                sb.replace(index, sb.length(), ".jpg");
                            } else {
                                sb.append("big");
                            }
                            File bigFile = new File(FileUtil.getThumbnailDir(), sb.toString());
                            if (!bigFile.exists()) {
                                //                                BitmapUtils.compressImageTo200KB(file, bigFile);
                                Log.w(TAG, "开始压缩" + localPath);
                                Luban.get(mContext).load(file).output(bigFile.getAbsolutePath()).launch();
                                Log.w(TAG, "压缩结束" + localPath);
                            }
                            LogF.d(TAG ,"压缩后图片的大小:"+bigFile.length());
                            if (bigFile.exists()) {
                                mediaItem.setLocalPath(bigFile.getAbsolutePath());
                            }
                        }
                    }
                    Intent data = new Intent();
                    data.putExtra(SELECTED_ITEMS, mOrderSelectedItems);
                    data.putExtra(ORIGIN_PHOTO, mIsOriginPhoto);
                    Activity activity = (Activity) mContext;
                    activity.setResult(-1, data);
                    activity.finish();
                }
            }.start();
        } else {
            Intent data = new Intent();
            data.putExtra(SELECTED_ITEMS, mOrderSelectedItems);
            data.putExtra(ORIGIN_PHOTO, mIsOriginPhoto);
            Activity activity = (Activity) mContext;
            activity.setResult(-1, data);
            activity.finish();
        }
    }

    @Override
    public void handlePreviewClicked(View v) {//点击预览， 实际上是预览 已经选中的图片list
        if (getSelectItems().size() > 0) {
            Intent intent = new Intent(mContext, GalleryChangedActivity.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean(PREVIEW_SELECT, true);
            bundle.putBoolean(ORIGIN_PHOTO, mIsOriginPhoto);
            intent.putExtras(bundle);
            ((GalleryFragment) mView).startActivityForResult(intent, 1);
        }
    }

    @Override
    public void handleImageClicked(android.view.View v, int p) {//点击图片， 预览的是整个相册的图片list
        Log.e("XXX", "handleImageClicked:" + p);
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        Intent intent = new Intent(mContext, GalleryChangedActivity.class);
        Bundle bundle = new Bundle();
        //获取图片的大小和位置
        bundle.putInt("locationX", location[0]);
        bundle.putInt("locationY", location[1]);
        bundle.putInt("width", v.getWidth());
        bundle.putInt("height", v.getHeight());
        bundle.putInt(CURRENT_POSITION, p - 0);// 开始预览的第一向
        bundle.putBoolean(ORIGIN_PHOTO, mIsOriginPhoto);
        intent.putExtras(bundle);
        ((GalleryFragment) mView).startActivityForResult(intent, 1);
        //清空activity的切换效果
        ((GalleryFragment) mView).getActivity().overridePendingTransition(0, 0);
    }


    @Override
    public void handleSelect(int p) {
        if( p<0 || p >= mMediaSetList.size() ){ // 防止下标越界
            return;
        }
        MediaItem item = mMediaSetList.get(p); // 从用户当前选中的文件夹中选择，如果从全局集合中选择，会发送不对应的情况。
        //mOrderSelectedItems先遍历这个集合，看是否包含这想
        if(mOrderSelectedItems.contains(item)){  // 选中的集合中已经包含了这项，则需要移除调这项
            mOrderSelectedItems.remove(item);
        }else{
//            if(getSelectItemsNubmer()>8){
//                Toast.makeText(mContext, mContext.getString(R.string.limit_9_media), Toast.LENGTH_SHORT).show();
//            }else if(item.getFileLength()> MAX_FILE_SIZE){
//                Toast.makeText(mContext, mContext.getString(R.string.max_file_length), Toast.LENGTH_SHORT).show();
//            }else{
//                mOrderSelectedItems.add(item);
//            }
            Iterator<MediaItem> iterator = mOrderSelectedItems.iterator() ;  // 不管从那个文件夹中选择，已选择中的对象放在的是用一个集合中。
            if (item.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) {
                boolean canSelect = true; // 可以选择的标志
                while (iterator.hasNext()){
                    MediaItem mediaItem = iterator.next();
                    if(mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO){
                        Toast.makeText(mContext, mContext.getString(R.string.pic_or_video), Toast.LENGTH_SHORT).show();
                        canSelect = false;
                        break;
                    }else if(mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE){
                        if(getSelectItemsNubmer()>8){
                            Toast.makeText(mContext, mContext.getString(R.string.limit_upper_9), Toast.LENGTH_SHORT).show();
                            canSelect = false;
                            break;
                        }

                    }
                }
                if (canSelect) {
                    item.setSelected(true);
                    mOrderSelectedItems.add(item);
                }
            } else if (item.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                boolean canSelect = true; // 可以选择的标志
                while (iterator.hasNext()){
                    MediaItem mediaItem = iterator.next();
                    if(mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO){
                        if(getSelectItemsNubmer()>0){
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
                    item.setSelected(true);
                    mOrderSelectedItems.add(item);
                }
            }
        }

        mView.updateButton(getSelectItemsNubmer());
    }

    @Override
    public void start() {
    }

    /**
     * 获取图片列表数据
     */
    public void getMediaList() {
        mAllMediaItems.clear();
        Uri mMediaItemUri = MediaStore.Files.getContentUri("external");
        mMContentResolver = mContext.getContentResolver();
        StringBuffer selection = new StringBuffer();
        selection.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        selection.append(") AND (");
        for (int i = 0; i < DEFAULT_IMAGE_MIME.length; i++) {
            selection.append(MediaStore.Files.FileColumns.MIME_TYPE).append(" = '").append(DEFAULT_IMAGE_MIME[i]).append("' OR ");
        }
        for (int i = 0; i < DEFAULT_VIDEO_MIME.length; i++) {
            selection.append(MediaStore.Files.FileColumns.MIME_TYPE).append(" = '").append(DEFAULT_VIDEO_MIME[i]).append("'");
            if (i < DEFAULT_VIDEO_MIME.length - 1) {
                selection.append(" OR ");
            }
        }
        selection.append(" )");
        mMCursor = null;
        try {
            mMCursor = mMContentResolver.query(mMediaItemUri, null, selection.toString(), null, FileColumns.DATE_MODIFIED + " DESC");
            if (mMCursor != null) {
                LogF.e(TAG , "gallery Cursor Count = "+mMCursor.getCount());
                while (mMCursor.moveToNext()) {
                    // 获取图片的路径
                    String path = mMCursor.getString(mMCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    if (TextUtils.isEmpty(path)) {
                        continue;
                    }
                    long length = mMCursor.getLong(mMCursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                    if (length <= 0) {
                        continue;
                    }

                    try {
                        File file = new File(path);
                        if (!file.exists() || file.length() <= 0) {
                            continue;
                        }
                        LogF.d("dchan","file last modify time:"+file.lastModified()+",name:"+path);
                    } catch (Exception e) {
                        continue;
                    }

                    // 获取该图片的父路径名
                    int mMediaType = mMCursor.getInt(mMCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
                    MediaItem item = null;
                    if (mMediaType == 1) {//图片
                        item = new MediaItem(path, MediaItem.MEDIA_TYPE_IMAGE);
                    } else if (mMediaType == 3) {//视频
                        item = new MediaItem(path, MediaItem.MEDIA_TYPE_VIDEO);
                    }
                    long thumbNailsId = mMCursor.getLong(mMCursor.getColumnIndex("_ID"));
                    if (item != null) {
                        item.setThumbNailsId(thumbNailsId);
                        item.setFileLength(length);
                        item.setBucketDisplayName(mMCursor.getString(mMCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)));
                        if (mMediaType == 3) {
                            item.setDuration(mMCursor.getLong(mMCursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)));
                        }
                        mAllMediaItems.add(item);
                    } else {
                        LogF.e(TAG, "item is null");
                    }
                }
            }
        } catch (Exception e) {
            LogF.e(TAG, "exception= " + e.toString());
            new RxAsyncHelper<>("").runOnMainThread(new Func1<String, Object>() {
                @Override
                public Object call(String s) {
                    if(mContext == null){
                        return null;
                    }
                    Toast.makeText(mContext, mContext.getString(R.string.load_image_fail), Toast.LENGTH_SHORT).show();
                    return null;
                }
            }).subscribe();
        } finally {
            if(mAllMediaItems!=null) {
                LogF.e(TAG, "gallery AllMediaItems Count = " + mAllMediaItems.size());
            }else{
                LogF.e(TAG, "gallery AllMediaItems null ");
            }
        }
    }

    public ArrayList<MediaItem> getSelectItems() {
        ArrayList<MediaItem> selectedItems = new ArrayList<MediaItem>();
        selectedItems.addAll(mOrderSelectedItems);
        return selectedItems;
    }

    /**
     * 获取选中的数量
     * @return
     */
    public int getSelectItemsNubmer() {
       return mOrderSelectedItems.size();
    }

    @Override
    public void onDestroy() {
        if (mMContentResolver != null) {
            mMContentResolver = null;
        }
        if (mAllMediaItems != null) {
            mAllMediaItems.clear();
            mAllMediaItems = null;
        }

        if (mMCursor != null) {
            mMCursor.close();
            mMCursor = null;
        }
    }

    @Override
    public void handlePhotoModeChange(boolean isChecked) {
        mIsOriginPhoto = isChecked;
    }

    /**
     * 设置当前选中的图片文件夹
     * @param list
     */
    @Override
    public void setSelectMediaList(ArrayList<MediaItem> list) {
        mMediaSetList.clear();
        mMediaSetList.addAll(list);
    }


    public static Bitmap getBitmap(int position) {
        if (position < 0) {
            return null;
        }
        Bitmap mBitmap = null;
        MediaItem mediaItem = null;
        if (mAllMediaItems != null) {
            synchronized (mAllMediaItems){
                if(mAllMediaItems != null && position < mAllMediaItems.size()){
                    mediaItem = mAllMediaItems.get(position);
                }
            }
            if (mediaItem != null) {
                if (mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                    mBitmap = MediaStore.Video.Thumbnails.getThumbnail(GalleryPresenter.mMContentResolver,
                            mediaItem.getThumbNailsId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
                } else {
                    mBitmap = MediaStore.Images.Thumbnails.getThumbnail(GalleryPresenter.mMContentResolver,
                            mediaItem.getThumbNailsId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
                }
            } else {
                return null;
            }
        }
        ExifInterface exifInterface = null;
        try {
            if(mediaItem != null) {
                exifInterface = new ExifInterface(mediaItem.getLocalPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exifInterface != null && mBitmap != null) {
            int mAnInt = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            int mDigree = 0;
            switch (mAnInt) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    mDigree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    mDigree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    mDigree = 270;
                    break;
                default:
                    mDigree = 0;
                    break;
            }
            if (mDigree != 0) {
                Matrix mM = new Matrix();
                mM.postRotate(mDigree);
                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
                        mBitmap.getHeight(), mM, true);
            }
        }
        return mBitmap;

    }

    /**
     * 从当前用户做选择的图片问价夹中取出bitmap
     * @param position
     * @return
     */
    public static Bitmap getSelectListBitmap(int position) {
        if (position < 0) {
            return null;
        }
        Bitmap mBitmap = null;
        MediaItem mediaItem = null;
        if (mMediaSetList != null) {
            synchronized (mMediaSetList){
                if(mMediaSetList != null && position < mMediaSetList.size()){
                    mediaItem = mMediaSetList.get(position);
                }
            }
            if (mediaItem != null && GalleryPresenter.mMContentResolver != null) {
                if (mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                    mBitmap = MediaStore.Video.Thumbnails.getThumbnail(GalleryPresenter.mMContentResolver,
                            mediaItem.getThumbNailsId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
                } else {
                    mBitmap = MediaStore.Images.Thumbnails.getThumbnail(GalleryPresenter.mMContentResolver,
                            mediaItem.getThumbNailsId(), MediaStore.Images.Thumbnails.MINI_KIND, null);
                }
            } else {
                return null;
            }
        }
        ExifInterface exifInterface = null;
        try {
            if(mediaItem != null) {
                exifInterface = new ExifInterface(mediaItem.getLocalPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exifInterface != null && mBitmap != null) {
            int mAnInt = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            int mDigree = 0;
            switch (mAnInt) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    mDigree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    mDigree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    mDigree = 270;
                    break;
                default:
                    mDigree = 0;
                    break;
            }
            if (mDigree != 0) {
                Matrix mM = new Matrix();
                mM.postRotate(mDigree);
                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
                        mBitmap.getHeight(), mM, true);
            }
        }
        return mBitmap;

    }

}
