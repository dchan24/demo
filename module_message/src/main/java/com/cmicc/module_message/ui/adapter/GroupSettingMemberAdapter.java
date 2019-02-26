package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.cmcc.cmrcs.android.cap.RcsCapQueryUtil;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmicc.module_message.R;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

import java.util.List;

/**
 * @anthor situ
 * @time 2017/5/17 17:40
 * @description 群聊设置界面显示的成员gridview
 */

public class GroupSettingMemberAdapter extends BaseCustomCursorAdapter<GroupSettingMemberAdapter.ViewHolder, GroupMember> {
    private static final String TAG = "GroupSettingMemberAdapter";

    private Context mContext;

    private int mGroupType ;


    public GroupSettingMemberAdapter(Context context) {
        super(GroupMember.class);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_setting_member, parent, false);
        return new GroupSettingMemberAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        GroupMember groupMember = getItem(position);
        if (groupMember.getAddress().equals("+")) {
            holder.mImgHead.setImageResource(R.drawable.group_setting_add_selector);
        } else if (groupMember.getAddress().equals("-")) {
            holder.mImgHead.setImageResource(R.drawable.group_setting_delete_selector);
        } else {
            binderHeard(holder , groupMember.getAddress(),groupMember.getPerson());
            String person = NickNameUtils.getNickName(mContext ,groupMember.getAddress() ,groupMember.getGroupId());
            holder.setupFilters();
            if (NumberUtils.isFormatPerson(person)) {
                person = person.replace("****", "...");
                holder.clearFilter();
            }
            holder.mTvName.setText(person);

            if (groupMember.getType() == Type.TYPE_LEVEL_ORDER) {
                holder.mImgChairmanTag.setVisibility(View.VISIBLE);
            } else {
                holder.mImgChairmanTag.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 绑定头像
     * @param holder
     * @param number
     */
    private void binderHeard(ViewHolder holder , String number , String name ){
        if(mGroupType == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY){
            holder.headTv.setVisibility(View.GONE); // 文字头像隐藏
            RcsCapQueryUtil.getInstance().queryRcsCap(number);
            String cap = RcsCapQueryUtil.getInstance().getRcsCap(number);
            Log.d(TAG , "cap = "+cap +" number : "+number);
            if(TextUtils.equals(cap, "0")){
                holder.mImgHead.setImageResource(R.drawable.cc_chat_personal_default_notopen_notv);
            }else{
                holder.headTv.setTag(com.cmic.module_base.R.id.glide_image_id, NumberUtils.getDialablePhoneWithCountryCode(number));
                if(TextUtils.isEmpty(name)){
                    GlidePhotoLoader.getInstance(mContext).loadProfilePhotoFromNet(mContext, holder.mImgHead, holder.headTv, number, PinYin.buildPinYin(NumberUtils.getNumForStore(number)), true);  //加载头像
                }else{
                    GlidePhotoLoader.getInstance(mContext).loadProfilePhotoFromNet(mContext, holder.mImgHead, holder.headTv, number, PinYin.buildPinYin(name), true);  //加载头像
                }
            }
        }else{
            holder.headTv.setVisibility(View.GONE); // 文字头像隐藏
            holder.headTv.setTag(com.cmic.module_base.R.id.glide_image_id, NumberUtils.getDialablePhoneWithCountryCode(number));
            if(TextUtils.isEmpty(name)){
                GlidePhotoLoader.getInstance(mContext).loadProfilePhotoFromNet(mContext, holder.mImgHead, holder.headTv, number, PinYin.buildPinYin(NumberUtils.getNumForStore(number)), true);  //加载头像
            }else{
                GlidePhotoLoader.getInstance(mContext).loadProfilePhotoFromNet(mContext, holder.mImgHead, holder.headTv, number, PinYin.buildPinYin(name), true);  //加载头像
            }
//            GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext, holder.mImgHead, number);
        }
    }

    public List<GroupMember> getData() {
        return mDataList;
    }

    public void setData(List<GroupMember> data) {
        mDataList.clear();
        mDataList.addAll(data);
    }

    @Override
    public void onDataSetChanged() {

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mImgHead;
        ImageView mImgChairmanTag;
        TextView mTvName;
        TextView headTv ;
        public ViewHolder(View itemView) {
            super(itemView);
            mImgHead = (ImageView) itemView.findViewById(R.id.iv_avatar);
            mImgChairmanTag = (ImageView) itemView.findViewById(R.id.iv_group_chairman_tag);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            headTv = itemView.findViewById(R.id.head_tv);
            itemView.setOnClickListener(this);
            mTvName.setFilters(new InputFilter[]{filter});
        }

        public void clearFilter() {
            mTvName.setFilters(new InputFilter[]{});
        }

        public void setupFilters() {
            mTvName.setFilters(new InputFilter[]{filter});
        }

        @Override
        public void onClick(View v) {
            if (mOnRecyclerViewItemClickListener != null) {
                mOnRecyclerViewItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    private InputFilter filter = new InputFilter() {

        private int maxLen = 8;

        @Override
        public CharSequence filter(CharSequence src, int start, int end, Spanned dest, int dstart, int dend) {
            int count = 0;
            int sindex = 0;
            while (count <= maxLen && sindex < src.length()) {
                char c = src.charAt(sindex++);
                if (c < 128) {
                    count = count + 1;
                } else {
                    count = count + 2;
                }
            }

            if (count <= maxLen) {
                return src;
            } else {
                sindex--;
                return src.subSequence(0, sindex-1) + "...";
            }
        }
    };

    public int getmGroupType() {
        return mGroupType;
    }

    public void setmGroupType(int mGroupType) {
        this.mGroupType = mGroupType;
    }
}
