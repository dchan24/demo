package com.cmicc.module_message.ui.constract;


import android.database.Cursor;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;

/**
 * Created by situ on 2017/3/30.
 */

public interface MessageSearchContract {

    interface IView {
        void updateResultListView(Cursor data, String keyword, int boxtype);

        void showEmptyView(boolean show);

        void showTextHint(boolean show);

        void switchToStaticMode(int count, String keyword, String title);

        void showOhterFileSearchView(boolean show);
    }

    interface IPresenter extends BasePresenter {

        void searchKeyword(String keyword);

        void openItem(Message message);

    }

}
