package com.cmicc.module_message.ui.constract;

import android.database.Cursor;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

/**
 * Created by tigger on 2017/5/23.
 */

public interface SysMsgContract {

    /**
     * 作用1：设置Presenter
     * 作用2：数据加载完成，更新列表的数据
     * 作用3：隐藏加载对话框
     */
    interface View extends BaseView<Presenter> {
        void setPresenter(SysMsgContract.Presenter p);

        void updateListView(Cursor cursor);

        void dismissProgressDialog();

    }

    /**
     * 作用1：加载数库中的系统消息
     * 作用2：跳转到群聊界面
     * 作用3：设置当前点击的群ID
     * 作用4：获取当前点击的群ID
     */
    interface Presenter extends BasePresenter {

        void gotoChat(String groupChatId, String person);

        void setConsentEntryGroupID(String gorupID);

        String getConsentEntryGroupID();
    }

}
