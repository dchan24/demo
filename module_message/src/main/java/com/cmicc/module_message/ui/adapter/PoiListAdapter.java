package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.data.PoiListBeanData;

import java.util.ArrayList;
import java.util.List;

public class PoiListAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder> implements View.OnClickListener {
    private ArrayList<PoiListBeanData> mList;
    private Context mContext;
    private PoiItemClickListener mPoiItemclickListener;
    private int mSelectPosition;
    private static final int NONE_SELECT_POSITION = 0;
    private boolean mAllPoiLoaded;
    public PoiListAdapter(Context context){
        mContext = context;
    }
    public PoiListAdapter(Context context,ArrayList<PoiListBeanData> list){
        mContext = context;
        mList = list;
        PoiListBeanData footerDara = new PoiListBeanData();
    }
    public void setDataList(ArrayList<PoiListBeanData> list){
        mList = list;
        mSelectPosition = NONE_SELECT_POSITION;
    }

    public void addDataList(ArrayList<PoiListBeanData> list){
        mList.addAll(list);
    }

    public void onAllPoiLoaded(){
        mAllPoiLoaded = true;
    }

    public void setItemClickListener(PoiItemClickListener itemClickListener ){
        mPoiItemclickListener = itemClickListener;
    }
    public void setSelectPosition(int selectPosition){
        mSelectPosition = selectPosition;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == PoiListBeanData.TYPE_NORMAL_POI_DATA) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view, mPoiItemclickListener);
            return viewHolder;
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_list_loading_view, parent, false);
            FooterViewHolder viewHolder = new FooterViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public int getItemViewType(int position){
        if(position == mList.size() )
            return PoiListBeanData.TYPE_FOOTER_VIEW_DATA;
        return PoiListBeanData.TYPE_NORMAL_POI_DATA;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position >= mList.size())
            return;
        PoiListBeanData beanData =  mList.get(position);
        if(holder instanceof  ViewHolder){
            ViewHolder viewHolder = (ViewHolder)holder;
            viewHolder.mAdressDetail.setText(beanData.mDetailAddress);
            viewHolder.mAdressTitle.setText(beanData.mTitleAddress);
            if(position == mSelectPosition){
                viewHolder.mSelectPoiIcon.setVisibility(View.VISIBLE);
            }else{
                viewHolder.mSelectPoiIcon.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        if(mList == null )
            return 0;
        if(mAllPoiLoaded == true)
            return mList.size();
        return mList.size()+1;
    }

    @Override
    public void onClick(View v) {

    }

    public PoiListBeanData getItemData(int position){
        if(mList == null || mList.size()<position || mList.size()<=0)
            return null;
        return mList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View mRootView;
        public TextView mAdressTitle;
        public TextView mAdressDetail;
        public ImageView mSelectPoiIcon;
        public PoiItemClickListener mItemClickListener;
        public ViewHolder(View itemView, PoiItemClickListener listener) {
            super(itemView);
            mRootView = itemView.findViewById(R.id.poi_list_item_root_view);
            mAdressTitle = (TextView)itemView.findViewById(R.id.poi_list_item_title);
            mAdressDetail = (TextView)itemView.findViewById(R.id.poi_list_item_detail);
            mSelectPoiIcon = (ImageView)itemView.findViewById(R.id.poi_list_item_select);
            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //会话数量改变的时候点击可能会崩溃？？？
                    if (getAdapterPosition() == -1) {
                        return;
                    }
                    int position = getAdapterPosition();
                    if(mItemClickListener!=null)
                        mItemClickListener.onItemClick(position);
                }
            });
            mItemClickListener = listener;
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder{

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }


    public static interface PoiItemClickListener{
        public void onItemClick(int position);
    }
}
