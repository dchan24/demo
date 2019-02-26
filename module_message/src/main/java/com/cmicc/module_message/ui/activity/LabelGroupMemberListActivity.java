package com.cmicc.module_message.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.module.proxys.modulecontact.ContactProxy;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.adapter.LabelGroupMemberListAdapter;
import com.cmcc.cmrcs.android.ui.utils.GroupUtils;
import com.cmicc.module_message.R;

import java.util.ArrayList;

/**
 * Created by cq on 2018/5/16.
 * 标签分组成员列表
 */

public class LabelGroupMemberListActivity extends BaseActivity {
    private static final String TAG = LabelGroupMemberListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private LabelGroupMemberListAdapter adapter;
    private TextView mTitle;
    private RelativeLayout mBack;
    private int groupId;

    private ArrayList<SimpleContact> mlist =  new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_group_member_list);
    }

    @Override
    protected void findViews() {
        mRecyclerView = findViewById(R.id.label_group_member_list);
        mToolbar = findViewById(R.id.tb_label_group_member);
        mTitle = findViewById(R.id.title);
        mTitle.setText(getResources().getString(R.string.label_group_member_list));
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mBack = findViewById(R.id.back);
    }

    @Override
    protected void init() {
        LogF.i(TAG, "init");
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        adapter = new LabelGroupMemberListAdapter(this);
        adapter.setOnItemClickListener(new LabelGroupMemberListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showContactDetail(adapter.getItem(position));
            }
        });
        mRecyclerView.setAdapter(adapter);
        initData();
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showContactDetail(SimpleContact simpleContact) {
        if(simpleContact == null || TextUtils.isEmpty(simpleContact.getNumber())){
            LogF.e(TAG, "contact is " + simpleContact);
            return;
        }
        ContactProxy.g.getUiInterface().getContactDetailActivityUI()
                .showForSimpleContact(this ,simpleContact, 0);
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        groupId = bundle.getInt(ContactSelectorActivity.LABEL_ID, 0);
        mlist = GroupUtils.getContactsByLabelId(mContext, groupId);
        adapter.changeDataSet(mlist);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (groupId > 0) {
            mlist = GroupUtils.getContactsByLabelId(mContext, groupId);
            adapter.changeDataSet(mlist);
        }
    }
}
