package com.cmicc.module_message.ui.constract;

import android.content.Intent;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;

/**
 * @anthor situ
 * @time 2017/6/5 11:20
 * @description 修改群名片界面
 */

public interface GroupCardContract {
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
