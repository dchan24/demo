package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.model.GroupMassModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by yangshaowei on 2017/4/3.
 */

public class GroupSMSListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<GroupMassModel> mList;
    private OnItemClickListener mOnItemClickListener;
    private String sendTo;

    public GroupSMSListAdapter(Context context, ArrayList<GroupMassModel> list) {
        mContext = context;
        mList = list;
        sendTo = mContext.getResources().getString(R.string.send_to)+" ";
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_mass, parent, false);
            return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
        final GroupMassModel groupNotify = mList.get(position);
        long date = groupNotify.getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        String time = sdf.format(date);
        String content = groupNotify.getBody();
        contentViewHolder.tv_time.setText(time);
        contentViewHolder.tv_content.setText(content);

        if(!TextUtils.isEmpty(groupNotify.getSendAddress())){
            String names = sendTo + groupNotify.getSendAddress();
            DynamicLayout dynamicLayout = new DynamicLayout(names, contentViewHolder.sendeeNameText.getPaint(),
                    (int) AndroidUtil.dip2px(mContext, 296), Layout.Alignment.ALIGN_NORMAL, 1.2F, 0.0F, true);
            int lineCount = dynamicLayout.getLineCount();
            if (lineCount > 3) {
                contentViewHolder.sendeeNameText.setMaxLines(3);
                contentViewHolder.expandIcon.setVisibility(View.VISIBLE);
            } else {
                contentViewHolder.marginView.setVisibility(View.VISIBLE);
                contentViewHolder.expandIcon.setVisibility(View.GONE);
            }
            contentViewHolder.sendeeNameText.setText(names);
            contentViewHolder.sendeeNameText.setTag(position);
//            contentViewHolder.sendeeNameText.post(new Runnable() {
//                @Override
//                public void run() {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Layout l = contentViewHolder.sendeeNameText.getLayout();
//                            if (l != null) {
//                                int lines = l.getLineCount();
//                                if (lines > 0 && l.getEllipsisCount(lines - 1) > 0 && position == (int)contentViewHolder.sendeeNameText.getTag() ) {
//                                    String showName = l.getText().toString();
//                                    showName = showName.substring( 0 , ( showName.lastIndexOf("、")- 5)); // 减去 等500人 。（有问题，名字中有字母的时候 ，暂时这样）
//                                    showName = showName.substring(0 ,showName.lastIndexOf("、"));
//                                    String[] names = contentViewHolder.sendeeNameText.getText().toString().split("、");
//                                    final String name = showName + String.format(mContext.getResources().getString(R.string.group_sms_number) ,names!=null?names.length : 0) ;
//                                    if( mContext instanceof Activity){
//                                        ((Activity)mContext).runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                if( position == (int)contentViewHolder.sendeeNameText.getTag()){
//                                                    contentViewHolder.sendeeNameText.setText(name);
//                                                }
//                                            }
//                                        });
//                                    }
//                                }
//                            }
//                        }
//                    }).start();
//                }
//            });
        }else{
            contentViewHolder.sendeeNameText.setText(sendTo);
        }

        contentViewHolder.expandLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String names = sendTo + groupNotify.getSendAddress();
                if (!groupNotify.isExpand()) {
                    groupNotify.setExpand(true);
                    contentViewHolder.sendeeNameText.setMaxLines(Integer.MAX_VALUE);
                    contentViewHolder.expandIcon.setBackgroundResource(R.drawable.ic_collapse_small_holo_light);
                } else {
                    groupNotify.setExpand(false);
                    contentViewHolder.sendeeNameText.setMaxLines(3);
                    contentViewHolder.expandIcon.setBackgroundResource(R.drawable.ic_expand_small_holo_light);
                }
                contentViewHolder.sendeeNameText.setText(names);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size():0 ;
    }

    public GroupMassModel getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return 0 ;
    }

    public void setDataList(ArrayList<GroupMassModel> list){
        mList = list;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        TextView tv_time;
        TextView tv_content;
        TextView sendeeNameText;
        ImageView expandIcon;
        RelativeLayout expandLayout;
        View marginView;

        public ContentViewHolder(View itemView) {
            super(itemView);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_content = itemView.findViewById(R.id.tv_content);
            sendeeNameText = itemView.findViewById(R.id.sendeeNameText);
            expandLayout = itemView.findViewById(R.id.expand_container);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            marginView = itemView.findViewById(R.id.margin_view);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemLongClick(v, getAdapterPosition());
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
}
