package com.cmicc.module_message.ui.constract;

import android.content.Intent;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;

/**
 * @anthor situ
 * @time 2017/5/23 9:24
 * @description 修改群聊名称
 */

public interface GroupNameContract {
    interface IView {

        void showDelete(boolean show);

        void setSaveClickable(boolean clickable);

        void finish(Intent intent);

        void setEditText(String text);

        void toast(CharSequence sequence);

        void toggleProgressDialog(boolean show);

        void setTextColor(int color);

    }

    interface IPresenter extends BasePresenter {

        void save();

    }
}
