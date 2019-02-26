package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.cmcc.cmrcs.android.contact.data.ContactList;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by cq on 2018/5/16.
 */

public class LabelGroupMemberListAdapter extends RecyclerView.Adapter<LabelGroupMemberListAdapter.MemberViewHolder> {
    private final LayoutInflater mFactory;
    private ArrayList<SimpleContact> mGroupList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    public LabelGroupMemberListAdapter(Context context) {
        mFactory = LayoutInflater.from(context);
        this.mContext = context;
    }

    public void changeDataSet(ArrayList<SimpleContact> list) {
        initListData(list);
        notifyDataSetChanged();
    }

    private void initListData(ArrayList<SimpleContact> list) {
        mGroupList.clear();
        mGroupList.addAll(list);
        sort();
    }

    public void sort() {
        Collections.sort(mGroupList, ContactList.mAscComparator);
    }

    public SimpleContact getItem(int position) {
        if (mGroupList != null && position < mGroupList.size()) {
            return mGroupList.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public LabelGroupMemberListAdapter.MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = this.mFactory.inflate(R.layout.label_group_member_list_item,
                parent, false);
        return new LabelGroupMemberListAdapter.MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LabelGroupMemberListAdapter.MemberViewHolder viewHolder, final int position) {
        SimpleContact groupKind = mGroupList.get(position);
        viewHolder.memberName.setText(groupKind.getName());
        viewHolder.memberPhone.setText(groupKind.getNumber());
        GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext ,viewHolder.memberIcon, groupKind.getNumber());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(view, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mGroupList != null) {
            return mGroupList.size();
        }
        return 0;
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView memberIcon;
        private TextView memberName;
        private TextView memberPhone;

        private MemberViewHolder(View itemView) {
            super(itemView);
            memberIcon = itemView.findViewById(R.id.group_member_icon);
            memberName = itemView.findViewById(R.id.group_member_name);
            memberPhone = itemView.findViewById(R.id.group_member_number);
        }
    }
}
