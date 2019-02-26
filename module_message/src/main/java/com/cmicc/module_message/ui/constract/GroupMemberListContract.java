package com.cmicc.module_message.ui.constract;

import android.database.Cursor;

import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

/**
 * Created by Dchan on 2017/5/18.
 */

public interface GroupMemberListContract {
	interface View extends BaseView<Presenter> {

		void showGroupMembers(Cursor cursor);
		void showDeleteGroupMemberDialog(GroupMember groupMember);
	}

	interface Presenter extends BasePresenter {
		void loadGroupMember(String groupChatId);
		void clickItem(GroupMember groupMember);
		void longClickItem(GroupMember groupMember);
		void setGroupOwner(GroupMember owner);
		String getGroupName();
		String getGroupCard();
	}
}
