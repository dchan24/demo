package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.GroupStrangerFragment;
import com.cmicc.module_message.ui.presenter.GroupStrangerPresenter;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

/**
 * Created by zhufang_lu on 2017/6/19.
 * 群陌生联系人界面
 */

public class GroupStrangerActivity extends BaseActivity {
    private  boolean isExchangeCard=false;//是否发送交换名片请求
    private GroupStrangerFragment fragment;
    Toolbar mToolbar;
    RelativeLayout rlBack;
    TextView mToobarTextView;
    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_fragment_with_toolbar_layout);
    }


    @Override
    protected void findViews() {
        rlBack = findViewById(R.id.left_back);
        mToobarTextView = findViewById(R.id.text_title);
        mToobarTextView.setText(R.string.staranger_people_detail);
    }

    @Override
    protected void init() {
        rlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
         fragment = (GroupStrangerFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment == null) {
            fragment = GroupStrangerFragment.newInstance();
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                fragment.setArguments(bundle);
            }
            fragment.setPresenter(new GroupStrangerPresenter(fragment, this));
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.contentFrame);
        }else{
            fragment.setPresenter(new GroupStrangerPresenter(fragment, this));
        }
    }

    public static void show(Context context,String num,String name,String groupId,String groupName,String groupCard){
        Intent intent = new Intent(context, GroupStrangerActivity.class);
        intent.putExtra("name",name);
        intent.putExtra("num",num);
        intent.putExtra("groupName",groupName);
        intent.putExtra("groupCard",groupCard);
        intent.putExtra("groupId",groupId);
        context.startActivity(intent);
    }

    public static void show(Context context,String num,String completeAddress , String name,String groupId,String groupName,String groupCard){
        Intent intent = new Intent(context, GroupStrangerActivity.class);
        intent.putExtra("name",name);
        intent.putExtra("num",num);
        intent.putExtra("completeAddress",completeAddress);   // 包含国家码的地址
        intent.putExtra("groupName",groupName);
        intent.putExtra("groupCard",groupCard);
        intent.putExtra("groupId",groupId);
        context.startActivity(intent);
    }
    public void setIsExchangeCard(boolean isExchangeCard){
        BaseToast.show(this, getString(R.string.has_been_sent));
        this.isExchangeCard=isExchangeCard;
    }
    public void updateHint(){
        if(fragment!=null){
            fragment.updateHint(isExchangeCard);
        }
    }

}
