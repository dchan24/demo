package com.cmicc.module_message.ui.adapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.util.EmployeeUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.ui.data.NoRcsUser;
import com.cmicc.module_message.R;
import java.util.ArrayList;

/**
 * Created by LY on 2018/5/22.
 */

public class NoRcsGroupMemberAdapter extends RecyclerView.Adapter<NoRcsGroupMemberAdapter.ViewHolder> {

    private ArrayList<NoRcsUser> datas ;
    private ArrayList<String> phons = new ArrayList<>() ;
    private boolean isCanChoose = false ; // 可不可点击的标志
    private Context mContext ;
    private String groupID ;

    public NoRcsGroupMemberAdapter(Context context  , String groupid){
        mContext = context ;
        groupID = groupid ;
    }

    @Override
    public int getItemCount() {
        return datas != null ? datas.size():0 ;
    }

    @Override
    public NoRcsGroupMemberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itme_norsc_groupmember_layout, parent, false);
        NoRcsGroupMemberAdapter.ViewHolder viewHolder = new NoRcsGroupMemberAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NoRcsGroupMemberAdapter.ViewHolder holder, int position) {
        if(position >= 0 && position < datas.size() && datas.get(position)!=null){
            if(datas.get(position).isBeChosen()){
                holder.choiceImage.setVisibility(View.VISIBLE);
            }else{
                holder.choiceImage.setVisibility(View.GONE);
            }
            bind( holder , datas.get(position).getPhone());
        }
    }

    private void bind( ViewHolder holder , String phone ){
        if(TextUtils.isEmpty(phone) || phone.length()<3){
            return;
        }
        holder.onlyPhone.setVisibility(View.VISIBLE);
        holder.nameTimeLl.setVisibility(View.GONE);
        holder.phoneStateLl.setVisibility(View.GONE);

        // 先拿在群中的昵称
        String name =  GroupChatUtils.getMemberNumber(mContext ,groupID ,  NumberUtils.getDialablePhoneWithCountryCode(phone));
        if(!TextUtils.isEmpty(name)){
            holder.onlyPhone.setText(name);
            GlidePhotoLoader.getInstance(App.getAppContext()).loadPhotoAndHeadTv(App.getAppContext(),
                    holder.head , holder.headText , PinYin.buildPinYin(name), phone ,false);
            holder.head.setVisibility(View.VISIBLE);
            holder.headText.setVisibility(View.GONE);
            return;
        }
        // 企业通讯录拿
        name = EmployeeUtils.getEmployeeName(mContext , NumberUtils.getNumForStore(phone));
        if(!TextUtils.isEmpty(name)){
            holder.onlyPhone.setText(name);
            GlidePhotoLoader.getInstance(App.getAppContext()).loadPhotoAndHeadTv(App.getAppContext(),
                    holder.head , holder.headText , PinYin.buildPinYin(name), phone ,false);
            holder.head.setVisibility(View.VISIBLE);
            holder.headText.setVisibility(View.GONE);
            return;
        }

        SimpleContact simpleContact = ContactsCache.getInstance().searchContactByNumber(phone);
        if(simpleContact == null || TextUtils.isEmpty(simpleContact.getName())){ // 联系人不在本地或联系人的名称为“” ，null;
            holder.onlyPhone.setText(NumberUtils.formatPersonStart(phone));
            int intValue = 0 ;
            try {
                intValue = Integer.valueOf(NumberUtils.getNumForStore(phone).substring(0,3));
                intValue = intValue % 5 ;
            }catch (Exception e){
                intValue = 3 ;
            }
            if(intValue == 0 ){
                holder.headText.setBackgroundResource(R.drawable.custom_tv_ff2a2a2asp);
            }else if(intValue == 1){
                holder.headText.setBackgroundResource(R.drawable.custom_tv_ff14bc6fsp);
            }else if(intValue == 2){
                holder.headText.setBackgroundResource(R.drawable.custom_tv_ff157cf8sp);
            }else if(intValue == 3){
                holder.headText.setBackgroundResource(R.drawable.custom_tv_ff7890c2sp);
            }else if(intValue == 4){
                holder.headText.setBackgroundResource(R.drawable.custom_tv_ffbac8e0sp);
            }
            holder.headText.setText(NumberUtils.formatPersonStart(phone).substring(0,1));
            holder.head.setVisibility(View.GONE);
            holder.headText.setVisibility(View.VISIBLE);
        }else{
            GlidePhotoLoader.getInstance(App.getAppContext()).loadPhotoAndHeadTv(App.getAppContext(),
                    holder.head , holder.headText ,simpleContact.getPinyin(), phone,false );
            holder.onlyPhone.setText(simpleContact.getName());
            holder.head.setVisibility(View.VISIBLE);
            holder.headText.setVisibility(View.GONE);
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        private View rootView ;
        private TextView headText ;
        private ImageView head ;
        private ImageView choiceImage ;
        private TextView onlyPhone ;
        private LinearLayout nameTimeLl ;
        private LinearLayout phoneStateLl ;
        private TextView nameText ;
        private TextView timeText ;
        private TextView phoneText;
        private TextView stateText ;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);
            headText = (TextView) itemView.findViewById(R.id.tv_head);
            head = (ImageView) itemView.findViewById(R.id.iv_head);
            choiceImage = (ImageView) itemView.findViewById(R.id.choice_image);
            onlyPhone = (TextView) itemView.findViewById(R.id.only_phone);
            nameTimeLl = (LinearLayout) itemView.findViewById(R.id.name_time_ll);
            phoneStateLl = (LinearLayout) itemView.findViewById(R.id.phone_state_ll);
            nameText = (TextView) itemView.findViewById(R.id.name);
            timeText = (TextView) itemView.findViewById(R.id.time);
            phoneText = (TextView) itemView.findViewById(R.id.phone);
            stateText = (TextView) itemView.findViewById(R.id.state_text);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isCanChoose || onItemClick == null ){
                        return;
                    }
                    int position = getLayoutPosition();
                    if(position < 0 || position  >= datas.size()){ // 防止越界
                        return;
                    }
                    if(datas.get(position).isBeChosen()){ // 已经处于选择状态 取消选择
                        datas.get(position).setBeChosen(false);
                        phons.remove(datas.get(position).getPhone());
                    }else{ // 没有被选择的状态 选择上
                        datas.get(position).setBeChosen(true);
                        phons.add(datas.get(position).getPhone());
                    }
                    notifyDataSetChanged();
                    onItemClick.onItemClick(datas.get(position).getPhone());
                }
            });
        }
    }

    /**
     * 接口
     */
    public interface OnItemClick{
        void onItemClick(String phone);
    }
    private OnItemClick onItemClick ;
    public void setOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick ;
    }

    /**
     * 设置可点击
     * @param isCanChoose
     */
    public void setIsCanChoose(boolean isCanChoose){
        this.isCanChoose = isCanChoose ;
    }

    /**
     * 全选
     * @return
     */
    public ArrayList<NoRcsUser> getData(){
        return datas ;
    }

    /**
     * 设置数据源
     * @param d
     */
    public void setDatas(ArrayList<NoRcsUser> d ){
        datas = d ;
        notifyDataSetChanged();
    }

    /**
     *
     * @param isChoose true 全选 ， false 取消全选
     */
    public void setState(boolean isChoose){
        if(datas == null || datas.size() == 0 ){
            return;
        }
        phons.clear();
        for(int i = 0 ; i < datas.size() ; i++ ){
            if(isChoose){
                datas.get(i).setBeChosen(true);
                phons.add(datas.get(i).getPhone());
            }else{
                datas.get(i).setBeChosen(false);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 获取选中的手机号码
     * @return
     */
    public ArrayList<String> getSelectPhons(){
        return phons ;
    }

}
