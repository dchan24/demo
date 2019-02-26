package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.model.GroupNotify;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.GroupSMSListAdapter;
import com.cmicc.module_message.ui.constract.GroupSMSContract;
import com.cmcc.cmrcs.android.ui.dialogs.CopyDelectDialog;
import com.cmicc.module_message.ui.model.GroupMassModel;
import com.cmicc.module_message.ui.model.GroupSMSModel;
import com.cmicc.module_message.ui.model.impls.GroupSMSModelImpl;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunter on 2017/7/10.
 * 群短信
 */

public class GroupSMSActivity extends BaseActivity implements GroupSMSContract.IView, GroupSMSModel.GroupSMSLoadFinishCallback {

    public static final String BUNDLE_KEY_ADDRESS = "group_address";
    public static final String BUNDLE_KEY_NAME = "group_name";
    private String mName;
    private String mAddress;
    private GroupSMSModelImpl groupSMSModel;
    private ArrayList<GroupMassModel> mMessageLists = new ArrayList<>();
    private GroupSMSListAdapter mAdapter;
    private List<GroupMember> mGroupMembers;

    TextView leftText ;
    RecyclerView mRecyclerView;
    RelativeLayout group_message_empty;
    ImageView iv_edit;
    ImageView iv_normal_edit;
    private RelativeLayout list ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_sms);
    }

    @Override
    protected void findViews() {
        mRecyclerView = findViewById(R.id.group_sms_list);
        group_message_empty = findViewById(R.id.group_message_empty);
        iv_edit = findViewById(R.id.iv_edit);
        iv_normal_edit = findViewById(R.id.iv_normal_edit);
        list = findViewById(R.id.list);
    }

    @Override
    protected void init() {
        initToolBar();
        Intent intent = getIntent();
        mAddress = intent.getStringExtra(GroupSMSActivity.BUNDLE_KEY_ADDRESS);
        mName = intent.getStringExtra(GroupSMSActivity.BUNDLE_KEY_NAME);
        groupSMSModel = new GroupSMSModelImpl();
        groupSMSModel.loadGroupSMS(this, mAddress, getLoaderManager(), this);
        WrapContentLinearLayoutManager linearLayoutManager = new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new GroupSMSListAdapter(this, mMessageLists);
        mAdapter.setOnItemClickListener(new GroupSMSListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, final int position) {
                final CopyDelectDialog copyDelectDialog = new CopyDelectDialog(GroupSMSActivity.this);
                if(!copyDelectDialog.isShowing()) {
                    copyDelectDialog.setOnDelectListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            copyDelectDialog.dismiss();
                            new DeleteTasK().execute(position);
                        }
                    });
                    copyDelectDialog.setOnCopyListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            copyDelectDialog.dismiss();
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            // 将文本内容放到系统剪贴板里。
                            GroupMassModel groupNotify = mAdapter.getItem(position);
                            cm.setText(groupNotify.getBody());
                            Toast.makeText(GroupSMSActivity.this , "已复制", Toast.LENGTH_SHORT).show();
                        }
                    });
                    copyDelectDialog.show();
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mGroupMembers = GroupChatUtils.getMembers(this, mAddress);
        iv_edit.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           GroupSMSEditActivity.start(GroupSMSActivity.this, mAddress);
                                           finish();
                                       }
                                   }
              );
        iv_normal_edit.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  GroupSMSEditActivity.start(GroupSMSActivity.this, mAddress);
                                                  finish();
                                              }
                                          }
               );
    }

    private void initToolBar() {
        findViewById(R.id.select_rl).setVisibility(View.GONE);
        leftText = findViewById(R.id.select_picture_custom_toolbar_title_text);
        leftText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        leftText.setText(getResources().getString(R.string.group_sms));
        findViewById(R.id.left_back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
               );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static void start(Context context, String address, String groupName) {
        Intent intent = new Intent(context, GroupSMSActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(GroupSMSActivity.BUNDLE_KEY_ADDRESS, address);
        bundle.putString(GroupSMSActivity.BUNDLE_KEY_NAME, groupName);
        intent.putExtras(bundle);
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
        if (cursor.getCount() == 0) {
            group_message_empty.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        }else{
            group_message_empty.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        }
        mMessageLists.clear();
        while (cursor.moveToNext()) {
            GroupMassModel groupNotify = new GroupMassModel();
            long id = cursor.getLong(cursor.getColumnIndex("_id"));
            String body = cursor.getString(cursor.getColumnIndex("body"));
            long date = cursor.getLong(cursor.getColumnIndex("date"));
            int status = cursor.getInt(cursor.getColumnIndex("status"));
            String sendeeName = cursor.getString(cursor.getColumnIndex(GroupNotify.COLUMN_SENDEENAME));
            groupNotify.setId(id);
            groupNotify.setBody(body);
            groupNotify.setDate(date);
            groupNotify.setStatus(status);
            groupNotify.setSendAddress(sendeeName);
            if(status == 2){
                mMessageLists.add(groupNotify);
            }
        }
        if (mAdapter == null){
            mAdapter = new GroupSMSListAdapter(this, mMessageLists);
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.setDataList(mMessageLists);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(mMessageLists.size() - 1);
        }
    }

    private class DeleteTasK extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            GroupMassModel conv = mAdapter.getItem(params[0]);
            GroupChatUtils.deleteGroupMessageConversation(GroupSMSActivity.this, conv.getId());
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            groupSMSModel.reloadGroupSMS(getLoaderManager());
        }
    }
}
