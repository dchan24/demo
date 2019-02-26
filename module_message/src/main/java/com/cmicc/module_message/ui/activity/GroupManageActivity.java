package com.cmicc.module_message.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.constract.GroupManageContract;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmicc.module_message.ui.presenter.GroupManagePresenter;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @anthor situ
 * @time 2017/6/20 16:04
 * @description 群管理页面
 */

public class GroupManageActivity extends BaseActivity implements GroupManageContract.IView {

    public static final String BUNDLE_KEY_MEMBERS = "members";
    public static final String BUNDLE_KEY_GROUP_ID = "group_id";

    Toolbar mToolbar;

    private GroupManagePresenter mPresenter;

    private ProgressDialog mProgressDialog;

    private boolean isEPgroup = false;//是否为企业群
    public static ArrayList<String> mGroupMembers;
    private TextView groupTransferTv ; // 群主管理权转让
    private TextView groupDisbandTv ;  // 解散群
    private String group_ID ; //
    private TextView mTitle;
    private RelativeLayout mBack;

    private final float SETTING_ITEM_FONT_SIZE = 18f;
    private static boolean isFocuse = false ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
    }

    @Override
    protected void findViews() {
        mToolbar = (Toolbar)findViewById(R.id.id_toolbar);
        findViewById(R.id.group_transfer).setOnClickListener(this);
        findViewById(R.id.group_disband).setOnClickListener(this);
        groupTransferTv = (TextView) findViewById(R.id.group_transfer_tv);
        groupDisbandTv = (TextView) findViewById(R.id.group_disband_tv);
        mTitle = (TextView) findViewById(R.id.title);
        mBack = (RelativeLayout) findViewById(R.id.back);
    }

    @Override
    protected void init() {
        initToolBar();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("请稍后..");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);

        Bundle bundle = getIntent().getExtras();
        mPresenter = new GroupManagePresenter(this, this, bundle);
        isEPgroup = bundle.getBoolean("isEPgroup",false);
        group_ID = bundle.getString(BUNDLE_KEY_GROUP_ID);
        if(isEPgroup){
            findViewById(R.id.group_transfer).setVisibility(View.GONE);
            findViewById(R.id.group_disband).setVisibility(View.GONE);
        }
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitle.setText(getResources().getString(R.string.group_manage));
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }

    private void initToolBar() {
        mToolbar.setTitleTextColor(getResources().getColor(R.color.tv_title_color));
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.group_transfer) {
            mPresenter.transferGroup();

        } else if (i == R.id.group_disband) {
            showDissolveConfirmDialog();
        }
    }

    public static void start(Context context, String groupId, ArrayList<String> members ,boolean isEPgroup) {
        Bundle bundle = new Bundle();
        mGroupMembers = members;
//        bundle.putStringArrayList(BUNDLE_KEY_MEMBERS, members);
        bundle.putString(BUNDLE_KEY_GROUP_ID, groupId);
        bundle.putBoolean("isEPgroup" ,isEPgroup);
        Intent intent = new Intent(context, GroupManageActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void toast(final CharSequence sequence) {
        runOnUiThread(new ToastRunnable(this ,sequence));
    }

    private static class ToastRunnable implements Runnable{
        WeakReference<Context> weakReference;
        CharSequence sequence;
        public ToastRunnable(Context context ,CharSequence sequence) {
            weakReference = new WeakReference<Context>(context);
            this.sequence = sequence;
        }

        @Override
        public void run() {
            Context context = weakReference.get();
            if (context == null) {
                return;
            }
            BaseToast.makeText(context, sequence, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    public void showDissolveConfirmDialog() {
        String message = this.getString(R.string.dissolve_group_confirm);
        CommomDialog commomDialog = new CommomDialog(this, null, message);
        commomDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
            @Override
            public void onClick() {
                mPresenter.disbandGroup();
            }
        });
        commomDialog.show();
    }

    @Override
    public void toggleProgressDialog(boolean show) {
        if (show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onAppFontSizeChanged(float scale ){
        groupTransferTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale);
        groupDisbandTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale);
        mTitle.setTextSize(20f * scale);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFocuse = true ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFocuse = false ;
    }

    public static boolean isFocuse (){
        return isFocuse ;
    }
}
