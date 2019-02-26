package com.cmicc.module_message.ui.constract;

import android.content.Context;

import com.chinamobile.app.yuliao_business.model.Conversation;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

/**
 * Created by tigger on 2017/3/13.
 */

public interface ConvListContracts {

    interface View extends BaseView<Presenter> {
//        void updateListView(Cursor cursor);
        void showLoading(boolean isOfflineMsg);
        void hideLoading();
        void notifyDataSetChanged();
//        void showErrorNotice(String msg);
//        void hideErrorNotice();
        void showLoginNotice(String msg);
        void hideLoginNotice();
        void showPCOnline(boolean b);
    }

    interface Presenter extends BasePresenter {
        void openItem(Context context, Conversation conversation);
        void updateUnreadCount(final Context mContext, final Conversation conversation, int boxType);
        void reLoadConversations();
        boolean checkNet(final String url);
        void synSms();
        void synMms();
        void ondestory();
        void checkPcLoginState();
        void toMyDeviceActivity();
    }
}
