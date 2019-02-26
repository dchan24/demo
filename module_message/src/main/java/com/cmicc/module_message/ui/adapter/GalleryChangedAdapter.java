package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.presenter.GalleryPresenter;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GuoXietao on 2017/4/11.
 */

public class GalleryChangedAdapter extends PagerAdapter {

    private final WeakReference<Context> mContextWeakReference;
    private List<MediaItem> mMediaItems = new ArrayList<>();
    private LayoutInflater mInflater;

//    private List<View> mViewList = new ArrayList<>();//viewPager的view复用

    public void setPhotoClickListener(OnViewTapListener photoClickListener) {
        mPhotoClickListener = photoClickListener;
    }

    private OnViewTapListener mPhotoClickListener;

    /**
     * 构造函数
     *
     * @param
     * @param c         上下文
     * @param
     */
    public GalleryChangedAdapter(Context c, ArrayList<MediaItem> items) {
        mContextWeakReference = new WeakReference<>(c);
        mMediaItems.clear(); // 先清楚原来的数据
        mMediaItems.addAll(items); // 添加全新的数据
        mInflater = LayoutInflater.from(mContextWeakReference.get());
        Glide.get(App.getAppContext()).clearMemory();
        System.gc();

//        mViewList.add(mInflater.inflate(R.layout.item_gallery_gallery_view_pager, null));
//        mViewList.add(mInflater.inflate(R.layout.item_gallery_gallery_view_pager, null));
//        mViewList.add(mInflater.inflate(R.layout.item_gallery_gallery_view_pager, null));
    }

    @Override
    public int getCount() {
        return mMediaItems!=null?mMediaItems.size():0;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {//此处需要优化， 每次都需要重新inflate一个View，然后重新 findView， 效率很低
        View view = mInflater.inflate(R.layout.item_gallery_gallery_view_pager, null);
        final PhotoView photoView = view.findViewById(R.id.pv_item);
        final View videoLayer = view.findViewById(R.id.video_layer);
        photoView.setOnViewTapListener(mPhotoClickListener);

        if(position>=0 && position<mMediaItems.size() && mMediaItems.get(position)!=null ){ // 防止下标越界和null
            final MediaItem mediaItem = mMediaItems.get(position);
            LogF.e("GalleryFragment 3","path = "+ mediaItem.getLocalPath());
            int index = GalleryPresenter.mAllMediaItems.indexOf(mediaItem); // mAllMediaItems
            if (index <0) {
                Glide.with(App.getAppContext()).load(R.drawable.chat_image_loading_fail).into(photoView);
                photoView.setZoomable(false);
                container.addView(view);
                return view;
            }


            index = GalleryPresenter.mMediaSetList.indexOf(mediaItem);

            if (mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                photoView.setZoomable(false);
                videoLayer.setVisibility(View.VISIBLE);
                videoLayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPhotoClickListener.onViewTap(v,0,0);
                    }
                });

            } else {
                photoView.setZoomable(true);
                videoLayer.setVisibility(View.VISIBLE);
            }

            if(index > 0){
                Bitmap bitmap = GalleryPresenter.getSelectListBitmap(index);
                if(bitmap != null){
                    LogF.e("GalleryFragment 3","bitmap = "+ bitmap.toString());
                    photoView.setImageBitmap(bitmap);
                }
            }



            String url = mediaItem.getLocalPath();
            boolean isFileExist = false;
            boolean isGif = false;
            if (url != null && !url.isEmpty()) {
                int indexUrl = url.lastIndexOf(".");
                if (indexUrl < 0) {
                    indexUrl = 0;
                }
                String suffix = url.substring(indexUrl);
                isGif = ".gif".equalsIgnoreCase(suffix);
                File file = new File(url);
                if (file != null && file.exists()) {
                    isFileExist = true;
                }
            }
            if (isFileExist) {
                if (isGif) {
                    RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).dontAnimate();
                    Glide.with(App.getAppContext()).load(url).apply(options).into(photoView);
                } else {
                    RequestOptions options = new RequestOptions().dontAnimate();
                    Glide.with(App.getAppContext()).asBitmap().load(url).apply(options).into(photoView);
                }
            } else {
                Glide.with(App.getAppContext()).load(R.drawable.chat_image_loading_fail).into(photoView);
                photoView.setZoomable(false);
            }
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


}
