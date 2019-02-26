package com.cmicc.module_message.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.dialogs.PermissionDeniedDialog;
import com.cmcc.cmrcs.android.ui.dialogs.PermissionNavigationListener;
import com.cmcc.cmrcs.android.ui.utils.GlobalConfig;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.SettingUtil;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmicc.module_message.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_NEW_FREE_MSG;


/**
 * Created by xyz on 2017/7/12.
 * 超级短信界面
 */

public class SuperMsgActivity extends BaseActivity {

    private final static String TAG = "SuperMsgActivity";
    public MyHandler handler;
    private static final int OPEN = 1001;
    private static final int CLOSE = 1000;
    public static final String SP_KEY_FIRST_SUPER_MSG = "first_send_super_msg_key";
    private RelativeLayout mSuperMsgLayout;
    TextView tvFree;
    TextView groupTip;
    TextView tvTip;
    TextView tvTip2;
    TextView tvTip3;
    TextView tvTipNewone ;
    TextView tvTipMore;
    int isFrom;//哪里调起的 0 会话列表  1 消息列表
    private PermissionNavigationListener mPermissionNavigationListener;
    private String accountUser ;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_msg);
//        if (Build.VERSION.SDK_INT >= 21) {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(option);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
        accountUser = LoginUtils.getInstance().getLoginUserName();
    }

    @Override
    protected void findViews() {
        tvFree = (TextView) findViewById(R.id.free_message);
        groupTip = (TextView) findViewById(R.id.group_tip);
        tvTip2 = (TextView) findViewById(R.id.tv_tip2);
        tvTip3 = (TextView) findViewById(R.id.tv_tip3);
        tvTip = (TextView) findViewById(R.id.tv_tip);
        tvTipNewone = (TextView) findViewById(R.id.tv_tip_newone);
        tvTipMore = (TextView) findViewById(R.id.tv_tip_more);
    }

    public static Intent createIntentForSuperMsgSetting(Context context, int isFrom) {
        Intent intent = new Intent(context, SuperMsgActivity.class);
        intent.putExtra("isFrom", isFrom);
        return intent;
    }


    @Override
    protected void init() {
        Intent intent = getIntent();
        isFrom = intent.getIntExtra("isFrom", 0);

        if(isFrom == 1){
            UmengUtil.buryPoint(mContext,"p2pmessage_sms_welcome","消息-点对点会话-加号-免费短信-欢迎页",0);
        }

        handler = new MyHandler(this);
        mSuperMsgLayout = (RelativeLayout) findViewById(R.id.super_msg_layout);
        findViewById(R.id.sure_btn).setOnClickListener(this);
        findViewById(R.id.cancle_btn).setOnClickListener(this);
//        TextView tv=(TextView)findViewById(R.id.tv_notices);
//        tv.setText(Html.fromHtml("你的短信将在和飞信消息列表单独显示；<br/>给<font color='#e0c154'>移动用户发送短信免费</font>，非移动用户<br/>将按标准收费"));
        mPermissionNavigationListener = PermissionNavigationListener.getInstance();
        int statusBarHeight = (int) (getStatusBarHeight(this) + AndroidUtil.dip2px(this, 50));
        int left = (int) AndroidUtil.dip2px(this, 20);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tvFree.getLayoutParams();
        if (Build.VERSION.SDK_INT >= 21) {
            layoutParams.setMargins(left, (int) AndroidUtil.dip2px(this, 39), 0, 0);
        } else {
            layoutParams.setMargins(left, statusBarHeight, 0, 0);
        }

        if ((NumberUtils.getLoginNumberAreaCode(this).equals(NumberUtils.AREA_CODE_CHINA) && !AndroidUtil.isCMCCMobileByNumber(LoginUtils.getInstance().getLoginUserName()))
                || NumberUtils.getLoginNumberAreaCode(this).equals(NumberUtils.AREA_CODE_CHINA_HK)
                ) {
            groupTip.setText(getResources().getString(R.string.use_sms));
            tvFree.setText(getResources().getString(R.string.welcome_to_use_sms));

            String using_system_SMS = getResources().getString(R.string.using_system_SMS);
            SpannableString spannableString = new SpannableString(using_system_SMS);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF157CF8"));
            ForegroundColorSpan colorSpanT0 = new ForegroundColorSpan(Color.parseColor("#FF157CF8"));
            spannableString.setSpan(colorSpan, 4, 8, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(colorSpanT0 , using_system_SMS.length()-2 , using_system_SMS.length() , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tvTip.setText(spannableString);
            tvTip2.setText(getResources().getString(R.string.send_sms_by_andfetion));
            String standard_of_tariff_SMS = getResources().getString(R.string.standard_of_tariff_SMS);
            SpannableString spannableStringTo = new SpannableString(standard_of_tariff_SMS);
            ForegroundColorSpan standard_of_tariffcolor_Span = new ForegroundColorSpan(Color.parseColor("#FF157CF8"));
            spannableStringTo.setSpan(standard_of_tariffcolor_Span ,0 ,9 ,  Spanned.SPAN_INCLUSIVE_EXCLUSIVE );
            tvTipNewone.setText(spannableStringTo);
            tvTipNewone.setVisibility(View.VISIBLE);
            tvTip3.setVisibility(View.GONE);
            tvTipMore.setVisibility(View.GONE);
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<SuperMsgActivity> weakReference;

        public MyHandler(SuperMsgActivity mActivity) {
            weakReference = new WeakReference<SuperMsgActivity>(mActivity);
        }

        public void handleMessage(android.os.Message msg) {
            final SuperMsgActivity mActivity = weakReference.get();
            if (mActivity == null) {
                return;
            }
            if (msg.what == OPEN) {//打开短信接管和显示
//                final String currentPn = mActivity.getPackageName();
                mActivity.requestPermissions(new OnPermissionResultListener() {
                                                 @Override
                                                 public void onAllGranted() {
                                                     super.onAllGranted();
                                                     mActivity.updateSwitch(true);
                                                     if (mActivity.isFrom == 0) {
                                                         Intent intent = ContactSelectorActivity.creatIntent(mActivity, SOURCE_NEW_FREE_MSG, 1);
                                                         mActivity.startActivity(intent);
                                                         mActivity.finish();
                                                     } else {
                                                         mActivity.setResult(RESULT_OK);
                                                         mActivity.finish();
                                                     }
//                                                     CommomDialog mDialog = new CommomDialog(mActivity, "应用开机自启动权限", "请开启应用自启动权限，以保证短信正常接收");
//                                                     mDialog.setPositiveName("去设置");
//                                                     mDialog.show();
//                                                     mDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
//
//                                                         @Override
//                                                         public void onClick() {
//                                                             SettingUtil.gotoPermissionSetting(mActivity);
//                                                             mActivity.finish();
//                                                         }
//                                                     });
//                                                     mDialog.setOnNegativeClickListener(new CommomDialog.OnClickListener() {
//
//                                                         @Override
//                                                         public void onClick() {
//                                                             mActivity.finish();
//                                                         }
//                                                     });

                                                 }

                                                 @Override
                                                 public void onAnyDenied(String[] permissions) {
                                                     super.onAnyDenied(permissions);
                                                     StringBuilder stringBuilder = new StringBuilder();
                                                     stringBuilder.append(mActivity.getString(R.string.need_authority) + "<br/>");
                                                     PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(mActivity, Html.fromHtml(stringBuilder.toString()));
                                                     permissionDeniedDialog.setOnNegativeClickListener(new CommomDialog.OnClickListener() {
                                                         @Override
                                                         public void onClick() {
                                                             SettingUtil.gotoPermissionSetting(mActivity);
                                                         }
                                                     });
                                                     permissionDeniedDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
                                                         @Override
                                                         public void onClick() {
                                                             mActivity.finish();
                                                         }
                                                     });
                                                     permissionDeniedDialog.show();
                                                     Toast.makeText(mActivity, mActivity.getResources().getString(R.string.lack_authority), Toast.LENGTH_SHORT).show();
                                                     mActivity.updateSwitch(false);
                                                 }
                                             }, Manifest.permission.READ_SMS,//阅读消息
                        Manifest.permission.READ_CONTACTS,//读取联系人
                        Manifest.permission.SEND_SMS,//发送消息
                        Manifest.permission.RECEIVE_MMS,//阅读彩信
                        Manifest.permission.RECEIVE_SMS//接收消息
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkDeviceHasNavigationBar(this)) {
            int virtualBarHeight = getNavigationBarHeight(this, Configuration.ORIENTATION_PORTRAIT);
            Log.d(TAG, "virtualBarHeight= " + virtualBarHeight);
            Log.d(TAG, "isNavigationBarShow= " + isNavigationBarShow());
            Display display = getWindowManager().getDefaultDisplay(); // 为获取屏幕宽、高
            Log.d(TAG, "display = " + display.getHeight());
            if (virtualBarHeight > 0 && isNavigationBarShow()) {
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mSuperMsgLayout.getLayoutParams();
//                params.height = virtualBarHeight;
//                mSuperMsgLayout.requestLayout();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }
            }
        }
    }

    private void updateSwitch(final boolean status) {
        boolean swStatus = (boolean) SharePreferenceUtils.getDBParam(getApplicationContext(), GlobalConfig.OPEN_SMS_STATUS, false);
        if (status != swStatus && status) {
            long date = System.currentTimeMillis();
            LogF.d("tigger", "开关处 短信开关开启时间-----------------" + date);
            SharePreferenceUtils.setDBParam(getApplicationContext(), GlobalConfig.SMS_SYS_USER, LoginUtils.getInstance().getLoginUserName());
            SharePreferenceUtils.setDBParam(getApplicationContext(), GlobalConfig.SMS_SYS_TIME, date);
        }
        SharePreferenceUtils.setDBParam(getApplicationContext(), GlobalConfig.OPEN_SMS_STATUS, status);
        SharePreferenceUtils.setDBParam(getApplicationContext(), SP_KEY_FIRST_SUPER_MSG, false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.sure_btn) {
            if(isFrom == 1){
                UmengUtil.buryPoint(mContext,"p2pmessage_sms_welcome_done","消息-点对点会话-加号-免费短信-欢迎页-确定",0);
            }
            Log.d(TAG, "onClick");
            handler.sendEmptyMessage(OPEN);

        } else if (i == R.id.cancle_btn) {
            if(isFrom == 1){
                UmengUtil.buryPoint(mContext,"p2pmessage_sms_welcome_later","消息-点对点会话-加号-免费短信-欢迎页-以后再说",0);
            }
            Log.d(TAG, "cancle_btn onClick");
            finish();

        }
    }


    private interface Manufacturer {
        String HUAWEI = "huawei";    // 华为
        String MEIZU = "meizu";      // 魅族
        String XIAOMI = "xiaomi";    // 小米
        String SONY = "sony";        // 索尼
        String SAMSUNG = "samsung";  // 三星
        String LETV = "letv";        // 乐视
        String ZTE = "zte";          // 中兴
        String YULONG = "yulong";    // 酷派
        String LENOVO = "lenovo";    // 联想
        String LG = "lg";            // LG
        String OPPO = "oppo";        // oppo
        String VIVO = "vivo";        // vivo
        String SMARTISAN = "smartisan";        // 锤子
    }


    /*
    *获取虚拟键高度
    */
    private int getNavigationBarHeight(Context context, int orientation) {
        Resources resources = context.getResources();

        int id = resources.getIdentifier(
                orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }

    //获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }
        return hasNavigationBar;
    }

    public boolean isNavigationBarShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = this.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            boolean result = realSize.y != size.y;
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }

    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
