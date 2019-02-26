package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.Threads.HandlerThreadFactory;
import com.cmicc.module_message.ui.constract.LocationConstract;
import com.cmicc.module_message.ui.data.PoiListBeanData;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.fragment.LocationNativDialogFragment;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocationPresenter implements LocationConstract.IPresenter,AMapLocationListener, PoiSearch.OnPoiSearchListener,
        GeocodeSearch.OnGeocodeSearchListener ,AMap.OnCameraChangeListener,LocationSource {
    private final String TAG = "LocationPresenter";
    private LocationConstract.IView mIview;
    private Context mContext;
    private static final String mSearchQueryCtg = "公司企业|地名地址信息|汽车维修|生活服务|摩托车服务|餐饮服务|购物服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|道路附属设施|公共设施";
    //声明AMapLocationClient类对象，定位发起端
    private AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象，定位参数
    private AMapLocationClientOption mLocationOption = null;
    //声明mListener对象，定位监听器
    private OnLocationChangedListener mListener = null;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    private double mLatitude;
    private double mLongitude;
    private double mLocationLati; //定位出来的经纬度，用于回到当前位置
    private double mLocationLonti;//定位出来的经纬度，用于回到当前位置
    private AMap mAmap;
    private static final float DEFAULT_ZOOM_SCALE = 16;
    private float mZoom = DEFAULT_ZOOM_SCALE;
    private static final int SEARCH_DEFAULT_DISTANCE = 500;//搜索POI默认周边距离 ，单位米
    private boolean mIsOnlyShowMap = false; // 点击别人发送过来的位置，只是显示位置，不进行定位
    private int mCurrentPageNum = 1;
    private boolean mAllPoiLoaded = false;

    private String mLocationPosCityName;//当前定位的城市名称

    public LocationPresenter(Context context,LocationConstract.IView iView,AMap amap){
        mContext = context;
        mIview = iView;
        mAmap = amap;
        if(mAmap != null){
            UiSettings uiSettings = mAmap.getUiSettings();
            mAmap.setLocationSource(this);
//            uiSettings.setScrollGesturesEnabled(false); //禁止滑动拖动地图
//            uiSettings.setMyLocationButtonEnabled(true);
            mAmap.setMyLocationEnabled(false);
            uiSettings.setZoomControlsEnabled(false);
            mAmap.setOnCameraChangeListener(this);
//            mAmap.clear();
        }
    }

    @Override
    public void location() {
        mIview.showProgress(true);
        //初始化定位
        mLocationClient = new AMapLocationClient(mContext.getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息(默认返回地址信息)
        mLocationOption.setNeedAddress(true);
        //设置是否定位定位一次，默认为false
        mLocationOption.setOnceLocation(true);
        //设置是否允许模拟位置，默认为false,不允许模拟位置
        mLocationOption.setMockEnable(false);
//        //设置定位间隔，单位毫秒，默认为2000ms
//        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();


//        MyLocationStyle  myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//        mAmap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
//        mAmap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
//
//
//        myLocationStyle.showMyLocation(true);
    }

    private void moveToLatlng(LatLng latlng){
        moveToLatlng(latlng,false);
    }

//    public void showMap(double latitude,double longitude){
////        mAmap.clear();
//        mAmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),mZoom));
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(new LatLng(latitude,longitude));
//        markerOptions.title("当前位置");
//        markerOptions.visible(true);
//        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cc_chat_location_pin_pressed));
//        markerOptions.icon(bitmapDescriptor);
//        mAmap.addMarker(markerOptions);
//    }
    @Override
    public void moveToLatlng(LatLng latlng,boolean justMove){

        mLatitude = latlng.latitude;
        mLongitude = latlng.longitude;
        if(mLocationLati == mLatitude && mLongitude == mLocationLonti){
            mIview.selectedLocationPos(true);
        }else{
            mIview.selectedLocationPos(false);
        }

        //设置缩放级别
        mAmap.clear();
        mAmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,mZoom));
        if(mIsOnlyShowMap == false){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(mLocationLati,mLocationLonti));
            markerOptions.title("当前位置");
            markerOptions.visible(true);
    //        markerOptions.draggable(true);
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cc_chat_map_point_blue));
            markerOptions.icon(bitmapDescriptor);
            mAmap.addMarker(markerOptions);
        }else{
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latlng);
            markerOptions.visible(true);
            //        markerOptions.draggable(true);
            BitmapDescriptor bitmapDescriptor = null;
            if(mLocationLati == mLatitude && mLocationLonti == mLongitude) {  //这个是为了让红色位置图标对准蓝色定位图标的中心点
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cc_chat_map_point_red));
            }else {
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cc_chat_location_pin_pressed));
            }
            markerOptions.icon(bitmapDescriptor);
            mAmap.addMarker(markerOptions);
        }

        if(justMove) {
            mIgnoreNextCameraChange = true;
            return;
        }
        mIview.showProgress(true);
        PoiSearch.Query query = new PoiSearch.Query("", mSearchQueryCtg,"");
        query.setPageNum(mCurrentPageNum);
        query.setPageSize(100);
        PoiSearch search = new PoiSearch(mContext, query);
        search.setBound(new PoiSearch.SearchBound(new LatLonPoint(latlng.latitude, latlng.longitude), SEARCH_DEFAULT_DISTANCE));
        search.setOnPoiSearchListener(this);
        search.searchPOIAsyn();

        //逆地理编码搜索地图

//                GeocodeSearch geocodeSearch = new GeocodeSearch(this);
//                geocodeSearch.setOnGeocodeSearchListener(this);
//                RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(mLatitude, mLongitude), 1000,
//                        GeocodeSearch.AMAP);
//                query.setPoiType(mSearchQueryCtg);
//                geocodeSearch.getFromLocationAsyn(query);
    }

    @Override
    public void loadMorePoiData() {
        if(mAllPoiLoaded == true)
            return;
        mCurrentPageNum++;
        PoiSearch.Query query = new PoiSearch.Query("", mSearchQueryCtg,"");
        query.setPageNum(mCurrentPageNum);
        query.setPageSize(100);
        PoiSearch search = new PoiSearch(mContext, query);
        search.setBound(new PoiSearch.SearchBound(new LatLonPoint(mLocationLati, mLocationLonti), SEARCH_DEFAULT_DISTANCE));
        search.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int resultCode) {
                //判断搜索成功
                if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (null != poiResult && poiResult.getPois().size() > 0) {
                        ArrayList<PoiListBeanData> listBeanData = new ArrayList<PoiListBeanData>();
                        for(int i=0;i<poiResult.getPois().size();i++){
                            PoiItem poiItem = poiResult.getPois().get(i);
                            String titleAddress = poiItem.getTitle();
                            String detailAddress = poiItem.getProvinceName()+poiItem.getCityName()+ poiItem.getSnippet()+titleAddress;
                            PoiListBeanData beanData = new PoiListBeanData();

                            beanData.mLatitude = poiItem.getLatLonPoint().getLatitude();
                            beanData.mLongtitude = poiItem.getLatLonPoint().getLongitude();

                            beanData.mDetailAddress = detailAddress;
                            beanData.mTitleAddress = titleAddress;

                            listBeanData.add(beanData);
                        }
                        mIview.addDataList(listBeanData);

                    }else{
                        mAllPoiLoaded = true;
                        mIview.onAllPoiLoaded();
                        LogF.e(TAG,"onPoinSearched poi size is 0 ");
                    }
                }else{
                    LogF.e(TAG,"onPoinSearched error,resultcode = "+resultCode);
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        search.searchPOIAsyn();
    }

    @Override
    public void searchMap(String keyWord) {
//        PoiSearch.Query query = new PoiSearch.Query(keyWord, mSearchQueryCtg, "");
//        query.setPageNum(0);
//        query.setPageSize(100);
//        PoiSearch poiSearch = new PoiSearch(mContext,query);
//        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
//            @Override
//            public void onPoiSearched(PoiResult poiResult, int resultCode) {
//                if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
//                    if (null != poiResult && poiResult.getPois().size() > 0) {
//                        ArrayList<PoiListBeanData> listBeanData = new ArrayList<PoiListBeanData>();
//                        for(int i=0;i<poiResult.getPois().size();i++){
//                            PoiItem poiItem = poiResult.getPois().get(i);
//                            String titleAddress = poiItem.getTitle();
//                            String detailAddress = poiItem.getProvinceName()+poiItem.getCityName()+ poiItem.getSnippet()+titleAddress;
//                            PoiListBeanData beanData = new PoiListBeanData();
//
//                            beanData.mLatitude = poiItem.getLatLonPoint().getLatitude();
//                            beanData.mLongtitude = poiItem.getLatLonPoint().getLongitude();
//
//                            beanData.mDetailAddress = detailAddress;
//                            beanData.mTitleAddress = titleAddress;
//
//                            listBeanData.add(beanData);
//                        }
//                        mIview.setDataList(listBeanData);
//
//                    }else{
//                        mAllPoiLoaded = true;
//                        mIview.onAllPoiLoaded();
//                        LogF.e(TAG,"onPoinSearched poi size is 0 ");
//                    }
//                }else{
//                    LogF.e(TAG,"onPoinSearched error,resultcode = "+resultCode);
//                }
//            }
//
//            @Override
//            public void onPoiItemSearched(PoiItem poiItem, int i) {
//
//            }
//        });
//        poiSearch.searchPOIAsyn();

    }

    @Override
    public String getLocationPosCityName() {
        return mLocationPosCityName;
    }


    @Override
    public void onDrestroy() {
        if(mLocationClient!=null) {
            mLocationClient.stopLocation();//停止定位
            mLocationClient.onDestroy();//销毁定位客户端
        }
    }

    @Override
    public void backToLocationPosition() {
        mLatitude = mLocationLati;
        mLongitude = mLocationLonti;
        mIgnoreNextCameraChange = true;
        moveToLatlng(new LatLng(mLatitude,mLongitude));
    }

    @Override
    public LatLng getLatLng() {
        return new LatLng(mLatitude,mLongitude);
    }

    @Override
    public void setOnlyShowMap(boolean onlyShowMap) {
        mIsOnlyShowMap = onlyShowMap;
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {

//            Log.e("amap经纬度","lat="+lat+", lon="+lon);
            //如果不设置标志位，此时在拖动地图时，它会不断将地图移动到当前的位置
            if (isFirstLoc) {
                //设置当前地图 显示当前位置
                //经纬度
                mLatitude = aMapLocation.getLatitude();//纬度
                mLongitude = aMapLocation.getLongitude();//经度
                mLocationLati = mLatitude;
                mLocationLonti = mLongitude;
                mLocationPosCityName = aMapLocation.getCity();

                //点击定位按钮 能够将地图的中心移动到定位点
                if(mListener!=null) {
                    mListener.onLocationChanged(aMapLocation);
                }
                mIgnoreNextCameraChange = true;
                moveToLatlng(new LatLng(mLatitude,mLongitude));
            }
        }else {
            LogF.e(TAG,"onLocationChanged  aMapLocation is null ");
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int resultCode) {
        //判断搜索成功
        if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
            if (null != poiResult && poiResult.getPois().size() > 0) {
                ArrayList<PoiListBeanData> listBeanData = new ArrayList<PoiListBeanData>();
                for(int i=0;i<poiResult.getPois().size();i++){
                    PoiItem poiItem = poiResult.getPois().get(i);
                    String titleAddress = poiItem.getTitle();
                    String detailAddress = poiItem.getProvinceName()+poiItem.getCityName()+ poiItem.getSnippet()+titleAddress;
                    PoiListBeanData beanData = new PoiListBeanData();
                    if(i == 0){
                        mIview.showChooseAddress(titleAddress,detailAddress);
                        beanData.mLatitude = mLatitude;
                        beanData.mLongtitude = mLongitude;

                    }else{
                        beanData.mLatitude = poiItem.getLatLonPoint().getLatitude();
                        beanData.mLongtitude = poiItem.getLatLonPoint().getLongitude();
                    }

                    beanData.mDetailAddress = detailAddress;
                    beanData.mTitleAddress = titleAddress;

                    listBeanData.add(beanData);
                }
                isFirstLoc = false;
                mIview.setDataList(listBeanData);
                mIview.showProgress(false);

            }else{
                LogF.e(TAG,"onPoinSearched poi size is 0 ");
            }
        }else{
            LogF.e(TAG,"onPoinSearched error,resultcode = "+resultCode);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    private double mLastCameraChangeLatitude;
    private double mLastCameraChangeLongitude;
    private boolean mIgnoreNextCameraChange = false;

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        if(mIgnoreNextCameraChange){
            mIgnoreNextCameraChange = false;
            return;
        }
        LogF.d(TAG,"onCameraChangeFinish latitude = "+cameraPosition.target.latitude+",longtude = "+cameraPosition.target.longitude);
        if(mIsOnlyShowMap || (mLastCameraChangeLatitude == cameraPosition.target.latitude && mLastCameraChangeLongitude == cameraPosition.target.longitude)){
            return;
        }
        mLastCameraChangeLatitude = cameraPosition.target.latitude;
        mLastCameraChangeLongitude = cameraPosition.target.longitude;
        mZoom = cameraPosition.zoom;
        moveToLatlng(cameraPosition.target);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
//        if (i == AMapException.CODE_AMAP_SUCCESS) {
//            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
//                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
////                mLocAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress();
////                mSpecialAddress = regeocodeResult.getRegeocodeAddress().getPois().get(0).getTitle();
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        mSpecialAddressText.setText(mSpecialAddress);
////                        mAddressText.setText(mLocAddress);
////                    }
////                });
//            } else {
//                LogF.e(TAG,"onRegeocodeSearched size is 0 ");
//            }
//        } else {
//            LogF.e(TAG,"onRegeocodeSearched error,resultcode = "+i);
//        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }



}