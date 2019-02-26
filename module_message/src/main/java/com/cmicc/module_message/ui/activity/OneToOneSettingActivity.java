package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.OneToOneSettingFragment;
import com.cmicc.module_message.ui.presenter.OneToOneSettingPresenter;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;

/**
 * Created by GuoXietao on 2017/3/31.
 * 单聊设置界面
 */

public class OneToOneSettingActivity extends BaseActivity {

    OneToOneSettingFragment mOneToOneSettingFragment;
    OneToOneSettingPresenter mOneToOneSettingPresenter;

    public static final String BUNDLE_KEY_ADDRESS = "address";

    Toolbar mToolbar;
    FrameLayout mContentFrame;
    private TextView mTitle;
    private RelativeLayout mBack;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one_setting);
    }

    @Override
    protected void findViews() {
        mToolbar = (Toolbar)findViewById(R.id.id_toolbar);
        mContentFrame = (FrameLayout)findViewById(R.id.contentFrame);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(getResources().getString(R.string.chat_setting));
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mBack = (RelativeLayout) findViewById(R.id.back);
    }

    @Override
    protected void init() {

        initToolBar();

        mOneToOneSettingPresenter = new OneToOneSettingPresenter(this, mOneToOneSettingFragment);

        if (mOneToOneSettingFragment == null) {
            mOneToOneSettingFragment = new OneToOneSettingFragment();
        }
        mOneToOneSettingFragment.setPresenter(mOneToOneSettingPresenter);
        Bundle bundle = getIntent().getExtras();
        mOneToOneSettingFragment.setArguments(bundle);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mOneToOneSettingFragment, R.id.contentFrame);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initToolBar() {
        mToolbar.setTitleTextColor(getResources().getColor(R.color.tv_title_color));
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("");//getResources().getString(R.string.chat_setting)

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        if (mOneToOneSettingFragment != null) {
            intent.putExtra("slient", mOneToOneSettingFragment.getSilent());
            intent.putExtra("userName", mOneToOneSettingFragment.getUserName());
        }
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, OneToOneSettingActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void start(Context context, String address) {
        Intent intent = new Intent(context, OneToOneSettingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_ADDRESS,address);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void startForResult(Activity activity, int requestCode, Bundle bundle) {
        Intent intent = new Intent(activity, OneToOneSettingActivity.class);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }
}
