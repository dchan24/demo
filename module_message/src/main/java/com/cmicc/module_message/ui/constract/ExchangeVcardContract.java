package com.cmicc.module_message.ui.constract;


import com.chinamobile.icloud.im.sync.model.RawContact;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

/**
 * Created by tianshuai on 2017/7/20.
 */

public interface ExchangeVcardContract {
    interface View extends BaseView<Presenter> {
        void showInfo(RawContact rawContact);
    }

    interface Presenter extends BasePresenter {
        void buildEntries(String vcardString);
    }
}
