package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.util.Status;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.GroupMassMsgListAdapter;
import com.cmicc.module_message.ui.model.GroupMassModel;
import com.cmicc.module_message.ui.model.GroupMassMsgListModel;
import com.cmicc.module_message.ui.model.impls.GroupMassMsgListModelImpl;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.GroupSMSContract;

import java.util.ArrayList;

/**
 * 群发助手消息列表
 */

public class GroupMassMsgListActivity extends BaseActivity implements GroupSMSContract.IView, GroupMassMsgListModel.GroupMassMsgListLoadFinishCallback {

    private GroupMassMsgListModelImpl groupSMSModel;
    private ArrayList<GroupMassModel> mMessageLists = new ArrayList<>();
    private GroupMassMsgListAdapter mAdapter;

    TextView leftText ;
    RecyclerView mRecyclerView;
    ImageView iv_normal_edit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_mass_msg_list);
    }

    @Override
    protected void findViews() {
        mRecyclerView = findViewById(R.id.group_sms_list);
        iv_normal_edit = findViewById(R.id.iv_normal_edit);
    }

    @Override
    protected void init() {
        initToolBar();
        groupSMSModel = new GroupMassMsgListModelImpl();
        groupSMSModel.loadGroupMassMsgList(this, getLoaderManager(), this);
        WrapContentLinearLayoutManager linearLayoutManager = new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new GroupMassMsgListAdapter(this, mMessageLists);
        mRecyclerView.setAdapter(mAdapter);
        iv_normal_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupSMSEditActivity.start(GroupMassMsgListActivity.this, "", 1);
                finish();
            }
        });
    }

    private void initToolBar() {
        findViewById(R.id.select_rl).setVisibility(View.GONE);
        leftText = findViewById(R.id.select_picture_custom_toolbar_title_text);
        leftText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        leftText.setText(getResources().getString(R.string.group_mass_assistant));
        findViewById(R.id.left_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GroupMassMsgListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_sms ,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onLoadFinished(Cursor cursor) {
        if(cursor == null ){
            return;
        }
        mMessageLists.clear();
        while (cursor.moveToNext()) {
            GroupMassModel model = new GroupMassModel();
            long id = cursor.getLong(cursor.getColumnIndex("_id"));
            String body = cursor.getString(cursor.getColumnIndex("body"));
            long date = cursor.getLong(cursor.getColumnIndex("date"));
            int status = cursor.getInt(cursor.getColumnIndex("status"));
            String person = cursor.getString(cursor.getColumnIndex("person"));
            String sendAddress = cursor.getString(cursor.getColumnIndex("send_address"));
            model.setId(id);
            model.setBody(body);
            model.setDate(date);
            model.setStatus(status);
            model.setSendAddress(sendAddress);
            model.setPerson(person);
            if(status == Status.STATUS_OK){
                mMessageLists.add(model);
            }
        }
        if (mAdapter == null){
            mAdapter = new GroupMassMsgListAdapter(this, mMessageLists);
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.setDataList(mMessageLists);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(mMessageLists.size() - 1);
        }
    }
}
