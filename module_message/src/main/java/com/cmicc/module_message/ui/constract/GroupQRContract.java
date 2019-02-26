package com.cmicc.module_message.ui.constract;

import com.chinamobile.app.yuliao_business.model.GroupQrImage;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

/**
 * Created by hwb on 2017/7/10.
 */

public interface GroupQRContract {
    interface View extends BaseView<Presenter> {
        void updateUIAfterQueryQR(GroupQrImage groupImage);
        void finishUI(String toastTest);
    }

    interface Presenter extends BasePresenter {
    }
}
