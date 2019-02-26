package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmicc.module_message.R;


/**
 * Created by Yang on 2018/9/26.
 */

public class ChatRichMediaGirdAdapter extends BaseAdapter {

    private int [][] mData;
    private Context mContext ;
    private boolean isChinaMobileUser;
    private boolean isHongKongUser ;

    public ChatRichMediaGirdAdapter(Context context , int[][] data ,boolean isChinaMobileUser ,boolean isHongKongUser){
        this.mContext = context ;
        this.mData = data;
        this.isChinaMobileUser = isChinaMobileUser ;
        this.isHongKongUser = isHongKongUser ;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.length : 0 ;
    }

    @Override
    public Integer getItem(int position) {
        return mData[position][0];
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.itme_grid_chat_rich_media_layout, parent, false);
            mHolder.iv_img = convertView.findViewById(R.id.iocn_img);
            mHolder.labe_img = convertView.findViewById(R.id.labe_img);
            mHolder.tv_text = convertView.findViewById(R.id.iocn_tv);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        if(mData == null || position < 0 || position >= mData.length){
            return null;
        }

        int [] d = mData[position];
        mHolder.tv_text.setText(d[0]);
        mHolder.iv_img.setImageResource(d[1]);

        if(getItem(position) == R.string.hefeixin_call_free){ // 和飞信
            mHolder.labe_img.setVisibility(View.VISIBLE); //VISIBLE
            mHolder.labe_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cc_chat_input_ic_freeadmission));
        } else if(getItem(position) == R.string.network_call){ // 网络通话
            if(isChinaMobileUser && !isHongKongUser){
                mHolder.labe_img.setVisibility(View.VISIBLE); //VISIBLE
                mHolder.labe_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cc_chat_input_ic_freeflow));
            }else{
                mHolder.labe_img.setVisibility(View.GONE);
            }
        }else if(getItem(position) == R.string.multiparty_call){ // 多方电话
            mHolder.labe_img.setVisibility(View. VISIBLE); //VISIBLE
            mHolder.labe_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cc_chat_input_ic_freeadmission));
        }else if(getItem(position) == R.string.multi_video_call_toolbar_title){ // 多方视屏
            if(isChinaMobileUser && !isHongKongUser){
                mHolder.labe_img.setVisibility(View.VISIBLE); //VISIBLE
                mHolder.labe_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cc_chat_input_ic_freeflow));
            }else{
                mHolder.labe_img.setVisibility(View.GONE);
            }
        }else{
            mHolder.labe_img.setVisibility(View.GONE);
        }
        return convertView;
    }

    private class ViewHolder {
        private ImageView iv_img;
        private ImageView labe_img;
        private TextView tv_text;
    }

}
