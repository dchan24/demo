package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulecontact.ContactProxy;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Employee;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_contact.model.BaseContact;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.utils.AndroidUtil;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.GroupStrangerActivity;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.model.GroupChatMemberListModel;
import com.cmcc.cmrcs.android.ui.model.impls.GroupChatMemberListModelImpl;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.GroupMemberListContract;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;


/**
 * Created by Dchan on 2017/5/18.
 */

public class GroupMemberListPresenter implements GroupMemberListContract.Presenter,GroupChatMemberListModel.GroupChatMemberLoadFinishCallback{
	private static final String TAG = GroupMemberListPresenter.class.getSimpleName();
	GroupChatMemberListModel mMember;
	Context mContext;
	GroupMemberListContract.View mView;
	LoaderManager mLoaderManager;
//	String mGroupOwner = "";
	GroupMember mGroupOwner = null;
	String mUser = "";
	private static List<Integer> sActions;
	private String mAddress; // 会话标识
	private String mGroupName; // 会话名
	private String mGroupCard; //群名片

	boolean isEPGroup = false;

	public GroupMemberListPresenter(Context context, GroupMemberListContract.View view, LoaderManager loaderManager){

		mMember = new GroupChatMemberListModelImpl();
		mContext = context;
		mView = view;
		mLoaderManager = loaderManager;
//		mUser = PhoneUtils.getMinMatchNumber((String) SharePreferenceUtils.getDBParam(context, CommonConstant.LOGINED_USER,""));
		mUser = PhoneUtils.getMinMatchNumber(LoginUtils.getInstance().getLoginUserName());
		Log.d(TAG ,"user :"+mUser);

		ArrayList<Integer> actions = new ArrayList<Integer>();
		actions.add(LogicActions.GROUP_CHAT_EXPELLING_PARTICIPANT_OK_CB);
		actions.add(LogicActions.GROUP_CHAT_EXPELLING_PARTICIPANT_FAIL_CB);
		actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_OK_CB);
		actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL_CB);
		actions.add(LogicActions.GROUP_CHAT_ERROR_EXPELLED);
		actions.add(LogicActions.GROUP_CHAT_ERROR_GONE);
		UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
	}

	@Override
	public void start() {

	}

	/**
	 * 处理添加成员或者删除成员
	 * @param data
	 * @param isAdd true为添加，false为删除
	 * @return
	 */
	public boolean addMemberToGroup(Intent data ,boolean isAdd) {
		if (!AndroidUtil.isNetworkConnected(mContext)) {
			Toast.makeText(mContext, mContext.getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
			return false;
		}
		if (data != null) {
			final StringBuilder sb = new StringBuilder();
			ArrayList<BaseContact> contactList = (ArrayList<BaseContact>) data.getSerializableExtra(ContactSelectorActivity.KEY_BASECONTACT_LIST);
			for (BaseContact contact : contactList) {
				String number = contact.getNumber();
				if (TextUtils.isEmpty(number)) {
					number = "";
				}
				sb.append(number).append(";");
			}
			if (sb.length() >= 1) {
				sb.deleteCharAt(sb.length() - 1);
			}
			String pcUri = sb.toString().trim();
			Log.d(TAG, "addMemberToGroup pcUri = " + pcUri+",GroupId:"+mGroupOwner.getGroupId());
			if(isAdd){
				GroupChatControl.rcsImSessAddPartp(mGroupOwner.getGroupId(), pcUri);
			}else{
				GroupChatControl.rcsImSessEplPartp(mGroupOwner.getGroupId(), pcUri);
			}


			return true;
		}
		return false;
	}

	public void isEPGroup(boolean isEPGroup){
		this.isEPGroup = isEPGroup;
	}

	public boolean getIsEPGroup(){
		return isEPGroup;
	}

	@Override
	public void onLoadFinished(Cursor cursor) {
		mView.showGroupMembers(cursor);
	}

	@Override
	public void loadGroupMember(String groupChatId) {
		mMember.loadGroupMembers(mContext ,mLoaderManager ,groupChatId,this);
		GroupInfo groupInfo = GroupChatUtils.getGroupInfo(mContext, groupChatId);
		mAddress=groupChatId;
		mGroupName = groupInfo.getPerson();
		mGroupCard="";
	}

	@Override
	public void clickItem(GroupMember groupMember) {
		Log.d(TAG, "mGroupName: "+mGroupName);
		if(groupMember == null || TextUtils.isEmpty(groupMember.getAddress())){
			return;
		}
		String loginNum= LoginDaoImpl.getInstance().queryLoginUser(mContext);
		loginNum = PhoneUtils.getMinMatchNumber(loginNum);
		SimpleContact simpleContact = ContactsCache.getInstance().searchContactByNumber(PhoneUtils.getMinMatchNumber(groupMember.getAddress()));
		if(TextUtils.equals(PhoneUtils.getMinMatchNumber(groupMember.getAddress()),loginNum)){
			//跳转到个人名片
			AboutMeProxy.g.getUiInterface().goToUserProfileActivity(mContext);
			return;
		}
		if(simpleContact != null){
			ContactProxy.g.getUiInterface().getContactDetailActivityUI()
					.showForSimpleContact(mContext ,simpleContact, 0);
		}else {
			String address = groupMember.getAddress() ;
			String strangerPhone = PhoneUtils.getMinMatchNumber(address);
			if(!TextUtils.isEmpty(strangerPhone)){
				if(isEPGroup){
					Employee employee = new Employee();
					employee.setRegMobile(strangerPhone);
					employee.setName(groupMember.getPerson());
					employee.setAddress(strangerPhone);
					ContactProxy.g.getUiInterface().getContactDetailActivityUI()
							.showForEmployee(mContext ,employee);
				}else{
					GroupStrangerActivity.show(mContext,strangerPhone , address , groupMember.getPerson(),mAddress,mGroupName,mGroupCard);
				}
			}
		}
	}

	@Override
	public void longClickItem(GroupMember groupMember) {
		String phoneGroupOwner = PhoneUtils.getMinMatchNumber(mGroupOwner.getAddress());
		String clickMember = PhoneUtils.getMinMatchNumber(groupMember.getAddress());
		Log.d(TAG ,"longClickItem,groupOwner:"+phoneGroupOwner+
				",mUser:"+mUser+",clickMember:"+clickMember);
		if(mGroupOwner == null || TextUtils.isEmpty(mUser) ||
				!phoneGroupOwner.equals(mUser)){//不是群主
			return;
		}
		if(mUser.equals(clickMember)){//群主也不能删除自己
			return;
		}
		mView.showDeleteGroupMemberDialog(groupMember);
	}

	public void setGroupOwner(GroupMember groupMember){
		Log.d(TAG ,"GroupOwner phone:"+groupMember.getAddress());
		//PhoneNumUtilsEx.PhoneCheckUtil.getMinMatchNumber(phone)
		mGroupOwner = groupMember;
	}

	private UIObserver mUIObserver = new UIObserver() {
		@Override
		protected void onReceiveAction(int action, Intent intent) {
			Log.d(TAG ,"receive action:"+action);
			switch (action){
				case LogicActions.GROUP_CHAT_EXPELLING_PARTICIPANT_OK_CB:
					Log.d(TAG ,"删除成功！");
					break;
				case LogicActions.GROUP_CHAT_EXPELLING_PARTICIPANT_FAIL_CB:
					Log.d(TAG ,"删除失败！");
					break;
				case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_OK_CB:
					Log.d(TAG ,"添加成功！");
					break;
				case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL_CB:
					Log.d(TAG ,"添加失败！");
					break;
				case LogicActions.GROUP_CHAT_ERROR_EXPELLED:
					new RxAsyncHelper("").runOnMainThread(new Func1() {
						@Override
						public Object call(Object o) {
							Toast.makeText(mContext ,mContext.getString(R.string.you_get_out_group),Toast.LENGTH_SHORT).show();
							((BaseActivity)mContext).finish();
							return null;
						}
					}).subscribe();

					break;
				case LogicActions.GROUP_CHAT_ERROR_GONE:
					new RxAsyncHelper("").runOnMainThread(new Func1() {
						@Override
						public Object call(Object o) {
							Toast.makeText(mContext ,mContext.getString(R.string.group_miss),Toast.LENGTH_SHORT).show();
							((BaseActivity)mContext).finish();
							return null;
						}
					}).subscribe();

					break;
			}
		}
	};

	@Override
	public String getGroupName() {
		return mGroupName;
	}

	@Override
	public String getGroupCard() {
		return mGroupCard;
	}
}
