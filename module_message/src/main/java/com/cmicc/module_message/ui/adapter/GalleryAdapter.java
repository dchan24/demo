package com.cmicc.module_message.ui.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_common.application.App;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.presenter.GalleryPresenter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by GuoXietao on 2017/3/28.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private static final String TAG = "GalleryAdapter";
    public ArrayList<MediaItem> mAllMediaItems = new ArrayList<>();
    private Activity mActivity;
    private Listener mListener;
    private boolean canRefresh = true;

    public GalleryAdapter(Activity activity) {
        mActivity = activity;
        Glide.get(App.getAppContext()).clearMemory();
        System.gc();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        GalleryViewHolder viewHolder = new GalleryViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onViewAttachedToWindow(GalleryViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition == 0) {
            return;
        }
        updateItemSelectUI(holder, mAllMediaItems.get(adapterPosition - 0));
    }

    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, int position) {

        if (position < 0 || position >= mAllMediaItems.size()) { // 防止下标越界
            return;
        }
        if(!canRefresh ){
            holder.mIvVideoIcon.setVisibility(View.GONE);
            holder.mTvVideoTime.setVisibility(View.GONE);
            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.cc_chat_albumimage_default)
                    .error(R.drawable.cc_chat_albumimage_default);
            Glide.with(mActivity)
                    .load(R.drawable.cc_chat_albumimage_default)
                    .apply(requestOptions)
                    .into(holder.mIvGallery);
            return;
        }
        MediaItem mItem = mAllMediaItems.get(position);
        if (mItem != null) {
            holder.mIvGallery.setVisibility(View.VISIBLE);
            holder.mIvSelect.setVisibility(View.VISIBLE);
            String localPath = mItem.getLocalPath();
            int mediaType = mItem.getMediaType();
            if (mediaType == MediaItem.MEDIA_TYPE_VIDEO) {
                holder.mIvVideoIcon.setVisibility(View.VISIBLE);
                holder.mTvVideoTime.setVisibility(View.VISIBLE);
                int seconds = (int) (mItem.getDuration() / 1000);
                holder.mTvVideoTime.setText(TimeUtil.getHHMMSSTimeString(seconds));//设置视频时间
            } else if (mediaType == MediaItem.MEDIA_TYPE_IMAGE) {
                holder.mIvVideoIcon.setVisibility(View.GONE);
                holder.mTvVideoTime.setVisibility(View.GONE);
            }

            loadImage(holder.mIvGallery, mItem);
            updateItemSelectUI(holder, mItem);
        }
    }


    @Override
    public int getItemCount() {
        if (mAllMediaItems != null) {
            return mAllMediaItems.size() + 0;
        } else {
            return 0;
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        private final NoDoubleClickListener mNoDoubleClickListener;

        ImageView mIvGallery;

        RelativeLayout mRLIvSelect;
        ImageView mIvSelect;

        RelativeLayout mRlImg;

        View mIvVideoIcon;

        TextView mTvVideoTime;

        public GalleryViewHolder(final View itemView) {
            super(itemView);
            mIvGallery = itemView.findViewById(R.id.iv_gallery);
            mIvSelect = itemView.findViewById(R.id.iv_select);
            mRlImg = itemView.findViewById(R.id.rl_img);
            mIvVideoIcon = itemView.findViewById(R.id.iv_video_icon);
            mTvVideoTime = itemView.findViewById(R.id.tv_video_time);
            mRLIvSelect = itemView.findViewById(R.id.rliv_select);

            mNoDoubleClickListener = new NoDoubleClickListener() {
                @Override
                protected void onNoDoubleClick(View v) {
                    int position = getAdapterPosition();
                    if (position < 0 || position >= mAllMediaItems.size()) { // 防止下标越界
                        return;
                    }
                    Bitmap bitmap = GalleryPresenter.getSelectListBitmap(position - 0); // 在用户选择的当前文件夹中查看是否存在
                    if (bitmap == null) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.file_not_exit), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MediaItem mediaItem = mAllMediaItems.get(position - 0);
                    if (mediaItem == null) {   // 判null处理
                        Toast.makeText(mActivity, mActivity.getString(R.string.file_not_exit), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String localPath = mediaItem.getLocalPath();
                    if (localPath != null && !localPath.isEmpty()) {
                        File file = new File(localPath);
                        if (file != null && file.exists()) {
                            if (mListener != null) {
                                mListener.onClick(v, position);
                                if (v.getId() == R.id.iv_select || v.getId() == R.id.rliv_select) {
                                    updateItemSelectUI(GalleryViewHolder.this, mAllMediaItems.get(position - 0));
                                }
                            }
                        } else {
                            Toast.makeText(mActivity, mActivity.getString(R.string.file_not_exit), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mActivity, mActivity.getString(R.string.file_not_exit), Toast.LENGTH_SHORT).show();
                    }
                }
            };
            mIvGallery.setOnClickListener(mNoDoubleClickListener);
            mRLIvSelect.setOnClickListener(mNoDoubleClickListener); // 扩大点击范围
            mIvSelect.setOnClickListener(mNoDoubleClickListener);
            mRlImg.setOnClickListener(mNoDoubleClickListener);
        }

    }

    public interface Listener {
        void onClick(View v, int position);
    }

    public void updateItemSelectUI(GalleryViewHolder holder, MediaItem mediaItem) {
        if (GalleryPresenter.mOrderSelectedItems != null && mediaItem != null) {
            if (GalleryPresenter.mOrderSelectedItems.contains(mediaItem)) { // 当前向已经选中
                holder.mIvSelect.setImageResource(R.drawable.cc_chat_picture_selected);
            } else {
                holder.mIvSelect.setImageResource(R.drawable.cc_chat_picture_unselected);
            }
        }
    }

    public abstract class NoDoubleClickListener implements View.OnClickListener {

        public static final int MIN_CLICK_DELAY_TIME = 400;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;
                onNoDoubleClick(v);
            }
        }

        protected abstract void onNoDoubleClick(View v);
    }


    /**
     * 描述	：载入Image对象
     *
     * @param imageView
     * @param path      Image文件路径
     */
    private void loadImage(final ImageView imageView, MediaItem path) {
        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.cc_chat_albumimage_default)
                .error(R.drawable.cc_chat_albumimage_default);
        Glide.with(imageView.getContext())
                .load(path.getLocalPath())
                .apply(requestOptions)
                .into(imageView);
    }

    /**
     * 设置数据
     *
     * @param allMediaItems
     */
    public void setmAllMediaItemsClear(ArrayList<MediaItem> allMediaItems) {
        mAllMediaItems.clear();
        mAllMediaItems.addAll(allMediaItems);
        notifyDataSetChanged();
    }


    /**
     * 是否允许onBindView 加载本地图片
     * @param canRefresh
     */
    public void enableRefresh(boolean canRefresh){
        this.canRefresh = canRefresh;
    }
}

