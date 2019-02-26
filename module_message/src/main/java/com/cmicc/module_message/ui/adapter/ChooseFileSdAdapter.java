package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.utils.StringUtil;
import com.cmcc.cmrcs.android.ui.utils.VideoThumbLoaderAsync;
import com.cmicc.module_message.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by tigger on 2017/7/3.
 */

public class ChooseFileSdAdapter extends BaseAdapter implements Comparator<File> {
    ArrayList<File> fileList;
    private LayoutInflater mInflater;
    private Context mContext;
    /** 确保选中一项 */
    private int temp = -1;
    private DisplayImageOptions showOptions = null;
    private VideoThumbLoaderAsync mVideoThumbLoader = null;

    public ChooseFileSdAdapter(Context context, File file) {
        super();
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        setFile(file);
        mVideoThumbLoader = new VideoThumbLoaderAsync();
        showOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.message_files_icon_content_photo)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.message_files_icon_content_unknown)  //设置图片加载/解码过程中错误时候显示的图片
                .showImageOnLoading(R.drawable.message_files_icon_content_unknown) //设置图片在下载期间显示的图片
                .resetViewBeforeLoading(false)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .displayer(new RoundedBitmapDisplayer(0))
                .displayer(new FadeInBitmapDisplayer(0))
                .build();
    }

    public void setFile(File file) {
        temp = -1;
        if (fileList == null) {
            fileList = new ArrayList<File>();
        }
        fileList.clear();
        File[] arrayOfFile = file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
//                boolean isDir = pathname.isDirectory();
//                int dotIndex = pathname.getName().lastIndexOf('.');
//                return isDir || (dotIndex >= 0 && dotIndex < pathname.getName().length() - 1);
                return true;
            }
        });
        if (arrayOfFile != null) {
            for (File f : arrayOfFile) {
                if (f.isHidden())
                    continue;
                if (f.length() <= 0)
                    continue;
                if (f.isDirectory() && f.list() != null && f.list().length <= 0)
                    continue;
                fileList.add(f);
            }
        }
        Collections.sort(fileList, this);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public File getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = this.mInflater.inflate(R.layout.choose_file_list_item, null);
            viewHolder.mFileName = (TextView) convertView.findViewById(R.id.tv_file_name);
            viewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.mCheck = (CheckBox) convertView.findViewById(R.id.cb_choose_icon);
            viewHolder.mFileSize = (TextView)convertView.findViewById(R.id.textview_file_size);
            viewHolder.mFileCreateTime = (TextView)convertView.findViewById(R.id.textview_create_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        File localFile = (File) fileList.get(position);
        String name = localFile.getName();
        if (localFile.isDirectory()) {
//            if (localFile.list() != null) {
//                int num = 0;
//                File[] arrayOfFile = localFile.listFiles(new FileFilter() {
//
//                    @Override
//                    public boolean accept(File pathname) {
//                        boolean isDir = pathname.isDirectory();
//                        int dotIndex = pathname.getName().lastIndexOf('.');
//                        return isDir || (dotIndex >= 0 && dotIndex < pathname.getName().length() - 1);
//                    }
//                });
//                if (arrayOfFile != null) {
//                    for (File f : arrayOfFile) {
//                        if (f.isHidden())
//                            continue;
//                        if (f.length() <= 0)
//                            continue;
//                        if (f.isDirectory() && f.list() != null && f.list().length <= 0)
//                            continue;
//                        num++;
//                    }
//                }

//                viewHolder.mFileName.setText(localFile.getName() + "(" + num + ")");
            viewHolder.mFileName.setText(name);
//            }
            viewHolder.mFileCreateTime.setVisibility(View.GONE);
            viewHolder.mFileSize.setVisibility(View.GONE);
            viewHolder.mCheck.setVisibility(View.GONE);
            viewHolder.mIcon.setImageResource(R.drawable.filelist_icon_local);
        } else {
            viewHolder.mCheck.setVisibility(View.VISIBLE);
            viewHolder.mFileCreateTime.setVisibility(View.VISIBLE);
            viewHolder.mFileSize.setVisibility(View.VISIBLE);
            viewHolder.mCheck.setChecked(false);
            viewHolder.mFileName.setText(name);

            if (name.endsWith(".doc") || name.endsWith(".docx")) {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_doc);
            } else if (name.endsWith(".txt")) {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_txt);
            } else if (name.endsWith(".png") || name.endsWith(".PNG") ||
                    name.endsWith(".jpg") ||  name.endsWith(".JPG") ||
                    name.endsWith(".jpeg") || name.endsWith(".JPEG") ||
                    name.endsWith(".GIF") || name.endsWith(".gif") ||
                    name.endsWith(".bmp") || name.endsWith(".BMP") ) {
                ImageLoader.getInstance().displayImage("file://"+localFile.getPath(),viewHolder.mIcon, showOptions);
            } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".3ga") || name.endsWith(".amr")) {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_mp3);
            } else if(name.endsWith(".mp4") || name.endsWith(".MP4") ||
                    name.endsWith(".3gp") || name.endsWith(".3GP")) {
                viewHolder.mIcon.setTag(localFile.getPath());
                mVideoThumbLoader.showVideoThumbByAsynctask(localFile.getPath(), viewHolder.mIcon, position);
            } else if (name.endsWith(".ppt") || name.endsWith(".ppts") || name.endsWith(".pptx")) {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_ppt);
            } else if (name.endsWith(".pdf")) {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_pdf);
//            } else if (name.endsWith(".zip")) {
//                viewHolder.mIcon.setImageResource(R.drawable.message_files_icon_content_zip);
            } else if (name.endsWith(".rar")) {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_rar);
            } else if (name.endsWith(".zip")) {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_zip);
            }else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_xlsx);
            } else {
                viewHolder.mIcon.setImageResource(R.drawable.chat_localfile_unknown);
            }

            viewHolder.mFileCreateTime.setText(StringUtil.formatFileTime(localFile.lastModified()));
            viewHolder.mFileSize.setText(StringUtil.formetFileSize(localFile.length()));
        }

        if (position == temp) {
            viewHolder.mCheck.setChecked(true);
        } else {
            viewHolder.mCheck.setChecked(false);
        }

        return convertView;
    }

    @Override
    public int compare(File o1, File o2) {
        if (o1.isDirectory() && !o2.isDirectory()) {
            return -1;
        }

        if (!o1.isDirectory() && o2.isDirectory()) {
            return 1;
        }

        return o1.getName().compareToIgnoreCase(o2.getName());
    }

    class ViewHolder {
        TextView mFileName;
        CheckBox mCheck;
        ImageView mIcon;
        TextView mFileSize;
        TextView mFileCreateTime;
    }

    public void select(int position) {
        temp = position;
        notifyDataSetChanged();
    }

    public int getTemp() {
        return temp;
    }

    public File getSelect() {
        if (temp < 0 || temp >= fileList.size()) {
            return null;
        } else {
            return fileList.get(temp);
        }
    }
}
