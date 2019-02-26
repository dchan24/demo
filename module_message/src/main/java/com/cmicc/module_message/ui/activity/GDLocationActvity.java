package com.cmicc.module_message.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.LatLng;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.Threads.HandlerThreadFactory;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.dialogs.PermissionDeniedDialog;
import com.cmcc.cmrcs.android.ui.utils.SettingUtil;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.adapter.PoiListAdapter;
import com.cmicc.module_message.ui.constract.LocationConstract;
import com.cmicc.module_message.ui.data.PoiListBeanData;
import com.cmicc.module_message.ui.fragment.LocationNativDialogFragment;
import com.cmicc.module_message.ui.presenter.LocationPresenter;
import com.cmicc.module_message.ui.view.GDMapView;
import com.cmicc.module_message.utils.LocationUtils;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;

public class GDLocationActvity extends BaseActivity implements LocationConstract.IView,View.OnClickListener,
       PoiListAdapter.PoiItemClickListener{
    private String TAG = "lgh--GDLocationActvity";
    private GDMapView mMapView;
    private AMap mAmap;
    private RecyclerView mPoiListView;
    private PoiListAdapter mPoiListAdapter;


//    private ProgressBar mProgressbar;

    private boolean mIsOnlyShowMap = false;

    private String mLocAddress;
    private String mSpecialAddress;
    private View mOkBtnView;
    private TextView mOkBtnText;
    private TextView mSpecialAddressText;
    private ImageView mShowMapNativBtn;
    private TextView mAddressText;
    private View mBackBtn;
    private View mBackLocationBtn;
    private View mProgressLayout;
    private LocationConstract.IPresenter mPresenter;
    private LocationNativDialogFragment mLocationDialog;
    private boolean isSlidingToLast;
    private boolean mAllPoiLoaded = false;
    private EditText mSearchKeyWordEt;
    private ImageView mCurrentPosIcon;
    private boolean mNeedShowMapAgain = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mIsOnlyShowMap = getIntent().getBooleanExtra(MessageModuleConst.INTENT_KEY_FOR_DISPLAY_MAP,false);
        //得到系统的位置服务，判断GPS是否激活
        LocationManager lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        boolean openGps=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(mIsOnlyShowMap){
            setContentView(R.layout.activity_gdlocation_show_map_layout);
        }else {
            setContentView(R.layout.activity_gdlocation_layout);
        }

        initMapView(savedInstanceState);
        mPresenter = new LocationPresenter(GDLocationActvity.this,this,mAmap);
        startShowMap();

    }

    private void startShowMap(){
        requestPermissions(new OnPermissionResultListener(){

            public void onAllGranted() {

                mPresenter.setOnlyShowMap(mIsOnlyShowMap);
                if(mIsOnlyShowMap){
//                    mProgressbar.setVisibility(View.GONE);
                    showProgress(false);
                    double latitude = getIntent().getDoubleExtra(MessageModuleConst.INTENT_KEY_FOR_LATITUDE,0.0);
                    double longitude = getIntent().getDoubleExtra(MessageModuleConst.INTENT_KEY_FOR_LONGITUDE,0.0);
                    mLocAddress = getIntent().getStringExtra(MessageModuleConst.INTENT_LEY_FOR_LOCATION_ADDRESS);
                    mSpecialAddress = getIntent().getStringExtra(MessageModuleConst.INTENT_LEY_FOR_LOCATION_SPECIAL_ADDRESS);
                    mSpecialAddressText.setText(mSpecialAddress);
                   // mAddressText.setText(mLocAddress);
                    mPresenter.moveToLatlng(new LatLng(latitude,longitude),true);
                }else {
                    mPoiListView.setVisibility(View.VISIBLE);
//                    mBackLocationBtn.setVisibility(View.VISIBLE);
                    mPresenter.location();
                }
            }

            public void onAnyDenied(String[] permissions) {
                LogF.i(TAG,"onAnyDenied");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getString(R.string.need_gps_permission) + "<br/>");
                PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(GDLocationActvity.this, Html.fromHtml(stringBuilder.toString()));
                permissionDeniedDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
                    @Override
                    public void onClick() {
                        if(GDLocationActvity.this!=null) {
                            SettingUtil.gotoPermissionSetting(GDLocationActvity.this);
                            mNeedShowMapAgain = true;
                        }
                    }
                });
                permissionDeniedDialog.setUsedMyPositiveClick(true);
                permissionDeniedDialog.setOnNegativeClickListener(new CommomDialog.OnClickListener() {
                    @Override
                    public void onClick() {
                        if(GDLocationActvity.this!=null)
                            GDLocationActvity.this.finish();
                    }
                });
                permissionDeniedDialog.show();
                if(mContext !=null) {
                    Toast.makeText(mContext, mContext.getString(R.string.lack_authority), Toast.LENGTH_SHORT).show();
                }
            }
            public void onAlwaysDenied(String[] permissions) {
                LogF.i(TAG,"onAlwaysDenied");
            }

        },Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION);
        if (mContext!=null && !AndroidUtil.isNetworkConnected(mContext)) {
            BaseToast.makeText(mContext, mContext.getString(R.string.location_network_invalid_tip), Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    protected void findViews() {
        if(mIsOnlyShowMap){
            initShowMapView();
        }else{
            initLocationView();
        }

    }

    private void initShowMapView(){
        mProgressLayout = findViewById(R.id.progress_layout);
        mSpecialAddressText = (TextView)findViewById(R.id.tv_map_title);
        mShowMapNativBtn = (ImageView) findViewById(R.id.location_nativ_btn);
        mBackBtn = findViewById(R.id.left_back);
        mShowMapNativBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
//        mAddressText = (TextView)findViewById(R.id.tv_map_detail);
    }

    private void initLocationView(){
        mOkBtnView = findViewById(R.id.select_rl);
        mOkBtnView.setOnClickListener(this);
        mOkBtnText = (TextView)findViewById(R.id.location_ok_btn);
        mOkBtnView.setClickable(false);
        mOkBtnText.setTextColor(0xffd5d5d5);


//        mLocationIcon.setVisibility(View.VISIBLE);

        mBackBtn = findViewById(R.id.left_back);
        mBackBtn.setOnClickListener(this);

        mBackLocationBtn = findViewById(R.id.back_to_location);
        mBackLocationBtn.setOnClickListener(this);

        mSearchKeyWordEt = (EditText)findViewById(R.id.search_edit) ;
        mSearchKeyWordEt.setOnClickListener(this);


//        mProgressbar = (ProgressBar) findViewById(R.id.progressbar);
        mProgressLayout = findViewById(R.id.progress_layout);


        mPoiListView = (RecyclerView)findViewById(R.id.location_poi_list);
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
                            mPresenter.loadMorePoiData();
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
        mPoiListAdapter = new PoiListAdapter(this);
        mPoiListAdapter.setItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mPoiListView.setLayoutManager(linearLayoutManager);
        mPoiListView.setAdapter(mPoiListAdapter);

        adjustChooseLocationIcon();
    }

    private void initMapView( Bundle savedInstanceState){
        mMapView = (GDMapView) findViewById(R.id.gd_map_view);
        mMapView.onCreate(savedInstanceState);
        if(mAmap == null){
            mAmap = mMapView.getMap();
//            mAmap.setOnMarkerClickListener(this);
        }
    }
    @Override
    protected void init() {

    }


    private boolean mHasAdjust = false;
    private void adjustChooseLocationIcon(){
        if(mHasAdjust)
            return;
        mHasAdjust = true;

//        float locationAdjustY = mCurrentPosIcon.getY()- (int)AndroidUtil.dip2px(GDLocationActvity.this,19);
//        mCurrentPosIcon.setY(locationAdjustY);

        mCurrentPosIcon = (ImageView) findViewById(R.id.iv_map_position);
        FrameLayout.LayoutParams currentPosParams = (FrameLayout.LayoutParams)mCurrentPosIcon.getLayoutParams();
        currentPosParams.topMargin = currentPosParams.topMargin-(int)AndroidUtil.dip2px(this,24);
        mCurrentPosIcon.setLayoutParams(currentPosParams);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestory时执行mapView.onDestory(),实现地图生命周期管理
        mMapView.onDestroy();
        mPresenter.onDrestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mNeedShowMapAgain){
            mNeedShowMapAgain = false;
            startShowMap();
        }
        LocationUtils.cauclateNativNumber(mContext);
        //在Activity执行onResume时执行mapView.onResume()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mapView.onPause(),实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //在activity执行onSaveInstanceState时执行mapView.onSaveInstanceState(outState),实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.select_rl){
            if (!AndroidUtil.isNetworkConnected(mContext)) {
                BaseToast.makeText(mContext, mContext.getString(R.string.location_network_invalid_tip), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent  = new Intent();
            LatLng latlng = mPresenter.getLatLng();
            intent.putExtra(MessageModuleConst.INTENT_KEY_FOR_LATITUDE,latlng.latitude);
            intent.putExtra(MessageModuleConst.INTENT_KEY_FOR_LONGITUDE,latlng.longitude);
            intent.putExtra(MessageModuleConst.INTENT_LEY_FOR_LOCATION_ADDRESS,mLocAddress);
            intent.putExtra(MessageModuleConst.INTENT_LEY_FOR_LOCATION_SPECIAL_ADDRESS,mLocAddress);
            setResult(RESULT_OK,intent);
            finish();
        }else if(id == R.id.left_back){
            finish();
        }else if(id == R.id.back_to_location){
            mPresenter.backToLocationPosition();
        }else if(id == R.id.location_nativ_btn){
            if(mIsOnlyShowMap == false)
                return ;
            if(mLocationDialog == null){
                mLocationDialog = new LocationNativDialogFragment();
            }
            HandlerThreadFactory.getHandlerThread(HandlerThreadFactory.BackgroundThread).post(new Runnable() {
                @Override
                public void run() {
//                    Log.i(TAG,"BackgroundThread get install Info");
                    if(LocationUtils.getInstallNativNumber() == 0){
//                        Log.i(TAG,"BackgroundThread get install nnum = 0");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GDLocationActvity.this,getResources().getString(R.string.phone_no_nativ_software),Toast.LENGTH_SHORT).show();
                            }
                        });
                        return ;
                    }
                    if(mLocationDialog.isAdded ()) {
//                        Log.i(TAG,"BackgroundThread mLocationDialog isAdded");
                        return;
                    }
                    mLocationDialog.init(GDLocationActvity.this,mPresenter.getLatLng(),mSpecialAddress,LocationUtils.getNativSoftWareInfo());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLocationDialog.show(getSupportFragmentManager(), "");
//                            Log.i(TAG,"mLocationDialog.show");
                        }
                    });

                }
            });

            return ;
        }else if(id == R.id.search_edit){
            Intent intent = new Intent(this,GDSearchPoiActivity.class);
            intent.putExtra(MessageModuleConst.Location.LOCATION_POSITION_CITY_NAME,mPresenter.getLocationPosCityName());
            startActivityForResult(intent,MessageModuleConst.Location.GO_SEARCH_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void showChooseAddress(String titleAddress,String detailAddress){
        mSpecialAddress = titleAddress;
        mLocAddress =detailAddress;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mProgressbar.setVisibility(View.GONE);
                showProgress(false);
                mOkBtnView.setClickable(true);
                mOkBtnText.setTextColor(getResources().getColor(R.color.location_send_btn_text_color));
//                mSpecialAddressText.setText(mSpecialAddress);
//                mAddressText.setText(mLocAddress);
            }
        });
    }

    @Override
    public void setDataList(ArrayList<PoiListBeanData> list) {
        mPoiListAdapter.setDataList(list);
        mPoiListAdapter.notifyDataSetChanged();
    }

    @Override
    public void addDataList(ArrayList<PoiListBeanData> list) {
        mPoiListAdapter.addDataList(list);
        mPoiListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgress(boolean show) {
        if(mProgressLayout == null)
            return;
        if(show){
            mProgressLayout.setVisibility(View.VISIBLE);
        }else{
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAllPoiLoaded() {
        mAllPoiLoaded = true;
        mPoiListAdapter.onAllPoiLoaded();
        mPoiListAdapter.notifyDataSetChanged();
    }

    @Override
    public void selectedLocationPos(boolean selected) {
        if(mBackLocationBtn == null)
            return;

        if(selected){
            mBackLocationBtn.setBackgroundResource(R.drawable.cc_chat_location_selected);
        }else{
            mBackLocationBtn.setBackgroundResource(R.drawable.cc_chat_location_normal);
        }
    }


    @Override
    public void onItemClick(int position) {
        if(mPoiListAdapter == null)
            return;
        PoiListBeanData beanData = mPoiListAdapter.getItemData(position);
        if(beanData == null)
            return;
        mPresenter.moveToLatlng(new LatLng(beanData.mLatitude,beanData.mLongtitude),true);
        showChooseAddress(beanData.mTitleAddress,beanData.mDetailAddress);
        mPoiListAdapter.setSelectPosition(position);
        mPoiListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case MessageModuleConst.Location.GO_SEARCH_ACTIVITY_REQUEST_CODE:
                    if(data == null)
                        break;
                    double latilute = data.getDoubleExtra(MessageModuleConst.INTENT_KEY_FOR_LATITUDE,0);
                    double latlong = data.getDoubleExtra(MessageModuleConst.INTENT_KEY_FOR_LONGITUDE,0);
                    LatLng latLng = new LatLng(latilute,latlong);
                    mPresenter.moveToLatlng(latLng,false);
                    break;
            }
        }

    }

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//        Log.i(TAG,"onMarkerClick");
//
//        return false;
//    }
}
