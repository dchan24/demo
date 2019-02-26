package com.cmicc.module_message.ui.activity;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.MailOA;
import com.chinamobile.app.yuliao_business.model.MailOAConversation;
import com.chinamobile.app.yuliao_business.model.OAList;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.BeanUtils;
import com.chinamobile.app.yuliao_business.util.BeanUtils.ColumnIndex;
import com.chinamobile.app.yuliao_business.util.OAUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.MailOASummaryAdapter;
import com.cmcc.cmrcs.android.ui.utils.MailOAUtils;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by Dchan on 2018/1/17.
 * 具体某个OA的列表界面
 */

public class MailOASummaryActivity extends BaseActivity implements LoaderCallbacks<Cursor> {
	private static final String TAG = "MailOASummaryActivity";
	RecyclerView recyclerView;
	private MailOASummaryAdapter mAdapter;
	private WrapContentLinearLayoutManager linearLayoutManager;

	private long loadTime = 0;
	private String address;
	private String send_address;
	private int box_type;

	private String name;
	private boolean isFirstLoad=true;
	private int mMessageLoadCount = 10;
	private int ID = new Random(System.currentTimeMillis()).nextInt();
	boolean isLoadMore = false;
	boolean mHasMore = true;
	public static void startMailOASummaryActivity(Context context , MailOAConversation conversation){
		int boxtype = conversation.getBoxType();
		Intent intent = new Intent(context ,MailOASummaryActivity.class);
		intent.putExtra("address" ,conversation.getAddress());
		intent.putExtra("send_address", conversation.getSendAddress());
		intent.putExtra("box_type",conversation.getBoxType());
		if ((boxtype & Type.TYPE_BOX_MAIL_OA) > 0) {
			intent.putExtra("from_number", conversation.getFromNumber());
		}
		intent.putExtra("boxtype", boxtype);
		context.startActivity(intent);
	}

	public static void startMailOASummaryActivity(Context context , Conversation conversation){
		int boxtype = conversation.getBoxType();
		Intent intent = new Intent(context ,MailOASummaryActivity.class);
		intent.putExtra("address" ,conversation.getAddress());
		intent.putExtra("send_address", conversation.getSendAddress());
		intent.putExtra("box_type",conversation.getBoxType());
		intent.putExtra("boxtype", boxtype);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mail_summary);
	}

	@Override
	protected void onStop() {
		super.onStop();
		MailOAUtils.updateSeenRead(MailOASummaryActivity.this,address,send_address,box_type);
	}

	private void loadMore(){
		isLoadMore = true;
		mMessageLoadCount += 10;
		getLoaderManager().restartLoader(ID, null, this);
	}

	@Override
	protected void findViews() {
		recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		recyclerView.addOnScrollListener(new OnScrollListener() {
			private int firstVisibleItem;
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				LogF.d(TAG ,"onScrollStateChanged:"+newState+",firstVisibleItem:"
						+firstVisibleItem+",mHasMore"+mHasMore+",isLoadMore"+isLoadMore);
				if (newState == SCROLL_STATE_IDLE) {
					if(firstVisibleItem <= 1 && firstVisibleItem >= 0 && mHasMore){
						if(!isLoadMore){
							loadMore();
						}
					}
				}

			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				LogF.d(TAG ,"onScrolled");
				firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
			}
		});
	}

	@Override
	protected void init() {
		linearLayoutManager = new WrapContentLinearLayoutManager(this);
		recyclerView.setLayoutManager(linearLayoutManager);

		address = getIntent().getStringExtra("address");
		loadTime = getIntent().getLongExtra("load_time",0);
		send_address = getIntent().getStringExtra("send_address");
		box_type = getIntent().getIntExtra("box_type",-1);

		//toolbar
		TextView title = (TextView) findViewById(R.id.text_title);
		title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
		OAList oa = OAUtils.getOA(MailOASummaryActivity.this, address);
		String name = "";
		if (oa != null) {
			name = oa.getName();
		}
		if(TextUtils.isEmpty(name)){
			name = address;
		}
		title.setText(name);

		View rlBack = findViewById(R.id.left_back);
		rlBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		SimpleContact contact = ContactsCache.getInstance().searchContactByNumber(address);
		if (contact != null) {
			name = contact.getName();
		} else {
			name = address;
		}
		LogF.i("xxx", "address and name is:" + address + "--" + name);
		String expression = "^((86)|(\\+86)){0,1}[1]{1}[0-9]{10}$";
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(address);
		if(matcher.matches()){
//			isNumber = true;
		}
		getLoaderManager().initLoader(ID, null, this);
//		boolean isFromNotify = getIntent().getBooleanExtra("isFromNotify",false);
//		if(isFromNotify){
//			MailAssistantUtils.updateSeen(MailSummaryActivity.this,address);
//			int urm = getIntent().getIntExtra("urm", 0);
//			BadgeUtil.subBadgeCount(MailSummaryActivity.this,urm);
//		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String where = String.format(Conversations.WHERE_ADDRESS_GROUP + " AND date>=%s", address, loadTime);
		String order = loadTime > 0 ? Conversations.DATE_DESC : String.format(Conversations.DATE_DESC_LIMIT, mMessageLoadCount);
		CursorLoader loader = new CursorLoader(this ,Conversations.MailOA.CONTENT_URI ,new String[]{BaseModel.COLUMN_NAME_ID ,BaseModel.COLUMN_NAME_BOX_TYPE
				,BaseModel.COLUMN_NAME_ADDRESS,BaseModel.COLUMN_NAME_BODY,BaseModel.COLUMN_NAME_DATE
				,BaseModel.COLUMN_NAME_TYPE,BaseModel.COLUMN_NAME_STATUS,BaseModel.COLUMN_NAME_MAILURL,BaseModel.COLUMN_NAME_MAILTITLE,BaseModel.COLUMN_NAME_MAILSUMMARY,
				BaseModel.COLUMN_NAME_FROMADDRESS,BaseModel.COLUMN_NAME_SENDTIME,BaseModel.COLUMN_NAME_ATTACHEDCOUNT,BaseModel.COLUMN_NAME_ACCESSNO,BaseModel.COLUMN_NAME_FROMNUMBER},where,null ,order);

//		CursorLoader loader = new CursorLoader(this, Conversations.MailOA.CONTENT_URI, new String[] { "_id", Type.TYPE_BOX_OA+" AS box_type", "msg_id", "msg_id_backup", "thread_id", "address", "person", "body", "date",
//				"type", "status", "read", "locked", "error_code", "ext_url", "ext_short_url", "ext_title", "ext_file_name", "ext_file_path", "ext_thumb_path", "ext_size_descript", "ext_file_size",
//				"ext_down_size", "seen", "send_address", BaseModel.COLUMN_NAME_PA_UUID, BaseModel.COLUMN_NAME_XML_CONTENT, BaseModel.COLUMN_NAME_TITLE, BaseModel.COLUMN_NAME_ANIM_ID,
//				BaseModel.COLUMN_NAME_AUTHOR, BaseModel.COLUMN_NAME_SUB_IMG_PATH, BaseModel.COLUMN_NAME_SUB_ORIGIN_LINK, BaseModel.COLUMN_NAME_SUB_SOURCE_LINK, BaseModel.COLUMN_NAME_SUB_TITLE,
//				BaseModel.COLUMN_NAME_SUB_URL, BaseModel.COLUMN_NAME_SUB_BODY, BaseModel.COLUMN_NAME_ADDRESS_ID, BaseModel.COLUMN_NAME_MESSAGE_RECEIPT, BaseModel.COLUMN_NAME_SHOW_SEND,
//				BaseModel.COLUMN_NAME_ACCESSNO,BaseModel.COLUMN_NAME_MAILURL,BaseModel.COLUMN_NAME_MAILTITLE,BaseModel.COLUMN_NAME_MAILSUMMARY }, where, null, order);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		List<MailOA> mMailList = new ArrayList<>();
		if (data != null && data.getCount()>0) {
			data.moveToLast();
//			String from = data.getString(data.getColumnIndex("from_address"));
//			LogF.i("xxx","from is:"+from);
//			if(from!=null && !from.endsWith("@139.com")){
//				callBtn.setVisibility(View.GONE);
//				msgBtn.setVisibility(View.GONE);
//			}
			do {
				MailOA mailOA = BeanUtils.fillBean(new ColumnIndex(data), data, MailOA.class);
//				MailOA mail = new MailOA();
//				mail.setAddress(address);
//				mail.setId(data.getLong(data.getColumnIndex("_id")));
//				mail.setBoxType(data.getInt(data.getColumnIndex("box_type")));
//				mail.setMsgId(data.getString(data.getColumnIndex("msg_id")));
//				mail.setThreadId(data.getString(data.getColumnIndex("thread_id")));
//				mail.setBoxType(data.getInt(data.getColumnIndex("box_type")));
//				mail.setBoxType(data.getInt(data.getColumnIndex("box_type")));
//				mail.setBoxType(data.getInt(data.getColumnIndex("box_type")));
//				mail.setBoxType(data.getInt(data.getColumnIndex("box_type")));
//				mail.setBoxType(data.getInt(data.getColumnIndex("box_type")));
//				mail.setBoxType(data.getInt(data.getColumnIndex("box_type")));
//				mail.setBoxType(data.getInt(data.getColumnIndex("box_type")));
//
//				mail.setMailId(data.getString(data.getColumnIndex("mail_id")));
//				mail.setMailTitle(data.getString(data.getColumnIndex("mail_title")));
//				mail.setMailSummary(data.getString(data.getColumnIndex("mail_summary")));
//				mail.setAttachedNameString(data.getString(data.getColumnIndex("attached_namestring")));
//				mail.setAttachedCount(data.getString(data.getColumnIndex("attached_count")));
//				mail.setDate(data.getLong(data.getColumnIndex("date")));
//				mail.setSendTime(data.getLong(data.getColumnIndex("send_time")));
				mMailList.add(mailOA);
			} while (data.moveToPrevious());

		}
		if (mAdapter == null) {
			mAdapter = new MailOASummaryAdapter(MailOASummaryActivity.this);
//			mAdapter.setCanLoadMore();
			mAdapter.setData(mMailList);
			recyclerView.setAdapter(mAdapter);
			linearLayoutManager.scrollToPositionWithOffset(linearLayoutManager.getItemCount() - 1, -40000);
		}else{
			LogF.d(TAG ,"load finish,adapter size:"+mAdapter.getDataList().size());
			int lastPosition = mAdapter.getItemCount();
			mAdapter.setData(mMailList);
			mAdapter.notifyDataSetChanged();
//			mAdapter.notifyItemRangeInserted(0,mMailList.size()-lastPosition);
			if(isLoadMore){
				if(mMailList.size()  < mMessageLoadCount){
					mHasMore = false;
				}
				isLoadMore = false;
				linearLayoutManager.scrollToPositionWithOffset(mMailList.size()-lastPosition , 0);
				LogF.d(TAG ,"load finish: lastPosition:"+lastPosition);
			}else{
				if(mMailList.size()  > mMessageLoadCount){
					mMessageLoadCount = mMailList.size();
				}
				linearLayoutManager.scrollToPositionWithOffset(linearLayoutManager.getItemCount() - 1, -40000);
			}
		}
		LogF.d(TAG ,"load finish: count"+mMailList.size()+",mHasMore："+mHasMore+",mMessageLoadCount:"+mMessageLoadCount);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

}
