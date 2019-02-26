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
import android.view.View;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.MailAssistant;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.utils.CommonConstant;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.MailSummaryAdapter;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.utils.MailAssistantUtils;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

/**
 * Created by Dchan on 2018/1/17.
 * 具体某个邮箱的列表界面
 */

public class MailSummaryActivity extends BaseActivity implements LoaderCallbacks<Cursor> {
    private static final String TAG = MailSummaryActivity.class.getSimpleName();
    RecyclerView recyclerView;
    private MailSummaryAdapter mAdapter;
    private WrapContentLinearLayoutManager linearLayoutManager;

    private long loadTime = 0;
    private String address;
    private String name;
    private boolean isFirstLoad = true;
    private int mMessageLoadCount = 10;
    private int ID = new Random(System.currentTimeMillis()).nextInt();
    boolean isLoadMore = false;
    boolean mHasMore = true;

    public static void startMailSummaryActivity(Context context, String address) {
        Intent intent = new Intent(context, MailSummaryActivity.class);
        intent.putExtra("address", address);
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
        updateSeen();
    }

    private void updateSeen() {
//        List<MailAssistant> list = mAdapter.getDataList();
//        MailAssistant ma = list.get(list.size() - 1);
        if (!StringUtil.isEmpty(address)) {

            new RxAsyncHelper("").runInThread(new Func1() {
                @Override
                public Object call(Object o) {
                    Message msg = MailAssistantUtils.getLastMessage(MailSummaryActivity.this, address);
                    if (msg != null) {
                        if (address.contains("@139.com")) {
                            ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getTimestamp(), address, CommonConstant.MAILCHATTYPE);
                        } else {
                            ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getTimestamp(), address + "@139.com", CommonConstant.MAILCHATTYPE);
                        }
                    }
                    MailAssistantUtils.updateSeen(MailSummaryActivity.this, address);
                    return null;
                }
            }).subscribe();
        }
    }

    private void loadMore() {
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
                LogF.d(TAG, "onScrollStateChanged:" + newState + ",firstVisibleItem:"
                        + firstVisibleItem + ",mHasMore" + mHasMore + ",isLoadMore" + isLoadMore);
                if (firstVisibleItem <= 1 && firstVisibleItem >= 0 && mHasMore) {
                    if (!isLoadMore) {
                        loadMore();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LogF.d(TAG, "onScrolled");
                firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            }
        });
    }

    @Override
    protected void init() {
        linearLayoutManager = new WrapContentLinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        address = getIntent().getStringExtra("address");
        loadTime = getIntent().getLongExtra("load_time", 0);

        //toolbar
        TextView title = (TextView) findViewById(R.id.text_title);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setText(address);

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
        if (matcher.matches()) {
//			isNumber = true;
        }
        getLoaderManager().initLoader(ID, null, this);
//		boolean isFromNotify = getIntent().getBooleanExtra("isFromNotify",false);
//		if(isFromNotify){
//			MailAssistantUtils.updateSeen(MailSummaryActivity.this,address);
//			int urm = getIntent().getIntExtra("urm", 0);
//			BadgeUtil.subBadgeCount(MailSummaryActivity.this,urm);
//		}
        updateSeen();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where = String.format(Conversations.WHERE_ADDRESS_GROUP + " AND send_time>=%s", address, loadTime);
        String order = loadTime > 0 ? Conversations.SENDTIME_DESC : String.format(Conversations.SENDTIME_DESC_LIMIT, mMessageLoadCount);
        CursorLoader loader = new CursorLoader(this, Conversations.MailAssistant.CONTENT_URI,
                new String[]{"_id", "attached_namestring", "mail_summary", "mail_title", "date", "send_time", "mail_id", "attached_count", "from_address"}, where, null, order);
        LogF.d(TAG, "where:" + where + ",order:" + order);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<MailAssistant> mMailList = new ArrayList<>();
        if (data != null && data.getCount() > 0) {
            data.moveToLast();
            String from = data.getString(data.getColumnIndex("from_address"));
            LogF.i("xxx", "from is:" + from);
//			if(from!=null && !from.endsWith("@139.com")){
//				callBtn.setVisibility(View.GONE);
//				msgBtn.setVisibility(View.GONE);
//			}
            do {
                MailAssistant mail = new MailAssistant();
                mail.setAddress(address);
                mail.setId(data.getLong(data.getColumnIndex("_id")));
                mail.setMailId(data.getString(data.getColumnIndex("mail_id")));
                mail.setMailTitle(data.getString(data.getColumnIndex("mail_title")));
                mail.setMailSummary(data.getString(data.getColumnIndex("mail_summary")));
                mail.setAttachedNameString(data.getString(data.getColumnIndex("attached_namestring")));
                mail.setAttachedCount(data.getString(data.getColumnIndex("attached_count")));
                mail.setDate(data.getLong(data.getColumnIndex("date")));
                mail.setSendTime(data.getLong(data.getColumnIndex("send_time")));
                mMailList.add(mail);
            } while (data.moveToPrevious());

        }
        if (mAdapter == null) {
            mAdapter = new MailSummaryAdapter(MailSummaryActivity.this);
//			mAdapter.setCanLoadMore();
            mAdapter.setData(mMailList);
            recyclerView.setAdapter(mAdapter);
            linearLayoutManager.scrollToPositionWithOffset(linearLayoutManager.getItemCount() - 1, -40000);
        } else {
            LogF.d(TAG, "load finish,adapter size:" + mAdapter.getDataList().size());
            int lastPosition = mAdapter.getItemCount();
            mAdapter.setData(mMailList);
            mAdapter.notifyDataSetChanged();
            if (isLoadMore) {
                if (mMailList.size() < mMessageLoadCount) {
                    mHasMore = false;
                }
                isLoadMore = false;
                linearLayoutManager.scrollToPositionWithOffset(mMailList.size() - lastPosition, 0);
                LogF.d(TAG, "load finish: lastPosition:" + lastPosition);
            } else {
                if (mMailList.size() > mMessageLoadCount) {
                    mMessageLoadCount = mMailList.size();
                }
                linearLayoutManager.scrollToPositionWithOffset(linearLayoutManager.getItemCount() - 1, -40000);
            }
        }
        LogF.d(TAG, "load finish: count" + mMailList.size() + ",mHasMore：" + mHasMore + ",mMessageLoadCount:" + mMessageLoadCount);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
