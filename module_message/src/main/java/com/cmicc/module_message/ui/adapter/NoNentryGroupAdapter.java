package com.cmicc.module_message.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.R;
import java.util.ArrayList;

/**
 * Created by LY on 2018/5/22.
 */

public class NoNentryGroupAdapter extends RecyclerView.Adapter<NoNentryGroupAdapter.ViewHolder> {

    private ArrayList<String> datas ;

    public NoNentryGroupAdapter(){

    }

    @Override
    public int getItemCount() {
        return datas != null ? datas.size():0 ;
    }

    @Override
    public NoNentryGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itme_nonentry_group_layout, parent, false);
        NoNentryGroupAdapter.ViewHolder viewHolder = new NoNentryGroupAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NoNentryGroupAdapter.ViewHolder holder, int position) {
        if(position >= 0 && position < datas.size() && datas.get(position)!=null){
            bind( holder , datas.get(position));
        }
    }

    private void bind( ViewHolder holder , String phone ){
        if(TextUtils.isEmpty(phone) || phone.length()<3){
            return;
        }

        SimpleContact simpleContact = ContactsCache.getInstance().searchContactByNumber(phone);
        if(simpleContact == null || TextUtils.isEmpty(simpleContact.getName())){ // 联系人不在本地或联系人的名称为“” ，null
            holder.phoneText.setText(NumberUtils.formatPersonStart(phone)); // 手机号码
            int intValue = 0 ;
            try {
                intValue = Integer.valueOf(phone.substring(0 ,3));
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
            holder.headText.setText(phone.substring(0,2));
            holder.head.setVisibility(View.GONE);
            holder.headText.setVisibility(View.VISIBLE);
        }else{
            GlidePhotoLoader.getInstance(App.getAppContext()).loadPhotoAndHeadTv(App.getAppContext(),
                    holder.head , holder.headText ,simpleContact.getPinyin(), phone,false );
            holder.phoneText.setText(simpleContact.getName()); // 手机号码
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        private View rootView ;
        private TextView headText ;
        private ImageView head ;
        private TextView phoneText;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);
            headText = (TextView) itemView.findViewById(R.id.tv_head);
            head = (ImageView) itemView.findViewById(R.id.iv_head);
            phoneText = (TextView) itemView.findViewById(R.id.phone);
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
     * 设置数据源
     * @param d
     */
    public void setDatas(ArrayList<String> d ){
        datas = d ;
        notifyDataSetChanged();
    }


}
