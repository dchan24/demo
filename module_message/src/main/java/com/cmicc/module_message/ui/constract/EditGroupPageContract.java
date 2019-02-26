package com.cmicc.module_message.ui.constract;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.contracts.ContactSelectContract;

/**
 * Created by tigger on 2017/5/18.
 */

public interface EditGroupPageContract {
    interface View extends BaseView<ContactSelectContract.Presenter> {
        void setPresenter(EditGroupPageContract.Presenter p);

        void dismissProgressDialog();

        void showReasonsFailureDialog(String message);
    }

    interface Presenter extends BasePresenter {
        void sendGroupInvite(int id, String pcSubject, String pcPartpList);
    }
}
