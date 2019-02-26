package com.cmicc.module_message.ui.adapter;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.GroupSMSSendeeActivity;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by LY on 2018/6/22.
 */

public class GroupSMSSendeeAdapter extends  RecyclerView.Adapter<GroupSMSSendeeAdapter.ViewHolder>{

    private ArrayList<GroupSMSSendeeActivity.GroupSmsSendee> datas = new ArrayList<>();
    private ArrayList<GroupSMSSendeeActivity.GroupSmsSendee> selectionDatas = new ArrayList<>();
    private Context mContext ;
    public boolean isSearchStats ;
    public String searKey ;

    protected static final int CONTACT_NAME = 0;
    protected static final int CONTACT_NUMBER = 1;
    protected static final int CONTACT_MULTIPLE_NAME = 3;
    protected static final int CONTACT_ORTHER = 4;

    private int MAXNUMBEROFPEOPLE = 200 ; // 可以选择的最大人数

    public GroupSMSSendeeAdapter(Context context){
        mContext = context;
    }

    /**
     * 设置数据源
     * @param data
     */
    public void setDatas(ArrayList<GroupSMSSendeeActivity.GroupSmsSendee> data){
        this.datas.clear();
        this.datas.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return datas!=null ? datas.size() : 0;
    }

    public GroupSMSSendeeActivity.GroupSmsSendee getItem(int position) {
        if(position >=0 && position < datas.size()){
            return datas.get(position);
        }
        return null ;
    }

    @Override
    public GroupSMSSendeeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itme_group_sms_sendee_list_layout, parent, false);
        GroupSMSSendeeAdapter.ViewHolder viewHolder = new GroupSMSSendeeAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupSMSSendeeActivity.GroupSmsSendee groupMember = getItem(position);
        if(groupMember != null ){
            if(position == 0 ){
                displayAlphabetIndex( holder , true , groupMember , null  );
            }else if(position >0 ){
                displayAlphabetIndex( holder ,false , groupMember ,  getItem(position -1) );
            }
            bindName(holder,groupMember.getGroupMember());
            bindHead(holder,groupMember);
            displayHeightLight(holder ,  groupMember  );
        }
    }

    /**
     * 绑定头像
     * @param holder
     * @param groupSmsSendee
     */
    private void bindHead(ViewHolder holder , GroupSMSSendeeActivity.GroupSmsSendee groupSmsSendee){
        if(groupSmsSendee.isChoice()){
            holder.select_icon.setVisibility(View.VISIBLE);
        }else{
            holder.select_icon.setVisibility(View.GONE);
        }
        GlidePhotoLoader.getInstance(mContext).loadPhoto(
                mContext, holder.contact_icon, groupSmsSendee.getGroupMember().getAddress());
    }

    /**
     * 绑定群成员昵称
     * @param holder
     * @param groupMember
     */
    private void bindName(ViewHolder holder ,GroupMember groupMember ){
        if(true){// !isSearchStats
            holder.sear_ll.setVisibility(View.GONE);
            holder.me_gorup_name.setVisibility(View.VISIBLE);
            if(TextUtils.isEmpty(groupMember.getPerson())){
                String phone = groupMember.getAddress();
                holder.me_gorup_name.setText(NumberUtils.toHideAsStar(NumberUtils.getNumForStore(phone)));
            }else{
                holder.me_gorup_name.setText(groupMember.getPerson());
            }
        }else{
            holder.me_gorup_name.setVisibility(View.GONE);
            holder.sear_ll.setVisibility(View.VISIBLE);
            holder.me_name_text.setText(groupMember.getPerson());
            holder.me_phone_text.setText(groupMember.getAddress());
        }
    }

    /**
     * 绑定字母索引
     * @param holder
     * @param isFirstPosition
     * @param groupSmsSendee
     * @param groupMemberOld
     */
    private void displayAlphabetIndex( ViewHolder holder , boolean isFirstPosition , GroupSMSSendeeActivity.GroupSmsSendee groupSmsSendee ,  GroupSMSSendeeActivity.GroupSmsSendee groupMemberOld  ) {
        if (isFirstPosition) { //第一个
            holder.alphabetIndexText.setVisibility(View.VISIBLE);
            PinYin pinYin = groupSmsSendee.getPinyin();
            if(pinYin != null && pinYin.getIndexKey() != null){
                holder.alphabetIndexText.setText(pinYin.getIndexKey().toUpperCase());
            }else{
                holder.alphabetIndexText.setVisibility(View.INVISIBLE);
            }
        } else {
            PinYin pinYin = groupSmsSendee.getPinyin();    // 当前
            PinYin pinYinT = groupMemberOld.getPinyin();   // 前一个
            if(pinYin == null || pinYinT == null || TextUtils.isEmpty(pinYin.getIndexKey()) || TextUtils.isEmpty(pinYinT.getIndexKey())){
                holder.alphabetIndexText.setVisibility(View.INVISIBLE);
            }else{
                String preIndex = pinYin.getIndexKey().toUpperCase();
                String currIndex = pinYinT.getIndexKey().toUpperCase();
                if (currIndex.equals(preIndex)) {
                    holder.alphabetIndexText.setVisibility(View.INVISIBLE);
                } else {
                    holder.alphabetIndexText.setVisibility(View.VISIBLE);
                    holder.alphabetIndexText.setText(preIndex);
                }
            }
        }
    }

    protected void displayHeightLight(ViewHolder holder ,  GroupSMSSendeeActivity.GroupSmsSendee groupSmsSendee  ) {
        if (!isSearchStats) {
            return;
        }
        String keyWord = searKey ;
        if (groupSmsSendee.getSearchType() == GroupSMSSendeeActivity.GroupSmsSendee.SEARCH_TYPE_PINYIN) {
            String name = holder.me_gorup_name.getText().toString();
            GroupSMSSendeeActivity.GroupSmsSendee c = groupSmsSendee;
            SpannableString sp = new SpannableString(name);
            for (int i = 0; i < c.getWeightLight().size(); i++) {
                int value = c.getWeightLight().get(i);
                if ((value + 1) > sp.length())
                    return;
                int spanCount = 0;
                int charCount = value + 1;
                int zm = 0;
                for (int j = 0; j < name.length(); j++) {
                    if (name.charAt(j) != ' ') {
                        zm++;
                        if (zm == charCount) {
                            break;
                        }
                    } else {
                        spanCount++;
                    }
                }
                try {
                    sp.setSpan(
                            new BackgroundColorSpan(mContext.getResources().getColor(R.color.color_fcf5aa)),
                            spanCount + value, spanCount + value + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            holder.me_gorup_name.setText(sp);
        } else {
            LightResult result = findHighLightResult(keyWord ,
                    groupSmsSendee.getGroupMember().getAddress(), groupSmsSendee);
            if (result.index != -1) {
                lightKeyWords(holder ,groupSmsSendee ,  result);
            }
        }
    }

    private void lightKeyWords( ViewHolder holder ,  GroupSMSSendeeActivity.GroupSmsSendee groupSmsSendee , LightResult result) {
        int start = result.start;
        int end = result.end;
        int type = result.type;
        int color = mContext.getResources().getColor(R.color.color_fcf5aa);
        String name = holder.me_gorup_name.getText().toString();
        if (type == CONTACT_NAME) {
            // String temp = name.replace(" ", "");
            String temp = name;
            temp = temp.substring(start, end < temp.length() ? end : temp.length());
            int index = start;
            int lastIndex = -1;
            int istart = 0;
            int iend = 0;
            for (int i = 0; i < temp.length(); i++) {
                index = name.indexOf(temp.charAt(i), index);
                if (index == lastIndex) {
                    if (index + 1 < name.length()) {
                        index = name.indexOf(temp.charAt(i), index + 1);
                    }
                }
                if (i == 0) {
                    istart = index;
                }
                if (i == temp.length() - 1) {
                    iend = index + 1;
                }
                lastIndex = index;
            }
            SpannableString spannableString = new SpannableString(name);
            try {
                spannableString.setSpan(new BackgroundColorSpan(color), istart, iend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.me_gorup_name.setText(spannableString);
        } else if (type == CONTACT_NUMBER || type == CONTACT_ORTHER) {
            if (groupSmsSendee.getAddressCount() >= 1) {
                SpannableString spannableString = new SpannableString(holder.me_phone_text.getText().toString());
                try {
                    spannableString.setSpan(new BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.me_phone_text.setText(spannableString);
            }
        }
    }

    class LightResult {
        protected int index = -1;
        protected int end = -1;
        protected int start = -1;
        protected int type = -1;
        protected Reference<GroupSMSSendeeActivity.GroupSmsSendee> simpleContact;

        public void setSimpleContact(Reference<GroupSMSSendeeActivity.GroupSmsSendee> simpleContact) {
            this.simpleContact = simpleContact;
        }

        public Reference<GroupSMSSendeeActivity.GroupSmsSendee> getSimpleContact() {
            return simpleContact;
        }

        public void clear() {
            simpleContact.clear();
            this.simpleContact = null;
        }
    }

    protected LightResult findHighLightResult(String words, String number , GroupSMSSendeeActivity.GroupSmsSendee groupSmsSendee ) {
        LightResult result = new LightResult();
        if (!TextUtils.isEmpty(words)) {
            int index = 0 ;
//            if (groupSmsSendee instanceof AdvancedSearchContact) {  // 其他搜索不要 公司 工作
//                AdvancedSearchContact contact = (AdvancedSearchContact) groupSmsSendee;
//                // TODO 3
//                if (!TextUtils.isEmpty(contact.getNote())) {
//                    index = contact.getNote().indexOf(words);
//
//                    if (index != -1) {
//
//                        result = new LightResult();
//                        result.end = index + words.length();
//                        result.start = index;
//                        result.index = index;
//                        result.type = CONTACT_ORTHER;
//                        return result;
//                    }
//                }
//
//                // TODO 4
//                if (!TextUtils.isEmpty(contact.getCompanyJob())) {
//                    index = contact.getCompanyJob().indexOf(words);
//                    if (index != -1) {
//                        result = new LightResult();
//                        result.end = index + words.length();
//                        result.start = index;
//                        result.index = index;
//                        result.type = CONTACT_ORTHER;
//                        return result;
//                    }
//                }
//
//                // TODO 5
//                if (!TextUtils.isEmpty(contact.getNickName())) {
//                    index = contact.getNickName().indexOf(words);
//                    if (index != -1) {
//                        result = new LightResult();
//                        result.end = index + words.length();
//                        result.start = index;
//                        result.index = index;
//                        result.type = CONTACT_ORTHER;
//                        return result;
//                    }
//                }
//            }

            // TODO 1  名字搜索
            index = groupSmsSendee.getName().indexOf(words);
            if (index != -1) {
                result = new LightResult();
                result.end = index + words.length();
                result.start = index;
                result.index = index;
                result.type = CONTACT_NAME;
                return result;
            }
            // TODO 2  手机号码搜索
//            if (groupSmsSendee.getAddressCount() != 0) {
//                index = number.indexOf(words);
//                if (index != -1) {
//                    result = new LightResult();
//                    result.end = index + words.length();
//                    result.start = index;
//                    result.index = index;
//                    result.type = CONTACT_NUMBER;
//                    return result;
//                }
//            }
        }
        return result;
    }


    /**
     * 接口
     */
    public interface OnItemClick{
        void onItemClick(int count);
        void maximumNumber(int maximumNumber);
    }
    private OnItemClick onItemClick ;
    public void setOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick ;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private View root_view ;
        private TextView alphabetIndexText ;
        private ImageView contact_icon ;
        private ImageView select_icon ;
        private TextView me_gorup_name;

        private LinearLayout sear_ll ;
        private TextView me_name_text ;
        private TextView me_phone_text ;

        public ViewHolder(View itemView) {
            super(itemView);
            root_view = itemView.findViewById(R.id.root_view);
            alphabetIndexText = (TextView) itemView.findViewById(R.id.alphabetIndexText);
            contact_icon = (ImageView) itemView.findViewById(R.id.contact_icon);
            select_icon = (ImageView) itemView.findViewById(R.id.select_icon);
            me_gorup_name = (TextView) itemView.findViewById(R.id.me_gorup_name);

            sear_ll = (LinearLayout) itemView.findViewById(R.id.sear_ll);
            me_name_text = (TextView) itemView.findViewById(R.id.me_name_text);
            me_phone_text = (TextView) itemView.findViewById(R.id.me_phone_text);

            root_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClick == null ){
                        LogF.d( "GroupSMSSendeeAdapter" , "监听器为空");
                        return;
                    }
                    int position = getLayoutPosition();
                    if(position < 0 || position>= getItemCount()){
                        LogF.d( "GroupSMSSendeeAdapter" , "下标越界");
                        return;
                    }

                    GroupSMSSendeeActivity.GroupSmsSendee groupSmsSendee = getItem(position);

                    if(!groupSmsSendee.isChoice() && selectionDatas.size() >= MAXNUMBEROFPEOPLE  ){ // 点击的是不是已选中的状态，已经达到最大的可选人数
                        onItemClick.maximumNumber(MAXNUMBEROFPEOPLE);
                        return;
                    }
                    if(groupSmsSendee.isChoice()){
                        groupSmsSendee.setChoice(false); // 取消选择
                        selectionDatas.remove(groupSmsSendee);
                    }else{
                        groupSmsSendee.setChoice(true); // 选择
                        selectionDatas.add(groupSmsSendee);
                    }
                    notifyDataSetChanged();
                    onItemClick.onItemClick(selectionDatas.size());
                }
            });
        }
    }

    /**
     * 获取选中的人
     * @return
     */
    public ArrayList<GroupSMSSendeeActivity.GroupSmsSendee> getSelectionDatas(){
        return this.selectionDatas ;
    }

    /**
     * 一开始就被选中的人的集合
     * @param arrayList
     */
    public void setSelectionDatas(ArrayList<GroupSMSSendeeActivity.GroupSmsSendee> arrayList){
        this.selectionDatas.clear(); // 清楚原来的数据
        this.selectionDatas.addAll(arrayList);
        if(onItemClick!=null){
            onItemClick.onItemClick(selectionDatas.size());
        }
    }

    public boolean isSearchStats() {
        return isSearchStats;
    }

    public void setSearchStats(boolean searchStats , String key) {
        isSearchStats = searchStats;
        searKey = key ;
    }

    public int getMAXNUMBEROFPEOPLE() {
        return MAXNUMBEROFPEOPLE;
    }

    public void setMAXNUMBEROFPEOPLE(int MAXNUMBEROFPEOPLE) {
        this.MAXNUMBEROFPEOPLE = MAXNUMBEROFPEOPLE;
    }

    public void moverSelectSendee(final String phone ){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Iterator<GroupSMSSendeeActivity.GroupSmsSendee> iterator =  selectionDatas.iterator();
                while (iterator.hasNext()){
                    GroupSMSSendeeActivity.GroupSmsSendee groupSmsSendee = iterator.next();
                    if(groupSmsSendee.getNumber().contains(phone)){
                        iterator.remove();
                        if(mContext instanceof Activity)
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(onItemClick!=null){
                                        onItemClick.onItemClick(selectionDatas.size());
                                    }
                                }
                            });
                        break;
                    }
                }
            }
        }).start();
    }
}
