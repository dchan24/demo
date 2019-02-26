package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.fragment.GroupSmsEditFragment;

import static com.cmicc.module_message.ui.fragment.GroupSmsEditFragment.FROM_TYPE_GROUP_MASS;


/**
 * Created by yangshaowei on 2017/7/13.
 * 群短信编辑
 */

public class GroupSMSEditActivity extends BaseActivity {

    public static int requestCode = 1001 ;
    public static String NAMES = "NAMES";
    public static String PHONES = "PHONES";
    public static String MAXNUMBEROFPEOPLE = "MAXNUMBEROFPEOPLE" ; // 可选人数的最大值

    TextView leftBack ;
    private GroupSmsEditFragment groupSmsEditFragment;
    private String address;
    private int fromType;
    public static final String BUNDLE_KEY_ADDRESS = "group_address";
    public static final String BUNDLE_KEY_FROM_TYPE = "from_type";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_sms_edit);
    }

    @Override
    protected void findViews() {

    }

    @Override
    protected void init() {
        Intent intent = getIntent();
        address = intent.getStringExtra(GroupSMSEditActivity.BUNDLE_KEY_ADDRESS);
        fromType = intent.getIntExtra(GroupSMSEditActivity.BUNDLE_KEY_FROM_TYPE, 0);
        if(groupSmsEditFragment == null){
            groupSmsEditFragment = new GroupSmsEditFragment();
            groupSmsEditFragment.setAddress(this , address, fromType);
        }
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), groupSmsEditFragment, R.id.context_fragment);
        initToolBar();
    }

    private void initToolBar() {
        findViewById(R.id.select_rl).setVisibility(View.GONE);
        leftBack = (TextView) findViewById(R.id.select_picture_custom_toolbar_title_text);
        leftBack.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        if (fromType == FROM_TYPE_GROUP_MASS) {
            leftBack.setText(getResources().getString(R.string.group_mass_assistant));
        } else {
            leftBack.setText(getResources().getString(R.string.group_sms));
        }
        findViewById(R.id.left_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( groupSmsEditFragment != null && !TextUtils.isEmpty(groupSmsEditFragment.et_edit.getText().toString())){
                    exitGroupSMSDialog();
                    return;
                }
                finish();
            }
        });
    }

    public static void start(Context context, String address) {
        Intent intent = new Intent(context, GroupSMSEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(GroupSMSEditActivity.BUNDLE_KEY_ADDRESS, address);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void start(Context context, String address, int fromType) {
        Intent intent = new Intent(context, GroupSMSEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(GroupSMSEditActivity.BUNDLE_KEY_ADDRESS, address);
        bundle.putInt(GroupSMSEditActivity.BUNDLE_KEY_FROM_TYPE, fromType);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if( groupSmsEditFragment != null && !TextUtils.isEmpty(groupSmsEditFragment.et_edit.getText().toString())){
            exitGroupSMSDialog();
            return;
        }
        super.onBackPressed();
    }

    /**
     * 退出群短信提示
     */
    private void exitGroupSMSDialog() {
        String message = this.getString(R.string.eixt_group_SMS);
        CommomDialog commomDialog = new CommomDialog(this, null, message);
        commomDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
        commomDialog.show();
    }
}
