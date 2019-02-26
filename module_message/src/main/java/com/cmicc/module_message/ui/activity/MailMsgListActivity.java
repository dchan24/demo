package com.cmicc.module_message.ui.activity;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.MailAssistantConversation;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.MailListAdapter;
import com.cmcc.cmrcs.android.ui.utils.MailAssistantUtils;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.ui.view.dragbubble.DragBubbleView;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dchan on 2018/1/17.
 * 139邮箱列表页面
 */

public class MailMsgListActivity extends BaseActivity implements LoaderCallbacks<Cursor> {
	private final String TAG = MailMsgListActivity.class.getSimpleName();
			RecyclerView mRecyclerView;
	MailListAdapter adapter;
	private DragBubbleView mDragBubble;
	private int ID = new Random(System.currentTimeMillis()).nextInt();
	public static void startMailMsgListActivity(Context context){
		Intent intent = new Intent(context ,MailMsgListActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mail_msg_list);

	}

	@Override
	protected void findViews() {
		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		mDragBubble = (DragBubbleView) findViewById(R.id.dragBubble);

		TextView title = (TextView) findViewById(R.id.text_title);
		title.setText(getString(R.string.email_control_helper));

		View rlBack = findViewById(R.id.left_back);
		rlBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				MailAssistantUtils.ClearMailAssistantConversation(MailMsgListActivity.this);
			}
		},1500);

	}

	@Override
	protected void init() {
		mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
		getLoaderManager().initLoader(ID, null, this);
	}

	public DragBubbleView getDragBubble() {
		return mDragBubble;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		LogF.v(TAG, "MailMsgListActivity onCreateLoader  id = " + id);
		CursorLoader loader = new CursorLoader(this, Conversations.MailAssistantConversation.CONTENT_URI, new String[] { "_id", "type", "status", "box_type", "date", "body", "person", "address",
				"unread_count","mail_count","attached_count" }, null, null, Conversations.DATE_DESC);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		List<MailAssistantConversation> list = new ArrayList<>();
		while (data.moveToNext()) {
			MailAssistantConversation mailAssistantConversation = null;
			String address = data.getString(data.getColumnIndex("address"));
			if (adapter != null) {
				mailAssistantConversation = adapter.getValue(address);
			}
			if (mailAssistantConversation == null)
				mailAssistantConversation = new MailAssistantConversation();
			mailAssistantConversation.setContent(data.getString(data.getColumnIndex("body")));
			int unread_count = data.getInt(data.getColumnIndex("unread_count"));
			mailAssistantConversation.setUnReadCount(unread_count);

			mailAssistantConversation.setAddress(address);
			mailAssistantConversation.setDate(data.getLong(data.getColumnIndex("date")));
			mailAssistantConversation.setType(data.getInt(data.getColumnIndex("type")));
			mailAssistantConversation.setStatus(data.getInt(data.getColumnIndex("status")));
			mailAssistantConversation.setMailCount(data.getInt(data.getColumnIndex("mail_count")));
			mailAssistantConversation.setAttachedCount(data.getString(data.getColumnIndex("attached_count")));
			LogF.v(TAG, "onLoadFinished  address = " + address);

			list.add(mailAssistantConversation);
		}

		if (adapter == null) {
			adapter = new MailListAdapter(MailMsgListActivity.this, list);
			mRecyclerView.setAdapter(adapter);
		}else{
			adapter.setData(list);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
