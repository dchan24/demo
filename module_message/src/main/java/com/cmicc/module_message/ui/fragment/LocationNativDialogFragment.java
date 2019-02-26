package com.cmicc.module_message.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.model.LatLng;
import com.cmicc.module_message.R;
import com.cmicc.module_message.utils.LocationUtils;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocationNativDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private Context mContext;
    private LatLng mLatlng;
    private View mRootView;
    private TextView mGoGD;
    private View  mGDLine;

    private TextView mGoBaidu;
    private TextView mGoTencent;
    private View  mBaiduLine;
    private View  mTencentLine;
//    private TextView mCancel;


    public final static String BAIDU_PACKAGENAME = "com.baidu.BaiduMap";
    public final static String GAODE_PACKAGENAME = "com.autonavi.minimap";
    public final static String TENCENT_PACKAGENAME = "com.tencent.map";

    private ConcurrentHashMap<String,Integer> mInstallNativInfo;
    private String mNativAddressName;


    public void init(Context context, LatLng latlng,String natvAddressName, Map<String,Integer> nativInstallInfo){
        mContext = context;
        mLatlng = latlng;
        mNativAddressName = natvAddressName;
        mInstallNativInfo = new ConcurrentHashMap<String,Integer>(nativInstallInfo);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        mRootView = inflater.inflate(R.layout.location_nativ_dialog_fragment, null);
        mGoGD = (TextView)mRootView.findViewById(R.id.gd_nativ);
        mGoBaidu = (TextView)mRootView.findViewById(R.id.baidu_nativ);
        mGoTencent = (TextView)mRootView.findViewById(R.id.tecent_nativ);
//        mCancel = (TextView)mRootView.findViewById(R.id.cancel_nativ);
        mGoGD.setOnClickListener(this);
        mGoBaidu.setOnClickListener(this);
        mGoTencent.setOnClickListener(this);
        mGDLine = mRootView.findViewById(R.id.gd_item_line);
        mBaiduLine = mRootView.findViewById(R.id.baidu_item_line);
        mTencentLine = mRootView.findViewById(R.id.tencent_item_line);
//        mCancel.setOnClickListener(this);
        if(mInstallNativInfo!=null && mInstallNativInfo.get(GAODE_PACKAGENAME)!=null && mInstallNativInfo.get(GAODE_PACKAGENAME)== 1){
            mGoGD.setVisibility(View.VISIBLE);
            mGDLine.setVisibility(View.VISIBLE);
        }

        if(mInstallNativInfo!=null && mInstallNativInfo.get(BAIDU_PACKAGENAME)!=null && mInstallNativInfo.get(BAIDU_PACKAGENAME) == 1){
            mGoBaidu.setVisibility(View.VISIBLE);
            mBaiduLine.setVisibility(View.VISIBLE);
        }

        if(mInstallNativInfo!=null && mInstallNativInfo.get(TENCENT_PACKAGENAME)!=null && mInstallNativInfo.get(TENCENT_PACKAGENAME) == 1){
            mGoTencent.setVisibility(View.VISIBLE);
            mTencentLine.setVisibility(View.VISIBLE);
        }
        return mRootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.gd_nativ){
            dismiss();
            gaodeGuide(mContext,mLatlng);
        }else if(id == R.id.baidu_nativ){
            dismiss();
            baiduGuide(mContext,mLatlng);
        }else if(id == R.id.tecent_nativ){
            tencentGuide(mContext,mLatlng);
            dismiss();
        }
    }
    /**
     * 高德导航
     * @param context
     * @param location
     */
    public void gaodeGuide(Context context, LatLng latLng) {
//        if (isAvilible(context, GAODE_PACKAGENAME)) {
//            try {
//                Intent intent = Intent.getIntent("androidamap://navi?sourceApplication=" +
//                        context.getResources().getString(R.string.app_name) +
//                        "&poiname=我的目的地" +
//                        "&lat=" + latLng.latitude +
//                        "&lon=" + latLng.longitude +
//                        "&dev=0");
//                context.startActivity(intent);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Toast.makeText(context, "您尚未安装高德地图", Toast.LENGTH_LONG).show();
//            Uri uri = Uri.parse("market://details?id=com.autonavi.minimap");
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            context.startActivity(intent);
//        }

//        try {
//            Intent intent = Intent.getIntent("androidamap://navi?sourceApplication=" +
//                    context.getResources().getString(R.string.app_name) +
//                    "&poiname=我的目的地" +
//                    "&lat=" + latLng.latitude +
//                    "&lon=" + latLng.longitude +
//                    "&dev=0"+
//                    "&t=2");
//            context.startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        if(LocationUtils.getVersion(GAODE_PACKAGENAME) < LocationUtils.GAO_DE_SUPPORT_MIN_VERSION){
           Toast.makeText(context,context.getResources().getString(R.string.phone_nativ_version_low),Toast.LENGTH_SHORT).show();
           return;
        }
        try{
            openGaodeMapToGuide(context,latLng);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void openGaodeMapToGuide(Context context, LatLng latLng) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        String url = "androidamap://route?sourceApplication="+context.getResources().getString(R.string.app_name)
                +"&did=BGVIS2&dlat="+latLng.latitude+"&dlon="+latLng.longitude+"&dname="+mNativAddressName+"&dev=0&t=0";
//"amapuri://route/plan/?sid=BGVIS1&slat=39.92848272&slon=116.39560823&sname=A&did=BGVIS2&dlat=39.98848272&dlon=116.47560823&dname=B&dev=0&t=0"
  //      String url = "amapuri://route/plan/?&did=BGVIS2&dlat="+latLng.latitude+"&dlon="+latLng.longitude+"&dev=0&t=0";
        Uri uri = Uri.parse(url);
        //将功能Scheme以URI的方式传入data
        intent.setData(uri);
        //启动该页面即可
        startActivity(intent);
    }

    /**
     * 百度导航
     * @param context
     * @param location location[0]纬度lat，location[1]经度lon
     */
    public  void baiduGuide(Context context,  LatLng latLng) {
        double[] baiduLoc = gcj02_To_Bd09(latLng.latitude, latLng.longitude);

//        if (isAvilible(context, BAIDU_PACKAGENAME)) {//传入指定应用包名
//            try {
//                Intent intent = Intent.getIntent("intent://map/direction?" +
//                        //"origin=latlng:"+"34.264642646862,108.95108518068&" +   //起点  此处不传值默认选择当前位置
//                        "destination=latlng:" + baiduLoc[0] + "," + baiduLoc[1] + "|name:我的目的地" +        //终点
//                        "&mode=driving" +          //导航路线方式
//                        "&region=" +           //
//                        "&src=" +
//                        context.getResources().getString(R.string.app_name) +
//                        "#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
//                context.startActivity(intent); //启动调用
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        } else {//未安装
//            //market为路径，id为包名
//            //显示手机上所有的market商店
//            Toast.makeText(context, "您尚未安装百度地图", Toast.LENGTH_LONG).show();
//            Uri uri = Uri.parse("market://details?id=com.baidu.BaiduMap");
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            context.startActivity(intent);
//        }

            try {
                Intent intent = Intent.getIntent("intent://map/direction?" +
                        //"origin=latlng:"+"34.264642646862,108.95108518068&" +   //起点  此处不传值默认选择当前位置
                        "destination=latlng:" + baiduLoc[0] + "," + baiduLoc[1] + "|name:我的目的地" +        //终点
//                        "&mode=walking" +          //导航路线方式
                        "&mode=driving" +          //导航路线方式
                        "&region=" +           //
                        "&src=" +
                        context.getResources().getString(R.string.app_name) +
                        "#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
                context.startActivity(intent); //启动调用
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param lat
     * @param lon
     */
    public static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    public static double[] gcj02_To_Bd09(double lat, double lon) {
        double x = lon, y = lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta) + 0.0065;
        double tempLat = z * Math.sin(theta) + 0.006;
        double[] gps = {tempLat, tempLon};
        return gps;
    }


    /**
     * 腾讯导航
     * @param context
     * @param location
     */
    public void tencentGuide(Context context, LatLng latLng) {
        String downloadUri = "http://softroute.map.qq.com/downloadfile?cid=00001";
        String baseUrl = "qqmap://map/";
        String searchPlace = "search?keyword=酒店&bound=39.907293,116.368935,39.914996,116.379321";
        String searchAround = "search?keyword=肯德基&center=39.908491,116.374328&radius=1000";
        String busPlan = "routeplan?type=bus&from=我的家&fromcoord=39.980683,116.302&to=柳巷&tocoord=39.9836,116.3164&policy=2";
        String drivePlan = "routeplan?type=drive&from=&fromcoord=&to=&tocoord=" + latLng.latitude + "," + latLng.longitude + "&policy=1";
     //   String drivePlan = "routeplan?type=walk&from=&fromcoord=&to=&tocoord=" + latLng.latitude + "," + latLng.longitude + "&policy=1";
        String tencnetUri = baseUrl + drivePlan + "&referer=" + context.getResources().getString(R.string.app_name);

//        if (isAvilible(context, TENCENT_PACKAGENAME)) {
//            Intent intent;
//            try {
//                intent = Intent.parseUri(tencnetUri, 0);
//                context.startActivity(intent);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        } else {
//            //直接下载
////            Intent intent;
////            try {
////                intent = Intent.parseUri(downloadUri, 0);
////                context.startActivity(intent);
////            } catch (URISyntaxException e) {
////                e.printStackTrace();
////            }
//            //市场下载
//            Toast.makeText(context, "您尚未安装腾讯地图", Toast.LENGTH_LONG).show();
//            Uri uri = Uri.parse("market://details?id=" + TENCENT_PACKAGENAME);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            context.startActivity(intent);
//        }


            Intent intent;
            try {
                intent = Intent.parseUri(tencnetUri, 0);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }




}
