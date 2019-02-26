package com.cmicc.module_message.ui.constract;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;

/**
 * @anthor situ
 * @time 2017/6/21 11:24
 * @description 群管理页面
 */

public class GroupManageContract {
    public interface IView {

        void toast(CharSequence sequence);

        void toggleProgressDialog(boolean show);

    }

    public interface IPresenter extends BasePresenter {
        void transferGroup();

        void disbandGroup();

    }
}
