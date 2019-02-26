package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.cmcc.cmrcs.android.glide.GlideApp;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.MediaSet;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.presenter.PreviewImagePresenter;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

import rx.functions.Func1;


/**
 * Created by guoxietao on 2017/4/28.
 */

public class PreviewImageAdapter extends PagerAdapter {
    private static final String TAG = "PreviewImageAdapter";
    private final WeakReference<Context> mContextWeakReference;
    private MediaSet mMediaSet;
    private LayoutInflater mInflater;
    private OnPhotoTapListener mPhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private View.OnLongClickListener mOnLongClickListener;
    private PreviewImagePresenter mPresenter;

    public PreviewImageAdapter(Context c, MediaSet set, PreviewImagePresenter presenter) {
        mContextWeakReference = new WeakReference<>(c);
        mMediaSet = set;
        mInflater = LayoutInflater.from(mContextWeakReference.get());
        mPresenter = presenter;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        mOnLongClickListener = onLongClickListener;
    }

    public void setOnPhotoTapListener(OnPhotoTapListener mPhotoTapListener) {
        this.mPhotoTapListener = mPhotoTapListener;
    }

    public void setOnViewTapListener(OnViewTapListener listener){
        this.mViewTapListener = listener;
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        final MediaItem item = mMediaSet.getMediaList().get(position);
        View view = mInflater.inflate(R.layout.item_preview_image_view_pager, null);
        final PhotoView photoView = (PhotoView) view;
        if (mPhotoTapListener != null) {
            photoView.setOnPhotoTapListener(mPhotoTapListener);
        }
        if(mViewTapListener != null){
            photoView.setOnViewTapListener(mViewTapListener);
        }
        photoView.setOnLongClickListener(mOnLongClickListener);
        //photoView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); //关闭硬件加速

        //缩略图
        String thumbPath = item.getThumbPath();
        boolean isThumbExist = false;
        if (thumbPath != null && !thumbPath.isEmpty() && (new File(thumbPath)).exists()) {
            isThumbExist = true;
        }
        //压缩图
        String imagePath = item.getLocalPath();
        File file = new File(imagePath);
        long fileSize = item.getFileLength();
        long downSize = item.getDownSize();
        boolean isExists = file.exists();
        boolean isFullFile = isExists && fileSize != 0 && fileSize == file.length() && downSize == fileSize;
        boolean isLeft = (item.getMessageType() & Type.TYPE_RECV) > 0;
        if (!isLeft && isExists) {
            isFullFile = true;
        }

        // 公众号图片，公众号下发的gif图不跑这里，gif图已存本地，url为文件路径
        if (imagePath != null && imagePath.contains("http:")) {
            loadPublicAccoutImage(imagePath, thumbPath, photoView, item, position);
            container.addView(view);
            return view;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        RequestOptions options = new RequestOptions()
               // .fallback(R.drawable.preview_image_fail)//url为空时的图
                .error(R.drawable.preview_image_fail)//错误图
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .override(Target.SIZE_ORIGINAL)  //加载原图，不做优化
//                .dontTransform() //图片不做拉伸等操作
                //.dontAnimate()  //图片不做渐入等动画
                ;
        if (isFullFile) {
            BitmapFactory.decodeFile(imagePath, opts);
            if ((opts.outWidth <= 0 || opts.outHeight <= 0)) {
                photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);// imageview， 错误图，居中显示，不拉伸。
                photoView.setImageResource(R.drawable.preview_image_fail);
            } else {
                photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);//
                if (isThumbExist) {
                   //优化加载大图闪烁问题
                    options = options.placeholder(new BitmapDrawable(BitmapFactory.decodeFile(thumbPath)));
                }
                if(file.length()> FileUtil.MAX_IMG_SIZE){//20M以上的原图，要优先显示 中间图
                    String destPath = PreviewImagePresenter.getPreviewImagePath(file.getPath());
                    File midFile = new File(destPath);
                    if(midFile != null && midFile.exists()){//中间图存在
                        GlideApp.with(App.getAppContext()).load(midFile).apply(options).into(photoView);
                    }else{//中间图不存在
                        GlideApp.with(App.getAppContext()).load(imagePath).apply(options).into(photoView);
                    }
                }else{
                    GlideApp.with(App.getAppContext()).load(imagePath).apply(options).into(photoView);
                }

//                Bitmap bm = BitmapFactory.decodeFile(url);
//                photoView.setImageBitmap(bm);
            }
        } else if(item.getStatus() == Status.STATUS_DESTROY){//下载失败的图。
            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);// imageview， 错误图，居中显示，不拉伸。
            photoView.setImageResource(R.drawable.preview_image_fail);
        }else if (isThumbExist) {// 大图未完整，，也没失败， 认为该图片正在下载中。
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);//
            GlideApp.with(App.getAppContext()).asBitmap().load(thumbPath).apply(options).into(photoView);
        } else {//完全没有缩略图,
            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);// imageview， 错误图，居中显示，不拉伸。
            photoView.setImageResource(R.drawable.preview_image_fail);
        }
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return mMediaSet.getMediaItemCounts();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        try {
            ImageView photoView = (ImageView) ((View) object).findViewById(R.id.pv_item);
            if (null == photoView) {
                return;
            }
            Glide.with(App.getAppContext()).clear(photoView);
            Drawable drawable = photoView.getDrawable();
            if (drawable == null) {
                return;
            }
            Bitmap bitmap = null;
            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }
            if (bitmap!= null && !bitmap.isRecycled()) {
                bitmap.recycle();
                System.gc();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    // 加载显示公众号图片
    private void loadPublicAccoutImage(String url, String thumbpath, ImageView photoView, final MediaItem item,final int position) {
        RequestOptions thumbnailOpts = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).dontAnimate().dontTransform();
        RequestBuilder<Drawable> thumbnailRequest = Glide
                .with(App.getAppContext())
                .load(thumbpath).apply(thumbnailOpts);

        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(R.drawable.preview_image_fail).dontAnimate().dontTransform();
        Glide.with(App.getAppContext()).load(url).apply(options)
                .thumbnail(thumbnailRequest)
                .into(photoView);
        final String httpurl = url;
        RxAsyncHelper rxAsyncHelper = new RxAsyncHelper("");
        rxAsyncHelper.runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                // 下载到本地
                try {
                    File file = Glide.with(App.getAppContext())
                            .load(httpurl)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                    if (file == null) {
                        return null;
                    }
                    String originPath = file.getPath();
                    Log.d(TAG, "public account photo glide disk cache path :" + originPath);
                    // 获取原文件名
                    String name = httpurl;
                    int beginIndex = httpurl.lastIndexOf("/") + 1;
                    if (beginIndex < httpurl.length()) {
                        name = httpurl.substring(beginIndex);
                    }
                    String destPath = FileUtil.getThumbnailDir() + "/" + name;
                    Log.d(TAG, "public account photo dest path :" + destPath);
                    File destFile = new File(destPath);
                    boolean destFilExists = false;
                    if (destFile.exists()) {
                        Log.d(TAG, "public account photo dest path exists");
                        destFilExists = true;
                    } else {
                        boolean copyResult =  FileUtil.copyTo(originPath, destPath);
                        Log.d(TAG, "public account photo copy result :" + copyResult);
                        if (copyResult) {
                            destFilExists = true;
                        }
                    }
                    if (destFilExists) {
                        item.setLocalPath(destPath);// localpath由http网络路径改为本地文件路径，才能根据localpath识别二维码，与保存
                        long filesize = destFile.length();
                        item.setDownSize(filesize);//不可少
                        item.setFileLength(filesize);// 不可少
                        mPresenter.checkImageForTowDimension(position);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).subscribe();
    }

}
