package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.WebConfig;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

public class GroupMassWelcomeActivity extends BaseActivity {

    public static final String GROUP_MASS_SHOW_WELCOME_PAGE = "group_mass_show_welcome_page"; //是否显示群发助手欢迎页
    public static final String GROUP_MASS_FIRST_TIP_SHOW_KEY = "group_mass_first_show"; //是否显示群发消息红点

    private TextView tvConfirm;
    private TextView tvCancel;
    private TextView tvNotify;
    private TextView tvTips;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_mass_welcome);
    }

    @Override
    protected void findViews() {
        tvConfirm = findViewById(R.id.confirm_btn);
        tvCancel = findViewById(R.id.cancel_btn);
        tvNotify = findViewById(R.id.group_mass_notify);
        tvTips = findViewById(R.id.tv_tip);
        tvTips.setText(Html.fromHtml(getString(R.string.group_mass_tip)));
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupSMSEditActivity.start(GroupMassWelcomeActivity.this, "", 1);
                SharePreferenceUtils.setDBParam(getApplicationContext(), GROUP_MASS_SHOW_WELCOME_PAGE, false);
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAgreementUrl();
            }
        });
    }

    @Override
    protected void init() {

    }

    public static Intent createIntent(Context context) {
        return new Intent(context, GroupMassWelcomeActivity.class);
    }

    private void goToAgreementUrl() {
        if (!AndroidUtil.isNetworkConnected(this)) {
            BaseToast.makeText(this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "https://static.wemeetyou.cn/feixin_new/agreement/index.html";//正式
        EnterPriseProxy.g.getUiInterface().jumpToBrowser(this, new WebConfig.Builder().enableRequestToken(false).build(url));
    }
}
