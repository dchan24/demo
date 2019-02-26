package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.data.PoiListBeanData;

import java.util.ArrayList;

public class PoiSearchListAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder> implements View.OnClickListener {
    private ArrayList<PoiListBeanData> mList;
    private Context mContext;
    private PoiItemClickListener mPoiItemclickListener;
    private int mSelectPosition;
    private static final int NONE_SELECT_POSITION = 0;
    private boolean mAllPoiLoaded;
    private String[] mKeyWord;
    public PoiSearchListAdapter(Context context){
        mContext = context;
    }
    public PoiSearchListAdapter(Context context,ArrayList<PoiListBeanData> list){
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

    public void setKeyWord(String keyword){
        if(keyword == null || keyword.equals("")){
            return;
        }
        mKeyWord = keyword.split("");
//        mKeyWord = new String[keyword.length()];
//        for(String s: keyword.g){
//
//        }
//        mKeyWord = keyword;
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
            PoiListAdapter.FooterViewHolder viewHolder = new PoiListAdapter.FooterViewHolder(view);
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
        if(holder instanceof ViewHolder){
            ViewHolder viewHolder = (ViewHolder)holder;
            viewHolder.mAdressDetail.setText(beanData.mDetailAddress);
            viewHolder.mAdressTitle.setText(beanData.mTitleAddress);
//            setUpTextView( viewHolder.mAdressDetail,beanData.mDetailAddress,mKeyWord,mContext);
//            setUpTextView( viewHolder.mAdressTitle,beanData.mTitleAddress,mKeyWord,mContext);
//            if(position == mSelectPosition){
//                viewHolder.mSelectPoiIcon.setVisibility(View.VISIBLE);
//            }else{
//                viewHolder.mSelectPoiIcon.setVisibility(View.GONE);
//            }
        }

    }

//
//    public static void setUpTextView(TextView view, final String value, String[] keys, Context context) {
//        if (value == null ||keys == null) {
//            view.setText(value);
//            return;
//        }
//
//        final SpannableStringBuilder builder = new SpannableStringBuilder(value);
//        // 先将所有需要高亮的索引置为1
//        final int[] array = new int[value.length()];
//        for (int i = 0; i < array.length; i++)
//            array[i] = 0;
//
//        String lastKey = "";
//        for (String key : keys) {
//            if (TextUtils.isEmpty(key)) {
//                continue;
//            }
//
//            int s = value.indexOf(key);
//            int e = s + key.length();
//            if (e <= value.length() && s >= 0) {
//                for (int j = s; j < e; j++) {
//                    array[j] = 1;
//                }
//            }
//
//            if(lastKey.equals(key)){
//                String newKey = lastKey+key;
//                int ss = value.indexOf(newKey);
//                int ee = ss + newKey.length();
//                if (ee <= value.length() && ss >= 0) {
//                    for (int j = ss; j < ee; j++) {
//                        array[j] = 1;
//                    }
//                }
//            }
//            lastKey = key;
//        }
//        // 根据上面结果，合并索引
//        int index = 0;
//        while (index < array.length) {
//            if (array[index] == 0) {
//                index++;
//                continue;
//            }
//            final int s = index;
//            while (index < array.length && array[index] == 1) {
//                index++;
//            }
//            int e = index;
////            if(e>value.length())
////                e = value.length();
//            //设置关键字颜色 #fb5b5e
//            final ForegroundColorSpan span = new ForegroundColorSpan(context.getResources().getColor(R.color.color_157cf8));
//            builder.setSpan(span, s, e, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        }
//        view.setText(builder);
//    }

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

    public class ViewHolder extends RecyclerView.ViewHolder{
        public View mRootView;
        public TextView mAdressTitle;
        public TextView mAdressDetail;
//        public ImageView mSelectPoiIcon;
        public PoiItemClickListener mItemClickListener;
        public ViewHolder(View itemView, PoiItemClickListener listener) {
            super(itemView);
            mRootView = itemView.findViewById(R.id.poi_list_item_root_view);
            mAdressTitle = (TextView)itemView.findViewById(R.id.poi_list_item_title);
            mAdressDetail = (TextView)itemView.findViewById(R.id.poi_list_item_detail);
//            mSelectPoiIcon = (ImageView)itemView.findViewById(R.id.poi_list_item_select);
            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //会话数量改变的时候点击可能会崩溃？？？
                    if (getAdapterPosition() == -1) {
                        return;
                    }
                    int position = getAdapterPosition();
                    if(mItemClickListener!=null)
                        mItemClickListener.onItemClick(getItemData(position));
                }
            });
            mItemClickListener = listener;
        }
    }

//    public static class FooterViewHolder extends RecyclerView.ViewHolder{
//
//        public FooterViewHolder(View itemView) {
//            super(itemView);
//        }
//    }
//
//
    public static interface PoiItemClickListener{
        public void onItemClick(PoiListBeanData beanData);
    }
}
