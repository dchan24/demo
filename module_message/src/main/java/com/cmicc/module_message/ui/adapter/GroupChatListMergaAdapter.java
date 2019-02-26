package com.cmicc.module_message.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.ui.LazyRecyclerViewHolder;
import com.cmcc.cmrcs.android.ui.ListRecyclerAdapter;
import com.cmicc.module_message.R;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

/**
 * Created by yangshengfu on 2018/11/23.
 */

public class GroupChatListMergaAdapter extends ListRecyclerAdapter<GroupInfo> {

    @Override
    protected int getLayoutRes() {
        return R.layout.itme_rec_group_chat_list_layout;
    }

    @Override
    protected void onBindViewHolder(LazyRecyclerViewHolder holder, GroupInfo groupInfo, int position) {
        TextView mContactName =  holder.get(R.id.contact_name);
        TextView mAlphabetIndexTextView =  holder.get(R.id.contact_index);
        ImageView mcontactImage =  holder.get(R.id.contact_image);
        ImageView mGroupEP =  holder.get(R.id.group_ep);
        ImageView mGroupParty =  holder.get(R.id.group_party);

        mContactName.setText(groupInfo.getPerson());
        GlidePhotoLoader.getInstance(App.getAppContext()).loadGroupPhoto(App.getAppContext(), mcontactImage, null, groupInfo.getAddress());
        if (groupInfo.getType() == MtcImConstants.EN_MTC_GROUP_TYPE_ENTERPRISE) {
            mGroupEP.setVisibility(View.VISIBLE);
            mGroupParty.setVisibility(View.GONE);
        } else if (groupInfo.getType() == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY) {
            mGroupParty.setVisibility(View.VISIBLE);
            mGroupEP.setVisibility(View.GONE);
        } else {
            mGroupEP.setVisibility(View.GONE);
            mGroupParty.setVisibility(View.GONE);
        }
        displayAlphabetIndex(groupInfo, mAlphabetIndexTextView, position);
    }

    private void displayAlphabetIndex(GroupInfo groupInfo, TextView mAlphabetIndexTextView, int position) {
        int first = 1;
        GroupInfo preGroupInfo = null;
        if (position > 0 && position < getItemCount()) {
            preGroupInfo = getData().get(position - 1);
        }
        PinYin pinYin = PinYin.buildPinYinDuoYinXing(groupInfo.getPerson());

        String indexKey = pinYin.getIndexKey().toUpperCase();

        if (position < first) {
            mAlphabetIndexTextView.setVisibility(View.VISIBLE);
            mAlphabetIndexTextView.setText(indexKey);
        } else {
            String preIndexKey = null;
            if (preGroupInfo != null) {
                PinYin preInfoPinYin = PinYin.buildPinYinDuoYinXing(preGroupInfo.getPerson());
                preIndexKey = preInfoPinYin.getIndexKey().toUpperCase();
            }
            boolean showIndex = !indexKey.equals(preIndexKey);
            // 如果跟上一个的字母索引不同就显示 或者类型不同
            if (showIndex) {
                mAlphabetIndexTextView.setVisibility(View.VISIBLE);
                mAlphabetIndexTextView.setText(indexKey);
            } else {
                mAlphabetIndexTextView.setText("");
            }
        }
    }
}
