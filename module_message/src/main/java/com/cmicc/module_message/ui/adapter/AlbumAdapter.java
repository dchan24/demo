package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.utils.AndroidUtil;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.MediaSet;
import com.cmcc.cmrcs.android.ui.utils.MD5Util;
import com.cmcc.cmrcs.android.ui.utils.ThumbnailUtils;
import com.cmcc.cmrcs.android.ui.utils.bitmap.NativeImageLoader;
import com.cmicc.module_message.R;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LY on 2018/5/2.
 */

public class AlbumAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private final List<MediaSet> mMediaSetList;
    public static final int COVER_NUMBER = 3;
    private LayoutInflater mInflater;
    private NativeImageLoader mImageLoader;
    private final Point mPoint = new Point(0, 0);// 用来封装ImageView的宽和高的对象
    private ListView mListView;
    private boolean isScrolling = false;// 当不滚动时加载图片
    private LinkedHashMap<String, MediaItem[]> cache;
    private int mType;
    private Bitmap defalut;
    private Context context;

    /**
     * 构造函数
     * @param context 上下文
     * @param mediaSetList 相册对象
     * @param listView
     * @param type 类型
     */
    public AlbumAdapter(Context context, List<MediaSet> mediaSetList, ListView listView, int type) {
        mMediaSetList = mediaSetList;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new NativeImageLoader();
        mPoint.set((int) AndroidUtil.dip2px(context, NativeImageLoader.ALBUM_IMAGE_SIZE), (int) AndroidUtil.dip2px(context, NativeImageLoader.ALBUM_IMAGE_SIZE));
        mListView = listView;
        mListView.setOnScrollListener(this);
        cache = new LinkedHashMap<String, MediaItem[]>();
        mType = type;
        defalut = BitmapFactory.decodeResource(context.getResources(), R.drawable.cc_me_share_logo);
        this.context = context;
    }


    @Override
    public int getCount() {
        return mMediaSetList.size();
    }


    @Override
    public Object getItem(int position) {
        return mMediaSetList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    private static final int STR_MAX_CHAR_COUNT = 10;	//	显示最多字符个数

    /**
     * 描述		：格式化String对象
     * @param stringToFormat 要格式化的文本
     * @return 格式化后的String对象
     */
    private String formatString(String stringToFormat) {
        if(stringToFormat.length() > STR_MAX_CHAR_COUNT){
            stringToFormat = stringToFormat.substring(0, STR_MAX_CHAR_COUNT) + "...";
        }
        return stringToFormat;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_list_gallery_grid_album, null);
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.albumCover);
            viewHolder.mPlayImageView = (ImageView) convertView.findViewById(R.id.albumCoverPlay);
            viewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.albumTitle);
            viewHolder.mSelect = (ImageView) convertView.findViewById(R.id.albumSelect);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MediaSet mediaSet = mMediaSetList.get(position);
        if(mediaSet != null && mediaSet.getMediaList()!= null && mediaSet.getMediaList().size()>0 && mediaSet.getMediaList().get(0)!=null){
            String path = mediaSet.getMediaSetPath();
            viewHolder.mTextViewTitle.setText(String.format(context.getString(R.string.gallery_album_title),formatString(mediaSet.getMediaSetName()),mediaSet.getNum()));
            // viewHolder.mTextViewCounts.setText(Integer.toString(mImageBean.getImageCounts()));
            // 给ImageView设置路径Tag,这是异步加载图片的小技巧
            String tag = path + mediaSet.getMediaSetName();
            viewHolder.mImageView.setTag(tag);
            //视屏图标显示不显示的逻辑处理
            if (mediaSet.getMediaList().get(0).getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                viewHolder.mPlayImageView.setVisibility(View.VISIBLE);
            } else if (mediaSet.getMediaList().get(0).getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) {
                viewHolder.mPlayImageView.setVisibility(View.GONE);
            }
//            if(mediaSet.getMediaList().size()>0){
            MediaItem item = mediaSet.getMediaList().get(0);
            Log.e("AlbumAdapter" ,"path = "+ item.getLocalPath() );
            if(item.getMediaType() == MediaItem.MEDIA_TYPE_TAKE_IMAGE||item.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE){
                viewHolder.mImageView.setImageResource(R.drawable.and_fetion_icon_192);   // 图片显示的默认图标
            }else if(item.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO||item.getMediaType() == MediaItem.MEDIA_TYPE_TAKE_VIDEO){
                viewHolder.mImageView.setImageResource(R.drawable.and_fetion_icon_192);   // 视频显示的默认图标
            }
            if(new File(item.getLocalPath()).exists()){
                loadImage(viewHolder.mImageView, tag, item);
            }
//            }else{
//                viewHolder.mImageView.setImageResource(R.drawable.and_fetion_icon_192);
//            }
        }
        return convertView;
    }

    /**
     * 描述	：载入Image对象
     * @param imageView
     * @param viewTag 识别码
     * @param path Image文件路径
     */
    private void loadImage(ImageView imageView, final String viewTag, MediaItem path) {
        // 利用NativeImageLoader类加载本地图片 Bitmap bitmap =
        Bitmap bitmap = mImageLoader.getBitmapFromMemCache(path.getLocalPath());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            File f = new File(FileUtil.getThumbnailDir());
            if (!f.exists()) {
                f.mkdirs();
            }
            String pmd5 = MD5Util.getMD5(ThumbnailUtils.getThumbMD5Key(path.getLocalPath()));
            f = new File(f, pmd5);
            path.setThumbPath(f.getAbsolutePath());
        } else {
            mImageLoader.loadNativeImage(mPoint, new NativeImageLoader.NativeImageCallBack() {
                @Override
                public Bitmap onImageLoader(Bitmap[] bitmaps, MediaItem[] paths, String viewTag) {
                    ImageView mImageView = (ImageView) mListView.findViewWithTag(viewTag);
                    if (bitmaps[0] != null && mImageView != null) {
                        mImageView.setImageBitmap(bitmaps[0]);
                    }
                    return bitmaps[0];
                }
            }, viewTag, viewTag , true, path);
        }
    }

    /**
     * 描述	：生成相册封面位图对象
     * @param bitmaps 位图
     * @param num 位图个数
     * @return 相册封面
     */
    protected Bitmap getAlbumCover(Bitmap[] bitmaps, int num) {
        if (bitmaps.length > 0 && bitmaps[0] != null) {
            if (bitmaps[0] == null) {
                bitmaps[0] = defalut;
            }
            int width = bitmaps[0].getWidth();
            int height = bitmaps[0].getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width + 10, height + 10, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            int l, t;
            l = t = 10;
            Paint paint = new Paint();
            paint.setColor(0xffffffff);
            canvas.drawBitmap(bitmaps[0], l, t, null);
            for (int a = 1; a < num; a++) {
                l -= 5;
                t -= 5;
                Bitmap bt = bitmaps[a % bitmaps.length];
                if (bt == null) {
                    bt = defalut;
                }
                canvas.drawLine(l + bt.getWidth(), t, l + bt.getWidth(), t + bt.getHeight(), paint);
                canvas.drawLine(l, t + bt.getHeight(), l + bt.getWidth(), t + bt.getHeight(), paint);
                canvas.drawBitmap(bt, l, t, null);
            }
            return bitmap;
        }
        return null;
    }

    public static class ViewHolder {
        public ImageView mImageView;
        public ImageView mPlayImageView;
        public TextView mTextViewTitle;
        public ImageView mSelect;
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            loadImageFromFile();
        } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            isScrolling = true;
        }
    }

    /**
     * 描述	：从文件中载入Image对象
     */
    private void loadImageFromFile() {
        synchronized (cache) {
            for (Map.Entry<String, MediaItem[]> entry : cache.entrySet()) {
                String key = getKey(entry.getValue(), COVER_NUMBER);
                Bitmap bitmap = mImageLoader.getBitmapFromMemCache(key.toString());
                if (bitmap != null) {
                    View view = mListView.findViewWithTag(entry.getKey());
                    if (view != null && view instanceof ImageView) {
                        ImageView imageView = (ImageView) view;
                        imageView.setImageBitmap(bitmap);
                    }
                } else {
                    mImageLoader.loadNativeImage(mPoint, new NativeImageLoader.NativeImageCallBack() {
                        public Bitmap onImageLoader(Bitmap[] bitmaps, MediaItem[] paths, String viewTag) {
                            Bitmap bitmap = getAlbumCover(bitmaps, COVER_NUMBER);
                            ImageView mImageView = (ImageView) mListView.findViewWithTag(viewTag);
                            if (bitmap != null && mImageView != null) {
                                mImageView.setImageBitmap(bitmap);
                            }
                            return bitmap;
                        }
                    }, entry.getKey(), key, true, entry.getValue());
                }
            }
            cache.clear();
            isScrolling = false;
        }
    }

    private String getKey(MediaItem[] value, int num) {
        StringBuffer sb = new StringBuffer();
        for (int a = 0; a < num; a++) {
            sb.append(value[a % value.length].getLocalPath());
        }
        return sb.toString();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
    }

    /**
     * 描述	：销毁对象
     */
    public void destroy() {
        mImageLoader.shutDown();
    }

}
