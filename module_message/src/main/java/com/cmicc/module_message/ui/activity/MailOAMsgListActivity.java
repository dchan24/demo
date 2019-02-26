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
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.MailOAConversation;
import com.chinamobile.app.yuliao_business.model.OAList;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.OAUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.adapter.MailOAListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dchan on 2018/1/24.
 * OA列表界面
 */

public class MailOAMsgListActivity extends BaseActivity implements LoaderCallbacks<Cursor> {
	private final String TAG = MailOAMsgListActivity.class.getSimpleName();
	RecyclerView mRecyclerView;
	MailOAListAdapter adapter;
	private String mAddress;
	private int boxtype;
	private int ID = new Random(System.currentTimeMillis()).nextInt();
	public static void startActivity(Context context ,String address ,int boxType){
		Intent intent = new Intent(context ,MailOAMsgListActivity.class);
		intent.putExtra("address", address);
		intent.putExtra("boxtype", boxType);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mail_oa_list);
	}

	@Override
	protected void findViews() {
		Intent intent = getIntent();
		mAddress = intent.getStringExtra("address");
		boxtype = intent.getIntExtra("boxtype", -1);
		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		TextView title = (TextView) findViewById(R.id.text_title);
		String addressTemp = mAddress;
		if (addressTemp.contains("@")) {
			addressTemp = addressTemp.substring(0, addressTemp.indexOf("@"));
		}
		OAList oa = OAUtils.getOA(MailOAMsgListActivity.this, addressTemp);
		if (oa != null) {
			String name = oa.getName();
			if (!TextUtils.isEmpty(name)) {
				title.setText(name);
			} else {
				title.setText(addressTemp);
			}
		}

		View rlBack = findViewById(R.id.left_back);
		rlBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(TextUtils.isEmpty(mAddress)){
					return;
				}
				ConversationUtils.updateUnreadCount(MailOAMsgListActivity.this ,mAddress);
			}
		},1500);

	}

	@Override
	protected void init() {

		mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
		getLoaderManager().initLoader(ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String where = null;
		if ((boxtype & Type.TYPE_BOX_OA) > 0) {
			where = String.format(" %s='%s' and %s='%s'", BaseModel.COLUMN_NAME_SEND_ADDRESS, mAddress, "box_type", Type.TYPE_BOX_OA);
		} else {
			where = String.format(" %s='%s' and %s='%s'", BaseModel.COLUMN_NAME_SEND_ADDRESS, mAddress, "box_type", Type.TYPE_BOX_MAIL_OA);
		}
		LogF.d(TAG, "MailMsgListActivity onCreateLoader  id = " + id+" where="+where);

		CursorLoader loader = new CursorLoader(this, Conversations.MailOAConversation.CONTENT_URI, new String[] { "_id", "type", "status", "box_type", "date", "body", "person", "address",
				"send_address", "unread_count", "mail_count", "attached_count", "from_number" }, where, null, Conversations.DATE_DESC);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		List<MailOAConversation> list = new ArrayList<>();
		while (data.moveToNext()) {
			MailOAConversation mailOAConversation = null;
			String address = data.getString(data.getColumnIndex("address"));
			String send_address = data.getString(data.getColumnIndex("send_address"));
			if (adapter != null) {
				mailOAConversation = adapter.getValue(address);
			}
			if (mailOAConversation == null)
				mailOAConversation = new MailOAConversation();
			mailOAConversation.setContent(data.getString(data.getColumnIndex("body")));
			int unread_count = data.getInt(data.getColumnIndex("unread_count"));
			mailOAConversation.setUnReadCount(unread_count);
			mailOAConversation.setAddress(address);
			mailOAConversation.setSendAddress(send_address);
			mailOAConversation.setDate(data.getLong(data.getColumnIndex("date")));
			mailOAConversation.setType(data.getInt(data.getColumnIndex("type")));
			mailOAConversation.setStatus(data.getInt(data.getColumnIndex("status")));
			mailOAConversation.setMailCount(data.getInt(data.getColumnIndex("mail_count")));
			mailOAConversation.setAttachedCount(data.getInt(data.getColumnIndex("attached_count")));
			mailOAConversation.setBoxType(data.getInt(data.getColumnIndex("box_type")));
			mailOAConversation.setFromNumber(data.getString(data.getColumnIndex("from_number")));
			LogF.d(TAG, "onLoadFinished  address = " + address + "body = " + data.getString(data.getColumnIndex("body")));

			list.add(mailOAConversation);
		}

		if (adapter == null) {
			adapter = new MailOAListAdapter(MailOAMsgListActivity.this, list);
			mRecyclerView.setAdapter(adapter);
		}else{
			adapter.setData(list);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
