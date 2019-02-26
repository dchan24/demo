package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmicc.module_message.R;

/**
 * Created by situ on 2017/3/30.
 */

public class MessageSearchAdapter extends BaseCustomCursorAdapter<MessageSearchAdapter.ViewHolder, Message> {

    private Context mContext;

    private String mKeyWord;
    private int mBoxType;

    public MessageSearchAdapter(Context context) {
        super(Message.class);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_search, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Message message = getItem(position);
        bindImage(holder, message);
        bindContent(holder, message);
        bindDate(holder, message);
        bindName(holder, message);
    }

    public void bindImage(ViewHolder holder, Message message) {
        if (mBoxType == Type.TYPE_BOX_GROUP) {
            GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext, holder.mImgHead, message.getSendAddress());
        } else if(mBoxType == Type.TYPE_BOX_PC){
            int type = message.getType();
            if(!((type & Type.TYPE_RECV) > 0)){
                GlidePhotoLoader.getInstance(mContext).loadPcMsgPhoto( holder.mImgHead, message.getSendAddress(), false);
                return;
            }
            GlidePhotoLoader.getInstance(mContext).loadPcMsgPhoto(holder.mImgHead, message.getAddress(), true);
        } else {
            GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext, holder.mImgHead, message.getSendAddress());
        }

    }

    public void bindName(ViewHolder holder, Message message) {
        if (mBoxType == Type.TYPE_BOX_GROUP) {
            int type = message.getType();
            if (!((type & Type.TYPE_RECV) > 0)) {
                holder.mTvSenderName.setText( mContext.getResources().getString(R.string.home_me));
                return;
            }
            String nickName = NickNameUtils.getNickName(mContext, message.getSendAddress(), message.getAddress());
            holder.mTvSenderName.setText(nickName.trim());
            holder.mTvSenderName.setText(toColor(nickName.trim()));
        } else if(mBoxType == Type.TYPE_BOX_PC){
            int type = message.getType();
            if(!((type & Type.TYPE_RECV) > 0)){
                holder.mTvSenderName.setText( mContext.getResources().getString(R.string.my_phone));
                return;
            }
            holder.mTvSenderName.setText(mContext.getString(R.string.my_computer));
        } else {
            String address = message.getSendAddress();
            String name = address;
            int type = message.getType();
            String nickName = NickNameUtils.getNickName(address);
            if (!((type & Type.TYPE_RECV) > 0)) {
                name = mContext.getResources().getString(R.string.home_me);
            }else if (!TextUtils.isEmpty(nickName)) {
                name = nickName;
            }
//            holder.mTvSenderName.setText(name);
            holder.mTvSenderName.setText(toColor(name));
        }

    }


    public SpannableString toColor(String fromText) {

        String body = fromText;
        String replaceAll = body.replaceAll("\n".intern(), " ".intern());
        if (mKeyWord != null && replaceAll.toLowerCase().contains(mKeyWord.toLowerCase())) {
            int length = replaceAll.length();
            int index = replaceAll.toLowerCase().indexOf(mKeyWord.toLowerCase());
            int len = mKeyWord.length();
            try {
                if (index > 8) {
                    replaceAll = "..." + replaceAll.substring(index - 8 + len / 2);
                } else if ((len + index > 16) && index < 8) {
                    if (len > 16) {
                        replaceAll = "..." + replaceAll.substring(index);
                    } else {
                        int abs = Math.abs(len - 16);
                        if (index - abs / 2 > 0) {
                            replaceAll = "..." + replaceAll.substring(index - abs / 2);
                        } else {
                            replaceAll = "..." + replaceAll.substring(index);
                        }
                    }
                }
                index = replaceAll.toLowerCase().indexOf(mKeyWord.toLowerCase());
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

            SpannableString temp = new SpannableString(replaceAll);
            temp.setSpan(new BackgroundColorSpan(mContext.getResources().getColor(R.color.color_fcf5aa)), index, index + mKeyWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return temp;

        }

        return new SpannableString(fromText);

    }





    public void bindDate(ViewHolder holder, Message message) {
        // 获取数据库里的时间
//        long msgTime = message.getDate();
//        holder.mTvDate.setText(TimeUtil.formatTime(message.getDate()));
        holder.mTvDate.setText(TimeUtil.timeShow(message.getDate()));
    }

    public void bindContent(ViewHolder holder, Message message) {

        String content = "";

        if (mBoxType != Type.TYPE_BOX_GROUP) {
            int type = message.getType();
            boolean recv = (type & Type.TYPE_RECV) > 0;
            if (!recv) {
                content = mContext.getResources().getString(R.string.from_me);
            }
        }





        String body = message.getBody();
        String replaceAll = body.replaceAll("\n".intern(), " ".intern());
        if (mKeyWord != null && replaceAll.toLowerCase().contains(mKeyWord.toLowerCase())) {
            int length = replaceAll.length();
            int index = replaceAll.toLowerCase().indexOf(mKeyWord.toLowerCase());
            int len = mKeyWord.length();
            try {
                if (index > 8) {
                    replaceAll = "..." + replaceAll.substring(index - 8 + len / 2);
                } else if ((len + index > 16) && index < 8) {
                    if (len > 16) {
                        replaceAll = "..." + replaceAll.substring(index);
                    } else {
                        int abs = Math.abs(len - 16);
                        if (index - abs / 2 > 0) {
                            replaceAll = "..." + replaceAll.substring(index - abs / 2);
                        } else {
                            replaceAll = "..." + replaceAll.substring(index);
                        }
                    }
                }
                index = replaceAll.toLowerCase().indexOf(mKeyWord.toLowerCase());
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

//            Spanned temp = Html.fromHtml(content + replaceAll.substring(0, index) + "<font color=#fc464a>" + replaceAll.substring(index, index + len) + "</font>"
//                    + replaceAll.substring(index + len, replaceAll.length()));


//            Spanned temp = Html.fromHtml(content + replaceAll.substring(0, index) + "<span style='background-color:#fcf5aa'>" + replaceAll.substring(index, index + len) + "</span>"
//                    + replaceAll.substring(index + len, replaceAll.length()));

            SpannableString temp = new SpannableString(replaceAll);
            temp.setSpan(new BackgroundColorSpan(mContext.getResources().getColor(R.color.color_fcf5aa)), index, index + mKeyWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            holder.mTvContent.setText(temp);
        } else {
            holder.mTvContent.setText(content + body);
        }


    }

    @Override
    public void onDataSetChanged() {

    }

    public void setKeyWord(String keyWord) {
        mKeyWord = keyWord;
    }

    public void setBoxType(int boxType) {
        mBoxType = boxType;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mImgHead;

        TextView mTvDate;

        TextView mTvSenderName;

        TextView mTvContent;

        public ViewHolder(View itemView) {
            super(itemView);
            mImgHead =  (ImageView)itemView.findViewById(R.id.svd_head);
            mTvDate =  (TextView)itemView.findViewById(R.id.tv_date);
            mTvSenderName =  (TextView)itemView.findViewById(R.id.tv_conv_name);
            mTvContent =  (TextView)itemView.findViewById(R.id.tv_content);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mOnRecyclerViewItemClickListener != null) {
                mOnRecyclerViewItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

}
