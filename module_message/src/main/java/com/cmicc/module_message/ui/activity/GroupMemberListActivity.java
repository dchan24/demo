package com.cmicc.module_message.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.module.proxys.modulecontact.ContactProxy;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.PinyinUtils;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.adapter.GroupMemberListAdapter;
import com.cmicc.module_message.ui.adapter.GroupMemberListAdapter.AdapterDataChangeListener;
import com.cmicc.module_message.ui.constract.GroupMemberListContract;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog.OnClickListener;
import com.cmicc.module_message.ui.presenter.GroupMemberListPresenter;
import com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.ui.view.GroupMemberOperationDialog;
import com.cmcc.cmrcs.android.ui.view.GroupMemberOperationDialog.OnOprationItemClickListener;
import com.cmcc.cmrcs.android.ui.view.contactlist.ExpIndexView;
import com.cmcc.cmrcs.android.ui.view.contactlist.ExpIndexView.OnIndexClickListener;
import com.cmcc.cmrcs.android.ui.view.contactlist.IndexBarView;
import com.cmcc.cmrcs.android.ui.view.contactlist.IndexBarView.OnIndexTouchListener;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.List;

import static com.constvalue.MessageModuleConst.GroupSettingActivity.REQUEST_CODE_ADD_MEMBER;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.REQUEST_CODE_DELETE_MEMBER;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_ADD_CONTACT_TO_GROUP;

/**
 * Created by Dchan on 2017/5/18.
 * 群成员列表
 */

public class GroupMemberListActivity extends BaseActivity implements GroupMemberListContract.View
		,AdapterDataChangeListener,OnIndexTouchListener,OnIndexClickListener{
	private static final String TAG = GroupMemberListActivity.class.getSimpleName();

	RecyclerView mRecyclerView;
	IndexBarView mIndexBarView;
	ExpIndexView mExpIndexView;
//	Toolbar mToolbar;

	GroupMemberListAdapter mAdapter;
	LinearLayoutManager mLinearLayoutManager;
	GroupMemberListPresenter mPresenter;

	ProgressDialog mDialog;
	private static final String GROUP_CHAT_ID = "group_chat_id";
	private static final String GROUP_LIST_MEMBER = "group_list_member";
	private static final String GROUP_LIST_MEMBER_STRING = "group_list_member_string";

	public static ArrayList<GroupMember> mListGroupMember = null;
	public static ArrayList<String> mListGroupMemberStr = null;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_member_list);
	}

	@Override
	protected void findViews() {
		mDialog = new ProgressDialog(this);
		mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mDialog.setMessage(getString(R.string.dialog_title_qr_wait));
		mDialog.show();
		mRecyclerView = (RecyclerView)findViewById(R.id.group_member_list);
		mIndexBarView = (IndexBarView)findViewById(R.id.values);
		mExpIndexView = (ExpIndexView)findViewById(R.id.contact_exp_index_view);
//		mToolbar = (Toolbar)findViewById(R.id.tb_group_member);
	}

	@Override
	protected void init() {
//		mToolbar.setTitle(getResources().getString(R.string.group_member_list_name));
//		setSupportActionBar(mToolbar);
//		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				finish();
//			}
//		});
		Intent intent = getIntent();
		LogF.d("a","time begin load:"+System.currentTimeMillis());
		mPresenter = new GroupMemberListPresenter(this ,this ,this.getSupportLoaderManager());
		mPresenter.loadGroupMember(intent.getStringExtra(GROUP_CHAT_ID));
		mPresenter.isEPGroup(intent.getBooleanExtra("isEPGroup",false));
		mLinearLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.addOnScrollListener(new ScrollListener());
		mAdapter = new GroupMemberListAdapter(this,mPresenter);
		if(mListGroupMember!= null && mListGroupMember.size() > 0){
			mPresenter.setGroupOwner(mListGroupMember.get(0));
			mAdapter.getDataList().addAll(mListGroupMember);
			mAdapter.getAllMemberInString().addAll(mListGroupMemberStr);
			mAdapter.sortData();
			mDialog.dismiss();
		}

		mAdapter.setOnAdapterDataChangeListener(this);
		mRecyclerView.setAdapter(mAdapter);
		mIndexBarView.setOnIndexTouchListener(this);
		mExpIndexView.setOnIndexClickListener(this);
	}


	@Override
	public void showGroupMembers(Cursor cursor) {
		mAdapter.setCursor(cursor);
	}

	@Override
	public void showDeleteGroupMemberDialog(final GroupMember groupMember) {
		String[] items = getResources().getStringArray(R.array.delete_group_member_item);
		final GroupMemberOperationDialog dialog = new GroupMemberOperationDialog(this ,groupMember.getPerson()
				,items ,groupMember.getAddress());
		dialog.setOnMessageItemClickListener(new OnOprationItemClickListener() {
			@Override
			public void onClick(String item, int which, String address) {
				if(which == 0){
					dialog.dismiss();
					showCommonDialog(groupMember);
				}else if(which == 1){
					dialog.dismiss();
					Intent intent = ContactProxy.g.getUiInterface().getContactSelectActivityUI()
							.createIntentForDeleteMemberInGroup(GroupMemberListActivity.this,groupMember.getGroupId()
									,mAdapter.getAllMemberInString(),groupMember.getAddress(),true);
					GroupMemberListActivity.this.startActivityForResult(intent ,REQUEST_CODE_DELETE_MEMBER);
				}
			}
		});
		dialog.show();

	}

	private void showCommonDialog(final GroupMember groupMember){
		final CommomDialog commomDialog = new CommomDialog(this ,null,getResources().getString(R.string.delete_group_member_tip ,groupMember.getPerson()));
		commomDialog.setOnNegativeClickListener(new OnClickListener() {
			@Override
			public void onClick() {
				commomDialog.dismiss();
			}
		});
		commomDialog.setOnPositiveClickListener(new OnClickListener() {
			@Override
			public void onClick() {
				GroupChatControl.rcsImSessEplPartp(groupMember.getGroupId() ,groupMember.getAddress());
				commomDialog.dismiss();
			}
		});
		commomDialog.show();
	}


	public static void openGroupMemberListActivity(Context context , String groupChatId ,ArrayList<GroupMember> groupMemberList ,ArrayList<String> listString,boolean isEPGroup){
		if(TextUtils.isEmpty(groupChatId)){
			return;
		}
		int size = groupMemberList.size();
//		String loginUser = (String) SharePreferenceUtils.getDBParam(MyApplication.getApplication(), CommonConstant.LOGINED_USER, "");
		String loginUser = LoginUtils.getInstance().getLoginUserName();
		for(int i=0 ;i<size;i++){
			GroupMember member = groupMemberList.get(i);
			String person = NickNameUtils.getNickNameWithoutDb(context ,member.getAddress() ,member.getGroupId() ,loginUser);
			if(!TextUtils.isEmpty(person)) {
				member.setPerson(person);
			}else{
				if(TextUtils.isEmpty(member.getPerson())){
					member.setPerson(NumberUtils.formatPersonStart(member.getAddress()));
				}
			}
			person = member.getPerson();
			if(person.length() > 0){
				String pinyin = PinyinUtils.getInstance(context).getPinyin(person);
				String pinyin2 = PinyinUtils.getInstance(context).getPinyinsString(person);
				if ( !"".equals(pinyin) ) {
					member.setPinyin(pinyin);
					member.setPinyin2(pinyin2);
					String sortString = pinyin.substring(0, 1).toUpperCase();
					if (sortString.matches("[A-Z]")) {
						member.setLetter(sortString);
					} else {
						member.setLetter("#");
					}
				}
			}
		}
		LogF.d("aba" ,"groupChatId---"+groupChatId);
		Intent intent = new Intent(context ,GroupMemberListActivity.class);
		GroupMemberListActivity.mListGroupMember = groupMemberList;
		GroupMemberListActivity.mListGroupMemberStr = listString;
		intent.putExtra(GROUP_CHAT_ID ,groupChatId);
		intent.putExtra("isEPGroup",isEPGroup);
//		intent.putExtra(GROUP_LIST_MEMBER ,groupMemberList);
//		intent.putStringArrayListExtra(GROUP_LIST_MEMBER_STRING ,listString);
		context.startActivity(intent);
	}

	@Override
	public void onDataChange() {
		LogF.d("aa" ,"onDataChange !");
		mDialog.dismiss();
		mIndexBarView.setVisibility(View.VISIBLE);
		mExpIndexView.setVisibility(View.GONE);
		mIndexBarView.setOnIndexTouchListener(indexTouchListener);
		mIndexBarView.setIndexWordHeightLight(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main ,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		/*if (i == R.id.action_search) {
			showSearchGroupMember();
			return true;
		} else */if (i == R.id.action_add_sms) {
			openAdd();
			return true;
		}
		return false;
	}

	private void openAdd() {
		ArrayList<String> list = new ArrayList<String>();
		List<GroupMember> groupMembers = mAdapter.getData();
		for (GroupMember member : groupMembers) {
			list.add(PhoneUtils.getMinMatchNumber(member.getAddress()));
		}
		int count = (mPresenter.getIsEPGroup()?2000:500)-(list==null?0:list.size());
		if (count<1) {
			return;
		}
		Intent intent = ContactSelectorActivity.creatIntent(this, SOURCE_ADD_CONTACT_TO_GROUP, count);
		if (list != null) {
			intent.putStringArrayListExtra(ContactSelectorUtil.SELECTED_NUMBERS_KEY, list);
		}
		this.startActivityForResult(intent, REQUEST_CODE_ADD_MEMBER);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LogF.d("aa", "onActivityResult requestCode = " + requestCode + " , resulteCode = " + resultCode);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
			case REQUEST_CODE_ADD_MEMBER:
				if(mPresenter.addMemberToGroup(data ,true)){
					Toast.makeText(GroupMemberListActivity.this ,getString(R.string.have_send_invitation),Toast.LENGTH_SHORT).show();
				}
				break;
			case REQUEST_CODE_DELETE_MEMBER:
				if(mPresenter.addMemberToGroup(data ,false)){
					LogF.d(TAG ,"已发送删除请求");
				}
				break;
		}
	}

	private void showSearchGroupMember(){
//		if(mHashMap == null){
//			mHashMap = new HashMap<>();
//			List<GroupMember> list = mAdapter.getData();
//			for(GroupMember groupMember : list){
//				mHashMap.put(groupMember.getPerson() ,groupMember.getAddress());
//			}
//		}
		ContactProxy.g.getUiInterface().newSearchActivityForGroupMember(this ,mPresenter.getGroupName(),
				mPresenter.getGroupCard() ,mPresenter.getIsEPGroup());
	}

	private IndexBarView.OnIndexTouchListener indexTouchListener = new IndexBarView.OnIndexTouchListener() {

		@Override
		public void onIndex(String arg0) {
			updateAlphabetIndex(arg0);
		}

		@Override
		public void onIndex(String word, int[] location) {

		}
	};

	private void updateAlphabetIndex(String word) {
		int index = getPositionForSection(word);
		if (index != -1) {
            mLinearLayoutManager.scrollToPositionWithOffset(index ,0);
            mIndexBarView.setIndexWordPosition(word);
        }
//		mExpIndexView.setTopOffset(location[1]);
		mExpIndexView.setVisibility(View.VISIBLE);
		mExpIndexView.show(word);
	}

	public int getPositionForSection(String arg0) {
		int size = mAdapter.getItemCount();
		for (int i = 0; i < size; i++) {
			String word = mAdapter.getItem(i).getLetter();
			if (arg0.equals(word)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onIndex(String word) {
		updateAlphabetIndex(word);
	}

	@Override
	public void onIndex(String word, int[] location) {

	}

	@Override
	public void onIndexClick(String word) {
		mExpIndexView.dismiss();
		setSelection(word);
	}

	@Override
	public void updateExpStation(String word) {
		if (TextUtils.isEmpty(word)){
			return ;
		}
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mExpIndexView.getLayoutParams();
		int i;
		if (!word.equalsIgnoreCase("#")) {
			char c = word.charAt(0);
			i = c - (char) 'A';
		}else{
			i = 26;
		}
		layoutParams.topMargin=i*mIndexBarView.getItemHeight()+ (int) AndroidUtil.dip2px(this,56);
	}

	/**
	 * 滚动到指定位置
	 *
	 * @param word
	 *            字母索引
	 */
	public void setSelection(String word) {
		Integer position = null;
		if (word.equalsIgnoreCase(mIndexBarView.getIndexWord(0))) {
			position = 0;
		} else {
			position = getPositionForSection(word);
		}

		if (position != null) {
			mLinearLayoutManager.scrollToPositionWithOffset(position ,0);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdapter.setClose(true);
	}

	private class ScrollListener extends OnScrollListener {
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
			if (firstVisibleItem >= 0) {
				mIndexBarView.setIndexWordPosition(mAdapter.getItem(firstVisibleItem).getLetter());
			}
		}
	}
}
