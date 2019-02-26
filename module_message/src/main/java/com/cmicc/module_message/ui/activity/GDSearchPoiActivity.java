package com.cmicc.module_message.ui.activity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.adapter.PoiListAdapter;
import com.cmicc.module_message.ui.adapter.PoiSearchListAdapter;
import com.cmicc.module_message.ui.data.PoiListBeanData;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;
import java.util.List;

public class GDSearchPoiActivity extends BaseActivity implements PoiSearchListAdapter.PoiItemClickListener,Inputtips.InputtipsListener,PoiSearch.OnPoiSearchListener {

    private final String TAG = "GDSearchPoiActivity";
    private EditText mSearchEt;
    private RecyclerView mPoiListView;
    private PoiSearchListAdapter mPoiListAdapter;
    private ImageView mClearBtn;
    private String mLocationPosCityName;
    private String mKeyWord;
    private boolean mAllPoiLoaded;
    private boolean isSlidingToLast;
    private View mBackBtn;
    private TextView mNotSearchResultTopsTv;
    private final int ONE_PAGE_SIZE = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gd_search_poi);
    }
    @Override
    protected void findViews() {
        mSearchEt = (EditText)findViewById(R.id.edit_query01);
        mPoiListView = (RecyclerView)findViewById(R.id.location_poi_list);
        mNotSearchResultTopsTv = (TextView)findViewById(R.id.not_search_result);
        mClearBtn  = (ImageView) findViewById(R.id.iv_delect01);
        mBackBtn = findViewById(R.id.iv_back01);
        mClearBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);

        mPoiListAdapter = new PoiSearchListAdapter(this);
        mPoiListAdapter.setItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mPoiListView.setLayoutManager(linearLayoutManager);
        mPoiListView.setAdapter(mPoiListAdapter);

        mPoiListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(mAllPoiLoaded == true)
                    return;
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();
                    //判断是否滚动到底部
                    if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
//                        mPresenter.loadMorePoiData();
                        doSerachPoi(mKeyWord);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(mAllPoiLoaded == true)
                    return;
                //dx用来判断横向滑动方向，dy用来判断纵向滑动方向
                if (dy > 0) {
                    //大于0表示向下滚动
                    isSlidingToLast = true;
                } else {
                    //小于等于0表示停止或向上滚动
                    isSlidingToLast = false;
                }
            }
        });

        mSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String keyWord =charSequence.toString();
                if(!TextUtils.isEmpty(keyWord)) {
                    mClearBtn.setVisibility(View.VISIBLE);
                }else{
                    mClearBtn.setVisibility(View.GONE);
                }
                //search(keyWord);
                mKeyWord = keyWord;
                mCurrentPage = 0;
                doSerachPoi(mKeyWord);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        initData();
    }

    private int mCurrentPage = 0;
    private void doSerachPoi(String keyWord){
        if(!AndroidUtil.isNetworkAvailable(this)){
            ArrayList<PoiListBeanData> listBeanData = new ArrayList<PoiListBeanData>();
            mNotSearchResultTopsTv.setVisibility(View.VISIBLE);
            mPoiListView.setVisibility(View.GONE);
            mPoiListAdapter.setDataList(listBeanData);
            mPoiListAdapter.onAllPoiLoaded();
            mPoiListAdapter.notifyDataSetChanged();
            Toast.makeText(this,getString(R.string.net_connect_error),Toast.LENGTH_SHORT).show();
            return;
        }
        PoiSearch.Query query = new PoiSearch.Query(keyWord,"",mLocationPosCityName);
        query.setPageSize(ONE_PAGE_SIZE);
        query.setPageNum(mCurrentPage);
        PoiSearch poiSearch = new PoiSearch(this,query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
        mCurrentPage++;
    }

    private void initData(){
        mLocationPosCityName = getIntent().getStringExtra(MessageModuleConst.Location.LOCATION_POSITION_CITY_NAME);
    }

//    private void search(String keyWord){
//        InputtipsQuery inputquery = new InputtipsQuery(keyWord, mLocationPosCityName);
//        Inputtips inputTips = new Inputtips(mContext, inputquery);
//        inputTips.setInputtipsListener(this);
//        inputTips.requestInputtipsAsyn();
//    }

    @Override
    protected void init() {

    }

    @Override
    public void onClick(View v){

        int id = v.getId();
        if(id == R.id.iv_delect01){
            mSearchEt.setText("");
            mClearBtn.setVisibility(View.GONE);
        }else if (id == R.id.iv_back01){
            finish();
        }

    }

    @Override
    public void onItemClick(PoiListBeanData beanData) {
        Intent intent = new Intent();
        intent.putExtra(MessageModuleConst.INTENT_KEY_FOR_LATITUDE,beanData.mLatitude);
        intent.putExtra(MessageModuleConst.INTENT_KEY_FOR_LONGITUDE,beanData.mLongtitude);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onGetInputtips(List<Tip> list, int resultCode) {
//        if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
//            if(list!=null && list.size()>0){
//                final ArrayList<PoiListBeanData> listBeanData = new ArrayList<PoiListBeanData>();
//                for(Tip tip:list){
//                    if(tip.getPoiID()!=null && tip.getPoint()!=null){
//                        PoiListBeanData beanData = new PoiListBeanData();
//                        beanData.mTitleAddress = tip.getDistrict();
//                        beanData.mDetailAddress = tip.getAddress();
//                        beanData.mLatitude = tip.getPoint().getLatitude();
//                        beanData.mLongtitude = tip.getPoint().getLongitude();
//                        listBeanData.add(beanData);
//                    }
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mPoiListAdapter.setDataList(listBeanData);
//                        mPoiListAdapter.notifyDataSetChanged();
//                    }
//                });
//
//            }
//        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {
        final ArrayList<PoiListBeanData> listBeanData = new ArrayList<PoiListBeanData>();
        if(rCode == AMapException.CODE_AMAP_SUCCESS && !TextUtils.isEmpty(mKeyWord)){
            List<PoiItem> poiItems = poiResult.getPois();
            List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
            if (null != poiResult && poiResult.getPois().size() > 0) {
                if(poiResult.getPois().size()<ONE_PAGE_SIZE){
                    mAllPoiLoaded = true;
                    mPoiListAdapter.onAllPoiLoaded();
                    LogF.i(TAG,"doSerachMorePoi poi is serchAll ");
                }
                for (int i = 0; i < poiResult.getPois().size(); i++) {
                    PoiItem poiItem = poiResult.getPois().get(i);
                    String titleAddress = poiItem.getTitle();
                    String detailAddress = poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getSnippet() + titleAddress;
                    PoiListBeanData beanData = new PoiListBeanData();

                    beanData.mLatitude = poiItem.getLatLonPoint().getLatitude();
                    beanData.mLongtitude = poiItem.getLatLonPoint().getLongitude();

                    beanData.mDetailAddress = detailAddress;
                    beanData.mTitleAddress = titleAddress;

                    listBeanData.add(beanData);
                }
            }else{
                mAllPoiLoaded = true;
                mPoiListAdapter.onAllPoiLoaded();
                LogF.e(TAG,"doSerachMorePoi poi is serchAll ");
            }

        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(listBeanData.size() == 0){
                    mNotSearchResultTopsTv.setVisibility(View.VISIBLE);
                    mPoiListView.setVisibility(View.GONE);
                }else{
                    mNotSearchResultTopsTv.setVisibility(View.GONE);
                    mPoiListView.setVisibility(View.VISIBLE);
                }

                if(mCurrentPage>1){
                    mPoiListAdapter.addDataList(listBeanData);
                }else {
                    mPoiListAdapter.setDataList(listBeanData);
                }
                mPoiListAdapter.setKeyWord(mKeyWord);
                mPoiListAdapter.notifyDataSetChanged();


            }
        });
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
