package com.cmicc.module_message.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.chinamobile.app.yuliao_common.utils.Threads.HandlerThreadFactory;
import com.cmicc.module_message.ui.fragment.LocationNativDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocationUtils {
    private static int mNativSoftWareNum = 0;//本地有多少个导航软件
    private static ConcurrentHashMap<String,Integer> mNativSoftWareInstallInfo = new ConcurrentHashMap<String,Integer>();
    public static int NOT_INSTALL = -1;
    public static int GAO_DE_SUPPORT_MIN_VERSION = 6000;
    private static int mGaoDeVersion;
    private static int mBaiDuVersion;
    private static int mTencentVersion;
    public static void cauclateNativNumber(final Context context) {
        if(context == null)
            return;
        HandlerThreadFactory.getHandlerThread(HandlerThreadFactory.BackgroundThread).post(new Runnable() {
            @Override
            public void run() {
                Log.i("lgh--GDLocationActvity","Start");
                long startTime = System.currentTimeMillis();
                mNativSoftWareInstallInfo.clear();
                mNativSoftWareNum = 0;
                mGaoDeVersion = isAvilible(context, LocationNativDialogFragment.GAODE_PACKAGENAME);
                mBaiDuVersion = isAvilible(context,LocationNativDialogFragment.BAIDU_PACKAGENAME);
                mTencentVersion = isAvilible(context,LocationNativDialogFragment.TENCENT_PACKAGENAME);
                if(mGaoDeVersion!=NOT_INSTALL){
                    mNativSoftWareNum++;
                    mNativSoftWareInstallInfo.put(LocationNativDialogFragment.GAODE_PACKAGENAME,1);
                }
                if(mTencentVersion!=NOT_INSTALL){
                    mNativSoftWareNum++;
                    mNativSoftWareInstallInfo.put(LocationNativDialogFragment.TENCENT_PACKAGENAME,1);
                }
                if(mBaiDuVersion!=NOT_INSTALL){
                    mNativSoftWareNum++;
                    mNativSoftWareInstallInfo.put(LocationNativDialogFragment.BAIDU_PACKAGENAME,1);
                }
                long costTime = System.currentTimeMillis()-startTime;
                Log.i("lgh--GDLocationActvity","cost time = "+costTime);
            }
        });
    }


    public static Map<String, Integer> getNativSoftWareInfo() {
        return mNativSoftWareInstallInfo;
    }

    public static  int getInstallNativNumber() {
        return mNativSoftWareNum;
    }

    public static int getVersion(String packageName){
        if(LocationNativDialogFragment.GAODE_PACKAGENAME.equals(packageName)){
            return mGaoDeVersion;
        }else if(LocationNativDialogFragment.BAIDU_PACKAGENAME.equals(packageName)){
            return mBaiDuVersion;
        }
        else if(LocationNativDialogFragment.TENCENT_PACKAGENAME.equals(packageName)){
            return mTencentVersion;
        }
        return NOT_INSTALL;
    }


    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName：应用包名
     * @return : 返回对应软件得版本号，如果返回值为-1，说明没有安装对应软件
     */
    public static int isAvilible(Context context, String packageName) {
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
//        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String installPackName = packageInfos.get(i).packageName;
                if(installPackName.equals(packageName)){
                    return packageInfos.get(i).versionCode;
                }
//                packageNames.add(packName);
//                packageInfos.get(i).applicationInfo.loadIcon(context.getPackageManager());
            }
        }

        return NOT_INSTALL;
    }
}
